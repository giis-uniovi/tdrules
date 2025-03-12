using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Dtypes
{
    public class SqlDataTypes : DataTypes
    {
        public SqlDataTypes() : base()
        {
        }

        /// <summary>
        /// Configuracion del mapeo id-tipo de datos
        /// </summary>
        protected override void ConfigureAllIds(IList<string> allTypes)
        {
            ConfigureId(allTypes, DT_CHARACTER, new string[] { "char", "character", "varchar", "varchar2", "nchar", "nvarchar", "nvarchar2", "text", "ntext" });
            ConfigureId(allTypes, DT_INTEGER, new string[] { "int", "integer", "smallint", "bigint", "tinyint", "long", "serial", "smallserial", "bigserial" });
            ConfigureId(allTypes, DT_EXACT_NUMERIC, new string[] { "numeric", "decimal", "number", "currency", "money", "smallmoney" });
            ConfigureId(allTypes, DT_APPROXIMATE_NUMERIC, new string[] { "float", "real", "double", "binary_float", "binary_double" });
            ConfigureId(allTypes, DT_LOGICAL, new string[] { "bit", "boolean" });
            ConfigureId(allTypes, DT_DATE, new string[] { "date" });
            ConfigureId(allTypes, DT_TIME, new string[] { "time" });
            ConfigureId(allTypes, DT_DATETIME, new string[] { "timestamp", "datetime", "smalldatetime" });
            ConfigureId(allTypes, DT_INTERVAL, new string[] { "interval" });
            ConfigureId(allTypes, DT_BLOB, new string[] { "blob", "longblob", "binary", "varbinary", "image" });
        }

        /// <summary>
        /// Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
        /// </summary>
        public override string GetDefault()
        {
            return "int";
        }
    }
}