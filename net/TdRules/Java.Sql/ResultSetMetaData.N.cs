//This is part of CORE.Net, don't edit outside this solution
using System.Data.Common;
namespace Java.Sql
{
    /**
     * Implementacion parcial de clase java.sql para compatibilidad con codigo traducido de Java a C#;
     * Wrapper de ResultSetMetadata
     * implementa el minimo numero de metodos necesarios para la compatibilidad.
     * Doc de Java: https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSetMetaData.html
     */
    public class ResultSetMetaData
    {
        private readonly DbDataReader Dr;
        public ResultSetMetaData(DbDataReader NetDataReader)
        {
            this.Dr = NetDataReader;
        }
        public int GetColumnCount()
        {
            return Dr.FieldCount;
        }
        public string GetColumnName(int Column)
        {
            throw new System.NotImplementedException("ResultSetMetaDataX not implemented");
        }
        public int GetColumnType(int Column)
        {
            throw new System.NotImplementedException("ResultSetMetaDataX not implemented");
        }
        public string GetColumnTypeName(int Column)
        {
            throw new System.NotImplementedException("ResultSetMetaDataX not implemented");
        }
        public int GetPrecision(int Column)
        {
            throw new System.NotImplementedException("ResultSetMetaDataX not implemented");
        }
        public int GetScale(int Column)
        {
            throw new System.NotImplementedException("ResultSetMetaDataX not implemented");
        }
        public int IsNullable(int Column)
        {
            throw new System.NotImplementedException("ResultSetMetaDataX not implemented");
        }
    }
}
