package giis.tdrules.store.stypes;

/**
 * A few features of mysql, not thoroughly tested.
 */
public class StoreTypeMysql extends StoreType {
	protected StoreTypeMysql(String dbms) {
		super(dbms);
	}

	@Override
	public boolean isMySQL() {
		return true;
	}

	/**
	 * Determina la clausula que se puede anyadir al final de una query limitar el
	 * maximo numero de filas devueltas (vacio si no existe en este SGBD)
	 */
	@Override
	public String getMaxRowsLimitClause(long maxRows) {
		return "LIMIT " + maxRows;
	}
}
