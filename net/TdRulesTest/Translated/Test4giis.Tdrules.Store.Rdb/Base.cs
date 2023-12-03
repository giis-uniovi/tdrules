/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using System.IO;
using Giis.Portable.Util;
using Giis.Tdrules.Store.Rdb;
using Java.Sql;
using NLog;
using NUnit.Framework;


using Sharpen;

namespace Test4giis.Tdrules.Store.Rdb
{
	/// <summary>Common definitions to be inherited by the database tests.</summary>
	/// <remarks>
	/// Common definitions to be inherited by the database tests.
	/// The reference implementation of tests is Sqlserver,
	/// that is inherited to test other DBMS
	/// </remarks>
	public class Base
	{
		protected internal static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(Base));

		protected internal static readonly string Platform = Parameters.GetPlatformName();

		protected internal static readonly string SetupPath = FileUtil.GetPath(Parameters.GetProjectRoot(), "..", "setup");

		protected internal static readonly string EnvironmentProperties = FileUtil.GetPath(SetupPath, "environment.properties");

		protected internal static readonly string DatabaseProperties = FileUtil.GetPath(SetupPath, "database.properties");

		public const string TestDbname2 = "tdstorerdb2";

		protected internal static string tablePrefix = string.Empty;

		protected internal string dbmsname = "sqlserver";

		protected internal string dbmsproductname = "Microsoft SQL Server";

		protected internal bool storesUpperCase = false;

		protected internal bool storesLowerCase = false;

		
		

		// nombres de las bases de datos a utilizar
		// Prefijo que se pone antes del nombre de las tablas al crear
		// necesario en SQLServer cuando se entra con un usuario no privilegiado para poner dbo como owner
		// Datos principales para parametrizacion del sgbd usado en el test
		// Esta es la referencia, el resto de sgbds son clases heredadas que cambian este valor
		/// <exception cref="Java.Sql.SQLException"/>
		[NUnit.Framework.SetUp]
		public virtual void SetUp()
		{
			log.Info("****** Running test: {} ******", NUnit.Framework.TestContext.CurrentContext.Test.Name );
		}

		/// <summary>
		/// Connection user and url are obtained from a properties file,
		/// password is obtained from environment, if not defined, another properties file is used as fallback
		/// </summary>
		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual Connection GetConnection(string database)
		{
			log.Debug("Create connection to '{}' database", dbmsname);
			string propPrefix = "tdrules." + Platform + "." + database + "." + dbmsname;
			Connection conn = DriverManager.GetConnection(new JdbcProperties().GetProp(DatabaseProperties, propPrefix + ".url"), new JdbcProperties().GetProp(DatabaseProperties, propPrefix + ".user"), new JdbcProperties().GetEnvVar(EnvironmentProperties, "TEST_" + dbmsname.ToUpper() + "_PWD")
				);
			//Avoid lock problems that make flaky tests when running concurrently with other tests (mainly for getTableList)
			if ("sqlserver".Equals(dbmsname))
			{
				conn.SetTransactionIsolation(ConnectionConstants.TransactionReadUncommitted);
			}
			return conn;
		}

		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual void Execute(Connection dbt, string sql)
		{
			Statement stmt = dbt.CreateStatement();
			stmt.ExecuteUpdate(sql);
		}

		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual void ExecuteNotThrow(Connection dbt, string sql)
		{
			Statement stmt = dbt.CreateStatement();
			try
			{
				stmt.ExecuteUpdate(sql);
			}
			catch (SQLException)
			{
			}
		}

		/// <exception cref="Java.Sql.SQLException"/>
		protected internal virtual ResultSet Query(Connection dbt, string sql)
		{
			Statement stmt = dbt.CreateStatement();
			return stmt.ExecuteQuery(sql);
		}

		/// <summary>
		/// Cambia el case de un string leido de los metadatos en el caso de que la BD
		/// tenga esta configuracion (p.e.
		/// </summary>
		/// <remarks>
		/// Cambia el case de un string leido de los metadatos en el caso de que la BD
		/// tenga esta configuracion (p.e. oracle todo en uppercase)
		/// </remarks>
		public virtual string AsStored(string value)
		{
			if (storesUpperCase)
			{
				value = value.ToUpper();
			}
			else
			{
				if (storesLowerCase)
				{
					value = value.ToLower();
				}
			}
			return value;
		}

		public virtual void AssertEqualsDBObj(string expected, string actual)
		{
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored(expected), actual);
		}

		public virtual void AssertContains(string expectedSubstring, string actual)
		{
			NUnit.Framework.Legacy.ClassicAssert.IsTrue(actual.Contains(expectedSubstring), "Expected substring should be contained in actual: " + actual);
		}

		public virtual void AssertDoesNotContain(string unexpectedSubstring, string actual)
		{
			NUnit.Framework.Legacy.ClassicAssert.IsFalse(actual.Contains(unexpectedSubstring), "Expected substring should not be contained in actual: " + actual);
		}

		public virtual string ListToString(IList<string> lst, string separator)
		{
			StringWriter sw = new StringWriter();
			for (int i = 0; i < lst.Count; i++)
			{
				sw.Write((i != 0 ? separator : string.Empty) + lst[i]);
			}
			return sw.ToString();
		}
	}
}
