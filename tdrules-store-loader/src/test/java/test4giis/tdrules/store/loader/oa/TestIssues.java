package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import giis.portable.util.FileUtil;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.oa.OaLocalAdapter;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

/**
 * Some tests derived from past issues
 */
public class TestIssues extends Base {
	private VisualAssert va = new VisualAssert().setFramework(Framework.JUNIT4);

	/**
	 * Gitlab qagrow issue #34 reproduction
	 */
	@Test
	public void testPkInArray() throws IOException {
		String fileName = "issue-gitlab-qagrow-34-pk-null-in-array";
		OaSchemaApi oas1 = new OaSchemaApi(FileUtil.getPath(TEST_PATH_INPUT, fileName + ".json"));
		TdSchema schema1 = oas1.getSchema();

		DataLoader dg = new DataLoader(schema1, new OaLocalAdapter());
		dg.load("ProductDTORes", "productId=@ProductDTORes_productId_0");
		dg.load("CartDTO_cartItems_xa",
				"pk_xa=@CartDTO_cartItems_xa_pk_xa_0,productId=@ProductDTORes_productId_0,fk_xa=XXX");
		dg.load("UserDTORes", "email=XXX");
		dg.load("UserDTORes", "email=1");
		dg.load("CartDTO", "user=XXX");
		dg.load("CartDTO", "user=1");
		
		String actual=dg.getDataAdapter().getAllAsString().replace("\r", "");
		FileUtil.fileWrite(TEST_PATH_OUTPUT, fileName + ".txt", actual);
		String expected=
				("'ProductDTORes':{'productId':1,'age':2}\n"
				+ "'UserDTORes':{'email':'XXX','name':'202'}\n"
				+ "'UserDTORes':{'email':'1','name':'302'}\n"
				+ "'CartDTO':{'user':'XXX','cartItems':[{'productId':1,'quantity':104}],'totalItems':403}\n"
				+ "'CartDTO':{'user':'1','cartItems':[],'totalItems':503}").replace("'", "\"");
		assertEquals(expected, actual);
	}
	
	/**
	 * Gitlab qagrow issue #37 reproduction:
	 * The value of a constraint in age: "maximum": 50 is transformed into age<=5E+1
	 * The number in exponential form is not recogni<ed as a number by NumberUtils
	 * Testing: integer / real with no decimals in limits / real with decimals in limits
	 */
	@Test
	public void testConstraintsFromOaSchema() {
		OaSchemaApi api = new OaSchemaApi("src/test/resources/inp/issue-gitlab-qagrow-37-market-constraints.json");
		TdSchema model = api.getSchema();
		DataLoader dtg = new DataLoader(model, new OaLocalAdapter());
		dtg.load("ProductDTOReq", "");
		dtg.load("ProductDTOReq", "");
		dtg.load("ProductDTOReq", "");
		String expected = "\"ProductDTOReq\":{\"age\":1,\"alcohol\":95.3,\"price\":110.4}\n"
				+ "\"ProductDTOReq\":{\"age\":50,\"alcohol\":10.2,\"price\":10.3}\n"
				+ "\"ProductDTOReq\":{\"age\":48,\"alcohol\":20.2,\"price\":20.3}";
		va.assertEquals(expected, dtg.getDataAdapter().getAllAsString(), "", "testConstraintsFromOaSchema.html");
	}

}
