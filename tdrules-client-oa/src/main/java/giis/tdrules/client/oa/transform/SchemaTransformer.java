package giis.tdrules.client.oa.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.client.oa.shared.OaSchemaLogger;
import giis.tdrules.client.oa.shared.OaUtil;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Converts a previously parsed OpenApi schema specification into the TbSchema
 * model needed to generate FPC Coverage Rules
 */
@SuppressWarnings("rawtypes")
public class SchemaTransformer {
	protected static final Logger log = LoggerFactory.getLogger(SchemaTransformer.class);

	private Map<String, Schema> oaSchemas; // parsed OpenApi source for component/schemas
	private PathTransformer pathTransformer; // Preprocessed info about entities and paths
	private TdSchema tdSchema; // target schema
	private CompositeTransformer ct; // manages transformation of composites (objects and arrays)
	private UpstreamAttribute upstreamAttr;
	private OaSchemaLogger oaLogger; // specialized logger to store important messages
	
	// Scope configuration parameters
	private boolean onlyEntitiesInPaths;
	private List<String> onlyEntitiesInSelection;
	private boolean excludeVisitedNotInScope;
	private Map<String, Schema> entitiesToInclude;
	private Map<String, Schema> entitiesToExclude;

	public SchemaTransformer(Map<String, Schema> oaSchemas, PathTransformer pathTransformer, OaSchemaLogger oaLogger,
			boolean onlyEntitiesInPaths, String[] onlyEntitiesInSelection, boolean excludeVisitedNotInScope) {
		this.oaSchemas = oaSchemas;
		this.pathTransformer = pathTransformer;
		this.oaLogger = oaLogger;
		this.ct = new CompositeTransformer(this);
		this.tdSchema = new TdSchema().storetype("openapi");
		this.upstreamAttr = new UpstreamAttribute(this.tdSchema);
		// scope configuration
		this.onlyEntitiesInPaths = onlyEntitiesInPaths; // remember for further use during transform
		this.excludeVisitedNotInScope = excludeVisitedNotInScope;
		String[] selection = onlyEntitiesInSelection == null ? new String[0] : onlyEntitiesInSelection;
		this.onlyEntitiesInSelection = Arrays.asList(selection);
		this.configureScope();
	}
	private void configureScope() {
		entitiesToInclude = new LinkedHashMap<>();
		entitiesToExclude = new LinkedHashMap<>();
		for (Entry<String, Schema> entry : OaUtil.safe(oaSchemas).entrySet()) {
			// by default, all entities in schema, apply succesive filters if set
			boolean include = true;
			String entity = entry.getKey();
			Schema<?> oaSchema = entry.getValue();
			if (onlyEntitiesInPaths && !pathTransformer.containsEntity(entity)) {
				log.trace("Skip OA schema object: {} as it is not in any relevant path", entity);
				include = false;
			} else if (!onlyEntitiesInSelection.isEmpty() && !onlyEntitiesInSelection.contains(entity)) {
				log.trace("Skip OA schema object: {} as it is not in selected entities", entity);
				include = false;
			}
			if (include)
				entitiesToInclude.put(entity, oaSchema);
			// do not process visited entities out in scope (opt-in)
			if (!include && excludeVisitedNotInScope)
				entitiesToExclude.put(entity, oaSchema);
		}		
	}

	/**
	 * Gets the TdSchema object that has been converted
	 */
	public TdSchema getTdSchema() {
		return this.tdSchema;
	}
	
	OaSchemaLogger getOaLogger() {
		return this.oaLogger;
	}
	
	Map<String, Schema> getOaSchemas() {
		return this.oaSchemas;
	}
	
	/**
	 * Main internal entrypoint to transform a map of OpenApi schema objects to the
	 * TdSchema model (to be called from the client api)
	 */
	public SchemaTransformer transform() {
		for (Entry<String, Schema> oaSchema : entitiesToInclude.entrySet()) {
			log.debug("Transform OA schema object: {}", oaSchema.getKey());
			TdEntity entity = getEntity(oaSchema.getKey(), oaSchema.getValue(), pathTransformer, null);
			// This entity must store the Ddls that indicate the paths, if any
			pathTransformer.addDdls(entity, "post");
			pathTransformer.addDdls(entity, "put");
			addEntity(entity);
		}
		// When an array is created, the rid can't be determined if his immediate upstream is a nested object,
		// and a recursive search on all upstreams can't be made because upstream entities are created later.
		// This second step gets the rid of arrays that may be missing
		for (TdEntity entity : tdSchema.getEntities())
			if (entity.isArray() && entity.getRids().isEmpty())
				fillMissingRid(entity);
		// Remove the extended upstream attribute, as it is not part of the current model
		for (TdEntity entity : tdSchema.getEntities())
			upstreamAttr.removeUpstream(entity);
		return this;
	}

	// This is called in a second main step to set rids that where not found
	// in the immediate upstream of the array at the array creation
	void fillMissingRid(TdEntity entity) {
		log.debug("Try to link to missing Rid for array: {}", entity.getName());
		TdEntity upstreamWithUid = upstreamAttr.findUpstreamWithUid(entity);
		if (upstreamWithUid == null)
			oaLogger.warn(log, "Can't get the rid for array {} because it has not any upstream with uid", entity.getName());
		else {
			ct.linkArrayToContainerEntity(entity, upstreamWithUid);
		}
	}

	TdEntity getEntity(String name, Schema<?> oaSchema, PathTransformer pathTransformer, TdEntity upstream) {
		TdEntity entity = createNewEntity(name, upstream);
		Map<String, Schema> oaAttributes = oaSchema.getProperties();
		if (oaAttributes == null) {
			oaLogger.warn(log, "Open Api schema for {} does not have any property, generated entity will be empty", name);
			return entity;
		}
		// Gitlab rp issue #12: As the processing of each attribute is recursive,
		// if an object (eg. array), is declared before the uid of the entity,
		// when creating the new entity for this object, the uid couldn't be found.
		// Process in order: first the uids, next the rids and finally other attributes
		List<String> uids = new ArrayList<>();
		List<String> rids = new ArrayList<>();
		this.collectAttributeIds(oaAttributes, uids, rids);
		for (Entry<String, Schema> oaAttribute : oaAttributes.entrySet())
			if (uids.contains(oaAttribute.getKey()))
				addAttribute(oaAttribute, entity);
		
		for (Entry<String, Schema> oaProperty : oaAttributes.entrySet())
			if (!uids.contains(oaProperty.getKey()) && rids.contains(oaProperty.getKey()))
				addAttribute(oaProperty, entity);
		
		// create an additional attribute for each post path parameter that is rid
		// (only if this rid is not already in the entity)
		addPathAttributes(pathTransformer, entity);
		
		for (Entry<String, Schema> oaProperty : oaAttributes.entrySet())
			if (!uids.contains(oaProperty.getKey()) && !rids.contains(oaProperty.getKey()))
				addAttribute(oaProperty, entity);

		// additionalProperties are handled as an array
		if (oaSchema.getAdditionalProperties() != null) {
			log.debug("Processing additionalProperties as an array");
			addAdditionalAttributes(oaSchema.getAdditionalProperties(), entity);
		}
		
		return entity;
	}
	
	void addEntity(TdEntity entity) {
		log.trace("*Add entity if does not exists {}", entity.getName());
		tdSchema.addEntitiesItemIfNotExist(entity);
	}
	// Issue #361:
	// This variant is used to add an entity that is visited during transformation
	// (not from the main loop that explores all definitions in the schema).
	// These entities would not be added to the model when generating only entities in paths.
	// The visited entities are included at the moment of the visit only when processing
	// entities in paths for compatibility with the ordering of other tests.
	void addVisitedEntity(TdEntity entity) {
		if (onlyEntitiesInPaths || !onlyEntitiesInSelection.isEmpty()) {
			log.trace("*Add visited entity (not in path) if does not exists {}", entity.getName());
			tdSchema.addEntitiesItemIfNotExist(entity);
		}
	}

	// returns a new empty entity (initially as object entity)
	TdEntity createNewEntity(String name, TdEntity upstream) {
		String upstreamName = upstream == null ? "null" : upstream.getName();
		log.debug("Entity: {}, Upstream: {}", name, upstreamName);
		TdEntity entity = new TdEntity().name(name).setObject();
		upstreamAttr.setUpstream(entity, upstreamName);
		return entity;
	}

	private void addAttribute(Entry<String, Schema> oaAttribute, TdEntity entity) {
		TdAttribute attribute = createNewAttribute(oaAttribute.getKey(), oaAttribute.getValue(), entity);
		if (attribute != null) // if null, something happened preventing the appropriate creation of the attribute
			entity.addAttributesItem(attribute);
	}
	
	// To add additionalAttributes: creates an object array where the items 
	// are of the same type as the additional properties
	private void addAdditionalAttributes(Object additionalProperties, TdEntity entity) {
		// A definition of additionalProperties true must be converted into an array of free form objects.
		if (additionalProperties instanceof Boolean && additionalProperties == Boolean.TRUE) {
			additionalProperties = new ArraySchema().type(OaExtensions.FREE_FORM_OBJECT);
		} // false is ignored
		
		// General form converted into an array
		if (additionalProperties instanceof Schema<?>) {
			ArraySchema oaArray = new ArraySchema();
			oaArray.setItems((Schema<?>) additionalProperties);
			
			// If array items have neither type nor ref, it is a definition of additionalProperties empty
			// that must be converted into an array of free form objects.
			Schema<?> items = ((ArraySchema) oaArray).getItems();
			if (items.getType() == null && items.get$ref() == null) {
				oaArray.getItems().setType(OaExtensions.FREE_FORM_OBJECT);
			}
			// Create a new attribute to convert the additional properties into an array
			TdAttribute attribute = createNewAttribute(OaExtensions.ADDITIONAL_PROPERTIES, oaArray, entity);
			if (attribute != null)
				entity.addAttributesItem(attribute);
		}
	}

	TdAttribute createNewAttribute(String name, Schema<?> oaProperty, TdEntity entity) {
		log.trace("  property: {}", name);
		TdAttribute attribute = new TdAttribute().name(name);
		// Sets the type of the attribute and proceeds recursively for nestings.
		// If returning false, the attribute couldn't be processed
		// (e.g. because it references a non existing entity)
		// and returns with an empty attribute that should not be added to the model
		boolean handledOk = handleAttributeType(oaProperty, attribute, entity);
		if (!handledOk)
			return null;
		setAttributeDescriptors(oaProperty, attribute, entity);
		setAttributeIds(oaProperty.getExtensions(), attribute, entity);
		return attribute;
	}

	private void addPathAttributes(PathTransformer pathTransformer, TdEntity entity) {
		if (pathTransformer != null) {
			List<Parameter> oaParams = pathTransformer.getPathParams(entity.getName());
			for (Parameter oaParam : oaParams) {
				if (OaUtil.isObject(oaParam.getSchema()) || OaUtil.isArray(oaParam.getSchema())) {
					log.debug("  ignore non primitive attribute {} from path parameters", oaParam.getName());
				}
				else if (oaParam.getExtensions() == null || !oaParam.getExtensions().containsKey(OaExtensions.X_FK)) {
					log.debug("  ignore non rid attribute {} from path parameters", oaParam.getName());
				} else {
					log.debug("  add attribute {} from path parameters", oaParam.getName());
					addPathAttributeIfRequired(oaParam, entity);
				}
			}
		}
	}
	private void addPathAttributeIfRequired(Parameter oaParam, TdEntity entity) {
		TdAttribute attr = this.createNewAttribute(oaParam.getName(), oaParam.getSchema(), entity);
		// When the second param in getAttribute is a property in the schema,
		// the method getExtensions gets all extensions from ites s, but when it is a parameter,
		// the schema is unable to get extensions from the parameter schema,
		// they must be taken from the parameter
		if (attr != null ) {
			setAttributeIds(oaParam.getExtensions(), attr, entity);
			if (entity.getAttribute(attr.getName()) == null)
				entity.addAttributesItem(attr);
		}
	}

	/**
	 * Main processing for each property of the schema object to transform into a TdAttribute
	 * This is assisted by the CompositeTransformer for non primitive attributes
	 */
	private boolean handleAttributeType(Schema<?> oaProperty, TdAttribute attribute, TdEntity entity) {
		if (this.entitiesToExclude.containsKey(entity.getName()))
			return false;
		attribute.datatype(OaUtil.getOaDataType(oaProperty.getType(), oaProperty.getFormat()));
		if (OaUtil.isFreeFormObject(oaProperty)) {
			// special case for free form, they are handled as a primitive
			attribute.datatype(OaExtensions.FREE_FORM_OBJECT);
			return true;
		} else if (oaProperty.get$ref() != null) {
			// Special case for refs: Parser should have been invoked with ResolveFully, but it seems
			// that there are some bugs (eg: https://github.com/swagger-api/swagger-parser/issues/1538)
			return ct.extractReferencedType(oaProperty, attribute, entity);
		} else if (OaUtil.isObject(oaProperty)) { // same than rdb (type)
			return ct.extractInlineType(oaProperty, attribute, entity);
		} else if (OaUtil.isArray(oaProperty)) { // this also handles refs
			return ct.extractArray(oaProperty, attribute, entity);
		}
		return true;
	}

	//Additional processing to transform oa property into attribute
	
	@SuppressWarnings("unchecked")
	private void setAttributeDescriptors(Schema oaProperty, TdAttribute attribute, TdEntity entity) {
		attribute.notnull(OaUtil.oaBoolean(oaProperty.getNullable()) ? "" : "true");
		attribute.readonly(OaUtil.oaBoolean(oaProperty.getReadOnly()) ? "true" : "");
		attribute.size(oaProperty.getMaxLength() == null ? "" : String.valueOf(oaProperty.getMaxLength()));
		attribute.defaultvalue(oaProperty.getDefault() == null ? "" : oaProperty.getDefault().toString());
		if (oaProperty.getEnum() != null)
			attribute.checkin(OaUtil.oaEnumString(oaProperty.getEnum()));
		if (oaProperty.getMinimum() != null) {
			String rop = oaProperty.getExclusiveMinimum() == Boolean.TRUE ? ">" : ">=";
			entity.addChecksItem(new TdCheck().name("chkmin_" + entity.getName() + "_" + attribute.getName())
					.attribute(attribute.getName()).constraint(attribute.getName() + rop + oaProperty.getMinimum()));
		}
		if (oaProperty.getMaximum() != null) {
			String rop = oaProperty.getExclusiveMaximum() == Boolean.TRUE ? "<" : "<=";
			entity.addChecksItem(new TdCheck().name("chkmax_" + entity.getName() + "_" + attribute.getName())
					.attribute(attribute.getName()).constraint(attribute.getName() + rop + oaProperty.getMaximum()));
		}
	}
	
	// After Gitlab issue #12, we need to collect the uids and rids before transforming each entity
	private void collectAttributeIds(Map<String, Schema> oaAttributes, List<String> uids, List<String> rids) {
		for (Entry<String, Schema> oaAttribute : oaAttributes.entrySet())
			this.collectAttributeIds(oaAttribute.getValue(), oaAttribute.getKey(), uids, rids);
		log.debug("  entity uids: {}, rids: {}", uids, rids);
	}

	private void collectAttributeIds(Schema<?> oaAttribute, String name, List<String> uids, List<String> rids) {
		Map<String, Object> extensions = oaAttribute.getExtensions();
		if (extensions == null)
			return;
		for (Entry<String, Object> oaExtension : extensions.entrySet()) {
			if (OaExtensions.X_PK.equals(oaExtension.getKey()))
				uids.add(name);
			else if (OaExtensions.X_FK.equals(oaExtension.getKey()))
				rids.add(name);
		}
	}

	// Note: this has similar structure to the above methods, refactor?
	// this could receive the uids and rids determined before,
	// but this should require passing additional parameters over the call tree
	private void setAttributeIds(Map<String, Object> extensions, TdAttribute attribute, TdEntity entity) {
		if (extensions == null)
			return;
		for (Entry<String, Object> oaExtension : extensions.entrySet()) {
			log.trace("Update uids/rids: {}, column: {}", oaExtension.getKey(), attribute.getName());
			if (OaExtensions.X_PK.equals(oaExtension.getKey()))
				attribute.uid("true"); // does not check the value, the presence of x-pk is enough
			else if (OaExtensions.X_FK.equals(oaExtension.getKey())) {
				attribute.rid(oaExtension.getValue().toString());
				attribute.ridname(OaExtensions.getFkName(entity.getName(), attribute.getName()));
			}
		}
	}

}
