//This is part of CORE.Net, don't edit outside this solution
using System;
using System.Data.Common;

namespace Java.Sql
{
    /**
     * Implementacion parcial de clase java.sql para compatibilidad con codigo traducido de Java a C#;
     * Wrapper de una Connection a partir de una conexion nativa DbConnection
     * que implementa la lectura de datos devolviendo un ResultSet
     */
    public class Connection
    {
        private readonly DbConnection Conn;
        public Connection(DbConnection NetConnection)
        {
            this.Conn = NetConnection;
        }
        public void Close()
        {
            this.Conn.Close();
        }
        public DatabaseMetaData GetMetaData()
        {
            Connection jdbcConn = new Connection(this.Conn);
            //La determinacion del tipo de conexion se puede realizar a partir de la clase del objeto
            //pero no siempre se tendra instalada la libreria, por lo que se hace de forma aproximada a partir
            //del nombre de la clase
            String dbname = Conn.GetType().ToString().ToLower();
            if (dbname.Contains("sqlclient"))
                return DatabaseMetaDataFactory.GetMetaData("sqlserver", new Connection(this.Conn));
            else if (dbname.Contains("sqlite"))
                return DatabaseMetaDataFactory.GetMetaData("sqlite", new Connection(this.Conn));
            else if (dbname.Contains("oracle"))
                return DatabaseMetaDataFactory.GetMetaData("oracle", new Connection(this.Conn));
            return new DatabaseMetaData(jdbcConn);
        }
        public DbConnection GetDbConnection()
        {
            return this.Conn;
        }
        public string GetDriverName()
        {
            //si no, es el nombre de la clase (incluyendo el paquete)
            return Conn.GetType().FullName;
        }
        public virtual Statement CreateStatement()
        {
            return new Statement(this.Conn);
        }
        /** 
         * Forma corta de ejecutar una consulta para obtener un DbDataReader (no portable con Java).
         * Alternativamente se puede usar una Statement para obtener un ResultSet como en java (para consultas y actualizaciones)
         */
        public virtual DbDataReader ExecuteQuery(string sql)
        {
            try
            {
                DbCommand command = this.Conn.CreateCommand();
                command.CommandText = sql;
                return command.ExecuteReader();

            }
            catch (Exception e)
            {
                throw new SQLException(e.Message);
            }
        }
        public virtual void SetTransactionIsolation(int level)
        {
            //no implementa, solo para compatibilidad
        }

    }
}
