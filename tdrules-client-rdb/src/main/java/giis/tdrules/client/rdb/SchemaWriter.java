package giis.tdrules.client.rdb;

import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.store.rdb.SchemaReader;

/**
 * Manages the different stages of the creation of DbSchema model object from a SchemaReader
 * (begin, writing columns, end)
 */
public class SchemaWriter {
	protected TdSchema model;
	protected TdEntity currentTable;
	
	public SchemaWriter(SchemaReader sr) {
		model = new TdSchema();
		model.setCatalog(sr.getCatalog());
		model.setSchema(sr.getSchema());
		model.setStoretype(sr.getDbmsType().toString());
	}

	public void beginWriteTable(String tabName, String tableType) {
		currentTable = new TdEntity();
		currentTable.setName(tabName);
		currentTable.setEntitytype(tableType);
	}

	public void endWriteTable() {
		model.addEntitiesItem(currentTable);
		currentTable = null;
	}
	
	public void writeColumn(String colName, String colDataType, String colSubType, //NOSONAR all parameters needed, simpler than a builder
			int colSize, int decimalDigits, boolean isKey, boolean isAutoincrement, boolean isNotNull, String foreignKey, String foreignKeyName, 
			String checkIn, String defaultValue) {
		TdAttribute col = new TdAttribute();
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
			col.setUid("true");
		if (isNotNull)
			col.setNotnull("true");
		col.setRid(foreignKey);
		col.setRidname(foreignKeyName);
		col.setCheckin(checkIn);
		col.setDefaultvalue(defaultValue);
		currentTable.addAttributesItem(col);
	}

	public void writeCheckConstraint(String colName, String constraintName, String constraint) {
		TdCheck check = new TdCheck();
		check.setAttribute(colName);
		check.setName(constraintName);
		check.setConstraint(constraint);
		currentTable.addChecksItem(check);
	}
	
	/**
	 * Returns the model that has been writen in this object
	 */
	public TdSchema getModel() {
		return this.model;
	}

}
