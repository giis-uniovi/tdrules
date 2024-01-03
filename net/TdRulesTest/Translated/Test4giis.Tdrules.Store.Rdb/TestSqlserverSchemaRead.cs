/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using System.Collections.Generic;
using Giis.Tdrules.Store.Rdb;
using Java.Sql;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Store.Rdb
{
	/// <summary>Reading advanced metadata (FKs, quoted identifiers...)</summary>
	public class TestSqlserverSchemaRead : Base
	{
		protected internal string catalog = null;

		protected internal string schema = null;

		protected internal Connection dbt;

		protected internal string myCatalogSchema2 = TestDbname2.ToLower() + ".dbo.";

		protected internal string sTab1 = "create table " + tablePrefix + "stab1 (col11 int not null primary key, " + "col12 char(3) not null, col13 varchar(16))";

		protected internal string sTab2 = "create table " + tablePrefix + "stab2 (col21 decimal(8,4), col22 int default (22), " + "col23 bit, " + "PRIMARY KEY (col21,col22), " + "CONSTRAINT ref_stab1_col11 FOREIGN KEY(col22) REFERENCES stab1(col11) " + ")";

		// Nombres de catalogo utilizados para el acceso a los metadatos
		// en sqlserver no se utilizan ya que se accede a las tablas de una base de datos
		// prefijo de catalogo y esquema para tablas completamente cualificadas
		// creacion de tablas para pruebas
		// tabla con dos fks, una a una clave y otra a una no clave
		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual void CreateTablesAndViews()
		{
			Connection dbt = GetConnection(TestDbname2);
			// modo conectado para que se cierre la conexion
			Execute(dbt, sTab1);
			Execute(dbt, sTab2);
			dbt.Close();
		}

		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual void DropTablesAndViews()
		{
			Connection dbt = GetConnection(TestDbname2);
			// modo conectado para que se cierre la conexion
			ExecuteNotThrow(dbt, "drop table stab33");
			ExecuteNotThrow(dbt, "drop table stab2");
			ExecuteNotThrow(dbt, "drop table stab1");
			dbt.Close();
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[NUnit.Framework.SetUp]
		public override void SetUp()
		{
			base.SetUp();
			// Not all tests need call createTablesAndViews, but drop before to ensure a cleaner setup
			this.DropTablesAndViews();
			dbt = GetConnection(TestDbname2);
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[NUnit.Framework.TearDown]
		public virtual void TearDown()
		{
			dbt.Close();
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReadDbmsMetadata()
		{
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			// tipo de base de datos
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(dbmsname, mr.GetDbmsType().ToString());
			// identificacion de la plataforma
			string propPrefix = "tdrules." + Platform + "." + TestDbname2 + "." + dbmsname;
			string metaDbms = new JdbcProperties().GetProp(DatabaseProperties, propPrefix + ".meta.dbms");
			string metaDriver = new JdbcProperties().GetProp(DatabaseProperties, propPrefix + ".meta.driver");
			AssertContains(metaDbms, mr.GetPlatformInfo());
			AssertContains(metaDriver, mr.GetPlatformInfo());
			// otros datos de DatabaseMetaData que estan en las versiones para .NET
			DatabaseMetaData md = dbt.GetMetaData();
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(dbmsproductname, md.GetDatabaseProductName());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(mr.GetDbmsType().ToString().Equals("postgres") ? !md.StoresLowerCaseIdentifiers() : md.StoresLowerCaseIdentifiers());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(md.StoresLowerCaseQuotedIdentifiers());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(mr.GetDbmsType().ToString().Equals("oracle") ? !md.StoresUpperCaseIdentifiers() : md.StoresUpperCaseIdentifiers());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(md.StoresUpperCaseQuotedIdentifiers());
		}

		[Test]
		public virtual void TestReaderTableDoesNotExist()
		{
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			try
			{
				mr.ReadTable("tablenotexists");
				NUnit.Framework.Legacy.ClassicAssert.Fail("Se deberia haber producido una excepcion");
			}
			catch (SchemaException e)
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("schemareaderjdbc.settabletype: can't find table or view: tablenotexists", e.Message.ToLower());
			}
		}

		/// <summary>Comportamiento con identificadores mayusculas y minusculas</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestCaseIdentifiers()
		{
			ExecuteNotThrow(dbt, "drop table " + tablePrefix + "STab33");
			Execute(dbt, "create table " + tablePrefix + "STab33 (coL33 int) ");
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			// leo indiferentemente en mayusculas o minusculas
			mr.ReadTable("sTab33");
			mr.ReadTable("StaB33");
			mr.ReadTable("stab33");
			mr.ReadTable("STAB33");
			// leo el nombre de la tabla y la columna
			mr.ReadTable("STab33");
			AssertEqualsDBObj("STab33", mr.GetTableName());
			AssertEqualsDBObj("coL33", mr.GetColumn(0).GetColName());
			// y esta no existe
			try
			{
				mr.ReadTable("STabl33");
				NUnit.Framework.Legacy.ClassicAssert.Fail("se deberia haber producido una excepcion");
			}
			catch (Exception e)
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("SchemaReaderJdbc.setTableType: Can't find table or view: STabl33".ToLower(), e.Message.ToLower());
			}
		}

		/// <summary>Comportamiento con identificadores con comillas</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestQuotedIdentifiers()
		{
			ExecuteNotThrow(dbt, "drop table " + tablePrefix + "\"Tab Cuatro\"");
			Execute(dbt, "create table " + tablePrefix + "\"Tab Cuatro\" (coL44 int, \"Columna 55\" char(1)) ");
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			// leo indiferentemente en mayusculas o minusculas
			// En Oracle y postgres los quoted strings son case sensitives y por tanto solo
			// puedo localizar el primero
			mr.ReadTable("\"Tab Cuatro\"");
			if (mr.GetDbmsType().IsOracle() || mr.GetDbmsType().IsPostgres())
			{
				try
				{
					mr.ReadTable("\"tab cuatro\"");
					NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
				}
				catch (Exception e)
				{
					AssertContains("Can't find table or view", e.Message);
				}
				try
				{
					mr.ReadTable("\"taB cuatrO\"");
					NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
				}
				catch (Exception e)
				{
					AssertContains("Can't find table or view", e.Message);
				}
				try
				{
					mr.ReadTable("\"TAB CUATRO\"");
					NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
				}
				catch (Exception e)
				{
					AssertContains("Can't find table or view", e.Message);
				}
			}
			else
			{
				mr.ReadTable("\"tab cuatro\"");
				mr.ReadTable("\"taB cuatrO\"");
				mr.ReadTable("\"TAB CUATRO\"");
			}
			// tambien puedo leer si no hay comillas
			// Si hay espacios en blanco se considera quoted string tambien, por lo que el
			// funcionamiento sera el mismo que antes
			mr.ReadTable("Tab Cuatro");
			if (mr.GetDbmsType().IsOracle() || mr.GetDbmsType().IsPostgres())
			{
				try
				{
					mr.ReadTable("tab cuatro");
					NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
				}
				catch (Exception e)
				{
					AssertContains("Can't find table or view", e.Message);
				}
				try
				{
					mr.ReadTable("taB cuatrO");
					NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
				}
				catch (Exception e)
				{
					AssertContains("Can't find table or view", e.Message);
				}
				try
				{
					mr.ReadTable("TAB CUATRO");
					NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
				}
				catch (Exception e)
				{
					AssertContains("Can't find table or view", e.Message);
				}
			}
			else
			{
				mr.ReadTable("tab cuatro");
				mr.ReadTable("taB cuatrO");
				mr.ReadTable("TAB CUATRO");
			}
			// leo el nombre de la tabla y las columnas (como cualquier identificador, sin comillas)
			mr.ReadTable("\"Tab Cuatro\"");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("Tab Cuatro", mr.GetTableName());
			AssertEqualsDBObj("coL44", mr.GetColumn(0).GetColName());
			// esta se pasa a mayusculas en oracle
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("Columna 55", mr.GetColumn(1).GetColName());
			// esta no porque era quoted
			// y esta no existe
			try
			{
				mr.ReadTable("\"Tabla Cuatro\"");
				NUnit.Framework.Legacy.ClassicAssert.Fail("se deberia haber producido una excepcion");
			}
			catch (Exception e)
			{
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("SchemaReaderJdbc.setTableType: Can't find table or view: \"Tabla Cuatro\"".ToLower(), e.Message.ToLower());
			}
		}

		/// <summary>Comportamiento con identificadores entre corchetes (sqlserver)</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestQuotedIdentifiersWithBracket()
		{
			ExecuteNotThrow(dbt, "drop table " + tablePrefix + "[Tab Cuatrob]");
			Execute(dbt, "create table " + tablePrefix + "[Tab Cuatrob] (coL44 int, [Columna 55] char(1)) ");
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			// leo indiferentemente en mayusculas o minusculas
			mr.ReadTable("[tab cuatrob]");
			// tambien puedo leer si no hay comillas
			mr.ReadTable("tab cuatrob");
			// leo el nombre de la tabla y las columnas (como cualquier identificador, sin
			// comillas)
			mr.ReadTable("[Tab Cuatrob]");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("Tab Cuatrob", mr.GetTableName());
			AssertEqualsDBObj("coL44", mr.GetColumn(0).GetColName());
			// esta se pasa a mayusculas en oracle
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("Columna 55", mr.GetColumn(1).GetColName());
		}

		// esta no porque era quoted
		/// <summary>Lectura de metadatos basicos de las FKs (no han sido comprobados en Test*SchemaMetadata)</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestFkMetadata()
		{
			if ("netcore".Equals(Platform) && "sqlite".Equals(dbmsname))
			{
				return;
			}
			// fk features not implemented in sqlite netcore
			Execute(dbt, sTab1);
			Execute(dbt, sTab2);
			SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			SchemaTable stab1 = mr.ReadTable("stab1");
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(stab1.GetColumns()[0].IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab1.GetColumns()[1].IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab1.GetColumns()[2].IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab1.GetColumns()[0].IsForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab1.GetColumns()[1].IsForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab1.GetColumns()[2].IsForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[0].GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[0].GetForeignKeyName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignKeyName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignKeyName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[0].GetForeignTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[0].GetForeignKeyColumn());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignKeyColumn());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab1.GetColumns()[1].GetForeignKeyColumn());
			SchemaTable stab2 = mr.ReadTable("stab2");
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(stab2.GetColumns()[0].IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(stab2.GetColumns()[1].IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab2.GetColumns()[2].IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab2.GetColumns()[0].IsForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(stab2.GetColumns()[1].IsForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(stab2.GetColumns()[2].IsForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab2.GetColumns()[0].GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("stab1.col11"), stab2.GetColumns()[1].GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("stab1.col11"), stab2.GetColumns()[1].GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab2.GetColumns()[0].GetForeignKeyName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("ref_stab1_col11"), stab2.GetColumns()[1].GetForeignKeyName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("ref_stab1_col11"), stab2.GetColumns()[1].GetForeignKeyName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab2.GetColumns()[0].GetForeignTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("stab1"), stab2.GetColumns()[1].GetForeignTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("stab1"), stab2.GetColumns()[1].GetForeignTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, stab2.GetColumns()[0].GetForeignKeyColumn());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("col11"), stab2.GetColumns()[1].GetForeignKeyColumn());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("col11"), stab2.GetColumns()[1].GetForeignKeyColumn());
		}

		/// <summary>Lectura avanzada de FKs, leyendo tambien las incoming FKs</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestFkIncomingAndOutgoing()
		{
			if ("netcore".Equals(Platform) && "sqlite".Equals(dbmsname))
			{
				return;
			}
			// fk features not implemented in sqlite netcore
			Execute(dbt, sTab1);
			Execute(dbt, sTab2);
			SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.SetUseIncomingFKs(true);
			// para que tambien obtenga claves ajenas entrantes a las tablas
			// Busca fks salientes de stab2 (una) y stab1 (cero)
			// comprueba tambien las pks
			SchemaTable stab2 = mr.ReadTable("stab2");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(1, stab2.GetFKs().Count);
			SchemaForeignKey fk = stab2.GetFKs()[0];
			NUnit.Framework.Legacy.ClassicAssert.AreEqual((myCatalogSchema2 + "stab2 CONSTRAINT ref_stab1_col11 FOREIGN KEY(col22) REFERENCES " + myCatalogSchema2 + "stab1(col11)").ToLower(), fk.ToString().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("ref_stab1_col11", fk.GetName().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("stab2", fk.GetFromTable().GetName().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("stab2", fk.GetFromTableIdentifier().GetTab().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("col22", ListToString(fk.GetFromColumnNames(), ",").ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("col11", ListToString(fk.GetToColumnNames(), ",").ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("stab1", fk.GetToTableIdentifier().GetTab().ToLower());
			SchemaTable stab1 = mr.ReadTable("stab1");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, stab1.GetFKs().Count);
			// Busca fks entrantes de las mismas tablas
			// en este caso no compara las columnas pues no se almacenan
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(1, stab1.GetIncomingFKs().Count);
			fk = stab1.GetIncomingFKs()[0];
			// la comparacion del string de la fk no incluye columnas puesto que en estas no
			// se guarda esta informacion
			NUnit.Framework.Legacy.ClassicAssert.AreEqual((myCatalogSchema2 + "stab2 CONSTRAINT ref_stab1_col11 FOREIGN KEY() REFERENCES " + myCatalogSchema2 + "stab1()").ToLower(), fk.ToString().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("ref_stab1_col11", fk.GetName().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("stab2", fk.GetFromTableIdentifier().GetTab().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("stab1", fk.GetToTableIdentifier().GetTab().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, stab2.GetIncomingFKs().Count);
		}

		/// <summary>
		/// Obtencion de una lista de tablas dependientes de las fks (prueba basica, la
		/// logica de recursividad se prueba en la ordenacion de fks)
		/// </summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestFkDependentTables()
		{
			if ("netcore".Equals(Platform) && "sqlite".Equals(dbmsname))
			{
				return;
			}
			// fk features not implemented in sqlite netcore
			Execute(dbt, sTab1);
			Execute(dbt, sTab2);
			SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			// Busca tabla 2, se debe encontrar tabla 1
			IList<string> tables = new List<string>();
			tables.Add("stab2");
			tables = mr.GetTableListAndDependent(tables);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(2, tables.Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("stab2", tables[0]);
			AssertEqualsDBObj("stab1", (tables[1]));
			// se compara con assertEqualsDBObj pues stab1 provendra del modelo
			// busca tabla 1, no encuentra nada pues no tiene fks
			tables = new List<string>();
			tables.Add("stab1");
			tables = mr.GetTableListAndDependent(tables);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(1, tables.Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("stab1", tables[0]);
		}
	}
}
