package test4giis.tdrules.store.rdb.postgres;

import test4giis.tdrules.store.rdb.TestSqlserverDates;

public class TestPostgresDates extends TestSqlserverDates {
	public TestPostgresDates() {
		this.dbmsname = "postgres";
		this.DATETIME = "TIMESTAMP";
		this.DATETIME_SIZE = 29;
		this.DATETIME_DIGITS = 6;
		this.DATETIME_PREFIX_AS_SQL_STRING = "TIMESTAMP ";
	}

}
