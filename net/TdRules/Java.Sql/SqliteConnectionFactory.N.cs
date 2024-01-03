//This is part of CORE.Net, don't edit outside this solution
using System.Data.Common;

namespace Java.Sql
{
    internal class SqliteConnectionFactory
    {
        public static DbConnection GetDbConnection(string url)
        {
            return new Microsoft.Data.Sqlite.SqliteConnection(url);
        }
    }
}