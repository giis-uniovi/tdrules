package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.tdrules.store.stypes.StoreType;

/**
 * Reading and writen date related fields, with and without time
 */
public class TestSqlserverDates extends Base {
	protected String catalog = null;
	protected String schema = null;
	protected StoreType dbms;
	protected String DATETIME = "datetime";
	protected int DATETIME_SIZE = 0;
	protected int DATETIME_DIGITS = 0;
	protected String DATETIME_PREFIX_AS_SQL_STRING = "";
	protected Connection dbt;

	@Before
	public void setUp() throws SQLException {
		super.setUp();
		dbms = StoreType.get(dbmsname);
		dbt = getConnection(TEST_DBNAME2);
		executeNotThrow(dbt, "drop table sdates");
		execute(dbt, "create table sdates (Pk1 int, primary key(Pk1), cdatetime " + dbms.getDataTypeDatetime()
				+ ", cdate date)");
	}

	@After
	public void tearDown() throws SQLException {
		dbt.close();
	}

	@Test
	public void testReaderDatesAndTimes() throws SQLException {
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.readTable("sdates");
		assertEqualsDBObj("sdates", mr.getTableName());
		assertEqualsDBObj("cdatetime", mr.getColumn(1).getColName());
		assertEqualsDBObj(DATETIME, mr.getColumn(1).getDataType());
		assertEquals(DATETIME_SIZE, mr.getColumn(1).getColSize());
		assertEquals(DATETIME_DIGITS, mr.getColumn(1).getDecimalDigits());
		// Metodo para obtener el string de una fecha tal y como se admite para una sentencia sql
		assertEquals(DATETIME_PREFIX_AS_SQL_STRING + "'2013-05-01 10:02:01'",
				mr.getColumn(1).getAsSqlString("2013-05-01 10:02:01"));
		assertEquals("NULL", mr.getColumn(1).getAsSqlString(null));

		assertEqualsDBObj("cdate", mr.getColumn(2).getColName());
		assertEqualsDBObj("date", mr.getColumn(2).getDataType());
		assertEquals(0, mr.getColumn(2).getColSize());
		assertEquals(0, mr.getColumn(2).getDecimalDigits());
		assertEquals("DATE '2013-05-01'", mr.getColumn(2).getAsSqlString("2013-05-01 10:02:01"));
		assertEquals("NULL", mr.getColumn(2).getAsSqlString(null));
	}

	@Test
	public void testReadAndWrite() throws SQLException {
		// Escritura de los datos a la bd de literales datetime y date
		// En dos filas, en la primera coincide el literal escrito con el tipo de dato,
		// en la segunda al reves (escribe datetime en date y date en datetime)
		String cdatetime = dbms.getSqlDatetimeLiteral("2022-12-30", "14:10:11");
		String cdate = dbms.getSqlDatetimeLiteral("2022-12-30", "");
		String sql = "insert into sdates (pk1,cdatetime,cdate) values( 1, " + cdatetime + ", " + cdate + ")";
		execute(dbt, sql);
		sql = "insert into sdates (pk1,cdatetime,cdate) values( 2, " + cdate + ", " + cdatetime + ")";
		execute(dbt, sql);

		// Recuperacion de los datos insertados, formateados como string
		sql = "select pk1, " + dbms.getSqlDatetimeColumnString("cdatetime") + " as cdatetime, "
				+ dbms.getSqlDateColumnString("cdate") + " as cdate from sdates order by pk1";
		ResultSet rs = query(dbt, sql);
		rs.next();
		assertEquals("2022-12-30 14:10:11", rs.getString("cdatetime"));
		assertEquals("2022-12-30", rs.getString("cdate"));
		rs.next();
		assertEquals("2022-12-30 00:00:00", rs.getString("cdatetime"));
		assertEquals("2022-12-30", rs.getString("cdate"));
	}

}
