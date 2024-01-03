package giis.tdrules.store.stypes;

/**
 * A few features of mysql, not thoroughly tested.
 */
public class StoreTypeSqlite extends StoreType {
	protected StoreTypeSqlite(String dbms) {
		super(dbms);
	}

	@Override
	public boolean isSqlite() {
		return true;
	}

	@Override
	public boolean supportsBracketQuotation() {
		return true;
	}

	/**
	 * Los campos autoincrementales en sqlite se identifican buscando en una query
	 * https://stopbyte.com/t/how-to-check-if-a-column-is-autoincrement-primary-key-or-not-in-sqlite/174/2
	 * Tambien se pueden determinar consultando la tabla de secuencias, pero esta vacia si no se ha insertado en la tabla
	 */
	@Override
	public String getDataTypeIdentitySql(String tableName, String columnName) {
		return "SELECT 'is-autoincrement' FROM sqlite_master WHERE tbl_name='" + tableName + "' AND sql LIKE '%AUTOINCREMENT%'";
	}

	/**
	 * Query a utilizar para obtener el SQL de una vista
	 */
	@Override
	public String getViewDefinitionSQL(String catalog, String schema, String viewName) {
		return "select sql from sqlite_master where type = 'view' and name = '" + viewName + "' and tbl_name = '" + viewName + "'";
	}

	/**
	 * Determina la clausula que se puede anyadir al final de una query limitar el
	 * maximo numero de filas devueltas (vacio si no existe en este SGBD)
	 */
	@Override
	public String getMaxRowsLimitClause(long maxRows) {
		return "LIMIT " + maxRows;
	}

	/**
	 * Restricciones de la sintaxis de Sqlite: no soporta right joins, y requiere
	 * tener groupby si hay having
	 */
	@Override
	public boolean getSupportsHavingWithoutGroupBy() {
		return false;
	}

	@Override
	public boolean getSupportsRightJoin() {
		return false;
	}

}
