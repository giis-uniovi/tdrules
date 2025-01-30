/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Text;
using Giis.Portable.Xml.Tiny;
using Giis.Tdrules.Model.Shared;
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

		private const string EntityNode = "table";

		private const string AttributeNode = "column";

		private const string Name = "name";

		private const string EntityType = "type";

		private const string DataType = "type";

		private const string Compositetype = "compositetype";

		private const string Subtype = "subtype";

		private const string Size = "size";

		private const string Uid = "key";

		private const string Autoincrement = "autoincrement";

		private const string Notnull = "notnull";

		private const string Readonly = "readonly";

		private const string Rid = "fk";

		private const string RidName = "fkname";

		private const string Checkin = "checkin";

		private const string Default = "default";

		private const string CheckConstraint = "check";

		private const string DdlCommandNode = "command";

		private const string Ddl = "ddl";

		public virtual TdSchema Deserialize(string xml)
		{
			XNode xschema = new XNode(xml);
			TdSchema schema = new TdSchema();
			DeserializeSchemaAttributes(xschema, schema);
			foreach (XNode tnode in xschema.GetChildren(EntityNode))
			{
				TdEntity entity = new TdEntity();
				DeserializeEntityAttributes(tnode, entity);
				DeserializeEntities(tnode, entity);
				DeserializeChecks(tnode, entity);
				DeserializeDdls(tnode, entity);
				schema.AddEntitiesItem(entity);
			}
			return schema;
		}

		private void DeserializeSchemaAttributes(XNode xschema, TdSchema schema)
		{
			schema.SetStoretype(xschema.GetAttribute(Dbms));
			schema.SetCatalog(xschema.GetAttribute("catalog"));
			schema.SetSchema(xschema.GetAttribute("schema"));
		}

		private void DeserializeEntityAttributes(XNode tnode, TdEntity entity)
		{
			entity.SetName(tnode.GetAttribute(Name));
			entity.SetEntitytype(tnode.GetAttribute(EntityType));
			entity.SetSubtype(tnode.GetAttribute(Subtype));
			foreach (string attr in GetExtendedAttributeNames(tnode, new string[] { Name, EntityType, Subtype }))
			{
				entity.PutExtendedItem(attr, tnode.GetAttribute(attr));
			}
		}

		private void DeserializeEntities(XNode tnode, TdEntity entity)
		{
			foreach (XNode cnode in tnode.GetChildren(AttributeNode))
			{
				TdAttribute attribute = new TdAttribute();
				DeserializeAttributeDescriptors(cnode, attribute);
				entity.AddAttributesItem(attribute);
			}
		}

		private void DeserializeAttributeDescriptors(XNode cnode, TdAttribute attribute)
		{
			attribute.SetName(cnode.GetAttribute(Name));
			attribute.SetDatatype(cnode.GetAttribute(DataType));
			attribute.SetCompositetype(cnode.GetAttribute(Compositetype));
			attribute.SetSubtype(cnode.GetAttribute(Subtype));
			attribute.SetSize(cnode.GetAttribute(Size));
			attribute.SetUid(cnode.GetAttribute(Uid));
			attribute.SetAutoincrement(cnode.GetAttribute(Autoincrement));
			attribute.SetNotnull(cnode.GetAttribute(Notnull));
			attribute.SetReadonly(cnode.GetAttribute(Readonly));
			attribute.SetRid(cnode.GetAttribute(Rid));
			attribute.SetRidname(cnode.GetAttribute(RidName));
			attribute.SetCheckin(cnode.GetAttribute(Checkin));
			attribute.SetDefaultvalue(cnode.GetAttribute(Default));
			foreach (string attr in GetExtendedAttributeNames(cnode, new string[] { Name, DataType, Compositetype, Subtype, Size, Uid, Autoincrement, Notnull, Readonly, Rid, RidName, Checkin, Default }))
			{
				attribute.PutExtendedItem(attr, cnode.GetAttribute(attr));
			}
		}

		private void DeserializeChecks(XNode tnode, TdEntity entity)
		{
			foreach (XNode cnode in tnode.GetChildren(CheckConstraint))
			{
				TdCheck check = new TdCheck();
				check.SetAttribute(cnode.GetAttribute(AttributeNode));
				check.SetName(cnode.GetAttribute(Name));
				check.SetConstraint(XNode.DecodeText(cnode.InnerText()));
				entity.AddChecksItem(check);
			}
		}

		private void DeserializeDdls(XNode tnode, TdEntity entity)
		{
			foreach (XNode cnode in tnode.GetChildren(Ddl))
			{
				Ddl ddl = new Ddl();
				ddl.SetCommand(cnode.GetAttribute(DdlCommandNode));
				ddl.SetQuery(XNode.DecodeText(cnode.InnerText()));
				entity.AddDdlsItem(ddl);
			}
		}

		public virtual string Serialize(TdSchema sch)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append("<schema").Append(SetAttribute(Dbms, sch.GetStoretype())).Append(SetAttribute("catalog", sch.GetCatalog())).Append(SetAttribute("schema", sch.GetSchema())).Append(">");
			foreach (TdEntity entity in ModelUtil.Safe(sch.GetEntities()))
			{
				AppendEntity(sb, entity);
			}
			sb.Append("\n</schema>");
			return sb.ToString();
		}

		protected internal virtual void AppendEntity(StringBuilder sb, TdEntity entity)
		{
			sb.Append("\n<table").Append(SetAttribute(Name, entity.GetName())).Append(SetAttribute(EntityType, entity.GetEntitytype())).Append(SetAttribute(Subtype, entity.GetSubtype())).Append(SetExtendedAttributes(entity.GetExtended())).Append(">");
			foreach (TdAttribute attribute in ModelUtil.Safe(entity.GetAttributes()))
			{
				AppendAttribute(sb, attribute);
			}
			foreach (TdCheck check in ModelUtil.Safe(entity.GetChecks()))
			{
				//no serializa el nombre del check, no se utiliza desde xml
				sb.Append("\n<check").Append(SetAttribute(AttributeNode, check.GetAttribute())).Append(">").Append(XNode.EncodeText(check.GetConstraint())).Append("</check>");
			}
			foreach (Ddl ddl in ModelUtil.Safe(entity.GetDdls()))
			{
				sb.Append("\n<ddl").Append(SetAttribute(DdlCommandNode, ddl.GetCommand())).Append(">").Append(XNode.EncodeText(ddl.GetQuery())).Append("</ddl>");
			}
			sb.Append("\n</table>");
		}

		protected internal virtual void AppendAttribute(StringBuilder sb, TdAttribute attribute)
		{
			sb.Append("\n<column").Append(SetAttribute(Name, attribute.GetName())).Append(SetAttribute(DataType, attribute.GetDatatype())).Append(SetAttribute(Compositetype, attribute.GetCompositetype())).Append(SetAttribute(Subtype, attribute.GetSubtype())).Append(SetAttribute(Size, attribute
				.GetSize())).Append(SetAttribute(Uid, attribute.GetUid())).Append(SetAttribute(Autoincrement, attribute.GetAutoincrement())).Append(SetAttribute(Notnull, attribute.GetNotnull())).Append(SetAttribute(Readonly, attribute.GetReadonly())).Append(SetAttribute(Rid, attribute.GetRid()))
				.Append(SetAttribute(RidName, attribute.GetRidname())).Append(SetAttribute(Checkin, attribute.GetCheckin())).Append(SetAttribute(Default, attribute.GetDefaultvalue())).Append(SetExtendedAttributes(attribute.GetExtended())).Append(" />");
		}
	}
}
