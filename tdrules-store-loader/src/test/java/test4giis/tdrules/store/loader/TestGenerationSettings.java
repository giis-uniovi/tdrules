package test4giis.tdrules.store.loader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.store.loader.IAttrGen;
import giis.tdrules.store.loader.gen.DeterministicAttrGen;

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

}
