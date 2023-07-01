package giis.tdrules.store.stypes;

import giis.portable.util.JavaCs;

/**
 * Manages the variability of capabilities and behaviours of Data Stores.
 * 
 * Behaves as a factory by calling the get method with a string
 * representing the name of the data store and returning the specific implementation.
 * 
 * NOTE: This class provides default implementations that must be overriden
 * by the subclasses. Do not assume that implementations are complete,
 * they are added as needed.
 */
public class StoreType {
	protected String dbmsVendorName = "";
	private static final String MYSQL = "mysql";
	private static final String SQLITE = "sqlite";
	private static final String H2 = "h2";
	private static final String ORACLE = "oracle";
	private static final String SQLSERVER = "sqlserver";
	private static final String POSTGRES = "postgres";
	
	// nombres de los errores de ejecucion conocidos para el SGBD.
	// Las subclases para cada DBMS implementaran los metodos para obtener estos
	// valores a partir de los mensajes de error
	public static final String DBMS_UNKNOWN_ERROR = "unknown";
	public static final String DBMS_TIMEOUT = "Query timeout";
	public static final String DBMS_DIVIDE_BY_ZERO = "Divisor is equal to zero";
	public static final String DBMS_INVALID_DATE_MONTH = "Date not valid for month specified";

	// Constructor (privado, la instanciacion se realiza con el metodo get)
	protected StoreType(String dbms) {
		this.dbmsVendorName = dbms;
	}

	/**
	 * Factoria que devuelve el objeto correspondiente que implementa las
	 * particularidades del vendedor
	 */
	public static StoreType get(String dbms) {
		String productName = dbms.toLowerCase();
		if (productName.equals("openapi"))
			return new StoreTypeOpenApi("openapi");
		if (productName.equals("microsoft sql server") || productName.equals("microsoft sqlserver")
				|| productName.equals("sql server") || productName.equals(SQLSERVER))
			return new StoreTypeSqlserver(SQLSERVER);
		if (productName.equals(ORACLE))
			return new StoreTypeOracle(ORACLE);
		if (productName.startsWith(POSTGRES))
			return new StoreTypePostgres(POSTGRES);
		if (productName.startsWith(H2))
			return new StoreTypeH2(H2);
		if (productName.equals(MYSQL))
			return new StoreTypeMysql(MYSQL);
		if (productName.equals("my sql"))
			return new StoreTypeMysql(MYSQL);
		if (productName.equals(SQLITE))
			return new StoreTypeSqlite(SQLITE);
		return new StoreType(dbms);
	}

	/**
	 * Solo para uso interno, instancia un objeto Dbms sin ningun sgbd especifico
	 */
	public static StoreType get() {
		return get("");
	}

	// metodos basicos comunes
	public String getVendorName() {
		return this.dbmsVendorName;
	}
	public String getName() {
		return this.dbmsVendorName;
	}
	@Override
	public String toString() {
		return this.dbmsVendorName;
	}
	public boolean equals(StoreType other) { // NOSONAR
		if (other == null)
			throw new RuntimeException("DBMSType.equals+: Type to compare can't be null"); // NOSONAR
		return JavaCs.equalsIgnoreCase(this.toString(), other.toString());
	}

	// Determinacion del tipo de BD para aquellos que son conocidos (a concretar por las subclases)
	public boolean isUnknown() {
		return this.dbmsVendorName.equals("");
	}
	public boolean isOracle() {
		return false;
	}
	public boolean isSQLServer() {
		return false;
	}
	public boolean isPostgres() {
		return false;
	}
	public boolean isH2() {
		return false;
	}
	public boolean isMySQL() {
		return false;
	}
	public boolean isSqlite() {
		return false;
	}
	public boolean isOpenApi() {
		return false;
	}

	/**
	 * Devuelve la constante que indica el tipo de error conocido por el gestor (las
	 * subclases deben implementar el metodo para sus propios errores)
	 */
	public String getKnownError(String message) {
		return DBMS_UNKNOWN_ERROR;
	}

	/** 
	 * Devuelve el string apropiado para un valor logico en una cadena SQL
	 */
	public String sqlBoolean(boolean b) {
		return b ? "(1=1)" : "(1=0)";
	}

	/**
	 * Devuelve true si el dbms soporta identificadore entre corchetes
	 */
	public boolean supportsBracketQuotation() {
		return false;
	}

	/**
	 * Mapea el nombre interno de un tipo de datos con el nombre documentado del
	 * tipo, necesario en algunos sgbd como postgres
	 */
	public String mapAliasToDataType(String alias) {
		return alias;
	}

	/**
	 * Devuelve una expresion sql representando la funcion ISNULL/COALESCE
	 */
	public String sqlCoalesce(String expr, String sust) {
		return this.sqlCoalesceFunctionName().toUpperCase() + "(" + expr + "," + sust + ")";
	}

	public String sqlCoalesceFunctionName() {
		return "coalesce";
	}

	/**
	 * Devuelve el string apropiado para un valor logico en una cadena SQL
	 */
	public String sqlDropView() {
		return "DROP VIEW";
	}

	/**
	 * Los campos autoincrementales se pueden establecer sobre un tipo base
	 * anyadiendo un sufijo (sqlserver) o mediante un tipo especifico (postgres);
	 * esta funcion permite obtener el tipo base o el tipo especifico segun el sgbd
	 */
	public String getDataTypeIdentity(String baseType) {
		return baseType; // si se usa un tipo especifico la sublcase lo establecera
	}

	/**
	 * Algunos campos autoincrementales (p.e. jdbc con SQLServer) se identifican
	 * anyadiendo un sufijo al tipo base de la clave, este es el que devuelve este
	 * metodo
	 */
	public String getDataTypeIdentitySuffix() {
		return "";
	}

	/**
	 * Los campos identity (p.e. .net con SQLServer) no se identifican anyadiendo un
	 * sufijo sino que se debe ejecutar una query esta es el que devuelve este metodo
	 */
	public String getDataTypeIdentitySql(String tableName) {
		return "";
	}

	/**
	 * Comandos para habilitar/deshabilitar las columnas incrementales (por ejemplo
	 * existen en oracle, pero no en sqlserver)
	 */
	public String getEnableIdentityCommand(String tableName) {
		return "";
	}

	public String getDisableIdentityCommand(String tableName) {
		return "";
	}

	/**
	 * Indica si existen comandos para habilitar/deshabilitar la comprobacion de una
	 * clave ajena de forma individual
	 */
	public boolean canDisableForeignKey() {
		return false;
	}

	/**
	 * Comando para habilitar una clave ajena
	 */
	public String getEnableForeignKeyCommand(String tableName, String fkName) {
		return "";
	}

	/**
	 * Comando para deshabilitar una clave ajena
	 */
	public String getDisableForeignKeyCommand(String tableName, String fkName) {
		return "";
	}

	/**
	 * Comando para deshabilitar todas las constraints de una tabla, usado para
	 * deshabilitar fks cuando no se puede hacer de forma individual
	 */
	public String getDisableConstraintsCommand(String tableName) {
		return "";
	}

	/**
	 * Comando para habilitar todas las constraints de una tabla, usado para
	 * habilitar fks cuando no se puede hacer de forma individual
	 */
	public String getEnableConstraintsCommand(String tableName) {
		return "";
	}

	/**
	 * Caracteres y comandos que finalizan una query en un script y un bloque de queries
	 */
	public String getEndSQLQueryChar() {
		return ";";
	}

	public String getEndSQLBlockCommand() {
		return "";
	}

	/**
	 * Clausulas para definir como se ordenaran los valores null
	 */
	public String getSortNullsFirst() {
		return "NULLS FIRST";
	}

	public String getSortNullsLast() {
		return "NULLS LAST";
	}

	/**
	 * Especifica si se permiten alias de las columnas de las select como columnas
	 * en un orderby (p.e. oracle lo permite, sqlserver no)
	 */
	public boolean getAliasInOrderByAllowed() {
		return false;
	}

	/**
	 * Especifica si se requiere un alias cuando se representa una tabla derivada
	 * (ej. select * form (select...), oracle no lo requiere,sqlserver si
	 */
	public boolean getAliasInDerivedTableRequired() {
		return true;
	}

	/**
	 * Especifica difersas restricciones que pueden tener algunos SGBD (p.e. sqlite)
	 */
	public boolean getSupportsHavingWithoutGroupBy() {
		return true;
	}

	public boolean getSupportsRightJoin() {
		return true;
	}

	/**
	 * Query a utilizar para obtener el SQL de una vista. Segun el estandar SQL se
	 * puede obtener de INFORMATION_SCHEMA, pero limitado en numero de caracteres.
	 * Las subclases deberan implementar el metodo usando los recursos especificos
	 * del SGBD que tratan para que no se tenga esta limitacion
	 */
	public String getViewDefinitionSQL(String catalog, String schema, String viewName) {
		if (catalog == null)
			catalog = "";
		if (schema == null)
			schema = "";
		String sql = "SELECT CAST(VIEW_DEFINITION AS VARCHAR(4000)) AS VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE";
		if (!catalog.equals(""))
			sql += " TABLE_CATALOG='" + catalog + "' AND ";
		if (!schema.equals(""))
			sql += " TABLE_SCHEMA='" + schema + "' AND ";
		sql += " TABLE_NAME='" + viewName + "'";
		return sql;
	}

	// Determinacion de si se trata de un objeto del sistema. Cuando se buscan
	// tablas sin especificar un esquema por defecto, algunos DBMS devuelven tambien
	// otras tablas de este esquema o de otros esquemas que estan predefinidas por
	// el SGBD. Estos metodos las determinan para que puedan ser ingoradas si procede

	/** Determina si se trata de un esquema del sistema */
	public boolean isSystemSchema(String schemaName) {
		return false;
	}

	/** Determina si se trata de un esquema del sistema */
	public boolean isSystemTable(String tableName) {
		return false;
	}

	/** Determina si se trata de un esquema del sistema */
	public boolean isSystemView(String viewName) {
		return false;
	}

	// Recursos para limitar resultados de una query 

	/**
	 * Determina la condicion que se puede utilizar para limitar el maximo numero de
	 * filas devueltas (vacio si no existe en este SGBD)
	 */
	public String getMaxRowCondition(long maxRows) {
		return "";
	}

	/**
	 * Determina la clausula que se puede anyadir a SELECT para limitar el maximo
	 * numero de filas devueltas (vacio si no existe en este SGBD)
	 */
	public String getMaxRowSelectClause(long maxRows) {
		return "";
	}

	/**
	 * Determina la clausula que se puede anyadir al final de una query limitar el
	 * maximo numero de filas devueltas (vacio si no existe en este SGBD)
	 */
	public String getMaxRowsLimitClause(long maxRows) {
		return "";
	}

	/**
	 * Devuelve la query pasada como parametro con la restriccion de que devuelva
	 * solo el numero de filas indicado
	 */
	public String getSqlLimitRows(String sql, long maxRows) {
		return sql;
	}

	/**
	 * Determina la funcion que genera el ranking de una fila dentro de las que
	 * forman parte de un grupo
	 */
	public String getRankFunctionInGroup() {
		return "";
	}

	/**
	 * Determina si una version concreta DBMS soporta funciones over (partition
	 * by...) utilizadas en las optimizaciones de QAShrink
	 */
	public boolean getSupportsPartitionBy(int version) {
		return false; // por defecto no se sabe
	}

	/**
	 * devuelve la funcion substring correspondiente al gestor de base de datos
	 * actual
	 */
	public String getSQLSubstring(String str, int start, int length) {
		return "substring(" + str + "," + start + "," + length + ")";
	}

	/**
	 * devuelve el operador de concatenacion de strings correspondiente al gestor de
	 * base de datos actual
	 */
	public String getSQLStringConcat() {
		return "+";
	}

	/**
	 * Devuelve el tipo de datos del esquema correspondiente a una fecha+hora
	 * (timestamp es el valor estandar)
	 */
	public String getDataTypeDatetime() {
		return "timestamp"; // NOSONAR necesario metodo para hacer override en las subclases
	}

	/**
	 * Devuelve el string apropiado para un valor literal de tipo fecha y/o Hora a
	 * incluir en una sentencia sql:
	 * - si hay fecha y hora los concatena dando lugar a una fecha formato iso
	 * - si uno de estos strings esta vacio, muestra el otro
	 * 
	 * @param sDate string de fecha en formato yyyy-mm-dd
	 * @param sTime string de hora en formato hh:mm:ss
	 */
	public String getSqlDatetimeLiteral(String sDate, String sTime) {
		if (sDate.contains("T"))
			return sqlString(sDate); // si pasa una fecha iso en sDate la devuelve tal cual ignorando sTime
		else if ("".equals(sTime))
			return sqlString(sDate);
		else if ("".equals(sDate))
			return sqlString(sTime);
		else // con fecha y hora lo escribe en formato iso, con una T
			return sqlString(sDate + "T" + sTime);
	}

	/**
	 * Devuelve el string apropiado para que una columna de tipo fecha+hora en una
	 * sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
	 * HH:MM:SS. Implmentacion por defecto que no pone formato, debe existir una
	 * implmeentacion en las subclases
	 */
	public String getSqlDatetimeColumnString(String sCol) {
		return sCol;
	}

	/**
	 * Devuelve el string apropiado para que una columna de tipo fecha+hora en una
	 * sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
	 * Implmentacion por defecto que no pone formato, debe existir una
	 * implmeentacion en las subclases
	 */
	public String getSqlDateColumnString(String sCol) {
		return sCol;
	}

	protected String sqlString(String s) {
		return "'" + s.replace("'", "''").trim() + "'";
	}

}
