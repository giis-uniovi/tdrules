package test4giis.tdrules.client.oa;

import java.io.IOException;

import org.junit.Test;

import giis.portable.util.FileUtil;
import giis.tdrules.client.oa.OaSchemaIdResolver;
import giis.tdrules.model.io.TdSchemaXmlSerializer;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Some tests derived from past issues
 */
public class TestIssues extends Base {
	
	/**
	 * Gitlab rp issue #12:
	 * The uid in an entity can't be located if defined after an array (that should reference the uid of this entity).
	 * Fix: process in order while transforming the schema: uids, rids, rest of attributes
	 */
	@Test
	public void testSchemaBasicTypesAndConstraints() throws IOException {
		String fileName = "issue-gitlab-rp-12-marketArrays";
		OaSchemaApi oas1 = new OaSchemaApi(FileUtil.getPath(TEST_PATH_INPUT, "issue-gitlab-rp-12-marketArrays" +".json"))
				.setIdResolver(new OaSchemaIdResolver().setIdName("id").setIdName("productId"));
		TdSchema schema1 = oas1.getSchema();
		String schema1Xml =new TdSchemaXmlSerializer().serialize(schema1);
		fileWrite(TEST_PATH_OUTPUT, "issue-gitlab-rp-12-marketArrays" + ".xml", schema1Xml);
		assertModelXml(fileName, schema1);
	}

}
