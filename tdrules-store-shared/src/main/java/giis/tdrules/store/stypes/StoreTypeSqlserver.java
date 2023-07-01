package giis.tdrules.store.stypes;

import giis.portable.util.JavaCs;

public class StoreTypeSqlserver extends StoreType {
	protected StoreTypeSqlserver(String dbms) {
		super(dbms);
	}

	@Override
	public boolean isSQLServer() {
		return true;
	}

	@Override
	public boolean supportsBracketQuotation() {
		return true;
	}
	@Override
	public boolean canDisableForeignKey() {
		return true;
	}
	
	@Override
	public String getEnableIdentityCommand(String tableName) {
		return "SET IDENTITY_INSERT " + tableName + " ON";
	}

	@Override
	public String getDisableIdentityCommand(String tableName) {
		return "SET IDENTITY_INSERT " + tableName + " OFF";
	}

	@Override
	public String getEnableForeignKeyCommand(String tableName, String fkName) {
		return "ALTER TABLE " + tableName + " CHECK CONSTRAINT " + fkName;
	}
	@Override
	public String getDisableForeignKeyCommand(String tableName, String fkName) {
		return "ALTER TABLE " + tableName + " NOCHECK CONSTRAINT " + fkName;
	}
	@Override
	public String getEndSQLQueryChar() {
		return "";
	}
	@Override
	public String getEndSQLBlockCommand() {
		return "GO";
	}
	@Override
	public boolean getAliasInOrderByAllowed() {
		return false;
	}

	/**
	 * Los campos autoincrementales en sqlserver se identifican anyadiendo un sufijo
	 * identity al tipo base de la clave, este es el que devuelve este metodo
	 */
	@Override
	public String getDataTypeIdentitySuffix() {
		return "identity";
	}

	/**
	 * Los campos autoincrementales (p.e. .net con SQLServer) no se identifican
	 * anyadiendo un sufijo sino que se debe ejecutar una query, esta es el que
	 * devuelve este metodo
	 */
	@Override
	public String getDataTypeIdentitySql(String tableName) {
		return "select name from sys.identity_columns where [object_id] = object_id('" + tableName + "')";
	}

	/**
	 * Query a utilizar para obtener el SQL de una vista sin limitar el numero de caracteres
	 * https://stackoverflow.com/questions/4765323/is-there-a-way-to-retrieve-the-view-definition-from-a-sql-server-using-plain-ado
	 */
	@Override
	public String getViewDefinitionSQL(String catalog, String schema, String viewName) {
		String catSchema = "";
		if (catalog != null && !"".equals(catalog))
			catSchema += catalog + ".";
		if (schema != null && !"".equals(schema))
			catSchema += schema + ".";
		String sql = "SELECT definition AS VIEW_DEFINITION FROM sys.objects o";
		sql += " JOIN sys.sql_modules m on m.object_id = o.object_id WHERE";
		sql += " o.object_id = object_id('" + catSchema + viewName + "') and o.type = 'V'";
		return sql;
	}

	/**
	 * En SQLServer, a partir de 2005 un usuario (posiblemente con privilegios)
	 * puede ver las vistas que estan en estos esquemas del sistema
	 */
	@Override
	public boolean isSystemSchema(String schemaName) {
		return JavaCs.equalsIgnoreCase(schemaName, "information_schema")
				|| JavaCs.equalsIgnoreCase(schemaName, "sys");
	}

	/**
	 * Jul 2022, En alguna BD ha aparecido sysdiagrams que se ve como user table
	 * pero que parece que es creada por el management studio
	 */
	@Override
	public boolean isSystemTable(String tableName) {
		return JavaCs.equalsIgnoreCase(tableName, "sysdiagrams");
	}

	/**
	 * En SQLServer 2000 (no a partir de 2005) hay otras vistas visibles por el usuario
	 */
	@Override
	public boolean isSystemView(String viewName) {
		return JavaCs.equalsIgnoreCase(viewName, "sysconstraints")
				|| JavaCs.equalsIgnoreCase(viewName, "syssegments");
	}

	/**
	 * Determina la clausula que se puede anyadir a SELECT para limitar el maximo
	 * numero de filas devueltas (vacio si no existe en este SGBD)
	 */
	@Override
	public String getMaxRowSelectClause(long maxRows) {
		return "TOP " + maxRows;
	}

	/**
	 * Devuelve la query pasada como parametro con la restriccion de que devuelva
	 * solo el numero de filas indicado
	 */
	@Override
	public String getSqlLimitRows(String sql, long maxRows) {
		return "SELECT " + getMaxRowSelectClause(maxRows) + " * FROM (" + sql + ") X";
	}

	/**
	 * Determina la funcion que genera el ranking de una fila dentro de las que
	 * forman parte de un grupo
	 */
	@Override
	public String getRankFunctionInGroup() {
		return "rank()";
	}

	/**
	 * Determina si el DBMS soporta funciones over (partition by...) utilizadas en
	 * las optimizaciones de QAShrink
	 */
	@Override
	public boolean getSupportsPartitionBy(int version) {
		// Solo a partir de SQLServer 2005 (9.0) segun
		// http://archive.cpradio.org/work/row_number-and-partition-by-in-sql-server-2000/
		return version >= 9;
	}

	/**
	 * Devuelve el tipo de datos del esquema correspondiente a una fecha+hora
	 * (datetime es el valor en sqlserver)
	 */
	@Override
	public String getDataTypeDatetime() {
		return "datetime";
	}

	/**
	 * Devuelve el string apropiado para un valor literal de tipo fecha y/o Hora a
	 * incluir en una sentencia sql: - si hay fecha y hora los concatena (formato
	 * iso) de forma que se puedan utilizar para insertar valores en una tabla - si
	 * solo hay una fecha utiliza el formato sin guiones porque con guiones funciona
	 * bien si se inserta en un campo DATE pero no en un DATETIME - si no hay fecha
	 * muestra la hora tal y como se ha recibido
	 * 
	 * @param sDate string de fecha en formato yyyy-mm-dd
	 * @param sTime string de hora en formato hh:mm:ss.d
	 */
	@Override
	public String getSqlDatetimeLiteral(String sDate, String sTime) {
		if (sDate.contains("T"))
			return sqlString(sDate); // si se pasa una fecha iso en sDate la devuelve tal cual ignorando sTime
		else if ("".equals(sTime))
			return sqlString(sDate.replace("-", "")); // en sqlserver si solo hay fecha debe tener formato sin guiones
		else if ("".equals(sDate))
			return sqlString(sTime);
		else // con fecha y hora lo escribe en formato iso, con una T
			return sqlString(sDate + "T" + sTime);
	}

	/**
	 * Devuelve el string apropiado para que una columna de tipo fecha+hora en una
	 * sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
	 * HH:MM:SS
	 */
	@Override
	public String getSqlDatetimeColumnString(String sCol) {
		return "CONVERT(CHAR(19)," + sCol + ",120)";
	}

	/**
	 * Devuelve el string apropiado para que una columna de tipo fecha+hora en una
	 * sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
	 */
	@Override
	public String getSqlDateColumnString(String sCol) {
		return "CONVERT(CHAR(10)," + sCol + ",23)";
	}

}
