package giis.tdrules.model.io;

import static giis.tdrules.model.ModelUtil.safe;

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
	private static final String TABLE_NODE = "table";
	private static final String COLUMN_NODE = "column";
	private static final String NAME = "name";
	private static final String ENTITY_TYPE = "type";
	private static final String DATA_TYPE = "type";
	private static final String COMPOSITETYPE = "compositetype";
	private static final String SUBTYPE = "subtype";
	private static final String SIZE = "size";
	private static final String KEY = "key";
	private static final String AUTOINCREMENT = "autoincrement";
	private static final String NOTNULL = "notnull";
	private static final String FK = "fk";
	private static final String FKNAME = "fkname";
	private static final String CHECKIN = "checkin";
	private static final String DEFAULT = "default";
	private static final String CHECK_CONSTRAINT = "check";
	private static final String DDL_COMMAND_NODE = "command";
	private static final String DDL = "ddl";
	
	public TdSchema deserialize(String xml) {
		XNode xschema=new XNode(xml);
		TdSchema schema=new TdSchema();
		deserializeDbSchemaAttributes(xschema, schema);
		for (XNode tnode : xschema.getChildren(TABLE_NODE)) {
			TdEntity table=new TdEntity();
			deserializeDbTableAttributes(tnode, table);
			deserializeDbColumns(tnode, table);
			deserializeDbChecks(tnode, table);
			deserializeDdls(tnode, table);
			schema.addEntitiesItem(table);
		}
		return schema;
	}
	private void deserializeDbSchemaAttributes(XNode xschema, TdSchema schema) {
		schema.setStoretype(xschema.getAttribute(DBMS));
		schema.setCatalog(xschema.getAttribute("catalog"));
		schema.setSchema(xschema.getAttribute("schema"));
	}
	private void deserializeDbTableAttributes(XNode tnode, TdEntity table) {
		table.setName(tnode.getAttribute(NAME));
		table.setEntitytype(tnode.getAttribute(ENTITY_TYPE));
		table.setSubtype(tnode.getAttribute(SUBTYPE));
		for (String attr : getExtendedAttributeNames(tnode, new String[] {NAME,ENTITY_TYPE,SUBTYPE}))
			table.putExtendedItem(attr, tnode.getAttribute(attr));
	}
	private void deserializeDbColumns(XNode tnode, TdEntity table) {
		for (XNode cnode : tnode.getChildren(COLUMN_NODE)) {
			TdAttribute column=new TdAttribute();
			deserializeDbColumnAttributes(cnode, column);
			table.addAttributesItem(column);
		}
	}
	private void deserializeDbColumnAttributes(XNode cnode, TdAttribute column) {
		column.setName(cnode.getAttribute(NAME));
		column.setDatatype(cnode.getAttribute(DATA_TYPE));
		column.setCompositetype(cnode.getAttribute(COMPOSITETYPE));
		column.setSubtype(cnode.getAttribute(SUBTYPE));
		column.setSize(cnode.getAttribute(SIZE));
		column.setUid(cnode.getAttribute(KEY));
		column.setAutoincrement(cnode.getAttribute(AUTOINCREMENT));
		column.setNotnull(cnode.getAttribute(NOTNULL));
		column.setRid(cnode.getAttribute(FK));
		column.setRidname(cnode.getAttribute(FKNAME));
		column.setCheckin(cnode.getAttribute(CHECKIN));
		column.setDefaultvalue(cnode.getAttribute(DEFAULT));
		for (String attr : getExtendedAttributeNames(cnode, new String[] {NAME,DATA_TYPE,COMPOSITETYPE,SUBTYPE,SIZE,KEY,AUTOINCREMENT,NOTNULL,FK,FKNAME,CHECKIN,DEFAULT}))
			column.putExtendedItem(attr, cnode.getAttribute(attr));
	}
	private void deserializeDbChecks(XNode tnode, TdEntity table) {
		for (XNode cnode : tnode.getChildren(CHECK_CONSTRAINT)) {
			TdCheck check=new TdCheck();
			check.setAttribute(cnode.getAttribute(COLUMN_NODE));
			check.setName(cnode.getAttribute(NAME));
			check.setConstraint(XNode.decodeText(cnode.innerText()));
			table.addChecksItem(check);
		}
	}
	private void deserializeDdls(XNode tnode, TdEntity table) {
		for (XNode cnode : tnode.getChildren(DDL)) {
			Ddl ddl=new Ddl();
			ddl.setCommand(cnode.getAttribute(DDL_COMMAND_NODE));
			ddl.setQuery(XNode.decodeText(cnode.innerText()));
			table.addDdlsItem(ddl);
		}
	}
	
	public String serialize(TdSchema sch) {
		StringBuilder sb=new StringBuilder();
		sb.append("<schema")
			.append(setAttribute(DBMS, sch.getStoretype()))
			.append(setAttribute("catalog", sch.getCatalog()))
			.append(setAttribute("schema", sch.getSchema()))
			.append(">");
		for (TdEntity table : safe(sch.getEntities()))
			appendTable(sb, table);
		sb.append("\n</schema>");
		return sb.toString();
	}
	protected void appendTable(StringBuilder sb, TdEntity table) {
		sb.append("\n<table")
			.append(setAttribute(NAME, table.getName()))
			.append(setAttribute(ENTITY_TYPE, table.getEntitytype()))
			.append(setAttribute(SUBTYPE, table.getSubtype()))
			.append(setExtendedAttributes(table.getExtended()))
			.append(">");
		for (TdAttribute column : safe(table.getAttributes()))
			appendColumn(sb, column);
		for (TdCheck check : safe(table.getChecks()))
			//no serializa el nombre del check, no se utiliza desde xml
			sb.append("\n<check")
				.append(setAttribute(COLUMN_NODE, check.getAttribute()))
				.append(">")
				.append(XNode.encodeText(check.getConstraint()))
				.append("</check>");
		for (Ddl ddl : safe(table.getDdls()))
			sb.append("\n<ddl")
				.append(setAttribute(DDL_COMMAND_NODE, ddl.getCommand()))
				.append(">")
				.append(XNode.encodeText(ddl.getQuery()))
				.append("</ddl>");
		sb.append("\n</table>");
	}
	protected void appendColumn(StringBuilder sb, TdAttribute column) {
		sb.append("\n<column")
		.append(setAttribute(NAME, column.getName()))
		.append(setAttribute(DATA_TYPE, column.getDatatype()))
		.append(setAttribute(COMPOSITETYPE, column.getCompositetype()))
		.append(setAttribute(SUBTYPE, column.getSubtype()))
		.append(setAttribute(SIZE, column.getSize()))
		.append(setAttribute(KEY, column.getUid()))
		.append(setAttribute(AUTOINCREMENT, column.getAutoincrement()))
		.append(setAttribute(NOTNULL, column.getNotnull()))
		.append(setAttribute(FK, column.getRid()))
		.append(setAttribute(FKNAME, column.getRidname()))
		.append(setAttribute(CHECKIN, column.getCheckin()))
		.append(setAttribute(DEFAULT, column.getDefaultvalue()))
		.append(setExtendedAttributes(column.getExtended()))
		.append(" />");
	}

}
