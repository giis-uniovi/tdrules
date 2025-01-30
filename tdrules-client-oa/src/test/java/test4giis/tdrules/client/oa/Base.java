package test4giis.tdrules.client.oa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.portable.util.PropertiesFactory;
import giis.tdrules.model.io.TdSchemaXmlSerializer;
import giis.tdrules.client.oa.OaSchemaApi;
import giis.tdrules.model.io.ModelJsonSerializer;
import giis.tdrules.openapi.model.TdSchema;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

/**
 * Common utilities of all tests, to be used as the base class
 */
public class Base {
	protected Properties config;
	// location of expected and actual test data files
	protected static final String TEST_PATH_INPUT="src/test/resources/inp";
	protected static final String TEST_PATH_BENCHMARK="src/test/resources/bmk";
	protected static final String TEST_PATH_OUTPUT="target";
	
	@Rule
	public TestName testName = new TestName();
	
	@Before
	public void setUp() {
		String fileName = FileUtil.getPath(Parameters.getProjectRoot(), "..", "tdrules4.properties");
		config = new PropertiesFactory().getPropertiesFromFilename(fileName);
	}

	protected OaSchemaApi getDbApi(String fileName) {
		return new OaSchemaApi(FileUtil.getPath(TEST_PATH_INPUT, fileName));
	}

	protected void fileWrite(String path, String fileName, String value) throws IOException {
		FileUtils.writeStringToFile(new File(FilenameUtils.concat(path, fileName)), value, "UTF-8");
	}

	protected String fileRead(String path, String fileName) throws IOException {
		return FileUtils.readFileToString(new File(FilenameUtils.concat(path, fileName)), "UTF-8");
	}

	protected String serialize(TdSchema model) {
		return new ModelJsonSerializer().serialize(model, true);
	}

	protected void assertModel(String fileName, TdSchema actualModel) throws IOException {
		assertEquals("openapi", actualModel.getStoretype());
		assertModelFile(fileName, serialize(actualModel));
	}

	protected void assertModelXml(String fileName, TdSchema actualModel) throws IOException {
		assertEquals("openapi", actualModel.getStoretype());
		assertModelFile(fileName + ".xml", new TdSchemaXmlSerializer().serialize(actualModel));
	}

	protected void assertModelMermaid(String fileName, String mermaidModel) throws IOException {
		assertModelFile(fileName, mermaidModel);
	}

	protected void assertModelFile(String fileName, String actualFile) throws IOException {
		// saves actual in target, reads expected file from resources and performs the
		// assert
		fileWrite(TEST_PATH_OUTPUT, fileName, actualFile);
		actualFile = actualFile.replace("\r", "");
		String expected = fileRead(TEST_PATH_BENCHMARK, fileName).replace("\r", "");
		new VisualAssert().setFramework(Framework.JUNIT4).setBrightColors(true)
			.assertEquals(expected, actualFile, "failed " + fileName, "diff-" + fileName + ".html");
	}

}
