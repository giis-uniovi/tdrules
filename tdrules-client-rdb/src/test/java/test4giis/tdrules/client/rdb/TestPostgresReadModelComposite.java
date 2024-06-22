package test4giis.tdrules.client.rdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.tdrules.client.rdb.DbSchemaApi;
import giis.tdrules.openapi.model.TdSchema;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

/**
 * Basic test for reading a model with composite types (arrays, types), using postgres.
 * Detailed tests are in module tdrules-store-rdb
 */
public class TestPostgresReadModelComposite extends Base{
	Connection dbt;

	@Before
	public void setUp() throws SQLException {
		super.setUp();
		//tablas similares a las usadas en las correspondietes pruebas del esquema
		dbt = getConnection("postgres", TEST_DBNAME);
		executeNotThrow(dbt, "drop table clirdb0");
		executeNotThrow(dbt, "drop table clirdb1");
		executeNotThrow(dbt, "drop type clirdbtype cascade");
		execute(dbt, "create type clirdbtype as (key decimal(10,2), value varchar(16))");
		execute(dbt, "create table clirdb0 (id int)");
		execute(dbt, "create table clirdb1 (arrcol decimal(11,3)[] , udt clirdbtype)");
	}
	@After
	public void TearDown() throws SQLException {
		dbt.close();
	}

	@Test
	public void testGetModelComposite() throws SQLException {
		DbSchemaApi api=new DbSchemaApi(dbt);
		List<String> tables = new ArrayList<String>();
		tables.add("clirdb1"); //solo una tabla, pero el modelo debe contener otra con el udt (clitype)
		TdSchema model=api.getSchema(tables);
		VisualAssert va=new VisualAssert().setFramework(Framework.JUNIT4);
		String expectedFileName=Parameters.isJava() 
				? "src/test/resources/model-composite-bmk.txt" 
				: FileUtil.getPath(Parameters.getProjectRoot(), "resources", "model-composite.txt"); 
		String actual=api.modelToString(model);
		va.assertEquals(FileUtil.fileRead(expectedFileName).replace("\r", ""), actual.replace("\r", ""));
	}

}
