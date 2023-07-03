/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Portable.Util;
using Giis.Tdrules.Store.Ids;
using Sharpen;

namespace Giis.Tdrules.Store.Rdb
{
	public class SchemaTable
	{
		private IList<SchemaColumn> columns;

		private IList<SchemaForeignKey> fks;

		private IList<SchemaForeignKey> incomingFks;

		private IList<SchemaCheckConstraint> checkConstraints;

		private TableIdentifier givenId;

		private TableIdentifier globalId;

		private string tableType;

		private string catalog = string.Empty;

		private string schema = string.Empty;

		private SchemaReader schemaReader = null;

		public SchemaTable(SchemaReader sReader)
		{
			// claves ajenas
			// claves ajenas de otras tablas que referencian a esta
			// checks aplicables a las columnas de esta tabla
			// nombre de la tabla actual tal como se ha indicado al buscarla
			// nombre de la tabla actual con todos los datos de catalogo y esquema
			// encontrados en los metadatos
			// tipo de tabla: table, view, type (udt o row-set)
			// nombre del catalogo tal y como figura en los metadatos
			// el SchemaReader desde el que se ha leido la tabla
			this.schemaReader = sReader;
			this.columns = new List<SchemaColumn>();
			this.fks = new List<SchemaForeignKey>();
			this.incomingFks = new List<SchemaForeignKey>();
			this.checkConstraints = new List<SchemaCheckConstraint>();
			this.givenId = new TableIdentifier(string.Empty, string.Empty, string.Empty, string.Empty, string.Empty, false);
			this.globalId = new TableIdentifier(string.Empty, string.Empty, string.Empty, string.Empty, string.Empty, false);
			this.tableType = string.Empty;
		}

		public virtual IList<SchemaColumn> GetColumns()
		{
			return columns;
		}

		public virtual TableIdentifier GetGivenId()
		{
			return givenId;
		}

		public virtual TableIdentifier GetGlobalId()
		{
			return globalId;
		}

		public virtual string GetTableType()
		{
			return tableType;
		}

		public virtual string GetCatalog()
		{
			return catalog;
		}

		public virtual string GetSchema()
		{
			return schema;
		}

		public virtual SchemaReader GetSchemaReader()
		{
			return schemaReader;
		}

		public virtual void SetGivenId(TableIdentifier givenId)
		{
			this.givenId = givenId;
		}

		public virtual void SetGlobalId(TableIdentifier globalId)
		{
			this.globalId = globalId;
		}

		public virtual void SetTableType(string tableType)
		{
			this.tableType = tableType;
		}

		public virtual void SetCatalog(string catalog)
		{
			this.catalog = catalog;
		}

		public virtual void SetSchema(string schema)
		{
			this.schema = schema;
		}

		public virtual void SetSchemaReader(SchemaReader schemaReader)
		{
			this.schemaReader = schemaReader;
		}

		public override string ToString()
		{
			return this.GetName();
		}

		public virtual string GetName()
		{
			return this.givenId.GetDefaultQualifiedTableName(givenId.GetCat(), givenId.GetSch());
		}

		public virtual string GetGlobalName()
		{
			return this.GetGlobalId().GetFullQualifiedTableName();
		}

		public virtual bool IsTable()
		{
			return this.tableType.Equals("table");
		}

		public virtual bool IsView()
		{
			return this.tableType.Equals("view");
		}

		public virtual bool IsType()
		{
			return this.tableType.Equals("type");
		}

		public virtual string[] GetColumnNames()
		{
			string[] names = new string[this.columns.Count];
			for (int i = 0; i < names.Length; i++)
			{
				names[i] = this.columns[i].GetColName();
			}
			return names;
		}

		/// <summary>
		/// Devuelve la estructura de clave ajena correspondiente a este nombre, null si
		/// no se encuentra
		/// </summary>
		public virtual SchemaForeignKey GetFK(string name)
		{
			for (int i = 0; i < this.fks.Count; i++)
			{
				SchemaForeignKey fk = this.fks[i];
				if (JavaCs.EqualsIgnoreCase(fk.GetName(), name))
				{
					return fk;
				}
			}
			return null;
		}

		public virtual IList<SchemaForeignKey> GetFKs()
		{
			return this.fks;
		}

		/// <summary>
		/// Devuelve todas las claves ajenas entrantes (de tablas que referencian a
		/// esta).
		/// </summary>
		/// <remarks>
		/// Devuelve todas las claves ajenas entrantes (de tablas que referencian a
		/// esta). A diferencia de las FKs de salida, estas no guardan como objeto la
		/// tabla origen ni los valores de las columnas pues su uso es solamente para
		/// localizar nombres de tablas que se relacionan con esta. Ademas se obtienen
		/// solamente cuando el reader ha sido configurado para obtenerlas (solo
		/// implementado para Jdbc)
		/// </remarks>
		public virtual IList<SchemaForeignKey> GetIncomingFKs()
		{
			return this.incomingFks;
		}

		public virtual void AddCheckConstraint(SchemaCheckConstraint check)
		{
			this.checkConstraints.Add(check);
		}

		public virtual IList<SchemaCheckConstraint> GetCheckConstraints()
		{
			return this.checkConstraints;
		}
	}
}
