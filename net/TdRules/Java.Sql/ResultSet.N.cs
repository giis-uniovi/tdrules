//This is part of CORE.Net, don't edit outside this solution
using System;
using System.Data.Common;

namespace Java.Sql
{
    /**
     * Implementacion parcial de clase java.sql para compatibilidad con codigo traducido de Java a C#;
     * Wrapper de un ResultSet usando un DbDataReader, 
     * implementa el minimo numero de metodos necesarios para la compatibilidad.
     */
    public class ResultSet
    {
        //Objeto DataSet que es manejado por esta clase
        private readonly DbDataReader Dr;
        //Indica si el ultimo campo leido fue nulo
        private bool LastFieldWasNull = false;

        public ResultSet(DbDataReader DataReader)
        {
            this.Dr = DataReader;
        }
        public Statement GetStatement()
        {
            return null; //la implementacion interna no guarda statements o algo similar, usar solo en cierres de resultsets controlados
        }
        public ResultSetMetaData GetMetaData()
        {
            return new ResultSetMetaData(this.Dr);
        }
        public void Dispose() // Java2CSharp converts close into dispose
        {
            this.Dr.Dispose();
        }
        public void Close()
        {
#if NETCORE
            this.Dr.Dispose();
#else
            this.Dr.Close();
#endif
        }
        public Boolean IsClosed()
        {
            return this.Dr.IsClosed;
        }
        public bool Next()
        {
            return this.Dr.Read();
        }
        public bool WasNull()
        {
            return this.LastFieldWasNull;
        }
        private object GetCellValue(string field)
        {
            try
            {
                int ordinal = Dr.GetOrdinal(field);
                //ordinal siempre devuelve a partir de 0, pero luego getCellValue requiere empezar desde 1
                return GetCellValue(ordinal+1);
            }
            catch (System.IndexOutOfRangeException)
            {
                Dr.Dispose();
                throw new SQLException("Nombre de columna no válido");
            }
        }
        private object GetCellValue(int col)
        {
            try
            {
                //El nulo puede ser por leer un DBNull 
                //o por una variable nula cuando el resultset es unwrap de una estructura de datos
                object obj = Dr.GetValue(col-1);
                this.LastFieldWasNull = obj is DBNull || obj==null;
                return obj;
            }
            catch (System.IndexOutOfRangeException)
            {
                Dr.Dispose();
                throw new SQLException("Índice de columnas no válido");
            }
            catch (System.InvalidOperationException)
            {
                Dr.Dispose();
                throw new SQLException("Posición de cursor no válida");
            }
        }
        public string GetString(string field)
        {
            object valueObj = GetCellValue(field);
            if (this.LastFieldWasNull)
                return null;
            return valueObj.ToString();
        }
        public string GetString(int col)
        {
            string Value = GetCellValue(col).ToString();
            if (this.LastFieldWasNull)
                return null;
            return Value;
        }
        public int GetInt(string field)
        {
            object Value=GetCellValue(field);
            if (this.LastFieldWasNull)
                return 0;
            return Convert.ToInt32(Value);
        }
        public int GetInt(int col)
        {
            object Value = GetCellValue(col);
            if (this.LastFieldWasNull)
                return 0;
            return Convert.ToInt32(Value);
        }
        public long GetLong(string field)
        {
            object Value = GetCellValue(field);
            if (this.LastFieldWasNull)
                return 0;
            return Convert.ToInt64(Value);
        }
        public long GetLong(int col)
        {
            object Value = GetCellValue(col);
            if (this.LastFieldWasNull)
                return 0;
            return Convert.ToInt64(Value);
        }
        public bool GetBoolean(string field)
        {
            object Value = GetCellValue(field);
            if (this.LastFieldWasNull)
                return false;
            return (bool)Value;
        }
        public bool GetBoolean(int col)
        {
            object Value = GetCellValue(col);
            if (this.LastFieldWasNull)
                return false;
            return (bool)Value;
        }
        public DateTime GetDate(string field)
        {
            object Value = GetCellValue(field);
            if (this.LastFieldWasNull)
                return DateTime.MinValue; //datetime no es nullable
            return (DateTime)Value;
        }
        public DateTime GetDate(int col)
        {
            object Value = GetCellValue(col);
            if (this.LastFieldWasNull)
                return DateTime.MinValue; //DateTime no es nullable
            return (DateTime)Value;
        }

    }
}
