package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import giis.tdrules.model.ModelException;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.Ddl;

public class TestTdSchemaExtensions extends Base {

	//schema used by all extension tests
	public static TdSchema getSchema() {
		TdEntity tab1 = new TdEntity().name("clirdb1").entitytype("table")
				.addAttributesItem(new TdAttribute().name("col11").datatype("int").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("col12").datatype("varchar").size("16"))
				.addDdlsItem(new Ddl().command("create").query("create table clirdb1 (col11 int not null primary key, col12 varchar(16)"));
		TdEntity tab2 = new TdEntity().name("clirdb2").entitytype("table")
				.addAttributesItem(new TdAttribute().name("col21").datatype("decimal").size("8,4").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("col22").datatype("int").rid("clirdb1.col11").ridname("ref_clirdb1_col11").defaultvalue("22"))
				.addChecksItem(new TdCheck().attribute("col22").constraint("([col22]>(0))"));
		TdEntity view = new TdEntity().name("clirdbv").entitytype("view")
				.addAttributesItem(new TdAttribute().name("col01").datatype("int").notnull("true"));
		return new TdSchema().storetype("sqlserver").addEntitiesItem(tab1).addEntitiesItem(tab2).addEntitiesItem(view);
	}
	@Test
	public void testTdSchemaAddEntities() {
		TdSchema model = getSchema();
		// Add all entities
		TdSchema model0 = new TdSchema().entities(model.getEntities());
		model0.storetype("sqlserver");
		assertEquals(3, model.getEntities().size());
		va.assertEquals(model.toString(), model0.toString());

		// Add individual entities
		model0 = new TdSchema();
		model0.storetype("sqlserver");
		model0.addEntitiesItem(model.getEntities().get(0));
		model0.addEntitiesItemIfNotExist(model.getEntities().get(1));
		model0.addEntitiesItem(model.getEntities().get(2));
		model0.addEntitiesItemIfNotExist(model.getEntities().get(0));
		assertEquals(3, model0.getEntities().size());
		va.assertEquals(model.toString(), model0.toString());
	}

	@Test
	public void testTdSchemaFindEntities() {
		TdSchema model = getSchema();
		assertEquals("clirdb1", model.getEntity("clirdb1").getName());
		assertEquals("clirdb2", model.getEntity("clirdb2").getName());
		assertEquals("clirdbv", model.getEntity("clirdbv").getName());
		// case insensitive and trim
		assertEquals("clirdb1", model.getEntity("CliRDB1").getName());
		assertEquals("clirdb1", model.getEntity(" clirdb1 ").getName());

		try {
			model.getEntity("doesnotexist");
			fail("Should fail");
		} catch (ModelException e) {
			assertEquals("Can't find any entity in the schema with name doesnotexist", e.getMessage());
		}
	}

	@Test
	public void testTdSchemaGetEntityNames() {
		TdSchema model = getSchema();
		model.addEntitiesItem(new TdEntity().name("clitype").entitytype("type"));
		model.addEntitiesItem(new TdEntity().name("cliarray").entitytype("array"));
		//using different scopes (arrays are considered as tables)
		assertEquals("[clirdb1, clirdb2, clirdbv, clitype, cliarray]", model.getEntityNames().toString());
		assertEquals("[clirdb1, clirdb2, cliarray]", model.getEntityNames(true, false, false).toString());
		assertEquals("[clirdbv]", model.getEntityNames(false, true, false).toString());
		assertEquals("[clitype]", model.getEntityNames(false, false, true).toString());
	}

}
