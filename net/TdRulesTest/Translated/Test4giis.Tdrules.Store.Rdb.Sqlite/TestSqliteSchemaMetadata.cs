using Test4giis.Tdrules.Store.Rdb;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Rdb.Sqlite
{
    public class TestSqliteSchemaMetadata : TestSqlserverSchemaMetadata
    {
        public TestSqliteSchemaMetadata()
        {
            this.dbmsname = "sqlite";

            // Uses the same datatypes that for sqlserver,
            // only changing the autoincrement definition of pk column
            this.sTypes1 = base.sTypes1.Replace("int identity primary key", "integer primary key autoincrement");
        }
    }
}