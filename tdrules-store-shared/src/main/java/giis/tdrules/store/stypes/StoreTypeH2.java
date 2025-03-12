package giis.tdrules.store.stypes;

import java.util.HashMap;
import java.util.Map;

public class StoreTypeH2 extends StoreType {
	private Map<String, String> aliasMap = null;

	public StoreTypeH2(String dbms) {
		super(dbms);
	}

	@Override
	public boolean isH2() {
		return true;
	}

	/**
	 * Los campos autoincrementales se establecen mediante un tipo especifico
	 * (identity) en H2
	 */
	@Override
	public String getDataTypeIdentity(String baseType) {
		return "identity";
	}

	/**
	 * Cuando se usan los metadatos, la V2 ha cambiado el tipo de datos interno que
	 * se obitene, se mapea aqui. Tabla de equivalencias en
	 * http://www.h2database.com/html/datatypes.html
	 */
	@Override
	public String mapAliasToDataType(String alias) {
		if (aliasMap == null) { // confecciona el mapping si no esta definido
			aliasMap = new HashMap<String, String>();
			aliasMap.put("CHARACTER VARYING", "VARCHAR");
		}
		// devuelve el mapeo si existe, si no el mismo valor recibido
		if (aliasMap.containsKey(alias))
			return aliasMap.get(alias);
		return alias;
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
