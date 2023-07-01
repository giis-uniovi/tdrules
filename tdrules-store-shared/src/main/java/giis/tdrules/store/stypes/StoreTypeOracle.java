package giis.tdrules.store.stypes;

public class StoreTypeOracle extends StoreType {
	protected StoreTypeOracle(String dbms) {
		super(dbms);
	}

	@Override
	public boolean isOracle() {
		return true;
	}

	@Override
	public boolean canDisableForeignKey() {
		return true;
	}
	@Override
	public String getEnableForeignKeyCommand(String tableName, String fkName) {
		return "ALTER TABLE " + tableName + " ENABLE CONSTRAINT " + fkName;
	}
	@Override
	public String getDisableForeignKeyCommand(String tableName, String fkName) {
		return "ALTER TABLE " + tableName + " DISABLE CONSTRAINT " + fkName;
	}
	@Override
	public boolean getAliasInOrderByAllowed() {
		return true;
	}

	/**
	 * Mensajes de error conocidos
	 */
	@Override
	public String getKnownError(String message) {
		if (message.contains("ORA-01013"))
			return DBMS_TIMEOUT;
		else if (message.contains("ORA-01476"))
			return DBMS_DIVIDE_BY_ZERO;
		else if (message.contains("ORA-01839"))
			return DBMS_INVALID_DATE_MONTH;
		return DBMS_UNKNOWN_ERROR; // cualquier otro lo marco como no conocido
	}

	/**
	 * Query a utilizar para obtener el SQL de una vista
	 */
	@Override
	public String getViewDefinitionSQL(String catalog, String schema, String viewName) {
		// En Oracle no he localizado como definir el catalogo y esquema, de todas formas
		// esto se usa para manejar vistas en QAShrink, y no se soporten vistas con
		// nombre cualificado
		String sql = "SELECT TEXT AS VIEW_DEFINITION FROM USER_VIEWS WHERE";
		sql += " VIEW_NAME='" + viewName + "'";
		return sql;
	}

	/**
	 * Algunas tablas de sistema son visibles en Oracle (al menos en 10g)
	 */
	@Override
	public boolean isSystemTable(String tableName) {
		return tableName.length() >= 4
			&& (tableName.startsWith("BIN$") || tableName.startsWith("JAVA$") || tableName.startsWith("CREATE$JAVA$"));
	}

	/**
	 * Especifica si se requiere un alias cuando se representa una tabla derivada
	 * (ej. select * form (select...), oracle no lo requiere,sqlserver si
	 */
	@Override
	public boolean getAliasInDerivedTableRequired() {
		return false;
	}

	/**
	 * Determina la condicion que se puede utilizar para limitar el maximo numero de
	 * filas devueltas
	 */
	@Override
	public String getMaxRowCondition(long maxRows) {
		return "ROWNUM <= " + maxRows;
	}

	/**
	 * Devuelve la query pasada como parametro con la restriccion de que devuelva
	 * solo el numero de filas indicado
	 */
	@Override
	public String getSqlLimitRows(String sql, long maxRows) {
		return "SELECT * FROM (" + sql + ") WHERE " + getMaxRowCondition(maxRows);
	}

	/**
	 * Determina la funcion que genera el ranking de una fila dentro de las que
	 * forman parte de un grupo
	 */
	@Override
	public String getRankFunctionInGroup() {
		return "row_number()";
	}

	/**
	 * Determina si el DBMS soporta funciones over (partition by...) utilizadas en
	 * las optimizaciones de QAShrink
	 */
	public boolean getSupportsPartitionBy() {
		return true;
	}

	/**
	 * Determina si el DBMS soporta funciones over (partition by...) utilizadas en
	 * las optimizaciones de QAShrink
	 */
	@Override
	public boolean getSupportsPartitionBy(int version) {
		// Solo a partir de Oracle 8i segun
		// http://www.techonthenet.com/oracle/functions/rank.php
		return version >= 8;
	}

	/**
	 * devuelve la funcion substring correspondiente al gestor de base de datos
	 * actual
	 */
	@Override
	public String getSQLSubstring(String str, int start, int length) {
		return "substr(" + str + "," + start + "," + length + ")";
	}

	/**
	 * devuelve el operador de concatenacion de strings correspondiente al gestor de
	 * base de datos actual
	 */
	@Override
	public String getSQLStringConcat() {
		return "||";
	}

	/**
	 * Devuelve el string apropiado para un valor literal de tipo fecha y/o Hora a
	 * incluir en una sentencia sql: en oracle debe ser precedido por TIMESTAMP,
	 * DATE o INTERVAL (lo mas parecido a time)
	 * 
	 * @param sDate string de fecha en formato yyyy-mm-dd
	 * @param sTime string de hora en formato hh:mm:ss.d
	 */
	@Override
	public String getSqlDatetimeLiteral(String sDate, String sTime) {
		if (sDate.contains("T"))
			return "TIMESTAMP " + sqlString(sDate).replace("T", " ");
		else if ("".equals(sTime))
			return "DATE " + sqlString(sDate);
		else if ("".equals(sDate))
			return "INTERVAL " + sqlString(sTime);
		else
			return "TIMESTAMP " + sqlString(sDate + " " + sTime);
	}

	/**
	 * Devuelve el string apropiado para que una columna de tipo fecha+hora en una
	 * sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
	 * HH:MM:SS
	 */
	@Override
	public String getSqlDatetimeColumnString(String sCol) {
		return "TO_CHAR(" + sCol + ", 'YYYY-MM-DD HH24:MI:SS')";
	}

	/**
	 * Devuelve el string apropiado para que una columna de tipo fecha+hora en una
	 * sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
	 */
	@Override
	public String getSqlDateColumnString(String sCol) {
		return "TO_CHAR(" + sCol + ", 'YYYY-MM-DD')";
	}

}
