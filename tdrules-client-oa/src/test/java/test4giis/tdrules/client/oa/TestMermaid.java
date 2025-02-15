package test4giis.tdrules.client.oa;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.client.oa.MermaidWriter;
import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Basic drawing is exercised at the integration level, but
 * there are some features that require a little bit more testing.
 */
public class TestMermaid extends Base {

	// Drawing boxes and repetition of entities
	//   req/res content: two different / only one 
	//     (by construction model does not generate repeated paths in an entity)
	//   entity repeated in other paths: request / response / req=res
	//   put, placed before its post (should be processed at the end)
	@Test
	public void test() throws IOException {
		// put, placed before its post
		// req/res content: two different / only one 
		TdSchema schema=new TdSchema().storetype("openapi")
			.addEntitiesItem(new TdEntity().name("EntityReqPut"))
			.addEntitiesItem(new TdEntity().name("EntityResPut"))
			.addEntitiesItem(new TdEntity().name("EntityReq"))
			.addEntitiesItem(new TdEntity().name("EntityRes"))
			.addEntitiesItem(new TdEntity().name("EntityReqRes"));
		schema.getEntity("EntityReqPut").addDdlsItem(new Ddl().command("put").query("/my/postreqres"));
		schema.getEntity("EntityResPut").addDdlsItem(new Ddl().command("put").query("/my/postreqres"));
		schema.getEntity("EntityReq").addDdlsItem(new Ddl().command("post").query("/my/{param}/postreqres"));
		schema.getEntity("EntityRes").addDdlsItem(new Ddl().command("post").query("/my/{param}/postreqres"));
		schema.getEntity("EntityReqRes").addDdlsItem(new Ddl().command("post").query("/my/postreqreq"));
		
		// entity and response in request repeated
		schema.addEntitiesItem(new TdEntity().name("EntityReq0"))
			.addEntitiesItem(new TdEntity().name("EntityRes0"));
		schema.getEntity("EntityReq0").addDdlsItem(new Ddl().command("post").query("/my/postreqres0"));
		schema.getEntity("EntityRes0").addDdlsItem(new Ddl().command("post").query("/my/postreqres0"));
		schema.getEntity("EntityReq0").addDdlsItem(new Ddl().command("post").query("/rep0/postreqres0"));
		schema.getEntity("EntityRes0").addDdlsItem(new Ddl().command("post").query("/rep0/postreqres0"));
		
		// entity in request repeated
		schema.addEntitiesItem(new TdEntity().name("EntityReq1"))
			.addEntitiesItem(new TdEntity().name("EntityRes1"))
			.addEntitiesItem(new TdEntity().name("EntityRes1other"));
		schema.getEntity("EntityReq1").addDdlsItem(new Ddl().command("post").query("/my/postreqres1"));
		schema.getEntity("EntityRes1").addDdlsItem(new Ddl().command("post").query("/my/postreqres1"));
		schema.getEntity("EntityReq1").addDdlsItem(new Ddl().command("post").query("/rep1/postreqres1"));
		schema.getEntity("EntityRes1other").addDdlsItem(new Ddl().command("post").query("/rep1/postreqres1"));
		
		// entity in response repeated
		schema.addEntitiesItem(new TdEntity().name("EntityReq2"))
			.addEntitiesItem(new TdEntity().name("EntityRes2"))
			.addEntitiesItem(new TdEntity().name("EntityReq2other"));
		schema.getEntity("EntityReq2").addDdlsItem(new Ddl().command("post").query("/my/postreqres2"));
		schema.getEntity("EntityRes2").addDdlsItem(new Ddl().command("post").query("/my/postreqres2"));
		schema.getEntity("EntityReq2other").addDdlsItem(new Ddl().command("post").query("/rep2/postreqres2"));
		schema.getEntity("EntityRes2").addDdlsItem(new Ddl().command("post").query("/rep2/postreqres2"));
		
		// request=response repeated in other path
		schema.addEntitiesItem(new TdEntity().name("EntityReq3"))
			.addEntitiesItem(new TdEntity().name("EntityRes3"));
		schema.getEntity("EntityReq3").addDdlsItem(new Ddl().command("post").query("/my/postreqres3"));
		schema.getEntity("EntityRes3").addDdlsItem(new Ddl().command("post").query("/my/postreqres3"));
		schema.getEntity("EntityRes3").addDdlsItem(new Ddl().command("post").query("/rep3/postreqreq3"));
		
		MermaidWriter writer = new MermaidWriter(schema).setLinkEntitiesInPath().setGroupEntitiesInPath();
		System.out.println(writer.getMermaid());
		assertModelMermaid("schema-mermaid-paths.md", writer.getMermaid());
	}
}
