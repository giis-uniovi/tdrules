/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Tdrules.Store.Ids;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Store.Ids
{
	public class TestSimpleIdentifier
	{
		[Test]
		public virtual void TestQuotation()
		{
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, SimpleIdentifier.GetQuote("a b"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"", SimpleIdentifier.GetQuote("\"a b\""));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("[", SimpleIdentifier.GetQuote("[a b]"));
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(SimpleIdentifier.IsQuoted("a b"));
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(SimpleIdentifier.IsQuoted("\"a b\""));
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(SimpleIdentifier.IsQuoted("[a b]"));
		}

		[Test]
		public virtual void TestQuoteIfNeeded()
		{
			// dash/space requires quote, underscore does not
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("a_b", SimpleIdentifier.ProcessIdentifier("a_b", true));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"a b\"", SimpleIdentifier.ProcessIdentifier("a b", true));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"a-b\"", SimpleIdentifier.ProcessIdentifier("a-b", true));
			// already quoted, remove if not needed
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("a_b", SimpleIdentifier.ProcessIdentifier("\"a_b\"", true));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"a b\"", SimpleIdentifier.ProcessIdentifier("\"a b\"", true));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"a-b\"", SimpleIdentifier.ProcessIdentifier("\"a-b\"", true));
			// already bracked quoted, preserves quote if needed
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("a_b", SimpleIdentifier.ProcessIdentifier("[a_b]", true));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("[a b]", SimpleIdentifier.ProcessIdentifier("[a b]", true));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("[a-b]", SimpleIdentifier.ProcessIdentifier("[a-b]", true));
		}

		[Test]
		public virtual void TestLastItem()
		{
			// dash/space requires quote, underscore does not
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("c", SimpleIdentifier.GetLastComponent("a.b.c"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("c", SimpleIdentifier.GetLastComponent("c"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"c c\"", SimpleIdentifier.GetLastComponent("a.b.\"c c\""));
		}

		[Test]
		public virtual void TestForceQuotation()
		{
			// not quoted
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"ab\"", SimpleIdentifier.GetQuotedName("ab", "\""));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("[ab]", SimpleIdentifier.GetQuotedName("ab", "["));
			// already quoted, preseve quotation
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"ab\"", SimpleIdentifier.GetQuotedName("\"ab\"", "\""));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("[ab]", SimpleIdentifier.GetQuotedName("[ab]", "["));
			// want use different quotation, preserve existing
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"ab\"", SimpleIdentifier.GetQuotedName("\"ab\"", "["));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("[ab]", SimpleIdentifier.GetQuotedName("[ab]", "\""));
		}
	}
}
