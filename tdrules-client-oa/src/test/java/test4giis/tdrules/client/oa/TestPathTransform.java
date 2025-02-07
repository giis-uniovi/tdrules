package test4giis.tdrules.client.oa;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import giis.portable.util.FileUtil;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Transformations from Open API models to TdSchema regarding the paths in the schema.
 * The PathTransformer keeps track of the operations (post and get) that are available
 * for each entity and allows setting the Ddl attributes in the transformed model.
 * 
 * Tests here exercise the different situations (entity in request, response, etc.).
 * Use a single OpenAPI yaml file that is mutated to represent the test situations,
 * then generate the model and compare the Ddls
 * 
 * Additional tests (at the end) exercise the filtering of entities that are not 
 * in any path (when this feature is set)
 */
public class TestPathTransform extends Base {
	
	// POST operations (requests and responses)
	
	@Test
	public void testPathRequestNotEqualToResponse() { // baseline
		String yaml = getTemplate();
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityRes1 post /api/p1", api);
	}
	
	@Test
	public void testPathRequestIsEqualToResponse() {
		String yaml = getTemplate();
		yaml = yaml.replace("#/components/schemas/EntityRes1", "#/components/schemas/EntityReq1");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1", api);
	}
	
	@Test
	public void testMediaTypeJsonAfterXml() {
		String yaml = getTemplate();
		yaml = yaml.replace("application/json:", "application/tmp:");
		yaml = yaml.replace("application/xml:", "application/json:");
		yaml = yaml.replace("application/tmp:", "application/xml:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq2 post /api/p1\n"
				+ "EntityRes2 post /api/p1", api);
	}
	
	@Test
	public void testMediaTypeIsXxRange() {
		String yaml = getTemplate();
		yaml = yaml.replace("application/json:", "'*/*':");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityRes1 post /api/p1", 
				"WARN  Accepting media type range */* as media type for post /api/p1 (request)\n"
				+ "WARN  Accepting media type range */* as media type for post /api/p1 (response)", api);
	}
	
	@Test
	public void testResponseIsOkNot200But2XX() {
		String yaml = getTemplate();
		yaml = yaml.replace("'200':", "'299':");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityRes1 post /api/p1", api);
	}
	
	@Test
	public void testResponseIsOkMultiple2XX() { // takes first
		String yaml = getTemplate();
		yaml = yaml.replace("'200':", "'201':");
		yaml = yaml.replace("'300':", "'202':");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityRes1 post /api/p1", api);
	}
	
	@Test
	public void testResponseIsOkAfterKo() {
		String yaml = getTemplate();
		yaml = yaml.replace("'200':", "'399':");
		yaml = yaml.replace("'300':", "'200':");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityRes3 post /api/p1", api);
	}
	
	@Test
	public void testResponseIsKo() {
		String yaml = getTemplate();
		yaml = yaml.replace("'200':", "'399':");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1",
				"WARN  Can't find any 2XX response for post /api/p1 (response)", api);
	}
	
	// Other methods (put) and combinations with post
	
	@Test
	public void testMethodsOnlyPut() {
		String yaml = getTemplate();
		yaml = yaml.replace("get:", "put:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityRes1 post /api/p1\n"
				+ "EntityReq1b put /api/p2\n"
				+ "EntityRes1b put /api/p2", api);
	}

	@Test
	public void testMethodsEntityWithPostAndThenPut() {
		String yaml = getTemplate();
		yaml = yaml.replace("get:", "put:");
		yaml = yaml.replace("/api/p2:", ""); // to join the post with this put under same path
		yaml = yaml.replace("'#/components/schemas/EntityReq1b'", "'#/components/schemas/EntityReq1'");
		yaml = yaml.replace("'#/components/schemas/EntityRes1b'", "'#/components/schemas/EntityRes1'");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityReq1 put /api/p1\n"
				+ "EntityRes1 post /api/p1\n"
				+ "EntityRes1 put /api/p1", api);
	}

	@Test
	public void testMethodsEntityWithPutAndThenPost() { // always post stored first
		String yaml = getTemplate();
		yaml = yaml.replace("post:", "put:");
		yaml = yaml.replace("get:", "post:");
		yaml = yaml.replace("/api/p2:", ""); // to join the post with this put under same path
		yaml = yaml.replace("'#/components/schemas/EntityReq1b'", "'#/components/schemas/EntityReq1'");
		yaml = yaml.replace("'#/components/schemas/EntityRes1b'", "'#/components/schemas/EntityRes1'");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityReq1 put /api/p1\n"
				+ "EntityRes1 post /api/p1\n"
				+ "EntityRes1 put /api/p1", api);
	}

	@Test
	public void testMethodsEntityMultiplePost() {
		String yaml = getTemplate();
		yaml = yaml.replace("get:", "post:");
		yaml = yaml.replace("'#/components/schemas/EntityReq1b'", "'#/components/schemas/EntityReq1'");
		yaml = yaml.replace("'#/components/schemas/EntityRes1b'", "'#/components/schemas/EntityRes1'");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityReq1 post /api/p2\n"
				+ "EntityRes1 post /api/p1\n"
				+ "EntityRes1 post /api/p2", api);
	}
	
	// Reusable request/response definitions defined under components:
	
	@Test
	public void testReusableRequest() {
		String yaml = getTemplate();
		yaml = yaml.replace("\r", "").replace(
				"requestBody:\n"
				+ "        content:", 
				"requestBody:\n"
				+ "        $ref: '#/components/requestBodies/ReusableRequest'\n"
				+ "        undefined:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityRes1 post /api/p1\n"
				+ "EntityReusableReq post /api/p1", "", api);
	}
	
	@Test
	public void testReusableResponse() {
		String yaml = getTemplate();
		yaml = yaml.replace("\r", "").replace(
				"'200':\n"
				+ "          content:", 
				"'200':\n"
				+ "          $ref: '#/components/responses/ReusableResponse'\n"
				+ "          undefined:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1\n"
				+ "EntityReusableRes post /api/p1", "", api);
	}
	
	// Edge cases for invalid specification because some entries are missing in the yaml
	
	@Test
	public void testWithoutPaths() {
		String yaml = getTemplate();
		yaml = yaml.replace("paths:", "undefined:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("", api); // no log required, could use only schema and a path resolver
	}
	
	@Test
	public void testWithoutRequestBody() {
		String yaml = getTemplate();
		yaml = yaml.replace("requestBody:", "undefined:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityRes1 post /api/p1", 
				"WARN  Can't find the request body content for post /api/p1 (request)", api);
	}
	
	@Test
	public void testWithoutResponses() {
		String yaml = getTemplate();
		yaml = yaml.replace("responses:", "undefined:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1",
				"WARN  Can't find the responses for post /api/p1 (response)", api);
	}
	
	@Test
	public void testWithoutContent() {
		String yaml = getTemplate();
		yaml = yaml.replace("content:", "undefined:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("", 
				"WARN  Can't find the 200 response content for post /api/p1 (response)", api);
	}
	
	@Test
	public void testWithoutResponseCodes() {
		String yaml = getTemplate();
		yaml = yaml.replace("'200':", "undefined1:");
		yaml = yaml.replace("'300':", "undefined1:");
		yaml = yaml.replace("'400':", "undefined1:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityReq1 post /api/p1",
				"WARN  Can't find any 2XX response for post /api/p1 (response)", api);
	}
	
	@Test
	public void testWithoutSupportedMediaTypes() {
		String yaml = getTemplate();
		yaml = yaml.replace("application/json:", "undefined1:");
		yaml = yaml.replace("application/json:", "undefined2:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("", "WARN  Can't find an application/json media type for post /api/p1 (request)\n"
				+ "WARN  Can't find an application/json media type for post /api/p1 (response)", api);
	}
	
	@Test
	public void testWithoutSchema() {
		String yaml = getTemplate();
		yaml = yaml.replace("schema:", "undefined:");
		OaSchemaApi api = getApi(yaml);
		assertPaths("", "WARN  Can't find a schema definition for post /api/p1 (request)\n"
				+ "WARN  Can't find a schema definition for post /api/p1 (response)", api);
	}
	
	@Test
	public void testWithoutSchemaHavingAnyDescription() {
		String yaml = getTemplate();
		yaml = yaml.replace("$ref: '#/components/schemas/EntityReq1'", "");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityRes1 post /api/p1", 
				"WARN  Can't find a schema definition for post /api/p1 (request)", api);
	}
	
	@Test
	public void testWithoutSchemaHavingValidRef() { // e.g a primitive
		String yaml = getTemplate();
		yaml = yaml.replace("$ref: '#/components/schemas/EntityReq1'", "type: string");
		OaSchemaApi api = getApi(yaml);
		assertPaths("EntityRes1 post /api/p1", 
				"WARN  Can't find a schema with a valid ref for post /api/p1 (request)", api);
	}
	
	// Utilities
	
	private String getTestFileName() {
		return FileUtil.getPath(TEST_PATH_OUTPUT, "tmp-path-transform-" + testName.getMethodName() + ".yml");
	}
	private String getTemplate() {
		return FileUtil.fileRead(TEST_PATH_INPUT, "oa-path-transform-template.yml");
	}
	protected OaSchemaApi getApi(String yaml) {
		String fileName = getTestFileName();
		FileUtil.fileWrite(fileName, yaml);
		return new OaSchemaApi(fileName);
	}

	protected void assertPaths(String expectedDdls, OaSchemaApi api) {
		assertPaths(expectedDdls, "", api);
	}
	protected void assertPaths(String expectedDdls, String expectedLogs, OaSchemaApi api) {
		StringBuilder sb = new StringBuilder();
		for (TdEntity entity : api.getSchema().getEntities()) {
			List<Ddl> ddls = entity.getDdls();
			for (Ddl ddl : ddls)
				sb.append("\n" + entity.getName() + " " + ddl.getCommand() + " " + ddl.getQuery());
		}
		assertEquals("Ddls: ", expectedDdls, sb.toString().trim());
		assertEquals("Logs: ", expectedLogs, api.getOaLogs().trim());
	}

	protected void assertEntities(String expectedEntities, OaSchemaApi api) {
		StringBuilder sb = new StringBuilder();
		for (TdEntity entity : api.getSchema().getEntities()) {
			sb.append("\n" + entity.getName());
		}
		assertEquals("Entities: ", expectedEntities, sb.toString().trim());
	}
	
	// Filtering of entities not present in paths.
	// Uses the same template, but with the setting setOnlyEntitiesInPaths(true)
	// to filter out other entities
	
	@Test
	public void testFilterOfEntitiesNotInPathsPost() {
		String yaml = getTemplate();
		OaSchemaApi api = getApi(yaml).setOnlyEntitiesInPaths(true);
		assertEntities("EntityReq1\nEntityRes1", api);
	}
	
	@Test
	public void testFilterOfEntitiesNotInPathsPostAndPut() {
		String yaml = getTemplate();
		yaml = yaml.replace("post:", "put:");
		yaml = yaml.replace("get:", "post:");
		OaSchemaApi api = getApi(yaml).setOnlyEntitiesInPaths(true);
		assertEntities("EntityReq1\nEntityRes1\nEntityReq1b\nEntityRes1b", api);
	}
	
}
