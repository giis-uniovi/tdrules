package test4giis.tdrules.store.rdb.postgres;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaChecks;

public class TestPostgresSchemaChecks extends TestSqlserverSchemaChecks {
	public TestPostgresSchemaChecks() {
		this.dbmsname = "postgres";
		this.storesLowerCase = true;
	}

}
