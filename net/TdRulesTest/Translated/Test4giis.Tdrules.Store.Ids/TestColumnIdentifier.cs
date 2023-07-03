/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Tdrules.Store.Ids;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Store.Ids
{
	public class TestColumnIdentifier
	{
		[Test]
		public virtual void TestSchemaTableIdentifier()
		{
			ColumnIdentifier sci = new ColumnIdentifier(null, null, "a", false);
			NUnit.Framework.Assert.AreEqual("a", sci.GetCol());
			NUnit.Framework.Assert.IsNull(sci.GetTabId());
			NUnit.Framework.Assert.IsFalse(sci.IsQualifiedByTable());
			sci = new ColumnIdentifier(null, null, "t.a", false);
			NUnit.Framework.Assert.AreEqual("a", sci.GetCol());
			NUnit.Framework.Assert.AreEqual("t", sci.GetTabId().GetFullQualifiedTableName());
			NUnit.Framework.Assert.IsTrue(sci.IsQualifiedByTable());
			sci = new ColumnIdentifier(null, null, "c.s.t.a", false);
			NUnit.Framework.Assert.AreEqual("a", sci.GetCol());
			NUnit.Framework.Assert.AreEqual("c.s.t", sci.GetTabId().GetFullQualifiedTableName());
			NUnit.Framework.Assert.IsTrue(sci.IsQualifiedByTable());
		}

		[Test]
		public virtual void TestSchemaTableIdentifierQuotes()
		{
			ColumnIdentifier sci = new ColumnIdentifier(null, null, "a a", true);
			NUnit.Framework.Assert.AreEqual("\"a a\"", sci.GetCol());
			sci = new ColumnIdentifier(null, null, "\"t t\".[a a]", false);
			NUnit.Framework.Assert.AreEqual("[a a]", sci.GetCol());
			NUnit.Framework.Assert.AreEqual("\"t t\"", sci.GetTabId().GetFullQualifiedTableName());
		}

		[Test]
		public virtual void TestGetSchemaTableIdentifierNoDefaultSchCat()
		{
			NUnit.Framework.Assert.AreEqual("a", new ColumnIdentifier(null, null, "a", false).GetDefaultQualifiedColumnName(null, null));
			NUnit.Framework.Assert.AreEqual("t.a", new ColumnIdentifier(null, null, "t.a", false).GetDefaultQualifiedColumnName(null, null));
			NUnit.Framework.Assert.AreEqual("x.y.t.a", new ColumnIdentifier(null, null, "x.y.t.a", false).GetDefaultQualifiedColumnName(null, null));
			// default tab/sch fills missing info
			NUnit.Framework.Assert.AreEqual("x.y.t.a", new ColumnIdentifier("c", "s", "x.y.t.a", false).GetDefaultQualifiedColumnName(null, null));
			NUnit.Framework.Assert.AreEqual("c.s.t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName(null, null));
			// not table component, do not show cat/sch
			NUnit.Framework.Assert.AreEqual("a", new ColumnIdentifier("c", "s", "a", false).GetDefaultQualifiedColumnName(null, null));
		}

		[Test]
		public virtual void TestSchemaTableIdentifierGetterDefaultSchCat()
		{
			// default sch/cat specified in getter
			NUnit.Framework.Assert.AreEqual("t.a", new ColumnIdentifier(null, null, "t.a", false).GetDefaultQualifiedColumnName("c", "s"));
			NUnit.Framework.Assert.AreEqual("x.y.t.a", new ColumnIdentifier("x", "y", "t.a", false).GetDefaultQualifiedColumnName("c", "s"));
			// remove cat and/or sch when someone matches
			NUnit.Framework.Assert.AreEqual("t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName("c", "s"));
			NUnit.Framework.Assert.AreEqual("s.t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName("c", "y"));
			NUnit.Framework.Assert.AreEqual("c..t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName("x", "s"));
		}
	}
}
