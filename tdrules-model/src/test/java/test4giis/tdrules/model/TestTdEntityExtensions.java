package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;

public class TestTdEntityExtensions extends Base {

	@Test
	public void testTdEntityGetKeys() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		assertEquals("col11", model.getEntity("clirdb1").getUid().getName());
		assertNull(model.getEntity("clirdbv").getUid());
	}

	@Test
	public void testTdEntityGetRids() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdEntity entity = model.getEntity("clirdb2");
		List<String> rids = entity.getUniqueRids();
		assertEquals(1, rids.size());
		assertEquals("clirdb1.col11", rids.get(0));
		List<String> names = entity.getUniqueRidNames();
		assertEquals(1, names.size());
		assertEquals("ref_clirdb1_col11", names.get(0));
		// getting whole attributes instead the string values of rids
		List<TdAttribute> ridAttrs = entity.getRids();
		assertEquals(1, ridAttrs.size());
		assertEquals("clirdb1.col11", ridAttrs.get(0).getRid());
	}

	@Test
	public void testTdEntityGetRidsRepeated() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdEntity entity = model.getEntity("clirdb2");
		entity.addAttributesItem(new TdAttribute().name("newcol").rid("xx.yy"))
				.addAttributesItem(new TdAttribute().name("repeated").rid("clirdb1.col11"));
		List<String> rids = entity.getUniqueRids();
		assertEquals(2, rids.size());
		assertEquals("clirdb1.col11", rids.get(0));
		assertEquals("xx.yy", rids.get(1));
		// getting whole attributes instead the string values of rids (without duplicate removal)
		List<TdAttribute> ridAttrs = entity.getRids();
		assertEquals(3, ridAttrs.size());
		assertEquals("clirdb1.col11", ridAttrs.get(0).getRid());
		assertEquals("xx.yy", ridAttrs.get(1).getRid());
		assertEquals("clirdb1.col11", ridAttrs.get(2).getRid());
	}

	@Test
	public void testFindAttribute() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		assertEquals("col11", model.getEntity("clirdb1").getAttribute("Col11").getName());
		assertNull(model.getEntity("clirdb1").getAttribute("col99"));
	}

	@Test
	public void testFindDdl() {
		TdEntity entity = TestTdSchemaExtensions.getSchema().getEntity("clirdb2")
			.addDdlsItem(new Ddl().command("put").query("/path1"))
			.addDdlsItem(new Ddl().command("post").query("/path2"))
			.addDdlsItem(new Ddl().command("post").query("/path3"));
		assertEquals("/path1", entity.getDdl("put").getQuery());
		assertEquals("/path2", entity.getDdl("post").getQuery()); // should get the first found
		assertNull(entity.getDdl("get"));
		// ignore case
		assertEquals("/path1", entity.getDdl("PUT").getQuery());
		assertEquals("/path2", entity.getDdl("POST").getQuery());
	}

	@Test
	public void testEntityTypes() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdEntity entity = model.getEntity("clirdb1");
		assertTrue(entity.isObject());
		assertFalse(entity.isType());
		assertFalse(entity.isArray());
		
		entity.setType();
		assertTrue(entity.isType());
		entity.setArray();
		assertTrue(entity.isArray());
		entity.setObject();
		assertTrue(entity.isObject());
	}

}
