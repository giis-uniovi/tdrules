package giis.tdrules.store.stypes;

/**
 * Specific features when the data store is obtained from an OpenApi specification
 */
public class StoreTypeOpenApi extends StoreType {
	public StoreTypeOpenApi(String dbms) {
		super(dbms);
	}

	/**
	 * Devuelve el string apropiado para un valor logico en una cadena SQL
	 */
	@Override
	public String sqlBoolean(boolean b) {
		return b ? "true" : "false";
	}

	/**
	 * Devuelve true si soporta identificadore entre corchetes
	 */
	@Override
	public boolean supportsBracketQuotation() {
		return false;
	}

}
