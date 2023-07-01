package test4giis.tdrules.store.rdb.oracle;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaChecks;

public class TestOracleSchemaChecks extends TestSqlserverSchemaChecks {
	public TestOracleSchemaChecks() {
		this.dbmsname="oracle";
		this.storesUpperCase=true;
		this.enableCheck="ENABLE";
	}

}
