package giis.tdrules.model.io;

import static giis.tdrules.model.ModelUtil.safe;

import giis.portable.xml.tiny.XNode;
import giis.tdrules.openapi.model.DbCheck;
import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;
import giis.tdrules.openapi.model.Ddl;

/**
 * Custom xml serialization/deserialization of a schema model
 */
public class DbSchemaXmlSerializer extends BaseXmlSerializer {
	private static final String TABLE_NODE = "table";
	private static final String COLUMN_NODE = "column";
	private static final String NAME = "name";
	private static final String TYPE = "type";
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
	private static final String DDL_COMMAND_NODE = "command";
	
	public DbSchema deserialize(String xml) {
		XNode xschema=new XNode(xml);
		DbSchema schema=new DbSchema();
		deserializeDbSchemaAttributes(xschema, schema);
		for (XNode tnode : xschema.getChildren(TABLE_NODE)) {
			DbTable table=new DbTable();
			deserializeDbTableAttributes(tnode, table);
			deserializeDbColumns(tnode, table);
			deserializeDbChecks(tnode, table);
			deserializeDdls(tnode, table);
			schema.addTablesItem(table);
		}
		return schema;
	}
	private void deserializeDbSchemaAttributes(XNode xschema, DbSchema schema) {
		schema.setDbms(xschema.getAttribute("dbms"));
		schema.setCatalog(xschema.getAttribute("catalog"));
		schema.setSchema(xschema.getAttribute("schema"));
	}
	private void deserializeDbTableAttributes(XNode tnode, DbTable table) {
		table.setName(tnode.getAttribute(NAME));
		table.setTabletype(tnode.getAttribute(TYPE));
		for (String attr : getExtendedAttributeNames(tnode, new String[] {NAME,TYPE}))
			table.putExtendedItem(attr, tnode.getAttribute(attr));
	}
	private void deserializeDbColumns(XNode tnode, DbTable table) {
		for (XNode cnode : tnode.getChildren(COLUMN_NODE)) {
			DbColumn column=new DbColumn();
			deserializeDbColumnAttributes(cnode, column);
			table.addColumnsItem(column);
		}
	}
	private void deserializeDbColumnAttributes(XNode cnode, DbColumn column) {
		column.setName(cnode.getAttribute(NAME));
		column.setDatatype(cnode.getAttribute(TYPE));
		column.setCompositetype(cnode.getAttribute(COMPOSITETYPE));
		column.setSubtype(cnode.getAttribute(SUBTYPE));
		column.setSize(cnode.getAttribute(SIZE));
		column.setKey(cnode.getAttribute(KEY));
		column.setAutoincrement(cnode.getAttribute(AUTOINCREMENT));
		column.setNotnull(cnode.getAttribute(NOTNULL));
		column.setFk(cnode.getAttribute(FK));
		column.setFkname(cnode.getAttribute(FKNAME));
		column.setCheckin(cnode.getAttribute(CHECKIN));
		column.setDefaultvalue(cnode.getAttribute(DEFAULT));
		for (String attr : getExtendedAttributeNames(cnode, new String[] {NAME,TYPE,COMPOSITETYPE,SUBTYPE,SIZE,KEY,AUTOINCREMENT,NOTNULL,FK,FKNAME,CHECKIN,DEFAULT}))
			column.putExtendedItem(attr, cnode.getAttribute(attr));
	}
	private void deserializeDbChecks(XNode tnode, DbTable table) {
		for (XNode cnode : tnode.getChildren("check")) {
			DbCheck check=new DbCheck();
			check.setColumn(cnode.getAttribute(COLUMN_NODE));
			check.setName(cnode.getAttribute(NAME));
			check.setConstraint(XNode.decodeText(cnode.innerText()));
			table.addChecksItem(check);
		}
	}
	private void deserializeDdls(XNode tnode, DbTable table) {
		for (XNode cnode : tnode.getChildren("ddl")) {
			Ddl ddl=new Ddl();
			ddl.setCommand(cnode.getAttribute(DDL_COMMAND_NODE));
			ddl.setSql(XNode.decodeText(cnode.innerText()));
			table.addDdlsItem(ddl);
		}
	}
	
	public String serialize(DbSchema sch) {
		StringBuilder sb=new StringBuilder();
		sb.append("<schema")
			.append(setAttribute("dbms", sch.getDbms()))
			.append(setAttribute("catalog", sch.getCatalog()))
			.append(setAttribute("schema", sch.getSchema()))
			.append(">");
		for (DbTable table : safe(sch.getTables()))
			appendTable(sb, table);
		sb.append("\n</schema>");
		return sb.toString();
	}
	protected void appendTable(StringBuilder sb, DbTable table) {
		sb.append("\n<table")
			.append(setAttribute(NAME, table.getName()))
			.append(setAttribute(TYPE, table.getTabletype()))
			.append(setExtendedAttributes(table.getExtended()))
			.append(">");
		for (DbColumn column : safe(table.getColumns()))
			appendColumn(sb, column);
		for (DbCheck check : safe(table.getChecks()))
			//no serializa el nombre del check, no se utiliza desde xml
			sb.append("\n<check")
				.append(setAttribute(COLUMN_NODE, check.getColumn()))
				.append(">")
				.append(XNode.encodeText(check.getConstraint()))
				.append("</check>");
		for (Ddl ddl : safe(table.getDdls()))
			sb.append("\n<ddl")
				.append(setAttribute(DDL_COMMAND_NODE, ddl.getCommand()))
				.append(">")
				.append(XNode.encodeText(ddl.getSql()))
				.append("</ddl>");
		sb.append("\n</table>");
	}
	protected void appendColumn(StringBuilder sb, DbColumn column) {
		sb.append("\n<column")
		.append(setAttribute(NAME, column.getName()))
		.append(setAttribute(TYPE, column.getDatatype()))
		.append(setAttribute(COMPOSITETYPE, column.getCompositetype()))
		.append(setAttribute(SUBTYPE, column.getSubtype()))
		.append(setAttribute(SIZE, column.getSize()))
		.append(setAttribute(KEY, column.getKey()))
		.append(setAttribute(AUTOINCREMENT, column.getAutoincrement()))
		.append(setAttribute(NOTNULL, column.getNotnull()))
		.append(setAttribute(FK, column.getFk()))
		.append(setAttribute(FKNAME, column.getFkname()))
		.append(setAttribute(CHECKIN, column.getCheckin()))
		.append(setAttribute(DEFAULT, column.getDefaultvalue()))
		.append(setExtendedAttributes(column.getExtended()))
		.append(" />");
	}

}
