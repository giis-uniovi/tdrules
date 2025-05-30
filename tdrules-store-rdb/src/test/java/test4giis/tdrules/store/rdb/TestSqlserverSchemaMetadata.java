package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;

/**
 * Reading metadata for all datatypes (from views and tables).
 * As metadata has many differences between different DBMSs, asserts are made against external files.
 * Includes test for listing tables and views.
 */
public class TestSqlserverSchemaMetadata extends Base {
	protected Connection dbt;

	// Most used sqlserver datatypes:
	// https://learn.microsoft.com/en-us/sql/t-sql/data-types/data-types-transact-sql?view=sql-server-ver16
	protected String sTypes1 = "create table stypes1 (pkauto int identity primary key not null, "
			// exact numeric
			+ "tinteger integer not null, tint4 int, tint8 bigint, tint2 smallint, tint1 tinyint, "
			+ "tnum numeric default(8), tnum1 numeric(6), tnum2 numeric(6,2), "
			+ "tdec decimal, tdec1 decimal(6), tdec2 decimal(6,2), " + "tbit bit, " // replaces standard boolean
			// approximate numeric
			+ "treal real, tfloat float(7), " // no standard double
			// sqlserver specific money
			+ "tmoney money, tsmallmoney smallmoney, "
			// characters
			+ "tcharacter character(3) default 'abc', tchar char(3), tvarchar varchar(16), ttext text, "
			// dates and times
			+ "tdate date, ttime time, tdatetime datetime, ttimestamp timestamp, " // no interval
			+ "tdatetime2 datetime2, tsmalldatetime smalldatetime, tdatetimeoffset datetimeoffset " + ")";

	// View with arguments
	protected String sTypesvp = "create view stypesvp (p1,p2,p3) as select tinteger,tint4,tcharacter from stypes1";

	protected void createViewXXL() throws SQLException {
		// Las vistas que se guardan en INFORMATION_SCHEMA tienen un limite de 4000 caracteres, si son mayores se guarda null.
		// Para cada SGBD se deberan usar los metodos especificos de la plataforma al tratar vistas como la definida aqui
		String createViewMain = "create view stypesvxxl (p1,p2,p3) as ";
		String createViewSql = "select tinteger,tint4,tcharacter from stypes1";
		String addView = " \nunion all " + createViewSql;
		int numRepeats = 10000 / addView.length();
		StringBuilder sb = new StringBuilder();
		sb.append(createViewSql);
		for (int i = 0; i < numRepeats; i++)
			sb.append(addView);
		execute(dbt, createViewMain + sb.toString());
	}

	protected void createTablesAndViews() throws SQLException {
		execute(dbt, sTypes1);
		execute(dbt, sTypesvp);
	}

	protected void dropTablesAndViews() throws SQLException {
		executeNotThrow(dbt, "drop view stypesvp");
		executeNotThrow(dbt, "drop view stypesvxxl");
		executeNotThrow(dbt, "drop table stypes1");
	}

	@Before
	@Override
	public void setUp() throws SQLException {
		super.setUp();
		dbt = getConnection(TEST_DBNAME2);
		this.dropTablesAndViews();
	}

	@After
	public void tearDown() throws SQLException {
		dbt.close();
	}

	@Test
	public void testReadTableMetadata() throws SQLException {
		createTablesAndViews();
		SchemaReader mr = new SchemaReaderJdbc(dbt);
		mr.readTable("stypes1");
		assertEquals(true, mr.isTable());
		assertEquals(false, mr.isView());
		assertEquals(false, mr.isType());
		assertEqualsDBObj(asStored("stypes1"), mr.getTableName());
		String metadata = getMetadataAsString(mr);
		assertMetadata(metadata, PLATFORM + "." + dbmsname + ".metadata.types1.txt");
	}

	@Test
	public void testReadViewMetadata() throws SQLException {
		createTablesAndViews();
		SchemaReader mr = new SchemaReaderJdbc(dbt);
		mr.readTable("stypesvp");
		assertEquals(false, mr.isTable());
		assertEquals(true, mr.isView());
		assertEquals(false, mr.isType());
		assertEqualsDBObj(asStored("stypesvp"), mr.getTableName());
		String metadata = getMetadataAsString(mr);
		assertMetadata(metadata, PLATFORM + "." + dbmsname + ".metadata.typesvp.txt");
	}

	@Test
	public void testReadViewXXLMetadata() throws SQLException {
		createTablesAndViews();
		createViewXXL();
		SchemaReader mr = new SchemaReaderJdbc(dbt);
		mr.readTable("stypesvxxl");
		assertEquals(false, mr.isTable());
		assertEquals(true, mr.isView());
		assertEquals(false, mr.isType());
		assertEqualsDBObj(asStored("stypesvxxl"), mr.getTableName());
		String metadata = getMetadataAsString(mr);
		assertMetadata(metadata, PLATFORM + "." + dbmsname + ".metadata.typesvxxl.txt");
		// nota: oracle no permite conocer la nullabilidad en la vista generada con union
	}

	/**
	 * List de tablas y/o vistas, discriminado por tipo
	 */
	@Test
	public void testReadListTableAndView() throws SQLException {
		createTablesAndViews();
		createViewXXL();
		// comprueba los nombres, de estas tablas que se han creado (puede haber mas)
		SchemaReader mr = new SchemaReaderJdbc(dbt);
		List<String> lst = mr.getTableList(true, false); // tablas
		String lstString = "," + String.join(",", lst).toLowerCase() + ",";
		assertContains(",stypes1,", lstString);
		assertDoesNotContain(",stypesvp,", lstString);

		lst = mr.getTableList(false, true); // vistas
		lstString = "," + String.join(",", lst).toLowerCase() + ",";
		assertContains(",stypesvp,", lstString);
		assertContains(",stypesvxxl,", lstString);
		assertDoesNotContain(",stypes1,", lstString);

		lst = mr.getTableList(true, true); // vistas
		lstString = "," + String.join(",", lst).toLowerCase() + ",";
		assertContains(",stypes1,", lstString);
		assertContains(",stypesvp,", lstString);
		assertContains(",stypesvxxl,", lstString);
	}

}
