package test4giis.tdrules.it.loader;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.client.oa.OaSchemaIdResolver;
import giis.tdrules.it.sut.invoker.OpenApiGeneratorApplication;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.gen.DictionaryAttrGen;
import giis.tdrules.store.loader.gen.IAttrGen;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.oa.ApiWriter;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.oa.OaLiveUidGen;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

/**
 * Test the integration of the OpenApi model transformation and data loading 
 * using a live data adapter with backend generated uids.
 * See the SUT specification in main/resources/sut-api.yml.
 * The same tests using Zerocode and Postman adapters are in subclasses of this.
 * 
 * Lifecycle of each test:
 * - Delete all data in the backend (setUp)
 * - Generate the data with the live adapter
 * - Get the data generated from the backend and assert against expected
 * 
 * Notes:
 * - Subclasses for Zerocode and Postman have a different lifecycle because they first generate the script and then execute it
 * - Runs in the spring boot test context to automatically spin-up the backend when the tests start.
 * - Uses a fixed port (default 8080) 
 */
@SpringBootTest(classes = {OpenApiGeneratorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@SuppressWarnings("deprecation")
@RunWith(SpringRunner.class)
public class TestItLive {
	Logger log=LoggerFactory.getLogger(this.getClass());
	
	protected static final String TEST_PATH_BENCHMARK="src/test/resources";
	protected static final String TEST_PATH_OUTPUT="target";
	
	// Cache for the schema to avoid multiple calls per test (this class and subclasses).
	private static TdSchema schemaCache = null;

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() {
		log.debug("*** Running test: {}", testName.getMethodName());
		ApiWriter api = new ApiWriter();
		api.delete(getServerUrl() + "/workplace/all");
		api.delete(getServerUrl() + "/petstore/all");
	}

	protected TdSchema getSchema() {
		if (schemaCache == null)
			schemaCache = new OaSchemaApi("src/main/resources/sut-api.yml")
					.setIdResolver(new OaSchemaIdResolver().setIdName("id")).getSchema();
		return schemaCache;
	}
	protected DataLoader getDataLoader(IDataAdapter dataAdapter) {
		return new DataLoader(getSchema(), dataAdapter)
				.setUidGen(new OaLiveUidGen())
				.setAttrGen(getDictionary());
	}
	protected String getServerUrl() {
		return "http://localhost:8080";
	}
	protected IAttrGen getDictionary() {
		return new DictionaryAttrGen()
				.with("Staff", "name").dictionary("James Smith", "Mary Johnson", "Robert Williams", "Patricia Brown", "David Garcia", "Elizabeth Miller", "William Davis", "Barbara Wilson")
				.with("Proj", "name").dictionary("Postman", "Zerocode", "GitHub", "Jenkins", "GitLab", "SonarQube", "Maven", "NodeJS")
				.with("Pet", "name").dictionary("Max", "Luna", "Charlie", "Bella", "Cooper", "Daisy", "Milo", "Lucy")
				.with("Category", "name").dictionary("Tiger", "Lion", "Monkey", "Snake")
				.with("Address", "city").dictionary("Springfield", "Franklin", "Greenville", "Bristol", "Clinton", "Fairview", "Salem", "Madison")
				.with("Address", "zip").padLeft('0', 6)
				.with("Pet_photoUrls_xa", "url").padLeft('0', 6).mask("http://localhost/photos/{}.jpg")
				.with("Tagx", "name").dictionary("Puppy", "Young", "Old")
				;
	}
	
	protected void fileWrite(String path, String fileName, String value) throws IOException {
		FileUtils.writeStringToFile(new File(FilenameUtils.concat(path, fileName)), value, "UTF-8");
	}
	
	protected String fileRead(String path, String fileName) throws IOException {
		return FileUtils.readFileToString(new File(FilenameUtils.concat(path, fileName)), "UTF-8");
	}

	protected void assertFiles(String actual, String fileName) throws IOException {
		fileWrite(TEST_PATH_OUTPUT, fileName, actual);
		String expected = fileRead(TEST_PATH_BENCHMARK, fileName);
		VisualAssert va = new VisualAssert().setNormalizeEol(true).setFramework(Framework.JUNIT4);
		va.assertEquals(expected, actual, "Files compared: " + fileName, "diff-" + fileName + ".html");
	}

	protected String getWorkplaceAll() throws JsonProcessingException {
		String body = new ApiWriter().get(getServerUrl() + "/workplace/all").getBody();
		return new ObjectMapper().readTree(body).toPrettyString();
	}
	
	protected void loadWorkplace(DataLoader dtg) {
		dtg.load("Staff", "id=@s1");
		dtg.load("Proj", "id=@p1");
		dtg.load("Staff", "id=@s2");
		dtg.load("Proj", "id=@p2");
		dtg.load("Work", "staffId=@s1, projId=@p2");
		dtg.load("Work", "staffId=@s2, projId=@p1");
	}
	
	@Test
	public void TestItWorkplace() throws IOException {
		DataLoader dtg = getDataLoader(new OaLiveAdapter(getServerUrl()));
		loadWorkplace(dtg);
		assertFiles(getWorkplaceAll(), "it-live-workplace-data.json");
	}

	protected String getPetstoreAll() throws JsonProcessingException {
		String body = new ApiWriter().get(getServerUrl() + "/petstore/all").getBody();
		return new ObjectMapper().readTree(body).toPrettyString();
	}
	
	protected void loadPetstore(DataLoader dtg) {
		dtg.load("Category", "id=@cat1");
		dtg.load("Pet_photoUrls_xa", "fk_xa=@pet1");
		dtg.load("Pet_photoUrls_xa", "fk_xa=@pet1");
		dtg.load("Tagx", "id=@tag1");
		dtg.load("Pet_Tags_xa", "fk_xa=@pet1, id=@tag1");
		dtg.load("Pet", "id=@pet1, category::id=@cat1");
	}
	
	@Test
	public void TestItPetstore() throws IOException {
		DataLoader dtg = getDataLoader(new OaLiveAdapter(getServerUrl()));
		loadPetstore(dtg);
		assertFiles(getPetstoreAll(), "it-live-petstore-data.json");
	}

	@Test
	public void TestItSchema() throws IOException {
		TdSchema schema = getSchema();
		// gets xml format that is more compact
		String json = new giis.tdrules.model.io.TdSchemaXmlSerializer().serialize(schema);
		assertFiles(json, "it-model.xml");
	}
	
}
