using Test4giis.Tdrules.Store.Rdb;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Rdb.Sqlite
{
    public class TestSqliteSchemaRead : TestSqlserverSchemaRead
    {
        public TestSqliteSchemaRead()
        {
            this.dbmsname = "sqlite";
            this.dbmsproductname = "SQLite";
            this.myCatalogSchema2 = "";
        }
    }
}