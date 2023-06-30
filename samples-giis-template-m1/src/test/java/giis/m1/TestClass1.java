package giis.m1;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClass1 {
	private final Logger log=LoggerFactory.getLogger(this.getClass());

	@Rule 
	public TestName testName = new TestName();
	
	@Before
	public void setUp() {
		log.info("****** Running test: {} ******", testName.getMethodName());
	}

	@Test
	public void testFunction11() {
		assertEquals("11", new Class1().function11());
	}
	
}
