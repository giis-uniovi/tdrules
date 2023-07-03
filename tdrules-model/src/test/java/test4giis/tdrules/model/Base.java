package test4giis.tdrules.model;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

public class Base {
	protected static final Logger log = LoggerFactory.getLogger(Base.class);
	
	@Rule public TestName testName = new TestName();
	
	@Before
	public void setUp() {
		log.info("****** Running test: {} ******", testName.getMethodName());
	}

    protected VisualAssert va=new VisualAssert().setFramework(Framework.JUNIT4);

	protected static String TEST_PATH_BENCHMARK = Parameters.isJava()
			? "src/test/resources"
			: FileUtil.getPath(Parameters.getProjectRoot(), "../tdrules-model/src/test/resources");
	protected static String TEST_PATH_OUTPUT = Parameters.isJava()
			? "target"
			: FileUtil.getPath(Parameters.getProjectRoot(), "reports");
	
	protected static HashMap<String, String> singletonMap(String key, String value) {
		HashMap<String, String> map = new HashMap<>();
		map.put(key, value);
		return map;
	}
	public String readFile(String fileName) {
		return FileUtil.fileRead(TEST_PATH_BENCHMARK, fileName);
	}
	public void writeFile(String fileName, String content) {
		FileUtil.createDirectory(TEST_PATH_OUTPUT); // ensure that folder exists
		FileUtil.fileWrite(TEST_PATH_OUTPUT, fileName, content);
	}

	public void assertContains(String expectedSubstring, String actual) {
		assertThat(actual, CoreMatchers.containsString(expectedSubstring));
	}
}
