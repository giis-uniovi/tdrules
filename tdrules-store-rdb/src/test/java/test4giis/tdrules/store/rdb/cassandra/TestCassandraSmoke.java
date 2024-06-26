package test4giis.tdrules.store.rdb.cassandra;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import test4giis.tdrules.store.rdb.Base;

/**
 * A simple smoke test, just to check the ability to read basic metadata from Cassandra.
 * Uses a single table, created at container startup
 */
public class TestCassandraSmoke extends Base {
	protected Connection dbt;

	public TestCassandraSmoke() {
		this.dbmsname = "cassandra";
	}

	@Before
	public void setUp() throws SQLException {
		super.setUp();
		dbt = getConnection(TEST_DBNAME2);
	}

	@After
	public void tearDown() throws SQLException {
		dbt.close();
	}

	@Test
	public void testReadTableList() throws SQLException {
		SchemaReader mr = new SchemaReaderJdbc(dbt);
		assertEquals("cassandra", mr.getDbmsType().toString());
		List<String> tables = mr.getTableList(true, true);
		System.out.println(tables.toString());
		assertEquals("[stypes]", tables.toString());
	}

	@Test
	public void testReadTableMetadata() throws SQLException {
		SchemaReader mr = new SchemaReaderJdbc(dbt);
		mr.readTable("stypes");
		assertEquals(true, mr.isTable());
		assertEquals(false, mr.isView());
		assertEquals(false, mr.isType());
		assertEquals("stypes", mr.getTableName());
		String metadata = getMetadataAsString(mr);
		assertMetadata(metadata, PLATFORM + "." + dbmsname + ".metadata.types1.txt");
	}

}
