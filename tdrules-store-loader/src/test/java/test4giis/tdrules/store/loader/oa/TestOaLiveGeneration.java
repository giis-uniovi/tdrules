package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.stop.Stop.stopQuietly;

import java.sql.SQLException;
import java.util.Base64;

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
import giis.tdrules.store.loader.oa.OaBasicAuthStore;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.oa.OaLiveUidGen;
import giis.tdrules.store.loader.oa.OaPathResolver;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Data generation and loading with a live adapter.
 * Tests for different models with a local adapter are in a separate class,
 * this focus on different situations regarding 
 * - Sequential uid generator (frontend generated uid), all tests
 * - distinctive features of live generation like authentication, 
 *   path parameters, customization of the path resolver
 * - already tested features where live generation could impact, like
 *   the handling of symbolic keys
 * This does not focus on (tested in separated classes):
 * - Backend generated uids.
 * - Error handling
 */
public class TestOaLiveGeneration extends Base {
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
		TdEntity master=new TdEntity().name("Master")
				.addAttributesItem(new TdAttribute().name("pk1").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("I1").datatype("int32")
			);
		TdEntity detail=new TdEntity().name("Detail")
				.addAttributesItem(new TdAttribute().name("pk1").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("Master.pk1"))
				.addAttributesItem(new TdAttribute().name("i1").datatype("int32")
			);
		return new TdSchema().storetype("openapi")
				.addEntitiesItem(master).addEntitiesItem(detail);
	}

	// Default methods to get the data generator (some tests require other customized versions)
	protected DataLoader getLiveGenerator(TdSchema schema) {
		IDataAdapter dataAdapter=new OaLiveAdapter(getServerUrl());
		return new DataLoader(schema, dataAdapter);
	}
	protected String getServerUrl() {
		return "http://127.0.0.1:" + mockServer.getPort() + "/oatest";
	}
	
	@Test
	public void testGenerateLiveValues() {
		DataLoader dtg=getLiveGenerator(getModel());
		// expected input and output in the mock, if input (generated object) is not the expected, returns 404
		mockPost("/master", "{'pk1':1,'I1':2}", "{'pk1':1,'I1':2}");
		String json1 = dtg.load("master", "", "");
		assertJson("{'pk1':1,'I1':2}", json1);
		mockPost("/master", "{'pk1':101,'I1':102}", "{'pk1':101,'I1':102}");
		String json2 = dtg.load("master", "", "");
		assertJson("{'pk1':101,'I1':102}", json2);
		
		// global result as string
		String expected = "\"master\":{\"pk1\":1,\"I1\":2}\n"
				+ "\"master\":{\"pk1\":101,\"I1\":102}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
		
		// Reset, reset of the data sequence
		// Note that the uid is not generated in the backend because there is no symbolic values
		dtg.reset();
		mockPost("/master", "{'pk1':1,'I1':2}", "{'pk1':1,'I1':2}");
		dtg.load("master", "", "");
		mockPost("/master", "{'pk1':101,'I1':102}", "{'pk1':101,'I1':102}");
		dtg.load("master", "", "");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}
	@Test
	public void testGenerateLiveValuesWithReadonly() {
		// Same schema than testGenerateLiveValues, but I1 is changed to readonly and I2 added
		TdSchema schema = getModel();
		schema.getEntity("master").addAttributesItem(new TdAttribute().name("I2").datatype("int32"));
		schema.getEntity("master").getAttribute("I1").setReadonly("true");
		DataLoader dtg=getLiveGenerator(schema);
		// mock only accepts an object without the readonly I1, but returns I1
		mockPost("/master", "{'pk1':1,'I2':3}", "{'pk1':1,'I1':2,'I2':3}");
		String json1 = dtg.load("master", "", "");
		assertJson("{'pk1':1,'I2':3}", json1);
		// Note difference between getLast and getLastGenerated
		assertJson("{'pk1':1,'I2':3}", dtg.getDataAdapter().getLast());
		assertJson("{'pk1':1,'I1':2,'I2':3}", dtg.getDataAdapter().getLastResponse());
		dtg.getDataAdapter().getLast();
	}
	@Test
	public void testGenerateLiveValuesWithPathParams() {
		// model with a string and the ddl with the post url
		TdSchema schema = getModel();
		schema.getEntity("master")
			.addAttributesItem(new TdAttribute().name("s2").datatype("string"))
			.addDdlsItem(new Ddl().command("post").query("/master/{I1}/{s2}"));
		DataLoader dtg=getLiveGenerator(schema);
		
		// path parameter values are in the url, not in the request body
		mockPost("/master/2/3", "{'pk1':1}", "{'pk1':1,'I1':2,'s2':'3'}");
		String json1 = dtg.load("master", "", "");
		assertJson("{'pk1':1,'I1':2,'s2':'3'}", json1);
		// check parameter values are url encoded in the path
		mockPost("/master/102/ab%3Fcd", "{'pk1':101}", "{'pk1':101,'I1':102,'s2':'ab?cd'}");
		String json2 = dtg.load("master", "s2=ab?cd");
		assertJson("{'pk1':101,'I1':102,'s2':'ab?cd'}", json2);
		
		// global result as string (includes all entity, with param values in the json)
		String expected = "\"master\":{\"pk1\":1,\"I1\":2,\"s2\":\"3\"}\n"
				+ "\"master\":{\"pk1\":101,\"I1\":102,\"s2\":\"ab?cd\"}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	//Tests using a custom path resolver that modifies the behaviour for some entities
	// - Change default path (master)
	// - Set a null path (detail)
	// - Use put instead of post (master2)
	public class OaCustomPathResolver extends OaPathResolver {
		@Override public String getEndpointPath(String entityName) {
			if ("detail".equals(entityName))
				return null;
			else if ("master".equals(entityName))
				return super.getEndpointPath("surrogate/master");
			return super.getEndpointPath(entityName);
		}
		@Override public boolean usePut(String entityName) {
			return "master2".equals(entityName); // this entity will be created by a PUT
		}
	}
	protected DataLoader getLiveGeneratorWithCustomPathResolver(TdSchema schema) {
		IDataAdapter dataAdapter = new OaLiveAdapter(getServerUrl()).setPathResolver(new OaCustomPathResolver());
		return new DataLoader(schema, dataAdapter).setUidGen(new OaLiveUidGen());
	}
	@Test
	public void testGenerateLiveValuesWithCustomPathResolver() {
		// The custom path resolver changes the path of master, 
		// and prevents sending data thorought the api for detail (althoug generation is still done)
		DataLoader dtg=getLiveGeneratorWithCustomPathResolver(getModel());
		mockPost("/surrogate/master", "{'pk1':1,'I1':2}", "{'pk1':1,'I1':2}");
		String json1 = dtg.load("master", "", "");
		assertJson("{'pk1':1,'I1':2}", json1);
		
		// This is the post that is not sent, but generation is done 
		mockPost("/detail", "{}", "{}");
		String json2 = dtg.load("detail", "fk1=1");
		assertJson("{'pk1':101,'fk1':1,'i1':103}", json2);

		// Checks that generation continues at another entity
		mockPost("/surrogate/master", "{'pk1':201,'I1':202}", "{'pk1':201,'I1':202}");
		String json3 = dtg.load("master", "", "");
		assertJson("{'pk1':201,'I1':202}", json3);

		String expected = "\"master\":{\"pk1\":1,\"I1\":2}\n"
				+ "\"detail\":{\"pk1\":101,\"fk1\":1,\"i1\":103}\n"
				+ "\"master\":{\"pk1\":201,\"I1\":202}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}
	@Test
	public void testGenerateLiveValuesWithCustomMethodPut() {
		// The custom path resolver indicates that put is sent instead of post for entity master2
		// Instead of creating a new model, renames the master entity
		TdSchema schema = getModel();
		schema.getEntity("Master").name("Master2");

		DataLoader dtg = getLiveGeneratorWithCustomPathResolver(schema);
		mockOperation("/master2", "{'pk1':1,'I1':2}", "{'pk1':1,'I1':2}", "PUT"); // expects put
		String json1 = dtg.load("master2", "", "");
		assertJson("{'pk1':1,'I1':2}", json1);
		assertEquals("\"master2\":{\"pk1\":1,\"I1\":2}", dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testGenerateLiveValuesWithBasicAuthentication() {
		// Detailed tests are at the unit level (TestOaAuthentication), 
		// here only verifies the auth header is actually sent with the request
		// Extends the model with the attributes of auth provider (Master) and consumer (Detail)
		TdSchema schema = getModel();
		schema.getEntity("Master")
				.addAttributesItem(new TdAttribute().name("username").datatype("string"))
				.addAttributesItem(new TdAttribute().name("password").datatype("string"));
		schema.getEntity("Detail")
				.addAttributesItem(new TdAttribute().name("user").datatype("string"));
		// Configures the auth manager and injects it into the Data Adapter
		OaBasicAuthStore authenticator = new OaBasicAuthStore()
				.setProvider("Master", "username", "password")
				.addConsumer(new String[] { "Detail" }, "user");
		DataLoader dtg = getLiveGenerator(schema);
		((OaLiveAdapter) dtg.getDataAdapter()).setAuthStore(authenticator);
	
		// Post that remembers credentials of usr1
		mockPost("/master", "{'pk1':1,'I1':2,'username':'usr1','password':'abc'}",
				"{'pk1':1,'I1':2,'username':'usr1','password':'abc'}");
		String json1 = dtg.load("Master", "username=usr1, password=abc");
		assertJson("{'pk1':1,'I1':2,'username':'usr1','password':'abc'}", json1);
	
		// Post with a user to get credentials from and auth header in the request
		String header = "Basic " + Base64.getEncoder().encodeToString("usr1:abc".getBytes());
		mockPostWithAuth("/detail", "{'pk1':101,'fk1':1,'i1':103,'user':'usr1'}",
				"{'pk1':101,'fk1':1,'i1':103,'user':'usr1'}", header);
		String json2 = dtg.load("Detail", "fk1=1,user=usr1");
		assertJson("{'pk1':101,'fk1':1,'i1':103,'user':'usr1'}", json2);
	}
	
	@Test
	public void testGenerateLiveSymbolicKeys() {
		DataLoader dtg=getLiveGenerator(getModel());
		
		// Scenario with a master and an entity referencing it, two objects for each
		// Each step configures the mock server to return the same object plus the generted uid
		// Check the json generated and the backend generated uid
		
		//Sequential uid generator sets 1 for the first object created
		mockPost("/master", "{'pk1':1,'I1':10}", "{'pk1':1,'I1':10}");
		String json1 = dtg.load("master", "Pk1=@km1, i1=10");
		assertJson("{'pk1':1,'I1':10}", json1);
		assertEquals(1, dtg.getSymbolicKeyValues().size());
		assertJson("1", dtg.getSymbolicKeyValues().get("master.pk1.@km1"));

		//next in sequence (2)
		mockPost("/master", "{'pk1':2,'I1':20}", "{'pk1':992,'I1':20}");
		String json2 = dtg.load("master", "pk1=@km2, i1=20");
		assertJson("{'pk1':2,'I1':20}", json2);
		assertEquals(2, dtg.getSymbolicKeyValues().size());
		assertJson("2", dtg.getSymbolicKeyValues().get("master.pk1.@km2"));
		
		//a different entity, starts its own sequence
		mockPost("/detail", "{'pk1':1,'fk1':2,'i1':100}", "{'pk1':1,'fk1':2,'i1':100}");
		String json3 = dtg.load("detail", "pk1=@kd1, fk1=@km2, i1=100");
		assertJson("{'pk1':1,'fk1':2,'i1':100}", json3);
		assertEquals(3, dtg.getSymbolicKeyValues().size());
		assertJson("1", dtg.getSymbolicKeyValues().get("detail.pk1.@kd1"));
		
		mockPost("/detail", "{'pk1':2,'fk1':1,'i1':200}", "{'pk1':2,'fk1':991,'i1':200}");
		String json4 = dtg.load("detail", "pk1=@kd2 ,fk1=@km1 ,i1=200");
		assertJson("{'pk1':2,'fk1':1,'i1':200}", json4);
		assertEquals(4, dtg.getSymbolicKeyValues().size());
		assertJson("2", dtg.getSymbolicKeyValues().get("detail.pk1.@kd2"));
		
		// Comprueba el resultado global como string
		String expected = "\"master\":{\"pk1\":1,\"I1\":10}\n"
				+ "\"master\":{\"pk1\":2,\"I1\":20}\n"
				+ "\"detail\":{\"pk1\":1,\"fk1\":2,\"i1\":100}\n"
				+ "\"detail\":{\"pk1\":2,\"fk1\":1,\"i1\":200}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
		
		// Reset, only chek two first steps, sequence starts again at 1
		dtg.reset();
		mockPost("/master", "{'pk1':1,'I1':10}", "{'pk1':1,'I1':10}");
		dtg.load("master", "Pk1=@km1, i1=10");
		assertJson("{'pk1':1,'I1':10}", json1);
		assertEquals(1, dtg.getSymbolicKeyValues().size());
		assertJson("1", dtg.getSymbolicKeyValues().get("master.pk1.@km1"));

		mockPost("/master", "{'pk1':2,'I1':20}", "{'pk1':992,'I1':20}");
		dtg.load("master", "pk1=@km2, i1=20");
		assertJson("{'pk1':2,'I1':20}", json2);
		assertEquals(2, dtg.getSymbolicKeyValues().size());
		assertJson("2", dtg.getSymbolicKeyValues().get("master.pk1.@km2"));

		expected = "\"master\":{\"pk1\":1,\"I1\":10}\n"
				+ "\"master\":{\"pk1\":2,\"I1\":20}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}
		
	// Utility methods to mock the api

	private void mockPost(String endpointPath, String requestBody, String responseBody) {
		mockOperation(endpointPath, requestBody, responseBody, "POST");
	}
	private void mockOperation(String endpointPath, String requestBody, String responseBody, String method) {
		mockServer.when(request()
				.withMethod(method)
				.withPath("/oatest" + endpointPath)
				.withBody(exact(json(requestBody))), 
				exactly(1)
			).respond(response()
				.withBody(json(responseBody))
		);
	}
	private void mockPostWithAuth(String endpointPath, String requestBody, String responseBody, String authHeader) {
		mockServer.when(request()
				.withMethod("POST")
				.withPath("/oatest" + endpointPath)
				.withBody(exact(json(requestBody)))
				.withHeader("Authorization", authHeader), 
				exactly(1)
			).respond(response()
				.withBody(json(responseBody))
		);
	}

}
