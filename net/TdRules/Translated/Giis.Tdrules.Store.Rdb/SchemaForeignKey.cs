/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using System.Text;
using Giis.Tdrules.Store.Ids;
using Sharpen;

namespace Giis.Tdrules.Store.Rdb
{
	public class SchemaForeignKey
	{
		private SchemaTable fromTable;

		private string name;

		private TableIdentifier fromTableIdentifier;

		private TableIdentifier toTableIdentifier;

		private IList<string> fromColumnNames = new List<string>();

		private IList<string> toColumnNames = new List<string>();

		public SchemaForeignKey(SchemaTable fromTable, string name, TableIdentifier fromTableIdentifier, TableIdentifier toTableIdentifier)
		{
			this.fromTable = fromTable;
			this.name = name;
			this.fromTableIdentifier = fromTableIdentifier;
			this.toTableIdentifier = toTableIdentifier;
		}

		public virtual SchemaTable GetFromTable()
		{
			return fromTable;
		}

		public virtual string GetName()
		{
			return name;
		}

		public virtual TableIdentifier GetFromTableIdentifier()
		{
			return fromTableIdentifier;
		}

		public virtual TableIdentifier GetToTableIdentifier()
		{
			return toTableIdentifier;
		}

		public virtual IList<string> GetFromColumnNames()
		{
			return fromColumnNames;
		}

		public virtual IList<string> GetToColumnNames()
		{
			return toColumnNames;
		}

		public virtual void SetFromTable(SchemaTable fromTable)
		{
			this.fromTable = fromTable;
		}

		public virtual string GetFromTableShortName()
		{
			return this.fromTableIdentifier.GetTab();
		}

		public virtual string GetToTableShortName()
		{
			return this.toTableIdentifier.GetTab();
		}

		public virtual string GetFromTableFullName()
		{
			return this.fromTableIdentifier.GetFullQualifiedTableName();
		}

		public virtual string GetToTableFullName()
		{
			return this.toTableIdentifier.GetFullQualifiedTableName();
		}

		public virtual void AddColumn(string fromColumnName, string toColumnName)
		{
			this.fromColumnNames.Add(fromColumnName);
			this.toColumnNames.Add(toColumnName);
		}

		public override string ToString()
		{
			return GetFromTableFullName() + " CONSTRAINT " + name + " FOREIGN KEY(" + JoinElements(fromColumnNames, ",") + ") REFERENCES " + GetToTableFullName() + "(" + JoinElements(toColumnNames, ",") + ")";
		}

		// Concatena elementos de la lista con el separador indicado
		private string JoinElements(IList<string> elements, string separator)
		{
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < elements.Count; i++)
			{
				sb.Append((i == 0 ? string.Empty : separator) + elements[i]);
			}
			return sb.ToString();
		}
	}
}
