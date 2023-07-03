/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using System.Text;
using Giis.Tdrules.Model;
using Giis.Tdrules.Openapi.Model;
using Giis.Tdrules.Store.Rdb;
using Java.Sql;
using Sharpen;

namespace Giis.Tdrules.Client.Rdb
{
	/// <summary>Client api to get the schema of a relational data store</summary>
	public class DbSchemaApi
	{
		private const string NamePrompt = " name:";

		private Connection conn;

		private SchemaReader sr = null;

		private string catalog = string.Empty;

		private string schema = string.Empty;

		/// <summary>New instance for a given jdbc connection</summary>
		public DbSchemaApi(Connection conn)
		{
			// Lazy creation of the schema reader created lazy,
			// any change in catalog/schema will reset it
			this.conn = conn;
		}

		/// <summary>New instance given a schema reader, only used for compatibility with legacy code</summary>
		public DbSchemaApi(SchemaReader sr)
		{
			this.sr = sr;
			this.conn = sr.GetDb();
			this.catalog = sr.GetCatalog();
			this.schema = sr.GetSchema();
		}

		/// <summary>Restrict the scope of metadata search to the specified catalog and schema</summary>
		public virtual Giis.Tdrules.Client.Rdb.DbSchemaApi SetCatalogAndSchema(string catalog, string schema)
		{
			this.catalog = catalog;
			this.schema = schema;
			this.sr = null;
			// to recreate with the new scope
			return this;
		}

		/// <summary>
		/// Gets the database schema for the current instance for the whole database,
		/// allowing filtering by the kind of objects to get
		/// </summary>
		public virtual DbSchema GetDbSchema()
		{
			return GetDbSchema(true, true, true, string.Empty);
		}

		/// <summary>
		/// Gets the database schema for the current instance for the whole database,
		/// allowing filtering by the kind of objects to get
		/// </summary>
		public virtual DbSchema GetDbSchema(bool includeTables, bool includeViews, bool includeTypes, string startingWith)
		{
			if (sr == null)
			{
				// lazy creation to support catealog/schema changes
				sr = new SchemaReaderJdbc(conn, catalog, schema).SetUseCache(true);
			}
			IList<string> tableNames = sr.GetTableList(includeTables, includeViews, includeTypes, startingWith);
			return GetDbSchema(tableNames);
		}

		/// <summary>Gets the database schema for the current instance including the specified tables only</summary>
		public virtual DbSchema GetDbSchema(IList<string> tables)
		{
			if (sr == null)
			{
				sr = new SchemaReaderJdbc(conn, catalog, schema).SetUseCache(true);
			}
			SchemaWriter sw = new SchemaWriter(sr);
			foreach (string table in tables)
			{
				if (table != null && !table.Trim().Equals(string.Empty))
				{
					// ensures there is a table name
					WriteTable(table, sr, sw);
				}
			}
			// Second scan to get the schema of UDT included in tables, if any
			foreach (string table_1 in tables)
			{
				if (table_1 != null && !table_1.Trim().Equals(string.Empty))
				{
					// asegura que hay nombre de tabla
					WriteReferencedTypes(table_1, sr, sw);
				}
			}
			return sw.GetModel();
		}

		private void WriteTable(string tableName, SchemaReader reader, SchemaWriter writer)
		{
			reader.ReadTable(tableName);
			writer.BeginWriteTable(reader.GetDefaultQualifiedTableName(), reader.GetCurrentTable().GetTableType());
			for (int i = 0; i < reader.GetCurrentTable().GetColumns().Count; i++)
			{
				SchemaColumn col = reader.GetColumn(i);
				writer.WriteColumn(col.GetColName(), col.GetDataType(), col.GetDataSubType(), col.GetColSize(), col.GetDecimalDigits(), col.IsKey(), col.IsAutoIncrement(), col.IsNotNull(), col.GetForeignKey(), col.GetForeignKeyName(), col.GetCheckInConstraint(), col.GetDefaultValue());
			}
			for (int i_1 = 0; i_1 < reader.GetCurrentTable().GetCheckConstraints().Count; i_1++)
			{
				SchemaCheckConstraint check = reader.GetCurrentTable().GetCheckConstraints()[i_1];
				writer.WriteCheckConstraint(check.GetColumn(), check.GetName(), check.GetConstraint());
			}
			writer.EndWriteTable();
		}

		private void WriteReferencedTypes(string table, SchemaReader sr, SchemaWriter sw)
		{
			SchemaTable tab = sr.ReadTable(table);
			foreach (SchemaColumn column in tab.GetColumns())
			{
				if (TableTypes.DtType.Equals(column.GetCompositeType()))
				{
					WriteTable(column.GetDataType(), sr, sw);
				}
			}
		}

		/// <summary>
		/// Gets a string representation of the model,
		/// intended to facilitate comparison in different platforms (java/net)
		/// </summary>
		public virtual string ModelToString(DbSchema model)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append("SCHEMA").Append(" dbms:").Append(model.GetDbms()).Append(" catalog:").Append(model.GetCatalog()).Append(" schema:").Append(model.GetSchema());
			foreach (DbTable table in model.GetTables())
			{
				AppendTable(sb, table);
			}
			return sb.ToString();
		}

		private void AppendTable(StringBuilder sb, DbTable table)
		{
			sb.Append("\nTABLE").Append(NamePrompt).Append(table.GetName()).Append(" type:").Append(table.GetTabletype());
			foreach (DbColumn column in table.GetColumns())
			{
				sb.Append("\n  COLUMN").Append(NamePrompt).Append(column.GetName()).Append(" datatype:").Append(column.GetDatatype()).Append(" compositetype:").Append(column.GetCompositetype()).Append(" subtype:").Append(column.GetSubtype()).Append(" size:").Append(column.GetSize()).Append(" key:"
					).Append(column.GetKey()).Append(" notnull:").Append(column.GetNotnull()).Append(" fk:").Append(column.GetFk()).Append(" fkname:").Append(column.GetFkname()).Append(" checkin:").Append(column.GetCheckin()).Append(" defaultvalue:").Append(column.GetDefaultvalue());
			}
			foreach (DbCheck check in table.GetChecks() == null ? new List<DbCheck>() : table.GetChecks())
			{
				sb.Append("\n  CHECK").Append(" column:").Append(check.GetColumn()).Append(NamePrompt).Append(check.GetName()).Append(" constraint:").Append(check.GetConstraint());
			}
		}
	}
}
