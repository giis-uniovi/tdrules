/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using Giis.Tdrules.Store.Ids;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Store.Ids
{
	public class TestTableIdentifier
	{
		[Test]
		public virtual void TestSchemaTableIdentifierInstantiation()
		{
			TableIdentifier sti = new TableIdentifier("c", "s", "t", false);
			NUnit.Framework.Assert.AreEqual("c", sti.GetCat());
			NUnit.Framework.Assert.AreEqual("s", sti.GetSch());
			NUnit.Framework.Assert.AreEqual("t", sti.GetTab());
			NUnit.Framework.Assert.AreEqual("cat=c, sch=s, tab=t", sti.ToString());
			sti.SetCat("x");
			sti.SetSch("y");
			sti.SetTab("z");
			NUnit.Framework.Assert.AreEqual("cat=x, sch=y, tab=z", sti.ToString());
		}

		[Test]
		public virtual void TestSchemaTableIdentifierGetQualifiedName()
		{
			NUnit.Framework.Assert.AreEqual("c.s.t", TableIdentifier.GetQualifiedName("c", "s", "t"));
			NUnit.Framework.Assert.AreEqual("s.t", TableIdentifier.GetQualifiedName(string.Empty, "s", "t"));
			NUnit.Framework.Assert.AreEqual("c..t", TableIdentifier.GetQualifiedName("c", string.Empty, "t"));
			NUnit.Framework.Assert.AreEqual("t", TableIdentifier.GetQualifiedName(string.Empty, string.Empty, "t"));
			NUnit.Framework.Assert.AreEqual("t", TableIdentifier.GetQualifiedName(null, null, "t"));
			try
			{
				TableIdentifier.GetQualifiedName("c", "s", null);
				NUnit.Framework.Assert.Fail("Should fail");
			}
			catch (Exception e)
			{
				NUnit.Framework.Assert.AreEqual("SchemaTableIdentifier.getQualifiedName: table name is empty", e.Message);
			}
		}

		[Test]
		public virtual void TestSchemaTableIdentifierFullQualifiedNames()
		{
			TableIdentifier sti = new TableIdentifier("t", false);
			NUnit.Framework.Assert.AreEqual("t", sti.GetFullQualifiedTableName());
			NUnit.Framework.Assert.AreEqual("x.y.t", sti.GetFullQualifiedTableName("x", "y"));
			NUnit.Framework.Assert.AreEqual("t", sti.GetDefaultQualifiedTableName("x", "y"));
			// with separated components
			sti = new TableIdentifier(string.Empty, string.Empty, "t", false);
			NUnit.Framework.Assert.AreEqual("t", sti.GetFullQualifiedTableName());
			NUnit.Framework.Assert.AreEqual("x.y.t", sti.GetFullQualifiedTableName("x", "y"));
			NUnit.Framework.Assert.AreEqual("t", sti.GetDefaultQualifiedTableName("x", "y"));
			// with default catalog and schema not equal to the specified in getters
			sti = new TableIdentifier("a", "b", "t", false);
			NUnit.Framework.Assert.AreEqual("a.b.t", sti.GetFullQualifiedTableName());
			NUnit.Framework.Assert.AreEqual("a.b.t", sti.GetFullQualifiedTableName("x", "y"));
			NUnit.Framework.Assert.AreEqual("a.b.t", sti.GetDefaultQualifiedTableName("x", "y"));
		}

		[Test]
		public virtual void TestSchemaTableIdentifierNullNames()
		{
			TableIdentifier sti = new TableIdentifier(null, null, "t", false);
			NUnit.Framework.Assert.AreEqual("t", sti.GetFullQualifiedTableName());
			NUnit.Framework.Assert.AreEqual("x.y.t", sti.GetFullQualifiedTableName("x", "y"));
			NUnit.Framework.Assert.AreEqual("t", sti.GetDefaultQualifiedTableName("x", "y"));
		}

		[Test]
		public virtual void TestSchemaTableIdentifierQuotes()
		{
			// No quotes
			TableIdentifier sti = new TableIdentifier("c", "s", "t t", true);
			NUnit.Framework.Assert.AreEqual("c.s.\"t t\"", sti.GetFullQualifiedTableName());
			sti = new TableIdentifier("c c", "s s", "t t", true);
			NUnit.Framework.Assert.AreEqual("\"c c\".\"s s\".\"t t\"", sti.GetFullQualifiedTableName());
			// already quoted
			sti = new TableIdentifier("\"c c\"", "\"s s\"", "\"t t\"", true);
			NUnit.Framework.Assert.AreEqual("\"c c\".\"s s\".\"t t\"", sti.GetFullQualifiedTableName());
			// bracket quotes, preserve
			sti = new TableIdentifier("[c c]", "[s s]", "[t t]", true);
			NUnit.Framework.Assert.AreEqual("[c c].[s s].[t t]", sti.GetFullQualifiedTableName());
			// mixed, preserve brackets, put quotes if needed
			sti = new TableIdentifier("[c c]", "s s", "\"t t\"", true);
			NUnit.Framework.Assert.AreEqual("[c c].\"s s\".\"t t\"", sti.GetFullQualifiedTableName());
		}
	}
}
