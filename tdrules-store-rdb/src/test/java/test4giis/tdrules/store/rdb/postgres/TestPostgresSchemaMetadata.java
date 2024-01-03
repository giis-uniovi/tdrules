package test4giis.tdrules.store.rdb.postgres;

import test4giis.tdrules.store.rdb.TestSqlserverSchemaMetadata;

public class TestPostgresSchemaMetadata extends TestSqlserverSchemaMetadata {

	public TestPostgresSchemaMetadata() {
		this.dbmsname = "postgres";
		// Most used postgres datatypes:
		// https://www.postgresql.org/docs/current/datatype.html#DATATYPE-TABLE
		this.sTypes1 = "create table stypes1 (int serial primary key not null,"
				// exact numeric
				+ "tinteger integer not null, tint4 int, tint8 bigint, tint2 smallint, "
				+ "tnum numeric default(8), tnum1 numeric(6), tnum2 numeric(6,2), "
				+ "tdec decimal, tdec1 decimal(6), tdec2 decimal(6,2), " + "tbit bit, tboolean boolean,"
				// approximate numeric
				+ "treal real, tfloat float(7), tdouble double precision, "
				// postgres specific money
				+ "tmoney money, "
				// characters
				+ "tcharacter character(3) default 'abc', tchar char(3), tvarchar varchar(16), ttext text, "
				// dates and times
				+ "tdate date, ttime time, ttimestamp timestamp, tinterval interval " + ")";
		// default value of char is returned as 'abc'::bpchar
	}

}
