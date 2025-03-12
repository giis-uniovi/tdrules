using Java.Sql;
using NUnit.Framework;
using Giis.Tdrules.Store.Rdb;
using Giis.Tdrules.Store.Stypes;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Reading of check constraints
    /// </summary>
    public class TestSqlserverSchemaChecks : Base
    {
        protected string catalog = null;
        protected string schema = null;
        protected StoreType dbms;
        protected string enableCheck = ""; // algunas dbms como oracle requieren que se especifique enabled en la creacion
        protected Connection dbt;
        [NUnit.Framework.SetUp]
        public override void SetUp()
        {
            base.SetUp();
            dbms = StoreType.Get(dbmsname);
            dbt = GetConnection(TEST_DBNAME2);
            ExecuteNotThrow(dbt, "drop table stabc");
            Execute(dbt, "create table " + tablePrefix + "stabc (id numeric(10) not null primary key, num numeric(10), text1 char(1)" + ", check(id>0)" + enableCheck + ", check(id<num)" + enableCheck + ", check(text1 in('S','N'))" + enableCheck + ")");
        }

        [Test]
        public virtual void TestReaderCheckConstraints()
        {
            SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
            SchemaTable tab1 = mr.ReadTable("stabc");
            IList<SchemaCheckConstraint> checks = tab1.GetCheckConstraints();

            // las comparaciones son muy diferentes segun el sgbd, guardo los datos para los
            // assets en arrays
            string[] checkColumns;
            string[] checkConstraints;
            if (mr.IsOracle())
            {

                // Oracle incluye las is not null (pero son filtradas por el reader), no
                // proporciona nombre de columna y los in los traduce literalmente
                checkColumns = new string[]
                {
                    "",
                    "",
                    ""
                };
                checkConstraints = new string[]
                {
                    "id>0",
                    "id<num",
                    "text1 in('S','N')"
                };
            }
            else if (mr.IsSqlite())
            {
                checkColumns = new string[]
                {
                    "",
                    "",
                    ""
                };
                checkConstraints = new string[]
                {
                    "(id>0)",
                    "(id<num)",
                    "(text1 in('S','N'))"
                };
            } // muestra la columna, algunas duplicadas si involucran diferentes columnas
            else if (mr.IsPostgres())
            {

                // muestra la columna, algunas duplicadas si involucran diferentes columnas
                checkColumns = new string[]
                {
                    "id",
                    "id",
                    "num",
                    "text1"
                };
                checkConstraints = new string[]
                {
                    "((id < num))",
                    "((id > (0)::numeric))",
                    "((id < num))",
                    "((text1 = ANY (ARRAY['S'::bpchar, 'N'::bpchar])))"
                };
            } // muestra la columna, algunas duplicadas si involucran diferentes columnas
            else if (mr.IsSQLServer())
            {

                // muestra la columna, algunas duplicadas si involucran diferentes columnas
                checkColumns = new string[]
                {
                    "id",
                    "id",
                    "num",
                    "text1"
                };
                checkConstraints = new string[]
                {
                    "([id]<[num])",
                    "([id]>(0))",
                    "([id]<[num])",
                    "([text1]='N' OR [text1]='S')"
                };
            }
            else
                throw new SchemaException("Expected values for checks not stablished for this dbms");
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(checkConstraints.Length, checks.Count); // hay tres constraints, pero una involucra dos columnas,

            // luego aparece dos veces
            for (int i = 0; i < checkConstraints.Length; i++)
            {
                NUnit.Framework.Legacy.ClassicAssert.AreEqual(checkColumns[i], checks[i].GetColumn());
                NUnit.Framework.Legacy.ClassicAssert.AreEqual(checkConstraints[i], checks[i].GetConstraint());
            }


            // Comprueba los checks asociados a columnas
            // Solo se ha implementado para oracle en el caso de check in
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("", mr.GetColumn(0).GetCheckInConstraint());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("", mr.GetColumn(1).GetCheckInConstraint());
            if (dbms.IsOracle())
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("('S','N')", mr.GetColumn(2).GetCheckInConstraint());
            else
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("", mr.GetColumn(2).GetCheckInConstraint());
        }
    }
}