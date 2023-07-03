/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Portable.Util;
using Giis.Tdrules.Store.Rdb;
using Java.Sql;
using NLog;
using NUnit.Framework;

using Sharpen;

namespace Test4giis.Tdrules.Client.Rdb
{
	public class Base
	{
		protected internal static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(Base));

		protected internal static readonly string Platform = Parameters.GetPlatformName();

		private static readonly string SetupPath = FileUtil.GetPath(Parameters.GetProjectRoot(), "..", "setup");

		private static readonly string EnvironmentProperties = FileUtil.GetPath(SetupPath, "environment.properties");

		private static readonly string DatabaseProperties = FileUtil.GetPath(SetupPath, "database.properties");

		protected internal const string TestDbname = "tdclirdb";

		protected internal static string TestPathBenchmark = Parameters.IsJava() ? "src/test/resources" : FileUtil.GetPath(Parameters.GetProjectRoot(), "../tdrules-client-rdb/src/test/resources");

		
		

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
		protected internal virtual Connection GetConnection(string dbmsVendor, string database)
		{
			log.Debug("Create connection to '{}' database", dbmsVendor);
			string propPrefix = "tdrules." + Platform + "." + TestDbname + "." + dbmsVendor;
			return DriverManager.GetConnection(new JdbcProperties().GetProp(DatabaseProperties, propPrefix + ".url"), new JdbcProperties().GetProp(DatabaseProperties, propPrefix + ".user"), new JdbcProperties().GetEnvVar(EnvironmentProperties, "TEST_" + dbmsVendor.ToUpper() + "_PWD"));
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
	}
}
