using Java.Sql;
using Microsoft.Data.SqlClient;
using System.Data.SQLite;
using System.Runtime.CompilerServices;

namespace TdRulesTest.Test4Giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Register the database providers for the tests.
    /// This is done in a separate class with a module initializer to keep the transformed code without changes
    /// </summary>
    public class RegisterProviders
    {
        [ModuleInitializer]
        public static void RegisterDbProviders() {
            DriverManager.RegisterProvider("Microsoft.Data.SqlClient", () => SqlClientFactory.Instance);
            DriverManager.BindProviderUrl("Microsoft.Data.SqlClient", "server", "UID", "PWD");

            DriverManager.RegisterProvider("System.Data.SQLite", () => SQLiteFactory.Instance);
            DriverManager.BindProviderUrl("System.Data.SQLite", "data source", "", "");
         }

    }
}
