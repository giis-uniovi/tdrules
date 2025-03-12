//This is part of CORE.Net, don't edit outside this solution
using System.Text;
using System.Data.Common;
namespace Java.Sql
{
    /**
     * Implementacion parcial de clase java.sql para compatibilidad con codigo traducido de Java a C#;
     * Metadatos de una BD obtenidos directamente realizando queries en INFORMATION_SCHEMA.
     * En esta clase se encuentra la implementacion por defecto, pero por las particularidades de los diferentes DBMS
     * sera normalmente necesario sobrescribir algunos metodos utilizando subclases.
     * Los objetos de esta clase se deberan instanciar con la factoria DatabaseMetaDataFactory para incorporar estas particularidades.
     * Doc de Java: https://docs.oracle.com/javase/7/docs/api/java/sql/DatabaseMetaData.html
     * Doc de SqlServer: https://docs.microsoft.com/en-us/sql/relational-databases/system-information-schema-views/system-information-schema-views-transact-sql 
     */
    public class DatabaseMetaData
    {
        public const int columnNullable = 1;

        protected readonly Connection Conn;

        public DatabaseMetaData(Connection JdbcConnection)
        {
            this.Conn = JdbcConnection;
        }

        //Identificacion de la base de datos y versiones, a implementar en las subclases cuando sea necesario

        public virtual string GetDatabaseProductName()
        {
            return "Unknown";
        }
        public virtual int GetDatabaseMajorVersion()
        {
            string dbpv = GetDatabaseProductVersion();
            return int.Parse(dbpv.Split('.')[0]);
        }
        public virtual string GetDatabaseProductVersion()
        {
            return "0.0.0";
        }
        protected string GetDatabaseProductVersion(string Sql)
        {
            using (DbDataReader Dr = this.Conn.ExecuteQuery(Sql))
            {
                Dr.Read();
                return Dr.GetString(0);
            }
        }

        //identificacion del driver (solo el nombre, que obtiene por el tipo de la conexion)

        public virtual string GetDriverName()
        {
            return this.Conn.GetDriverName();
        }
        public virtual string GetDriverVersion()
        {
            return "N/A";
        }

        //Obtencion de informacion de metadatos usando INFORMATION_SCHEMA (comun, salvo que el SGBD no lo sporte)

        public virtual ResultSet GetTables(string Catalog, string SchemaPattern, string TableNamePattern, string[] Types)
        {
            string Sql = "select TABLE_CATALOG AS TABLE_CAT, TABLE_SCHEMA AS TABLE_SCHEM, TABLE_NAME,"
                + " CASE TABLE_TYPE WHEN 'BASE TABLE' THEN 'TABLE' ELSE TABLE_TYPE END as TABLE_TYPE"
                + " from INFORMATION_SCHEMA.TABLES"
                + " where "
                + GetWhereFilter("TABLE_CATALOG", "TABLE_SCHEMA", "TABLE_NAME",
                        Catalog, SchemaPattern, TableNamePattern, Types, null);
            return new ResultSet(Conn.ExecuteQuery(Sql));
        }

        // NOTA: el atributo DATA_TYPE (tipo datos numerico en java.sql.Types) no se obtiene con este metodo (siempre es -1).
        // si se necesitasen habria que traducirlos segun https://docs.oracle.com/javase/6/docs/api/java/sql/Types.html
        // El valor de IS_AUTOINCREMENT toma el mismo valor para todas las columnas, no solo para la 
        // columna que es autoincremental
        public virtual ResultSet GetColumns(string Catalog, string SchemaPattern, string TableNamePattern, string columnNamePattern)
        {
            //Primero determina el valor de columna autoincremental
            //NOTA: En teoria deberia ponerse el valor solo en la columna que es autoincremental
            //pero de momento se pondra YES en todas las columnas si hay alguna autoincremental
            string autoIncrement = GetAutoincrementColumn(TableNamePattern);
            string Sql = "select TABLE_CATALOG AS TABLE_CAT, TABLE_SCHEMA AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, -1 AS DATA_TYPE, DATA_TYPE AS TYPE_NAME,"
                + " CASE WHEN CHARACTER_MAXIMUM_LENGTH IS NOT NULL THEN CHARACTER_MAXIMUM_LENGTH"
                + "   WHEN NUMERIC_PRECISION IS NOT NULL THEN NUMERIC_PRECISION"
                + "   ELSE DATETIME_PRECISION END AS COLUMN_SIZE,"
                + " CASE WHEN NUMERIC_SCALE IS NOT NULL THEN NUMERIC_SCALE ELSE 0 END AS DECIMAL_DIGITS,"
                + " CASE IS_NULLABLE WHEN 'YES' THEN 1 ELSE 0 END AS NULLABLE,"
                + " COLUMN_DEFAULT AS COLUMN_DEF,"
                + " " + autoIncrement + " AS IS_AUTOINCREMENT"
                + " from INFORMATION_SCHEMA.COLUMNS"
                + " where "
                + GetWhereFilter("TABLE_CATALOG", "TABLE_SCHEMA", "TABLE_NAME",
                        Catalog, SchemaPattern, TableNamePattern, null, columnNamePattern)
                + " order by TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME,ORDINAL_POSITION";
            return new ResultSet(Conn.ExecuteQuery(Sql));
        }

        //El campo IS_AUTOINCREMENT de jdbc no existe en information_schema
        //esta funcion debe devolver YES o NO en funcion de que haya columnas autoincrementales o no
        //para que en el select de getColumns ponga el valor adecuado
        //Debe devolver 'YES' o 'NO', string vacio ('') si no se puede determinar
        //Este es el valor por defecto aqui, las subclases deberan poner el valor adecuado
        protected virtual string GetAutoincrementColumn(string tableName)
        {
            return "''";
        }

        public virtual ResultSet GetPrimaryKeys(string Catalog, string Schema, string Table)
        {
            //Se usa KEY_COLUMN_USAGE para buscar las columnas con clave, pero 
            //ahi se encuentran pk, fk y check, filtra con una subquery para aquellas que son primarykeys
            string Sql = "select TABLE_CATALOG AS TABLE_CAT, TABLE_SCHEMA AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME,"
                + " ORDINAL_POSITION AS KEY_SEQ,CONSTRAINT_NAME AS PK_NAME"
                + " from INFORMATION_SCHEMA.KEY_COLUMN_USAGE"
                + " where "
                + GetWhereFilter("TABLE_CATALOG", "TABLE_SCHEMA", "TABLE_NAME",
                        Catalog, Schema, Table, null, null)
                + " and CONSTRAINT_NAME IN (" + GetPrimaryKeysSql(Catalog, Schema, Table) + ")"
                + " order by TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME,KEY_SEQ";
            return new ResultSet(Conn.ExecuteQuery(Sql));
        }

        //obtiene la sql que proporciona los nombres de constraints primary key de la tabla
        private string GetPrimaryKeysSql(string Catalog, string Schema, string Table)
        {
            return "select CONSTRAINT_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS"
                + " where "
                + GetWhereFilter("TABLE_CATALOG", "TABLE_SCHEMA", "TABLE_NAME",
                        Catalog, Schema, Table, null, null)
                + " and CONSTRAINT_TYPE = 'PRIMARY KEY'";
        }
        public virtual ResultSet GetImportedKeys(string Catalog, string Schema, string Table)
        {
            return GetForeignKeys("fk.TABLE_CATALOG", "fk.TABLE_SCHEMA", "fk.TABLE_NAME", Catalog, Schema, Table);
        }
        public virtual ResultSet GetExportedKeys(string Catalog, string Schema, string Table)
        {
            return GetForeignKeys("pk.TABLE_CATALOG", "pk.TABLE_SCHEMA", "pk.TABLE_NAME", Catalog, Schema, Table);
        }
        public virtual ResultSet GetForeignKeys(string InfSchColCaltalog, string InfSchColSchema, string InfSchColTable,
            string Catalog, string Schema, string Table)
        {
            /* Queries para test/depuracion de este metodo
use test4in2testDB2
select * from INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
select * from INFORMATION_SCHEMA.KEY_COLUMN_USAGE
select pk.TABLE_CATALOG as PKTABLE_CAT, pk.TABLE_SCHEMA as PKTABLE_SCHEM, pk.TABLE_NAME as PKTABLE_NAME, pk.COLUMN_NAME AS PKCOLUMN_NAME,
	fk.TABLE_CATALOG as FKTABLE_CAT, fk.TABLE_SCHEMA as FKTABLE_SCHEM, fk.TABLE_NAME as FKTABLE_NAME, FK.COLUMN_NAME  AS FKCOLUMN_NAME, 
	fk.ORDINAL_POSITION as KEY_SEQ, ref.CONSTRAINT_NAME as FK_NAME
  from INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS ref 
  inner join INFORMATION_SCHEMA.KEY_COLUMN_USAGE fk on ref.CONSTRAINT_NAME=fk.CONSTRAINT_NAME
  inner join INFORMATION_SCHEMA.KEY_COLUMN_USAGE pk on ref.UNIQUE_CONSTRAINT_NAME=pk.CONSTRAINT_NAME
  */
            string Sql = "select pk.TABLE_CATALOG as PKTABLE_CAT, pk.TABLE_SCHEMA as PKTABLE_SCHEM, pk.TABLE_NAME as PKTABLE_NAME, pk.COLUMN_NAME AS PKCOLUMN_NAME,"
+ " fk.TABLE_CATALOG as FKTABLE_CAT, fk.TABLE_SCHEMA as FKTABLE_SCHEM, fk.TABLE_NAME as FKTABLE_NAME, FK.COLUMN_NAME AS FKCOLUMN_NAME, "
+ " fk.ORDINAL_POSITION as KEY_SEQ, ref.CONSTRAINT_NAME as FK_NAME"
+ " from INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS ref"
+ " inner join INFORMATION_SCHEMA.KEY_COLUMN_USAGE fk on ref.CONSTRAINT_NAME = fk.CONSTRAINT_NAME"
+ " inner join INFORMATION_SCHEMA.KEY_COLUMN_USAGE pk on ref.UNIQUE_CONSTRAINT_NAME = pk.CONSTRAINT_NAME"
                + " where "
                + GetWhereFilter(InfSchColCaltalog, InfSchColSchema, InfSchColTable,
                        Catalog, Schema, Table, null, null)
                + " order by " + InfSchColCaltalog + "," + InfSchColTable + "," + InfSchColSchema + "," + "KEY_SEQ";
            return new ResultSet(Conn.ExecuteQuery(Sql));
        }
        /**
         * Obtiene un filtro para el where para las diferentes consultas de INFORMATION_SCHEMA
         * (infSch* son las tablas donde se buscan los datos para aplicar el filtro, dependen de la consulta realizada,
         * el resto son los valores de los filtros, que tambien dependen de la consulta realizada)
         */
        private string GetWhereFilter(string infSchColCatalog, string InfSchColSchema, string InfSchColTable,
        string Catalog, string SchemaPattern, string TableNamePattern,
        string[] Types, string ColumnNamePattern)
        {
            StringBuilder filter = new StringBuilder();
            filter.Append("(1=1)");
            AddCatalogSchemaFilter(filter,infSchColCatalog, InfSchColSchema, Catalog, SchemaPattern);
            AddTableColumnFilter(filter, InfSchColTable, TableNamePattern, ColumnNamePattern);
            AddTypesFilter(filter, Types);
            return filter.ToString();
        }
        private void AddCatalogSchemaFilter(StringBuilder filter, string infSchColCatalog, string InfSchColSchema, string Catalog, string SchemaPattern)
        {
            //catalog - a catalog name; must match the catalog name as it is stored in the database; "" retrieves those without a catalog; null means that the catalog name should not be used to narrow the search
            if (Catalog != null && filter.Length != 0)
                filter.Append(" AND ");
            if (Catalog != null)
                filter.Append(Catalog == "" ? infSchColCatalog + " IS NULL" : infSchColCatalog + "='" + Catalog + "'");
            //SchemaPattern - a schema name pattern; must match the schema name as it is stored in the database; "" retrieves those without a schema; null means that the schema name should not be used to narrow the search
            if (SchemaPattern != null && filter.Length != 0)
                filter.Append(" AND ");
            if (SchemaPattern != null)
                filter.Append(SchemaPattern == "" ? InfSchColSchema + " IS NULL" : InfSchColSchema + "='" + SchemaPattern + "'");
        }
        private void AddTableColumnFilter(StringBuilder filter, string InfSchColTable, string TableNamePattern, string ColumnNamePattern)
        {
            //tableNamePattern - a table name pattern; must match the table name as it is stored in the database
            if (TableNamePattern != null && filter.Length != 0)
                filter.Append(" AND ");
            if (TableNamePattern != null)
                filter.Append(InfSchColTable + " LIKE '" + TableNamePattern + "'");
            //columnNamePattern - a column name pattern; must match the column name as it is stored in the database
            if (ColumnNamePattern != null && filter.Length != 0)
                filter.Append(" AND ");
            if (ColumnNamePattern != null)
                filter.Append("COLUMN_NAME LIKE '" + ColumnNamePattern + "'");
        }
        private void AddTypesFilter(StringBuilder filter, string[] Types)
        {
            //types - a list of table types, which must be from the list of table types returned from getTableTypes(),to include; null returns all types
            if (Types != null && filter.Length != 0)
                filter.Append(" AND ");
            if (Types != null && Types.Length > 0)
            {
                filter.Append("(");
                for (int i = 0; i < Types.Length; i++)
                {
                    string tableType = Types[i];
                    if ("TABLE".Equals(tableType)) //las TABLE estan como BASE TABLE en INFORMATION_SCHEMA
                        tableType = "BASE TABLE";
                    filter.Append((i > 0 ? " OR " : "") + "TABLE_TYPE='" + tableType + "'");
                }
                filter.Append(")");
            }
        }

        // Valores para SQLServer, no probados para otros SGBD

        public virtual bool StoresLowerCaseIdentifiers()
        {
            return false;
        }
        public virtual bool StoresLowerCaseQuotedIdentifiers()
        {
            return false;
        }
        public virtual bool StoresUpperCaseIdentifiers()
        {
            return false;
        }
        public virtual bool StoresUpperCaseQuotedIdentifiers()
        {
            return false;
        }
    }

    public static class DatabaseMetaDataConstants
    {
        public const int columnNullable = 1;
    }

}
