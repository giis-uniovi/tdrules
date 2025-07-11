package giis.tdrules.store.loader.oa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import giis.tdrules.model.shared.EntityTypes;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.store.dtypes.DataTypes;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.gen.IUidGen;
import giis.tdrules.store.loader.gen.SequentialUidGen;
import giis.tdrules.store.loader.shared.LoaderException;
import giis.tdrules.store.loader.shared.LoaderUtil;

/**
 * A Local Data Adapter to generate a json object according to an openapi model,
 * but not sending any data
 */
public class OaLocalAdapter implements IDataAdapter {
	private static final Logger log = LoggerFactory.getLogger(OaLocalAdapter.class);

	// To differentiate array types
	private static final String PRIMITIVE_ARRAY = "primitive" + EntityTypes.DT_ARRAY;
	private static final String OBJECT_ARRAY = "object" + EntityTypes.DT_ARRAY;

	private DataTypes types = DataTypes.get(DataTypes.OA_DBMS_VENDOR_NAME);

	// Where the data adapter is currently writing
	protected GeneratedObject current;
	
	// All generated objects
	protected List<GeneratedObject> allGenerated;

	public OaLocalAdapter() {
		this.reset();
	}
	
	@Override
	public void reset() {
		allGenerated = new ArrayList<>();
		current = new GeneratedObject("");
	}

	@Override
	public DataTypes getDataTypes() {
		return this.types;
	}

	@Override
	public IDataAdapter getNewLocalAdapter() {
		return new OaLocalAdapter();
	}

	@Override
	public IUidGen getDefaultUidGen() {
		return new SequentialUidGen();
	}

	@Override
	public String getLast() {
		return current.generated.toString();
	}

	@Override
	public List<String> getAll() {
		List<String> all = new ArrayList<>();
		for (GeneratedObject obj : allGenerated)
			all.add("\"" + obj.entityName + "\":" + obj.generated.toString());
		return all;
	}

	@Override
	public void beginWrite(String entityName) {
		current = new GeneratedObject(entityName);
		current.id = "step" + this.allGenerated.size() + "_" + entityName;
	}

	@Override
	public void writeValue(String dataType, String attrName, String attrValue) {
		writeValueTo(dataType, attrName, attrValue, current.generated);
	}

	protected void writeValueTo(String dataType, String attrName, String attrValue, ObjectNode targetRoot) {
		// If numeric values are passed as a placeholder, they must be handled as strings
		// to avoid runtime errors when stored in the json
		if (LoaderUtil.canBeVariablePlaceholder(attrValue) && isNumber(dataType))
			dataType = "string";

		if (attrValue == null)
			targetRoot.set(attrName, targetRoot.nullNode());
		else if (EntityTypes.DT_TYPE.equals(dataType))
			targetRoot.set(attrName, parseObject(attrValue));
		else if (OBJECT_ARRAY.equals(dataType) || EntityTypes.DT_ARRAY.equals(dataType))
			writeArrayValueTo(attrName, attrValue, targetRoot, true);
		else if (PRIMITIVE_ARRAY.equals(dataType))
			writeArrayValueTo(attrName, attrValue, targetRoot, false);
		else if (isFreeFormObject(dataType))
			targetRoot.set(attrName, parseFreeFormObject(attrValue));
		else if (isString(dataType) || isDate(dataType))
			targetRoot.set(attrName, targetRoot.textNode(attrValue));
		else if (isInteger(dataType))
			targetRoot.set(attrName, targetRoot.numberNode(Long.parseLong(attrValue)));
		else if (isDecimal(dataType))
			targetRoot.set(attrName, targetRoot.numberNode(Double.parseDouble(attrValue)));
		else if (isBoolean(dataType))
			targetRoot.set(attrName, targetRoot.booleanNode("true".equals(attrValue)));
		else // if any other, it will be shown as string
			targetRoot.set(attrName, targetRoot.textNode(attrValue));
	}
	
	// Handling of arrays has 4 variants:
	// - array / additionalProperties (handled as an array but producing a map of values)
	// - primitive / object
	private void writeArrayValueTo(String attrName, String attrValue, ObjectNode targetRoot, boolean asObject) {
		if (asObject) {
			if (OaExtensions.ADDITIONAL_PROPERTIES.equals(attrName))
				targetRoot.set(attrName, parseArrayValuesToMap(attrValue, true));
			else
				targetRoot.set(attrName, parseArrayValues(attrValue, true));
		} else {
			if (OaExtensions.ADDITIONAL_PROPERTIES.equals(attrName))
				targetRoot.set(attrName, parseArrayValuesToMap(attrValue, false));
			else
				targetRoot.set(attrName, parseArrayValues(attrValue, false));
		}
	}
	
	@Override
	public void endWrite() {
		allGenerated.add(current);
		log.debug("endWrite: entity={} Json={}", current.entityName, current.generated.toString());
	}

	private ObjectNode parseObject(String value) {
		if (value != null && "".equals(value.trim()))
			value = "{}";
		try {
			if (value != null && "".equals(value.trim()))
				value = "{}";
			return (ObjectNode) new ObjectMapper().readTree(value);
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
	}

	// Converts the array of objects by removing the array pk attribute,
	// and returning an object with the rest of attributes or a single value (primitive array)
	private ArrayNode parseArrayValues(String value, boolean asObject) {
		ArrayNode source = parseArrayFromString(value);
		ArrayNode target = new ObjectMapper().createArrayNode();
		for (int i = 0; i < source.size(); i++) {
			ObjectNode item = new ObjectMapper().createObjectNode();
			Iterator<String> it = source.get(i).fieldNames();
			while (it.hasNext()) {
				String fieldName = it.next();
				JsonNode fieldValue = source.get(i).get(fieldName);
				boolean isPk = OaExtensions.ARRAY_PK.equals(fieldName);
				if (asObject && !isPk) {
					item.set(fieldName, fieldValue);
				} else if (!isPk) { // primitive write the value (not object) and exit loop here
					target.add(fieldValue);
					break;
				}
			}
			if (asObject)
				target.add(item);
		}
		return target;
	}

	// Converts the array of objects representation to the map required by additionalProperties
	private ObjectNode parseArrayValuesToMap(String arrayString, boolean asObject) {
		ArrayNode source = parseArrayFromString(arrayString);
		ObjectNode target = new ObjectMapper().createObjectNode();
		for (int i = 0; i < source.size(); i++) {
			String key = "";
			ObjectNode item = new ObjectMapper().createObjectNode();
			Iterator<String> it = source.get(i).fieldNames();
			while (it.hasNext()) {
				String fieldName = it.next();
				JsonNode fieldValue = source.get(i).get(fieldName);
				if (OaExtensions.ARRAY_PK.equals(fieldName)) { // Should be the first
					key = fieldValue.asText();
				} else if (asObject) {
					item.set(fieldName, fieldValue);
				} else { // primitive write the value (not object) and exit loop here
					target.set(key, fieldValue);
					break;
				}
			}
			if (asObject)
				target.set(key, item);
		}
		return target;
	}
	
	private ArrayNode parseArrayFromString(String value) {
		try {
			if (value != null && "".equals(value.trim()))
				value = "[]";
			ObjectMapper mapper = new ObjectMapper();
			Object[] asArray = mapper.readValue(value, Object[].class);
			return mapper.valueToTree(asArray);
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
	}

	private JsonNode parseFreeFormObject(String value) {
		if (value != null && "".equals(value.trim()))
			value = "{}";
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (value.trim().startsWith("{")) // return this string as object
				return mapper.readTree(value);
			else { // Returns an object with key "generated" and this value
				ObjectNode object = mapper.createObjectNode();
				object.put("generated", value);
				return object;
			}
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
	}

}
