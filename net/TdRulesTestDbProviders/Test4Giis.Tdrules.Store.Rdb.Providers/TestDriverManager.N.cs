using Giis.Portable.Util;
using Giis.Tdrules.Store.Rdb;
using Java.Sql;
using NUnit.Framework;
using NUnit.Framework.Legacy;
using System.Data.SqlClient;
using System.Data.SQLite;

namespace Test4giis.Tdrules.Store.Rdb.Providers
{
    public class TestDriverManager
    {
        private static string dbProps = FileUtil.GetPath(Parameters.GetProjectRoot(), "..", "setup", "database.properties");
        private static string envProps = FileUtil.GetPath(Parameters.GetProjectRoot(), "..", "setup", "environment.properties");

        private static string sqliteUrl = new JdbcProperties().GetProp(dbProps, "tdrules.netcore.tdstorerdb2.sqlite.url");
        private static string sqlserverUrl = new JdbcProperties().GetProp(dbProps, "tdrules.netcore.tdstorerdb2.sqlserver.url");
        private static string sqlserverUser = new JdbcProperties().GetProp(dbProps, "tdrules.netcore.tdstorerdb2.sqlserver.user");
        private static string sqlserverPass = new JdbcProperties().GetEnvVar(envProps, "TEST_SQLSERVER_PWD");
        private static string sqlserverUrlAuth = sqlserverUrl + ";UID=" + sqlserverUser + ";PWD=" + sqlserverPass;

        private static string sqlserverProviderName = "System.Data.SqlClient";
        private static string sqliteProviderName = "System.Data.SQLite";

        private Connection conn; // connection to be used in the tests

        [SetUp]
        public void SetUp()
        {
            DriverManager.DeregisterProviders();
        }
        [TearDown]
        public void TearDown()
        {
            conn?.Close();
            conn = null;
        }

        [Test]
        public void TestRegisterSingleProvider()
        {
            DriverManager.RegisterProvider(sqliteProviderName, () => SQLiteFactory.Instance);
            conn = DriverManager.GetConnection(sqliteUrl);
            ClassicAssert.AreEqual("SQLite", conn.GetMetaData().GetDatabaseProductName());

            // Clear providers to check another one
            DriverManager.DeregisterProviders(); 
            DriverManager.RegisterProvider(sqlserverProviderName, () => SqlClientFactory.Instance);
            conn = DriverManager.GetConnection(sqlserverUrlAuth);
            ClassicAssert.AreEqual("Microsoft SQL Server", conn.GetMetaData().GetDatabaseProductName());
        }

        [Test]
        public void TestRegisterProviderAgain()
        {
            DriverManager.RegisterProvider(sqliteProviderName, () => SQLiteFactory.Instance);
            conn = DriverManager.GetConnection(sqliteUrl);
            ClassicAssert.AreEqual("SQLite", conn.GetMetaData().GetDatabaseProductName());
            conn.Close();

            // The new provider info (sqlserver) should replace the old one (sqlite)
            // even the name of the provider is still sqlite
            DriverManager.RegisterProvider(sqliteProviderName, () => SqlClientFactory.Instance);
            conn = DriverManager.GetConnection(sqlserverUrlAuth);
            ClassicAssert.AreEqual("Microsoft SQL Server", conn.GetMetaData().GetDatabaseProductName());
        }

        [Test]
        public void TestRegisterMultipleProviders()
        {
            // Uses the first registered provider
            DriverManager.RegisterProvider(sqliteProviderName, () => SQLiteFactory.Instance);
            DriverManager.RegisterProvider(sqlserverProviderName, () => SqlClientFactory.Instance);
            conn = DriverManager.GetConnection(sqliteUrl); // uses the first registered provider
            ClassicAssert.AreEqual("SQLite", conn.GetMetaData().GetDatabaseProductName());
        }

        [Test]
        public void TestConnectionWithNoProvidersThrowsJavaSqlException()
        {
            SQLException e = Assert.Throws<SQLException>(() =>
            {
                DriverManager.GetConnection(sqliteUrl);
            });
            ClassicAssert.AreEqual("No providers registered. Example: " + DriverManager.registerExample, e.Message);
        }

        [Test]
        public void TestBindCredentialKeys()
        {
            // Using the url without credentials and binding adds credentials to the url
            DriverManager.RegisterProvider(sqlserverProviderName, () => SqlClientFactory.Instance);
            DriverManager.BindProviderUrl(sqlserverProviderName, "", "UID", "PWD");
            conn = DriverManager.GetConnection(sqlserverUrl, sqlserverUser, sqlserverPass);
            ClassicAssert.AreEqual("Microsoft SQL Server", conn.GetMetaData().GetDatabaseProductName());

            // Exception raised by provider if not credentials set when getting connection
            // Note that exception is the ado.net sql exception, not java sql exception
            DriverManager.DeregisterProviders();
            DriverManager.RegisterProvider(sqlserverProviderName, () => SqlClientFactory.Instance);
            DriverManager.BindProviderUrl(sqlserverProviderName, "", "UID", "PWD");
            Assert.Throws<SqlException>(() =>
            {
                DriverManager.GetConnection(sqlserverUrl);
            });
        }

        [Test]
        public void TestBindUrlPrefix()
        {
            DriverManager.RegisterProvider(sqliteProviderName, () => SQLiteFactory.Instance);
            DriverManager.RegisterProvider(sqlserverProviderName, () => SqlClientFactory.Instance);
            DriverManager.BindProviderUrl(sqliteProviderName, "data source", "", "");
            DriverManager.BindProviderUrl(sqlserverProviderName, "server", "UID", "PWD");

            // Finds the appropriate provider that matches the url prefix
            conn = DriverManager.GetConnection(sqlserverUrl, sqlserverUser, sqlserverPass);
            ClassicAssert.AreEqual("Microsoft SQL Server", conn.GetMetaData().GetDatabaseProductName());
            conn.Close();
            conn = DriverManager.GetConnection(sqliteUrl);
            ClassicAssert.AreEqual("SQLite", conn.GetMetaData().GetDatabaseProductName());
        }

        [Test]
        public void TestBindPreservedIfRegisterAgain()
        {
            DriverManager.RegisterProvider(sqliteProviderName, () => SQLiteFactory.Instance);
            DriverManager.RegisterProvider(sqlserverProviderName, () => SqlClientFactory.Instance);
            DriverManager.BindProviderUrl(sqliteProviderName, "data source", "", "");
            DriverManager.BindProviderUrl(sqlserverProviderName, "server", "UID", "PWD");

            // A previous test check that registering again is able to change the provider,
            // No checks that the bind information is preserved
            DriverManager.RegisterProvider(sqlserverProviderName, () => SqlClientFactory.Instance);
            conn = DriverManager.GetConnection(sqlserverUrl, sqlserverUser, sqlserverPass);
            ClassicAssert.AreEqual("Microsoft SQL Server", conn.GetMetaData().GetDatabaseProductName());
        }

        [Test]
        public void TestBindUnregisteredProviderThrowsJavaSqlException()
        {
            DriverManager.RegisterProvider(sqliteProviderName, () => SQLiteFactory.Instance);
            SQLException e = Assert.Throws<SQLException>(() =>
            {
                DriverManager.BindProviderUrl("Other.Provider", "data source", "", "");
            });
            ClassicAssert.AreEqual("Provider Other.Provider not registered. Example: " + DriverManager.registerExample, e.Message);
        }

    }
}
