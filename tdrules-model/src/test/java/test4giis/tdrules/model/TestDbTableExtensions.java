package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;

public class TestDbTableExtensions extends Base {

	@Test
	public void testDbTableGetKeys() {
		DbSchema model = TestDbSchemaExtensions.getSchema();
		assertEquals("col11", model.getTable("clirdb1").getPk().getName());
		assertNull(model.getTable("clirdbv").getPk());
	}

	@Test
	public void testDbTableGetFks() {
		DbSchema model = TestDbSchemaExtensions.getSchema();
		DbTable table = model.getTable("clirdb2");
		List<String> fks = table.getUniqueFks();
		assertEquals(1, fks.size());
		assertEquals("clirdb1.col11", fks.get(0));
		List<String> fknames = table.getUniqueFkNames();
		assertEquals(1, fknames.size());
		assertEquals("ref_clirdb1_col11", fknames.get(0));
		//obteniendo las columnas en vez de valores de fks
		List<DbColumn> fkcols = table.getFks();
		assertEquals(1, fkcols.size());
		assertEquals("clirdb1.col11", fkcols.get(0).getFk());
	}

	@Test
	public void testDbTableGetFksRepeated() {
		DbSchema model = TestDbSchemaExtensions.getSchema();
		DbTable table = model.getTable("clirdb2");
		table.addColumnsItem(new DbColumn().name("newcol").fk("xx.yy"))
				.addColumnsItem(new DbColumn().name("repeated").fk("clirdb1.col11"));
		List<String> fks = table.getUniqueFks();
		assertEquals(2, fks.size());
		assertEquals("clirdb1.col11", fks.get(0));
		assertEquals("xx.yy", fks.get(1));
		//obteniendo las columnas en vez de valores de fks (sin eliminacion de duplicados)
		List<DbColumn> fkcols = table.getFks();
		assertEquals(3, fkcols.size());
		assertEquals("clirdb1.col11", fkcols.get(0).getFk());
		assertEquals("xx.yy", fkcols.get(1).getFk());
		assertEquals("clirdb1.col11", fkcols.get(2).getFk());
	}


}
