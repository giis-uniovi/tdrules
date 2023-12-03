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
	/// <summary>Reading basic metadata (columns, fks, views...)</summary>
	public class TestSqlserverSchemaRead : Base
	{
		protected internal string catalog = null;

		protected internal string schema = null;

		protected internal Connection dbt;

		protected internal string myCatalogSchema2 = TestDbname2.ToLower() + ".dbo.";

		protected internal string Int = "int";

		protected internal string Identity = "int identity";

		protected internal int INT_precision = 0;

		protected internal string Decimal = "decimal";

		protected internal string Bit = "bit";

		protected internal int BIT_precision = 0;

		protected internal string Varchar = "varchar";

		protected internal string stringConcat = "+";

		protected internal string concatCharType = "varchar";

		protected internal int concatCharSize = 14;

		protected internal string sTab1 = "create table " + tablePrefix + "stab1 (col11 int not null primary key, " + "col12 char(3) not null, col13 varchar(16))";

		protected internal string sTab2 = "create table " + tablePrefix + "stab2 (col21 decimal(8,4), col22 int default (22), " + "col23 bit, " + "PRIMARY KEY (col21,col22), " + "CONSTRAINT ref_stab1_col11 FOREIGN KEY(col22) REFERENCES stab1(col11) " + ")";

		// Nombres de catalogo utilizados para el acceso a los metadatos
		// en sqlserver no se utilizan ya que se accede a las tablas de una base de datos
		// prefijo de catalogo y esquema para tablas completamente cualificadas
		// definiciones de tipos de datos cuyo nombre es diferente en otras plataformas
		// creacion de tablas para pruebas
		// tabla con dos fks, una a una clave y otra a una no clave
		// tabla con una fk no clave, y pk identity
		protected internal virtual string GetsTab3()
		{
			return "create table " + tablePrefix + "stab3 (colpk " + Identity + ", colfk int, " + "PRIMARY KEY (colpk), " + "CONSTRAINT ref_stab1_col11fk FOREIGN KEY(colfk) REFERENCES stab1(col11) " + ")";
		}

		protected internal string sView1 = "create view " + tablePrefix + "view1 (v11,v12,v13) as " + "select col11,col12,col13 from stab1";

		// dos vistas, simple/compleja con/sin argumentos
		protected internal virtual string GetsView2()
		{
			return "create view " + tablePrefix + "view2 as " + "select t2.col21, t2.col21*col11 as v22, " + "cast(t1.col12 as varchar(13)) as v23 ,t1.col12 " + stringConcat + " 'hello world' as v24 " + "from stab1 t1 left join stab2 t2 on t2.col22=t1.col11";
		}

		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual void CreateTablesAndViews()
		{
			Connection dbt = GetConnection(TestDbname2);
			// modo conectado para que se cierre la conexion
			Execute(dbt, sTab1);
			Execute(dbt, sTab2);
			Execute(dbt, GetsTab3());
			Execute(dbt, sView1);
			Execute(dbt, GetsView2());
			dbt.Close();
		}

		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual void DropTablesAndViews()
		{
			Connection dbt = GetConnection(TestDbname2);
			// modo conectado para que se cierre la conexion
			ExecuteNotThrow(dbt, "drop view view2");
			ExecuteNotThrow(dbt, "drop view view1");
			ExecuteNotThrow(dbt, "drop table stab33");
			ExecuteNotThrow(dbt, "drop table stab3");
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

		/// <summary>List de tablas y/o vistas, no entra en los metadatos de cada una</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReadListTableAndView()
		{
			CreateTablesAndViews();
			// comprueba los nombres, de estas tablas que se han creado (puede haber mas)
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			IList<string> lst = mr.GetTableList(true, false);
			// tablas
			string lstString = "," + ListToString(lst, ",").ToLower() + ",";
			AssertContains(",stab1,", lstString);
			AssertContains(",stab2,", lstString);
			AssertContains(",stab3,", lstString);
			AssertDoesNotContain(",view1,", lstString);
			lst = mr.GetTableList(false, true);
			// vistas
			lstString = "," + ListToString(lst, ",").ToLower() + ",";
			AssertContains(",view1,", lstString);
			AssertContains(",view2,", lstString);
			AssertDoesNotContain(",stab1,", lstString);
			lst = mr.GetTableList(true, true);
			// vistas
			lstString = "," + ListToString(lst, ",").ToLower() + ",";
			AssertContains(",stab1,", lstString);
			AssertContains(",view1,", lstString);
		}

		/// <summary>
		/// Lectura de metadatos de columnas en tablas, lectura secuencial de varias tablas
		/// con comprobacion detallada de los metadatos
		/// </summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReadMetadataInTables()
		{
			CreateTablesAndViews();
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.ReadTable("stab1");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsView());
			AssertEqualsDBObj("stab1", mr.GetTableName());
			AssertEqualsDBObj("col11", mr.GetColumn(0).GetColName());
			AssertEqualsDBObj("col11", mr.GetColumn(0).ToString());
			AssertEqualsDBObj(this.Int, mr.GetColumn(0).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(this.INT_precision, mr.GetColumn(0).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(0).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(0).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(0).GetCheckInConstraint());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.GetColumn(0).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.GetColumn(0).IsNotNull());
			AssertEqualsDBObj("col12", mr.GetColumn(1).GetColName());
			AssertEqualsDBObj("char", mr.GetColumn(1).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(3, mr.GetColumn(1).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(1).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(1).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.GetColumn(1).IsNotNull());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(1).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(1).GetCheckInConstraint());
			AssertEqualsDBObj("col13", mr.GetColumn(2).GetColName());
			AssertEqualsDBObj(this.Varchar, mr.GetColumn(2).GetDataType());
			// comprueba como se ven los string al pasar a sql
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("'Abc'", mr.GetColumn(1).GetAsSqlString("Abc"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("'A''c'", mr.GetColumn(1).GetAsSqlString("A'c"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("NULL", mr.GetColumn(1).GetAsSqlString(null));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(16, mr.GetColumn(2).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(2).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsNotNull());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(2).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(2).GetCheckInConstraint());
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReadMetadataInTables2()
		{
			CreateTablesAndViews();
			// Segunda tabla
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.ReadTable("stab2");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsView());
			AssertEqualsDBObj("stab2", mr.GetTableName());
			AssertEqualsDBObj("col21", mr.GetColumn(0).GetColName());
			AssertEqualsDBObj(this.Decimal, mr.GetColumn(0).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(8, mr.GetColumn(0).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(4, mr.GetColumn(0).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.GetColumn(0).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.GetColumn(0).IsNotNull());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(0).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(0).GetCheckInConstraint());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(0).GetDefaultValue());
			AssertEqualsDBObj("col22", mr.GetColumn(1).GetColName());
			AssertEqualsDBObj(this.Int, mr.GetColumn(1).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(this.INT_precision, mr.GetColumn(1).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(1).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.GetColumn(1).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.GetColumn(1).IsNotNull());
			AssertEqualsDBObj("stab1.col11", mr.GetColumn(1).GetForeignKey());
			AssertEqualsDBObj("ref_stab1_col11", mr.GetColumn(1).GetForeignKeyName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(1).GetCheckInConstraint());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("22", mr.GetColumn(1).GetDefaultValue());
			AssertEqualsDBObj("col23", mr.GetColumn(2).GetColName());
			AssertEqualsDBObj(this.Bit, mr.GetColumn(2).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(this.BIT_precision, mr.GetColumn(2).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(2).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsNotNull());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(2).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(2).GetCheckInConstraint());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(2).GetDefaultValue());
			// tercera tabla, fk que no es pk y pk identity, solo compruebo claves
			mr.ReadTable("stab3");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsTable());
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(mr.GetColumn(0).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(mr.GetColumn(0).IsForeignKey());
			if (!mr.GetDbmsType().IsOracle())
			{
				NUnit.Framework.Legacy.ClassicAssert.IsTrue(mr.GetColumn(0).IsAutoIncrement());
			}
			else
			{
				NUnit.Framework.Legacy.ClassicAssert.IsFalse(mr.GetColumn(0).IsAutoIncrement());
			}
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(mr.GetColumn(1).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(mr.GetColumn(1).IsForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(mr.GetColumn(1).IsAutoIncrement());
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReadMetadataInViews()
		{
			CreateTablesAndViews();
			SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.ReadTable("view1");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsView());
			AssertEqualsDBObj("view1", mr.GetTableName());
			AssertEqualsDBObj("v11", mr.GetColumn(0).GetColName());
			AssertEqualsDBObj(this.Int, mr.GetColumn(0).GetDataType());
			// el number en oracle se ve con precision 38
			NUnit.Framework.Legacy.ClassicAssert.AreEqual((mr.GetDbmsType().IsOracle() ? 38 : this.INT_precision), mr.GetColumn(0).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(0).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(0).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(0).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(0).GetCheckInConstraint());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(!mr.IsPostgres(), mr.GetColumn(0).IsNotNull());
			// postgres no puede determinar si es notnull
			AssertEqualsDBObj("v12", mr.GetColumn(1).GetColName());
			AssertEqualsDBObj("char", mr.GetColumn(1).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(3, mr.GetColumn(1).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(1).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(1).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(!mr.IsPostgres(), mr.GetColumn(1).IsNotNull());
			// postgres no puede determinar si es notnull
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(1).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(1).GetCheckInConstraint());
			AssertEqualsDBObj("v13", mr.GetColumn(2).GetColName());
			AssertEqualsDBObj(this.Varchar, mr.GetColumn(2).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(16, mr.GetColumn(2).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(2).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsNotNull());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(2).GetForeignKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, mr.GetColumn(2).GetCheckInConstraint());
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReadMetadataInViews2()
		{
			CreateTablesAndViews();
			// Segunda vista, ya no compruebo foreign ni constraint
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.ReadTable("view2");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsTable());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsView());
			AssertEqualsDBObj("view2", mr.GetTableName());
			AssertEqualsDBObj("col21", mr.GetColumn(0).GetColName());
			// el nombre de la columna base pues no se especifico
			// alias
			AssertEqualsDBObj(this.Decimal, mr.GetColumn(0).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(8, mr.GetColumn(0).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(4, mr.GetColumn(0).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(0).IsKey());
			// col21 es key en la tabla base, pero de la vista no salen claves
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(0).IsNotNull());
			// nullable pues el dato base lo es
			AssertEqualsDBObj("v22", mr.GetColumn(1).GetColName());
			AssertEqualsDBObj(this.Decimal, mr.GetColumn(1).GetDataType());
			// resultado de decimal y de int es decimal
			// aunque el tipo de dato era decimal(8,4) en la vista aparece como 19,4 en
			// SQLServer y 0 en Oracle y postgres
			NUnit.Framework.Legacy.ClassicAssert.AreEqual((mr.GetDbmsType().IsOracle() || mr.GetDbmsType().IsPostgres() ? 0 : 19), mr.GetColumn(1).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual((mr.GetDbmsType().IsOracle() || mr.GetDbmsType().IsPostgres() ? 0 : 4), mr.GetColumn(1).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(1).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(1).IsNotNull());
			// uno es nullable y otro no, es nullable
			AssertEqualsDBObj("v23", mr.GetColumn(2).GetColName());
			AssertEqualsDBObj(this.Varchar, mr.GetColumn(2).GetDataType());
			// cast de char a varchar es varchar
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(13, mr.GetColumn(2).GetColSize());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(2).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsKey());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(2).IsNotNull());
			AssertEqualsDBObj("v24", mr.GetColumn(3).GetColName());
			AssertEqualsDBObj(this.concatCharType, mr.GetColumn(3).GetDataType());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(this.concatCharSize, mr.GetColumn(3).GetColSize());
			// el tamanyo es 14 (hello world tiene 11 mas
			// los 3 del char)
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, mr.GetColumn(3).GetDecimalDigits());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.GetColumn(3).IsKey());
			// esta concatenacion es no nullable en sqlserver, pero nullable en oracle
			NUnit.Framework.Legacy.ClassicAssert.AreEqual((mr.GetDbmsType().IsOracle() || mr.GetDbmsType().IsPostgres() ? false : true), mr.GetColumn(3).IsNotNull());
		}

		/// <summary>Consulta de la query de creacion de una vista</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReaderViewDescription()
		{
			this.CreateTablesAndViews();
			SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.ReadTable("View1");
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(mr.IsView());
			string expectedView = AsStored("create view " + tablePrefix + "view1 (v11,v12,v13) as") + " select col11,col12,col13 from stab1";
			if (mr.IsPostgres())
			{
				expectedView = "CREATE VIEW view1 (v11,v12,v13) AS SELECT stab1.col11 AS v11,\n" + "    stab1.col12 AS v12,\n" + "    stab1.col13 AS v13\n" + "   FROM stab1;";
			}
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(expectedView, mr.GetQuery(mr.GetCurrentTable()));
			// comprueba ante cambio de la vista, mayusculas, minusculas y espacios, solo en
			// sqlserver
			// en oracle o postgres no se usa alter view con esta sintaxis
			if (mr.IsSQLServer())
			{
				Execute(dbt, "  alter   view   view1   As   Select   col13, col12, col11   from   stab1  ");
				mr.ReadTable("View1");
				NUnit.Framework.Legacy.ClassicAssert.IsTrue(mr.IsView());
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("col13", mr.GetColumn(0).GetColName());
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("col12", mr.GetColumn(1).GetColName());
				NUnit.Framework.Legacy.ClassicAssert.AreEqual("col11", mr.GetColumn(2).GetColName());
				NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored("CREATE   view   " + tablePrefix + "view1   As") + "   Select   col13, col12, col11   from   stab1", mr.GetQuery(mr.GetCurrentTable()));
			}
			// Las vistas que se guardan en INFORMATION_SCHEMA tienen un limite de 4000
			// caracteres, si son mayores
			// se guarda null. Para cada SGBD se deberan usar los metodos especificos de la
			// plataforma para evitar este problema
			Execute(dbt, "drop view view1");
			string createViewMain = "create view view1 (col11,col12,col13) as ";
			string createViewSql = "select col11,col12,col13 from stab1";
			string addView = " \nunion all select col11,col12,col13 from stab1";
			int numRepeats = 10000 / addView.Length;
			for (int i = 0; i < numRepeats; i++)
			{
				createViewSql += addView;
			}
			Execute(dbt, createViewMain + createViewSql);
			mr.ReadTable("View1");
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(mr.IsView());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("col11", mr.GetColumn(0).GetColName().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("col12", mr.GetColumn(1).GetColName().ToLower());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("col13", mr.GetColumn(2).GetColName().ToLower());
			createViewMain = AsStored(createViewMain);
			if (mr.IsPostgres())
			{
				createViewMain = "CREATE VIEW view1 (col11,col12,col13) AS ";
				createViewSql = "SELECT stab1.col11,\n" + "    stab1.col12,\n" + "    stab1.col13\n" + "   FROM stab1";
				addView = "\nUNION ALL\n" + " SELECT stab1.col11,\n" + "    stab1.col12,\n" + "    stab1.col13\n" + "   FROM stab1";
				for (int i_1 = 0; i_1 < numRepeats; i_1++)
				{
					createViewSql += addView;
				}
				createViewSql += ";";
			}
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(createViewMain + createViewSql, mr.GetQuery(mr.GetCurrentTable()));
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
		/// <summary>Lectura de FKs</summary>
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestGetFKs()
		{
			Execute(dbt, sTab1);
			Execute(dbt, sTab2);
			SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.SetUseIncomingFKs(true);
			// para que tambien obtenga claves ajenas entrantes a las tablas
			// Busca fks salientes de stab2 (una) y stab1 (cero)
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
		public virtual void TestDependentFKs()
		{
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
