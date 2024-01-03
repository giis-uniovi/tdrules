package test4giis.tdrules.store.rdb.oracle;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaMetadata;

/**
 * Reading metadata for all datatypes
 */
public class TestOracleSchemaMetadata extends TestSqlserverSchemaMetadata {

	public TestOracleSchemaMetadata() {
		this.dbmsname = "oracle";
		this.storesUpperCase = true;
		// Most used oracle datatypes:
		// https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Data-Types.html
		sTypes1 = "create table stypes1 (" // does not have autoincrement columns
				// exact numeric
				+ "tinteger integer primary key not null, tint4 int not null, "
				+ "tnum numeric default(8), tnum1 numeric(6), tnum2 numeric(6,2), "
				+ "tdec decimal, tdec1 decimal(6), tdec2 decimal(6,2), "
				// approximate numeric
				+ "treal real, tfloat float(7), tfloat32 binary_float, tfloat64 binary_double, "
				// characters
				+ "tcharacter character(3) default 'abc', tchar char(3), tvarchar varchar(16), tvarchar2 varchar2(16), "
				// dates and times
				+ "tdate date, ttimestamp timestamp, ttimestamp3 timestamp(3), ttimestamptz timestamp(3) with time zone, "
				+ "tintervalym interval year to month, tintervalds interval day to second " + ")";
	}

}
