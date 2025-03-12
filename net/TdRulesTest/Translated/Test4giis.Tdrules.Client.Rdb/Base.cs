using Java.Sql;

using NLog;
using Giis.Portable.Util;
using Giis.Tdrules.Store.Rdb;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Client.Rdb
{
    public class Base
    {
        protected static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(Base));
        protected static readonly string PLATFORM = Parameters.GetPlatformName();
        private static readonly string SETUP_PATH = FileUtil.GetPath(Parameters.GetProjectRoot(), "..", "setup");
        private static readonly string ENVIRONMENT_PROPERTIES = FileUtil.GetPath(SETUP_PATH, "environment.properties");
        private static readonly string DATABASE_PROPERTIES = FileUtil.GetPath(SETUP_PATH, "database.properties");
        protected static readonly string TEST_DBNAME = "tdclirdb";
        protected static string TEST_PATH_BENCHMARK = Parameters.IsJava() ? "src/test/resources" : FileUtil.GetPath(Parameters.GetProjectRoot(), "../tdrules-client-rdb/src/test/resources");
        
        [NUnit.Framework.SetUp]
        public virtual void SetUp()
        {
            log.Info("****** Running test: {} ******", NUnit.Framework.TestContext.CurrentContext.Test.Name);
        }

        /// <summary>
        /// Connection user and url are obtained from a properties file,
        /// password is obtained from environment, if not defined, another properties file is used as fallback
        /// </summary>
        protected virtual Connection GetConnection(string dbmsVendor, string database)
        {
            log.Debug("Create connection to '{}' database", dbmsVendor);
            string propPrefix = "tdrules." + PLATFORM + "." + TEST_DBNAME + "." + dbmsVendor;
            return DriverManager.GetConnection(new JdbcProperties().GetProp(DATABASE_PROPERTIES, propPrefix + ".url"), new JdbcProperties().GetProp(DATABASE_PROPERTIES, propPrefix + ".user"), new JdbcProperties().GetEnvVar(ENVIRONMENT_PROPERTIES, "TEST_" + dbmsVendor.ToUpper() + "_PWD"));
        }

        protected virtual void Execute(Connection dbt, string sql)
        {
            Statement stmt = dbt.CreateStatement();
            stmt.ExecuteUpdate(sql);
        }

        protected virtual void ExecuteNotThrow(Connection dbt, string sql)
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