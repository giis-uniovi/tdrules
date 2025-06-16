package test4giis.tdrules.it.loader;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.oa.script.PostmanAdapter;
import test4giis.tdrules.it.util.PostmanRunner;

/**
 * Test all it SUTs using a postman adapter (with backend generated uids that are represented as variables).
  * Lifecycle of each test:
 * - Delete all data in the backend (setUp)
 * - Generate the postman collection, save and assert
 * - Run the postman collection from the cli (using newman) to generate the data.
 *   - this run uses the postman collection stored in the expected folder.
 *   - if the collection changes, the file generated in target must be moved to expected folcer before continue.
 *   - the execution does not guarantees test failure if something fails, requires verify the data at next step
 * - Get the data generated from the backend and assert against expected.
 */
public class TestItPostman extends TestItLive {

	@Test
	@Override
	public void TestItWorkplace() throws IOException {
		DataLoader dtg = getDataLoader(new PostmanAdapter(getServerUrl()));
		loadWorkplace(dtg);
		assertFiles(dtg.getDataAdapter().getAllAsString(), "it-postman-workplace-script.json");
		
		new PostmanRunner().run("src/test/resources/it-postman-workplace-script.json");
		assertFiles(getWorkplaceAll(), "it-postman-workplace-data.json");
	}

	@Test
	@Override
	public void TestItPetstore() throws IOException {
		DataLoader dtg = getDataLoader(new PostmanAdapter(getServerUrl()));
		loadPetstore(dtg);
		assertFiles(dtg.getDataAdapter().getAllAsString(), "it-postman-petstore-script.json");
		
		new PostmanRunner().run("src/test/resources/it-postman-petstore-script.json");
		assertFiles(getPetstoreAll(), "it-postman-petstore-data.json");
	}

}
