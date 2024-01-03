package test4giis.tdrules.store.rdb.sqlite;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaMetadata;

public class TestSqliteSchemaMetadata extends TestSqlserverSchemaMetadata {

	public TestSqliteSchemaMetadata() {
		this.dbmsname = "sqlite";
		// Uses the same datatypes that for sqlserver,
		// only changing the autoincrement definition of pk column
		this.sTypes1 = super.sTypes1.replace("int identity primary key", "integer primary key autoincrement");
	}

}
