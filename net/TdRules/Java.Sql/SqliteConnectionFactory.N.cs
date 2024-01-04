//This is part of CORE.Net, don't edit outside this solution
using System.Data.Common;

namespace Java.Sql
{
    internal class SqliteConnectionFactory
    {
        public static DbConnection GetDbConnection(string url)
        {
            // Microsoft.Data.SQLite solo es soportado hasta v2.0.1
            // Versiones superiores causan errores que indican qel DataReader esta cerrado
            // al manejar resultsets
            // return new Microsoft.Data.Sqlite.SqliteConnection(url);
            return new System.Data.SQLite.SQLiteConnection(url);
        }
    }
}