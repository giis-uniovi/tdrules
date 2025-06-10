package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.stop.Stop.stopQuietly;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;

import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.oa.OaLiveUidGen;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Data generation and loading with a live adapter and backend generated uid
 * Exercises the uid generation and the symbol assignments:
 * - uid data type: int/string
 * - rid is/is not part of the uid
 * - rid are passed in the body/path params
 * Tests with:
 * - all combinations of types (use two master)
 * - base with rid not part of the uid and passed in the body
 * - variants with rid part of the uid/passed in path params
 * 
 * Additional tests with some situations where the handling of the symbolic uid can have impact:
 * - Nested object with an uid
 */
public class TestOaLiveBackendUid extends Base {
	// A single MockServer for this class, each test initializes its expectation before post
	private static ClientAndServer mockServer=null;
	
	@BeforeClass
	public static void setUpClass() {
		mockServer = startClientAndServer(PortFactory.findFreePort());
	}
	@AfterClass
	public static void tearDownClass() throws SQLException {
		if (mockServer!=null) {
			stopQuietly(mockServer);
			mockServer=null;
		}
	}
	
	protected TdSchema getModel() {
		TdEntity master1=new TdEntity().name("Master1")
				.addAttributesItem(new TdAttribute().name("pk1").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("s1").datatype("string")
			).addDdlsItem(new Ddl().command("post").query("/oatest/master1"));
		TdEntity master2=new TdEntity().name("Master2")
				.addAttributesItem(new TdAttribute().name("pk2").datatype("string").uid("true"))
				.addAttributesItem(new TdAttribute().name("i2").datatype("integer")
			).addDdlsItem(new Ddl().command("post").query("/oatest/master2"));
		TdEntity detail1=new TdEntity().name("Detail1")
				.addAttributesItem(new TdAttribute().name("dk").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("Master1.pk1"))
				.addAttributesItem(new TdAttribute().name("fk2").datatype("string").rid("Master2.pk2"))
				.addAttributesItem(new TdAttribute().name("di").datatype("int32")
			).addDdlsItem(new Ddl().command("post").query("/oatest/detail1"));
		TdEntity detail2=new TdEntity().name("Detail2")
				.addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("Master1.pk1").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk2").datatype("string").rid("Master2.pk2").uid("true"))
				.addAttributesItem(new TdAttribute().name("di").datatype("int32")
			).addDdlsItem(new Ddl().command("post").query("/oatest/detail2"));
		TdEntity detail3=new TdEntity().name("Detail3")
				.addAttributesItem(new TdAttribute().name("dk").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("Master1.pk1"))
				.addAttributesItem(new TdAttribute().name("fk2").datatype("string").rid("Master2.pk2"))
				.addAttributesItem(new TdAttribute().name("di").datatype("int32")
			).addDdlsItem(new Ddl().command("post").query("/oatest/detail3/{fk1}/{fk2}"));
		return new TdSchema().storetype("openapi")
				.addEntitiesItem(master1).addEntitiesItem(master2)
				.addEntitiesItem(detail1).addEntitiesItem(detail2).addEntitiesItem(detail3);
	}

	// Default methods to get the data generator (some tests require other customized versions)
	// Note that uses a live uid gen and the path should be resolved from the model
	protected DataLoader getLiveGenerator(TdSchema schema) {
		IDataAdapter dataAdapter=new OaLiveAdapter(getServerUrl());
		return new DataLoader(schema, dataAdapter).setUidGen(new OaLiveUidGen());
	}
	protected String getServerUrl() {
		return "http://127.0.0.1:" + mockServer.getPort();
	}
	
	// To reuse as tests differ in a few things
	
	protected void mockAndLoadMasters(DataLoader dtg) {
		mockPost("/oatest/master1", "{'s1':'s10'}", "{'pk1':111,'s1':'s10'}");
		dtg.load("master1", "pk1=@km11, s1=s10");
		mockPost("/oatest/master1", "{'s1':'s20'}", "{'pk1':222,'s1':'s20'}");
		dtg.load("master1", "pk1=@km12, s1=s20");
		
		mockPost("/oatest/master2", "{'i2':10}", "{'pk2':'aaa','i2':10}");
		dtg.load("master2", "pk2=@km21, i2=10");
		mockPost("/oatest/master2", "{'i2':20}", "{'pk2':'bbb','i2':20}");
		dtg.load("master2", "pk2=@km22, i2=20");
	}
	protected void assertSymbols(DataLoader dtg, int symbolCount, String detailName) {
		assertEquals(symbolCount, dtg.getSymbolicKeyValues().size());
		assertJson("111", dtg.getSymbolicKeyValues().get("master1.pk1.@km11"));
		assertJson("222", dtg.getSymbolicKeyValues().get("master1.pk1.@km12"));
		assertJson("aaa", dtg.getSymbolicKeyValues().get("master2.pk2.@km21"));
		assertJson("bbb", dtg.getSymbolicKeyValues().get("master2.pk2.@km22"));
		if (symbolCount == 4)
			return;
		assertJson("1111", dtg.getSymbolicKeyValues().get(detailName +".dk.@dk1"));
		assertJson("2222", dtg.getSymbolicKeyValues().get(detailName +".dk.@dk2"));
	}
	protected void assertAll(DataLoader dtg, String detailName) throws IOException {
		String expected = "\"master1\":{\"s1\":\"s10\"}\n"
				+ "\"master1\":{\"s1\":\"s20\"}\n"
				+ "\"master2\":{\"i2\":10}\n"
				+ "\"master2\":{\"i2\":20}\n"
				+ "\"detail1\":{\"fk1\":111,\"fk2\":\"bbb\",\"di\":100}\n"
				+ "\"detail1\":{\"fk1\":222,\"fk2\":\"aaa\",\"di\":200}";
		expected = expected.replace("detail1", detailName);
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}
	protected void assertAllFiles(DataLoader dtg, String fileName) throws IOException {
		String actual = dtg.getDataAdapter().getAllAsString();
		fileWrite(TEST_PATH_OUTPUT, fileName, actual);
		String expected = fileRead(TEST_PATH_BENCHMARK, fileName);
		VisualAssert va = new VisualAssert().setNormalizeEol(true).setFramework(Framework.JUNIT4);
		va.assertEquals(expected, actual, "Files compared: " + fileName, "diff-" + fileName + ".html");
	}
	
	@Test
	public void testLiveBackUidBase() throws IOException {
		DataLoader dtg=getLiveGenerator(getModel());
		
		// Fill each master and then detail. First detail linked to first and last of each master
		mockAndLoadMasters(dtg);
		
		mockPost("/oatest/detail1", "{'fk1':111,'fk2':'bbb','di':100}", "{'dk':1111,'fk1':111,'fk2':'bbb','di':100}");
		dtg.load("detail1", "dk=@dk1, fk1=@km11, fk2=@km22, di=100");
		mockPost("/oatest/detail1", "{'fk1':222,'fk2':'aaa','di':200}", "{'dk':2222,'fk1':222,'fk2':'aaa','di':200}");
		dtg.load("detail1", "dk=@dk2, fk1=@km12, fk2=@km21, di=200");
		
		// Check symbols and the global result as string
		assertSymbols(dtg, 6, "detail1");
		assertAll(dtg, "detail1");
	}

	@Test
	public void testLiveBackUidRidsAreUids() throws IOException {
		DataLoader dtg=getLiveGenerator(getModel());
		
		mockAndLoadMasters(dtg);
		
		// Same as base, but detail excludes the uid. As a result, there will be only 4 symbols
		mockPost("/oatest/detail2", "{'fk1':111,'fk2':'bbb','di':100}", "{'fk1':111,'fk2':'bbb','di':100}");
		dtg.load("detail2", "fk1=@km11, fk2=@km22, di=100");
		mockPost("/oatest/detail2", "{'fk1':222,'fk2':'aaa','di':200}", "{'fk1':222,'fk2':'aaa','di':200}");
		dtg.load("detail2", "fk1=@km12, fk2=@km21, di=200");
		
		assertSymbols(dtg, 4, "");
		assertAll(dtg, "detail2");
	}
	
	@Test
	public void testLiveBackUidPathParams() throws IOException {
		DataLoader dtg=getLiveGenerator(getModel());
		
		mockAndLoadMasters(dtg);
		
		// Same as base, but rids are passed in the path instead of body
		mockPost("/oatest/detail3/111/bbb", "{'di':100}", "{'dk':1111,'fk1':111,'fk2':'bbb','di':100}");
		dtg.load("detail3", "dk=@dk1, fk1=@km11, fk2=@km22, di=100");
		mockPost("/oatest/detail3/222/aaa", "{'di':200}", "{'dk':2222,'fk1':222,'fk2':'aaa','di':200}");
		dtg.load("detail3", "dk=@dk2, fk1=@km12, fk2=@km21, di=200");
		
		assertSymbols(dtg, 6, "detail3");
		assertAll(dtg, "detail3");
	}
	
	
	// Special case: Nested object with uid
	
	protected TdSchema getNestObjModel() {
		TdEntity nestobj=new TdEntity().name("nestobj")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("nest").datatype("nestobj_nest_xt").compositetype("type")
			).addDdlsItem(new Ddl().command("post").query("/oatest/nestobj"));
		// the nested object inside nestobj, referencing nestobj generated after model transform
		TdEntity nestobjxt=new TdEntity().name("nestobj_nest_xt").entitytype("type").subtype("nested")
				.addAttributesItem(new TdAttribute().name("nid").datatype("integer").rid("nested.nid"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string")
			).addDdlsItem(new Ddl().command("post").query("/oatest/nestobj_nest_xt"));
		TdEntity nested=new TdEntity().name("nested")
				.addAttributesItem(new TdAttribute().name("nid").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string")
			).addDdlsItem(new Ddl().command("post").query("/oatest/nested"));
		return new TdSchema().storetype("openapi")
				.addEntitiesItem(nestobjxt).addEntitiesItem(nestobj).addEntitiesItem(nested);
	}
	protected void mockAndLoadNestedObject(DataLoader dtg) {
		mockPost("/oatest/nested", "{'value':'abc'}", "{'nid':1111, 'value':'abc'}");
		dtg.load("nested", "nid=@nid1, value=abc");
		mockPost("/oatest/nestobj", "{'nest':{'nid':1111,'value':'abc'}}", "{'id':11,'nest':{'nid':1111,'value':'abc'}}");
		dtg.load("nestobj", "id=@id1, nest::nid=@nid1");
	}
	protected void assertNestedObject(DataLoader dtg) throws IOException {
		String expected = "\"nested\":{\"value\":\"abc\"}\n"
				+ "\"nestobj\":{\"nest\":{\"nid\":1111,\"value\":\"abc\"}}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}
	@Test
	public void testLiveNestedObjectBackUid() throws IOException {
		DataLoader dtg=getLiveGenerator(getNestObjModel());
		mockAndLoadNestedObject(dtg);
		assertNestedObject(dtg);
	}
	
	// Utility methods to mock the api

	protected void mockPost(String endpointPath, String requestBody, String responseBody) {
		mockOperation(endpointPath, requestBody, responseBody, "POST");
	}
	private void mockOperation(String endpointPath, String requestBody, String responseBody, String method) {
		mockServer.when(request()
				.withMethod(method)
				.withPath(endpointPath)
				.withBody(exact(json(requestBody))), 
				exactly(1)
			).respond(response()
				.withBody(json(responseBody))
		);
	}

}
