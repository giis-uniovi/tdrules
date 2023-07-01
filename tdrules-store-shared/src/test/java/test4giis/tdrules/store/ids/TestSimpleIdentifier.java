package test4giis.tdrules.store.ids;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import giis.tdrules.store.ids.SimpleIdentifier;

public class TestSimpleIdentifier {

	@Test
	public void testQuotation() {
		assertEquals("", SimpleIdentifier.getQuote("a b"));
		assertEquals("\"", SimpleIdentifier.getQuote("\"a b\""));
		assertEquals("[", SimpleIdentifier.getQuote("[a b]"));
		assertFalse(SimpleIdentifier.isQuoted("a b"));
		assertTrue(SimpleIdentifier.isQuoted("\"a b\""));
		assertTrue(SimpleIdentifier.isQuoted("[a b]"));
	}

	@Test
	public void testQuoteIfNeeded() {
		// dash/space requires quote, underscore does not
		assertEquals("a_b", SimpleIdentifier.processIdentifier("a_b", true));
		assertEquals("\"a b\"", SimpleIdentifier.processIdentifier("a b", true));
		assertEquals("\"a-b\"", SimpleIdentifier.processIdentifier("a-b", true));
		// already quoted, remove if not needed
		assertEquals("a_b", SimpleIdentifier.processIdentifier("\"a_b\"", true));
		assertEquals("\"a b\"", SimpleIdentifier.processIdentifier("\"a b\"", true));
		assertEquals("\"a-b\"", SimpleIdentifier.processIdentifier("\"a-b\"", true));
		// already bracked quoted, preserves quote if needed
		assertEquals("a_b", SimpleIdentifier.processIdentifier("[a_b]", true));
		assertEquals("[a b]", SimpleIdentifier.processIdentifier("[a b]", true));
		assertEquals("[a-b]", SimpleIdentifier.processIdentifier("[a-b]", true));
	}

	@Test
	public void testLastItem() {
		// dash/space requires quote, underscore does not
		assertEquals("c", SimpleIdentifier.getLastComponent("a.b.c"));
		assertEquals("c", SimpleIdentifier.getLastComponent("c"));
		assertEquals("\"c c\"", SimpleIdentifier.getLastComponent("a.b.\"c c\""));
	}

	@Test
	public void testForceQuotation() {
		// not quoted
		assertEquals("\"ab\"", SimpleIdentifier.getQuotedName("ab", "\""));
		assertEquals("[ab]", SimpleIdentifier.getQuotedName("ab", "["));
		// already quoted, preseve quotation
		assertEquals("\"ab\"", SimpleIdentifier.getQuotedName("\"ab\"", "\""));
		assertEquals("[ab]", SimpleIdentifier.getQuotedName("[ab]", "["));
		// want use different quotation, preserve existing
		assertEquals("\"ab\"", SimpleIdentifier.getQuotedName("\"ab\"", "["));
		assertEquals("[ab]", SimpleIdentifier.getQuotedName("[ab]", "\""));
	}

}
