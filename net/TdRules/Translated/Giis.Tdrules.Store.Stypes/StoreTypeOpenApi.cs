using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Stypes
{
    /// <summary>
    /// Specific features when the data store is obtained from an OpenApi specification
    /// </summary>
    public class StoreTypeOpenApi : StoreType
    {
        public StoreTypeOpenApi(string dbms) : base(dbms)
        {
        }

        /// <summary>
        /// Devuelve el string apropiado para un valor logico en una cadena SQL
        /// </summary>
        public override string SqlBoolean(bool b)
        {
            return b ? "true" : "false";
        }

        /// <summary>
        /// Devuelve true si soporta identificadore entre corchetes
        /// </summary>
        public override bool SupportsBracketQuotation()
        {
            return false;
        }
    }
}