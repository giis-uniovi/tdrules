using Java.Sql;
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
    /// Common methods to generate a database schema.
    /// 
    /// Once instantiated, each call to getTable stores the metadata
    /// of the indicated table, that can be read with the getters.
    /// Only a single table is read and stored at each moment
    /// to get all tables a schema writer must be used
    /// </summary>
    public abstract class SchemaReader : SchemaReaderAbstract
    {
        //Datos almacenados de la ultima tabla leida
        protected SchemaTable currentTable = null;
        // Nombres de drivers, bd y versiones identificativos de la plataforma
        private string platformInfo = "unknown";
        private int majorVersion = 0;
        // Define si utiliza cache para minimizar las consulas a metadatos cuando
        // se buscan tablas, la implementacion estara en las subclases
        protected bool useCache = false;
        // base de datos donde reside el esquema fisico (desde donde se lee)
        protected Connection db = null;
        protected SchemaReader()
        {
            this.ResetAttributes();
        }

        /// <summary>
        /// Especifica si se usara cache, en caso afirmativo las lecturas de las tablas
        /// se realizan una sola vez contra la BD y en las siguientes se usa la cache
        /// evitando lecturas innecesarias, la implementacion ira en las subclases
        /// </summary>
        public virtual SchemaReader SetUseCache(bool useCache)
        {
            this.useCache = useCache;
            return this;
        }

        public virtual bool GetUseCache()
        {
            return this.useCache;
        }

        public virtual SchemaTable GetCurrentTable()
        {
            return currentTable;
        }

        /// <summary>
        /// Obtiene un string con la identificacion completa de la plataforma
        /// </summary>
        public virtual string GetPlatformInfo()
        {
            return this.platformInfo;
        }

        /// <summary>
        /// Actualiza el string con la identificacion completa de la plataforma
        /// </summary>
        public virtual void SetPlatformInfo(string info)
        {
            this.platformInfo = info;
        }

        /// <summary>
        /// Obtiene el numero de version mayor del DBMS (0 si no se puede obtener)
        /// </summary>
        protected virtual void SetMajorVersion(int version)
        {
            this.majorVersion = version;
        }

        /// <summary>
        /// Obtiene el numero de version mayor del DBMS (0 si no se puede obtener)
        /// </summary>
        public virtual int GetMajorVersion()
        {
            return this.majorVersion;
        }

        public virtual Connection GetDb()
        {
            return this.db;
        }

        // Determinacion del tipo de base de datos
        public virtual bool IsOracle()
        {
            return this.GetDbmsType().IsOracle();
        }

        public virtual bool IsSQLServer()
        {
            return this.GetDbmsType().IsSQLServer();
        }

        public virtual bool IsPostgres()
        {
            return this.GetDbmsType().IsPostgres();
        }

        public virtual bool IsSqlite()
        {
            return this.GetDbmsType().IsSqlite();
        }

        public virtual bool IsCassandra()
        {
            return this.GetDbmsType().IsCassandra();
        }

        public virtual bool IsTable()
        {
            return this.currentTable.IsTable();
        }

        public virtual bool IsView()
        {
            return this.currentTable.IsView();
        }

        public virtual bool IsType()
        {
            return this.currentTable.IsType();
        }

        public virtual SchemaColumn GetColumn(int index)
        {
            return this.currentTable.GetColumns()[index];
        }

        public virtual int GetColumnCount()
        {
            return this.currentTable.GetColumns().Count;
        }

        /// <summary>
        /// Nombre de la tabla actual nombre completamente qualificado
        /// </summary>
        public virtual string GetFullQualifiedTableName()
        {
            return this.currentTable.GetGivenId().GetFullQualifiedTableName(this.GetCatalog(), this.GetSchema());
        }

        /// <summary>
        /// Nombre de la tabla actual sin qualificar
        /// </summary>
        public virtual string GetTableName()
        {
            return this.currentTable.GetGivenId().GetTab();
        }

        public virtual string GetTableCatalog()
        {
            return this.currentTable.GetGivenId().GetCat();
        }

        public virtual string GetTableSchema()
        {
            return this.currentTable.GetGivenId().GetSch();
        }

        /// <summary>
        /// Nombre completamente qualificado excluyendo indicar catalogo y esquema cuando
        /// tienen los valores por defecto del esquema de esta tabla
        /// </summary>
        public virtual string GetDefaultQualifiedTableName()
        {
            return this.currentTable.GetGivenId().GetDefaultQualifiedTableName(this.GetCatalog(), this.GetSchema());
        }

        /// <summary>
        /// Nombre global, completamente cualificado tal y como han sido obtenidos los
        /// datos de Metadata
        /// </summary>
        public virtual string GetGlobalName()
        {
            return this.currentTable.GetGlobalId().GetFullQualifiedTableName();
        }

        protected virtual void SetTableGivenId(TableIdentifier table)
        {
            this.currentTable.SetGivenId(table);
        }

        protected virtual void SetTableGivenName(string tableName)
        {
            this.currentTable.GetGivenId().SetTab(tableName);
        }

        protected virtual void SetTableGlobalId(TableIdentifier id)
        {
            this.currentTable.SetGlobalId(id);
        }

        protected virtual void SetTableTypeTable()
        {
            this.currentTable.SetTableType("table");
        }

        protected virtual void SetTableTypeView()
        {
            this.currentTable.SetTableType("view");
        }

        protected virtual void SetTableTypeUdt()
        {
            this.currentTable.SetTableType("type");
        }

        protected virtual void SetTableCatalogSchema(string catalog, string schema)
        {
            this.currentTable.SetCatalog(catalog == null ? "" : catalog);
            this.currentTable.SetSchema(schema == null ? "" : schema);
        }

        protected virtual void AddColumn(SchemaColumn col)
        {
            this.currentTable.GetColumns().Add(col);
        }

        /// <summary>
        /// Reset de todos los atributos de una tabla
        /// </summary>
        protected virtual void ResetAttributes()
        {
            currentTable = new SchemaTable(this);
        }

        /// <summary>
        /// Lee la lista de todas las tablas, vistas y/o tipos definidos por el usuario
        /// (ROW en sql92) de la base de datos cuyo nombre comienza por el string indicado.
        /// </summary>
        public abstract IList<string> GetTableList(bool includeTables, bool includeViews, bool includeTypes, string startingWith);
        /// <summary>
        /// Lee la lista de todas las tablas y/o vistas de la base de datos cuyo nombre
        /// comienza por el string indicado.
        /// </summary>
        public abstract IList<string> GetTableList(bool includeTables, bool includeViews, string startingWith);
        /// <summary>
        /// Lee la lista de todas las tablas y/o vistas de la base de datos.
        /// </summary>
        public abstract IList<string> GetTableList(bool includeTables, bool includeViews);
        /// <summary>
        /// Lee todos los metadatos de la tabla o vista indicada manteniendolso
        /// inernamente para que puedan ser consultados
        /// </summary>
        public abstract SchemaTable ReadTable(string tabName, bool throwExceptionIfNotFound);
        public virtual SchemaTable ReadTable(string tabName)
        {
            return ReadTable(tabName, true);
        }

        /// <summary>
        /// Obtiene la lista de tablas formada por las de entrada (tables) y todas las
        /// que estas referencian (recursivamente). Incluye indistintamente tablas y vistas
        /// </summary>
        public virtual IList<string> GetTableListAndDependent(IList<string> tables)
        {

            // examina cada tabla de la lista y para cada una de sus columnas, incluye
            // en la lista las tablas referenciadas por esta (evitando duplicados)
            int pos = 0;
            while (pos < tables.Count)
            {

                // examina cada tabla de la lista el limite superior se determinara en cada
                // momento, pues se pueden anyadir columnas a la lista
                this.ReadTable(tables[pos]);
                for (int i = 0; i < this.GetColumnCount(); i++)
                {

                    // si tiene clave ajena prosigue
                    if (!this.GetColumn(i).GetForeignKey().Equals(""))
                    {
                        string fTable = this.GetColumn(i).GetForeignTable();

                        // recorre la lista de tablas hasta la posicion examinada actual
                        // y si no la encuentra la anyade, (evitando duplicados)
                        if (!JavaCs.ContainsIgnoreCase(tables, fTable))
                            tables.Add(fTable);
                    }
                }

                pos++;
            }

            return tables;
        }

        /// <summary>
        /// Dada una lista de tablas, obtiene otra lista con las mismas tablas ordenadas
        /// segun sus dependencias maestro-detalle (primero las maestros); NOTA: Causa
        /// excepcion si existen ciclos, en este caso utilizar SchemaSorter directamente
        /// indicando noFollowConstraint para evitar los ciclos
        /// </summary>
        public virtual IList<string> GetTableListInOrder(IList<string> tables)
        {
            return new SchemaSorter(this).Sort(tables);
        }

        // Existen otros metodos que declaran como tipo SchemaReader pero el objeto que
        // usan es SchemaReaderLiveJdbc o SchemaReaderJdbc
        // Estas clases son las que pueden implementar estos metodos para consulta de la base de datos
        // No se declaran como abstractos pues hay otros readers (e.g. SchemaReaderXml)
        // que no tienen acceso a base de datos
        public virtual ResultSet Query(string sql)
        {
            throw new SchemaException("Query method is only applicable to subclasses of SchemaReader");
        }

        public virtual void Execute(string sql)
        {
            throw new SchemaException("Execute sql method is only applicable to subclasses of SchemaReader");
        }

        public virtual void Execute(IList<string> sqls)
        {
            throw new SchemaException("Execute list of sql method is only applicable to subclasses of SchemaReader");
        }

        public virtual void CloseResultSet(ResultSet rs)
        {
            throw new SchemaException("Close ResultSet method is only applicable to subclasses of SchemaReader");
        }
    }
}