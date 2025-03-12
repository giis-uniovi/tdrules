using Giis.Tdrules.Store.Stypes;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Base class to generate the schema of a database,
    /// only implements basic methods to identify the schema
    /// </summary>
    public abstract class SchemaReaderAbstract
    {
        // nombre del catalogo y esquema por defecto del modelo si no se especifican seran vacios
        private string catalog = "";
        private string schema = "";
        private StoreType dbmsType = StoreType.Get(); // Identifiacion del DBMS
        /// <summary>
        /// Obtiene el objeto con las particularidades de base de datos actual
        /// </summary>
        public virtual StoreType GetDbmsType()
        {
            return dbmsType;
        }

        protected virtual void SetDbmsType(string dbmsname)
        {
            this.dbmsType = StoreType.Get(dbmsname);
        }

        protected virtual void SetCatalog(string catalog)
        {
            this.catalog = catalog;
        }

        public virtual string GetCatalog()
        {
            return this.catalog;
        }

        protected virtual void SetSchema(string schema)
        {
            this.schema = schema;
        }

        public virtual string GetSchema()
        {
            return this.schema;
        }
    }
}