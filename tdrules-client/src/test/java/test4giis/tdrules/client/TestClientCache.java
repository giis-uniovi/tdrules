package test4giis.tdrules.client;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.unlimited;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.mockserver.socket.PortFactory;
import org.mockserver.verify.VerificationTimes;

import giis.portable.util.FileUtil;
import giis.portable.util.JavaCs;
import giis.tdrules.client.TdRulesApi;
import giis.tdrules.client.TdRulesCacheManager;
import giis.tdrules.model.io.ModelJsonSerializer;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.VersionBody;
import giis.tdrules.openapi.model.QueryEntitiesBody;
import giis.tdrules.openapi.model.QueryParam;
import giis.tdrules.openapi.model.QueryParametersBody;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdRule;
import giis.tdrules.openapi.model.TdRules;
import giis.tdrules.openapi.model.TdRulesBody;

/**
 * Check services with caches (black-box)
 */
public class TestClientCache {

	private static ClientAndServer mockServer = null;
	private static String cacheFolder = "";

	@BeforeClass
	public static void setUpClass() {
		mockServer = startClientAndServer(PortFactory.findFreePort());
		// a single folder for class execution (tests use different versions)
		cacheFolder = "target/test-rules-cache/" + JavaCs.getUniqueId();
	}
	@AfterClass
	public static void tearDownClass() {
		if (mockServer != null) {
			stopQuietly(mockServer);
			mockServer = null;
		}
	}
	@Before
	public void setUp() {
		mockServer.reset();
		TdRulesCacheManager.reset();
	}
	
	// Basic cache transitions with same request and different services
	// - no cache set
	// - cache creation/reading: no hit -> hit
	// - invoking different service (as above)
	// - already cached (invoke fist service)
	// - clear cache
	@Test
	public void testCacheRuleServices() {
		TdSchema schema = new TdSchema().storetype("dbx").addEntitiesItem(new TdEntity().name("t"));
		String query = "select a from t where a='x'";
		String ruleFpc = "SELECT * FROM t WHERE a IS NULL";
		String ruleMut = "SELECT a FROM t WHERE a <> 'x'";

		// Start without cache
		createExpectation("/rules", getReq(schema, query, "opt"), getRes("fpc", ruleFpc));
		createExpectation("/mutants", getReq(schema, query, "opt"), getRes("mutants", ruleMut));
		createVersionExpectation("1.0.0");
		TdRulesApi api = getApi();
		
		assertService(api.getRules(schema, query, "opt"), "/api/v4/rules", 1, ruleFpc);

		// Set cache, first time the service is invoked, second time does not (cache  hit)
		api.setCache(cacheFolder);
		assertService(api.getRules(schema, query, "opt"), "/api/v4/rules", 2, ruleFpc);
		assertService(api.getRules(schema, query, "opt"), "/api/v4/rules", 2, ruleFpc);

		// different service, same sequence, first time call the service, next cache hit
		assertService(api.getMutants(schema, query, "opt"), "/api/v4/mutants", 1, ruleMut);
		assertService(api.getMutants(schema, query, "opt"), "/api/v4/mutants", 1, ruleMut);

		// cache hit from first expectation, call service when disabling cache
		assertService(api.getRules(schema, query, "opt"), "/api/v4/rules", 2, ruleFpc);
		api.setCache("");
		assertService(api.getRules(schema, query, "opt"), "/api/v4/rules", 3, ruleFpc);

		// content of the cache folder
		assertEquals(1, FileUtil.getFileListInDirectory(cacheFolder + "/1.0.0/mutantsPost").size());
		assertEquals(1, FileUtil.getFileListInDirectory(cacheFolder + "/1.0.0/rulesPost").size());
}

	public TdRulesApi getApi() {
		return new TdRulesApi("http://127.0.0.1:" + mockServer.getPort() + "/api/v4");
	}
	private void createExpectation(String endpointPath, TdRulesBody requestBody, TdRules responseBody) {
		mockServer.when(request()
				.withPath("/api/v4" + endpointPath)
				.withBody(new JsonBody(new ModelJsonSerializer().serialize(requestBody, false))),
				unlimited())
			.respond(response().withStatusCode(200)
				.withContentType(MediaType.APPLICATION_JSON)
				.withBody(new ModelJsonSerializer().serialize(responseBody, false))
			);
	}
	private void createVersionExpectation(String version) {
		mockServer.when(request()
				.withPath("/api/v4/version"),
				unlimited())
			.respond(response().withStatusCode(200)
				.withContentType(MediaType.APPLICATION_JSON)
				.withBody(new ModelJsonSerializer().serialize(new VersionBody().serviceVersion(version), false))
			);
	}

	private TdRulesBody getReq(TdSchema schema, String query, String options) {
		return new TdRulesBody().schema(schema).query(query).options(options);
	}

	private TdRules getRes(String rulesClass, String ruleQuery) {
		return new TdRules().rulesClass(rulesClass).addRulesItem(new TdRule().query(ruleQuery));
	}

	private void assertService(TdRules rules, String path, int timesExecuted, String ruleQuery) {
		mockServer.verify(request().withPath(path), VerificationTimes.exactly(timesExecuted));
		assertEquals(ruleQuery, rules.getRules().get(0).getQuery());
	}

	// Basic cache transitions with different request payloads and same service
	// - cache creation/reading: no hit -> hit
	// - each choice change schema / query / options
	@Test
	public void testCacheRuleRequests() {
		TdSchema schema0 = new TdSchema().storetype("dbx").addEntitiesItem(new TdEntity().name("t"));
		TdSchema schema1 = new TdSchema().storetype("dbx").addEntitiesItem(new TdEntity().name("u"));
		String query0 = "select a from t where a=1";
		String query1 = "select a from u where b=2";
		String rule000 = "SELECT * FROM t WHERE a IS NULL";
		String rule100 = "SELECT * FROM u WHERE b IS NULL";
		String rule010 = "SELECT * FROM t WHERE b <> 1";
		String rule001 = "SELECT * FROM t WHERE b = 0";

		createExpectation("/rules", getReq(schema0, query0, "opt0"), getRes("fpc", rule000));
		createExpectation("/rules", getReq(schema1, query0, "opt0"), getRes("fpc", rule100));
		createExpectation("/rules", getReq(schema0, query1, "opt0"), getRes("fpc", rule010));
		createExpectation("/rules", getReq(schema0, query0, "opt1"), getRes("fpc", rule001));
		createVersionExpectation("2.0.0");
		TdRulesApi api = getApi();

		// Two consecutive invocations with each combination (first no hit, second hit)
		api.setCache(cacheFolder);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 1, rule000);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 1, rule000);
		assertService(api.getRules(schema1, query0, "opt0"), "/api/v4/rules", 2, rule100);
		assertService(api.getRules(schema1, query0, "opt0"), "/api/v4/rules", 2, rule100);
		assertService(api.getRules(schema0, query1, "opt0"), "/api/v4/rules", 3, rule010);
		assertService(api.getRules(schema0, query1, "opt0"), "/api/v4/rules", 3, rule010);
		assertService(api.getRules(schema0, query0, "opt1"), "/api/v4/rules", 4, rule001);
		assertService(api.getRules(schema0, query0, "opt1"), "/api/v4/rules", 4, rule001);
		// back to first (already cached)
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 4, rule000);
		
		// get Version was called once even we had many hits
		mockServer.verify(request().withPath("/api/v4/version"), VerificationTimes.exactly(1));
		// content of the cache folder
		assertEquals(4, FileUtil.getFileListInDirectory(cacheFolder + "/2.0.0/rulesPost").size());
	}

	// A cached payload is invalidated when the service version changes
	// (cache is stored under another folder)
	@Test
	public void testCacheChangeVersion() {
		TdSchema schema0 = new TdSchema().storetype("dbx").addEntitiesItem(new TdEntity().name("t"));
		String query0 = "select a from t where a=1";
		String rule000 = "SELECT * FROM t WHERE a IS NULL";

		createExpectation("/rules", getReq(schema0, query0, "opt0"), getRes("fpc", rule000));
		createVersionExpectation("3.0.0");
		TdRulesApi api = getApi();

		// Scenario with first version
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 1, rule000);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 2, rule000);
		api.setCache(cacheFolder);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 3, rule000);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 3, rule000);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 3, rule000);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 3, rule000);
		// get Version was called once even we had 3 hits
		mockServer.verify(request().withPath("/api/v4/version"), VerificationTimes.exactly(1));

		// Refresh server mock with different version
		mockServer.clear(request().withPath("/api/v4/version"));
		TdRulesCacheManager.reset();
		createVersionExpectation("3.0.1");
		api = getApi();
		api.setCache(cacheFolder);

		// Cache is invalidated because version changed, call the service again
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 4, rule000);
		assertService(api.getRules(schema0, query0, "opt0"), "/api/v4/rules", 4, rule000);
	}

	// parameters and entities also have cache
	@Test
	public void testCacheOtherServices() {
		String query = "select * from t where key='val'";
		QueryParametersBody paramRes = new QueryParametersBody().query(query)
				.addParametersItem(new QueryParam().name("key").value("val"));
		QueryEntitiesBody entitiesRes = new QueryEntitiesBody().query(query)
				.addEntitiesItem("t").addEntitiesItem("u");
		createExpectationx("/query/parameters", query, new ModelJsonSerializer().serialize(paramRes, false));
		createExpectationx("/query/entities", query, new ModelJsonSerializer().serialize(entitiesRes, false));
		createVersionExpectation("4.0.0");
		TdRulesApi api = getApi();
		api.setCache(cacheFolder);

		QueryParametersBody params = api.getParameters(query);
		assertEquals(query, params.getQuery());
		assertEquals("key", params.getParameters().get(0).getName());
		assertEquals("val", params.getParameters().get(0).getValue());
		mockServer.verify(request().withPath("/api/v4/query/parameters"), VerificationTimes.exactly(1));
		params = api.getParameters(query);
		assertEquals(query, params.getQuery());
		assertEquals("key", params.getParameters().get(0).getName());
		assertEquals("val", params.getParameters().get(0).getValue());
		mockServer.verify(request().withPath("/api/v4/query/parameters"), VerificationTimes.exactly(1));

		QueryEntitiesBody entities = api.getEntities(query);
		assertEquals(query, entities.getQuery());
		assertEquals("t", entities.getEntities().get(0));
		mockServer.verify(request().withPath("/api/v4/query/entities"), VerificationTimes.exactly(1));
		entities = api.getEntities(query);
		assertEquals(query, entities.getQuery());
		assertEquals("t", entities.getEntities().get(0));
		mockServer.verify(request().withPath("/api/v4/query/entities"), VerificationTimes.exactly(1));

		mockServer.verify(request().withPath("/api/v4/version"), VerificationTimes.exactly(1));
	}

	private void createExpectationx(String endpointPath, String query, String response) {
		mockServer.when(request()
				.withPath("/api/v4" + endpointPath)
				.withBody("\"" + query + "\""),
				unlimited())
			.respond(response().withStatusCode(200)
				.withContentType(MediaType.APPLICATION_JSON)
				.withBody(response)
			);
	}
}
