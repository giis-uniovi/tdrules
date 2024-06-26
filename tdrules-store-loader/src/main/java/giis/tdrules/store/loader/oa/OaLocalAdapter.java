package giis.tdrules.store.loader.oa;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import giis.tdrules.model.EntityTypes;
import giis.tdrules.store.dtypes.DataTypes;
import giis.tdrules.store.loader.LoaderException;
import giis.tdrules.store.loader.IDataAdapter;
import giis.tdrules.store.loader.IUidGen;
import giis.tdrules.store.loader.gen.SequentialUidGen;

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

	// All generated json objects
	private List<GeneratedObject> allGenerated = new ArrayList<>();

	// Entity where the data adapter is currently writing
	protected String currentEntity = "";

	// Json object that the data adapter is currently writing
	private ObjectNode currentRoot;

	public class GeneratedObject {
		private String name;
		private String json;

		public GeneratedObject(String name, String json) {
			this.name = name;
			this.json = json;
		}
	}

	@Override
	public void reset() {
		allGenerated = new ArrayList<>();
		currentEntity = "";
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
		return allGenerated.get(allGenerated.size() - 1).json;
	}

	@Override
	public List<String> getAll() {
		List<String> all = new ArrayList<>();
		for (GeneratedObject obj : allGenerated)
			all.add("\"" + obj.name + "\":" + obj.json);
		return all;
	}

	@Override
	public void beginWrite(String entityName) {
		this.currentEntity = entityName;
		ObjectMapper mapper = new ObjectMapper();
		currentRoot = mapper.createObjectNode();
	}

	@Override
	public void writeValue(String dataType, String attrName, String attrValue) {
		if (attrValue == null)
			currentRoot.set(attrName, currentRoot.nullNode());
		else if (EntityTypes.DT_TYPE.equals(dataType))
			currentRoot.set(attrName, parseObject(attrValue));
		else if (OBJECT_ARRAY.equals(dataType) || EntityTypes.DT_ARRAY.equals(dataType))
			currentRoot.set(attrName, parseArrayOfObjects(attrValue));
		else if (PRIMITIVE_ARRAY.equals(dataType))
			currentRoot.set(attrName, parseArrayOfPrimitive(attrValue));
		else if (isString(dataType) || isDate(dataType))
			currentRoot.set(attrName, currentRoot.textNode(attrValue));
		else if (isNumber(dataType) && !hasDecimals(dataType, "")) // OA does not have exact numeric
			currentRoot.set(attrName, currentRoot.numberNode(Long.parseLong(attrValue)));
		else if (isNumber(dataType) && hasDecimals(dataType, ""))
			currentRoot.set(attrName, currentRoot.numberNode(Double.parseDouble(attrValue)));
		else if (isBoolean(dataType))
			currentRoot.set(attrName, currentRoot.booleanNode("true".equals(attrValue)));
		else // if any other, it will be shown as string
			currentRoot.set(attrName, currentRoot.textNode(attrValue));
	}

	@Override
	public void endWrite() {

		allGenerated.add(new GeneratedObject(currentEntity, currentRoot.toString()));
		log.debug("endWrite: entity={} Json={}", this.currentEntity, getLast());
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

	private ArrayNode parseArrayOfObjects(String value) {
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

	private ArrayNode parseArrayOfPrimitive(String value) {
		try {
			if (value != null && "".equals(value.trim()))
				value = "[]";
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode newArray = mapper.createArrayNode();
			Object[] thisArray = mapper.readValue(value, Object[].class);
			// Los arrays siempre vienen como array de objetos, cuando se ha de escribir como array de primitivos
			// se buscan las claves de cada objeto (solo la primera) y se usa el valor para escribir
			// en un nuevo array que sera el devuelto por el metodo
			ArrayNode array = mapper.valueToTree(thisArray);
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i).fieldNames().hasNext()) {
					String fieldName = array.get(i).fieldNames().next();
					JsonNode fieldValue = array.get(i).get(fieldName);
					newArray.add(fieldValue);
				}
			}
			return newArray;
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
	}

}
