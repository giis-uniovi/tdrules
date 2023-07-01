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
	 * Los campos autoincrementales en sqlite se identifican anyadiendo un sufijo
	 * autoincrement al tipo base de la clave, este es el que devuelve este metodo
	 */
	@Override
	public String getDataTypeIdentitySuffix() {
		return "autoincrement";
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
