/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using Giis.Tdrules.Store.Ids;
using Giis.Tdrules.Store.Rdb;
using Java.Sql;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Store.Rdb
{
	/// <summary>Management of multiple schemas and identifier resolution.</summary>
	/// <remarks>
	/// Management of multiple schemas and identifier resolution.
	/// A number of tests are only implemented in oracle because of
	/// its particularities when an user access multiple schemas.
	/// </remarks>
	public class TestSqlserverSchemaMulti : Base
	{
		protected internal string catalog = null;

		protected internal string schema = null;

		protected internal string enableCheck = string.Empty;

		// algunas dbms como oracle requieren que se especifique enabled en la creacion
		/// <exception cref="Java.Sql.SQLException"/>
		[NUnit.Framework.SetUp]
		public override void SetUp()
		{
			base.SetUp();
		}

		/// <summary>Obtencion de nombres cualificados a partir de sus componentes</summary>
		[Test]
		public virtual void TestGetQualifiedNames()
		{
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("c.s.t", TableIdentifier.GetQualifiedName("c", "s", "t"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("s.t", TableIdentifier.GetQualifiedName(string.Empty, "s", "t"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("s.t", TableIdentifier.GetQualifiedName(null, "s", "t"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("c..t", TableIdentifier.GetQualifiedName("c", string.Empty, "t"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("c..t", TableIdentifier.GetQualifiedName("c", null, "t"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", TableIdentifier.GetQualifiedName(string.Empty, string.Empty, "t"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("t", TableIdentifier.GetQualifiedName(null, null, "t"));
			try
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("c.s.", TableIdentifier.GetQualifiedName("c", "s", string.Empty));
				NUnit.Framework.Legacy.ClassicAssert.Fail("Se esperaba excepcion");
			}
			catch (Exception e)
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("SchemaTableIdentifier.getQualifiedName: table name is empty", e.Message);
			}
			try
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("c.s.", TableIdentifier.GetQualifiedName("c", "s", null));
				NUnit.Framework.Legacy.ClassicAssert.Fail("Se esperaba excepcion");
			}
			catch (Exception e)
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("SchemaTableIdentifier.getQualifiedName: table name is empty", e.Message);
			}
		}

		/// <summary>
		/// Obtencion de identificadores completos y por defecto teniendo en cuenta una
		/// especificacion dada de catalogo y esquema.
		/// </summary>
		/// <remarks>
		/// Obtencion de identificadores completos y por defecto teniendo en cuenta una
		/// especificacion dada de catalogo y esquema. Identificadores incluidos con
		/// diferente cantidad de componentes
		/// </remarks>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestSchemaIdentifiers()
		{
			Connection db = GetConnection(TestDbname2);
			// solo para dar contexto a la instancia de QualifiedTableName
			// tabla sola, sin cualificar, conbinaciones en especificacion de
			// catalogo/esquema por defecto
			AssertQ(db, "c.s.n", "n", "c", "s", "n");
			AssertQ(db, "s.n", "n", string.Empty, "s", "n");
			AssertQ(db, "c..n", "n", "c", string.Empty, "n");
			AssertQ(db, "n", "n", string.Empty, string.Empty, "n");
			// tabla qualificada:
			// Catalogo y esquema por defecto se especifican,
			AssertQ(db, "a.b.n", "a.b.n", "c", "s", "a.b.n");
			AssertQ(db, "c.b.n", "b.n", "c", "s", "b.n");
			AssertQ(db, "a.s.n", "a..n", "c", "s", "a..n");
			// Catalogo por defecto no se especifican y esquema si
			AssertQ(db, "a.b.n", "a.b.n", string.Empty, "s", "a.b.n");
			AssertQ(db, "b.n", "b.n", string.Empty, "s", "b.n");
			AssertQ(db, "a.s.n", "a..n", string.Empty, "s", "a..n");
			// Catalogo por defecto se especifica y esquema no
			AssertQ(db, "a.b.n", "a.b.n", "c", string.Empty, "a.b.n");
			AssertQ(db, "c.b.n", "b.n", "c", string.Empty, "b.n");
			AssertQ(db, "a..n", "a..n", "c", string.Empty, "a..n");
			// otros tests con cero o mas componentes (error)
			try
			{
				new TableIdentifier("c", "s", string.Empty, false);
			}
			catch (Exception e)
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("Quotation.splitQuotedRight: Name is empty", e.Message);
			}
			try
			{
				new TableIdentifier("c", "s", "a.b.c.d", false);
			}
			catch (Exception e)
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("Quotation.splitQuotedRight: Name has more than 3 componentes: a.b.c.d", e.Message);
			}
			db.Close();
		}

		// asserts correspondientes a obtener el nombre de una tabla full/default
		public virtual void AssertQ(Connection db, string expectedFull, string expectedDefault, string defaultCat, string defaultSch, string name)
		{
			// SchemaTableIdentifier
			TableIdentifier si = new TableIdentifier(defaultCat, defaultSch, name, false);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(expectedFull, si.GetFullQualifiedTableName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(expectedDefault, si.GetDefaultQualifiedTableName(defaultCat, defaultSch));
			// QualifiedTableName, debe tener en cuenta que los identificadores en oracle
			// son mayusculas y en sqlserver minusculas
			SchemaReaderJdbc mr = new SchemaReaderJdbc(db, defaultCat, defaultSch);
			// idem
			SchemaReaderJdbc.QualifiedTableName qt = mr.GetNewQualifiedTableName(defaultCat, defaultSch, name);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored(expectedFull), qt.GetFullQualifiedTableName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored(expectedDefault), qt.GetDefaultQualifiedTableName(defaultCat, defaultSch));
		}
	}
}
