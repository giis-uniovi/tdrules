package giis.tdrules.client.rdb;

import giis.tdrules.openapi.model.DbCheck;
import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;
import giis.tdrules.store.rdb.SchemaReader;

/**
 * Manages the different stages of the creation of DbSchema model object from a SchemaReader
 * (begin, writing columns, end)
 */
public class SchemaWriter {
	protected DbSchema model;
	protected DbTable currentTable;
	
	public SchemaWriter(SchemaReader sr) {
		model = new DbSchema();
		model.setCatalog(sr.getCatalog());
		model.setSchema(sr.getSchema());
		model.setDbms(sr.getDbmsType().toString());
	}

	public void beginWriteTable(String tabName, String tableType) {
		currentTable = new DbTable();
		currentTable.setName(tabName);
		currentTable.setTabletype(tableType);
	}

	public void endWriteTable() {
		model.addTablesItem(currentTable);
		currentTable = null;
	}
	
	public void writeColumn(String colName, String colDataType, String colSubType, //NOSONAR all parameters needed, simpler than a builder
			int colSize, int decimalDigits, boolean isKey, boolean isAutoincrement, boolean isNotNull, String foreignKey, String foreignKeyName, 
			String checkIn, String defaultValue) {
		DbColumn col = new DbColumn();
		col.setName(colName);
		col.setDatatype(colDataType);
		col.setSubtype(colSubType);
		
		String size = "";
		if (colSize > 0) {
			size = String.valueOf(colSize);
			if (decimalDigits != 0)
				size += "," + decimalDigits;
		}
		col.setSize(size);
		
		if (isAutoincrement)
			col.setAutoincrement("true");
		if (isKey)
			col.setKey("true");
		if (isNotNull)
			col.setNotnull("true");
		col.setFk(foreignKey);
		col.setFkname(foreignKeyName);
		col.setCheckin(checkIn);
		col.setDefaultvalue(defaultValue);
		currentTable.addColumnsItem(col);
	}

	public void writeCheckConstraint(String colName, String constraintName, String constraint) {
		DbCheck check = new DbCheck();
		check.setColumn(colName);
		check.setName(constraintName);
		check.setConstraint(constraint);
		currentTable.addChecksItem(check);
	}
	
	/**
	 * Returns the model that has been writen in this object
	 */
	public DbSchema getModel() {
		return this.model;
	}

}
