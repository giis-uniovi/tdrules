package test4giis.tdrules.store.ids;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import giis.tdrules.store.ids.TableIdentifier;

public class TestTableIdentifier {

	@Test
	public void testSchemaTableIdentifierInstantiation() {
		TableIdentifier sti = new TableIdentifier("c", "s", "t", false);
		assertEquals("c", sti.getCat());
		assertEquals("s", sti.getSch());
		assertEquals("t", sti.getTab());
		assertEquals("cat=c, sch=s, tab=t", sti.toString());
		sti.setCat("x");
		sti.setSch("y");
		sti.setTab("z");
		assertEquals("cat=x, sch=y, tab=z", sti.toString());
	}

	@Test
	public void testSchemaTableIdentifierGetQualifiedName() {
		assertEquals("c.s.t", TableIdentifier.getQualifiedName("c", "s", "t"));
		assertEquals("s.t", TableIdentifier.getQualifiedName("", "s", "t"));
		assertEquals("c..t", TableIdentifier.getQualifiedName("c", "", "t"));
		assertEquals("t", TableIdentifier.getQualifiedName("", "", "t"));
		assertEquals("t", TableIdentifier.getQualifiedName(null, null, "t"));
		try {
			TableIdentifier.getQualifiedName("c", "s", null);
			fail("Should fail");
		} catch (RuntimeException e) {
			assertEquals("SchemaTableIdentifier.getQualifiedName: table name is empty", e.getMessage());
		}
	}

	@Test
	public void testSchemaTableIdentifierFullQualifiedNames() {
		TableIdentifier sti = new TableIdentifier("t", false);
		assertEquals("t", sti.getFullQualifiedTableName());
		assertEquals("x.y.t", sti.getFullQualifiedTableName("x", "y"));
		assertEquals("t", sti.getDefaultQualifiedTableName("x", "y"));
		// with separated components
		sti = new TableIdentifier("", "", "t", false);
		assertEquals("t", sti.getFullQualifiedTableName());
		assertEquals("x.y.t", sti.getFullQualifiedTableName("x", "y"));
		assertEquals("t", sti.getDefaultQualifiedTableName("x", "y"));
		// with default catalog and schema not equal to the specified in getters
		sti = new TableIdentifier("a", "b", "t", false);
		assertEquals("a.b.t", sti.getFullQualifiedTableName());
		assertEquals("a.b.t", sti.getFullQualifiedTableName("x", "y"));
		assertEquals("a.b.t", sti.getDefaultQualifiedTableName("x", "y"));
	}

	@Test
	public void testSchemaTableIdentifierNullNames() {
		TableIdentifier sti = new TableIdentifier(null, null, "t", false);
		assertEquals("t", sti.getFullQualifiedTableName());
		assertEquals("x.y.t", sti.getFullQualifiedTableName("x", "y"));
		assertEquals("t", sti.getDefaultQualifiedTableName("x", "y"));
	}

	@Test
	public void testSchemaTableIdentifierQuotes() {
		// No quotes
		TableIdentifier sti = new TableIdentifier("c", "s", "t t", true);
		assertEquals("c.s.\"t t\"", sti.getFullQualifiedTableName());
		sti = new TableIdentifier("c c", "s s", "t t", true);
		assertEquals("\"c c\".\"s s\".\"t t\"", sti.getFullQualifiedTableName());
		// already quoted
		sti = new TableIdentifier("\"c c\"", "\"s s\"", "\"t t\"", true);
		assertEquals("\"c c\".\"s s\".\"t t\"", sti.getFullQualifiedTableName());
		// bracket quotes, preserve
		sti = new TableIdentifier("[c c]", "[s s]", "[t t]", true);
		assertEquals("[c c].[s s].[t t]", sti.getFullQualifiedTableName());
		// mixed, preserve brackets, put quotes if needed
		sti = new TableIdentifier("[c c]", "s s", "\"t t\"", true);
		assertEquals("[c c].\"s s\".\"t t\"", sti.getFullQualifiedTableName());
	}

}
