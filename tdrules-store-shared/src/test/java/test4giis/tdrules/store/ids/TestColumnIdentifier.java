package test4giis.tdrules.store.ids;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import giis.tdrules.store.ids.ColumnIdentifier;

public class TestColumnIdentifier {

	@Test
	public void testSchemaTableIdentifier() {
		ColumnIdentifier sci = new ColumnIdentifier(null, null, "a", false);
		assertEquals("a", sci.getCol());
		assertNull(sci.getTabId());
		assertFalse(sci.isQualifiedByTable());
		sci = new ColumnIdentifier(null, null, "t.a", false);
		assertEquals("a", sci.getCol());
		assertEquals("t", sci.getTabId().getFullQualifiedTableName());
		assertTrue(sci.isQualifiedByTable());
		sci = new ColumnIdentifier(null, null, "c.s.t.a", false);
		assertEquals("a", sci.getCol());
		assertEquals("c.s.t", sci.getTabId().getFullQualifiedTableName());
		assertTrue(sci.isQualifiedByTable());
	}

	@Test
	public void testSchemaTableIdentifierQuotes() {
		ColumnIdentifier sci = new ColumnIdentifier(null, null, "a a", true);
		assertEquals("\"a a\"", sci.getCol());
		sci = new ColumnIdentifier(null, null, "\"t t\".[a a]", false);
		assertEquals("[a a]", sci.getCol());
		assertEquals("\"t t\"", sci.getTabId().getFullQualifiedTableName());
	}

	@Test
	public void testGetSchemaTableIdentifierNoDefaultSchCat() {
		assertEquals("a", new ColumnIdentifier(null, null, "a", false).getDefaultQualifiedColumnName(null, null));
		assertEquals("t.a", new ColumnIdentifier(null, null, "t.a", false).getDefaultQualifiedColumnName(null, null));
		assertEquals("x.y.t.a", new ColumnIdentifier(null, null, "x.y.t.a", false).getDefaultQualifiedColumnName(null, null));
		// default tab/sch fills missing info
		assertEquals("x.y.t.a", new ColumnIdentifier("c", "s", "x.y.t.a", false).getDefaultQualifiedColumnName(null, null));
		assertEquals("c.s.t.a", new ColumnIdentifier("c", "s", "t.a", false).getDefaultQualifiedColumnName(null, null));
		// not table component, do not show cat/sch
		assertEquals("a", new ColumnIdentifier("c", "s", "a", false).getDefaultQualifiedColumnName(null, null));
	}

	@Test
	public void testSchemaTableIdentifierGetterDefaultSchCat() {
		// default sch/cat specified in getter
		assertEquals("t.a", new ColumnIdentifier(null, null, "t.a", false).getDefaultQualifiedColumnName("c", "s"));
		assertEquals("x.y.t.a", new ColumnIdentifier("x", "y", "t.a", false).getDefaultQualifiedColumnName("c", "s"));
		// remove cat and/or sch when someone matches
		assertEquals("t.a", new ColumnIdentifier("c", "s", "t.a", false).getDefaultQualifiedColumnName("c", "s"));
		assertEquals("s.t.a", new ColumnIdentifier("c", "s", "t.a", false).getDefaultQualifiedColumnName("c", "y"));
		assertEquals("c..t.a", new ColumnIdentifier("c", "s", "t.a", false).getDefaultQualifiedColumnName("x", "s"));
	}

}
