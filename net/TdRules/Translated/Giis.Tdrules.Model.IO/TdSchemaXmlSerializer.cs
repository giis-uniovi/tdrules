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
	/// <summary>
	/// Custom xml serialization/deserialization of a schema model
	/// As of Api V4 names of schema properties are not tied to relational databases,
	/// but this serializer uses the Api V3 names.
	/// </summary>
	public class TdSchemaXmlSerializer : BaseXmlSerializer
	{
		private const string Dbms = "dbms";

		private const string TableNode = "table";

		private const string ColumnNode = "column";

		private const string Name = "name";

		private const string EntityType = "type";

		private const string DataType = "type";

		private const string Compositetype = "compositetype";

		private const string Subtype = "subtype";

		private const string Size = "size";

		private const string Key = "key";

		private const string Autoincrement = "autoincrement";

		private const string Notnull = "notnull";

		private const string Readonly = "readonly";

		private const string Fk = "fk";

		private const string Fkname = "fkname";

		private const string Checkin = "checkin";

		private const string Default = "default";

		private const string CheckConstraint = "check";

		private const string DdlCommandNode = "command";

		private const string Ddl = "ddl";

		public virtual TdSchema Deserialize(string xml)
		{
			XNode xschema = new XNode(xml);
			TdSchema schema = new TdSchema();
			DeserializeDbSchemaAttributes(xschema, schema);
			foreach (XNode tnode in xschema.GetChildren(TableNode))
			{
				TdEntity table = new TdEntity();
				DeserializeDbTableAttributes(tnode, table);
				DeserializeDbColumns(tnode, table);
				DeserializeDbChecks(tnode, table);
				DeserializeDdls(tnode, table);
				schema.AddEntitiesItem(table);
			}
			return schema;
		}

		private void DeserializeDbSchemaAttributes(XNode xschema, TdSchema schema)
		{
			schema.SetStoretype(xschema.GetAttribute(Dbms));
			schema.SetCatalog(xschema.GetAttribute("catalog"));
			schema.SetSchema(xschema.GetAttribute("schema"));
		}

		private void DeserializeDbTableAttributes(XNode tnode, TdEntity table)
		{
			table.SetName(tnode.GetAttribute(Name));
			table.SetEntitytype(tnode.GetAttribute(EntityType));
			table.SetSubtype(tnode.GetAttribute(Subtype));
			foreach (string attr in GetExtendedAttributeNames(tnode, new string[] { Name, EntityType, Subtype }))
			{
				table.PutExtendedItem(attr, tnode.GetAttribute(attr));
			}
		}

		private void DeserializeDbColumns(XNode tnode, TdEntity table)
		{
			foreach (XNode cnode in tnode.GetChildren(ColumnNode))
			{
				TdAttribute column = new TdAttribute();
				DeserializeDbColumnAttributes(cnode, column);
				table.AddAttributesItem(column);
			}
		}

		private void DeserializeDbColumnAttributes(XNode cnode, TdAttribute column)
		{
			column.SetName(cnode.GetAttribute(Name));
			column.SetDatatype(cnode.GetAttribute(DataType));
			column.SetCompositetype(cnode.GetAttribute(Compositetype));
			column.SetSubtype(cnode.GetAttribute(Subtype));
			column.SetSize(cnode.GetAttribute(Size));
			column.SetUid(cnode.GetAttribute(Key));
			column.SetAutoincrement(cnode.GetAttribute(Autoincrement));
			column.SetNotnull(cnode.GetAttribute(Notnull));
			column.SetReadonly(cnode.GetAttribute(Readonly));
			column.SetRid(cnode.GetAttribute(Fk));
			column.SetRidname(cnode.GetAttribute(Fkname));
			column.SetCheckin(cnode.GetAttribute(Checkin));
			column.SetDefaultvalue(cnode.GetAttribute(Default));
			foreach (string attr in GetExtendedAttributeNames(cnode, new string[] { Name, DataType, Compositetype, Subtype, Size, Key, Autoincrement, Notnull, Readonly, Fk, Fkname, Checkin, Default }))
			{
				column.PutExtendedItem(attr, cnode.GetAttribute(attr));
			}
		}

		private void DeserializeDbChecks(XNode tnode, TdEntity table)
		{
			foreach (XNode cnode in tnode.GetChildren(CheckConstraint))
			{
				TdCheck check = new TdCheck();
				check.SetAttribute(cnode.GetAttribute(ColumnNode));
				check.SetName(cnode.GetAttribute(Name));
				check.SetConstraint(XNode.DecodeText(cnode.InnerText()));
				table.AddChecksItem(check);
			}
		}

		private void DeserializeDdls(XNode tnode, TdEntity table)
		{
			foreach (XNode cnode in tnode.GetChildren(Ddl))
			{
				Ddl ddl = new Ddl();
				ddl.SetCommand(cnode.GetAttribute(DdlCommandNode));
				ddl.SetQuery(XNode.DecodeText(cnode.InnerText()));
				table.AddDdlsItem(ddl);
			}
		}

		public virtual string Serialize(TdSchema sch)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append("<schema").Append(SetAttribute(Dbms, sch.GetStoretype())).Append(SetAttribute("catalog", sch.GetCatalog())).Append(SetAttribute("schema", sch.GetSchema())).Append(">");
			foreach (TdEntity table in ModelUtil.Safe(sch.GetEntities()))
			{
				AppendTable(sb, table);
			}
			sb.Append("\n</schema>");
			return sb.ToString();
		}

		protected internal virtual void AppendTable(StringBuilder sb, TdEntity table)
		{
			sb.Append("\n<table").Append(SetAttribute(Name, table.GetName())).Append(SetAttribute(EntityType, table.GetEntitytype())).Append(SetAttribute(Subtype, table.GetSubtype())).Append(SetExtendedAttributes(table.GetExtended())).Append(">");
			foreach (TdAttribute column in ModelUtil.Safe(table.GetAttributes()))
			{
				AppendColumn(sb, column);
			}
			foreach (TdCheck check in ModelUtil.Safe(table.GetChecks()))
			{
				//no serializa el nombre del check, no se utiliza desde xml
				sb.Append("\n<check").Append(SetAttribute(ColumnNode, check.GetAttribute())).Append(">").Append(XNode.EncodeText(check.GetConstraint())).Append("</check>");
			}
			foreach (Ddl ddl in ModelUtil.Safe(table.GetDdls()))
			{
				sb.Append("\n<ddl").Append(SetAttribute(DdlCommandNode, ddl.GetCommand())).Append(">").Append(XNode.EncodeText(ddl.GetQuery())).Append("</ddl>");
			}
			sb.Append("\n</table>");
		}

		protected internal virtual void AppendColumn(StringBuilder sb, TdAttribute column)
		{
			sb.Append("\n<column").Append(SetAttribute(Name, column.GetName())).Append(SetAttribute(DataType, column.GetDatatype())).Append(SetAttribute(Compositetype, column.GetCompositetype())).Append(SetAttribute(Subtype, column.GetSubtype())).Append(SetAttribute(Size, column.GetSize())).Append
				(SetAttribute(Key, column.GetUid())).Append(SetAttribute(Autoincrement, column.GetAutoincrement())).Append(SetAttribute(Notnull, column.GetNotnull())).Append(SetAttribute(Readonly, column.GetReadonly())).Append(SetAttribute(Fk, column.GetRid())).Append(SetAttribute(Fkname, column
				.GetRidname())).Append(SetAttribute(Checkin, column.GetCheckin())).Append(SetAttribute(Default, column.GetDefaultvalue())).Append(SetExtendedAttributes(column.GetExtended())).Append(" />");
		}
	}
}
