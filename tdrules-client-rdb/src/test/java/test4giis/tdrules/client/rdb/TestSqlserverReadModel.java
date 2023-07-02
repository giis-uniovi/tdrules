package test4giis.tdrules.client.rdb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.tdrules.client.rdb.DbSchemaApi;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

/**
 * Basic tests of reading a model with usual elements
 * (different datatypes, relations, checks), using Sqlserver.
 * Detailed tests are in module tdrules-store-rdb
 */
public class TestSqlserverReadModel extends Base {

	protected Properties config;

	@Before
	public void setUp() throws SQLException {
		super.setUp();
		//una tabla que no sera leida y dos tablas enlazadas con fk, checks, defaults y varios tipos de datos
		//el check no lo pone directamente con check... sino con constraint para darle un nombre y luego poder comparar
		Connection dbt = getConnection("sqlserver", TEST_DBNAME);
		executeNotThrow(dbt, "drop view clirdbv");
		executeNotThrow(dbt, "drop table clirdb2");
		executeNotThrow(dbt, "drop table clirdb1");
		executeNotThrow(dbt, "drop table clirdb0");
		execute(dbt, "create table clirdb0 (col01 int not null primary key)");
		execute(dbt, "create table clirdb1 (col11 int not null primary key, col12 varchar(16))");
		execute(dbt, "create table clirdb2 (col21 decimal(8,4), col22 int not null default (22), CONSTRAINT chk_clirdb_col22 CHECK (col22>0), "+
				"PRIMARY KEY (col21,col22), CONSTRAINT ref_clirdb1_col11 FOREIGN KEY(col22) REFERENCES clirdb1(col11) )");
		execute(dbt, "create view clirdbv as select col01 from clirdb0");
		dbt.close();
	}
	
	// Each choice of
	// -Create from connection/schema reader
	// -Get objects by table list selection/table or view selection
	// -Different parameters in table or view selection
	
	@Test
	public void testGetModelUsingTableList() throws SQLException {
		DbSchemaApi api = new DbSchemaApi(getConnection("sqlserver", TEST_DBNAME));
		List<String> tables = new ArrayList<String>();
		tables.add("clirdb1");
		tables.add("clirdb2");
		DbSchema model = api.getDbSchema(tables);
		VisualAssert va = new VisualAssert().setFramework(Framework.JUNIT4);
		String expectedFileName = Parameters.isJava()
				? "src/test/resources/model-bmk.txt"
				: FileUtil.getPath(Parameters.getProjectRoot(), "resources", "model-bmk.txt");
		String actual = api.modelToString(model);
		va.assertEquals(FileUtil.fileRead(expectedFileName).replace("\r", ""), actual.replace("\r", ""));
	}

	@Test
	public void testGetModelSelectTablesOrViews() throws SQLException {
		// different form to create the api
		SchemaReader sr = new SchemaReaderJdbc(getConnection("sqlserver", TEST_DBNAME));
		DbSchemaApi api = new DbSchemaApi(sr);
		// starting with clirdb to do not show tables from other tests
		DbSchema model = api.getDbSchema(true, true, true, "clirdb");
		VisualAssert va = new VisualAssert().setFramework(Framework.JUNIT4);
		String expectedFileName = Parameters.isJava()
				? "src/test/resources/model-all-bmk.txt"
				: FileUtil.getPath(Parameters.getProjectRoot(), "resources", "model-all-bmk.txt");
		String actual = api.modelToString(model);
		va.assertEquals(FileUtil.fileRead(expectedFileName).replace("\r", ""), actual.replace("\r", ""));

		// repeat, using connection, but more selective, only views
		api = new DbSchemaApi(getConnection("sqlserver", TEST_DBNAME));
		model = api.getDbSchema(false, true, true, "clirdb");
		expectedFileName = Parameters.isJava()
				? "src/test/resources/model-view-bmk.txt"
				: FileUtil.getPath(Parameters.getProjectRoot(), "resources", "model-view-bmk.txt");
		actual = api.modelToString(model);
		va.assertEquals(FileUtil.fileRead(expectedFileName).replace("\r", ""), actual.replace("\r", ""));
	}

	@Test
	public void testGetModelDefault() throws SQLException {
		Connection conn = getConnection("sqlserver", TEST_DBNAME);
		DbSchemaApi api = new DbSchemaApi(conn);
		DbSchema schema = api.getDbSchema();
		// now check only that the tables and views are in the model
		assertEquals("clirdb0", schema.getTable("clirdb0").getName());
		assertEquals("clirdb1", schema.getTable("clirdb1").getName());
		assertEquals("clirdb2", schema.getTable("clirdb2").getName());
		assertEquals("clirdbv", schema.getTable("clirdbv").getName());
		assertEquals("table", schema.getTable("clirdb0").getTabletype());
		assertEquals("view", schema.getTable("clirdbv").getTabletype());
	}

}
