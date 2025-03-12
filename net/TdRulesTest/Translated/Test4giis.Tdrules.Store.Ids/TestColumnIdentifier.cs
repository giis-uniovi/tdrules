using NUnit.Framework;
using Giis.Tdrules.Store.Ids;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Ids
{
    public class TestColumnIdentifier
    {
        [Test]
        public virtual void TestSchemaTableIdentifier()
        {
            ColumnIdentifier sci = new ColumnIdentifier(null, null, "a", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a", sci.GetCol());
            NUnit.Framework.Legacy.ClassicAssert.IsNull(sci.GetTabId());
            NUnit.Framework.Legacy.ClassicAssert.IsFalse(sci.IsQualifiedByTable());
            sci = new ColumnIdentifier(null, null, "t.a", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a", sci.GetCol());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sci.GetTabId().GetFullQualifiedTableName());
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(sci.IsQualifiedByTable());
            sci = new ColumnIdentifier(null, null, "c.s.t.a", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a", sci.GetCol());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("c.s.t", sci.GetTabId().GetFullQualifiedTableName());
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(sci.IsQualifiedByTable());
        }

        [Test]
        public virtual void TestSchemaTableIdentifierQuotes()
        {
            ColumnIdentifier sci = new ColumnIdentifier(null, null, "a a", true);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"a a\"", sci.GetCol());
            sci = new ColumnIdentifier(null, null, "\"t t\".[a a]", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[a a]", sci.GetCol());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"t t\"", sci.GetTabId().GetFullQualifiedTableName());
        }

        [Test]
        public virtual void TestGetSchemaTableIdentifierNoDefaultSchCat()
        {
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a", new ColumnIdentifier(null, null, "a", false).GetDefaultQualifiedColumnName(null, null));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t.a", new ColumnIdentifier(null, null, "t.a", false).GetDefaultQualifiedColumnName(null, null));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("x.y.t.a", new ColumnIdentifier(null, null, "x.y.t.a", false).GetDefaultQualifiedColumnName(null, null));

            // default tab/sch fills missing info
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("x.y.t.a", new ColumnIdentifier("c", "s", "x.y.t.a", false).GetDefaultQualifiedColumnName(null, null));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("c.s.t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName(null, null));

            // not table component, do not show cat/sch
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a", new ColumnIdentifier("c", "s", "a", false).GetDefaultQualifiedColumnName(null, null));
        }

        [Test]
        public virtual void TestSchemaTableIdentifierGetterDefaultSchCat()
        {

            // default sch/cat specified in getter
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t.a", new ColumnIdentifier(null, null, "t.a", false).GetDefaultQualifiedColumnName("c", "s"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("x.y.t.a", new ColumnIdentifier("x", "y", "t.a", false).GetDefaultQualifiedColumnName("c", "s"));

            // remove cat and/or sch when someone matches
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName("c", "s"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("s.t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName("c", "y"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("c..t.a", new ColumnIdentifier("c", "s", "t.a", false).GetDefaultQualifiedColumnName("x", "s"));
        }
    }
}