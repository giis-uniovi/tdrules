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
    public class Statement
    {
        private readonly DbConnection Conn;

        public Statement(DbConnection NetConnection)
        {
            this.Conn = NetConnection;
        }
        public virtual void SetMaxRows(int maxRows)
        {
            //No implementado, existe esta posibilidad?
        }
        public virtual ResultSet ExecuteQuery(string sql)
        {
            try
            {
                using (DbCommand command=this.Conn.CreateCommand())
                 {
                    command.CommandText = sql;
                    
                    return new ResultSet(command.ExecuteReader());
                }
            }
            catch (Exception e)
            {
                throw new SQLException(e.Message);
            }
        }
        public virtual void ExecuteUpdate(string sql)
        {
            try
            {
                using (DbCommand command = this.Conn.CreateCommand())
                {
                    command.CommandText = sql;
                    command.ExecuteNonQuery();
                }
            }
            catch (Exception e)
            {
                throw new SQLException(e.Message);
            }
        }
        public virtual void Close()
        {
            //solo para compatibilidad
        }
        public virtual bool IsClosed()
        {
            return true; //solo para compatibilidad
        }
    }
}
