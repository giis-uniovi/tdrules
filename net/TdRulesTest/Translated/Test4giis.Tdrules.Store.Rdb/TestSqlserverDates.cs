/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Tdrules.Store.Rdb;
using Giis.Tdrules.Store.Stypes;
using Java.Sql;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Store.Rdb
{
	/// <summary>Reading and writing date related fields, with and without time</summary>
	public class TestSqlserverDates : Base
	{
		protected internal string catalog = null;

		protected internal string schema = null;

		protected internal StoreType dbms;

		protected internal string Datetime = "datetime";

		protected internal int DatetimeSize = 0;

		protected internal int DatetimeDigits = 0;

		protected internal string DatetimePrefixAsSqlString = string.Empty;

		protected internal Connection dbt;

		/// <exception cref="Java.Sql.SQLException"/>
		[NUnit.Framework.SetUp]
		public override void SetUp()
		{
			base.SetUp();
			dbms = StoreType.Get(dbmsname);
			dbt = GetConnection(TestDbname2);
			ExecuteNotThrow(dbt, "drop table sdates");
			Execute(dbt, "create table sdates (Pk1 int, primary key(Pk1), cdatetime " + dbms.GetDataTypeDatetime() + ", cdate date)");
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[NUnit.Framework.TearDown]
		public virtual void TearDown()
		{
			dbt.Close();
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReaderDatesAndTimes()
		{
			SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
			mr.ReadTable("sdates");
			AssertEqualsDBObj("sdates", mr.GetTableName());
			AssertEqualsDBObj("cdatetime", mr.GetColumn(1).GetColName());
			AssertEqualsDBObj(Datetime, mr.GetColumn(1).GetDataType());
			NUnit.Framework.Assert.AreEqual(DatetimeSize, mr.GetColumn(1).GetColSize());
			NUnit.Framework.Assert.AreEqual(DatetimeDigits, mr.GetColumn(1).GetDecimalDigits());
			// Metodo para obtener el string de una fecha tal y como se admite para una sentencia sql
			NUnit.Framework.Assert.AreEqual(DatetimePrefixAsSqlString + "'2013-05-01 10:02:01'", mr.GetColumn(1).GetAsSqlString("2013-05-01 10:02:01"));
			NUnit.Framework.Assert.AreEqual("NULL", mr.GetColumn(1).GetAsSqlString(null));
			AssertEqualsDBObj("cdate", mr.GetColumn(2).GetColName());
			AssertEqualsDBObj("date", mr.GetColumn(2).GetDataType());
			NUnit.Framework.Assert.AreEqual(0, mr.GetColumn(2).GetColSize());
			NUnit.Framework.Assert.AreEqual(0, mr.GetColumn(2).GetDecimalDigits());
			NUnit.Framework.Assert.AreEqual("DATE '2013-05-01'", mr.GetColumn(2).GetAsSqlString("2013-05-01 10:02:01"));
			NUnit.Framework.Assert.AreEqual("NULL", mr.GetColumn(2).GetAsSqlString(null));
		}

		/// <exception cref="Java.Sql.SQLException"/>
		[Test]
		public virtual void TestReadAndWrite()
		{
			// Escritura de los datos a la bd de literales datetime y date
			// En dos filas, en la primera coincide el literal escrito con el tipo de dato,
			// en la segunda al reves (escribe datetime en date y date en datetime)
			string cdatetime = dbms.GetSqlDatetimeLiteral("2022-12-30", "14:10:11");
			string cdate = dbms.GetSqlDatetimeLiteral("2022-12-30", string.Empty);
			string sql = "insert into sdates (pk1,cdatetime,cdate) values( 1, " + cdatetime + ", " + cdate + ")";
			Execute(dbt, sql);
			sql = "insert into sdates (pk1,cdatetime,cdate) values( 2, " + cdate + ", " + cdatetime + ")";
			Execute(dbt, sql);
			// Recuperacion de los datos insertados, formateados como string
			sql = "select pk1, " + dbms.GetSqlDatetimeColumnString("cdatetime") + " as cdatetime, " + dbms.GetSqlDateColumnString("cdate") + " as cdate from sdates order by pk1";
			ResultSet rs = Query(dbt, sql);
			rs.Next();
			NUnit.Framework.Assert.AreEqual("2022-12-30 14:10:11", rs.GetString("cdatetime"));
			NUnit.Framework.Assert.AreEqual("2022-12-30", rs.GetString("cdate"));
			rs.Next();
			NUnit.Framework.Assert.AreEqual("2022-12-30 00:00:00", rs.GetString("cdatetime"));
			NUnit.Framework.Assert.AreEqual("2022-12-30", rs.GetString("cdate"));
		}
	}
}
