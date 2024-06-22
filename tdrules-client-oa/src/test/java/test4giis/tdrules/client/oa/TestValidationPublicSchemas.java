package test4giis.tdrules.client.oa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.client.oa.MermaidWriter;
import giis.tdrules.client.oa.OaSchemaIdResolver;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Validation test of the schema transformations using some publicly available models
 */
public class TestValidationPublicSchemas extends Base {

	// https://github.com/swagger-api/swagger-petstore
	@Test
	public void testPetstoreOriginal() throws IOException {
		OaSchemaApi api = getDbApi("public-swagger-petstore-v3-1.0.17.yaml");
		TdSchema schema = api.getSchema();
		assertModel("public-swagger-petstore-v3-1.0.17.txt", schema);
		assertEquals("WARN  Can't get the rid for array Customer_address_xa because it has not any upstream with uid\n"
				+ "WARN  Can't get the rid for array Pet_photoUrls_xa because it has not any upstream with uid\n"
				+ "WARN  Can't get the rid for array Pet_tags_xa because it has not any upstream with uid",
				api.getOaLogs());
	}

	// Same petstore, but setting the ids using an id resolver.
	// Tag objects are excluded, as in basis of the implementation of the service,
	// it seems more reasonalbe to do not set any id (objects are always added
	// without any check for existing items.
	@Test
	public void testPetstoreOriginalIdsByConvention() throws IOException {
		OaSchemaApi api = getDbApi("public-swagger-petstore-v3-1.0.17.yaml")
				.setIdResolver(new OaSchemaIdResolver().setIdName("id").excludeEntity("Tag"));
		TdSchema schema = api.getSchema();
		assertModel("public-swagger-petstore-v3-1.0.17-with-ids-by-convention.txt", schema);
		assertEquals("", api.getOaLogs());
	}

	// Modified schema that adds the rids that we need beteween entities
	@Test
	public void testPetstoreWithIds() throws IOException {
		OaSchemaApi api = getDbApi("public-swagger-petstore-v3-1.0.17-with-ids.yaml");
		TdSchema schema = api.getSchema();
		assertModel("public-swagger-petstore-v3-1.0.17-with-ids.txt", schema);
		assertEquals("", api.getOaLogs());
	}

	@Test
	public void testPetstoreWithIdsMermaid() throws IOException {
		OaSchemaApi api = getDbApi("public-swagger-petstore-v3-1.0.17-with-ids.yaml");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("public-swagger-petstore-v3-1.0.17-with-ids.md", mermaid);
	}

	// 3/8/2023
	// https://github.com/ClaudiodelaRiva/GestaoHospital/blob/master/src/main/resources/api-docs.json
	@Test
	public void testGestaoHospital() throws IOException {
		OaSchemaApi api = getDbApi("public-GestaoHospital-original-20290410.json")
				.setIdResolver(new OaSchemaIdResolver().setIdName("id"));
		TdSchema schema = api.getSchema();
		assertModel("public-GestaoHospital-original-20290410.txt", schema);
		assertEquals("WARN  Can't get the rid for array GeoJsonPoint_coordinates_xa because it has not any upstream with uid",
				api.getOaLogs());
	}

	@Test
	public void testGestaoHospitalMermaid() throws IOException {
		OaSchemaApi api = getDbApi("public-GestaoHospital-original-20290410.json")
				.setIdResolver(new OaSchemaIdResolver().setIdName("id"));
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("public-GestaoHospital-original-20290410.md", mermaid);
	}

	// 3/8/2023
	// https://github.com/EMResearch/EMB/blob/master/openapi-swagger/market.json
	@Test
	public void testMarket() throws IOException {
		OaSchemaApi api = getDbApi("public-market-emb-json-20220927.json");
		TdSchema schema = api.getSchema();
		assertModel("public-market-emb-json-20220927.txt", schema);
		assertEquals("WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for Link_rel_xt does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for CartItemDTOReq__links_xa_rel_xt does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for Link_rel_xt does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for ContactsDTOReq__links_xa_rel_xt does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for Link_rel_xt does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for Link_rel_xt does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for LinkRelation does not have any property, generated entity will be empty\n"
				+ "WARN  Open Api schema for UserDTOReq__links_xa_rel_xt does not have any property, generated entity will be empty\n"
				+ "WARN  Can't get the rid for array CartDTO_cartItems_xa because it has not any upstream with uid\n"
				+ "WARN  Can't get the rid for array CartItemDTOReq__links_xa because it has not any upstream with uid\n"
				+ "WARN  Can't get the rid for array ContactsDTOReq__links_xa because it has not any upstream with uid\n"
				+ "WARN  Can't get the rid for array UserDTOReq__links_xa because it has not any upstream with uid",
				api.getOaLogs());
	}

	@Test
	public void testMarketMermaid() throws IOException {
		OaSchemaApi api = getDbApi("public-market-emb-json-20220927.json");
		String mermaid = new MermaidWriter(api.getSchema()).getMermaid();
		assertModelMermaid("public-market-emb-json-20220927.md", mermaid);
	}

	// EvoMaster rest api example
	// https://github.com/EMResearch/rest-api-example (Jan 31, 2023)
	@Test
	public void testRestApiExample() throws IOException {
		OaSchemaApi api = getDbApi("public-rest-api-example-original.json");
		TdSchema schema = api.getSchema();
		assertModel("public-rest-api-example-original.txt", schema);
		// Aparecen dos problemas diferentes:
		// 1- El controlador devuelve Iterable&lt;Item&gt; para getAll(), por lo que OpenApi no lo reconoce,
		//    Deberia devolver List para que lo reconozca como array, o configugurar Docklet
		//    (https://github.com/springfox/springfox/issues/3371)
		// 2- Problema de encoding? El texto Item aparece rodeado por caracteres no ascii, al menos en windows
		assertEquals("WARN  Open Api schema for Iterable«Item» does not have any property, generated entity will be empty",
				api.getOaLogs());
	}

}
