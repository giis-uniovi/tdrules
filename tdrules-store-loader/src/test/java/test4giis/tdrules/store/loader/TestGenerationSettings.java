package test4giis.tdrules.store.loader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.store.loader.gen.DeterministicAttrGen;
import giis.tdrules.store.loader.gen.DictionaryAttrGen;
import giis.tdrules.store.loader.gen.IAttrGen;

/**
 * How optional settings of the Attribute Generators influence the generated values
 */
public class TestGenerationSettings {
	
	@Test
	public void testGenerateDatesDefault() {
		//values generated do not give a large jump when incrementCount,
		//values are given by the sum of both counters
		DeterministicAttrGen gen= new DeterministicAttrGen();
		assertEquals("2007-01-01", gen.generateDate());
		gen.incrementAttrCount();
		assertEquals("2007-01-02", gen.generateDate());
		gen.incrementCount();
		gen.incrementAttrCount();
		assertEquals("2007-01-04", gen.generateDate());
	}

	@Test
	public void testGenerateDatesMinYear() {
		//values generated do not give a large jump when incrementCount,
		//values are given by the sum of both counters
		IAttrGen gen= new DeterministicAttrGen().setMinYear(2054);
		assertEquals("2054-01-01", gen.generateDate());
		gen.incrementAttrCount();
		assertEquals("2054-01-02", gen.generateDate());
		gen.incrementCount();
		gen.incrementAttrCount();
		assertEquals("2054-01-04", gen.generateDate());
	}
	
	// Limits to int values (when the schema does not specify any) must
	// be set through a dictionary because are attribute specific
	@Test
	public void testGenerateNumberInInterval() {
		IAttrGen gen= new DictionaryAttrGen().with("entity", "attr").setInterval(2, 4);
		assertEquals("4", gen.generateNumber(null, "entity", "attr")); // first is 1, out of rang, produces 4
		gen.incrementAttrCount();
		assertEquals("2", gen.generateNumber(null, "entity", "attr")); //2
		gen.incrementAttrCount();
		assertEquals("3", gen.generateNumber(null, "entity", "attr")); //3
		gen.incrementAttrCount();
		assertEquals("4", gen.generateNumber(null, "entity", "attr")); //4
		gen.incrementAttrCount();
		assertEquals("2", gen.generateNumber(null, "entity", "attr")); //5 out of range, produces 2
		gen.incrementAttrCount();
		assertEquals("3", gen.generateNumber(null, "entity", "attr"));

	}

}
