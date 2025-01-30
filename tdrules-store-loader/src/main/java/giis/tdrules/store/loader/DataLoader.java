package giis.tdrules.store.loader;

import java.util.Map;

import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.gen.DeterministicAttrGen;
import giis.tdrules.store.loader.gen.SequentialUidGen;

/**
 * Loads entities in a data store according to a TdSchema model through an
 * instance of IDataAdapter (by issuing sql commands to an rdb database or json
 * objects through an Api).
 * 
 * The user issues a series of load commands. Each,
 * can include fixed or symbolic values, the rest of attributes will be filled
 * with generated data. Symbolic values (prefixed with @) are used to get
 * backend generated data (to retrieve backend generated uids that are used in
 * subsequent calls to fill the rids).
 */
public class DataLoader {
	
	private TdSchema schema;
	private LoaderConfig config;
	private SymbolStore symbols; // filled with values during generation

	/**
	 * Instantiates the Data Loader for a given schema, the dataAdapter instance
	 * routes the data to the appropriate data store (rdb, OpenApi).
	 */
	public DataLoader(TdSchema schema, IDataAdapter dataAdapter) {
		this.schema = schema;
		this.symbols = new SymbolStore();
		this.config = new LoaderConfig();
		this.config.dataAdapter = dataAdapter;
		this.config.uidGen = config.dataAdapter.getDefaultUidGen();
		this.config.attrGen = new DeterministicAttrGen();
		this.config.arrayUidGen = new SequentialUidGen();
		// configure the model of the data adapter (needed for the live adapter)
		this.config.dataAdapter.setSchemaModel(schema);
	}

	/**
	 * Resets all internal parameters that define the state of the generator to allow
	 * reproducing the generated values
	 */
	public void reset() {
		symbols = new SymbolStore(); // Internal symbol memory
		config.reset(); // Resets all configurable objects used here
	}

	/**
	 * Sets the uid generator to be used to obtain values of uid attributes.
	 * If not configured, it will use the data adapter's default generator
	 */
	public DataLoader setUidGen(IUidGen uidGen) {
		config.uidGen = uidGen;
		return this;
	}

	/**
	 * Sets the attribute generator to be used to obtain values of non uid attributes.
	 * If not configured, it will use a deterministic generator
	 */
	public DataLoader setAttrGen(IAttrGen attrGen) {
		config.attrGen = attrGen;
		return this;
	}

	/**
	 * Specifies if values for non rid nullable attributes will always be generated
	 * (default is true)
	 */
	public DataLoader setGenerateNullable(boolean value) {
		config.genNullable = value;
		return this;
	}

	/**
	 * Specifies if values for attributes that have a default value will always be
	 * generated (default is true).
	 */
	public DataLoader setGenerateDefault(boolean value) {
		config.genDefault = value;
		return this;
	}

	/**
	 * Specifies the percent probability to generate a null value in a nullable attribute.
	 */
	public DataLoader setNullProbability(int percentage) {
		config.genNullProbability = percentage;
		return this;
	}

	public Map<String, String> getSymbolicKeyValues() {
		return symbols.getSymbolicKeyValues();
	}

	public IDataAdapter getDataAdapter() {
		return config.dataAdapter;
	}
	
	public TdSchema getSchema() {
		return this.schema;
	}

	/**
	 * Loads the values of an entity object with some user specified values and others automatically generated.
	 * @param entity The name of the entity to be loaded
	 * @param attributeNamesValues A csv string of pairs attribute=value that specify the values that must be sent.
	 * Attributes that are not specified here will have a generated value (depending on the configured parameters).
	 * A value that starts with @ represents a symbol that will store a backend generated value to
	 * be used in subsequent entity generations
	 * @return a string with the generated data (for debug or log)
	 */
	public String load(String entity, String attributeNamesValues) {
		// separates entities and values in different arrays
		String[] nameValues = EntityLoader.splitCsv(attributeNamesValues);
		String[] names = new String[nameValues.length];
		String[] values = new String[nameValues.length];
		for (int i = 0; i < nameValues.length; i++) {
			String nameValue = nameValues[i];
			if (!"".equals(nameValue)) {
				int eqPos = nameValue.indexOf('='); // must exist
				names[i] = nameValue.substring(0, eqPos).trim();
				values[i] = nameValue.substring(eqPos + 1, nameValue.length()).trim();
			}
		}
		EntityLoader loader = new EntityLoader(schema, config, symbols);
		return loader.loadValues(entity, names, values);
	}

	/**
	 * Loads the values of an entity object with some specified values and others automatically generated.
	 * @param entity The name of the entity to be loaded
	 * @param attributeNames The names of the attributes that will have an specified value to send.
	 * Attributes that are not specified here will have a generated value (depending on the configured parameters).
	 * @param attributeValues The value to be loaded for each attribute.
	 * A value that starts with @ represents a symbol that will store a backend generated value to
	 * be used in subsequent entity generations
	 * @return a string with the generated data (for debug or log)
	 */
	public String load(String entity, String attributeNames, String attributeValues) {
		EntityLoader loader = new EntityLoader(schema, config, symbols);
		return loader.loadValues(entity, EntityLoader.splitCsv(attributeNames), EntityLoader.splitCsv(attributeValues));
	}
	
}
