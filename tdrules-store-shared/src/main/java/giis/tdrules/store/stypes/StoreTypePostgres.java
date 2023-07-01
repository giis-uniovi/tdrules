package giis.tdrules.store.stypes;

import java.util.HashMap;
import java.util.Map;

import giis.portable.util.JavaCs;

public class StoreTypePostgres extends StoreType {
	private Map<String, String> aliasMap = null;

	protected StoreTypePostgres(String dbms) {
		super(dbms);
	}

	@Override
	public boolean isPostgres() {
		return true;
	}

	/**
	 * Comando para deshabilitar todas las constraints de una tabla, usado para
	 * deshabilitar fks porque no se puede hacer facilmente de forma individual
	 */
	@Override
	public String getDisableConstraintsCommand(String tableName) {
		return "ALTER TABLE " + tableName + " DISABLE TRIGGER ALL";
	}

	/**
	 * Comando para habilitar todas las constraints de una tabla, usado para
	 * habilitar fks porque no se puede hacer facilmente de forma individual
	 */
	@Override
	public String getEnableConstraintsCommand(String tableName) {
		return "ALTER TABLE " + tableName + " ENABLE TRIGGER ALL";
	}

	/**
	 * Los campos autoincrementales se establecen mediante un tipo especifico
	 * (serial) en postgres
	 */
	@Override
	public String getDataTypeIdentity(String baseType) {
		return "serial";
	}

	/**
	 * Query a utilizar para obtener el SQL de una vista sin limitar el numero de
	 * caracteres
	 */
	@Override
	public String getViewDefinitionSQL(String catalog, String schema, String viewName) {
		// En postgres el schema que se ve es siempre public, no puede anyadirse al nombre de vista
		return "select definition from pg_views where viewname = '" + viewName + "'";
	}

	/**
	 * Cuando se usan los metadatos, postgres devuelve los nombres de tipos de datos
	 * internos, que se mapean aqui a los documentados en postgress o tipos de datos
	 * comunes en sql Tabla de equivalencias en
	 * https://www.postgresql.org/docs/9.6/datatype.html
	 */
	@Override
	public String mapAliasToDataType(String alias) {
		if (aliasMap == null) { // confecciona el mapping si no esta definido
			aliasMap = new HashMap<>();
			aliasMap.put("int2", "smallint");
			aliasMap.put("int4", "integer");
			aliasMap.put("int", "integer");
			aliasMap.put("int8", "bigint");
			aliasMap.put("serial2", "smallserial");
			aliasMap.put("serial4", "serial");
			aliasMap.put("serial8", "bigserial");
			aliasMap.put("decimal", "numeric");
			aliasMap.put("float4", "real");
			aliasMap.put("float8", "double precision");
			aliasMap.put("bpchar", "char"); // no esta en la tabla, pero aparece
			aliasMap.put("bool", "boolean");
			aliasMap.put("timetz", "time with time zone");
			aliasMap.put("timestamptz", "timestamp with time zone");
		}
		// Postgres parece que anyade un underscore cuando el tipo proviene de un array, lo elimina
		if (alias.startsWith("_"))
			alias = JavaCs.substring(alias, 1, alias.length());
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
