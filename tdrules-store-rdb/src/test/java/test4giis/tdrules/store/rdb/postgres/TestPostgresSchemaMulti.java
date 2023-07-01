package test4giis.tdrules.store.rdb.postgres;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaChecks;

public class TestPostgresSchemaMulti extends TestSqlserverSchemaChecks {
	public TestPostgresSchemaMulti() {
		this.dbmsname = "postgres";
		this.storesLowerCase = true;
	}

}
