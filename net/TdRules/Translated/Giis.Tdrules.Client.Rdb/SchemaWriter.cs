/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Tdrules.Openapi.Model;
using Giis.Tdrules.Store.Rdb;
using Sharpen;

namespace Giis.Tdrules.Client.Rdb
{
	/// <summary>
	/// Manages the different stages of the creation of DbSchema model object from a SchemaReader
	/// (begin, writing columns, end)
	/// </summary>
	public class SchemaWriter
	{
		protected internal DbSchema model;

		protected internal DbTable currentTable;

		public SchemaWriter(SchemaReader sr)
		{
			model = new DbSchema();
			model.SetCatalog(sr.GetCatalog());
			model.SetSchema(sr.GetSchema());
			model.SetDbms(sr.GetDbmsType().ToString());
		}

		public virtual void BeginWriteTable(string tabName, string tableType)
		{
			currentTable = new DbTable();
			currentTable.SetName(tabName);
			currentTable.SetTabletype(tableType);
		}

		public virtual void EndWriteTable()
		{
			model.AddTablesItem(currentTable);
			currentTable = null;
		}

		public virtual void WriteColumn(string colName, string colDataType, string colSubType, int colSize, int decimalDigits, bool isKey, bool isAutoincrement, bool isNotNull, string foreignKey, string foreignKeyName, string checkIn, string defaultValue)
		{
			//NOSONAR all parameters needed, simpler than a builder
			DbColumn col = new DbColumn();
			col.SetName(colName);
			col.SetDatatype(colDataType);
			col.SetSubtype(colSubType);
			string size = string.Empty;
			if (colSize > 0)
			{
				size = colSize.ToString();
				if (decimalDigits != 0)
				{
					size += "," + decimalDigits;
				}
			}
			col.SetSize(size);
			if (isAutoincrement)
			{
				col.SetAutoincrement("true");
			}
			if (isKey)
			{
				col.SetKey("true");
			}
			if (isNotNull)
			{
				col.SetNotnull("true");
			}
			col.SetFk(foreignKey);
			col.SetFkname(foreignKeyName);
			col.SetCheckin(checkIn);
			col.SetDefaultvalue(defaultValue);
			currentTable.AddColumnsItem(col);
		}

		public virtual void WriteCheckConstraint(string colName, string constraintName, string constraint)
		{
			DbCheck check = new DbCheck();
			check.SetColumn(colName);
			check.SetName(constraintName);
			check.SetConstraint(constraint);
			currentTable.AddChecksItem(check);
		}

		/// <summary>Returns the model that has been writen in this object</summary>
		public virtual DbSchema GetModel()
		{
			return this.model;
		}
	}
}
