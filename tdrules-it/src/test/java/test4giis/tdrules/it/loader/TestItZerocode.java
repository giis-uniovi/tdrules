package test4giis.tdrules.it.loader;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.oa.script.ZerocodeAdapter;
import test4giis.tdrules.it.util.ZerocodeRunner;
import test4giis.tdrules.it.util.ZerocodeScriptTestClassPetstore;
import test4giis.tdrules.it.util.ZerocodeScriptTestClassWorkplace;

/**
 * Test all it SUTs using a zerodode adapter (with backend generated uids that are represented as jsonpath tokens).
  * Lifecycle of each test:
 * - Delete all data in the backend (setUp)
 * - Generate the zerocode scenario, save and assert:
 * - Run the scenario from zerocode to generate the data.
 *   - this run uses the scenario stored in the expected folder.
 *   - if the scenario changes, the file generated in target must be moved to expected folder before continue.
 *     Don't forget refreshing the environment because next step takes the secenarios from the classpath.
 *   - the execution launches the scenario tests included in two script test classes (at util)
 *   - the execution does not guarantees test failure if something fails, requires verify the data at next step
 * - Get the data generated from the backend and assert against expected
 */
public class TestItZerocode extends TestItLive {

	@Test
	@Override
	public void TestItWorkplace() throws IOException {
		DataLoader dtg = getDataLoader(new ZerocodeAdapter(getServerUrl()));
		loadWorkplace(dtg);
		assertFiles(dtg.getDataAdapter().getAllAsString(), "it-zerocode-workplace-script.json");
		
		new ZerocodeRunner().run(ZerocodeScriptTestClassWorkplace.class);
		assertFiles(getWorkplaceAll(), "it-zerocode-workplace-data.json");
	}

	@Test
	@Override
	public void TestItPetstore() throws IOException {
		DataLoader dtg = getDataLoader(new ZerocodeAdapter(getServerUrl()));
		loadPetstore(dtg);
		assertFiles(dtg.getDataAdapter().getAllAsString(), "it-zerocode-petstore-script.json");
		
		new ZerocodeRunner().run(ZerocodeScriptTestClassPetstore.class);
		assertFiles(getPetstoreAll(), "it-zerocode-petstore-data.json");
	}
	
}
