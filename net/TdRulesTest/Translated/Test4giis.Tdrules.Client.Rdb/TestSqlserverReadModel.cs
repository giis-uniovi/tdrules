/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Portable.Util;
using Giis.Tdrules.Client.Rdb;
using Giis.Tdrules.Openapi.Model;
using Giis.Tdrules.Store.Rdb;
using Giis.Visualassert;
using Java.Sql;
using Java.Util;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Client.Rdb
{
	/// <summary>
	/// Basic tests of reading a model with usual elements
	/// (different datatypes, relations, checks), using Sqlserver.
	/// </summary>
	/// <remarks>
	/// Basic tests of reading a model with usual elements
	/// (different datatypes, relations, checks), using Sqlserver.
	/// Detailed tests are in module tdrules-store-rdb
	/// </remarks>
	public class TestSqlserverReadModel : Base
	{
		protected internal Properties config;

		/// <exception cref="Java.Sql.SQLException"/>
		[NUnit.Framework.SetUp]
		public override void SetUp()
		{
			base.SetUp();
			//una tabla que no sera leida y dos tablas enlazadas con fk, checks, defaults y varios tipos de datos
			//el check no lo pone directamente con check... sino con constraint para darle un nombre y luego poder comparar
			Connection dbt = GetConnection("sqlserver", TestDbname);
			ExecuteNotThrow(dbt, "drop view clirdbv");
			ExecuteNotThrow(dbt, "drop table clirdb2");
			ExecuteNotThrow(dbt, "drop table clirdb1");
			ExecuteNotThrow(dbt, "drop table clirdb0");
			Execute(dbt, "create table clirdb0 (col01 int not null primary key)");
			Execute(dbt, "create table clirdb1 (col11 int not null primary key, col12 varchar(16))");
			Execute(dbt, "create table clirdb2 (col21 decimal(8,4), col22 int not null default (22), CONSTRAINT chk_clirdb_col22 CHECK (col22>0), " + "PRIMARY KEY (col21,col22), CONSTRAINT ref_clirdb1_col11 FOREIGN KEY(col22) REFERENCES clirdb1(col11) )");
			Execute(dbt, "create view clirdbv as select col01 from clirdb0");
			dbt.Close();
		}

		// Each choice of
		// -Create from connection/schema reader
		// -Get objects by table list selection/table or view selection
		// -Different parameters in table or view selection
		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestGetModelUsingTableList()
		{
			DbSchemaApi api = new DbSchemaApi(GetConnection("sqlserver", TestDbname));
			IList<string> tables = new List<string>();
			tables.Add("clirdb1");
			tables.Add("clirdb2");
			TdSchema model = api.GetDbSchema(tables);
			VisualAssert va = new VisualAssert();
			string expectedFileName = FileUtil.GetPath(TestPathBenchmark, "model-bmk.txt");
			string actual = api.ModelToString(model);
			va.AssertEquals(FileUtil.FileRead(expectedFileName).Replace("\r", string.Empty), actual.Replace("\r", string.Empty));
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestGetModelSelectTablesOrViews()
		{
			// different form to create the api
			SchemaReader sr = new SchemaReaderJdbc(GetConnection("sqlserver", TestDbname));
			DbSchemaApi api = new DbSchemaApi(sr);
			// starting with clirdb to do not show tables from other tests
			TdSchema model = api.GetDbSchema(true, true, true, "clirdb");
			VisualAssert va = new VisualAssert();
			string expectedFileName = FileUtil.GetPath(TestPathBenchmark, "model-all-bmk.txt");
			string actual = api.ModelToString(model);
			va.AssertEquals(FileUtil.FileRead(expectedFileName).Replace("\r", string.Empty), actual.Replace("\r", string.Empty));
			// repeat, using connection, but more selective, only views
			api = new DbSchemaApi(GetConnection("sqlserver", TestDbname));
			model = api.GetDbSchema(false, true, true, "clirdb");
			expectedFileName = FileUtil.GetPath(TestPathBenchmark, "model-view-bmk.txt");
			actual = api.ModelToString(model);
			va.AssertEquals(FileUtil.FileRead(expectedFileName).Replace("\r", string.Empty), actual.Replace("\r", string.Empty));
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestGetModelDefault()
		{
			Connection conn = GetConnection("sqlserver", TestDbname);
			DbSchemaApi api = new DbSchemaApi(conn);
			TdSchema schema = api.GetDbSchema();
			// now check only that the tables and views are in the model
			// not using model extensions for net compatibility
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("clirdb0", GetTable(schema, "clirdb0").GetName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("clirdb1", GetTable(schema, "clirdb1").GetName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("clirdb2", GetTable(schema, "clirdb2").GetName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("clirdbv", GetTable(schema, "clirdbv").GetName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("table", GetTable(schema, "clirdb0").GetEntitytype());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("view", GetTable(schema, "clirdbv").GetEntitytype());
		}

		private TdEntity GetTable(TdSchema schema, string name)
		{
			foreach (TdEntity table in schema.GetEntities())
			{
				if (name.Equals(table.GetName()))
				{
					return table;
				}
			}
			return null;
		}
	}
}
