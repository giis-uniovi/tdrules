package test4giis.tdrules.client.oa;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.client.oa.OaSchemaFilter;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Use of filters to remove objects from the OA model. The matching of filters
 * has been tested in SchemaFilter (tdrules-model), here the focus is on
 * removing entities, attributes of different types and the handling of paths
 * that correspond to filtered entities
 */
public class TestSchemaFilter extends Base {

	/**
	 * Not applying any filter, to use as baseline
	 */
	@Test
	public void testSchemaFilterBaseline() throws IOException {
		TdSchema schema = getDbApi("oa-filter.yml").getSchema();
		assertModel("schema-filter-baseline.txt", schema);
	}

	// -primitive attribute
	// -entire object
	// -empty object not removed
	// -object with path removed/no removed
	@Test
	public void testSchemaFilterEntityAndAttribute() throws IOException {

		OaSchemaFilter filter = new OaSchemaFilter().add("nokey*", "*").add("*", "string*");
		TdSchema schema = getDbApi("oa-filter.yml").setFilter(filter).getSchema();
		assertModel("schema-filter-entity-attribute.txt", schema);
	}

	// -array/object/referenced object attribute
	// (note that the referenced object is not removed, could be used by another object)
	// -empty object removed
	@Test
	public void testSchemaFilterComposites() throws IOException {
		OaSchemaFilter filter = new OaSchemaFilter().add("empty*", "*").add("*", "arr*").add("*", "obj*");
		TdSchema schema = getDbApi("oa-filter.yml").setFilter(filter).getSchema();
		assertModel("schema-filter-composites.txt", schema);
	}
		
}
