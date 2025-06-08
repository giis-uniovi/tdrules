package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
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

import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.oa.OaLiveUidGen;
import giis.tdrules.store.loader.shared.LoaderException;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Error situations when generating live values.
 */
public class TestOaLiveErrors extends Base {
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
		return new TdSchema().storetype("openapi").addEntitiesItem(master);
	}

	// Default methods to get the data generator
	protected DataLoader getLiveGenerator() {
		IDataAdapter dataAdapter=new OaLiveAdapter(getServerUrl());
		return new DataLoader(getModel(), dataAdapter).setUidGen(new OaLiveUidGen());
	}
	protected String getServerUrl() {
		return "http://127.0.0.1:" + mockServer.getPort() + "/oatest";
	}
	
	@Test
	public void testErrorClient() {
		// Different path in client and serverin the client

		LoaderException exception = assertThrows(LoaderException.class, () -> {
			DataLoader dtg=getLiveGenerator();
			mockPost("/notexists", "{'I1':10}", "{'pk1':991,'I1':10}");
			dtg.load("master", "pk1,i1", "@km1,10");
	    });
	    assertEquals("endWrite: Did not completed properly, response: 404 Not Found - body: "
	    		+ "\n  Posting to: http://127.0.0.1:" + mockServer.getPort() + "/oatest/master"
	    		+ "\n  With payload: {\"I1\":10}", json(exception.getMessage()));
	}
	
	@Test
	public void testErrorServer() {
		// expectation returns error 500
		LoaderException exception = assertThrows(LoaderException.class, () -> {
			DataLoader dtg = getLiveGenerator();
			mockPostError("/master", "{'I1':10}");
			dtg.load("master", "pk1,i1", "@km1,10");
		});
	    assertEquals("endWrite: Did not completed properly, response: 500 Internal Server Error - body: Error message"
	    		+ "\n  Posting to: http://127.0.0.1:" + mockServer.getPort() + "/oatest/master"
	    		+ "\n  With payload: {\"I1\":10}", json(exception.getMessage()));
	}
	
	@Test
	public void testErrorCantSerializeResponse() {
		LoaderException exception = assertThrows(LoaderException.class, () -> {
			DataLoader dtg=getLiveGenerator();
			mockPost("/master", "{'I1':10}", "{ , 'pk1':991,'I1':10}");
			dtg.load("master", "pk1,i1", "@km1,10");
	    });
	    assertTrue("Messaje does not start with expected JsonParseException, message was: "+exception.getMessage(), 
	    		exception.getMessage().startsWith("com.fasterxml.jackson.core.JsonParseException: Unexpected character"));
	}

	@Test
	public void testErrorCantFindGeneratedKey() {
		LoaderException exception = assertThrows(LoaderException.class, () -> {
			DataLoader dtg=getLiveGenerator();
			mockPost("/master", "{'I1':10}", "{'I1':10}");
			dtg.load("master", "pk1,i1", "@km1,10");
	    });
	    assertJson("getLast: no generated value for entity: master - attribute: pk1 - Last response: {'I1':10}", exception.getMessage());
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
	private void mockPostError(String endpointPath, String requestBody) {
		mockServer.when(request()
				.withMethod("POST")
				.withPath("/oatest" + endpointPath)
				.withBody(exact(json(requestBody))), 
				exactly(1)
			).respond(response()
                .withStatusCode(500)
				.withBody("Error message")
		);
	}

}
