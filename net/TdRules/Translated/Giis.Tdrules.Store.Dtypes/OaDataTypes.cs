using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Dtypes
{
    public class OaDataTypes : DataTypes
    {
        public OaDataTypes() : base()
        {
        }

        /// <summary>
        /// Configuracion del mapeo id-tipo de datos
        /// </summary>
        protected override void ConfigureAllIds(IList<string> allTypes)
        {
            ConfigureId(allTypes, DT_CHARACTER, new string[] { "string" });
            ConfigureId(allTypes, DT_INTEGER, new string[] { "integer", "int32", "int64" });
            ConfigureId(allTypes, DT_EXACT_NUMERIC, new string[] { });
            ConfigureId(allTypes, DT_APPROXIMATE_NUMERIC, new string[] { "number", "float", "double" });
            ConfigureId(allTypes, DT_LOGICAL, new string[] { "boolean" });
            ConfigureId(allTypes, DT_DATE, new string[] { "date" });
            ConfigureId(allTypes, DT_TIME, new string[] { });
            ConfigureId(allTypes, DT_DATETIME, new string[] { "date-time" });
            ConfigureId(allTypes, DT_INTERVAL, new string[] { });
            ConfigureId(allTypes, DT_BLOB, new string[] { "byte", "binary" });
        }

        /// <summary>
        /// Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
        /// </summary>
        public override string GetDefault()
        {
            return "integer";
        }
    }
}