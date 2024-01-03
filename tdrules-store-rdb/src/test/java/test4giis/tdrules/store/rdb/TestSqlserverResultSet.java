package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.portable.util.JavaCs;

/**
 * Reading basic data types from a ResultSet, check nullability and access by name or column number.
 * Intended to test the .net ResultSet wrapper implementation
 */
public class TestSqlserverResultSet extends Base {
	protected Connection dbt;
	protected ResultSet rs;

	protected String sCreate1 = "create table tabrs1 (pk integer primary key not null, "
			+ "tint integer, tchar varchar(16), tdate date, tlong bigint)";
	protected String sSelect1 = "select pk, tint, tchar, tdate, tlong from tabrs1";

	@Before
	public void setUp() throws SQLException {
		super.setUp();
		dbt = getConnection(TEST_DBNAME2);
		executeNotThrow(dbt, "drop table tabrs1");
		execute(dbt, sCreate1);
	}

	@After
	public void tearDown() throws SQLException {
		if (rs != null)
			rs.close();
		dbt.close();
	}

	@Test
	public void testReadResultSet() throws SQLException {
		execute(dbt, "insert into tabrs1 (pk,tint,tchar,tdate,tlong) values(1, 2, null, '2023-01-02', null)");
		execute(dbt, "insert into tabrs1 (pk,tint,tchar,tdate,tlong) values(2, null, 'chrchr', null, 55)");

		// 1st round: even columns are not null/odd columns are null (except pk), 
		// read by column number
		rs = query(dbt, sSelect1);
		assertTrue(rs.next());
		assertEquals(1, rs.getInt(1));

		int tint = rs.getInt(2);
		assertFalse(rs.wasNull());
		assertEquals(2, tint);
		assertEquals("2", rs.getString(2));

		String tchar = rs.getString(3);
		assertTrue(rs.wasNull());
		assertEquals(null, tchar);

		Date tdate;
		if (!"sqlite".equals(dbmsname)) { // sqlite does not have native Date
			tdate = rs.getDate(4);
			assertFalse(rs.wasNull());
			assertEquals("2023-01-02", dateString(tdate)); // normalize Date for .net compatibility
			assertNotNull(rs.getString(4)); // do not check value because is system/locale dependent
		}

		long tlong = rs.getLong(5);
		assertTrue(rs.wasNull());
		assertEquals(0, tlong);

		// 2nd round: alternate nullability, read by column name
		assertTrue(rs.next());
		assertEquals(2, rs.getInt("pk"));

		tint = rs.getInt("tint");
		assertTrue(rs.wasNull());
		assertEquals(0, tint);
		assertEquals(null, rs.getString("tint"));

		tchar = rs.getString("tchar");
		assertFalse(rs.wasNull());
		assertEquals("chrchr", tchar);

		if (!"sqlite".equals(dbmsname)) {
			tdate = rs.getDate("tdate");
			assertTrue(rs.wasNull());
			if ("java".equals(PLATFORM))
				assertEquals(null, tdate);
			else // on .net, DateTime is no nullable, returns DateTime.MinValue
				assertEquals("0001-01-01", dateString(tdate));
			assertEquals(null, rs.getString("tdate"));
		}

		tlong = rs.getLong("tlong");
		assertFalse(rs.wasNull());
		assertEquals(55, tlong);
		assertEquals("55", rs.getString("tlong"));

		// resultset end
		assertFalse(rs.next());
	}

	private String dateString(Date dt) {
		return JavaCs.substring(JavaCs.getIsoDate(dt), 0, 10);
	}
}
