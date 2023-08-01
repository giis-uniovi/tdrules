package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;

public class TestTdEntityExtensions extends Base {

	@Test
	public void testDbTableGetKeys() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		assertEquals("col11", model.getEntity("clirdb1").getUid().getName());
		assertNull(model.getEntity("clirdbv").getUid());
	}

	@Test
	public void testDbTableGetFks() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdEntity table = model.getEntity("clirdb2");
		List<String> fks = table.getUniqueRids();
		assertEquals(1, fks.size());
		assertEquals("clirdb1.col11", fks.get(0));
		List<String> fknames = table.getUniqueRidNames();
		assertEquals(1, fknames.size());
		assertEquals("ref_clirdb1_col11", fknames.get(0));
		//obteniendo las columnas en vez de valores de fks
		List<TdAttribute> fkcols = table.getRids();
		assertEquals(1, fkcols.size());
		assertEquals("clirdb1.col11", fkcols.get(0).getRid());
	}

	@Test
	public void testDbTableGetFksRepeated() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdEntity table = model.getEntity("clirdb2");
		table.addAttributesItem(new TdAttribute().name("newcol").rid("xx.yy"))
				.addAttributesItem(new TdAttribute().name("repeated").rid("clirdb1.col11"));
		List<String> fks = table.getUniqueRids();
		assertEquals(2, fks.size());
		assertEquals("clirdb1.col11", fks.get(0));
		assertEquals("xx.yy", fks.get(1));
		//obteniendo las columnas en vez de valores de fks (sin eliminacion de duplicados)
		List<TdAttribute> fkcols = table.getRids();
		assertEquals(3, fkcols.size());
		assertEquals("clirdb1.col11", fkcols.get(0).getRid());
		assertEquals("xx.yy", fkcols.get(1).getRid());
		assertEquals("clirdb1.col11", fkcols.get(2).getRid());
	}


}
