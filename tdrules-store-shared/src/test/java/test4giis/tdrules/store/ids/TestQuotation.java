package test4giis.tdrules.store.ids;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import giis.portable.util.JavaCs;
import giis.tdrules.store.ids.Quotation;

public class TestQuotation {

	@Test
	public void testSplitQuoted() {
		// caso normal sin comillas
		String[] stra = Quotation.splitQuoted("esto.es.un", '"', '"', '.');
		assertEquals(3, stra.length);
		assertEquals("esto", stra[0]);
		assertEquals("es", stra[1]);
		assertEquals("un", stra[2]);
		// un texto con comillas, otro con doble comillas y otro con el separador
		stra = Quotation.splitQuoted("\"es  to\".\"no es\"\"un\".\"circo.com\"", '"', '"', '.');
		assertEquals(3, stra.length);
		assertEquals("\"es  to\"", stra[0]);
		assertEquals("\"no es\"\"un\"", stra[1]);
		assertEquals("\"circo.com\"", stra[2]);
	}

	@Test
	public void testSplitQuotedAdjacentSeparators() {
		String[] stra = Quotation.splitQuoted(".es..un..\"test\".", '"', '"', '.');
		assertEquals(7, stra.length);
		assertEquals("", stra[0]);
		assertEquals("es", stra[1]);
		assertEquals("", stra[2]);
		assertEquals("un", stra[3]);
		assertEquals("", stra[4]);
		assertEquals("\"test\"", stra[5]);
		assertEquals("", stra[6]);
	}

	@Test
	public void testSplitQuotedNoDotSeparators() {
		String[] stra = Quotation.splitQuoted("es \"t o\" es", '"', '"', ' ');
		assertEquals(3, stra.length);
		assertEquals("es", stra[0]);
		assertEquals("\"t o\"", stra[1]);
		assertEquals("es", stra[2]);
	}

	@Test
	public void testSplitQuotedWithBrackets() {
		String[] stra = Quotation.splitQuoted("es.[x.y].to", '[', ']', '.');
		assertEquals(3, stra.length);
		assertEquals("es", stra[0]);
		assertEquals("[x.y]", stra[1]);
		assertEquals("to", stra[2]);
	}

	@Test
	public void testSplitQuotedMultipleSeparators() {
		String[] stra = Quotation.splitQuoted("a \t\n\r \"wo rd\"  end", '"', '"',
				new char[] { ' ', '\t', '\n', '\r' });
		assertEquals(8, stra.length);
		assertEquals("a", stra[0]);
		assertEquals("", stra[1]);
		assertEquals("", stra[2]);
		assertEquals("", stra[3]);
		assertEquals("", stra[4]);
		assertEquals("\"wo rd\"", stra[5]);
		assertEquals("", stra[6]);
		assertEquals("end", stra[7]);
	}

	@Test
	public void testSplitQuotedEdgeCases() {
		String[] stra = Quotation.splitQuoted(".", '"', '"', '.');
		assertEquals(2, stra.length);
		assertEquals("", stra[0]);
		assertEquals("", stra[1]);
		stra = Quotation.splitQuoted("", '"', '"', '.');
		assertEquals(1, stra.length);
		assertEquals("", stra[0]);
		// comillas sin cerrar, excepcion
		try {
			stra = Quotation.splitQuoted("esto.\"es", '"', '"', '.');
			fail("Se esperaba excepcion");
		} catch (Throwable e) {
			assertEquals("Str.splitQuoted: Quote not closed in string esto.\"es", e.getMessage());
		}
	}

	@Test
	public void testSplitRight() {
		assertEquals("[a, b, c]", JavaCs.deepToString(Quotation.splitQuotedRight("a.b.c", '"', '"', '.', 3)));
		assertEquals("[, b, c]", JavaCs.deepToString(Quotation.splitQuotedRight("b.c", '"', '"', '.', 3)));
		assertEquals("[, , c]", JavaCs.deepToString(Quotation.splitQuotedRight("c", '"', '"', '.', 3)));
		assertEquals("[a, b, c]", JavaCs.deepToString(Quotation.splitQuotedRight("a. b .c", '"', '"', '.', 3)));
		assertEquals("[a, \"b x\", c]",
				JavaCs.deepToString(Quotation.splitQuotedRight("a.\"b x\".c", '"', '"', '.', 3)));
		assertEquals("[, [b x], c]", JavaCs.deepToString(Quotation.splitQuotedRight("[b x].c", '[', ']', '.', 3)));
	}

	@Test
	public void testSplitRightExceptions() {
		try {
			Quotation.splitQuotedRight("", '"', '"', '.', 3);
			fail("Should fail");
		} catch (RuntimeException e) {
			assertEquals("Quotation.splitQuotedRight: Name is empty", e.getMessage());
		}
		try {
			Quotation.splitQuotedRight("a.b.c", '"', '"', '.', 2);
			fail("Should fail");
		} catch (RuntimeException e) {
			assertEquals("Quotation.splitQuotedRight: Name has more than 2 componentes: a.b.c", e.getMessage());
		}
	}

	@Test
	public void testSplitWordsWithoutQuotes() {
		// comprime blancos
		String[] str = Quotation.splitQuotedWords("  esto   es   una   ", '"', '"');
		assertEquals("esto", str[0]);
		assertEquals("es", str[1]);
		assertEquals("una", str[2]);
		// no se comprimen blancos en los strings entre comillas (tras build 56)
		str = Quotation.splitQuotedWords("\"es   to\"   es   \"  u  na  \"   ", '"', '"');
		assertEquals("\"es   to\"", str[0]);
		assertEquals("es", str[1]);
		assertEquals("\"  u  na  \"", str[2]);
		// si queda comilla sin cerrar excepcion (tras build 56)
		try {
			str = Quotation.splitQuotedWords("\"es   to\"   \"es     ", '"', '"');
			fail("Se esperaba excepcion");
		} catch (Throwable e) {
			assertEquals("Str.splitQuoted: Quote not closed in string \"es   to\"   \"es     ", e.getMessage());
		}
	}

	@Test
	public void testSplitWordsWithQuotes() {
		// isQuoted require string rodeado de los caracteres indicados
		assertTrue(Quotation.isQuoted("\"esto\"", '"', '"'));
		assertEquals("esto", Quotation.removeQuotes("\"esto\"", '"', '"'));
		assertFalse(Quotation.isQuoted("esto\"", '"', '"'));
		assertEquals("esto", Quotation.removeQuotes("esto\"", '"', '"'));
		assertFalse(Quotation.isQuoted("\"esto", '"', '"'));
		assertEquals("esto", Quotation.removeQuotes("\"esto", '"', '"'));
		// limites inferiores
		assertTrue(Quotation.isQuoted("\"\"", '"', '"'));
		assertEquals("", Quotation.removeQuotes("\"\"", '"', '"'));
		assertFalse(Quotation.isQuoted("\"", '"', '"'));
		assertEquals("", Quotation.removeQuotes("\"", '"', '"'));
		assertFalse(Quotation.isQuoted("", '"', '"'));
		assertEquals("", Quotation.removeQuotes("", '"', '"'));
	}

}
