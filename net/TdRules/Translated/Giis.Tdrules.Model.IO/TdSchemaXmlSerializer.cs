using Giis.Portable.Xml.Tiny;
using Giis.Tdrules.Model.Shared;
using Giis.Tdrules.Openapi.Model;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Model.IO
{
    /// <summary>
    /// Custom xml serialization/deserialization of a schema model
    /// 
    /// As of Api V4 names of schema properties are not tied to relational databases,
    /// but this serializer uses the Api V3 names.
    /// </summary>
    public class TdSchemaXmlSerializer : BaseXmlSerializer
    {
        private static readonly string DBMS = "dbms";
        private static readonly string ENTITY_NODE = "table";
        private static readonly string ATTRIBUTE_NODE = "column";
        private static readonly string NAME = "name";
        private static readonly string ENTITY_TYPE = "type";
        private static readonly string DATA_TYPE = "type";
        private static readonly string COMPOSITETYPE = "compositetype";
        private static readonly string SUBTYPE = "subtype";
        private static readonly string SIZE = "size";
        private static readonly string UID = "key";
        private static readonly string AUTOINCREMENT = "autoincrement";
        private static readonly string NOTNULL = "notnull";
        private static readonly string READONLY = "readonly";
        private static readonly string RID = "fk";
        private static readonly string RID_NAME = "fkname";
        private static readonly string CHECKIN = "checkin";
        private static readonly string DEFAULT = "default";
        private static readonly string CHECK_CONSTRAINT = "check";
        private static readonly string DDL_COMMAND_NODE = "command";
        private static readonly string DDL = "ddl";
        public virtual TdSchema Deserialize(string xml)
        {
            XNode xschema = new XNode(xml);
            TdSchema schema = new TdSchema();
            DeserializeSchemaAttributes(xschema, schema);
            foreach (XNode tnode in xschema.GetChildren(ENTITY_NODE))
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
            schema.SetStoretype(xschema.GetAttribute(DBMS));
            schema.SetCatalog(xschema.GetAttribute("catalog"));
            schema.SetSchema(xschema.GetAttribute("schema"));
        }

        private void DeserializeEntityAttributes(XNode tnode, TdEntity entity)
        {
            entity.SetName(tnode.GetAttribute(NAME));
            entity.SetEntitytype(tnode.GetAttribute(ENTITY_TYPE));
            entity.SetSubtype(tnode.GetAttribute(SUBTYPE));
            foreach (string attr in GetExtendedAttributeNames(tnode, new string[] { NAME, ENTITY_TYPE, SUBTYPE }))
                entity.PutExtendedItem(attr, tnode.GetAttribute(attr));
        }

        private void DeserializeEntities(XNode tnode, TdEntity entity)
        {
            foreach (XNode cnode in tnode.GetChildren(ATTRIBUTE_NODE))
            {
                TdAttribute attribute = new TdAttribute();
                DeserializeAttributeDescriptors(cnode, attribute);
                entity.AddAttributesItem(attribute);
            }
        }

        private void DeserializeAttributeDescriptors(XNode cnode, TdAttribute attribute)
        {
            attribute.SetName(cnode.GetAttribute(NAME));
            attribute.SetDatatype(cnode.GetAttribute(DATA_TYPE));
            attribute.SetCompositetype(cnode.GetAttribute(COMPOSITETYPE));
            attribute.SetSubtype(cnode.GetAttribute(SUBTYPE));
            attribute.SetSize(cnode.GetAttribute(SIZE));
            attribute.SetUid(cnode.GetAttribute(UID));
            attribute.SetAutoincrement(cnode.GetAttribute(AUTOINCREMENT));
            attribute.SetNotnull(cnode.GetAttribute(NOTNULL));
            attribute.SetReadonly(cnode.GetAttribute(READONLY));
            attribute.SetRid(cnode.GetAttribute(RID));
            attribute.SetRidname(cnode.GetAttribute(RID_NAME));
            attribute.SetCheckin(cnode.GetAttribute(CHECKIN));
            attribute.SetDefaultvalue(cnode.GetAttribute(DEFAULT));
            foreach (string attr in GetExtendedAttributeNames(cnode, new string[] { NAME, DATA_TYPE, COMPOSITETYPE, SUBTYPE, SIZE, UID, AUTOINCREMENT, NOTNULL, READONLY, RID, RID_NAME, CHECKIN, DEFAULT }))
                attribute.PutExtendedItem(attr, cnode.GetAttribute(attr));
        }

        private void DeserializeChecks(XNode tnode, TdEntity entity)
        {
            foreach (XNode cnode in tnode.GetChildren(CHECK_CONSTRAINT))
            {
                TdCheck check = new TdCheck();
                check.SetAttribute(cnode.GetAttribute(ATTRIBUTE_NODE));
                check.SetName(cnode.GetAttribute(NAME));
                check.SetConstraint(XNode.DecodeText(cnode.InnerText()));
                entity.AddChecksItem(check);
            }
        }

        private void DeserializeDdls(XNode tnode, TdEntity entity)
        {
            foreach (XNode cnode in tnode.GetChildren(DDL))
            {
                Ddl ddl = new Ddl();
                ddl.SetCommand(cnode.GetAttribute(DDL_COMMAND_NODE));
                ddl.SetQuery(XNode.DecodeText(cnode.InnerText()));
                entity.AddDdlsItem(ddl);
            }
        }

        public virtual string Serialize(TdSchema sch)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("<schema").Append(SetAttribute(DBMS, sch.GetStoretype())).Append(SetAttribute("catalog", sch.GetCatalog())).Append(SetAttribute("schema", sch.GetSchema())).Append(">");
            foreach (TdEntity entity in ModelUtil.Safe(sch.GetEntities()))
                AppendEntity(sb, entity);
            sb.Append("\n</schema>");
            return sb.ToString();
        }

        protected virtual void AppendEntity(StringBuilder sb, TdEntity entity)
        {
            sb.Append("\n<table").Append(SetAttribute(NAME, entity.GetName())).Append(SetAttribute(ENTITY_TYPE, entity.GetEntitytype())).Append(SetAttribute(SUBTYPE, entity.GetSubtype())).Append(SetExtendedAttributes(entity.GetExtended())).Append(">");
            foreach (TdAttribute attribute in ModelUtil.Safe(entity.GetAttributes()))
                AppendAttribute(sb, attribute);
            foreach (TdCheck check in ModelUtil.Safe(entity.GetChecks()))

                //no serializa el nombre del check, no se utiliza desde xml
                sb.Append("\n<check").Append(SetAttribute(ATTRIBUTE_NODE, check.GetAttribute())).Append(">").Append(XNode.EncodeText(check.GetConstraint())).Append("</check>");
            foreach (Ddl ddl in ModelUtil.Safe(entity.GetDdls()))
                sb.Append("\n<ddl").Append(SetAttribute(DDL_COMMAND_NODE, ddl.GetCommand())).Append(">").Append(XNode.EncodeText(ddl.GetQuery())).Append("</ddl>");
            sb.Append("\n</table>");
        }

        protected virtual void AppendAttribute(StringBuilder sb, TdAttribute attribute)
        {
            sb.Append("\n<column").Append(SetAttribute(NAME, attribute.GetName())).Append(SetAttribute(DATA_TYPE, attribute.GetDatatype())).Append(SetAttribute(COMPOSITETYPE, attribute.GetCompositetype())).Append(SetAttribute(SUBTYPE, attribute.GetSubtype())).Append(SetAttribute(SIZE, attribute.GetSize())).Append(SetAttribute(UID, attribute.GetUid())).Append(SetAttribute(AUTOINCREMENT, attribute.GetAutoincrement())).Append(SetAttribute(NOTNULL, attribute.GetNotnull())).Append(SetAttribute(READONLY, attribute.GetReadonly())).Append(SetAttribute(RID, attribute.GetRid())).Append(SetAttribute(RID_NAME, attribute.GetRidname())).Append(SetAttribute(CHECKIN, attribute.GetCheckin())).Append(SetAttribute(DEFAULT, attribute.GetDefaultvalue())).Append(SetExtendedAttributes(attribute.GetExtended())).Append(" />");
        }
    }
}