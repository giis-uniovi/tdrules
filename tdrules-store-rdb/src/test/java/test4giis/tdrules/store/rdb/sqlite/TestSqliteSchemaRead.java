package test4giis.tdrules.store.rdb.sqlite;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaRead;

public class TestSqliteSchemaRead extends TestSqlserverSchemaRead {
	public TestSqliteSchemaRead() {
		this.dbmsname="sqlite";
		this.dbmsproductname = "SQLite";
		this.myCatalogSchema2 = "";
	}
	
}
