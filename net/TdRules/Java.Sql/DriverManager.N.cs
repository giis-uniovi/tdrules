using System;
using System.Collections.Generic;
using System.Data.Common;

namespace Java.Sql
{
    /// <summary>
    /// Basic service to create a Java.Sql.SQLConnection connection to a database through an ADO.NET provider.
    /// 
    /// As .NET connection strings do not allow to identify the DB provider, the client application
    /// has to register the provider before using this service (see RegisterProvider).
    /// 
    /// An additional method (see BindProviderUrl) allows to set an association from connection strings
    /// and the actual provider to use.
    /// </summary>
    public static class DriverManager
    {
        private static List<Provider> providers;
        public static string registerExample = "DriverManager.RegisterProvider(\"Microsoft.Data.SqlClient\", () => SqlClientFactory.Instance)";
        private class Provider
        {
            public string ProviderName { get; set; }
            public Func<DbProviderFactory> ProviderFactory { get; set; }
            public string UrlPrefix { get; set; }
            public string UsernameKey { get; set; }
            public string PasswordKey { get; set; }

            public string TransformUrlWithCredentials(string url, string username, string password)
            {
                if (!string.IsNullOrEmpty(UsernameKey) && !string.IsNullOrEmpty(username))
                    url += ";" + UsernameKey + "=" + username;
                if (!string.IsNullOrEmpty(PasswordKey) && !string.IsNullOrEmpty(password))
                    url += ";" + PasswordKey + "=" + password;
                return url;
            }
        }

        /// <summary>
        /// Register the DB provider to be used by the DriverManager to create connections.
        /// Example: DriverManager.RegisterProvider("Microsoft.Data.SqlClient", () => SqlClientFactory.Instance)
        /// 
        /// Registration can be done e.g. in a static block or in a ModuleInitializer.
        /// By default (unless BindProviderUrl is called) the first registered provider is always be used.
        /// </summary>
        public static void RegisterProvider(string providerName, Func<DbProviderFactory> providerFactory)
        {
            EnsureInitialized();
            Provider provider = GetProviderOrNull(providerName);
            if (provider == null)
            {
                provider = new Provider();
                providers.Add(provider);
            }
            // set or overwrite provider name and factory
            provider.ProviderName = providerName;
            provider.ProviderFactory = providerFactory;
        }

        /// <summary>
        /// Binds a DB provider name to a connection string. There are kinds of bindings:
        /// - urlPrefix: If the connection string starts with this prefix, the bound provider will be used
        ///   to create connections even if it is not the first registered provider.
        /// - usernameKey/passwordKey: ADO.NET providers have different syntax to specify the keys that indicate
        ///   the username/password values in the connection strings. If the any of these keys are 
        ///   specified,  the pair key/value to set username and/or password will be added to the connection string.
        /// </summary>
        public static void BindProviderUrl(string providerName, string urlPrefix, string usernameKey, string passwordKey)
        {
            EnsureInitialized();
            Provider provider = GetProviderOrNull(providerName);
            if (provider == null)
                throw new SQLException("Provider " + providerName + " not registered. Example: " + registerExample);
            provider.UrlPrefix = urlPrefix;
            provider.UsernameKey = usernameKey;
            provider.PasswordKey = passwordKey;
        }

        private static Provider GetProviderOrNull(string providerName)
        {
            foreach (var provider in providers)
                if (provider.ProviderName == providerName)
                    return provider;
            return null;
        }

        private static Provider GetProvider(string url)
        {
            if (providers.Count == 0)
                throw new SQLException("No providers registered. Example: " + registerExample);

            // Returns provider such that the url starts with its registered url prefix (if any)
            foreach (var p in providers)
                if (!string.IsNullOrEmpty(p.UrlPrefix) && url.StartsWith(p.UrlPrefix, StringComparison.OrdinalIgnoreCase))
                    return p;

            // Default to the first registered provider
            return providers[0]; 
        }

        private static void EnsureInitialized()
        {
            if (providers == null)
                providers = new List<Provider>();
        }


        /// <summary>
        /// Removes all registered providers.
        /// </summary>
        public static void DeregisterProviders()
        {
            providers = null;
        }

        /// <summary>
        /// Creates a connection to the database with a connection string and the 
        /// user and password specified as parameter (provided that the 
        /// username and password keys have been bound to the connection string).
        /// <returns></returns>
        public static Connection GetConnection(string url, string user, string password)
        {
            EnsureInitialized();
            Provider provider = GetProvider(url);
            url = provider.TransformUrlWithCredentials(url, user, password);
            DbConnection nativeConn = provider.ProviderFactory().CreateConnection();
            nativeConn.ConnectionString = url;
            nativeConn.Open();
            return new Connection(nativeConn);
        }

        /// <summary>
        /// Creates a connection to the database with a connection string.
        /// <returns></returns>
        public static Connection GetConnection(string url)
        {
            return GetConnection(url, "", "");
        }

    }
}
