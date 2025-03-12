using Java.Sql;
using Java.Util;
using NUnit.Framework;
using Giis.Portable.Util;
using Giis.Tdrules.Client.Rdb;
using Giis.Tdrules.Openapi.Model;
using Giis.Tdrules.Store.Rdb;
using Giis.Visualassert;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Client.Rdb
{
    /// <summary>
    /// Basic tests of reading a model with usual elements
    /// (different datatypes, relations, checks), using Sqlserver.
    /// Detailed tests are in module tdrules-store-rdb
    /// </summary>
    public class TestSqlserverReadModel : Base
    {
        protected Properties config;
        [NUnit.Framework.SetUp]
        public override void SetUp()
        {
            base.SetUp();

            //una tabla que no sera leida y dos tablas enlazadas con fk, checks, defaults y varios tipos de datos
            //el check no lo pone directamente con check... sino con constraint para darle un nombre y luego poder comparar
            Connection dbt = GetConnection("sqlserver", TEST_DBNAME);
            ExecuteNotThrow(dbt, "drop view clirdbv");
            ExecuteNotThrow(dbt, "drop table clirdb2");
            ExecuteNotThrow(dbt, "drop table clirdb1");
            ExecuteNotThrow(dbt, "drop table clirdb0");
            Execute(dbt, "create table clirdb0 (col01 int not null primary key)");
            Execute(dbt, "create table clirdb1 (col11 int not null primary key, col12 varchar(16))");
            Execute(dbt, "create table clirdb2 (col21 decimal(8,4), col22 int not null default (22), CONSTRAINT chk_clirdb_col22 CHECK (col22>0), " + "PRIMARY KEY (col21,col22), CONSTRAINT ref_clirdb1_col11 FOREIGN KEY(col22) REFERENCES clirdb1(col11) )");
            Execute(dbt, "create view clirdbv as select col01 from clirdb0");
            dbt.Dispose();
        }

        // Each choice of
        // -Create from connection/schema reader
        // -Get objects by table list selection/table or view selection
        // -Different parameters in table or view selection
        [Test]
        public virtual void TestGetModelUsingTableList()
        {
            DbSchemaApi api = new DbSchemaApi(GetConnection("sqlserver", TEST_DBNAME));
            IList<string> tables = new List<string>();
            tables.Add("clirdb1");
            tables.Add("clirdb2");
            TdSchema model = api.GetSchema(tables);
            VisualAssert va = new VisualAssert();
            string expectedFileName = FileUtil.GetPath(TEST_PATH_BENCHMARK, "model-bmk.txt");
            string actual = api.ModelToString(model);
            va.AssertEquals(FileUtil.FileRead(expectedFileName).Replace("\r", ""), actual.Replace("\r", ""));
        }

        [Test]
        public virtual void TestGetModelSelectTablesOrViews()
        {

            // different form to create the api
            SchemaReader sr = new SchemaReaderJdbc(GetConnection("sqlserver", TEST_DBNAME));
            DbSchemaApi api = new DbSchemaApi(sr);

            // starting with clirdb to do not show tables from other tests
            TdSchema model = api.GetSchema(true, true, true, "clirdb");
            VisualAssert va = new VisualAssert();
            string expectedFileName = FileUtil.GetPath(TEST_PATH_BENCHMARK, "model-all-bmk.txt");
            string actual = api.ModelToString(model);
            va.AssertEquals(FileUtil.FileRead(expectedFileName).Replace("\r", ""), actual.Replace("\r", ""));

            // repeat, using connection, but more selective, only views
            api = new DbSchemaApi(GetConnection("sqlserver", TEST_DBNAME));
            model = api.GetSchema(false, true, true, "clirdb");
            expectedFileName = FileUtil.GetPath(TEST_PATH_BENCHMARK, "model-view-bmk.txt");
            actual = api.ModelToString(model);
            va.AssertEquals(FileUtil.FileRead(expectedFileName).Replace("\r", ""), actual.Replace("\r", ""));
        }

        [Test]
        public virtual void TestGetModelDefault()
        {
            Connection conn = GetConnection("sqlserver", TEST_DBNAME);
            DbSchemaApi api = new DbSchemaApi(conn);
            TdSchema schema = api.GetSchema();

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
                if (name.Equals(table.GetName()))
                    return table;
            return null;
        }
    }
}