//This is part of CORE.Net, don't edit outside this solution
using System.Text;
using System.Collections.Generic;
using System.Data.Common;
namespace Java.Sql
{
    /**
     * Metodos específicos de DatabaseMetaData para SQLite.
     * Como SQLite no soporta INFORMATION_SCHEMA la informacion se obtiene a partir de sentencias sql personalizadas
     * o usando PRAGMA (p.e. para obtener columnas y claves).
     * Las columnas devueltas por PRAGMA no permiten ser renombradas ni embebidas en ninguna consulta sql,
     * por lo que realiza estas guardando los resultados en una estructura (lista de arrays).
     * Esta estructura es usada en el constructor de un DbDataReader personalizado (DbDataReaderMap)
     * que es el que realiza el mapeo entre las columnas devueltas por PRAGMA y las que
     * deberian ser obtenidas de acuerdo con INFORMATION_SCHEMA (de acuerdo con DatabaseMetaData).
     */
    public class DatabaseMetaDataSqlite : DatabaseMetaData
    {
        public DatabaseMetaDataSqlite(Connection JdbcConnection) : base(JdbcConnection)
        {
        }

        public override string GetDatabaseProductName()
        {
            return "SQLite";
        }
        public override string GetDatabaseProductVersion()
        {
            return base.GetDatabaseProductVersion("select sqlite_version() as version");
         }

        /**
         * Convierte una sentencia Sql resultado de ejecutar un PRAGMA en una lista de arrays
         * que contiene todos los datos que seran usados en el constructor del DbDataReaderMap.
         * Dos parametros adicionales controlan operaciones especificas para transformar los datos
         * para que se correspondan con los requeridos por INFORMATION_SCHEMA
         */
        protected IList<object[]> QueryToList(string Sql, bool KeySeq5NoZero, bool InvertNullable3)
        {
            using (DbDataReader Dr=this.Conn.ExecuteQuery(Sql))
            {
                IList<object[]> rows = new List<object[]>();
                while (Dr.Read())
                {
                    //todos los datos a la lista de arays
                    object[] row = new object[Dr.FieldCount];
                    Dr.GetValues(row);
                    //invierte el valor de columna notnull [3] para que sea nullable (si InvertNullable3=true)
                    if (row[3].ToString().Equals("0"))
                        row[3] = 1;
                    else if (row[3].ToString().Equals("1"))
                        row[3] = 0;
                    //anyade fila salvo para valores cero de clave [5] (si KeySeq5NoZero=true)
                    if (!(KeySeq5NoZero && row[5].ToString().Equals("0")))
                        rows.Add(row);
                }
                return rows;
            }
        }

        public override ResultSet GetPrimaryKeys(string Catalog, string Schema, string Table)
        {
            //Obtiene los datos con el formato nativo de Sqlite (usando PRAGMA)
            IList<object[]> Dbr= QueryToList("PRAGMA table_info(\"" + Table + "\")",true,false);
            //Mapeo de las columnas de INFORMATION_SCHEMA requeridas por el metodo jdbc a las anteriores (ver comentario al principio de esta clase)
            object[][] keysMap = { //mapeo de columnas para obtener primary keys
                new object[]{ 0, "TABLE_CAT", -1, null, null },
                new object[]{ 1, "TABLE_SCHEM", -1, null, null },
                new object[]{ 2, "TABLE_NAME", -1, null, Table },
                new object[]{ 3, "COLUMN_NAME", 1, "name", null },
                new object[]{ 4, "KEY_SEQ", 5, "pk", null },
                new object[]{ 5, "PK_NAME", -1, null, "" },
            };
            //Wrap de estos datos en el DbDataReaderMap
            DbDataReader DbrCustom = new DbDataReaderMap(Dbr, keysMap);
            return new ResultSet(DbrCustom);
        }
        public override ResultSet GetColumns(string Catalog, string SchemaPattern, string TableNamePattern, string columnNamePattern)
        {
            //Primero determina si hay claves autoincrementales, este valor se incluira en el mapeo de columnas
            string autoincrement = GetAutoincrementColumn(TableNamePattern);
            object[][] columnMap = { //mapeo de columnas para obtener todas las columnas de una tabla
                new object[]{ 0, "TABLE_CAT", -1, null, null },
                new object[]{ 1, "TABLE_SCHEM", -1, null, null },
                new object[]{ 2, "TABLE_NAME", -1, null, TableNamePattern },
                new object[]{ 3, "COLUMN_NAME", 1, "name", null },
                new object[]{ 4, "DATA_TYPE", -1, null, -1 },
                new object[]{ 5, "TYPE_NAME", 2, "type", null }, //ojo, este incluye tipos como varchar(5), habria que descomponer esto en el tipo,precision y escala
                new object[]{ 6, "COLUMN_SIZE", -1, null, 0 },
                new object[]{ 7, "DECIMAL_DIGITS", -1, null, 0 },
                new object[]{ 8, "NULLABLE", 3, "notnull", null }, //esto hay que cambiarlo para que sea nullable
                new object[]{ 9, "COLUMN_DEF", -1, null, null },
                new object[]{ 10, "IS_AUTOINCREMENT", -1, null, autoincrement },
            };
            //Mismo proceso que en getPrimaryKeys.
            //Nota: solo servira para una tabla y todas sus columnas
            IList<object[]> Dbr = QueryToList("PRAGMA table_info(\"" + TableNamePattern + "\")",false,true);
            DbDataReader DbrCustom = new DbDataReaderMap(Dbr, columnMap);
            return new ResultSet(DbrCustom);
        }

        protected override string GetAutoincrementColumn(string tableName)
        {
            //lo mejor seria usar la query  SELECT * FROM sqlite_sequence WHERE name='...'
            //pero desde el datareader no siempre devuelve filas aunque haya una secuencia
            //si se ve p.e. con DBBrowser for Sqlite.
            //Uso otro enfoque (menos fiable si hay columnas que contienen el string)
            string sql = "select * from sqlite_master where type = 'table' and name='" + tableName + "' and lower(sql) like '% autoincrement%'";
            using (DbDataReader Dr = Conn.ExecuteQuery(sql))
            {
                if (Dr.HasRows) //si hay una fila, es que hay columna autoincremental
                    return "YES";
            }
            return "NO";
        }

        public override ResultSet GetTables(string Catalog, string SchemaPattern, string TableNamePattern, string[] Types)
        {
            //para este caso si existe una query que se pueda utilizar
            string Sql = "select null AS TABLE_CAT, null AS TABLE_SCHEM, name AS TABLE_NAME,"
                + " UPPER(type) as TABLE_TYPE"
                + " from sqlite_master"
                + " where name like '" + TableNamePattern + "'";
            if (Types!=null && Types.Length>0)
            {
                StringBuilder TypesList = new StringBuilder();
                for (int i = 0; i < Types.Length; i++)
                    TypesList.Append((i==0?"":",") + "'" + Types[i].ToLowerInvariant() + "'");
                Sql += " and type in(" + TypesList.ToString() + ")";
            }
            return new ResultSet(Conn.ExecuteQuery(Sql));
        }

        //No implementa obtencion de claves ajenas, pero el metodo existe para que no falle SchemaReaderJdbc

        public override ResultSet GetImportedKeys(string Catalog, string Schema, string Table)
        {
            return new ResultSet(new DbDataReaderMap(new List<object[]>(), new object[0][]));
        }
        public override ResultSet GetExportedKeys(string Catalog, string Schema, string Table)
        {
            return new ResultSet(new DbDataReaderMap(new List<object[]>(), new object[0][]));
        }
    }
}