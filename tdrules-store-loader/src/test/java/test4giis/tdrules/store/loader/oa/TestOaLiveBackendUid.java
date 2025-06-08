package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.stop.Stop.stopQuietly;

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
	
	private void mockAndLoadMasters(DataLoader dtg) {
		mockPost("/oatest/master1", "{'s1':'s10'}", "{'pk1':111,'s1':'s10'}");
		dtg.load("master1", "pk1=@km11, s1=s10");
		mockPost("/oatest/master1", "{'s1':'s20'}", "{'pk1':222,'s1':'s20'}");
		dtg.load("master1", "pk1=@km12, s1=s20");
		
		mockPost("/oatest/master2", "{'i2':10}", "{'pk2':'aaa','i2':10}");
		dtg.load("master2", "pk2=@km21, i2=10");
		mockPost("/oatest/master2", "{'i2':20}", "{'pk2':'bbb','i2':20}");
		dtg.load("master2", "pk2=@km22, i2=20");
	}
	private void assertSymbols(DataLoader dtg, int symbolCount, String detailName) {
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
	private String getExpectedAll() {
		return "\"master1\":{\"s1\":\"s10\"}\n"
				+ "\"master1\":{\"s1\":\"s20\"}\n"
				+ "\"master2\":{\"i2\":10}\n"
				+ "\"master2\":{\"i2\":20}\n"
				+ "\"detail1\":{\"fk1\":111,\"fk2\":\"bbb\",\"di\":100}\n"
				+ "\"detail1\":{\"fk1\":222,\"fk2\":\"aaa\",\"di\":200}";
	}
	
	@Test
	public void testLiveBackUidBase() {
		DataLoader dtg=getLiveGenerator(getModel());
		
		// Fill each master and then detail. First detail linked to first and last of each master
		mockAndLoadMasters(dtg);
		
		mockPost("/oatest/detail1", "{'fk1':111,'fk2':'bbb','di':100}", "{'dk':1111,'fk1':111,'fk2':'bbb','di':100}");
		dtg.load("detail1", "dk=@dk1, fk1=@km11, fk2=@km22, di=100");
		mockPost("/oatest/detail1", "{'fk1':222,'fk2':'aaa','di':200}", "{'dk':2222,'fk1':222,'fk2':'aaa','di':200}");
		dtg.load("detail1", "dk=@dk2, fk1=@km12, fk2=@km21, di=200");
		
		// Check symbols and the global result as string
		assertSymbols(dtg, 6, "detail1");
		String expected = getExpectedAll();
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testLiveBackUidRidsAreUids() {
		DataLoader dtg=getLiveGenerator(getModel());
		
		mockAndLoadMasters(dtg);
		
		// Same as base, but detail excludes the uid. As a result, there will be only 4 symbols
		mockPost("/oatest/detail2", "{'fk1':111,'fk2':'bbb','di':100}", "{'fk1':111,'fk2':'bbb','di':100}");
		dtg.load("detail2", "fk1=@km11, fk2=@km22, di=100");
		mockPost("/oatest/detail2", "{'fk1':222,'fk2':'aaa','di':200}", "{'fk1':222,'fk2':'aaa','di':200}");
		dtg.load("detail2", "fk1=@km12, fk2=@km21, di=200");
		
		assertSymbols(dtg, 4, "");
		String expected = getExpectedAll().replace("detail1", "detail2");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testLiveBackUidPathParams() {
		DataLoader dtg=getLiveGenerator(getModel());
		
		mockAndLoadMasters(dtg);
		
		// Same as base, but rids are passed in the path instead of body
		mockPost("/oatest/detail3/111/bbb", "{'di':100}", "{'dk':1111,'fk1':111,'fk2':'bbb','di':100}");
		dtg.load("detail3", "dk=@dk1, fk1=@km11, fk2=@km22, di=100");
		mockPost("/oatest/detail3/222/aaa", "{'di':200}", "{'dk':2222,'fk1':222,'fk2':'aaa','di':200}");
		dtg.load("detail3", "dk=@dk2, fk1=@km12, fk2=@km21, di=200");
		
		assertSymbols(dtg, 6, "detail3");
		String expected = getExpectedAll().replace("detail1", "detail3");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	// Utility methods to mock the api

	private void mockPost(String endpointPath, String requestBody, String responseBody) {
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
