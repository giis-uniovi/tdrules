using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Stypes
{
    public class StoreTypeOracle : StoreType
    {
        public StoreTypeOracle(string dbms) : base(dbms)
        {
        }

        public override bool IsOracle()
        {
            return true;
        }

        public override bool CanDisableForeignKey()
        {
            return true;
        }

        public override string GetEnableForeignKeyCommand(string tableName, string fkName)
        {
            return "ALTER TABLE " + tableName + " ENABLE CONSTRAINT " + fkName;
        }

        public override string GetDisableForeignKeyCommand(string tableName, string fkName)
        {
            return "ALTER TABLE " + tableName + " DISABLE CONSTRAINT " + fkName;
        }

        public override bool GetAliasInOrderByAllowed()
        {
            return true;
        }

        /// <summary>
        /// Mensajes de error conocidos
        /// </summary>
        public override string GetKnownError(string message)
        {
            if (message.Contains("ORA-01013"))
                return DBMS_TIMEOUT;
            else if (message.Contains("ORA-01476"))
                return DBMS_DIVIDE_BY_ZERO;
            else if (message.Contains("ORA-01839"))
                return DBMS_INVALID_DATE_MONTH;
            return DBMS_UNKNOWN_ERROR; // cualquier otro lo marco como no conocido
        }

        /// <summary>
        /// Query a utilizar para obtener el SQL de una vista
        /// </summary>
        public override string GetViewDefinitionSQL(string catalog, string schema, string viewName)
        {

            // En Oracle no he localizado como definir el catalogo y esquema, de todas formas
            // esto se usa para manejar vistas en QAShrink, y no se soporten vistas con
            // nombre cualificado
            string sql = "SELECT TEXT AS VIEW_DEFINITION FROM USER_VIEWS WHERE";
            sql += " VIEW_NAME='" + viewName + "'";
            return sql;
        }

        /// <summary>
        /// Algunas tablas de sistema son visibles en Oracle (al menos en 10g)
        /// </summary>
        public override bool IsSystemTable(string tableName)
        {
            return tableName.Length >= 4 && (tableName.StartsWith("BIN$") || tableName.StartsWith("JAVA$") || tableName.StartsWith("CREATE$JAVA$"));
        }

        /// <summary>
        /// Especifica si se requiere un alias cuando se representa una tabla derivada
        /// (ej. select * form (select...), oracle no lo requiere,sqlserver si
        /// </summary>
        public override bool GetAliasInDerivedTableRequired()
        {
            return false;
        }

        /// <summary>
        /// Determina la condicion que se puede utilizar para limitar el maximo numero de
        /// filas devueltas
        /// </summary>
        public override string GetMaxRowCondition(long maxRows)
        {
            return "ROWNUM <= " + maxRows;
        }

        /// <summary>
        /// Devuelve la query pasada como parametro con la restriccion de que devuelva
        /// solo el numero de filas indicado
        /// </summary>
        public override string GetSqlLimitRows(string sql, long maxRows)
        {
            return "SELECT * FROM (" + sql + ") WHERE " + GetMaxRowCondition(maxRows);
        }

        /// <summary>
        /// Determina la funcion que genera el ranking de una fila dentro de las que
        /// forman parte de un grupo
        /// </summary>
        public override string GetRankFunctionInGroup()
        {
            return "row_number()";
        }

        /// <summary>
        /// Determina si el DBMS soporta funciones over (partition by...) utilizadas en
        /// las optimizaciones de QAShrink
        /// </summary>
        public virtual bool GetSupportsPartitionBy()
        {
            return true;
        }

        /// <summary>
        /// Determina si el DBMS soporta funciones over (partition by...) utilizadas en
        /// las optimizaciones de QAShrink
        /// </summary>
        public override bool GetSupportsPartitionBy(int version)
        {

            // Solo a partir de Oracle 8i segun
            // http://www.techonthenet.com/oracle/functions/rank.php
            return version >= 8;
        }

        /// <summary>
        /// devuelve la funcion substring correspondiente al gestor de base de datos
        /// actual
        /// </summary>
        public override string GetSQLSubstring(string str, int start, int length)
        {
            return "substr(" + str + "," + start + "," + length + ")";
        }

        /// <summary>
        /// devuelve el operador de concatenacion de strings correspondiente al gestor de
        /// base de datos actual
        /// </summary>
        public override string GetSQLStringConcat()
        {
            return "||";
        }

        /// <summary>
        /// Devuelve el string apropiado para un valor literal de tipo fecha y/o Hora a
        /// incluir en una sentencia sql: en oracle debe ser precedido por TIMESTAMP,
        /// DATE o INTERVAL (lo mas parecido a time)
        /// </summary>
        /// <param name="sDate">string de fecha en formato yyyy-mm-dd</param>
        /// <param name="sTime">string de hora en formato hh:mm:ss.d</param>
        public override string GetSqlDatetimeLiteral(string sDate, string sTime)
        {
            if (sDate.Contains("T"))
                return "TIMESTAMP " + SqlString(sDate).Replace("T", " ");
            else if ("".Equals(sTime))
                return "DATE " + SqlString(sDate);
            else if ("".Equals(sDate))
                return "INTERVAL " + SqlString(sTime);
            else
                return "TIMESTAMP " + SqlString(sDate + " " + sTime);
        }

        /// <summary>
        /// Devuelve el string apropiado para que una columna de tipo fecha+hora en una
        /// sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
        /// HH:MM:SS
        /// </summary>
        public override string GetSqlDatetimeColumnString(string sCol)
        {
            return "TO_CHAR(" + sCol + ", 'YYYY-MM-DD HH24:MI:SS')";
        }

        /// <summary>
        /// Devuelve el string apropiado para que una columna de tipo fecha+hora en una
        /// sentencia sql se muestre formateada como string en un formato YYYY-MM-DD
        /// </summary>
        public override string GetSqlDateColumnString(string sCol)
        {
            return "TO_CHAR(" + sCol + ", 'YYYY-MM-DD')";
        }
    }
}