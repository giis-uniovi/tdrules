package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base {
	Logger log=LoggerFactory.getLogger(this.getClass());
	
	protected static String TEST_PATH_INPUT="src/test/resources/inp";
	protected static String TEST_PATH_BENCHMARK="src/test/resources/bmk";
	protected static String TEST_PATH_OUTPUT="target";
	
	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws SQLException {
		log.debug("*** Running test: {}", testName.getMethodName());
	}
	
	protected void fileWrite(String path, String fileName, String value) throws IOException {
		FileUtils.writeStringToFile(new File(FilenameUtils.concat(path, fileName)), value, "UTF-8");
	}
	
	protected String fileRead(String path, String fileName) throws IOException {
		return FileUtils.readFileToString(new File(FilenameUtils.concat(path, fileName)), "UTF-8");
	}

	protected String json(String jsonWitoutQuotes) {
		return jsonWitoutQuotes.replace("'", "\"");
	}
	
	protected void assertJson(String expected, String actual) {
		assertEquals(json(expected), json(actual));
	}

}
