package giis.tdrules.client.rdb;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import giis.tdrules.model.TableTypes;
import giis.tdrules.openapi.model.DbCheck;
import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;
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
	 * allowing filtering by the type of tables
	 */
	public DbSchema getDbSchema(boolean includeTables, boolean includeViews, boolean includeTypes,
			String startingWith) {
		if (sr == null) // lazy creation to support catealog/schema changes
			sr = new SchemaReaderJdbc(conn, catalog, schema).setUseCache(true);
		List<String> tableNames = sr.getTableList(includeTables, includeViews, includeTypes, startingWith);
		return getDbSchema(tableNames);
	}

	/**
	 * Gets the database schema for the current instance including the specified tables only
	 */
	public DbSchema getDbSchema(List<String> tables) {
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
			if (TableTypes.DT_TYPE.equals(column.getCompositeType()))
				writeTable(column.getDataType(), sr, sw);
	}

	/**
	 * Gets a string representation of the model,
	 * intended to facilitate comparison in different platforms (java/net)
	 */
	public String modelToString(DbSchema model) {
		StringBuilder sb = new StringBuilder();
		sb.append("SCHEMA").append(" dbms:").append(model.getDbms()).append(" catalog:").append(model.getCatalog())
				.append(" schema:").append(model.getSchema());
		for (DbTable table : model.getTables())
			appendTable(sb, table);
		return sb.toString();
	}

	private void appendTable(StringBuilder sb, DbTable table) {
		sb.append("\nTABLE").append(NAME_PROMPT).append(table.getName()).append(" type:").append(table.getTabletype());
		for (DbColumn column : table.getColumns()) {
			sb.append("\n  COLUMN")
				.append(NAME_PROMPT).append(column.getName())
				.append(" datatype:").append(column.getDatatype())
				.append(" compositetype:").append(column.getCompositetype())
				.append(" subtype:").append(column.getSubtype())
				.append(" size:").append(column.getSize())
				.append(" key:").append(column.getKey())
				.append(" notnull:").append(column.getNotnull())
				.append(" fk:").append(column.getFk())
				.append(" fkname:").append(column.getFkname())
				.append(" checkin:").append(column.getCheckin())
				.append(" defaultvalue:").append(column.getDefaultvalue());
		}
		for (DbCheck check : table.getChecks() == null ? new ArrayList<DbCheck>() : table.getChecks()) {
			sb.append("\n  CHECK")
				.append(" column:").append(check.getColumn())
				.append(NAME_PROMPT).append(check.getName())
				.append(" constraint:").append(check.getConstraint());
		}
	}

}
