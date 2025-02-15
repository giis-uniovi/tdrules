package test4giis.tdrules.client.oa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.client.oa.MermaidWriter;
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
		assertModel("oa-basic.txt", schema);
	}

	@Test
	public void testSchemaBasicTypesWithConstraints() throws IOException {
		TdSchema schema = getDbApi("oa-constraints.yml").getSchema();
		assertModel("oa-constraints.txt", schema);
	}

	@Test
	public void testSchemaObjectTypes() throws IOException {
		TdSchema schema = getDbApi("oa-object.yml").getSchema();
		assertModel("oa-object.txt", schema);
	}

	@Test
	public void testSchemaObjectTypesMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-object.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("oa-object.md", mermaid);
	}

	@Test
	public void testSchemaObjectNullableTypes() throws IOException {
		TdSchema schema = getDbApi("oa-object-nullable.yml").getSchema();
		assertModel("oa-object-nullable.txt", schema);
	}

	@Test
	public void testSchemaArrayTypes() throws IOException {
		TdSchema schema = getDbApi("oa-array.yml").getSchema();
		assertModel("oa-array.txt", schema);
	}

	@Test
	public void testSchemaArrayTypesMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-array.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("oa-array.md", mermaid);
	}

	@Test
	public void testSchemaArrayWithFks() throws IOException {
		TdSchema schema = getDbApi("oa-array-with-fk.yml").getSchema();
		assertModel("oa-array-with-fk.txt", schema);
	}

	@Test
	public void testSchemaArrayWithFksMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-array-with-fk.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("oa-array-with-fk.md", mermaid);
	}

	@Test
	public void testSchemaNestedCompositeObject() throws IOException {
		TdSchema schema = getDbApi("oa-nested-obj.yml").getSchema();
		assertModel("oa-nested-obj.txt", schema);
	}

	@Test
	public void testSchemaNestedCompositeObjectMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-nested-obj.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("oa-nested-obj.md", mermaid);
	}

	@Test
	public void testSchemaNestedCompositeArray() throws IOException {
		TdSchema schema = getDbApi("oa-nested-arr.yml").getSchema();
		assertModel("oa-nested-arr.txt", schema);
	}

	@Test
	public void testSchemaNestedCompositeArrayMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-nested-arr.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("oa-nested-arr.md", mermaid);
	}
	
	@Test
	public void testSchemaNestedCompositeRefs() throws IOException {
		TdSchema schema = getDbApi("oa-nested-refs.yml").getSchema();
		assertModel("oa-nested-refs.txt", schema);
	}

	@Test
	public void testSchemaNestedCompositeRefsMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-nested-refs.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("oa-nested-refs.md", mermaid);
	}
	
	// Special data types
	
	@Test
	public void testSchemaSpecialFreeFormObject() throws IOException {
		TdSchema schema = getDbApi("oa-special-free-form-object.yml").getSchema();
		System.out.println(new MermaidWriter(schema).getMermaid());
		assertModel("oa-special-free-form-object.txt", schema);
	}

	@Test
	public void testSchemaSpecialAdditionalProperties() throws IOException {
		TdSchema schema = getDbApi("oa-special-additional-properties.yml").getSchema();
		System.out.println(new MermaidWriter(schema).getMermaid());
		assertModel("oa-special-additional-properties.txt", schema);
	}

	// Special chars must pass without changes to the model, except references between entities
	@Test
	public void testSchemaUnreservedChars() throws IOException {
		TdSchema schema = getDbApi("oa-chars.yml").getSchema();
		assertModel("oa-chars.txt", schema);
	}

	@Test
	public void testSchemaUnreservedCharsXml() throws IOException {
		TdSchema schema = getDbApi("oa-chars.yml").getSchema();
		assertModelXml("oa-chars", schema);
	}

	@Test
	public void testSchemaUndefinedRefs() throws IOException {
		OaSchemaApi api = getDbApi("oa-undefined-ref.yml");
		TdSchema schema = api.getSchema();
		assertModel("oa-undefined-ref.txt", schema);
		// Check also that undefined refs are shown in the stored log
		assertEquals("WARN  Can't resolve oaRef: #/components/schemas/NotExistingObject0\n"
				+ "WARN  Can't resolve oaRef: #/components/schemas/NotExistingObject1\n"
				+ "WARN  Can't resolve oaRef: #/components/schemas/NotExistingArray1", api.getOaLogs());
	}

	@Test
	public void testSchemaUndefinedRefsMermaid() throws IOException {
		OaSchemaApi api = getDbApi("oa-undefined-ref.yml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("oa-undefined-ref.md", mermaid);
	}
	
	@Test
	public void testSchemaPathParams() throws IOException {
		OaSchemaApi api = getDbApi("oa-path-params.yml");
		TdSchema schema = api.getSchema();
		assertModel("oa-path-params.txt", schema);
	}

}
