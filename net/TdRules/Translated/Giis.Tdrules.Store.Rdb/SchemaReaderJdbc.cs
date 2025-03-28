using Java.Sql;
using Java.Util;
using NLog;
using Giis.Portable.Util;
using Giis.Tdrules.Store.Ids;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Implementation of the SchemaReader for a live jdbc connection
    /// </summary>
    public class SchemaReaderJdbc : SchemaReader
    {
        private static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(SchemaReaderJdbc));
        private static readonly string TABLE = "TABLE";
        private static readonly string VIEW = "VIEW";
        private static readonly string TYPE = "TYPE";
        private static readonly string TABLE_CAT = "TABLE_CAT";
        private static readonly string TABLE_SCHEM = "TABLE_SCHEM";
        private static readonly string TABLE_NAME = "TABLE_NAME";
        private static readonly string TABLE_TYPE = "TABLE_TYPE";
        private static readonly string COLUMN_NAME = "COLUMN_NAME";
        private static readonly string DATA_TYPE = "DATA_TYPE";
        private static readonly string TYPE_NAME = "TYPE_NAME";
        private static readonly string COLUMN_SIZE = "COLUMN_SIZE";
        private static readonly string DECIMAL_DIGITS = "DECIMAL_DIGITS";
        private static readonly string NULLABLE2 = "NULLABLE";
        private static readonly string COLUMN_DEF = "COLUMN_DEF";
        private static readonly string KEY_SEQ = "KEY_SEQ";
        private static readonly string PK_NAME = "PK_NAME";
        private static readonly string FK_NAME = "FK_NAME";
        private static readonly string DATE = "DATE";
        private static readonly string DATETIME = "DATETIME";
        private static readonly string SMALLDATETIME = "SMALLDATETIME";
        private static readonly string TIME = "TIME";
        private static readonly string TIMESTAMP = "TIMESTAMP";
        private static readonly string WITH_TIME_ZONE = "WITH TIME ZONE";
        private static readonly string WITH_LOCAL_TIME_ZONE = "WITH LOCAL TIME ZONE";
        private static readonly string INTERVAL = "INTERVAL";
        private static readonly string DOUBLE = "DOUBLE";
        private static readonly string PRECISION = "PRECISION";
        protected Connection conn = null;
        // objeto MetaData a traves del cual se conseguira la informacion
        private DatabaseMetaData metaData = null;
        // identificacion de ultimo sgbd analizado (para no repetir logs=
        private static string lastPlatformInfo = "";
        // Cache de las tablas, evita buscar en los metadatos la misma tabla multiples veces
        // Ago 2020 no hace falta el orden, solo se buscan elementos por clave
        protected Map<string, SchemaTable> tablesCache = new HashMap<string, SchemaTable>();
        // Define si se obtienen claves ajenas entrantes
        protected bool useIncomingFKs = false;
        // Nombre original de una tabla que se esta buscando (solo usado para los mensajes de error)
        private string tableOriginalName = "";
        // Tipos de datos en los que el tamanyo (precision y size) no es aplicable,
        // algunos de sqlserver, otros de posgres (text)
        private static readonly string[] typesWithoutSize = new string[]
        {
            "bit",
            "int",
            "bigint",
            "smallint",
            "tintyint",
            "real",
            "text"
        };
        // Como guarda este gestor los identificadores
        private bool storesLowerCaseIdentifiers = false;
        private bool storesLowerCaseQuotedIdentifiers = false;
        private bool storesUpperCaseIdentifiers = false;
        private bool storesUpperCaseQuotedIdentifiers = false;
        protected SchemaReaderJdbc() : base()
        {
        }

        /// <summary>
        /// Crea un SchemaReader a partir de una conexion jdbc abierta
        /// </summary>
        public SchemaReaderJdbc(Connection conn) : base()
        {
            this.conn = conn;
            InitializeMetaData();
        }

        /// <summary>
        /// Crea un SchemaReader a partir de una conexion jdbc abierta
        /// especificando el nombre de catalogo y de esquema de la base de datos
        /// (necesario para la lectura de los metadatos en Oracle cuando un usuario
        /// tiene acceso a varios esquemas).
        /// </summary>
        public SchemaReaderJdbc(Connection conn, string catalog, string schema) : this(conn)
        {
            InitializeCatalogSchema(catalog, schema);
        }

        protected virtual void InitializeCatalogSchema(string catalog, string schema)
        {

            //catalog y schema deben ser string vacios si se reciben como null
            this.SetCatalog(catalog == null || "".Equals(catalog) ? "" : PreprocessIdentifier(catalog));
            this.SetSchema(schema == null || "".Equals(schema) ? "" : PreprocessIdentifier(schema));
        }

        protected virtual void InitializeMetaData()
        {
            try
            {
                this.metaData = conn.GetMetaData();
                PopulatePlatformInfo();
                PopulateIdentifierStorage();
            }
            catch (SQLException e)
            {
                throw new SchemaException("SchemaReaderJdbc.new: Can't get the Database Product Name", e);
            }
        }

        private void PopulatePlatformInfo()
        {
            this.SetDbmsType(this.metaData.GetDatabaseProductName());
            try
            {

                // las conexiones jdbc/odbc bridge no soportan el metodo
                // getDatabaseMajorVersion(), quedara indefinida
                this.SetMajorVersion(this.metaData.GetDatabaseMajorVersion());
            }
            catch (Exception e2)
            {
                log.Error("SchemaReaderJdbc: Exception getting DatabaseMajorVersion: ", e2);
            }

            this.SetPlatformInfo("Database: " + this.metaData.GetDatabaseProductName() + " - Version: " + this.metaData.GetDatabaseProductVersion() + " - Driver: " + this.metaData.GetDriverName() + " - Version: " + this.metaData.GetDriverVersion());
            if (!lastPlatformInfo.Equals(this.GetPlatformInfo()))
                log.Info("Database Platform Info: " + this.GetPlatformInfo());
            lastPlatformInfo = this.GetPlatformInfo(); // NOSONAR
        }

        private void PopulateIdentifierStorage()
        {

            // Se han visto situaciones en las que se ha cerrado la conexion y luego se
            // consulta preprocessIdentifiers
            // que necesita conocer como se almacenan los identificadores, informacion que
            // esta en los metadatos
            // y que al menos Sql Server requiere una conexion activa.
            // Carga estos datos en el momento de la inicializacion de los metadatos
            try
            {
                this.storesUpperCaseQuotedIdentifiers = this.metaData.StoresUpperCaseQuotedIdentifiers();
                this.storesUpperCaseIdentifiers = this.metaData.StoresUpperCaseIdentifiers();
                this.storesLowerCaseQuotedIdentifiers = this.metaData.StoresLowerCaseQuotedIdentifiers();
                this.storesLowerCaseIdentifiers = this.metaData.StoresLowerCaseIdentifiers();
            }
            catch (SQLException e)
            {
                log.Warn("SchemaReaderJdbc.populateIdentifierStorage: Can't determine whether database stores or lowercase identifiers: " + e.Message);
            }
        }

        /// <summary>
        /// Limpia la cache, normalmente usado para iniciar metodos de prueba
        /// </summary>
        public virtual void ClearCache()
        {
            this.tablesCache = new HashMap<string, SchemaTable>();
        }

        /// <summary>
        /// Especifica que se deben obtener de las tablas las claves ajenas entrantes
        /// (usada para conocer todas las tablas relacionadas en un esquema. Tener en
        /// cuenta que estas claves ajenas entrantes no se guardan completamente, solo
        /// mantienen los nombres de las tablas que se relacionan
        /// </summary>
        public virtual void SetUseIncomingFKs(bool useIncomingFKs)
        {
            this.useIncomingFKs = useIncomingFKs;
        }

        /// <summary>
        /// Devuelve la lista con el nombre de todas las tablas de la base de datos en el
        /// catalogo/esquema especificados por defecto donde el nombre de la tabla
        /// comienza por el string indicado
        /// </summary>
        public override IList<string> GetTableList(bool includeTables, bool includeViews, bool includeTypes, string startingWith)
        {
            log.Trace("SchemaReaderJdbc.readTableList");
            string[] types = null; // sera null si no se seleccionan ni tablas ni vistas ni tipos
            IList<string> typesList = new List<string>();
            if (includeTables)
                typesList.Add(TABLE);
            if (includeViews)
                typesList.Add(VIEW);
            if (includeTypes)
                typesList.Add(TYPE);

            // NOTA: Postgres incluye los UDTs como TYPE al buscar tablas, aunque esto no
            // esta en la documentacion
            // de jdbc metadata. Si en otros gestores no lo hacen asi, buscar tablas con la
            // query usada para buscar las columnas de los Types
            if (typesList.Count > 0)
                types = JavaCs.ToArray(typesList);
            IList<string> ls = new List<string>();
            ResultSet rs = null;
            try
            {
                ReadMetadataTables(types, ls, startingWith);
            }
            catch (SQLException e)
            {
                throw (new SchemaException("SchemaReaderJdbc.getTableList", e));
            }
            finally
            {
                CloseResultSet(rs);
            }

            return ls;
        }

        public override IList<string> GetTableList(bool includeTables, bool includeViews, string startingWith)
        {
            return GetTableList(includeTables, includeViews, false, startingWith);
        }

        public override IList<string> GetTableList(bool includeTables, bool includeViews)
        {
            return GetTableList(includeTables, includeViews, false, "");
        }

        private void ReadMetadataTables(string[] types, IList<string> ls, string startingWith)
        {
            ResultSet rs = this.metaData.GetTables(Uncoalesce(this.GetCatalog()), Uncoalesce(this.GetSchema()), startingWith + "%", types);
            while (rs.Next())
            {
                string tableCat = rs.GetString(TABLE_CAT);
                string tableSchem = rs.GetString(TABLE_SCHEM);
                string tableName = rs.GetString(TABLE_NAME);
                string tableType = NormalizeJdbcTableType(rs.GetString(TABLE_TYPE));

                // excluye tablas y vistas de sistema, que deben ignorarse
                bool include = true;
                if (tableType.Equals(VIEW) && this.GetDbmsType().IsSystemView(tableName))
                    include = false;
                if (tableType.Equals(TABLE) && this.GetDbmsType().IsSystemTable(tableName))
                    include = false;

                // excluye esquemas de sistema si no se ha especificado un esquema por defecto
                if (Coalesce(this.GetSchema(), "").Equals("") && this.GetDbmsType().IsSystemSchema(tableSchem))
                    include = false;

                // Comprobacion de errores, si esta tabla que se va a anyadir ya se tenia en la
                // lista excepcion por tabla duplicaca
                if (ls.Contains(tableName))
                    throw new SchemaException("SchemaReaderJdbc.getTableList: Found more than one table or view: " + tableName + "\nTip: you may restrict the search in the metadata by specifying the schema name");

                // anyade la tabla si procede
                if (include)
                {
                    log.Trace("  Table/view ADDED: " + tableCat + " " + tableSchem + " " + tableName + " " + tableType);
                    ls.Add(tableName);
                }
                else
                {
                    log.Trace("  Table/view NOT added: " + tableCat + " " + tableSchem + " " + tableName + " " + tableType);
                }
            }

            rs.Dispose();
        }

        // cuando lee tablas, jdbc obtiene TABLE en el campo DATA_TYPE, pero algunos
        // drivers (H2 v2) retornan BASE TABLE
        // usar este metodo siempre que se lea el tipo de tabla desde los metadatos
        private string NormalizeJdbcTableType(string type)
        {
            return JavaCs.EqualsIgnoreCase("base table", type) ? TABLE : type;
        }

        /// <summary>
        /// Preprocesa un identificador (normalmente tabla) para adecuarlo segun las
        /// caracteristicas exigidas por el SGBD y tratar comillas. Admite tambien
        /// identificadores entre corchetes (sqlserver). De esta forma los
        /// identificadores de tablas tal como estan en SQL pueden ser interpretados para
        /// buscar las tablas en el esquema
        /// </summary>
        private string PreprocessIdentifier(string name)
        {
            name = name.Trim();
            if ("".Equals(name))
                return "";
            bool isQuoted = Quotation.IsQuoted(name, '"', '"') || Quotation.IsQuoted(name, '[', ']');

            // antes que nada, si es no quoted mira si tiene espacios en blanco, en cuyo
            // caso
            // se sabe que debe ser quoted, aunque no se hayan recibido las comillas
            if (!isQuoted && name.Contains(" "))
                isQuoted = true;

            // Pasa a mayusculas/minusculas en los casos en que la bd almacena de esta forma
            // para permitir que se localice correctamente la tabla cuando el case no
            // coincide con el que se tiene en tabName (p.e. en oracle es mayuscula)
            if (isQuoted)
            {

                // primero quita las comillas
                if (JavaCs.CharAt(name, 0) == '"')
                    name = Quotation.RemoveQuotes(name, '"', '"');
                else if (JavaCs.CharAt(name, 0) == '[')
                    name = Quotation.RemoveQuotes(name, '[', ']');
                if (this.storesUpperCaseQuotedIdentifiers)
                    name = name.ToUpper();
                else if (this.storesLowerCaseQuotedIdentifiers)
                    name = name.ToLower();
            }
            else
            {
                if (this.storesUpperCaseIdentifiers)
                    name = name.ToUpper();
                else if (this.storesLowerCaseIdentifiers)
                    name = name.ToLower();
            }


            // si no se dan los casos anteriores no transforma el nombre de la tabla
            return name;
        }

        /// <summary>
        /// Lee todos los metadatos de la tabla o vista indicada
        /// </summary>
        public override SchemaTable ReadTable(string tabName, bool throwExceptionIfNotFound)
        {
            this.tableOriginalName = tabName;
            tabName = PreprocessIdentifier(tabName);
            this.ResetAttributes(); // elimina contenido previo

            // crea un estructura de nombre de tabla que almacena los difersos componentes
            // que ha sido recibida en la forma [[catalog.]schema.]table
            QualifiedTableName qtn = GetNewQualifiedTableName(this.GetCatalog(), this.GetSchema(), tabName);
            log.Trace("SchemaReaderJdbc.readTable " + tabName + ". " + qtn.ToString());
            string cacheKey = qtn.ToString();

            // Si se ha indicado que se utilice cache, busca esta tabla en la cache y si la
            // encuentra actualiza la tabla actual, si no continuara buscando
            if (this.useCache && this.tablesCache.ContainsKey(cacheKey))
            {
                log.Trace("SchemaReaderJdbc.readTable " + tabName + ". Found in the metadata cache");
                this.currentTable = this.tablesCache[qtn.ToString()];
                return this.currentTable;
            }


            // guarda este valor como nombre de la tabla
            this.SetTableGivenId(qtn);

            // Lee los metadatos para determinar el tipo de tabla y el nombre global
            int foundCount = this.FindTableAndSetType(qtn, throwExceptionIfNotFound);

            // Lee los metadatos, de forma diferente si se trata de tabla o vista
            if (foundCount == 1)
            {
                if (this.IsTable())
                    ReadBaseTable(qtn);
                else if (this.IsView())
                    ReadViewTable(qtn);
                else if (this.IsType())
                    ReadTypeTable(qtn);

                // Si se ha indicado que se utilice cache, guarda esta tabla en la cache para
                // usos posteriores
                if (this.useCache)
                    this.tablesCache.Put(cacheKey, this.GetCurrentTable());
                return this.GetCurrentTable();
            } // foundCount==0 (no es >1 pues findTableAndSetType habra producido la
            else

                // excepcion)
                return null;
        }

        /// <summary>
        /// Encapsula la informacion de un nombre de tabla cualificado con catalogo y
        /// esquema, teniendo en cuenta el catalogo y esquema definidos por defecto.
        /// </summary>
        public class QualifiedTableName : TableIdentifier
        {
            public QualifiedTableName(SchemaReaderJdbc enclosing, string defCat, string defSch, string name) : base(defCat, defSch, name, false)
            {

                // normaliza los componentes teniendo en cuenta las capacidades de este SGBD
                this.SetCat(enclosing.PreprocessIdentifier(this.GetCat()));
                this.SetSch(enclosing.PreprocessIdentifier(this.GetSch()));
                this.SetTab(enclosing.PreprocessIdentifier(this.GetTab()));
            }
        }

        public virtual QualifiedTableName GetNewQualifiedTableName(string defCat, string defSch, string name)
        {
            return new QualifiedTableName(this, defCat, defSch, name);
        }

        /// <summary>
        /// Localiza un tipo de tabla (tabla, vista) tal y como es obtenido de los
        /// metadatos y lo almacena en la tabla en curso junto con su nombre e
        /// informacion de esquema. Esete es el paso previo para la obtencion de
        /// informacion de una tabla puesto que permite conocer su tipo y detectar si es
        /// visible o no en el esquema
        /// </summary>
        /// <returns>numero de tablas encontradas (0 si no ha sido encontrada)</returns>
        private int FindTableAndSetType(TableIdentifier qtn, bool throwExceptionIfNotFound)
        {

            // llama al mismo metodo usado en la busqueda de tablas para conseguir el valor
            // de la
            // columna TABLE_TYPE que es el que devuelve.
            int foundCount = 0;
            ResultSet rs = null;
            try
            {

                // en qTableName se tienen los nombres completos del esquema y catalogo precisos para buscar la tabla
                // ojo, aqui con Compiere daba error tras haber ejecutado muchas queries,
                // se soluciono pasando OPEN_CURSORS de 300 a 600
                rs = this.metaData.GetTables(Uncoalesce(qtn.GetCat()), Uncoalesce(qtn.GetSch()), qtn.GetTab(), null);
                while (rs.Next())
                {

                    // debe existir una fila solamente, si no, habra una excepcion
                    string tableCat = rs.GetString(TABLE_CAT);
                    string tableSchem = rs.GetString(TABLE_SCHEM);
                    string tableName = rs.GetString(TABLE_NAME);
                    string tableType = NormalizeJdbcTableType(rs.GetString(TABLE_TYPE));

                    // pone el id global tal como se encuentra en el esquema
                    QualifiedTableName gtn = GetNewQualifiedTableName(tableCat, tableSchem, tableName);
                    this.SetTableGlobalId(gtn);
                    this.SetTableCatalogSchema(tableCat, tableSchem);
                    if (tableType.Equals(TABLE))
                        this.SetTableTypeTable();
                    else if (tableType.Equals(VIEW))
                        this.SetTableTypeView();
                    else if (tableType.Equals(TYPE))
                        this.SetTableTypeUdt();
                    log.Trace("  Global name: " + gtn.GetFullQualifiedTableName() + " type: " + tableType);
                    foundCount++;
                }

                rs.Dispose();

                // Se ha de haber localizado exactamente una fila
                if (foundCount == 0 && throwExceptionIfNotFound)
                    throw (new SchemaException("SchemaReaderJdbc.setTableType: Can't find table or view: " + this.tableOriginalName));
                else if (foundCount > 1)
                    throw (new SchemaException("SchemaReaderJdbc.setTableType: Found more than one table or view: " + this.tableOriginalName + "\nTip: you may restrict the search in the metadata by specifying the schema name"));
            }
            catch (SQLException e)
            {
                throw new SchemaException("SchemaReaderJdbc.setTableType", e);
            }
            finally
            {
                CloseResultSet(rs);
            }

            return foundCount;
        }

        /// <summary>
        /// Lee todos los metadatos de la tabla fisica indicada (no vista) utilizando
        /// DatabaseMetadata. No comprueba si se trata de una vista, pero si se ejecuta
        /// sobre una vista es muy probable que los tipos de datos y atributos sean
        /// incorrectos, ya que DatabaseMetadata o obtiene correctamente los valores (al
        /// menos en oracle)
        /// </summary>
        private void ReadBaseTable(TableIdentifier qtn)
        {
            string cat = Uncoalesce(qtn.GetCat());
            string sch = Uncoalesce(qtn.GetSch());
            string tab = qtn.GetTab();
            try
            {

                // obtiene la lista de todas las columnas
                ReadMetadataColumns(cat, sch, tab);

                // obtiene la lista de todas las claves primarias
                ReadMetadataPks(cat, sch, tab);

                // busca campos autoincrementales si no se encontraron al examinar las columnas
                UpdateAutoIncrementColumns();

                // Obtiene las claves ajenas (clves salientes)
                if (!this.IsCassandra())
                    ReadMetadataFks(cat, sch, tab);

                // Obtiene las claves ajenas entrantes (procedentes de otras tablas que referencian esta)
                // El procedimiento es similar a las claves ajenas (salientes), pero no obtiene informacion de columnas
                if (this.useIncomingFKs && !this.IsCassandra())
                    ReadMetadataIncomingFks(cat, sch, tab);

                // To be Deprecated: Obtiene la lista de todas las constraints de tipo CHECK IN
                // en oracle
                if (this.IsOracle())
                    ReadOracleCheckConstraints(qtn);
            }
            catch (SQLException e)
            {
                log.Error("SchemaReaderJdbc.getTableList: ", e);
                throw new SchemaException("SchemaReaderJdbc.getTableList: Error reading table metadata for " + qtn.GetFullQualifiedTableName(), e);
            }


            // Obtiene la lista completa de check constraints, esta no causara excepcion
            ReadBaseTableCheckConstraints(cat, sch, tab);
        }

        private void ReadMetadataColumns(string cat, string sch, string tab)
        {
            ResultSet rs = this.metaData.GetColumns(cat, sch, tab, "%");
            while (rs.Next())
            {
                string tableCat = rs.GetString(TABLE_CAT);
                string tableSchem = rs.GetString(TABLE_SCHEM);
                string tableName = rs.GetString(TABLE_NAME);
                string columnName = rs.GetString(COLUMN_NAME);
                int typeCode = rs.GetInt(DATA_TYPE);
                string typeName = rs.GetString(TYPE_NAME);
                int columnSize = rs.GetInt(COLUMN_SIZE);
                int decimalDigits = rs.GetInt(DECIMAL_DIGITS);
                int nullable = rs.GetInt(NULLABLE2);
                string columnDef = rs.GetString(COLUMN_DEF);
                log.Trace("  Column: " + tableCat + " " + tableSchem + " " + tableName + " " + columnName + " " + typeName + " " + columnSize + " " + decimalDigits + " " + nullable + " " + (columnDef != null ? columnDef : ""));

                // El tipo de la tabla no se puede extraer de esta consulta, sino de la de la
                // lista de tablas. Por ello en vez de guardar un atributo se override
                // los metodos isTable e isView para que hagan la consulta necesaria para ello
                // (el tipo de tabla solo se consulta mediante metodos)
                // Datos de la columna
                SchemaColumn col = new SchemaColumn();
                col.SetColName(columnName);
                col.SetDataType(this.GetDbmsType().MapAliasToDataType(typeName)); // usa mapeo especifico de dbms si existe
                col.SetDataSubType("");
                col.SetDataTypeCode(typeCode);

                // Determina campos autoincrementales obtenidos a partir del tipo de datos de la columna
                this.UpdateAutoIncrementColumn(col);
                col.SetColSize(columnSize);
                col.SetDecimalDigits(decimalDigits);
                col.SetNotNull(nullable != DatabaseMetaData.columnNullable);

                // Parche para SQLite, devuelve nombres como CHAR(n) en vez de colocar n en
                // columnSize (pone un valor muy grande)
                if (this.IsSqlite())
                    col.ReparseNameWithPrecision();

                // en los tipos compuestos hay que determinar el tipo de estructura, actualizando algunos datos
                UpdateCompositeType(col);

                // valor por defecto, eliminando parentesis si tiene (algunos SGBD guardan el parentesis (puede haber dos)
                if (columnDef != null)
                {
                    col.SetDefaultValue(columnDef);
                    col.SetDefaultValue(Quotation.RemoveQuotes(col.GetDefaultValue(), '(', ')'));
                    col.SetDefaultValue(Quotation.RemoveQuotes(col.GetDefaultValue(), '(', ')'));
                }


                // La clave se determina a continuacion, demomento la inicio a false
                col.SetKey(false);

                // En aquellos tipos de datos en que no es necesario conocer
                // el tamanyo, pone los valores a cero
                CorrectTypeAttributes(col);

                // finalmente anyade esta columna
                this.AddColumn(col);
            }

            rs.Dispose();
        }

        // Esto deberia implementarse con jdbc, metodo GetAttributes (similar a getColumns)
        // pero algunos drivers no lo implementan todavia
        // ej: postgres driver 42.4, database 14.5 (updated at mid 2022).
        // Usare information_schema
        // NOTE: solo algunas pruebas en postgres, puede haber inconsistencias con getBaseTable
        private void ReadTypeTable(TableIdentifier qtn)
        {
            try
            {
                ReadUdtMetadataColumns(Uncoalesce(qtn.GetCat()), Uncoalesce(qtn.GetSch()), qtn.GetTab());
            }
            catch (SQLException e)
            {
                log.Error("SchemaReaderJdbc.readUdtTable: ", e);
                throw (new SchemaException("SchemaReaderJdbc.getUdtTable: Error reading table metadata for " + qtn.GetFullQualifiedTableName(), e));
            }
        }

        private void ReadUdtMetadataColumns(string cat, string sch, string tab)
        {
            string sql = "select UDT_NAME, ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT, IS_NULLABLE, " + " DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, DATETIME_PRECISION, INTERVAL_PRECISION, " + "ATTRIBUTE_UDT_NAME" + " from INFORMATION_SCHEMA.ATTRIBUTES" + " where UDT_NAME='" + tab + "'" + Ifnn(cat, " and UDT_CATALOG='" + cat + "'") + Ifnn(sch, " and UDT_SCHEMA='" + sch + "'") + " order by ORDINAL_POSITION";
            ResultSet rs = this.Query(sql, 0);
            while (rs.Next())
            {
                string tableName = rs.GetString("UDT_NAME");
                string columnName = rs.GetString("ATTRIBUTE_NAME");
                log.Trace("  UDT Column: " + tableName + "." + columnName);
                SchemaColumn col = new SchemaColumn();
                col.SetColName(rs.GetString("ATTRIBUTE_NAME"));
                col.SetDataType(this.GetDbmsType().MapAliasToDataType(rs.GetString("ATTRIBUTE_UDT_NAME")));
                this.UpdateCompositeType(col); // podria estar basado en otros tipos

                // precision/scale en campos diferentes segun el tipo de datos
                string[] precisions = new string[]
                {
                    rs.GetString("CHARACTER_MAXIMUM_LENGTH"),
                    rs.GetString("NUMERIC_PRECISION"),
                    rs.GetString("DATETIME_PRECISION"),
                    rs.GetString("INTERVAL_PRECISION")
                };
                int precision = 0;
                if (precisions[0] != null)
                    precision = JavaCs.StringToInt(precisions[0]);
                else if (precisions[1] != null)
                    precision = JavaCs.StringToInt(precisions[1]);
                else if (precisions[2] != null)
                    precision = JavaCs.StringToInt(precisions[2]);
                else if (precisions[3] != null)
                    precision = JavaCs.StringToInt(precisions[3]);
                col.SetColSize(precision);
                int scale = rs.GetInt("NUMERIC_SCALE");
                col.SetDecimalDigits(scale);
                CorrectTypeAttributes(col);

                // No se ponen mas atributos, postgres no permite poner otras restricciones.
                // Revisar si en algun momento se realiza para otro sgbd
                this.AddColumn(col);
            }

            rs.Dispose();
        }

        private void UpdateCompositeType(SchemaColumn col)
        {

            // La existencia de tipos compuestos se determinan por el numero de tipo de dato
            // en java
            // y se guardan de momento en compositeType
            if (col.GetDataTypeCode() == Types.ARRAY)
            {
                col.SetCompositeType("array"); // el tipo de dato coincide con el de cada elemento
            }
            else if (col.GetDataTypeCode() == Types.STRUCT)
            {
                col.SetCompositeType("type"); // es un objeto on un user defined type (row en sql92)
                col.SetColSize(0);
                col.SetDecimalDigits(0);
            }
            else
                col.SetCompositeType("");
        }

        private void ReadMetadataPks(string cat, string sch, string tab)
        {
            ResultSet rs = this.metaData.GetPrimaryKeys(cat, sch, tab);
            while (rs.Next())
            {
                string tableCat = rs.GetString(TABLE_CAT);
                string tableSchem = rs.GetString(TABLE_SCHEM);
                if (true)
                {
                    string tableName = rs.GetString(TABLE_NAME);
                    string columnName = rs.GetString(COLUMN_NAME);
                    string keySeq = rs.GetString(KEY_SEQ);
                    string pkName = rs.GetString(PK_NAME);
                    log.Trace("  Primary key: " + tableCat + " " + tableSchem + " " + tableName + " " + columnName + " " + keySeq + " " + pkName);

                    // busco la columna correspondiente en la lista anterior y actualizo su valor de
                    // clave
                    for (int i = 0; i < this.GetColumnCount(); i++)
                        if (columnName.Equals(this.GetColumn(i).GetColName()))
                            this.GetColumn(i).SetKey(true);
                }
            }

            rs.Dispose();
        }

        private void ReadMetadataFks(string cat, string sch, string tab)
        {
            ResultSet rs = this.metaData.GetImportedKeys(cat, sch, tab);
            while (rs.Next())
            {
                string pktableCat = rs.GetString("PKTABLE_CAT");
                string pktableSchem = rs.GetString("PKTABLE_SCHEM");
                string pktableName = rs.GetString("PKTABLE_NAME");
                string pkcolumnName = rs.GetString("PKCOLUMN_NAME");
                string fktableCat = rs.GetString("FKTABLE_CAT");
                string fktableSchem = rs.GetString("FKTABLE_SCHEM");
                string fktableName = rs.GetString("FKTABLE_NAME");
                string fkcolumnName = rs.GetString("FKCOLUMN_NAME");
                string keySeq = rs.GetString(KEY_SEQ);
                string fkName = rs.GetString(FK_NAME);

                // solo considero fks en el esquema especificado
                log.Trace("  Foreign key: " + pktableCat + " " + pktableSchem + " " + pktableName + " " + pkcolumnName + " " + fktableCat + " " + fktableSchem + " " + fktableName + " " + fkcolumnName + " " + fkName + " " + keySeq);

                // busco la columna a la que corresponde esta fk y actualiza el atributo en la forma tabla.columna referenciada
                for (int i = 0; i < this.GetColumnCount(); i++)
                    if (JavaCs.EqualsIgnoreCase(fkcolumnName, this.GetColumn(i).GetColName()))
                    {
                        this.GetColumn(i).SetForeignKeyName(fkName);

                        // No cualifica el nombre de la fk solo en el caso de que la tabla referenciada
                        // este en el mismo catalogo/esquema y que estos coincidan con los valores
                        // por defecto o no esten especificados
                        if (MatchPkFk(pktableCat, pktableSchem, fktableCat, fktableSchem))
                        {
                            this.GetColumn(i).SetForeignKeyTable(pktableName);
                        }
                        else
                        {

                            // pone el nombre completo:  Crea un objeto QualifiedTableName para obtener
                            // el nombre correcto dependiendo del contexto
                            TableIdentifier fkFullName = new TableIdentifier(this.GetCatalog(), this.GetSchema(), pktableCat, pktableSchem, pktableName, false);
                            this.GetColumn(i).SetForeignKeyTable(fkFullName.GetDefaultQualifiedTableName(this.GetCatalog(), this.GetSchema()));
                        }

                        this.GetColumn(i).SetForeignKeyColumn(pkcolumnName);
                        this.GetColumn(i).SetForeignKeyTableSchemaIdentifier(new TableIdentifier(pktableCat, pktableSchem, pktableName, true));
                    }


                // Creo/obtengo la estructura correspondiente a la FK (de uso en el paquete dbr)
                // si se crea se hace con los nombres de las tablas full qualified
                SchemaForeignKey fk = this.GetCurrentTable().GetFK(fkName);
                if (fk == null)
                {
                    fk = new SchemaForeignKey(this.GetCurrentTable(), fkName, this.GetCurrentTable().GetGlobalId(), new TableIdentifier(pktableCat, pktableSchem, pktableName, true));
                    this.GetCurrentTable().GetFKs().Add(fk);
                }


                // anyade los datos de las columnas referenciadas
                fk.AddColumn(fkcolumnName, pkcolumnName);
            }

            rs.Dispose();
        }

        private bool MatchPkFk(string pktableCat, string pktableSchem, string fktableCat, string fktableSchem)
        {
            return (JavaCs.EqualsIgnoreCase(this.GetCatalog(), Coalesce(pktableCat, "")) || this.GetCatalog().Equals("")) && (JavaCs.EqualsIgnoreCase(this.GetSchema(), Coalesce(pktableSchem, "")) || this.GetSchema().Equals("")) && Coalesce(pktableCat, "").Equals(Coalesce(fktableCat, "")) && Coalesce(pktableSchem, "").Equals(Coalesce(fktableSchem, ""));
        }

        private void ReadMetadataIncomingFks(string cat, string sch, string tab)
        {
            ResultSet rs = this.metaData.GetExportedKeys(cat, sch, tab);
            while (rs.Next())
            {
                string efktableCat = rs.GetString("FKTABLE_CAT");
                string efktableSchem = rs.GetString("FKTABLE_SCHEM");
                string efktableName = rs.GetString("FKTABLE_NAME");
                string efkName = rs.GetString(FK_NAME);

                // solo considero fks en el esquema especificado
                log.Trace("  Foreign key (incoming): " + efktableCat + " " + efktableSchem + " " + efktableName + " " + efkName + " ");

                // Creo/obtengo la estructura correspondiente a la FK en las incoming solamente 
                // se crea la estructura que relaciona las tablas, no hay asociacion de columnas
                SchemaForeignKey fk = new SchemaForeignKey(null, efkName, new TableIdentifier(efktableCat, efktableSchem, efktableName, true), this.GetCurrentTable().GetGlobalId());
                this.GetCurrentTable().GetIncomingFKs().Add(fk);
            }

            rs.Dispose();
        }

        private void ReadOracleCheckConstraints(TableIdentifier qtn)
        {

            //El sql busca en user_constraints para esta tabla aquellas con search_condition no nula
            //Dependiendo del catalogo/esquema se se debera cualificar la tabla user_constraints
            //correspondiendo al esquema de la tabla que se esta tratando
            string sql = "select * from USER_CONSTRAINTS where  table_name='";
            sql += qtn.GetTab(); // no debe ser fully qualified
            sql += "' and search_condition is not null";
            ResultSet rs = Query(sql);
            while (rs.Next())
            {
                string searchCond = rs.GetString("SEARCH_CONDITION");
                searchCond = searchCond.Replace("  ", " ").Trim();

                // parse de la condicion para ver si es de la forma columna IS condicion
                string[] parts = JavaCs.SplitByChar(searchCond, ' ');

                // Fixed in SqlCore version, supports in (..) and in(..)
                if (parts.Length >= 2 && (parts[1].ToLower().StartsWith("in") || parts[1].ToLower().StartsWith("in(")))
                {

                    // si la segunda parte comienza por in entonces es una de las constraints que interesan
                    string col = parts[0]; // nombre de la columna

                    // resto a partir del in, ojo, si hay varios espacios en blanco estos se convierten en uno solo
                    StringBuilder cond = new StringBuilder();
                    for (int i = 1; i < parts.Length; i++)
                        cond.Append(parts[i] + " ");
                    string inCondition = JavaCs.Substring(cond.ToString(), 2, cond.ToString().Length);

                    // busco la columna a la que corresponde esta constraint y actualiza
                    // el atributo en de checkInConstraint
                    for (int i = 0; i < this.GetColumnCount(); i++)
                        if (JavaCs.EqualsIgnoreCase(col, this.GetColumn(i).GetColName()))
                            this.GetColumn(i).SetCheckInConstraint(inCondition.Trim());
                    log.Trace("  Check in constraint: " + searchCond);
                }
            }

            CloseResultSet(rs);
        }

        private void ReadBaseTableCheckConstraints(string cat, string sch, string tab)
        {

            // Obtencion general de las constraints de la base de datos usando
            // information_schema
            // select * from INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE cu inner join
            // INFORMATION_SCHEMA.CHECK_CONSTRAINTS c on
            // c.CONSTRAINT_NAME=cu.CONSTRAINT_NAME
            string sql;
            ResultSet rs = null;
            try
            {

                // si se usa una bd que no soporta information_schema no fallara
                sql = GetBaseTableCheckSql(cat, sch, tab);

                // https://dataedo.com/kb/query/postgresql/list-check-constraints-in-database
                rs = Query(sql);
                if (this.IsSqlite())
                    ReadBaseTableCheckConstraintsSqlite(rs);
                else
                    ReadBaseTableCheckConstraintsOther(rs);
            }
            catch (Exception e)
            {
            }
            finally
            {
                CloseResultSet(rs);
            }
        }

        private void ReadBaseTableCheckConstraintsSqlite(ResultSet rs)
        {
            if (rs.Next())
            {

                // puede que no haya informacion, p.e. si se hace sobre una vista
                IList<string> lst = FindCheck(rs.GetString("sql"));
                for (int i = 0; i < lst.Count; i++)
                {
                    SchemaCheckConstraint check = new SchemaCheckConstraint();
                    check.SetColumn("");
                    check.SetName("");
                    check.SetConstraint(lst[i]);
                    this.GetCurrentTable().AddCheckConstraint(check);
                }
            }
        }

        private void ReadBaseTableCheckConstraintsOther(ResultSet rs)
        {
            while (rs.Next())
            {
                SchemaCheckConstraint check = new SchemaCheckConstraint();
                check.SetColumn(rs.GetString(COLUMN_NAME) == null ? "" : rs.GetString(COLUMN_NAME));
                check.SetName(rs.GetString("CONSTRAINT_NAME"));
                check.SetConstraint(rs.GetString("CHECK_CLAUSE"));
                if (!check.GetConstraint().ToLower().EndsWith(" is not null"))

                    // omite estas constraints que se generan en el caso de oracle
                    this.GetCurrentTable().AddCheckConstraint(check);
            }
        }

        private string GetBaseTableCheckSql(string cat, string sch, string tab)
        {
            if (this.IsOracle())

                // tabla
                // mismas columnas que con information schema pero otra query
                return "select null AS COLUMN_NAME, CONSTRAINT_NAME, SEARCH_CONDITION AS CHECK_CLAUSE from USER_CONSTRAINTS" + " where TABLE_NAME='" + tab + "' and CONSTRAINT_TYPE='C' order by CONSTRAINT_NAME";
            else if (this.IsSqlite())

                // diferentes columnas que con information schema, tratamiento diferente
                return "select sql from sqlite_master where type='table' and lower(name)='" + tab.ToLower() + "'";
            else
                return "select cu.COLUMN_NAME, cu.CONSTRAINT_NAME, c.CHECK_CLAUSE" + " from INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE cu" + " inner join INFORMATION_SCHEMA.CHECK_CONSTRAINTS c" + " on c.CONSTRAINT_NAME=cu.CONSTRAINT_NAME " + Ifnn(cat, " and c.CONSTRAINT_CATALOG=cu.CONSTRAINT_CATALOG") + Ifnn(sch, " and c.CONSTRAINT_SCHEMA=cu.CONSTRAINT_SCHEMA") + " where TABLE_NAME='" + tab + "'" + Ifnn(cat, " and TABLE_CATALOG='" + cat + "'") + Ifnn(sch, " and TABLE_SCHEMA='" + sch + "'") + " order by cu.COLUMN_NAME, c.CONSTRAINT_NAME";
        }

        public virtual IList<string> FindCheck(string s)
        {
            IList<string> check = new List<string>();
            int fromIndex = 0;
            int pos = -1;
            while ((pos = s.ToLower().IndexOf("check", fromIndex)) != -1)
            {
                int brackets = 0;
                bool inside = false;
                for (int i = pos + 5; i < s.Length; i++)
                {
                    brackets += BracketLevelIncrement(JavaCs.CharAt(s, i));
                    if (brackets == 1)
                        inside = true;
                    if (inside && brackets == 0)
                    {
                        check.Add(JavaCs.Substring(s, pos + 5, i + 1));
                        fromIndex = i + 1;
                        break;
                    }
                }
            }

            return check;
        }

        private int BracketLevelIncrement(char c)
        {
            if (c == '(')
                return 1;
            else if (c == ')')
                return -1;
            else
                return 0;
        }

        /// <summary>
        /// Lee todos los metadatos de la vista indicada (no table) utilizando
        /// ResultsetMetadata. No comprueba si se trata de una vista, pero si se ejecuta
        /// sobre una taba es posible que los tipos de datos y atributos sean
        /// incorrectos, ya que en este caso se debe usar la clase especifica de
        /// DatabaseMetadata
        /// </summary>
        private void ReadViewTable(TableIdentifier qtn)
        {
            log.Trace("SchemaReaderJdbc.readViewTable : table is view, reading ResultSetMetaData");
            ResultSet rs = null;
            string sqlView = "";
            try
            {

                // obtiene la lista de todas las columnas mediante un select (solo necesita leer una fila)
                // Nota: Utiliza queryConnected para usar un resultset estandar,
                // pues query() ha dado problemas con oracle en la BD de Compiere en vista C_DUNNING_HEADER_V
                // posiblemente por usar el CachedRowSet estandar y no el de Oracle
                // cualifica completamente la query puesto que se ejecutara una query con una conexion que puede
                // tener por defecto un esquema diferente del usado en este SchemaReaderJdbc
                sqlView = "SELECT * FROM " + qtn.GetFullQualifiedTableName(this.GetCatalog(), this.GetSchema());
                sqlView = this.GetDbmsType().GetSqlLimitRows(sqlView, 0);
                rs = Query(sqlView, 1);
                ResultSetMetaData rsmd = rs.GetMetaData();
                log.Trace("***** Reading view " + sqlView);

                // ahora determina todos los atributos de las columnas, ojo, deben ser los
                // mismos que en readBaseTable
                for (int i = 1; i <= rsmd.GetColumnCount(); i++)
                {
                    SchemaColumn col = new SchemaColumn();
                    col.SetColName(rsmd.GetColumnName(i));
                    col.SetDataTypeCode(rsmd.GetColumnType(i));
                    col.SetDataType(this.GetDbmsType().MapAliasToDataType(rsmd.GetColumnTypeName(i)));
                    this.UpdateAutoIncrementColumn(col);
                    col.SetColSize(rsmd.GetPrecision(i));
                    col.SetDecimalDigits(rsmd.GetScale(i));
                    col.SetNotNull(rsmd.IsNullable(i) != DatabaseMetaData.columnNullable);

                    // no puedo saber si es clave o clave ajena, supongo que no
                    col.SetKey(false);
                    col.SetForeignKey("");
                    col.SetForeignKeyName("");

                    // En aquellos tipos de datos en que no es necesario conocer
                    // el tamanyo, pone los valores a cero
                    CorrectTypeAttributes(col);

                    // finalmente anyade esta columna
                    this.AddColumn(col);
                }


                // busca campos autoincrementales si no se encontraron al examinar las columnas
                this.UpdateAutoIncrementColumns();
            }
            catch (Exception e)
            {

                // NOSONAR
                // Si ha habido algun problema (p.e. en Oracle al leer algunas vistas del
                // sistema como p.e. DBA_ANALYZE_OBJECTS la query da error)
                // Lee los metadatos como si fuera una tabla base
                log.Warn("Can't read view '" + qtn.GetTab() + "' as query. Switch to reading as base table. Exception is: " + e.Message);
                log.Info(sqlView);
                ReadBaseTable(qtn);
            }
            finally
            {
                CloseResultSet(rs);
            }
        }

        /// <summary>
        /// Procesa una columna de un tipo dado transformando el tipo de datos y/o la
        /// caracteristica de autoincremental
        /// </summary>
        private void UpdateAutoIncrementColumn(SchemaColumn col)
        {

            // Added in SqlCore version
            string identityType = this.GetDbmsType().GetDataTypeIdentity("");
            if (JavaCs.EqualsIgnoreCase(col.GetDataType(), identityType))
            {
                col.SetAutoIncrement(true);
                return;
            }


            // Caso particular. Al menos en SQLServer con jdbc los identity (p.e. int)
            // aparecen como int identity.
            // Revisa el tipo de datos y si la ultima palabra es identity la elimina
            string identity = this.GetDbmsType().GetDataTypeIdentitySuffix();
            if (!"".Equals(identity) && col.GetDataType().EndsWith(identity))
            {
                col.SetDataType(JavaCs.Substring(col.GetDataType(), 0, col.GetDataType().Length - identity.Length).Trim());
                col.SetAutoIncrement(true);
            }
        }

        /// <summary>
        /// Caso particular cuando para determinar la clave autoincremental hay una query definida para ello
        /// </summary>
        private void UpdateAutoIncrementColumns()
        {
            if ("".Equals(this.GetDbmsType().GetDataTypeIdentitySql("", "")))
                return;

            // Obtiene la posicion de la pk, debe haber solo una
            int pkIndex = -1;
            for (int i = 0; i < this.GetColumnCount(); i++)
                if (this.GetColumn(i).IsKey())
                {
                    if (pkIndex == -1)
                        pkIndex = i;
                    else
                        return; // more than one pk, can't have autoincrement
                }

            if (pkIndex == -1)
                return;

            // La ejecucion de la query devolvera una fila si la clave es autoincremental
            string sql = this.GetDbmsType().GetDataTypeIdentitySql(this.GetFullQualifiedTableName(), this.GetColumn(pkIndex).GetColName());
            ResultSet rs = Query(sql);
            if (rs.Next())
                this.GetColumn(pkIndex).SetAutoIncrement(true);
            rs.Dispose();
        }

        /// <summary>
        /// Para una columna, modifica atributos de tamanyo y nombre para casos
        /// especiales
        /// </summary>
        private void CorrectTypeAttributes(SchemaColumn col)
        {
            col.SetDataType(col.GetDataType().Trim());
            string dataType = col.GetDataType().ToUpper(); // siempre a mayusculas para comparar
            if (dataType.StartsWith(DATETIME) || dataType.StartsWith(DATE) || dataType.StartsWith(SMALLDATETIME))
            {

                // fecha en sqlserver, quito valores de tamanyo
                col.SetColSize(0);
                col.SetDecimalDigits(0);
            } // el dato basico es timestamp, la
            else if (dataType.StartsWith(TIMESTAMP))
            {

                // el dato basico es timestamp, la
                SetTimeZoneSubtypeIfNeeded(col, dataType);
                col.SetDataType(TIMESTAMP); // elimina cualquier osa que vaya despues
            } // no qita precision pues sqlserver 2008 permite especificarla
            else if (dataType.StartsWith(TIME))
            {

                // no qita precision pues sqlserver 2008 permite especificarla
                SetTimeZoneSubtypeIfNeeded(col, dataType);
                col.SetDataType(TIME);
            } // lo que va tras INTERVAL es el subtipo
            else if (dataType.StartsWith(INTERVAL))
            {

                // lo que va tras INTERVAL es el subtipo
                col.SetDataSubType(JavaCs.Substring(col.GetDataType(), INTERVAL.Length).Trim());
                col.SetDataType(INTERVAL);
            }
            else if (dataType.StartsWith(DOUBLE))
            {
                if (dataType.Contains(PRECISION))
                    col.SetDataSubType(PRECISION);
                col.SetDataType(DOUBLE);
            }
            else

                // En aquellos tipos de datos en que no es necesario conocer el tamanyo, pone los valores a cero
                ResetColSizesForOtherTypes(col);
        }

        private void SetTimeZoneSubtypeIfNeeded(SchemaColumn col, string dataType)
        {
            if (dataType.Contains(WITH_TIME_ZONE) || dataType.Contains(WITH_LOCAL_TIME_ZONE))
                col.SetDataSubType(WITH_TIME_ZONE);
        }

        private void ResetColSizesForOtherTypes(SchemaColumn col)
        {
            for (int i = 0; i < typesWithoutSize.Length; i++)
                if (JavaCs.EqualsIgnoreCase(typesWithoutSize[i], col.GetDataType()))
                {
                    col.SetColSize(0);
                    col.SetDecimalDigits(0);
                }
        }

        /// <summary>
        /// Obtiene el SQL almacenado en una tabla que representa una vista
        /// </summary>
        public virtual string GetQuery(SchemaTable thisTable)
        {

            // utiliza la sql definida en el DbmsType que cada dbms deberia implementar
            // si no usa por defecto utilizara una generica de information_schema, que esta
            // limitada a 4000 caracteres
            string sql = thisTable.GetSchemaReader().GetDbmsType().GetViewDefinitionSQL(thisTable.GetGlobalId().GetCat(), thisTable.GetGlobalId().GetSch(), thisTable.GetGlobalId().GetTab());
            string qry = "";
            ResultSet rs = Query(sql);
            try
            {
                if (!rs.Next())
                    throw new SchemaException("SchemaReader.getQuery: Source query not found for view " + thisTable.GetGlobalId().GetTab());
                qry = rs.GetString(1); // el primer campo es el nombre de la query
            }
            catch (SQLException t)
            {
                throw new SchemaException("SchemaReader.getQuery: Source query not found for view " + thisTable.GetGlobalId().GetTab() + "\nuUsing query from metadata: " + sql, t);
            }
            finally
            {
                CloseResultSet(rs);
            }


            // Esta query deberia ser de la forma create view ... as select ...
            // Pero algunos dbms como oracle solo guardan el select, por lo que si la query
            // obtenida empieza por select se debe anyadir el create view junto con los parametros 
            // (sacados de la tabla que representa la query en los metadatos)
            qry = qry.Trim();
            if (qry.ToLower().StartsWith("select"))
            {
                StringBuilder sCreate = new StringBuilder();
                sCreate.Append("CREATE VIEW " + thisTable.GetName() + " (");
                for (int i = 0; i < thisTable.GetColumnNames().Length; i++)
                    sCreate.Append((i == 0 ? "" : ",") + thisTable.GetColumnNames()[i]);
                sCreate.Append(") AS ");
                qry = sCreate.ToString() + qry;
            }

            return qry;
        }

        //Queries a la base de datos representada por esta conexion
        public virtual ResultSet Query(string sql, int maxRows)
        {
            Statement stmt = null;
            try
            {
                stmt = this.conn.CreateStatement(); // NOSONAR no se puede cerrar pues si no se pierde el resultset

                // Si se ha indicado un mumero maximo de filas limita el rowset al numero
                // indicado
                if (maxRows > 0)
                    stmt.SetMaxRows(maxRows);
                return stmt.ExecuteQuery(sql);
            }
            catch (SQLException ex)
            {
                CloseStmt(stmt);
                throw new SchemaException("SchemaReaderJdbc.query", ex);
            }
        }

        public override ResultSet Query(string sql)
        {
            return Query(sql, 0);
        }

        public override void Execute(string sql)
        {
            Statement stmt = null;
            try
            {
                stmt = this.conn.CreateStatement();
                stmt.ExecuteUpdate(sql);
            }
            catch (SQLException ex)
            {
                throw new SchemaException("SchemaReaderJdbc.execute", ex);
            }
            finally
            {
                CloseStmt(stmt);
            }
        }

        public override void Execute(IList<string> sqls)
        {
            for (int i = 0; i < sqls.Count; i++)
                Execute(sqls[i]);
        }

        public override void CloseResultSet(ResultSet rs)
        {
            try
            {
                if (rs != null)
                {
                    Statement stmt = rs.GetStatement();
                    rs.Dispose();
                    CloseStmt(stmt);
                }
            }
            catch (Exception e)
            {
            }
        }

        private void CloseStmt(Statement stmt)
        {
            try
            {
                if (stmt != null)
                    stmt.Dispose();
            }
            catch (SQLException e)
            {
            }
        }

        private string Ifnn(string check, string value)
        {
            return JavaCs.IsEmpty(check) ? "" : value;
        }

        /// <summary>
        /// Devuelve un string de remplazo en el caso de que el string indicado sea nulo
        /// </summary>
        protected virtual string Coalesce(string str, string replacement)
        {
            return str == null ? replacement : str;
        }

        /// <summary>
        /// Realiza la operacion contraria al coalesce (case insensitive), si el string
        /// es igual al de remplazo devuelve nulo, si no devuelve el string
        /// </summary>
        protected virtual string Uncoalesce(string str, string replacement)
        {
            if (str == null)
                return null; // por si acaso el string ya es nulo
            if (JavaCs.EqualsIgnoreCase(str, replacement))
                return null;
            return str;
        }

        /// <summary>
        /// Uncoalesce que devuelve espacio en blanco
        /// </summary>
        protected virtual string Uncoalesce(string str)
        {
            return Uncoalesce(str, "");
        }
    }
}