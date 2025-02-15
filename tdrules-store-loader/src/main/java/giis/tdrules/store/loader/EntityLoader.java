package giis.tdrules.store.loader;

import static giis.tdrules.model.shared.ModelUtil.safe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.model.shared.EntityTypes;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.gen.ConstraintFactory;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.shared.LoaderException;

/**
 * Loads entities as indicated by the DataGenerator
 */
class EntityLoader {
	private static final Logger log=LoggerFactory.getLogger(EntityLoader.class);

	enum GenType {
		GEN_DEFAULT, // Initial value, ignore if not changed
		SPEC_USER, // Use the value specified by the user
		GEN_HERE, // Generated during this process (by UidGen or an AttrGen)
		GEN_PKSRV, // The value is autogenerated by the backend
		SPEC_FKSYM, // Symbolic value specified by the user
		// Specific values for arrays
		ARRAY_PK, ARRAY_FK
	}

	/** 
	 * Represents each attribute that is being generated. Values here evolve during the generation process
	 */
	private class GeneratedAttribute {
		private GenType genType = GenType.GEN_DEFAULT;
		private TdAttribute attr; // Source attribute
		private String specValue = ""; // Specified value (if any)
		private String genValue = ""; // Generated value that must be sent to the data store
		private ConstraintFactory constraints; // this is null if no constraints
		private List<GeneratedAttribute> children = new ArrayList<>(); // if this is a composite, includes the details
		private GeneratedAttribute parent = null; // if this is part a composite, references to composite
		private boolean isArray = false; // to differentiate object entities from array entities

		public GeneratedAttribute(TdAttribute attr) {
			this.attr = attr;
		}

		public boolean isPrimitive() {
			return children.isEmpty();
		}

		public String getName() {
			return attr.getName();
		}

		// Full name of a composite (using postgres or OpenApi notation)
		public String getFullName(boolean useOaNotation) {
			if (this.parent == null)
				return attr.getName();
			else if (useOaNotation)
				return parent.getName() + "::" + attr.getName();
			else
				return "(" + parent.getName() + ")." + attr.getName();
		}

		public String toString() {
			return "Attribute: " + getName() + " specValue=" + specValue + " genValue=" + genValue + " genType=" + genType;
		}
	}
	
	private TdSchema schema;
	private LoaderConfig config;
	private SymbolStore symbols;
	
	EntityLoader(TdSchema schema, LoaderConfig config, SymbolStore symbols) {
		this.schema=schema;
		this.config=config;
		this.symbols=symbols;
	}

	/**
	 * Main entry point to load values of an entity, returns a string that represents the generated data
	 */
	String loadValues(String entity, String[] attrNames, String[] attrValues) {
		log.debug("loadValues: using attrGen: {}, uidGen: {}", config.attrGen.getClass().getName(), config.uidGen.getClass().getName());
		log.debug("loadValues: entity={} attrNames={} attrValues={}", entity,
				Arrays.deepToString(attrNames), // NOSONAR splitCsv ensures no null
				Arrays.deepToString(attrValues));
		
		TdEntity currentEntity = schema.getEntity(entity);
		// Array entities do store data, they have an special treatment
		boolean isArray = EntityTypes.DT_ARRAY.equals(currentEntity.getEntitytype());
		config.attrGen.resetAttrCount(); // for deterministic generation

		// Generation is done in several stages, the list of generated attributes tracks
		// what must be done at each stage and stores the generated values that will be stored.
		// Here each generated attribute is initialized
		List<GeneratedAttribute> gattrs = new ArrayList<>();
		for (TdAttribute attr : safe(currentEntity.getAttributes()))
			initializeGeneratedAttribute(null, gattrs, attr, currentEntity);

		// Configures the attributes with specified values or symbols
		setSpecifiedValues(gattrs, entity, attrNames, attrValues);
		if (isArray)
			setArrayKeyAttributes(gattrs);

		// Generation of all values, that are stored in the generated attribute objects
		setGeneratedValues(0, gattrs, entity);

		// Send the data to the data store through the data adapter
		String generatedString = writeGeneratedValues(isArray, config.dataAdapter, gattrs, entity);

		// After sending all data, backend generated values are known and their values added to the symols
		setSymbolValues(config.dataAdapter, gattrs, entity);

		config.attrGen.incrementCount(); // for deterministic generation
		return generatedString;
	}
	
	private void initializeGeneratedAttribute(GeneratedAttribute parent, List<GeneratedAttribute> gattrs,
			TdAttribute attr, TdEntity entity) {
		GeneratedAttribute thisAttr = new GeneratedAttribute(attr);
		thisAttr.parent = parent; // if is part of a composite
		thisAttr.constraints = new ConstraintFactory(config.dataAdapter, entity, attr);
		gattrs.add(thisAttr);
		// If it is a type, the attribute has children that must be associated now.
		// If it is an array, marks this to have an special treatment
		if (EntityTypes.DT_TYPE.equals(attr.getCompositetype())) {
			TdEntity type = schema.getEntity(attr.getDatatype());
			for (TdAttribute subfield : type.getAttributes())
				initializeGeneratedAttribute(thisAttr, thisAttr.children, subfield, type);
		} else if (EntityTypes.DT_ARRAY.equals(attr.getCompositetype())) {
			thisAttr.isArray = true;
		}
	}
	
	private void setSpecifiedValues(List<GeneratedAttribute> gattrs, String entity, String[] attrNames, String[] attrValues) {
		for (int i = 0; i < attrNames.length; i++) {
			if (!attrNames[i].trim().equals("")) {
				log.trace("setSpecifiedValues: attrName={} attrValue={}", attrNames[i], attrValues[i]);
				GeneratedAttribute gattr = findGeneratedAttribute(gattrs, attrNames[i]);
				log.trace("setSpecifiedValues:   before: {}", gattr);
				setSpecifiedValuesForAttribute(gattr, entity, attrValues[i]);
				log.trace("setSpecifiedValues:    after: {}", gattr);
			}
		}
	}
	private void setSpecifiedValuesForAttribute(GeneratedAttribute gattr, String entity, String attrValue) {
		gattr.specValue = attrValue;
		if (symbols.isSymbol(attrValue)) {
			// The specified value is symbolic, it is a uid that will store its value in a symbol
			// or a rid that will use a previously stored value.
			// Note that if is both uid and rid, it must use a previously stored value
			// #tdrules-oa-rp#29)
			if (gattr.attr.isUid() && !gattr.attr.isRid())
				gattr.genType = GenType.GEN_PKSRV; // backend generated uid
			else
				gattr.genType = GenType.SPEC_FKSYM; // rid with symbolic value of a uid
		} else {
			gattr.genType = GenType.SPEC_USER;
			// Even if specified, when using dictionaries it is possible some additional change because of collisions
			gattr.genValue = config.attrGen.transformSpecValue(entity, gattr.attr.getName(), attrValue);
		}
		// If composite, as a value has been specified, remove descendants
		if (!gattr.isPrimitive())
			gattr.children.clear();
	}
	
	private void setArrayKeyAttributes(List<GeneratedAttribute> gattrs) {
		for (GeneratedAttribute gattr : gattrs) {
			if (OaExtensions.ARRAY_PK.equals(gattr.attr.getName()))
				gattr.genType = GenType.ARRAY_PK;
			else if (OaExtensions.ARRAY_FK.equals(gattr.attr.getName()))
				gattr.genType = GenType.ARRAY_FK;
		}
	}

	private void setGeneratedValues(int depth, List<GeneratedAttribute> gattrs, String entity) {
		for (GeneratedAttribute gc : gattrs)
			mainSetGeneratedValue(depth, gc, entity);
	}

	private void mainSetGeneratedValue(int depth, GeneratedAttribute ga, String entity) {
		// Depending on the generation type, sets the generated falue or changes the generation type
		if (ga.genType == GenType.GEN_DEFAULT) {
			// Not specified, if it is not nullable and there is no default value, must generate a value
			mainSetGeneratedValueDefault(depth, ga, entity);
		} else if (ga.genType == GenType.GEN_PKSRV) {
			// A new uid must be generated now (not backend generated), only if the key gen indicates this
			mainSetGeneratedValuePkSrv(ga, entity);
		} else if (ga.genType == GenType.SPEC_FKSYM) {
			// A rid with a symbol, uses the symbol value that has been previously generated
			ga.genValue = symbols.getValue(ga.attr.getRid(), ga.specValue);
		} else if (ga.genType == GenType.ARRAY_PK) {
			// Arrays are transformed form OpenApi into entities and an uid has been created
			// to track each item. Although the user has not specified a symbol, a sequential
			// value must be generated
			String uid = config.arrayUidGen.getNew(entity, ga.getName());
			log.trace("setGeneratedValue: Getting array uid: {}", uid);
			ga.genValue = uid;
		}
		config.attrGen.incrementAttrCount(); // for deterministic generation 
		log.trace("setGeneratedValue: {}", ga);
	}
	private void mainSetGeneratedValueDefault(int depth, GeneratedAttribute ga, String entity) {
		if (!ga.attr.isNullable() && !ga.attr.hasDefaultvalue()
				|| config.genNullable && ga.attr.isNullable() && !ga.attr.isRid()
				|| config.genDefault && ga.attr.hasDefaultvalue() && !ga.attr.isRid()) {
			ga.genType = GenType.GEN_HERE;
			if (ga.isPrimitive()) {
				// Do not generate data when it is a composite that references an object with an uid
				// (this is because in this case, the values are given by the reference object)
				ga.genValue = isInCompositeTypeWithFk(ga) ? "" : generateAttributeValue(ga.constraints, entity, ga.attr);
			} else {
				log.debug("  generate composite: entity={} children={}", ga.getName(), ga.children);
				setGeneratedValues(depth + 1, ga.children, ga.getName());
				log.trace("  end generate composite: entity={}", ga.getName());
			}
		}
	}
	private void mainSetGeneratedValuePkSrv(GeneratedAttribute ga, String entity) {
		String generated = config.uidGen.getNew(entity, ga.getName());
		if (generated != null) {
			ga.genValue = generated;
			// as a value has been generated, changes the generation type
			ga.genType = GenType.GEN_HERE; 
		}
	}
	
	private boolean isInCompositeTypeWithFk(GeneratedAttribute ga) {
		if (ga.parent == null)
			return false;
		String compositeTypeName = ga.parent.attr.getDatatype();
		TdEntity compositeType = this.schema.getEntity(compositeTypeName);
		return compositeType.getUniqueRids().size() > 0;
	}

	private String writeGeneratedValues(boolean isArray, IDataAdapter adapter, List<GeneratedAttribute> gattrs, String entity) {
		if (!isArray) { // primitive attribute and type
			return mainWriteGeneratedValues(adapter, gattrs, entity);
		} else {
			// The values of arrays cant be written now because the must be sent with the entity that contains the array.
			// Stores the values in a symbol
			String generatedArrayObject = mainWriteGeneratedValues(adapter.getNewLocalAdapter(), gattrs, entity);
			GeneratedAttribute ga = findGeneratedAttribute(gattrs, OaExtensions.ARRAY_FK);
			symbols.addArrayItem(entity, OaExtensions.ARRAY_FK, ga.specValue, generatedArrayObject);
			return generatedArrayObject;
		}
	}
	
	private String mainWriteGeneratedValues(IDataAdapter adapter, List<GeneratedAttribute> gattrs, String entity) {
		adapter.beginWrite(entity);
		for (GeneratedAttribute gc : gattrs) // only if specified or generated value
			if (gc.genType == GenType.SPEC_USER || gc.genType == GenType.GEN_HERE 
					|| gc.genType == GenType.SPEC_FKSYM || gc.genType == GenType.ARRAY_PK) {
				mainWriteGeneratedValueForAttribute(adapter, gattrs, entity, gc);
			}
		adapter.endWrite();
		config.uidGen.setLastResponse(entity, adapter.getLastResponse());
		return adapter.getLast();
	}
	private void mainWriteGeneratedValueForAttribute(IDataAdapter adapter, List<GeneratedAttribute> gattrs, String entity, GeneratedAttribute gc) {
		if (gc.isPrimitive() && !gc.isArray) {
			String val = gc.genValue == null || "null".equalsIgnoreCase(gc.genValue) ? null : gc.genValue;
			if (!gc.attr.isReadonly())
				adapter.writeValue(gc.attr.getDatatype(), gc.getName(), val);
		} else if (!gc.isArray) {
			// if it is a type, generates the children using another data adapter instance.
			// Note that the adapter must be local because no data must be sent now trough the api,
			// data is sent as part of the entity that contains the composite
			IDataAdapter clon = adapter.getNewLocalAdapter();
			setGeneratedValues(0, gattrs, entity);
			mainWriteGeneratedValues(clon, gc.children, gc.getName());
			writeCompositeType(adapter, gc, clon.getLast());
		} else {
			// Array, the values were  generated previously and stored in a symbol
			GeneratedAttribute gkey = gattrs.get(0);
			List<String> arrayValues = symbols.getArray(gc.attr.getDatatype(), OaExtensions.ARRAY_FK, gkey.specValue);
			arrayValues = arrayValues == null ? new ArrayList<>() : arrayValues; // avoid null if no values
			log.debug("writeGeneratedValues: Array values, attribute: {}={}", gc.getName(), arrayValues);
			// Different handling of object and primitive arrays, adds a prefix to the data type
			String dataType = ("object".equals(gc.attr.getSubtype()) ? "object" : "primitive") + EntityTypes.DT_ARRAY;
			if (!gc.attr.isReadonly())
				adapter.writeValue(dataType, gc.getName(), arrayValues.toString());
		}
	}
	
	private void writeCompositeType(IDataAdapter adapter, GeneratedAttribute gc, String value) {
		// If the generated value must be null, the values for children that have been generated
		// must be discarded by setting the entire attribute to null
		if (gc.attr.isNullable() && config.attrGen.isRandomNull(config.genNullProbability))
			value = null;
		// If the composite has a rid with a symbolic value, the generated data is the contained
		// in the referenced entity, and must mach with the generated composite
		// to do not loose the referencial integrity
		for (GeneratedAttribute pgc : gc.children)
			if (pgc.attr.isRid() && pgc.genType == GenType.SPEC_FKSYM && symbols.isSymbol(pgc.specValue))
				value = symbols.getObject(pgc.attr.getRid(), pgc.specValue);
		
		// Data can be writen now
		if (!gc.attr.isReadonly()) {
			log.debug("writeCompositeType: Object value, attribute: {}={}", gc.attr.getName(), value);
			adapter.writeValue(EntityTypes.DT_TYPE, gc.getName(), value);
		}
	}
		
	private void setSymbolValues(IDataAdapter adapter, List<GeneratedAttribute> gattrs, String entity) {
		for (GeneratedAttribute gc : gattrs) {
			// if the uid has been generated either in the backend or frontend
			if (gc.genType==GenType.GEN_PKSRV || gc.genType==GenType.GEN_HERE && symbols.isSymbol(gc.specValue)) {
				String genValue=config.uidGen.getLast(entity, gc.getName());
				symbols.setValue(entity, gc.getName(), gc.specValue, genValue);
				
				// Creates an additional symbol with the full object generate by the adapter
				if (!"".equals(adapter.getLastResponse()))
					symbols.setObject(entity, gc.getName(), gc.specValue, adapter.getLastResponse());
				else
					symbols.setObject(entity, gc.getName(), gc.specValue, adapter.getLast());
			}
		}
	}

	/**
	 * Generates the appropriate value for each type of attribute
	 */
	private String generateAttributeValue(ConstraintFactory constraintFactory, String entity, TdAttribute attr) {
		// Generate nulls when appropriate
		if (attr.isNullable() && config.attrGen.isRandomNull(config.genNullProbability))
			return "NULL";
		
		String value;
		if ("".equals(attr.getCheckin())) { // normal generation
			value = getValueForDatatype(constraintFactory, entity, attr);
		} else { // an element in an enumeration
			String c = trimBrackets(attr.getCheckin());
			String[] allowedValues = splitCsv(c);
			value = config.attrGen.generateCheckInConstraint(allowedValues);
		}
		log.trace("generateAttributeValue: Entity: {} Attribute: {} Value: {}", entity, attr.getName(), value);
		return value;
	}
	private String getValueForDatatype(ConstraintFactory constraintFactory, String entity, TdAttribute attr) {
		String[] sizes = splitCsv(attr.getSize());
		int precision = sizes.length > 0 ? Integer.parseInt(sizes[0]) : 0;
		String value;
		if (config.dataAdapter.isString(attr.getDatatype()) // Characters
				|| config.dataAdapter.isFreeFormObject(attr.getDatatype())) 
			value = config.attrGen.generateString(entity, attr.getName(), precision);
		else if (config.dataAdapter.isBoolean(attr.getDatatype())) // Booleans
			value = config.attrGen.generateBoolean() ? "true" : "false";
		else if (config.dataAdapter.isDate(attr.getDatatype())) // Dates
			value = config.attrGen.generateDate();
		else if (config.dataAdapter.isNumber(attr.getDatatype())
				&& config.dataAdapter.hasDecimals(attr.getDatatype(), attr.getSize())) {
			// Numbers with decimals or floats are generated using the sma procedure that for integers,
			// but everything is scaled go obtain decimals. NOTE: Only one decimal in all cases
			value = config.attrGen.generateNumber(constraintFactory.getConstraint(), entity, attr.getName());
			value = String.valueOf(Double.parseDouble(value) / 10.0).replace(",", ".");
		} else // integer or any other
			value = config.attrGen.generateNumber(constraintFactory.getConstraint(), entity, attr.getName());
		return value;
	}
	
	// Find generated attribute by name in a list
	private GeneratedAttribute findGeneratedAttribute(List<GeneratedAttribute> gattrs, String name) {
		name = name.trim();
		GeneratedAttribute attr = findGeneratedAttributeRecursive(gattrs, name);
		if (attr != null)
			return attr;
		throw new LoaderException("findGeneratedAttribute: Attribute " + name + " does not exist");
	}

	private GeneratedAttribute findGeneratedAttributeRecursive(List<GeneratedAttribute> gattrs, String name) {
		for (int i = 0; i < gattrs.size(); i++)
			// Checks the composite notation (openapi or postgres)
			if (gattrs.get(i).getFullName(true).equalsIgnoreCase(name)) {
				return gattrs.get(i);
			} else if (gattrs.get(i).getFullName(false).equalsIgnoreCase(name)) {
				return gattrs.get(i);
			} else if (!gattrs.get(i).isPrimitive()) { // composite, look in children
				GeneratedAttribute gattr = findGeneratedAttributeRecursive(gattrs.get(i).children, name);
				if (gattr != null)
					return gattr;
			}
		return null;
	}
	
	// Utilities
	
	private String trimBrackets(String c) {
		c = c.trim();
		c = c.startsWith("(") ? c.substring(1, c.length()) : c;
		c = c.endsWith(")") ? c.substring(0, c.length()-1) : c;
		return c.trim();
	}
	
	static String[] splitCsv(String csv) {
		if (csv==null || "".equals(csv)) //si csv vacio asegura devolver un array sin elementos
			return new String[] {};
		String[] arr=csv.split(",");
		for (int i=0; i<arr.length; i++)
			if (arr[i]!=null)
				arr[i]=arr[i].trim();
		return arr;
	}
}
