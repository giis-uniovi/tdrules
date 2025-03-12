package giis.tdrules.store.rdb;

import giis.tdrules.store.stypes.StoreType;

/**
 * Base class to generate the schema of a database,
 * only implements basic methods to identify the schema
 */
public abstract class SchemaReaderAbstract {
	// nombre del catalogo y esquema por defecto del modelo si no se especifican seran vacios
	private String catalog = "";
	private String schema = "";
	private StoreType dbmsType = StoreType.get(); // Identifiacion del DBMS

	/** Obtiene el objeto con las particularidades de base de datos actual */
	public StoreType getDbmsType() {
		return dbmsType;
	}

	protected void setDbmsType(String dbmsname) {
		this.dbmsType = StoreType.get(dbmsname);
	}

	protected void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getCatalog() {
		return this.catalog;
	}

	protected void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		return this.schema;
	}

}
