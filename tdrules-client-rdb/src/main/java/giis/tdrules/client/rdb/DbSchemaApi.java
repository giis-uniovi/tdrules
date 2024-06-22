package giis.tdrules.client.rdb;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import giis.tdrules.model.EntityTypes;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.store.rdb.SchemaCheckConstraint;
import giis.tdrules.store.rdb.SchemaColumn;
import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.tdrules.store.rdb.SchemaTable;

/**
 * Client api to get the schema of a relational data store
 */
public class DbSchemaApi {
	private static final String NAME_PROMPT = " name:";
	private Connection conn;
	// Lazy creation of the schema reader created lazy,
	// any change in catalog/schema will reset it
	private SchemaReader sr = null;
	private String catalog = "";
	private String schema = "";

	/**
	 * New instance for a given jdbc connection
	 */
	public DbSchemaApi(Connection conn) {
		this.conn = conn;
	}

	/**
	 * New instance given a schema reader, only used for compatibility with legacy code
	 */
	public DbSchemaApi(SchemaReader sr) {
		this.sr = sr;
		this.conn = sr.getDb();
		this.catalog = sr.getCatalog();
		this.schema = sr.getSchema();
	}

	/**
	 * Restrict the scope of metadata search to the specified catalog and schema
	 */
	public DbSchemaApi setCatalogAndSchema(String catalog, String schema) {
		this.catalog = catalog;
		this.schema = schema;
		this.sr = null; // to recreate with the new scope
		return this;
	}

	/**
	 * Gets the database schema for the current instance for the whole database,
	 * allowing filtering by the kind of objects to get
	 */
	public TdSchema getSchema() {
		return getSchema(true, true, true, "");
	}

	/**
	 * Gets the database schema for the current instance for the whole database,
	 * allowing filtering by the kind of objects to get
	 */
	public TdSchema getSchema(boolean includeTables, boolean includeViews, boolean includeTypes, String startingWith) {
		if (sr == null) // lazy creation to support catalog/schema changes
			sr = new SchemaReaderJdbc(conn, catalog, schema).setUseCache(true);
		List<String> tableNames = sr.getTableList(includeTables, includeViews, includeTypes, startingWith);
		return getSchema(tableNames);
	}

	/**
	 * Gets the database schema for the current instance including the specified tables only
	 */
	public TdSchema getSchema(List<String> tables) {
		if (sr == null)
			sr = new SchemaReaderJdbc(conn, catalog, schema).setUseCache(true);
		SchemaWriter sw = new SchemaWriter(sr);

		for (String table : tables)
			if (table != null && !table.trim().equals("")) // ensures there is a table name
				writeTable(table, sr, sw);

		// Second scan to get the schema of UDT included in tables, if any
		for (String table : tables)
			if (table != null && !table.trim().equals("")) // asegura que hay nombre de tabla
				writeReferencedTypes(table, sr, sw);

		return sw.getModel();
	}

	/**
	 * @deprecated Use getSchema
	 */
	@Deprecated
	public TdSchema getDbSchema() {
		return getSchema();
	}

	/**
	 * @deprecated Use getSchema
	 */
	@Deprecated
	public TdSchema getDbSchema(boolean includeTables, boolean includeViews, boolean includeTypes, String startingWith) {
		return getSchema(includeTables, includeViews, includeTypes, startingWith);
	}

	/**
	 * @deprecated Use getSchema
	 */
	@Deprecated
	public TdSchema getDbSchema(List<String> tables) {
		return getSchema(tables);
	}

	private void writeTable(String tableName, SchemaReader reader, SchemaWriter writer) {
		reader.readTable(tableName);
		writer.beginWriteTable(reader.getDefaultQualifiedTableName(), reader.getCurrentTable().getTableType());
		for (int i = 0; i < reader.getCurrentTable().getColumns().size(); i++) {
			SchemaColumn col = reader.getColumn(i);
			writer.writeColumn(col.getColName(), col.getDataType(), col.getDataSubType(), col.getColSize(),
					col.getDecimalDigits(), col.isKey(), col.isAutoIncrement(), col.isNotNull(), col.getForeignKey(),
					col.getForeignKeyName(), col.getCheckInConstraint(), col.getDefaultValue());
		}
		for (int i = 0; i < reader.getCurrentTable().getCheckConstraints().size(); i++) {
			SchemaCheckConstraint check = reader.getCurrentTable().getCheckConstraints().get(i);
			writer.writeCheckConstraint(check.getColumn(), check.getName(), check.getConstraint());
		}
		writer.endWriteTable();
	}

	private void writeReferencedTypes(String table, SchemaReader sr, SchemaWriter sw) {
		SchemaTable tab = sr.readTable(table);
		for (SchemaColumn column : tab.getColumns())
			if (EntityTypes.DT_TYPE.equals(column.getCompositeType()))
				writeTable(column.getDataType(), sr, sw);
	}

	/**
	 * Gets a string representation of the model,
	 * intended to facilitate comparison in different platforms (java/net)
	 */
	public String modelToString(TdSchema model) {
		StringBuilder sb = new StringBuilder();
		sb.append("SCHEMA").append(" dbms:").append(model.getStoretype()).append(" catalog:").append(model.getCatalog())
				.append(" schema:").append(model.getSchema());
		for (TdEntity table : model.getEntities())
			appendTable(sb, table);
		return sb.toString();
	}

	private void appendTable(StringBuilder sb, TdEntity table) {
		sb.append("\nTABLE").append(NAME_PROMPT).append(table.getName()).append(" type:").append(table.getEntitytype());
		for (TdAttribute column : table.getAttributes()) {
			sb.append("\n  COLUMN")
				.append(NAME_PROMPT).append(column.getName())
				.append(" datatype:").append(column.getDatatype())
				.append(" compositetype:").append(column.getCompositetype())
				.append(" subtype:").append(column.getSubtype())
				.append(" size:").append(column.getSize())
				.append(" key:").append(column.getUid())
				.append(" notnull:").append(column.getNotnull())
				.append(" fk:").append(column.getRid())
				.append(" fkname:").append(column.getRidname())
				.append(" checkin:").append(column.getCheckin())
				.append(" defaultvalue:").append(column.getDefaultvalue());
		}
		for (TdCheck check : table.getChecks() == null ? new ArrayList<TdCheck>() : table.getChecks()) {
			sb.append("\n  CHECK")
				.append(" column:").append(check.getAttribute())
				.append(NAME_PROMPT).append(check.getName())
				.append(" constraint:").append(check.getConstraint());
		}
	}

}
