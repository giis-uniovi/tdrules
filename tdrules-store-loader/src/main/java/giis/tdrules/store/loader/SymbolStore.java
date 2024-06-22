package giis.tdrules.store.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores generated values and objects associated to symbols. Symbols are used
 * during the generation of an entity to specify a value that has been generated
 * for a previous entity
 */
public class SymbolStore {
	private static final Logger log = LoggerFactory.getLogger(SymbolStore.class);

	// Stores the generated symbolic values as pairs entity.name.symbol, value
	private Map<String, String> symbolicValues = new HashMap<>();
	
	// Stores the generated symbolic composite objects as pairs entity.name.symbol, json
	private Map<String, String> symbolicObjects = new HashMap<>();

	// Stores the generated symbolic arrays as pairs entity.name.symbol, array of json objects
	private Map<String, List<String>> symbolicKeyArrays=new HashMap<>();
	
	public Map<String, String> getSymbolicKeyValues() {
		return symbolicValues;
	}

	/**
	 * Returns the stored value of a symbol assigned to an attribute (in the form
	 * entity.attribute)
	 */
	public String getValue(String entityAndAttribute, String symbol) {
		String key = getSymbolicName(entityAndAttribute, symbol);
		String value = symbolicValues.get(key);
		log.trace("Get symbolic generated value: {}={}", key, value);
		return value;
	}

	/**
	 * Stores the generated value of a symbol assigned to an attribute of an entity
	 */
	public void setValue(String entity, String attribute, String symbol, String value) {
		String key = getSymbolicName(entity, attribute, symbol);
		this.symbolicValues.put(key, value);
		log.debug("Store symbolic generated value: {}={}", key, value);
	}

	/**
	 * Returns the stored object of a symbol assigned to an attribute (in the form
	 * entity.attribute)
	 */
	public String getObject(String entityAndAttribute, String symbol) {
		String key = getSymbolicName(entityAndAttribute, symbol);
		String value = symbolicObjects.get(key);
		log.trace("Get symbolic object: {}={}", key, value);
		return value;
	}

	/**
	 * Stores the a composite object value of a symbol assigned to an attribute of
	 * an entity
	 */
	public void setObject(String entity, String attribute, String symbol, String json) {
		String key = getSymbolicName(entity, attribute, symbol);
		this.symbolicObjects.put(key, json);
		log.debug("Store symbolic object: {}={}", key, json);
	}

	/**
	 * Returns the stored array of objects of a symbol assigned to an entity and attribute
	 */
	public List<String> getArray(String entity, String attribute, String symbol) {
		String key = this.getSymbolicName(entity, attribute, symbol);
		List<String> arrayValues = this.symbolicKeyArrays.get(key);
		log.debug("Get symbolic array: {}={}", key, arrayValues);
		return arrayValues;
	}

	/**
	 * Adds an object to the stored array assigned to an entity and attribute
	 */
	public void addArrayItem(String entity, String attribute, String symbol, String json) {
		String key = this.getSymbolicName(entity, attribute, symbol);
		if (!this.symbolicKeyArrays.containsKey(key)) // NOSONAR
			this.symbolicKeyArrays.put(key, new ArrayList<>());
		this.symbolicKeyArrays.get(key).add(json);
		log.debug("Store generated array item for symbolic key: {}={}", key, json);
	}

	/**
	 * Checks if a given string is a symbolic value (starting with the at symbol)
	 */
	public boolean isSymbol(String s) {
		return s != null && s.startsWith("@");
	}

	// fully qualified name of symbols
	
	String getSymbolicName(String entityName, String attrName, String value) {
		return (entityName + "." + attrName + "." + value).toLowerCase();
	}

	private String getSymbolicName(String entityAttrName, String value) {
		return (entityAttrName + "." + value).toLowerCase();
	}

}
