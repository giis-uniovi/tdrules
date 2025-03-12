using Java.Sql;
using NUnit.Framework;
using Giis.Tdrules.Store.Rdb;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Reading metadata for all datatypes (from views and tables).
    /// As metadata has many differences between different DBMSs, asserts are made against external files.
    /// Includes test for listing tables and views.
    /// </summary>
    public class TestSqlserverSchemaMetadata : Base
    {
        protected Connection dbt;
        // Most used sqlserver datatypes:
        // https://learn.microsoft.com/en-us/sql/t-sql/data-types/data-types-transact-sql?view=sql-server-ver16
        protected string sTypes1 = "create table stypes1 (pkauto int identity primary key not null, " + "tinteger integer not null, tint4 int, tint8 bigint, tint2 smallint, tint1 tinyint, " + "tnum numeric default(8), tnum1 numeric(6), tnum2 numeric(6,2), " + "tdec decimal, tdec1 decimal(6), tdec2 decimal(6,2), " + "tbit bit, " + "treal real, tfloat float(7), " + "tmoney money, tsmallmoney smallmoney, " + "tcharacter character(3) default 'abc', tchar char(3), tvarchar varchar(16), ttext text, " + "tdate date, ttime time, tdatetime datetime, ttimestamp timestamp, " + "tdatetime2 datetime2, tsmalldatetime smalldatetime, tdatetimeoffset datetimeoffset " + ")";
        // View with arguments
        protected string sTypesvp = "create view stypesvp (p1,p2,p3) as select tinteger,tint4,tcharacter from stypes1";
        protected virtual void CreateViewXXL()
        {

            // Las vistas que se guardan en INFORMATION_SCHEMA tienen un limite de 4000 caracteres, si son mayores se guarda null.
            // Para cada SGBD se deberan usar los metodos especificos de la plataforma al tratar vistas como la definida aqui
            string createViewMain = "create view stypesvxxl (p1,p2,p3) as ";
            string createViewSql = "select tinteger,tint4,tcharacter from stypes1";
            string addView = " \nunion all " + createViewSql;
            int numRepeats = 10000 / addView.Length;
            StringBuilder sb = new StringBuilder();
            sb.Append(createViewSql);
            for (int i = 0; i < numRepeats; i++)
                sb.Append(addView);
            Execute(dbt, createViewMain + sb.ToString());
        }

        protected virtual void CreateTablesAndViews()
        {
            Execute(dbt, sTypes1);
            Execute(dbt, sTypesvp);
        }

        protected virtual void DropTablesAndViews()
        {
            ExecuteNotThrow(dbt, "drop view stypesvp");
            ExecuteNotThrow(dbt, "drop view stypesvxxl");
            ExecuteNotThrow(dbt, "drop table stypes1");
        }

        [NUnit.Framework.SetUp]
        public override void SetUp()
        {
            base.SetUp();
            dbt = GetConnection(TEST_DBNAME2);
            this.DropTablesAndViews();
        }

        [NUnit.Framework.TearDown]
        public virtual void TearDown()
        {
            dbt.Dispose();
        }

        [Test]
        public virtual void TestReadTableMetadata()
        {
            CreateTablesAndViews();
            SchemaReader mr = new SchemaReaderJdbc(dbt);
            mr.ReadTable("stypes1");
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsTable());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsView());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsType());
            AssertEqualsDBObj(AsStored("stypes1"), mr.GetTableName());
            string metadata = GetMetadataAsString(mr);
            AssertMetadata(metadata, PLATFORM + "." + dbmsname + ".metadata.types1.txt");
        }

        [Test]
        public virtual void TestReadViewMetadata()
        {
            CreateTablesAndViews();
            SchemaReader mr = new SchemaReaderJdbc(dbt);
            mr.ReadTable("stypesvp");
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsTable());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsView());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsType());
            AssertEqualsDBObj(AsStored("stypesvp"), mr.GetTableName());
            string metadata = GetMetadataAsString(mr);
            AssertMetadata(metadata, PLATFORM + "." + dbmsname + ".metadata.typesvp.txt");
        }

        [Test]
        public virtual void TestReadViewXXLMetadata()
        {
            CreateTablesAndViews();
            CreateViewXXL();
            SchemaReader mr = new SchemaReaderJdbc(dbt);
            mr.ReadTable("stypesvxxl");
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsTable());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(true, mr.IsView());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(false, mr.IsType());
            AssertEqualsDBObj(AsStored("stypesvxxl"), mr.GetTableName());
            string metadata = GetMetadataAsString(mr);
            AssertMetadata(metadata, PLATFORM + "." + dbmsname + ".metadata.typesvxxl.txt"); // nota: oracle no permite conocer la nullabilidad en la vista generada con union
        }

        /// <summary>
        /// List de tablas y/o vistas, discriminado por tipo
        /// </summary>
        [Test]
        public virtual void TestReadListTableAndView()
        {
            CreateTablesAndViews();
            CreateViewXXL();

            // comprueba los nombres, de estas tablas que se han creado (puede haber mas)
            SchemaReader mr = new SchemaReaderJdbc(dbt);
            IList<string> lst = mr.GetTableList(true, false); // tablas
            string lstString = "," + String.Join(",", lst).ToLower() + ",";
            AssertContains(",stypes1,", lstString);
            AssertDoesNotContain(",stypesvp,", lstString);
            lst = mr.GetTableList(false, true); // vistas
            lstString = "," + String.Join(",", lst).ToLower() + ",";
            AssertContains(",stypesvp,", lstString);
            AssertContains(",stypesvxxl,", lstString);
            AssertDoesNotContain(",stypes1,", lstString);
            lst = mr.GetTableList(true, true); // vistas
            lstString = "," + String.Join(",", lst).ToLower() + ",";
            AssertContains(",stypes1,", lstString);
            AssertContains(",stypesvp,", lstString);
            AssertContains(",stypesvxxl,", lstString);
        }
    }
}