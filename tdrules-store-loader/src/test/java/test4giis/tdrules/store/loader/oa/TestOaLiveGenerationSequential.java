package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.socket.PortFactory;

import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.IDataAdapter;
import giis.tdrules.store.loader.oa.IPathResolver;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.oa.OaPathResolver;

/**
 * Data generation and loading with a live adapter.
 * Focus on the uid generation at the backend,
 * same situations of TestOaLiveGenerator (subclass)
 */
public class TestOaLiveGenerationSequential extends TestOaLocalGeneration {

	private static ClientAndServer mockServer=null;

	@BeforeClass
	public static void setUpClass() {
		// Here the mock serer is used only as a proxy, always answer with success
		mockServer = startClientAndServer(PortFactory.findFreePort());
		mockServer.when(request().withMethod("POST")).respond(response().withStatusCode(200));
	}
	@AfterClass
	public static void tearDownClass() throws SQLException {
		if (mockServer!=null) {
			stopQuietly(mockServer);
			mockServer=null;
		}
	}
	
	@Override
	protected DataLoader getGenerator(TdSchema model) {
		IPathResolver resolver=new OaPathResolver();
		IDataAdapter dataAdapter=new OaLiveAdapter("http://127.0.0.1:" + mockServer.getPort() + "/oatest", resolver);
		return new DataLoader(model, dataAdapter);

	}
	
	// As the goal here is to check the backend uid generation, the asserts are overriden to 
	// check these values.
	// As the MockServer recorded requests cannot be cleaned (expectations can) and this mock is static,
	// a global variable counts how many requests where read in previous tests
	protected static int numReadRequests=0;
	@Override
	protected void assertRequests(String expected, String actual) {
		StringBuilder sb=new StringBuilder();
		HttpRequest[] requests = mockServer.retrieveRecordedRequests(request().withMethod("POST"));
		int totalRequests=requests.length;
		for (int i=numReadRequests; i<totalRequests; i++) {
			numReadRequests++;
			String body=requests[i].getBodyAsJsonOrXmlString().replace(" ", "").replace("\n", "").replace("\r", "");
			String object=requests[i].getPath().getValue().replace("/oatest/", "");
			sb.append("\"" + object + "\":" + body + "\n");
		}
		actual=sb.toString().trim();
		assertEquals(json(expected), actual);
	}
	
}
