using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Stypes
{
    /// <summary>
    /// Although non relational, Cassandra can be managed if using a jdbc compatible driver or wrapper
    /// </summary>
    public class StoreTypeCassandra : StoreType
    {
        public StoreTypeCassandra(string dbms) : base(dbms)
        {
        }

        public override bool IsCassandra()
        {
            return true;
        }
    }
}