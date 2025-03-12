using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Stypes
{
    /// <summary>
    /// A few features of mysql, not thoroughly tested.
    /// </summary>
    public class StoreTypeMysql : StoreType
    {
        public StoreTypeMysql(string dbms) : base(dbms)
        {
        }

        public override bool IsMySQL()
        {
            return true;
        }

        /// <summary>
        /// Determina la clausula que se puede anyadir al final de una query limitar el
        /// maximo numero de filas devueltas (vacio si no existe en este SGBD)
        /// </summary>
        public override string GetMaxRowsLimitClause(long maxRows)
        {
            return "LIMIT " + maxRows;
        }
    }
}