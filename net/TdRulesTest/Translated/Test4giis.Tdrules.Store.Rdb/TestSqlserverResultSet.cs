using Java.Sql;
using Java.Util;
using NUnit.Framework;
using Giis.Portable.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Reading basic data types from a ResultSet, check nullability and access by name or column number.
    /// Intended to test the .net ResultSet wrapper implementation
    /// </summary>
    public class TestSqlserverResultSet : Base
    {
        protected Connection dbt;
        protected ResultSet rs;
        protected string sCreate1 = "create table tabrs1 (pk integer primary key not null, " + "tint integer, tchar varchar(16), tdate date, tlong bigint)";
        protected string sSelect1 = "select pk, tint, tchar, tdate, tlong from tabrs1";
        [NUnit.Framework.SetUp]
        public override void SetUp()
        {
            base.SetUp();
            dbt = GetConnection(TEST_DBNAME2);
            ExecuteNotThrow(dbt, "drop table tabrs1");
            Execute(dbt, sCreate1);
        }

        [NUnit.Framework.TearDown]
        public virtual void TearDown()
        {
            if (rs != null)
                rs.Dispose();
            dbt.Dispose();
        }

        [Test]
        public virtual void TestReadResultSet()
        {
            Execute(dbt, "insert into tabrs1 (pk,tint,tchar,tdate,tlong) values(1, 2, null, '2023-01-02', null)");
            Execute(dbt, "insert into tabrs1 (pk,tint,tchar,tdate,tlong) values(2, null, 'chrchr', null, 55)");

            // 1st round: even columns are not null/odd columns are null (except pk), 
            // read by column number
            rs = Query(dbt, sSelect1);
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(rs.Next());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(1, rs.GetInt(1));
            int tint = rs.GetInt(2);
            NUnit.Framework.Legacy.ClassicAssert.IsFalse(rs.WasNull());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(2, tint);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("2", rs.GetString(2));
            string tchar = rs.GetString(3);
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(rs.WasNull());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(null, tchar);
            DateTime tdate;
            if (!"sqlite".Equals(dbmsname))
            {

                // sqlite does not have native Date
                tdate = rs.GetDate(4);
                NUnit.Framework.Legacy.ClassicAssert.IsFalse(rs.WasNull());
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("2023-01-02", DateString(tdate)); // normalize Date for .net compatibility
                NUnit.Framework.Legacy.ClassicAssert.IsNotNull(rs.GetString(4)); // do not check value because is system/locale dependent
            }

            long tlong = rs.GetLong(5);
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(rs.WasNull());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, tlong);

            // 2nd round: alternate nullability, read by column name
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(rs.Next());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(2, rs.GetInt("pk"));
            tint = rs.GetInt("tint");
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(rs.WasNull());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, tint);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(null, rs.GetString("tint"));
            tchar = rs.GetString("tchar");
            NUnit.Framework.Legacy.ClassicAssert.IsFalse(rs.WasNull());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("chrchr", tchar);
            if (!"sqlite".Equals(dbmsname))
            {
                tdate = rs.GetDate("tdate");
                NUnit.Framework.Legacy.ClassicAssert.IsTrue(rs.WasNull());
                if ("java".Equals(PLATFORM))
                    NUnit.Framework.Legacy.ClassicAssert.AreEqual(null, tdate);
                else

                    // on .net, DateTime is no nullable, returns DateTime.MinValue
                    NUnit.Framework.Legacy.ClassicAssert.AreEqual("0001-01-01", DateString(tdate));
                NUnit.Framework.Legacy.ClassicAssert.AreEqual(null, rs.GetString("tdate"));
            }

            tlong = rs.GetLong("tlong");
            NUnit.Framework.Legacy.ClassicAssert.IsFalse(rs.WasNull());
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(55, tlong);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("55", rs.GetString("tlong"));

            // resultset end
            NUnit.Framework.Legacy.ClassicAssert.IsFalse(rs.Next());
        }

        private string DateString(DateTime tdate)
        {
            return JavaCs.Substring(JavaCs.GetIsoDate(tdate), 0, 10);
        }
    }
}