//This is part of CORE.Net, don't edit outside this solution
using System.Data.Common;
namespace Java.Sql
{
    /**
     * Metodos específicos de DatabaseMetaData para Oracle.
     */
    public class DatabaseMetaDataOracle : DatabaseMetaData
    {
        public DatabaseMetaDataOracle(Connection JdbcConnection) : base(JdbcConnection)
        {
        }
        public override string GetDatabaseProductName()
        {
            return "Oracle";
        }
        public override string GetDatabaseProductVersion()
        {
            string Sql = "select VERSION FROM V$INSTANCE";
            using (DbDataReader Dr = this.Conn.ExecuteQuery(Sql))
            {
                Dr.Read();
                return Dr.GetString(0);
            }
        }
        protected override string GetAutoincrementColumn(string tableName)
        {
            return "'NO'";
        }
        public override bool StoresUpperCaseIdentifiers()
        {
            return true;
        }
    }
}
