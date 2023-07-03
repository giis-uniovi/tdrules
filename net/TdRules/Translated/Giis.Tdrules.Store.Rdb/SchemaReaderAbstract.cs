/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Tdrules.Store.Stypes;
using Sharpen;

namespace Giis.Tdrules.Store.Rdb
{
	/// <summary>
	/// Base class to generate the schema of a database,
	/// only implements basic methods to identify the schema
	/// </summary>
	public abstract class SchemaReaderAbstract
	{
		private string catalog = string.Empty;

		private string schema = string.Empty;

		private StoreType dbmsType = StoreType.Get();

		// nombre del catalogo y esquema por defecto del modelo si no se especifican seran vacios
		// Identifiacion del DBMS
		/// <summary>Obtiene el objeto con las particularidades de base de datos actual</summary>
		public virtual StoreType GetDbmsType()
		{
			return dbmsType;
		}

		protected internal virtual void SetDbmsType(string dbms)
		{
			this.dbmsType = StoreType.Get(dbms);
		}

		protected internal virtual void SetCatalog(string catalog)
		{
			this.catalog = catalog;
		}

		public virtual string GetCatalog()
		{
			return this.catalog;
		}

		protected internal virtual void SetSchema(string schema)
		{
			this.schema = schema;
		}

		public virtual string GetSchema()
		{
			return this.schema;
		}
	}
}
