package test4giis.tdrules.store.rdb.oracle;

import test4giis.tdrules.store.rdb.TestSqlserverDates;

public class TestOracleDates extends TestSqlserverDates {
	public TestOracleDates() {
		this.dbmsname="oracle";
		this.storesUpperCase=true;
		this.DATETIME="TIMESTAMP";
		this.DATETIME_SIZE=11;
		this.DATETIME_DIGITS=6;
		this.DATETIME_PREFIX_AS_SQL_STRING="TIMESTAMP ";
	}

}
