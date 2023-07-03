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
			NUnit.Framework.Assert.AreEqual(string.Empty, SimpleIdentifier.GetQuote("a b"));
			NUnit.Framework.Assert.AreEqual("\"", SimpleIdentifier.GetQuote("\"a b\""));
			NUnit.Framework.Assert.AreEqual("[", SimpleIdentifier.GetQuote("[a b]"));
			NUnit.Framework.Assert.IsFalse(SimpleIdentifier.IsQuoted("a b"));
			NUnit.Framework.Assert.IsTrue(SimpleIdentifier.IsQuoted("\"a b\""));
			NUnit.Framework.Assert.IsTrue(SimpleIdentifier.IsQuoted("[a b]"));
		}

		[Test]
		public virtual void TestQuoteIfNeeded()
		{
			// dash/space requires quote, underscore does not
			NUnit.Framework.Assert.AreEqual("a_b", SimpleIdentifier.ProcessIdentifier("a_b", true));
			NUnit.Framework.Assert.AreEqual("\"a b\"", SimpleIdentifier.ProcessIdentifier("a b", true));
			NUnit.Framework.Assert.AreEqual("\"a-b\"", SimpleIdentifier.ProcessIdentifier("a-b", true));
			// already quoted, remove if not needed
			NUnit.Framework.Assert.AreEqual("a_b", SimpleIdentifier.ProcessIdentifier("\"a_b\"", true));
			NUnit.Framework.Assert.AreEqual("\"a b\"", SimpleIdentifier.ProcessIdentifier("\"a b\"", true));
			NUnit.Framework.Assert.AreEqual("\"a-b\"", SimpleIdentifier.ProcessIdentifier("\"a-b\"", true));
			// already bracked quoted, preserves quote if needed
			NUnit.Framework.Assert.AreEqual("a_b", SimpleIdentifier.ProcessIdentifier("[a_b]", true));
			NUnit.Framework.Assert.AreEqual("[a b]", SimpleIdentifier.ProcessIdentifier("[a b]", true));
			NUnit.Framework.Assert.AreEqual("[a-b]", SimpleIdentifier.ProcessIdentifier("[a-b]", true));
		}

		[Test]
		public virtual void TestLastItem()
		{
			// dash/space requires quote, underscore does not
			NUnit.Framework.Assert.AreEqual("c", SimpleIdentifier.GetLastComponent("a.b.c"));
			NUnit.Framework.Assert.AreEqual("c", SimpleIdentifier.GetLastComponent("c"));
			NUnit.Framework.Assert.AreEqual("\"c c\"", SimpleIdentifier.GetLastComponent("a.b.\"c c\""));
		}

		[Test]
		public virtual void TestForceQuotation()
		{
			// not quoted
			NUnit.Framework.Assert.AreEqual("\"ab\"", SimpleIdentifier.GetQuotedName("ab", "\""));
			NUnit.Framework.Assert.AreEqual("[ab]", SimpleIdentifier.GetQuotedName("ab", "["));
			// already quoted, preseve quotation
			NUnit.Framework.Assert.AreEqual("\"ab\"", SimpleIdentifier.GetQuotedName("\"ab\"", "\""));
			NUnit.Framework.Assert.AreEqual("[ab]", SimpleIdentifier.GetQuotedName("[ab]", "["));
			// want use different quotation, preserve existing
			NUnit.Framework.Assert.AreEqual("\"ab\"", SimpleIdentifier.GetQuotedName("\"ab\"", "["));
			NUnit.Framework.Assert.AreEqual("[ab]", SimpleIdentifier.GetQuotedName("[ab]", "\""));
		}
	}
}
