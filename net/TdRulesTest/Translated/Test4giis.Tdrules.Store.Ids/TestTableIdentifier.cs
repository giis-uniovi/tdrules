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
    public class TestTableIdentifier
    {
        [Test]
        public virtual void TestSchemaTableIdentifierInstantiation()
        {
            TableIdentifier sti = new TableIdentifier("c", "s", "t", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("c", sti.GetCat());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("s", sti.GetSch());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sti.GetTab());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("cat=c, sch=s, tab=t", sti.ToString());
            sti.SetCat("x");
            sti.SetSch("y");
            sti.SetTab("z");
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("cat=x, sch=y, tab=z", sti.ToString());
        }

        [Test]
        public virtual void TestSchemaTableIdentifierGetQualifiedName()
        {
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("c.s.t", TableIdentifier.GetQualifiedName("c", "s", "t"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("s.t", TableIdentifier.GetQualifiedName("", "s", "t"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("c..t", TableIdentifier.GetQualifiedName("c", "", "t"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", TableIdentifier.GetQualifiedName("", "", "t"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", TableIdentifier.GetQualifiedName(null, null, "t"));
            try
            {
                TableIdentifier.GetQualifiedName("c", "s", null);
                NUnit.Framework.Legacy.ClassicAssert.Fail("Should fail");
            }
            catch (Exception e)
            {
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("SchemaTableIdentifier.getQualifiedName: table name is empty", e.Message);
            }
        }

        [Test]
        public virtual void TestSchemaTableIdentifierFullQualifiedNames()
        {
            TableIdentifier sti = new TableIdentifier("t", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sti.GetFullQualifiedTableName());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("x.y.t", sti.GetFullQualifiedTableName("x", "y"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sti.GetDefaultQualifiedTableName("x", "y"));

            // with separated components
            sti = new TableIdentifier("", "", "t", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sti.GetFullQualifiedTableName());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("x.y.t", sti.GetFullQualifiedTableName("x", "y"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sti.GetDefaultQualifiedTableName("x", "y"));

            // with default catalog and schema not equal to the specified in getters
            sti = new TableIdentifier("a", "b", "t", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a.b.t", sti.GetFullQualifiedTableName());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a.b.t", sti.GetFullQualifiedTableName("x", "y"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("a.b.t", sti.GetDefaultQualifiedTableName("x", "y"));
        }

        [Test]
        public virtual void TestSchemaTableIdentifierNullNames()
        {
            TableIdentifier sti = new TableIdentifier(null, null, "t", false);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sti.GetFullQualifiedTableName());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("x.y.t", sti.GetFullQualifiedTableName("x", "y"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", sti.GetDefaultQualifiedTableName("x", "y"));
        }

        [Test]
        public virtual void TestSchemaTableIdentifierQuotes()
        {

            // No quotes
            TableIdentifier sti = new TableIdentifier("c", "s", "t t", true);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("c.s.\"t t\"", sti.GetFullQualifiedTableName());
            sti = new TableIdentifier("c c", "s s", "t t", true);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"c c\".\"s s\".\"t t\"", sti.GetFullQualifiedTableName());

            // already quoted
            sti = new TableIdentifier("\"c c\"", "\"s s\"", "\"t t\"", true);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("\"c c\".\"s s\".\"t t\"", sti.GetFullQualifiedTableName());

            // bracket quotes, preserve
            sti = new TableIdentifier("[c c]", "[s s]", "[t t]", true);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[c c].[s s].[t t]", sti.GetFullQualifiedTableName());

            // mixed, preserve brackets, put quotes if needed
            sti = new TableIdentifier("[c c]", "s s", "\"t t\"", true);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[c c].\"s s\".\"t t\"", sti.GetFullQualifiedTableName());
        }
    }
}