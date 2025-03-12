using Giis.Portable.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Stypes
{
    /// <summary>
    /// Manages the variability of capabilities and behaviours of Data Stores.
    /// 
    /// Behaves as a factory by calling the get method with a string
    /// representing the name of the data store and returning the specific implementation.
    /// 
    /// NOTE: This class provides default implementations that must be overriden
    /// by the subclasses. Do not assume that implementations are complete,
    /// they are added as needed.
    /// </summary>
    public class StoreType
    {
        protected string dbmsVendorName = "";
        private static readonly string MYSQL = "mysql";
        private static readonly string SQLITE = "sqlite";
        private static readonly string H2 = "h2";
        private static readonly string ORACLE = "oracle";
        private static readonly string SQLSERVER = "sqlserver";
        private static readonly string POSTGRES = "postgres";
        // Although non relational, Cassandra can be managed if using a jdbc compatible driver or wrapper
        private static readonly string CASSANDRA = "cassandra";
        // nombres de los errores de ejecucion conocidos para el SGBD.
        // Las subclases para cada DBMS implementaran los metodos para obtener estos
        // valores a partir de los mensajes de error
        public static readonly string DBMS_UNKNOWN_ERROR = "unknown";
        public static readonly string DBMS_TIMEOUT = "Query timeout";
        public static readonly string DBMS_DIVIDE_BY_ZERO = "Divisor is equal to zero";
        public static readonly string DBMS_INVALID_DATE_MONTH = "Date not valid for month specified";
        // Constructor (privado, la instanciacion se realiza con el metodo get)
        protected StoreType(string dbms)
        {
            this.dbmsVendorName = dbms;
        }

        /// <summary>
        /// Factoria que devuelve el objeto correspondiente que implementa las
        /// particularidades del vendedor
        /// </summary>
        public static StoreType Get(string dbms)
        {
            string productName = dbms.ToLower();
            if (productName.Equals("openapi"))
                return new StoreTypeOpenApi("openapi");
            if (productName.Equals("microsoft sql server") || productName.Equals("microsoft sqlserver") || productName.Equals("sql server") || productName.Equals(SQLSERVER))
                return new StoreTypeSqlserver(SQLSERVER);
            if (productName.Equals(ORACLE))
                return new StoreTypeOracle(ORACLE);
            if (productName.StartsWith(POSTGRES))
                return new StoreTypePostgres(POSTGRES);
            if (productName.StartsWith(H2))
                return new StoreTypeH2(H2);
            if (productName.Equals(MYSQL))
                return new StoreTypeMysql(MYSQL);
            if (productName.Equals("my sql"))
                return new StoreTypeMysql(MYSQL);
            if (productName.Equals(SQLITE))
                return new StoreTypeSqlite(SQLITE);
            if (productName.Equals(CASSANDRA))
                return new StoreTypeCassandra(CASSANDRA);
            return new StoreType(dbms);
        }

        /// <summary>
        /// Solo para uso interno, instancia un objeto Dbms sin ningun sgbd especifico
        /// </summary>
        public static StoreType Get()
        {
            return Get("");
        }

        // metodos basicos comunes
        public virtual string GetVendorName()
        {
            return this.dbmsVendorName;
        }

        public virtual string GetName()
        {
            return this.dbmsVendorName;
        }

        public override string ToString()
        {
            return this.dbmsVendorName;
        }

        public virtual bool Equals(StoreType other)
        {

            // NOSONAR
            if (other == null)
                throw new Exception("DBMSType.equals+: Type to compare can't be null"); // NOSONAR
            return JavaCs.EqualsIgnoreCase(this.ToString(), other.ToString());
        }

        // Determinacion del tipo de BD para aquellos que son conocidos (a concretar por las subclases)
        public virtual bool IsUnknown()
        {
            return this.dbmsVendorName.Equals("");
        }

        public virtual bool IsOracle()
        {
            return false;
        }

        public virtual bool IsSQLServer()
        {
            return false;
        }

        public virtual bool IsPostgres()
        {
            return false;
        }

        public virtual bool IsH2()
        {
            return false;
        }

        public virtual bool IsMySQL()
        {
            return false;
        }

        public virtual bool IsSqlite()
        {
            return false;
        }

        public virtual bool IsCassandra()
        {
            return false;
        }

        public virtual bool IsOpenApi()
        {
            return false;
        }

        /// <summary>
        /// Devuelve la constante que indica el tipo de error conocido por el gestor (las
        /// subclases deben implementar el metodo para sus propios errores)
        /// </summary>
        public virtual string GetKnownError(string message)
        {
            return DBMS_UNKNOWN_ERROR;
        }

        /// <summary>
        /// Devuelve el string apropiado para un valor logico en una cadena SQL
        /// </summary>
        public virtual string SqlBoolean(bool b)
        {
            return b ? "(1=1)" : "(1=0)";
        }

        /// <summary>
        /// Devuelve true si el dbms soporta identificadore entre corchetes
        /// </summary>
        public virtual bool SupportsBracketQuotation()
        {
            return false;
        }

        /// <summary>
        /// Mapea el nombre interno de un tipo de datos con el nombre documentado del
        /// tipo, necesario en algunos sgbd como postgres
        /// </summary>
        public virtual string MapAliasToDataType(string alias)
        {
            return alias;
        }

        /// <summary>
        /// Devuelve una expresion sql representando la funcion ISNULL/COALESCE
        /// </summary>
        public virtual string SqlCoalesce(string expr, string sust)
        {
            return this.SqlCoalesceFunctionName().ToUpper() + "(" + expr + "," + sust + ")";
        }

        public virtual string SqlCoalesceFunctionName()
        {
            return "coalesce";
        }

        /// <summary>
        /// Devuelve el string apropiado para un valor logico en una cadena SQL
        /// </summary>
        public virtual string SqlDropView()
        {
            return "DROP VIEW";
        }

        /// <summary>
        /// Los campos autoincrementales se pueden establecer sobre un tipo base
        /// anyadiendo un sufijo (sqlserver) o mediante un tipo especifico (postgres);
        /// esta funcion permite obtener el tipo base o el tipo especifico segun el sgbd
        /// </summary>
        public virtual string GetDataTypeIdentity(string baseType)
        {
            return baseType; // si se usa un tipo especifico la sublcase lo establecera
        }

        /// <summary>
        /// Algunos campos autoincrementales (p.e. jdbc con SQLServer) se identifican
        /// anyadiendo un sufijo al tipo base de la clave, este es el que devuelve este
        /// metodo
        /// </summary>
        public virtual string GetDataTypeIdentitySuffix()
        {
            return "";
        }

        /// <summary>
        /// Algunos campos autoincrementales (p.e. .net con SQLServer o sqlite) se identifican
        /// ejecuando una query que es la que se define en este metodo
        /// </summary>
        public virtual string GetDataTypeIdentitySql(string tableName, string columnName)
        {
            return "";
        }

        /// <summary>
        /// Comandos para habilitar/deshabilitar las columnas incrementales (por ejemplo
        /// existen en oracle, pero no en sqlserver)
        /// </summary>
        public virtual string GetEnableIdentityCommand(string tableName)
        {
            return "";
        }

        public virtual string GetDisableIdentityCommand(string tableName)
        {
            return "";
        }

        /// <summary>
        /// Indica si existen comandos para habilitar/deshabilitar la comprobacion de una
        /// clave ajena de forma individual
        /// </summary>
        public virtual bool CanDisableForeignKey()
        {
            return false;
        }

        /// <summary>
        /// Comando para habilitar una clave ajena
        /// </summary>
        public virtual string GetEnableForeignKeyCommand(string tableName, string fkName)
        {
            return "";
        }

        /// <summary>
        /// Comando para deshabilitar una clave ajena
        /// </summary>
        public virtual string GetDisableForeignKeyCommand(string tableName, string fkName)
        {
            return "";
        }

        /// <summary>
        /// Comando para deshabilitar todas las constraints de una tabla, usado para
        /// deshabilitar fks cuando no se puede hacer de forma individual
        /// </summary>
        public virtual string GetDisableConstraintsCommand(string tableName)
        {
            return "";
        }

        /// <summary>
        /// Comando para habilitar todas las constraints de una tabla, usado para
        /// habilitar fks cuando no se puede hacer de forma individual
        /// </summary>
        public virtual string GetEnableConstraintsCommand(string tableName)
        {
            return "";
        }

        /// <summary>
        /// Caracteres y comandos que finalizan una query en un script y un bloque de queries
        /// </summary>
        public virtual string GetEndSQLQueryChar()
        {
            return ";";
        }

        public virtual string GetEndSQLBlockCommand()
        {
            return "";
        }

        /// <summary>
        /// Clausulas para definir como se ordenaran los valores null
        /// </summary>
        public virtual string GetSortNullsFirst()
        {
            return "NULLS FIRST";
        }

        public virtual string GetSortNullsLast()
        {
            return "NULLS LAST";
        }

        /// <summary>
        /// Especifica si se permiten alias de las columnas de las select como columnas
        /// en un orderby (p.e. oracle lo permite, sqlserver no)
        /// </summary>
        public virtual bool GetAliasInOrderByAllowed()
        {
            return false;
        }

        /// <summary>
        /// Especifica si se requiere un alias cuando se representa una tabla derivada
        /// (ej. select * form (select...), oracle no lo requiere,sqlserver si
        /// </summary>
        public virtual bool GetAliasInDerivedTableRequired()
        {
            return true;
        }

        /// <summary>
        /// Especifica difersas restricciones que pueden tener algunos SGBD (p.e. sqlite)
        /// </summary>
        public virtual bool GetSupportsHavingWithoutGroupBy()
        {
            return true;
        }

        public virtual bool GetSupportsRightJoin()
        {
            return true;
        }

        /// <summary>
        /// Query a utilizar para obtener el SQL de una vista. Segun el estandar SQL se
        /// puede obtener de INFORMATION_SCHEMA, pero limitado en numero de caracteres.
        /// Las subclases deberan implementar el metodo usando los recursos especificos
        /// del SGBD que tratan para que no se tenga esta limitacion
        /// </summary>
        public virtual string GetViewDefinitionSQL(string catalog, string schema, string viewName)
        {
            if (catalog == null)
                catalog = "";
            if (schema == null)
                schema = "";
            string sql = "SELECT CAST(VIEW_DEFINITION AS VARCHAR(4000)) AS VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE";
            if (!catalog.Equals(""))
                sql += " TABLE_CATALOG='" + catalog + "' AND ";
            if (!schema.Equals(""))
                sql += " TABLE_SCHEMA='" + schema + "' AND ";
            sql += " TABLE_NAME='" + viewName + "'";
            return sql;
        }

        // Determinacion de si se trata de un objeto del sistema. Cuando se buscan
        // tablas sin especificar un esquema por defecto, algunos DBMS devuelven tambien
        // otras tablas de este esquema o de otros esquemas que estan predefinidas por
        // el SGBD. Estos metodos las determinan para que puedan ser ingoradas si procede
        /// <summary>
        /// Determina si se trata de un esquema del sistema
        /// </summary>
        public virtual bool IsSystemSchema(string schemaName)
        {
            return false;
        }

        /// <summary>
        /// Determina si se trata de un esquema del sistema
        /// </summary>
        public virtual bool IsSystemTable(string tableName)
        {
            return false;
        }

        /// <summary>
        /// Determina si se trata de un esquema del sistema
        /// </summary>
        public virtual bool IsSystemView(string viewName)
        {
            return false;
        }

        // Recursos para limitar resultados de una query 
        /// <summary>
        /// Determina la condicion que se puede utilizar para limitar el maximo numero de
        /// filas devueltas (vacio si no existe en este SGBD)
        /// </summary>
        public virtual string GetMaxRowCondition(long maxRows)
        {
            return "";
        }

        /// <summary>
        /// Determina la clausula que se puede anyadir a SELECT para limitar el maximo
        /// numero de filas devueltas (vacio si no existe en este SGBD)
        /// </summary>
        public virtual string GetMaxRowSelectClause(long maxRows)
        {
            return "";
        }

        /// <summary>
        /// Determina la clausula que se puede anyadir al final de una query limitar el
        /// maximo numero de filas devueltas (vacio si no existe en este SGBD)
        /// </summary>
        public virtual string GetMaxRowsLimitClause(long maxRows)
        {
            return "";
        }

        /// <summary>
        /// Devuelve la query pasada como parametro con la restriccion de que devuelva
        /// solo el numero de filas indicado
        /// </summary>
        public virtual string GetSqlLimitRows(string sql, long maxRows)
        {
            return sql;
        }

        /// <summary>
        /// Determina la funcion que genera el ranking de una fila dentro de las que
        /// forman parte de un grupo
        /// </summary>
        public virtual string GetRankFunctionInGroup()
        {
            return "";
        }

        /// <summary>
        /// Determina si una version concreta DBMS soporta funciones over (partition
        /// by...) utilizadas en las optimizaciones de QAShrink
        /// </summary>
        public virtual bool GetSupportsPartitionBy(int version)
        {
            return false; // por defecto no se sabe
        }

        /// <summary>
        /// devuelve la funcion substring correspondiente al gestor de base de datos
        /// actual
        /// </summary>
        public virtual string GetSQLSubstring(string str, int start, int length)
        {
            return "substring(" + str + "," + start + "," + length + ")";
        }

        /// <summary>
        /// devuelve el operador de concatenacion de strings correspondiente al gestor de
        /// base de datos actual
        /// </summary>
        public virtual string GetSQLStringConcat()
        {
            return "+";
        }

        /// <summary>
        /// Devuelve el tipo de datos del esquema correspondiente a una fecha+hora
        /// (timestamp es el valor estandar)
        /// </summary>
        public virtual string GetDataTypeDatetime()
        {
            return "timestamp"; // NOSONAR necesario metodo para hacer override en las subclases
        }

        /// <summary>
        /// Devuelve el string apropiado para un valor literal de tipo fecha y/o Hora a
        /// incluir en una sentencia sql:
        /// - si hay fecha y hora los concatena dando lugar a una fecha formato iso
        /// - si uno de estos strings esta vacio, muestra el otro
        /// </summary>
        /// <param name="sDate">string de fecha en formato yyyy-mm-dd</param>
        /// <param name="sTime">string de hora en formato hh:mm:ss</param>
        public virtual string GetSqlDatetimeLiteral(string sDate, string sTime)
        {
            if (sDate.Contains("T"))
                return SqlString(sDate); // si pasa una fecha iso en sDate la devuelve tal cual ignorando sTime
            else if ("".Equals(sTime))
                return SqlString(sDate);
            else if ("".Equals(sDate))
                return SqlString(sTime);
            else

                // con fecha y hora lo escribe en formato iso, con una T
                return SqlString(sDate + "T" + sTime);
        }

        /// <summary>
        /// Devuelve el string apropiado para que una columna de tipo fecha+hora en una
        /// sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
        /// HH:MM:SS. Implmentacion por defecto que no pone formato, debe existir una
        /// implmeentacion en las subclases
        /// </summary>
        public virtual string GetSqlDatetimeColumnString(string sCol)
        {
            return sCol;
        }

        /// <summary>
        /// Devuelve el string apropiado para que una columna de tipo fecha+hora en una
        /// sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
        /// Implmentacion por defecto que no pone formato, debe existir una
        /// implmeentacion en las subclases
        /// </summary>
        public virtual string GetSqlDateColumnString(string sCol)
        {
            return sCol;
        }

        protected virtual string SqlString(string s)
        {
            return "'" + s.Replace("'", "''").Trim() + "'";
        }
    }
}