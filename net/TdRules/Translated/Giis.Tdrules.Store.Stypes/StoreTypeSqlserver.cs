using Giis.Portable.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Stypes
{
    public class StoreTypeSqlserver : StoreType
    {
        public StoreTypeSqlserver(string dbms) : base(dbms)
        {
        }

        public override bool IsSQLServer()
        {
            return true;
        }

        public override bool SupportsBracketQuotation()
        {
            return true;
        }

        public override bool CanDisableForeignKey()
        {
            return true;
        }

        public override string GetEnableIdentityCommand(string tableName)
        {
            return "SET IDENTITY_INSERT " + tableName + " ON";
        }

        public override string GetDisableIdentityCommand(string tableName)
        {
            return "SET IDENTITY_INSERT " + tableName + " OFF";
        }

        public override string GetEnableForeignKeyCommand(string tableName, string fkName)
        {
            return "ALTER TABLE " + tableName + " CHECK CONSTRAINT " + fkName;
        }

        public override string GetDisableForeignKeyCommand(string tableName, string fkName)
        {
            return "ALTER TABLE " + tableName + " NOCHECK CONSTRAINT " + fkName;
        }

        public override string GetEndSQLQueryChar()
        {
            return "";
        }

        public override string GetEndSQLBlockCommand()
        {
            return "GO";
        }

        public override bool GetAliasInOrderByAllowed()
        {
            return false;
        }

        /// <summary>
        /// Los campos autoincrementales en sqlserver se identifican anyadiendo un sufijo
        /// identity al tipo base de la clave, este es el que devuelve este metodo
        /// </summary>
        public override string GetDataTypeIdentitySuffix()
        {
            return "identity";
        }

        /// <summary>
        /// Los campos autoincrementales (p.e. .net con SQLServer) no se identifican
        /// anyadiendo un sufijo sino que se debe ejecutar una query, esta es el que
        /// devuelve este metodo
        /// </summary>
        public override string GetDataTypeIdentitySql(string tableName, string columnName)
        {
            return "select name from sys.identity_columns where [object_id] = object_id('" + tableName + "') and name = '" + columnName + "'";
        }

        /// <summary>
        /// Query a utilizar para obtener el SQL de una vista sin limitar el numero de caracteres
        /// https://stackoverflow.com/questions/4765323/is-there-a-way-to-retrieve-the-view-definition-from-a-sql-server-using-plain-ado
        /// </summary>
        public override string GetViewDefinitionSQL(string catalog, string schema, string viewName)
        {
            string catSchema = "";
            if (catalog != null && !"".Equals(catalog))
                catSchema += catalog + ".";
            if (schema != null && !"".Equals(schema))
                catSchema += schema + ".";
            string sql = "SELECT definition AS VIEW_DEFINITION FROM sys.objects o";
            sql += " JOIN sys.sql_modules m on m.object_id = o.object_id WHERE";
            sql += " o.object_id = object_id('" + catSchema + viewName + "') and o.type = 'V'";
            return sql;
        }

        /// <summary>
        /// En SQLServer, a partir de 2005 un usuario (posiblemente con privilegios)
        /// puede ver las vistas que estan en estos esquemas del sistema
        /// </summary>
        public override bool IsSystemSchema(string schemaName)
        {
            return JavaCs.EqualsIgnoreCase(schemaName, "information_schema") || JavaCs.EqualsIgnoreCase(schemaName, "sys");
        }

        /// <summary>
        /// Jul 2022, En alguna BD ha aparecido sysdiagrams que se ve como user table
        /// pero que parece que es creada por el management studio
        /// </summary>
        public override bool IsSystemTable(string tableName)
        {
            return JavaCs.EqualsIgnoreCase(tableName, "sysdiagrams");
        }

        /// <summary>
        /// En SQLServer 2000 (no a partir de 2005) hay otras vistas visibles por el usuario
        /// </summary>
        public override bool IsSystemView(string viewName)
        {
            return JavaCs.EqualsIgnoreCase(viewName, "sysconstraints") || JavaCs.EqualsIgnoreCase(viewName, "syssegments");
        }

        /// <summary>
        /// Determina la clausula que se puede anyadir a SELECT para limitar el maximo
        /// numero de filas devueltas (vacio si no existe en este SGBD)
        /// </summary>
        public override string GetMaxRowSelectClause(long maxRows)
        {
            return "TOP " + maxRows;
        }

        /// <summary>
        /// Devuelve la query pasada como parametro con la restriccion de que devuelva
        /// solo el numero de filas indicado
        /// </summary>
        public override string GetSqlLimitRows(string sql, long maxRows)
        {
            return "SELECT " + GetMaxRowSelectClause(maxRows) + " * FROM (" + sql + ") X";
        }

        /// <summary>
        /// Determina la funcion que genera el ranking de una fila dentro de las que
        /// forman parte de un grupo
        /// </summary>
        public override string GetRankFunctionInGroup()
        {
            return "rank()";
        }

        /// <summary>
        /// Determina si el DBMS soporta funciones over (partition by...) utilizadas en
        /// las optimizaciones de QAShrink
        /// </summary>
        public override bool GetSupportsPartitionBy(int version)
        {

            // Solo a partir de SQLServer 2005 (9.0) segun
            // http://archive.cpradio.org/work/row_number-and-partition-by-in-sql-server-2000/
            return version >= 9;
        }

        /// <summary>
        /// Devuelve el tipo de datos del esquema correspondiente a una fecha+hora
        /// (datetime es el valor en sqlserver)
        /// </summary>
        public override string GetDataTypeDatetime()
        {
            return "datetime";
        }

        /// <summary>
        /// Devuelve el string apropiado para un valor literal de tipo fecha y/o Hora a
        /// incluir en una sentencia sql: - si hay fecha y hora los concatena (formato
        /// iso) de forma que se puedan utilizar para insertar valores en una tabla - si
        /// solo hay una fecha utiliza el formato sin guiones porque con guiones funciona
        /// bien si se inserta en un campo DATE pero no en un DATETIME - si no hay fecha
        /// muestra la hora tal y como se ha recibido
        /// </summary>
        /// <param name="sDate">string de fecha en formato yyyy-mm-dd</param>
        /// <param name="sTime">string de hora en formato hh:mm:ss.d</param>
        public override string GetSqlDatetimeLiteral(string sDate, string sTime)
        {
            if (sDate.Contains("T"))
                return SqlString(sDate); // si se pasa una fecha iso en sDate la devuelve tal cual ignorando sTime
            else if ("".Equals(sTime))
                return SqlString(sDate.Replace("-", "")); // en sqlserver si solo hay fecha debe tener formato sin guiones
            else if ("".Equals(sDate))
                return SqlString(sTime);
            else

                // con fecha y hora lo escribe en formato iso, con una T
                return SqlString(sDate + "T" + sTime);
        }

        /// <summary>
        /// Devuelve el string apropiado para que una columna de tipo fecha+hora en una
        /// sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
        /// HH:MM:SS
        /// </summary>
        public override string GetSqlDatetimeColumnString(string sCol)
        {
            return "CONVERT(CHAR(19)," + sCol + ",120)";
        }

        /// <summary>
        /// Devuelve el string apropiado para que una columna de tipo fecha+hora en una
        /// sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
        /// </summary>
        public override string GetSqlDateColumnString(string sCol)
        {
            return "CONVERT(CHAR(10)," + sCol + ",23)";
        }
    }
}