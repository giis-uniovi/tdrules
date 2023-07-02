package test4giis.tdrules.store.rdb.oracle;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaRead;

public class TestOracleSchemaRead extends TestSqlserverSchemaRead {
	/**
	 * El constructor adapta algunos de los tipos de datos de TestDBRData que no
	 * existen o son diferentes en Oracle
	 */
	public TestOracleSchemaRead() {
		this.dbmsname = "oracle";
		this.dbmsproductname = "Oracle";
		this.storesUpperCase = true;
		this.stringConcat = "||";
		this.concatCharType = "CHAR";
		this.concatCharSize = 14;
		this.catalog = "";
		this.schema = TEST_DBNAME2.toUpperCase();
		this.sTab2 = this.sTab2.replace("bit", "number(1)");
		this.INT = "number";
		this.IDENTITY = "number";
		this.INT_precision = 38;
		this.DECIMAL = "number";
		this.BIT = "number";
		this.BIT_precision = 1;
		this.VARCHAR = "varchar2";
		this.myCatalogSchema2 = TEST_DBNAME2.toUpperCase() + ".";
	}

	@Override
	public void testQuotedIdentifiersWithBracket() {
		// no se hereda este test pues causa errores sintacticos
	}

}
