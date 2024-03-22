package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import giis.tdrules.model.ModelException;
import giis.tdrules.model.transform.SchemaSorter;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Baseline tests with a diamond shape and additional tests to sort the entities derived 
 * from types and arrays that can appear in openapi schemas.
 * 
 * More ordering tests are made for the legacy rdb sorter in the tdrules-client-rdb module
 */
public class TestSchemaSorter extends Base {

	// simple diamond shape with a recursive relation
	private TdSchema getModelBase() {
		TdEntity bottom = new TdEntity().name("bottom")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").notnull("true").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("left.id"))
				.addAttributesItem(new TdAttribute().name("fk2").datatype("integer").rid("right.id"));
		TdEntity right = new TdEntity().name("right")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").notnull("true").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk").datatype("integer").rid("top.id"))
				.addAttributesItem(new TdAttribute().name("fk_recursive").datatype("integer").rid("right.id"));
		TdEntity left = new TdEntity().name("left")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").notnull("true").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk").datatype("integer").rid("top.id"));
		TdEntity top = new TdEntity().name("top")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").notnull("true").uid("true"));
		TdSchema model = new TdSchema().storetype("openapi").addEntitiesItem(bottom).addEntitiesItem(right)
				.addEntitiesItem(left).addEntitiesItem(top);
		return model;
	}
	private void addReference(TdSchema schema, String from, String to, String fieldName, String constraintName) {
		schema.getEntity(from).addAttributesItem(
				new TdAttribute().name(fieldName).ridname(constraintName).datatype("integer").rid(to));
	}
	
	@Test
	public void testSortBaseline() {
		SchemaSorter sorter = new SchemaSorter(getModelBase());
		// move bottom and top
		assertEquals("[top, left, right, bottom]", sorter.sort(Arrays.asList("bottom", "right", "left", "top")).toString());
		assertEquals("[top, left, right, bottom]", sorter.sort(Arrays.asList("bottom", "right", "top", "left")).toString());
		assertEquals("[top, left, right, bottom]", sorter.sort(Arrays.asList("bottom", "top", "right", "left")).toString());

		// left, right may have different orderings, depends on the source order
		assertEquals("[top, left, right, bottom]", sorter.sort(Arrays.asList("left", "bottom", "top", "right")).toString());
		assertEquals("[top, right, left, bottom]", sorter.sort(Arrays.asList("right", "bottom", "top", "left")).toString());
		assertEquals("[top, left, right, bottom]", sorter.sort(Arrays.asList("top", "left", "right", "bottom")).toString());
		assertEquals("[top, right, left, bottom]", sorter.sort(Arrays.asList("top", "right", "left", "bottom")).toString());
	}

	@Test
	public void testSortPartialSchema() {
		SchemaSorter sorter = new SchemaSorter(getModelBase());
		// only sort part of the entities
		assertEquals("[left, right, bottom]", sorter.sort(Arrays.asList("bottom", "right", "left")).toString());
		assertEquals("[top, right, left]", sorter.sort(Arrays.asList("right", "left", "top")).toString());
	}

	@Test
	public void testSortCycles() {
		TdSchema schema = getModelBase();
		addReference(schema, "top", "bottom.id", "fk_recurse", "constraint_name");

		// Detection of cycles
		ModelException exception = assertThrows(ModelException.class, () -> {
			new SchemaSorter(schema).sort(Arrays.asList("bottom", "right", "left", "top"));
		});
		assertEquals("Too many recusive levels when trying sort entities", exception.getMessage());

		// Do not throw if we break the cycle
		SchemaSorter sorter = new SchemaSorter(schema).noFollowConstraint("constraint_name");
		assertEquals("[top, left, right, bottom]",
				sorter.sort(Arrays.asList("bottom", "right", "left", "top")).toString());
	}

	@Test
	public void testFindDependentEntities() {
		SchemaSorter sorter = new SchemaSorter(getModelBase());
		assertEquals("[bottom, left, right, top]", sorter.getWithDependent(new ArrayList<String>(Arrays.asList("bottom"))).toString());
		assertEquals("[left, top]", sorter.getWithDependent(new ArrayList<String>(Arrays.asList("left"))).toString());
		assertEquals("[right, top]", sorter.getWithDependent(new ArrayList<String>(Arrays.asList("right"))).toString());
		assertEquals("[top]", sorter.getWithDependent(new ArrayList<String>(Arrays.asList("top"))).toString());

		assertEquals("[bottom, left, right, top]", sorter.getWithDependent(new ArrayList<String>(Arrays.asList("bottom", "left"))).toString());
		assertEquals("[left, right, top]", sorter.getWithDependent(new ArrayList<String>(Arrays.asList("left", "right"))).toString());
	}
	
	private void addType(TdSchema schema, String entity) {
		// Adds the entity that represents the type and the attribute that links to this entity
		TdEntity type = new TdEntity().name(entity + "_ttyp1_xt").entitytype("type")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string"));
		schema.getEntity(entity).addAttributesItem(
				new TdAttribute().name("ttyp1").datatype(entity + "_ttyp1_xt").compositetype("type"));
		schema.addEntitiesItem(type);
	}
	private void addArray(TdSchema schema, String entity) {
		// Adds the entity that represents the type and the attribute that links to this entity
		// Also, array references another entity (aref)
		TdEntity arr = new TdEntity().name(entity + "_tarr1_xa").entitytype("array")
				.addAttributesItem(new TdAttribute().name("pk_xa").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk_xa").datatype("integer").rid(entity + ".id"))
				.addAttributesItem(new TdAttribute().name("fk_aref").datatype("integer").rid("aref.id"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string"));
		TdEntity aref = new TdEntity().name("aref")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").uid("true"));
		schema.getEntity(entity).addAttributesItem(
				new TdAttribute().name("tarr1").datatype(entity + "_tarr1_xa").compositetype("array"));
		schema.addEntitiesItem(arr);
		schema.addEntitiesItem(aref);
	}
	
	@Test
	public void testSortTypeEntities() {
		TdSchema schema = getModelBase();
		addType(schema, "top");
		SchemaSorter sorter = new SchemaSorter(schema);
		// types in an entity are master even there is not rid from the entity
		assertEquals("[top_ttyp1_xt, top]", sorter.sort(Arrays.asList("top_ttyp1_xt", "top")).toString());
		assertEquals("[top_ttyp1_xt, top]", sorter.sort(Arrays.asList("top", "top_ttyp1_xt")).toString());
	}

	@Test
	public void testSortArrayEntities() {
		TdSchema schema = getModelBase();
		addArray(schema, "top");
		SchemaSorter sorter = new SchemaSorter(schema);
		// Arrays in an entity are detail because of the rid to the entity
		assertEquals("[top, top_tarr1_xa]", sorter.sort(Arrays.asList("top_tarr1_xa", "top")).toString());
		assertEquals("[top, top_tarr1_xa]", sorter.sort(Arrays.asList("top", "top_tarr1_xa")).toString());
		
		// Adding the entity referenced by the array at different positions
		assertEquals("[top, aref, top_tarr1_xa]", sorter.sort(Arrays.asList("top_tarr1_xa", "top", "aref")).toString());
		assertEquals("[top, aref, top_tarr1_xa]", sorter.sort(Arrays.asList("top_tarr1_xa", "aref", "top")).toString());
		// aref not near array but still follows the reference order 
		assertEquals("[aref, top, top_tarr1_xa]", sorter.sort(Arrays.asList( "aref", "top_tarr1_xa", "top")).toString());
	}

	@Test
	public void testSortArrayEntitiesWithHoisting() {
		TdSchema schema = getModelBase();
		addArray(schema, "top");
		SchemaSorter sorter = new SchemaSorter(schema).setArrayHoist();
		// Arrays in an entity are detail because of the rid to the entity
		assertEquals("[top_tarr1_xa, top]", sorter.sort(Arrays.asList("top_tarr1_xa", "top")).toString());
		assertEquals("[top_tarr1_xa, top]", sorter.sort(Arrays.asList("top", "top_tarr1_xa")).toString());
		
		// Adding the entity referenced by the array at different positions
		assertEquals("[aref, top_tarr1_xa, top]", sorter.sort(Arrays.asList("top_tarr1_xa", "top", "aref")).toString());
		assertEquals("[aref, top_tarr1_xa, top]", sorter.sort(Arrays.asList("top_tarr1_xa", "aref", "top")).toString());
		// aref not near array but still follows the reference order 
		assertEquals("[aref, top_tarr1_xa, top]", sorter.sort(Arrays.asList( "aref", "top_tarr1_xa", "top")).toString());
	}

	@Test
	public void testSortAllWithTypesAndArraysAtBottom() {
		TdSchema schema = getModelBase();
		addArray(schema, "bottom");
		addType(schema, "bottom");
		SchemaSorter sorter = new SchemaSorter(schema);
		assertEquals("[top, left, right, bottom_ttyp1_xt, bottom, bottom_tarr1_xa]",
				sorter.sort(Arrays.asList("bottom", "right", "left", "top", "bottom_tarr1_xa", "bottom_ttyp1_xt")).toString());
		assertEquals("[top, left, right, bottom_ttyp1_xt, bottom, bottom_tarr1_xa]",
				sorter.sort(Arrays.asList("bottom", "right", "left", "top", "bottom_ttyp1_xt", "bottom_tarr1_xa")).toString());
		assertEquals("[top, left, right, bottom_ttyp1_xt, bottom, bottom_tarr1_xa]",
				sorter.sort(Arrays.asList("bottom_tarr1_xa", "bottom_ttyp1_xt", "bottom", "right", "left", "top")).toString());
		// This moves the type to the top, but still follows the reference order
		assertEquals("[bottom_ttyp1_xt, top, left, right, bottom, bottom_tarr1_xa]",
				sorter.sort(Arrays.asList("bottom_ttyp1_xt", "bottom_tarr1_xa", "bottom", "right", "left", "top")).toString());
		
		// Now including the entity referenced by the array
		assertEquals("[top, left, right, bottom_ttyp1_xt, bottom, aref, bottom_tarr1_xa]",
				sorter.sort(Arrays.asList("bottom", "right", "left", "top", "bottom_tarr1_xa", "bottom_ttyp1_xt", "aref")).toString());
	}

	@Test
	public void testSortAllWithTypesAndArraysAtTop() {
		TdSchema schema = getModelBase();
		addArray(schema, "top");
		addType(schema, "top");
		SchemaSorter sorter = new SchemaSorter(schema);
		// Everything disordered, array goes far from top, but still follows the order
		assertEquals("[top_ttyp1_xt, top, left, right, bottom, aref, top_tarr1_xa]",
				sorter.sort(Arrays.asList("bottom", "right", "left", "top_tarr1_xa", "top", "top_ttyp1_xt", "aref")).toString());
	}
	
	@Test
	public void testSortAllWithTypesAndArraysAtTopWithHoist() {
		TdSchema schema = getModelBase();
		addArray(schema, "top");
		addType(schema, "top");
		SchemaSorter sorter = new SchemaSorter(schema).setArrayHoist();
		assertEquals("[aref, top_tarr1_xa, top_ttyp1_xt, top, left, right, bottom]",
				sorter.sort(Arrays.asList("bottom", "right", "left", "top_tarr1_xa", "top", "top_ttyp1_xt", "aref")).toString());
	}
	
}
