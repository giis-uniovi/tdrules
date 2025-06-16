package test4giis.tdrules.it.util;

import org.jsmart.zerocode.core.domain.Scenario;
import org.jsmart.zerocode.core.runner.ZeroCodeUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ZeroCodeUnitRunner.class)
public class ZerocodeScriptTestClassPetstore {

	@Test
	@Scenario("it-zerocode-petstore-script.json") // read from src/test/resources
	public void testZerocodeScriptPetstore() throws Exception {

	}

}
