package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import giis.tdrules.model.shared.ModelException;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;

public class TestTdAttributeExtensions extends Base {

	@Test
	public void testTdAttributeGetters() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdAttribute col11 = model.getEntities().get(0).getAttributes().get(0);
		assertEquals("col11", col11.getName());
		assertTrue(col11.isUid());
		assertTrue(col11.isNotnull());
		assertFalse(col11.isNullable());
		assertFalse(col11.isAutoincrement());
		assertFalse(col11.hasDefaultvalue());
		assertEquals("", col11.getDefaultvalue());
		assertFalse(col11.isRid());
		assertEquals("", col11.getRid());
		assertEquals("", col11.getRidEntity());
		assertEquals("", col11.getRidAttribute());

		TdAttribute col22 = model.getEntities().get(1).getAttributes().get(1);
		assertEquals("col22", col22.getName());
		assertFalse(col22.isUid());
		assertFalse(col22.isNotnull());
		assertTrue(col22.isNullable());
		assertTrue(col22.hasDefaultvalue());
		assertEquals("22", col22.getDefaultvalue());
		assertTrue(col22.isRid());
		assertEquals("clirdb1.col11", col22.getRid());
		assertEquals("clirdb1", col22.getRidEntity());
		assertEquals("col11", col22.getRidAttribute());
	}
	@Test
	public void testTdAttributeSetters() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdAttribute col22 = model.getEntities().get(1).getAttributes().get(1);
		assertTrue(col22.readonly(true).isReadonly());
		assertFalse(col22.readonly(false).isReadonly());
		assertTrue(col22.notnull(true).isNotnull());
		assertFalse(col22.notnull(false).isNotnull());
	}

	@Test
	public void testCompositeTypes() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdAttribute col22 = model.getEntities().get(1).getAttributes().get(1);
		assertFalse(col22.isType());
		assertFalse(col22.isArray());
		
		col22.setType();
		assertTrue(col22.isType());
		col22.setArray();
		assertTrue(col22.isArray());
	}

	@Test
	public void testMalformedRids() {
		TdSchema model = TestTdSchemaExtensions.getSchema();
		TdAttribute col22 = model.getEntities().get(1).getAttributes().get(1);
		col22.setRid(null);
		assertEquals("", col22.getRidEntity());
		assertEquals("", col22.getRidAttribute());
		
		col22.setRid("nodot");
		ModelException exception=assertThrows(ModelException.class, () -> {
			col22.getRidAttribute();
		});
		assertEquals("Referenced id nodot should have at least two components separated by a dot", exception.getMessage());
		exception=assertThrows(ModelException.class, () -> {
			col22.getRidEntity();
		});
		assertEquals("Referenced id nodot should have at least two components separated by a dot", exception.getMessage());
	}
}
