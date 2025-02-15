package test4giis.tdrules.client.oa;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.client.oa.OaSchemaIdResolver;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Automatic determination of uids and rids, both in schema and in paths
 */
public class TestIdResolver extends Base {

	// TestSchemaConvert uses vendor extensions to determine the ids in the schema.
	// Here the focus is on how the ids are automatically determined without vendor extensions
	@Test
	public void testSchemaIdResolver() throws IOException {
		OaSchemaApi api = getDbApi("oa-ids.yml")
				.setIdResolver(new OaSchemaIdResolver().setIdName("uid").excludeEntity("Excluded"));
		TdSchema schema = api.getSchema();
		assertModel("schema-ids.txt", schema);
	}

	// TestPathTransform focuses in the details of how the OpenAPI is parsed to determine
	// the paths that are included or excluded, and TestSchemaConvert has a test to 
	// check how the path parameters with vendor extensions are handle.
	// Here the focus is on how the ids in paths are automatically are determined
	@Test
	public void testSchemaPathParamsIdResolver() throws IOException {
		OaSchemaApi api = getDbApi("oa-path-params-resolve.yml").setIdResolver(new OaSchemaIdResolver().setIdName("id"));
		TdSchema schema = api.getSchema();
		assertModel("schema-path-params-resolve.txt", schema);
	}

}
