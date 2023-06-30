package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import giis.tdrules.model.ModelException;
import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;

public class TestDbColumnExtensions extends Base {

	@Test
	public void testDbColumnGetters() {
		DbSchema model = TestDbSchemaExtensions.getSchema();
		DbColumn col11 = model.getTables().get(0).getColumns().get(0);
		assertEquals("col11", col11.getName());
		assertTrue(col11.isPk());
		assertTrue(col11.isNotnull());
		assertFalse(col11.isNullable());
		assertFalse(col11.isAutoincrement());
		assertFalse(col11.hasDefaultvalue());
		assertEquals("", col11.getDefaultvalue());
		assertFalse(col11.isFk());
		assertEquals("", col11.getFk());
		assertEquals("", col11.getFkTable());
		assertEquals("", col11.getFkColumn());

		DbColumn col22 = model.getTables().get(1).getColumns().get(1);
		assertEquals("col22", col22.getName());
		assertFalse(col22.isPk());
		assertFalse(col22.isNotnull());
		assertTrue(col22.isNullable());
		assertTrue(col22.hasDefaultvalue());
		assertEquals("22", col22.getDefaultvalue());
		assertTrue(col22.isFk());
		assertEquals("clirdb1.col11", col22.getFk());
		assertEquals("clirdb1", col22.getFkTable());
		assertEquals("col11", col22.getFkColumn());
	}

	@Test
	public void testMalformedFks() {
		DbSchema model = TestDbSchemaExtensions.getSchema();
		DbColumn col22 = model.getTables().get(1).getColumns().get(1);
		col22.setFk(null);
		assertEquals("", col22.getFkTable());
		assertEquals("", col22.getFkColumn());
		
		col22.setFk("nodot");
		ModelException exception=assertThrows(ModelException.class, () -> {
			col22.getFkColumn();
		});
		assertEquals("Foreign key nodot should have at least two components separated by a dot", exception.getMessage());
		exception=assertThrows(ModelException.class, () -> {
			col22.getFkTable();
		});
		assertEquals("Foreign key nodot should have at least two components separated by a dot", exception.getMessage());
	}
}
