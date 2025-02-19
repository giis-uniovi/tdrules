package test4giis.tdrules.client.oa;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import giis.portable.util.FileUtil;
import giis.tdrules.client.oa.MermaidWriter;
import giis.tdrules.client.oa.OaSchemaIdResolver;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.openapi.model.TdSchema;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Smoke tests of the schema transformations using some publicly available models.
 * Includes only generation with different parameters, not checking the result contents,
 * only the execution free of errors.
 * Results are written in files for further manual checking.
 */
@RunWith(JUnitParamsRunner.class)
public class TestSmokePublicSchemas extends Base {

	@Test
	@Parameters({ 
		// EMB Repository (2025-02-10) https://github.com/WebFuzzing/EMB/tree/master/openapi-swagger
		// Excluding those included in validation
		"emb/bibliothek.json",
		"emb/catwatch.json",
		"emb/cwa-verification.json",
		"emb/familie-ba-sak.json",
		"emb/features-service.json",
		"emb/genome-nexus.json",
		"emb/languagetool.json",
		"emb/ocvn-rest.json",
		"emb/proxyprint.json",
		"emb/reservations-api.json",
		"emb/restcountries.yaml",
		"emb/rest-ncs.json",
		"emb/rest-news.json",
		"emb/rest-scs.json",
		"emb/scout-api.json",
		"emb/session-service.json",
		// Apideck (2025-02-10) https://github.com/apideck-libraries/openapi-specs
		// (selection)
		"apideck/pos.yml",
		"apideck/crm.yml",
		"apideck/hris.yml",
		"apideck/ats.yml",
		"apideck/ecommerce.yml",
		// Swiss openbanking api (2025-02-10) https://github.com/openbankingproject-ch/obp-apis
		"openbanking/swiss-ng-api.yaml"
		})

	public void testSmoke(String fileName) throws IOException {
		String source = "src/test/resources/inp/smoke/" + fileName;
		String target = "target/smoke/" + fileName;
		
		// Converts only entities in paths, using id resolver
		OaSchemaApi api = new OaSchemaApi(source)
				.setFilter(new giis.tdrules.client.oa.OaSchemaFilter().add("_links*", "*").add("*", "_links*"))
				.setOnlyEntitiesInPaths().setExcludeVisitedNotInScope()
				.setIdResolver(new OaSchemaIdResolver().setIdName("id"));
		TdSchema schema = api.getSchema();
		FileUtil.fileWrite(target + "-paths.txt", serialize(schema));
		FileUtil.fileWrite(target + "-paths.md", 
				new MermaidWriter(schema).setLeftToRight().setGroupEntitiesInPath().getMermaid());
		
		// Converts all entities, using id resolver
		api = new OaSchemaApi(source)
				.setIdResolver(new OaSchemaIdResolver().setIdName("id"));
		schema = api.getSchema();
		FileUtil.fileWrite(target + "-all.txt", serialize(schema));
		FileUtil.fileWrite(target + "-all.md",
				new MermaidWriter(schema).setLeftToRight().setGroupEntitiesInPath().getMermaid());
	}

}
