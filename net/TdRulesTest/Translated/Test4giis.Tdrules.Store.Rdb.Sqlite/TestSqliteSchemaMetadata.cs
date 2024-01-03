/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Sharpen;
using Test4giis.Tdrules.Store.Rdb;

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
