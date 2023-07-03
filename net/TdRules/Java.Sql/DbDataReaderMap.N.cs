//This is part of CORE.Net, don't edit outside this solution
using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Data.Common;

namespace Java.Sql
{
    /**
     * Wrapper de una estructura de datos en un DbDataReader que se instancia a partir de:
     * (a) Una estructura de entrada como lista de arrays con filas y columnas
     * (b) Un mapeo entre las columnas de esta estructura de datos y las que obtendra este DbDataReader.
     * 
     * El mapeo se define en una estructura (array de arrays) cuyas columnas contienen:
     * (0) Ordinal cada columna que sera la salida de este DbDataReader (deben ser consecutivos empezando en 0).
     * (1) Nombre de la columna.
     * (2) Ordinal de la columna de entrada (-1 si no existe columna correspondiente).
     * (3) Nombre de la columna de entrada (null si no existe columna correspondiente).
     * (4) Valor constante a incluir cuando no existe columna correspondiente (null en caso contrario).
     * 
     * Solamente implementa el minimo de clases del interfaz necesarias para el uso en las implementaciones de DatabaseMetaData
     * en SQLite (ver mas documentacion en DatabaseMetaDataSqlite)
     */
    public class DbDataReaderMap : DbDataReader
    {
        //columnas de la estructura que especifica el mapeo entre columnas (ver DatabaseMetaDataSqlite)
        protected const int DEST_ORDINAL = 0;
        protected const int DEST_NAME = 1;
        protected const int SRC_ORDINAL = 2;
        protected const int SRC_NAME = 3;
        protected const int SRC_VALUE = 4;

        //Estructura con los datos de entrada que seran mapeados a columnas del DbDataReader
        protected readonly IList<object[]> Wrapped;
        protected int currentPosition = -1;
        //Estructura el mapeo de columnas
        private readonly object[][] ColumnMap;
        //acceso al ordinal de una columna a partir de su nombre
        protected readonly IDictionary<string, int> OrdinalMap = new SortedDictionary<string, int>();

        public DbDataReaderMap(IList<object[]> WrappedData, object[][] ColumnMapping)
        {
            this.Wrapped = WrappedData;
            //establece el acceso al ordinal de columnas a partir del nombre
            for (int i = 0; i < ColumnMapping.Length; i++)
                OrdinalMap.Add((string)ColumnMapping[i][DEST_NAME], (int)ColumnMapping[i][DEST_ORDINAL]);
            this.ColumnMap = ColumnMapping;
        }

        public override object this[int ordinal] => throw new NotImplementedException();

        public override object this[string name] => throw new NotImplementedException();

        public override int Depth => throw new NotImplementedException();

        public override int FieldCount
        {
            get { return (this.Wrapped.Count>0 ? this.Wrapped[0].Length : 0);  }
        }

        public override bool HasRows => throw new NotImplementedException();

        public override bool IsClosed => throw new NotImplementedException();

        public override int RecordsAffected => throw new NotImplementedException();

#if NETCORE
#else
        public override void Close()
        {
        }
#endif

        public override bool GetBoolean(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override byte GetByte(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override long GetBytes(int ordinal, long dataOffset, byte[] buffer, int bufferOffset, int length)
        {
            throw new NotImplementedException();
        }

        public override char GetChar(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override long GetChars(int ordinal, long dataOffset, char[] buffer, int bufferOffset, int length)
        {
            throw new NotImplementedException();
        }

        public override string GetDataTypeName(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override DateTime GetDateTime(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override decimal GetDecimal(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override double GetDouble(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override IEnumerator GetEnumerator()
        {
            throw new NotImplementedException();
        }

        public override Type GetFieldType(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override float GetFloat(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override Guid GetGuid(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override short GetInt16(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override int GetInt32(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override long GetInt64(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override string GetName(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override int GetOrdinal(string name)
        {
            return this.OrdinalMap[name];
        }

#if NETCORE
#else
        public override DataTable GetSchemaTable()
        {
            throw new NotImplementedException();
        }
#endif

        public override string GetString(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override object GetValue(int ordinal)
        {
           //si no columna correspondiente en SRC_ORDINAL se devuelve un valor constante SRC_VALUE
           if ((int)this.ColumnMap[ordinal][SRC_ORDINAL] <0)
                return this.ColumnMap[ordinal][SRC_VALUE];
           else //devuelve el valor que se tenga en la estructura con las filas/columnas origen
           {
                int column = (int)(this.ColumnMap[ordinal][SRC_ORDINAL]);
                return this.Wrapped[currentPosition][column];
           }
        }

        public override int GetValues(object[] values)
        {
            throw new NotImplementedException();
        }

        public override bool IsDBNull(int ordinal)
        {
            throw new NotImplementedException();
        }

        public override bool NextResult()
        {
            throw new NotImplementedException();
        }

        public override bool Read()
        {
            this.currentPosition++;
            return this.currentPosition < this.Wrapped.Count;
        }
    }
}
