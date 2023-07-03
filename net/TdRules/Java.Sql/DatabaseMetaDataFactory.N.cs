//This is part of CORE.Net, don't edit outside this solution
namespace Java.Sql
{
    /**
     * Factoria para crear los objetos DatabaseMetaData para el DBMS especifico (indicado como un string) 
     */
    public static class DatabaseMetaDataFactory
    {
        public static DatabaseMetaData GetMetaData(string dbms, Connection conn)
        {
            //para los sgbd en en los que hay una subclase devuelve la instancia correspondiente
            if ("sqlserver".Equals(dbms))
                return new DatabaseMetaDataSqlServer(conn);
            if ("oracle".Equals(dbms))
                return new DatabaseMetaDataOracle(conn);
            if ("sqlite".Equals(dbms))
                return new DatabaseMetaDataSqlite(conn);
            //Por defecto, devuelve la instancia general
            return new DatabaseMetaData(conn);
        }
    }
}
