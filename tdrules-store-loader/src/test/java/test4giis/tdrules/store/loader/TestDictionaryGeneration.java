package test4giis.tdrules.store.loader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.store.loader.IAttrGen;
import giis.tdrules.store.loader.gen.DictionaryAttrGen;

/**
 * Generation of values from a dictionary
 */
public class TestDictionaryGeneration {

	// -different attributes same entity
	// -different entities
	// -dictionary recycling
	// -case insensitive search coordinates
	@Test
	public void testCompleteCoordinatesMainScenario() {
		IAttrGen generator = new DictionaryAttrGen()
				.with("aa", "xx").dictionary("xxone", "xxtwo")
				.with("Aa", "yY").dictionary("yyone", "yytwo", "yythree")
				.with("cc", "zz").dictionary("zzone", "zztwo");
		assertEquals("xxone", generator.generateString("aa", "xx", 0));
		assertEquals("yyone", generator.generateString("aa", "yy", 0));
		assertEquals("zzone", generator.generateString("cc", "zz", 0));

		// gets some values up to recycling
		assertEquals("xxtwo", generator.generateString("aa", "xx", 0));
		assertEquals("xxone-1", generator.generateString("aa", "xx", 0));
		assertEquals("xxtwo-1", generator.generateString("aa", "xx", 0));
		assertEquals("xxone-2", generator.generateString("aa", "xx", 0));

		// continues with other
		assertEquals("yytwo", generator.generateString("aa", "yy", 0));
		assertEquals("zztwo", generator.generateString("cc", "zz", 0));
	}

	// -attribute does not have dictionary (parent AttrGen applies)
	// -limit string length
	@Test
	public void testCoordinatesWithoutDictionary() {
		IAttrGen generator = new DictionaryAttrGen()
				.with("aa", "xx").dictionary("xxone", "xxtwo");
		assertEquals("xone", generator.generateString("aa", "xx", 4));
		generator.incrementAttrCount();
		assertEquals("2", generator.generateString("aa", "yy", 0));
		generator.incrementCount();
		generator.resetAttrCount();
		assertEquals("xtwo", generator.generateString("aa", "xx", 4));
		generator.incrementAttrCount();
		assertEquals("102", generator.generateString("aa", "yy", 0));
	}

	@Test
	public void testDictionaryIgnoredIfNoString() {
		IAttrGen generator = new DictionaryAttrGen()
				.with("aa", "xx").dictionary("xxone", "xxtwo");
		assertEquals("1", generator.generateNumber(null, "aa", "xx"));
	}

	// -string with/without dictionary
	// -number
	@Test
	public void testMask() {
		IAttrGen generator = new DictionaryAttrGen()
				.with("aa", "xx").dictionary("xxone", "xxtwo").mask("MM{}MM")
				.with("aa", "yy").mask("NN{}NN")
				.with("cc", "zz").mask("99{}");
		assertEquals("MMxxoneMM", generator.generateString("aa", "xx", 0));
		assertEquals("NN1NN", generator.generateString("aa", "yy", 0));
		assertEquals("991", generator.generateNumber(null, "cc", "zz"));
	}

	// -pad number/string
	// -pad only
	// -pad and mask (mask applied after pad)
	@Test
	public void testPad() {
		IAttrGen generator = new DictionaryAttrGen()
				.with("aa", "xx").padLeft('0', 4)
				.with("aa", "yy").mask("XX{}XX").padLeft('*', 4)
				.with("cc", "zz").dictionary("AA", "BB").mask("s{}s").padLeft('-', 4);
		assertEquals("0001", generator.generateNumber(null, "aa", "xx"));
		assertEquals("XX***1XX", generator.generateNumber(null, "aa", "yy"));
		assertEquals("s--AAs", generator.generateString("cc", "zz", 0));
	}

	// still not tested:
	// -dictionary ignored if no string o number
	// -check that 'with' was set before any configuration (not implemented)

}
