package test4giis.tdrules.client.oa;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.client.oa.MermaidWriter;
import giis.tdrules.client.oa.OaSchemaIdResolver;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Covers all transformations from Open API models to TdSchema.
 * All inputs and expected outputs are stored in external yml files (src/test/resources),
 * each indicates the situations covered.
 * Actual outputs are sent to target folder
 */
public class TestSchemaConvert extends Base {

	@Test
	public void testSchemaBasicTypesAndNulEnum() throws IOException {
		TdSchema schema = getDbApi("oa-basic.yml").getSchema();
		assertModel("schema-basic.txt", schema);
	}

	@Test
	public void testSchemaBasicTypesWithConstraints() throws IOException {
		TdSchema schema = getDbApi("oa-constraints.yml").getSchema();
		assertModel("schema-constraints.txt", schema);
	}

	@Test
	public void testSchemaObjectTypes() throws IOException {
		TdSchema schema = getDbApi("oa-object.yml").getSchema();
		assertModel("schema-object.txt", schema);
	}

	@Test
	public void testSchemaObjectTypesMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-object.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("schema-object.md", mermaid);
	}

	@Test
	public void testSchemaObjectNullableTypes() throws IOException {
		TdSchema schema = getDbApi("oa-object-nullable.yml").getSchema();
		assertModel("schema-object-nullable.txt", schema);
	}

	@Test
	public void testSchemaArrayTypes() throws IOException {
		TdSchema schema = getDbApi("oa-array.yml").getSchema();
		assertModel("schema-array.txt", schema);
	}

	@Test
	public void testSchemaArrayTypesMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-array.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("schema-array.md", mermaid);
	}

	@Test
	public void testSchemaArrayWithFks() throws IOException {
		TdSchema schema = getDbApi("oa-array-with-fk.yml").getSchema();
		assertModel("schema-array-with-fk.txt", schema);
	}

	@Test
	public void testSchemaArrayWithFksMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-array-with-fk.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("schema-array-with-fk.md", mermaid);
	}

	@Test
	public void testSchemaNestedCompositeObject() throws IOException {
		TdSchema schema = getDbApi("oa-nested-obj.yml").getSchema();
		assertModel("schema-nested-obj.txt", schema);
	}

	@Test
	public void testSchemaNestedCompositeObjectMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-nested-obj.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("schema-nested-obj.md", mermaid);
	}

	@Test
	public void testSchemaNestedCompositeArray() throws IOException {
		TdSchema schema = getDbApi("oa-nested-arr.yml").getSchema();
		assertModel("schema-nested-arr.txt", schema);
	}

	@Test
	public void testSchemaNestedCompositeArrayMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-nested-arr.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("schema-nested-arr.md", mermaid);
	}
	
	// Special chars must pass without changes to the model, except references between entities
	@Test
	public void testSchemaUnreservedChars() throws IOException {
		TdSchema schema = getDbApi("oa-chars.yml").getSchema();
		assertModel("schema-chars.txt", schema);
	}

	@Test
	public void testSchemaUnreservedCharsXml() throws IOException {
		TdSchema schema = getDbApi("oa-chars.yml").getSchema();
		assertModelXml("schema-chars", schema);
	}

	// The above use the default ids (require vendor extensions).
	// Here check the location of ids by conventions using the id resolver
	@Test
	public void testSchemaIdResolver() throws IOException {
		OaSchemaApi api = getDbApi("oa-ids.yml").setIdResolver(new OaSchemaIdResolver().setIdName("uid"));
		TdSchema schema = api.getSchema();
		assertModel("schema-ids.txt", schema);
	}

	@Test
	public void testSchemaIdResolverWithExclusions() throws IOException {
		OaSchemaApi api = getDbApi("oa-ids-exclusions.yml")
				.setIdResolver(new OaSchemaIdResolver().setIdName("uid").excludeEntity("With"));
		TdSchema schema = api.getSchema();
		assertModel("schema-ids-exclusions.txt", schema);
	}

}
