/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using Giis.Portable.Util;
using Giis.Tdrules.Store.Ids;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Store.Ids
{
	public class TestQuotation
	{
		[Test]
		public virtual void TestSplitQuoted()
		{
			// caso normal sin comillas
			string[] stra = Quotation.SplitQuoted("esto.es.un", '"', '"', '.');
			NUnit.Framework.Assert.AreEqual(3, stra.Length);
			NUnit.Framework.Assert.AreEqual("esto", stra[0]);
			NUnit.Framework.Assert.AreEqual("es", stra[1]);
			NUnit.Framework.Assert.AreEqual("un", stra[2]);
			// un texto con comillas, otro con doble comillas y otro con el separador
			stra = Quotation.SplitQuoted("\"es  to\".\"no es\"\"un\".\"circo.com\"", '"', '"', '.');
			NUnit.Framework.Assert.AreEqual(3, stra.Length);
			NUnit.Framework.Assert.AreEqual("\"es  to\"", stra[0]);
			NUnit.Framework.Assert.AreEqual("\"no es\"\"un\"", stra[1]);
			NUnit.Framework.Assert.AreEqual("\"circo.com\"", stra[2]);
		}

		[Test]
		public virtual void TestSplitQuotedAdjacentSeparators()
		{
			string[] stra = Quotation.SplitQuoted(".es..un..\"test\".", '"', '"', '.');
			NUnit.Framework.Assert.AreEqual(7, stra.Length);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[0]);
			NUnit.Framework.Assert.AreEqual("es", stra[1]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[2]);
			NUnit.Framework.Assert.AreEqual("un", stra[3]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[4]);
			NUnit.Framework.Assert.AreEqual("\"test\"", stra[5]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[6]);
		}

		[Test]
		public virtual void TestSplitQuotedNoDotSeparators()
		{
			string[] stra = Quotation.SplitQuoted("es \"t o\" es", '"', '"', ' ');
			NUnit.Framework.Assert.AreEqual(3, stra.Length);
			NUnit.Framework.Assert.AreEqual("es", stra[0]);
			NUnit.Framework.Assert.AreEqual("\"t o\"", stra[1]);
			NUnit.Framework.Assert.AreEqual("es", stra[2]);
		}

		[Test]
		public virtual void TestSplitQuotedWithBrackets()
		{
			string[] stra = Quotation.SplitQuoted("es.[x.y].to", '[', ']', '.');
			NUnit.Framework.Assert.AreEqual(3, stra.Length);
			NUnit.Framework.Assert.AreEqual("es", stra[0]);
			NUnit.Framework.Assert.AreEqual("[x.y]", stra[1]);
			NUnit.Framework.Assert.AreEqual("to", stra[2]);
		}

		[Test]
		public virtual void TestSplitQuotedMultipleSeparators()
		{
			string[] stra = Quotation.SplitQuoted("a \t\n\r \"wo rd\"  end", '"', '"', new char[] { ' ', '\t', '\n', '\r' });
			NUnit.Framework.Assert.AreEqual(8, stra.Length);
			NUnit.Framework.Assert.AreEqual("a", stra[0]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[1]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[2]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[3]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[4]);
			NUnit.Framework.Assert.AreEqual("\"wo rd\"", stra[5]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[6]);
			NUnit.Framework.Assert.AreEqual("end", stra[7]);
		}

		[Test]
		public virtual void TestSplitQuotedEdgeCases()
		{
			string[] stra = Quotation.SplitQuoted(".", '"', '"', '.');
			NUnit.Framework.Assert.AreEqual(2, stra.Length);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[0]);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[1]);
			stra = Quotation.SplitQuoted(string.Empty, '"', '"', '.');
			NUnit.Framework.Assert.AreEqual(1, stra.Length);
			NUnit.Framework.Assert.AreEqual(string.Empty, stra[0]);
			// comillas sin cerrar, excepcion
			try
			{
				stra = Quotation.SplitQuoted("esto.\"es", '"', '"', '.');
				NUnit.Framework.Assert.Fail("Se esperaba excepcion");
			}
			catch (Exception e)
			{
				NUnit.Framework.Assert.AreEqual("Str.splitQuoted: Quote not closed in string esto.\"es", e.Message);
			}
		}

		[Test]
		public virtual void TestSplitRight()
		{
			NUnit.Framework.Assert.AreEqual("[a, b, c]", JavaCs.DeepToString(Quotation.SplitQuotedRight("a.b.c", '"', '"', '.', 3)));
			NUnit.Framework.Assert.AreEqual("[, b, c]", JavaCs.DeepToString(Quotation.SplitQuotedRight("b.c", '"', '"', '.', 3)));
			NUnit.Framework.Assert.AreEqual("[, , c]", JavaCs.DeepToString(Quotation.SplitQuotedRight("c", '"', '"', '.', 3)));
			NUnit.Framework.Assert.AreEqual("[a, b, c]", JavaCs.DeepToString(Quotation.SplitQuotedRight("a. b .c", '"', '"', '.', 3)));
			NUnit.Framework.Assert.AreEqual("[a, \"b x\", c]", JavaCs.DeepToString(Quotation.SplitQuotedRight("a.\"b x\".c", '"', '"', '.', 3)));
			NUnit.Framework.Assert.AreEqual("[, [b x], c]", JavaCs.DeepToString(Quotation.SplitQuotedRight("[b x].c", '[', ']', '.', 3)));
		}

		[Test]
		public virtual void TestSplitRightExceptions()
		{
			try
			{
				Quotation.SplitQuotedRight(string.Empty, '"', '"', '.', 3);
				NUnit.Framework.Assert.Fail("Should fail");
			}
			catch (Exception e)
			{
				NUnit.Framework.Assert.AreEqual("Quotation.splitQuotedRight: Name is empty", e.Message);
			}
			try
			{
				Quotation.SplitQuotedRight("a.b.c", '"', '"', '.', 2);
				NUnit.Framework.Assert.Fail("Should fail");
			}
			catch (Exception e)
			{
				NUnit.Framework.Assert.AreEqual("Quotation.splitQuotedRight: Name has more than 2 componentes: a.b.c", e.Message);
			}
		}

		[Test]
		public virtual void TestSplitWordsWithoutQuotes()
		{
			// comprime blancos
			string[] str = Quotation.SplitQuotedWords("  esto   es   una   ", '"', '"');
			NUnit.Framework.Assert.AreEqual("esto", str[0]);
			NUnit.Framework.Assert.AreEqual("es", str[1]);
			NUnit.Framework.Assert.AreEqual("una", str[2]);
			// no se comprimen blancos en los strings entre comillas (tras build 56)
			str = Quotation.SplitQuotedWords("\"es   to\"   es   \"  u  na  \"   ", '"', '"');
			NUnit.Framework.Assert.AreEqual("\"es   to\"", str[0]);
			NUnit.Framework.Assert.AreEqual("es", str[1]);
			NUnit.Framework.Assert.AreEqual("\"  u  na  \"", str[2]);
			// si queda comilla sin cerrar excepcion (tras build 56)
			try
			{
				str = Quotation.SplitQuotedWords("\"es   to\"   \"es     ", '"', '"');
				NUnit.Framework.Assert.Fail("Se esperaba excepcion");
			}
			catch (Exception e)
			{
				NUnit.Framework.Assert.AreEqual("Str.splitQuoted: Quote not closed in string \"es   to\"   \"es     ", e.Message);
			}
		}

		[Test]
		public virtual void TestSplitWordsWithQuotes()
		{
			// isQuoted require string rodeado de los caracteres indicados
			NUnit.Framework.Assert.IsTrue(Quotation.IsQuoted("\"esto\"", '"', '"'));
			NUnit.Framework.Assert.AreEqual("esto", Quotation.RemoveQuotes("\"esto\"", '"', '"'));
			NUnit.Framework.Assert.IsFalse(Quotation.IsQuoted("esto\"", '"', '"'));
			NUnit.Framework.Assert.AreEqual("esto", Quotation.RemoveQuotes("esto\"", '"', '"'));
			NUnit.Framework.Assert.IsFalse(Quotation.IsQuoted("\"esto", '"', '"'));
			NUnit.Framework.Assert.AreEqual("esto", Quotation.RemoveQuotes("\"esto", '"', '"'));
			// limites inferiores
			NUnit.Framework.Assert.IsTrue(Quotation.IsQuoted("\"\"", '"', '"'));
			NUnit.Framework.Assert.AreEqual(string.Empty, Quotation.RemoveQuotes("\"\"", '"', '"'));
			NUnit.Framework.Assert.IsFalse(Quotation.IsQuoted("\"", '"', '"'));
			NUnit.Framework.Assert.AreEqual(string.Empty, Quotation.RemoveQuotes("\"", '"', '"'));
			NUnit.Framework.Assert.IsFalse(Quotation.IsQuoted(string.Empty, '"', '"'));
			NUnit.Framework.Assert.AreEqual(string.Empty, Quotation.RemoveQuotes(string.Empty, '"', '"'));
		}
	}
}
