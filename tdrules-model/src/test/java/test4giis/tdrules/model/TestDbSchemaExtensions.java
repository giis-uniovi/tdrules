package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import giis.tdrules.model.ModelException;
import giis.tdrules.openapi.model.DbCheck;
import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;
import giis.tdrules.openapi.model.Ddl;

public class TestDbSchemaExtensions extends Base {

	//schema used by all extension tests
	public static DbSchema getSchema() {
		DbTable tab1 = new DbTable().name("clirdb1").tabletype("table")
				.addColumnsItem(new DbColumn().name("col11").datatype("int").key("true").notnull("true"))
				.addColumnsItem(new DbColumn().name("col12").datatype("varchar").size("16"))
				.addDdlsItem(new Ddl().command("create").sql("create table clirdb1 (col11 int not null primary key, col12 varchar(16)"));
		DbTable tab2 = new DbTable().name("clirdb2").tabletype("table")
				.addColumnsItem(new DbColumn().name("col21").datatype("decimal").size("8,4").key("true").notnull("true"))
				.addColumnsItem(new DbColumn().name("col22").datatype("int").fk("clirdb1.col11").fkname("ref_clirdb1_col11").defaultvalue("22"))
				.addChecksItem(new DbCheck().column("col22").constraint("([col22]>(0))"));
		DbTable view = new DbTable().name("clirdbv").tabletype("view")
				.addColumnsItem(new DbColumn().name("col01").datatype("int").notnull("true"));
		DbSchema schema=new DbSchema().dbms("sqlserver").addTablesItem(tab1).addTablesItem(tab2).addTablesItem(view);
		return schema;
	}
	@Test
	public void testDbSchemaAddTables() {
		DbSchema model = getSchema();
		// Add all tables
		DbSchema model0 = new DbSchema().tables(model.getTables());
		model0.dbms("sqlserver");
		assertEquals(3, model.getTables().size());
		va.assertEquals(model.toString(), model0.toString());

		// Add individual tables
		model0 = new DbSchema();
		model0.dbms("sqlserver");
		model0.addTablesItem(model.getTables().get(0));
		model0.addTablesItemIfNotExist(model.getTables().get(1));
		model0.addTablesItem(model.getTables().get(2));
		model0.addTablesItemIfNotExist(model.getTables().get(0));
		assertEquals(3, model0.getTables().size());
		va.assertEquals(model.toString(), model0.toString());
	}

	@Test
	public void testDbSchemaFindTables() {
		DbSchema model = getSchema();
		assertEquals("clirdb1", model.getTable("clirdb1").getName());
		assertEquals("clirdb2", model.getTable("clirdb2").getName());
		assertEquals("clirdbv", model.getTable("clirdbv").getName());
		// case insensitive and trim
		assertEquals("clirdb1", model.getTable("CliRDB1").getName());
		assertEquals("clirdb1", model.getTable(" clirdb1 ").getName());

		try {
			model.getTable("doesnotexist");
			fail("Should fail");
		} catch (ModelException e) {
			assertEquals("Can't find any table in the schema with name doesnotexist", e.getMessage());
		}
	}

	@Test
	public void testDbSchemaGetTableNames() {
		DbSchema model = getSchema();
		model.addTablesItem(new DbTable().name("clitype").tabletype("type"));
		model.addTablesItem(new DbTable().name("cliarray").tabletype("array"));
		//using different scopes (arrays are considered as tables)
		assertEquals("[clirdb1, clirdb2, clirdbv, clitype, cliarray]", model.getTableNames().toString());
		assertEquals("[clirdb1, clirdb2, cliarray]", model.getTableNames(true, false, false).toString());
		assertEquals("[clirdbv]", model.getTableNames(false, true, false).toString());
		assertEquals("[clitype]", model.getTableNames(false, false, true).toString());
	}

}
