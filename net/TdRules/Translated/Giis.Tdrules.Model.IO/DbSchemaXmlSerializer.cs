/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Text;
using Giis.Portable.Xml.Tiny;
using Giis.Tdrules.Model;
using Giis.Tdrules.Openapi.Model;
using Sharpen;

namespace Giis.Tdrules.Model.IO
{
	/// <summary>Custom xml serialization/deserialization of a schema model</summary>
	public class DbSchemaXmlSerializer : BaseXmlSerializer
	{
		private const string TableNode = "table";

		private const string ColumnNode = "column";

		private const string Name = "name";

		private const string Type = "type";

		private const string Compositetype = "compositetype";

		private const string Subtype = "subtype";

		private const string Size = "size";

		private const string Key = "key";

		private const string Autoincrement = "autoincrement";

		private const string Notnull = "notnull";

		private const string Fk = "fk";

		private const string Fkname = "fkname";

		private const string Checkin = "checkin";

		private const string Default = "default";

		private const string DdlCommandNode = "command";

		public virtual DbSchema Deserialize(string xml)
		{
			XNode xschema = new XNode(xml);
			DbSchema schema = new DbSchema();
			DeserializeDbSchemaAttributes(xschema, schema);
			foreach (XNode tnode in xschema.GetChildren(TableNode))
			{
				DbTable table = new DbTable();
				DeserializeDbTableAttributes(tnode, table);
				DeserializeDbColumns(tnode, table);
				DeserializeDbChecks(tnode, table);
				DeserializeDdls(tnode, table);
				schema.AddTablesItem(table);
			}
			return schema;
		}

		private void DeserializeDbSchemaAttributes(XNode xschema, DbSchema schema)
		{
			schema.SetDbms(xschema.GetAttribute("dbms"));
			schema.SetCatalog(xschema.GetAttribute("catalog"));
			schema.SetSchema(xschema.GetAttribute("schema"));
		}

		private void DeserializeDbTableAttributes(XNode tnode, DbTable table)
		{
			table.SetName(tnode.GetAttribute(Name));
			table.SetTabletype(tnode.GetAttribute(Type));
			foreach (string attr in GetExtendedAttributeNames(tnode, new string[] { Name, Type }))
			{
				table.PutExtendedItem(attr, tnode.GetAttribute(attr));
			}
		}

		private void DeserializeDbColumns(XNode tnode, DbTable table)
		{
			foreach (XNode cnode in tnode.GetChildren(ColumnNode))
			{
				DbColumn column = new DbColumn();
				DeserializeDbColumnAttributes(cnode, column);
				table.AddColumnsItem(column);
			}
		}

		private void DeserializeDbColumnAttributes(XNode cnode, DbColumn column)
		{
			column.SetName(cnode.GetAttribute(Name));
			column.SetDatatype(cnode.GetAttribute(Type));
			column.SetCompositetype(cnode.GetAttribute(Compositetype));
			column.SetSubtype(cnode.GetAttribute(Subtype));
			column.SetSize(cnode.GetAttribute(Size));
			column.SetKey(cnode.GetAttribute(Key));
			column.SetAutoincrement(cnode.GetAttribute(Autoincrement));
			column.SetNotnull(cnode.GetAttribute(Notnull));
			column.SetFk(cnode.GetAttribute(Fk));
			column.SetFkname(cnode.GetAttribute(Fkname));
			column.SetCheckin(cnode.GetAttribute(Checkin));
			column.SetDefaultvalue(cnode.GetAttribute(Default));
			foreach (string attr in GetExtendedAttributeNames(cnode, new string[] { Name, Type, Compositetype, Subtype, Size, Key, Autoincrement, Notnull, Fk, Fkname, Checkin, Default }))
			{
				column.PutExtendedItem(attr, cnode.GetAttribute(attr));
			}
		}

		private void DeserializeDbChecks(XNode tnode, DbTable table)
		{
			foreach (XNode cnode in tnode.GetChildren("check"))
			{
				DbCheck check = new DbCheck();
				check.SetColumn(cnode.GetAttribute(ColumnNode));
				check.SetName(cnode.GetAttribute(Name));
				check.SetConstraint(XNode.DecodeText(cnode.InnerText()));
				table.AddChecksItem(check);
			}
		}

		private void DeserializeDdls(XNode tnode, DbTable table)
		{
			foreach (XNode cnode in tnode.GetChildren("ddl"))
			{
				Ddl ddl = new Ddl();
				ddl.SetCommand(cnode.GetAttribute(DdlCommandNode));
				ddl.SetSql(XNode.DecodeText(cnode.InnerText()));
				table.AddDdlsItem(ddl);
			}
		}

		public virtual string Serialize(DbSchema sch)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append("<schema").Append(SetAttribute("dbms", sch.GetDbms())).Append(SetAttribute("catalog", sch.GetCatalog())).Append(SetAttribute("schema", sch.GetSchema())).Append(">");
			foreach (DbTable table in ModelUtil.Safe(sch.GetTables()))
			{
				AppendTable(sb, table);
			}
			sb.Append("\n</schema>");
			return sb.ToString();
		}

		protected internal virtual void AppendTable(StringBuilder sb, DbTable table)
		{
			sb.Append("\n<table").Append(SetAttribute(Name, table.GetName())).Append(SetAttribute(Type, table.GetTabletype())).Append(SetExtendedAttributes(table.GetExtended())).Append(">");
			foreach (DbColumn column in ModelUtil.Safe(table.GetColumns()))
			{
				AppendColumn(sb, column);
			}
			foreach (DbCheck check in ModelUtil.Safe(table.GetChecks()))
			{
				//no serializa el nombre del check, no se utiliza desde xml
				sb.Append("\n<check").Append(SetAttribute(ColumnNode, check.GetColumn())).Append(">").Append(XNode.EncodeText(check.GetConstraint())).Append("</check>");
			}
			foreach (Ddl ddl in ModelUtil.Safe(table.GetDdls()))
			{
				sb.Append("\n<ddl").Append(SetAttribute(DdlCommandNode, ddl.GetCommand())).Append(">").Append(XNode.EncodeText(ddl.GetSql())).Append("</ddl>");
			}
			sb.Append("\n</table>");
		}

		protected internal virtual void AppendColumn(StringBuilder sb, DbColumn column)
		{
			sb.Append("\n<column").Append(SetAttribute(Name, column.GetName())).Append(SetAttribute(Type, column.GetDatatype())).Append(SetAttribute(Compositetype, column.GetCompositetype())).Append(SetAttribute(Subtype, column.GetSubtype())).Append(SetAttribute(Size, column.GetSize())).Append
				(SetAttribute(Key, column.GetKey())).Append(SetAttribute(Autoincrement, column.GetAutoincrement())).Append(SetAttribute(Notnull, column.GetNotnull())).Append(SetAttribute(Fk, column.GetFk())).Append(SetAttribute(Fkname, column.GetFkname())).Append(SetAttribute(Checkin, column.GetCheckin
				())).Append(SetAttribute(Default, column.GetDefaultvalue())).Append(SetExtendedAttributes(column.GetExtended())).Append(" />");
		}
	}
}
