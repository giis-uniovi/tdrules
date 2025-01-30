package giis.tdrules.model.io;

import static giis.tdrules.model.shared.ModelUtil.safe;

import giis.portable.xml.tiny.XNode;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.Ddl;

/**
 * Custom xml serialization/deserialization of a schema model
 * 
 * As of Api V4 names of schema properties are not tied to relational databases,
 * but this serializer uses the Api V3 names.
 */
public class TdSchemaXmlSerializer extends BaseXmlSerializer {
	private static final String DBMS = "dbms";
	private static final String ENTITY_NODE = "table";
	private static final String ATTRIBUTE_NODE = "column";
	private static final String NAME = "name";
	private static final String ENTITY_TYPE = "type";
	private static final String DATA_TYPE = "type";
	private static final String COMPOSITETYPE = "compositetype";
	private static final String SUBTYPE = "subtype";
	private static final String SIZE = "size";
	private static final String UID = "key";
	private static final String AUTOINCREMENT = "autoincrement";
	private static final String NOTNULL = "notnull";
	private static final String READONLY = "readonly";
	private static final String RID = "fk";
	private static final String RID_NAME = "fkname";
	private static final String CHECKIN = "checkin";
	private static final String DEFAULT = "default";
	private static final String CHECK_CONSTRAINT = "check";
	private static final String DDL_COMMAND_NODE = "command";
	private static final String DDL = "ddl";
	
	public TdSchema deserialize(String xml) {
		XNode xschema=new XNode(xml);
		TdSchema schema=new TdSchema();
		deserializeSchemaAttributes(xschema, schema);
		for (XNode tnode : xschema.getChildren(ENTITY_NODE)) {
			TdEntity entity=new TdEntity();
			deserializeEntityAttributes(tnode, entity);
			deserializeEntities(tnode, entity);
			deserializeChecks(tnode, entity);
			deserializeDdls(tnode, entity);
			schema.addEntitiesItem(entity);
		}
		return schema;
	}
	private void deserializeSchemaAttributes(XNode xschema, TdSchema schema) {
		schema.setStoretype(xschema.getAttribute(DBMS));
		schema.setCatalog(xschema.getAttribute("catalog"));
		schema.setSchema(xschema.getAttribute("schema"));
	}
	private void deserializeEntityAttributes(XNode tnode, TdEntity entity) {
		entity.setName(tnode.getAttribute(NAME));
		entity.setEntitytype(tnode.getAttribute(ENTITY_TYPE));
		entity.setSubtype(tnode.getAttribute(SUBTYPE));
		for (String attr : getExtendedAttributeNames(tnode, new String[] {NAME,ENTITY_TYPE,SUBTYPE}))
			entity.putExtendedItem(attr, tnode.getAttribute(attr));
	}
	private void deserializeEntities(XNode tnode, TdEntity entity) {
		for (XNode cnode : tnode.getChildren(ATTRIBUTE_NODE)) {
			TdAttribute attribute=new TdAttribute();
			deserializeAttributeDescriptors(cnode, attribute);
			entity.addAttributesItem(attribute);
		}
	}
	private void deserializeAttributeDescriptors(XNode cnode, TdAttribute attribute) {
		attribute.setName(cnode.getAttribute(NAME));
		attribute.setDatatype(cnode.getAttribute(DATA_TYPE));
		attribute.setCompositetype(cnode.getAttribute(COMPOSITETYPE));
		attribute.setSubtype(cnode.getAttribute(SUBTYPE));
		attribute.setSize(cnode.getAttribute(SIZE));
		attribute.setUid(cnode.getAttribute(UID));
		attribute.setAutoincrement(cnode.getAttribute(AUTOINCREMENT));
		attribute.setNotnull(cnode.getAttribute(NOTNULL));
		attribute.setReadonly(cnode.getAttribute(READONLY));
		attribute.setRid(cnode.getAttribute(RID));
		attribute.setRidname(cnode.getAttribute(RID_NAME));
		attribute.setCheckin(cnode.getAttribute(CHECKIN));
		attribute.setDefaultvalue(cnode.getAttribute(DEFAULT));
		for (String attr : getExtendedAttributeNames(cnode, new String[] {NAME,DATA_TYPE,COMPOSITETYPE,SUBTYPE,SIZE,UID,AUTOINCREMENT,NOTNULL,READONLY,RID,RID_NAME,CHECKIN,DEFAULT}))
			attribute.putExtendedItem(attr, cnode.getAttribute(attr));
	}
	private void deserializeChecks(XNode tnode, TdEntity entity) {
		for (XNode cnode : tnode.getChildren(CHECK_CONSTRAINT)) {
			TdCheck check=new TdCheck();
			check.setAttribute(cnode.getAttribute(ATTRIBUTE_NODE));
			check.setName(cnode.getAttribute(NAME));
			check.setConstraint(XNode.decodeText(cnode.innerText()));
			entity.addChecksItem(check);
		}
	}
	private void deserializeDdls(XNode tnode, TdEntity entity) {
		for (XNode cnode : tnode.getChildren(DDL)) {
			Ddl ddl=new Ddl();
			ddl.setCommand(cnode.getAttribute(DDL_COMMAND_NODE));
			ddl.setQuery(XNode.decodeText(cnode.innerText()));
			entity.addDdlsItem(ddl);
		}
	}
	
	public String serialize(TdSchema sch) {
		StringBuilder sb=new StringBuilder();
		sb.append("<schema")
			.append(setAttribute(DBMS, sch.getStoretype()))
			.append(setAttribute("catalog", sch.getCatalog()))
			.append(setAttribute("schema", sch.getSchema()))
			.append(">");
		for (TdEntity entity : safe(sch.getEntities()))
			appendEntity(sb, entity);
		sb.append("\n</schema>");
		return sb.toString();
	}
	protected void appendEntity(StringBuilder sb, TdEntity entity) {
		sb.append("\n<table")
			.append(setAttribute(NAME, entity.getName()))
			.append(setAttribute(ENTITY_TYPE, entity.getEntitytype()))
			.append(setAttribute(SUBTYPE, entity.getSubtype()))
			.append(setExtendedAttributes(entity.getExtended()))
			.append(">");
		for (TdAttribute attribute : safe(entity.getAttributes()))
			appendAttribute(sb, attribute);
		for (TdCheck check : safe(entity.getChecks()))
			//no serializa el nombre del check, no se utiliza desde xml
			sb.append("\n<check")
				.append(setAttribute(ATTRIBUTE_NODE, check.getAttribute()))
				.append(">")
				.append(XNode.encodeText(check.getConstraint()))
				.append("</check>");
		for (Ddl ddl : safe(entity.getDdls()))
			sb.append("\n<ddl")
				.append(setAttribute(DDL_COMMAND_NODE, ddl.getCommand()))
				.append(">")
				.append(XNode.encodeText(ddl.getQuery()))
				.append("</ddl>");
		sb.append("\n</table>");
	}
	protected void appendAttribute(StringBuilder sb, TdAttribute attribute) {
		sb.append("\n<column")
		.append(setAttribute(NAME, attribute.getName()))
		.append(setAttribute(DATA_TYPE, attribute.getDatatype()))
		.append(setAttribute(COMPOSITETYPE, attribute.getCompositetype()))
		.append(setAttribute(SUBTYPE, attribute.getSubtype()))
		.append(setAttribute(SIZE, attribute.getSize()))
		.append(setAttribute(UID, attribute.getUid()))
		.append(setAttribute(AUTOINCREMENT, attribute.getAutoincrement()))
		.append(setAttribute(NOTNULL, attribute.getNotnull()))
		.append(setAttribute(READONLY, attribute.getReadonly()))
		.append(setAttribute(RID, attribute.getRid()))
		.append(setAttribute(RID_NAME, attribute.getRidname()))
		.append(setAttribute(CHECKIN, attribute.getCheckin()))
		.append(setAttribute(DEFAULT, attribute.getDefaultvalue()))
		.append(setExtendedAttributes(attribute.getExtended()))
		.append(" />");
	}

}
