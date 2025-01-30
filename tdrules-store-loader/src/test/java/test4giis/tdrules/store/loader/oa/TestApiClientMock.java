package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import giis.tdrules.model.ModelException;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.IDataAdapter;
import giis.tdrules.store.loader.oa.ApiResponse;
import giis.tdrules.store.loader.oa.ApiWriter;
import giis.tdrules.store.loader.oa.IPathResolver;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.oa.OaLiveUidGen;
import giis.tdrules.store.loader.oa.OaPathResolver;

/**
 * Test a custom ApiClient to mock the servlet environment. This is used to test
 * the HTTP controller endpoints without the need to launch a servlet container
 */
public class TestApiClientMock {

	protected TdSchema getModel() {
		TdEntity master = new TdEntity().name("Master")
				.addAttributesItem(new TdAttribute().name("pk1").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("i1").datatype("int32"));
		TdEntity detail = new TdEntity().name("Detail")
				.addAttributesItem(new TdAttribute().name("pk1").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("Master.pk1"));
		return new TdSchema().storetype("openapi").addEntitiesItem(master).addEntitiesItem(detail);
	}

	// Data generator with a path resolver that uses a custom api writer
	protected DataLoader getLiveGenerator() {
		ApiWriter writer = new CustomApiWriter(); // this mocks the servlet environment and the server
		IPathResolver resolver = new OaPathResolver().setApiWriter(writer);
		IDataAdapter dataAdapter = new OaLiveAdapter("", resolver);
		return new DataLoader(getModel(), dataAdapter).setUidGen(new OaLiveUidGen());
	}

	// This custom api writer returns the content of the body request
	// and simulates a server generated uid by adding it to the response
	public class CustomApiWriter extends ApiWriter {
		public ApiResponse post(String url, String requestBody, boolean usePut) {
			if ("/master".equals(url) && "{\"i1\":2}".equals(requestBody))
				return new ApiResponse(200, "OK", "{\"pk1\":99,\"i1\":2}");
			else if ("/detail".equals(url))
				return new ApiResponse(200, "OK", requestBody);
			else
				return new ApiResponse(404, "NotFound", "");
		}
	}

	// Checks that the custom api writer works fine integrated with the data loading
	// - request sent
	// - response received
	// - a symbolic generated key for master that is recovered and sent to the detail
	@Test
	public void testGenerateMockOk() {
		DataLoader dtg = getLiveGenerator();
		String json1 = dtg.load("master", "pk1=@generatedpk");
		assertEquals("{\"i1\":2}", json1);
		assertEquals("{\"pk1\":99,\"i1\":2}", dtg.getDataAdapter().getLastResponse());
		
		String json2 = dtg.load("detail", "fk1=@generatedpk");
		assertEquals("{\"pk1\":101,\"fk1\":99}", json2);
		assertEquals("{\"pk1\":101,\"fk1\":99}", dtg.getDataAdapter().getLastResponse());
		
		assertEquals("\"master\":{\"i1\":2}\n" + "\"detail\":{\"pk1\":101,\"fk1\":99}",
				dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testGenerateMockError() {
		DataLoader dtg = getLiveGenerator();
		ModelException exception = assertThrows(ModelException.class, () -> {
			dtg.load("notexists", "");
		});
		assertEquals("Can't find any entity in the schema with name notexists", exception.getMessage());
	}

}
