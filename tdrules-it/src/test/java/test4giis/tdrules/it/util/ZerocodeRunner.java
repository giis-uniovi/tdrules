package test4giis.tdrules.it.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Runs a Zerocode scenario declared in a script test class.
 * It does not guarantees test failure if anything fails, it only fails when the scenario assertions fail.
 * Requires further verification of generated test data.
 */
public class ZerocodeRunner {

	@SuppressWarnings("rawtypes")
	public void run(Class clazz) {
		Result result = JUnitCore.runClasses(clazz);
		assertEquals(1, result.getRunCount());
		// note that this assert only checks the verifications in the zerocode script,
		// it does not detect a server failure
		assertTrue("Zerocode scenario execution has failures: " + result.getFailures().toString(),
				result.getFailures().isEmpty());
	}
}
