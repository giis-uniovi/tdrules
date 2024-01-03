using System.Data.Common;

namespace Java.Sql
{
    public static class DriverManager
    {
        /// <summary>
        /// Obtiene una conexion a partir de la cadena de conexion ADO NET, solo para sqlserver y sqlite
        /// Ejemplo de cadena de conexion sqlserver: Server=(local);Database=xxxx 
        /// Ejemplo de cadena de conexion sqlite: Data Source=xxxx.db 
        /// </summary>
        public static Connection GetConnection(string url, string user, string password)
        {
            DbConnection nativeConn;
            if (url.ToLower().StartsWith("server"))
                nativeConn = new System.Data.SqlClient.SqlConnection(url
                    + (string.IsNullOrEmpty(user) ? "" : ";UID=" + user)
                    + (string.IsNullOrEmpty(password) ? "" : ";PWD=" + password)
                    + ";MultipleActiveResultSets=true");
            else if (url.ToLower().StartsWith("data source"))
                nativeConn = SqliteConnectionFactory.GetDbConnection(url);
            else
                throw new SQLException("Unrecognized connection string, it should start with 'Server' (SqlServer) or 'Data Source' (Sqlite), but was " + url);
            nativeConn.Open();
            return new Connection(nativeConn);
        }
        public static Connection GetConnection(string url)
        {
            return GetConnection(url, "", "");
        }

    }
}
