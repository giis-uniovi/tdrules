//This is part of CORE.Net, don't edit outside this solution
using System.Data.Common;
namespace Java.Sql
{
    /**
     * Metodos específicos de DatabaseMetaData para SQL Server
     */
    public class DatabaseMetaDataSqlServer : DatabaseMetaData
    {
        public DatabaseMetaDataSqlServer(Connection JdbcConnection) : base(JdbcConnection)
        {
        }
        public override string GetDatabaseProductName()
        {
            return "Microsoft SQL Server";
        }
        public override string GetDatabaseProductVersion()
        {
            //https://stackoverflow.com/questions/949852/determine-version-of-sql-server-from-ado-net
            return base.GetDatabaseProductVersion("select CAST(SERVERPROPERTY('productversion') as varchar(32)) + ' ' + CAST(SERVERPROPERTY('productlevel') as varchar(32)) as version");
        }
        protected override string GetAutoincrementColumn(string tableName)
        {
            string sql = "SELECT COUNT(*) FROM sys.columns WHERE object_id = object_id('" + tableName + "') and is_identity=1";
            using (DbDataReader Dr = Conn.ExecuteQuery(sql))
            {
                while (Dr.Read())
                {
                    int Autoincrements = (int)Dr.GetValue(0);
                    if (Autoincrements > 0)
                        return "'YES'";
                }
            }
            return "'NO'";
        }
    }
}
