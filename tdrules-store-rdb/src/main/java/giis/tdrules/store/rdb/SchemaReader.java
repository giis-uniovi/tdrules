package giis.tdrules.store.rdb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import giis.portable.util.JavaCs;
import giis.tdrules.store.ids.TableIdentifier;

/**
 * Common methods to generate a database schema.
 * 
 * Once instantiated, each call to getTable stores the metadata 
 * of the indicated table, that can be read with the getters.
 * Only a single table is read and stored at each moment
 * to get all tables a schema writer must be used
 */
public abstract class SchemaReader extends SchemaReaderAbstract {
	
	//Datos almacenados de la ultima tabla leida
	protected SchemaTable currentTable=null;
	
	// Nombres de drivers, bd y versiones identificativos de la plataforma
	private String platformInfo="unknown";
	private int majorVersion=0;
	
	// Define si utiliza cache para minimizar las consulas a metadatos cuando
	// se buscan tablas, la implementacion estara en las subclases
	protected boolean useCache = false;

	// base de datos donde reside el esquema fisico (desde donde se lee)
	protected Connection db = null; 

	protected SchemaReader() {
		this.resetAttributes();
	}

	/**
	 * Especifica si se usara cache, en caso afirmativo las lecturas de las tablas
	 * se realizan una sola vez contra la BD y en las siguientes se usa la cache
	 * evitando lecturas innecesarias, la implementacion ira en las subclases
	 */
	public SchemaReader setUseCache(boolean useCache) {
		this.useCache = useCache;
		return this;
	}

	public boolean getUseCache() {
		return this.useCache;
	}

	public SchemaTable getCurrentTable() {
		return currentTable;
	}

	/** Obtiene un string con la identificacion completa de la plataforma */
	public String getPlatformInfo() {
		return this.platformInfo;
	}
	/** Actualiza el string con la identificacion completa de la plataforma */
	public void setPlatformInfo(String info) {
		this.platformInfo = info;
	}
	/** Obtiene el numero de version mayor del DBMS (0 si no se puede obtener) */
	protected void setMajorVersion(int version) {
		this.majorVersion = version;
	}
	/** Obtiene el numero de version mayor del DBMS (0 si no se puede obtener) */
	public int getMajorVersion() {
		return this.majorVersion;
	}

	public Connection getDb() {
		return this.db;
	}

	// Determinacion del tipo de base de datos
	public boolean isOracle() {
		return this.getDbmsType().isOracle();
	}
	public boolean isSQLServer() {
		return this.getDbmsType().isSQLServer();
	}
	public boolean isPostgres() {
		return this.getDbmsType().isPostgres();
	}
	public boolean isSqlite() {
		return this.getDbmsType().isSqlite();
	}

	public boolean isTable() {
		return this.currentTable.isTable();
	}
	public boolean isView() {
		return this.currentTable.isView();
	}
	public boolean isType() {
		return this.currentTable.isType();
	}
	
	public SchemaColumn getColumn(int index) {
		return this.currentTable.getColumns().get(index);
	}
	public int getColumnCount() {
		return this.currentTable.getColumns().size();
	}

	/** Nombre de la tabla actual nombre completamente qualificado */
	public String getFullQualifiedTableName() {
		return this.currentTable.getGivenId().getFullQualifiedTableName(this.getCatalog(), this.getSchema());
	}
	/** Nombre de la tabla actual sin qualificar */
	public String getTableName() {
		return this.currentTable.getGivenId().getTab();
	}
	public String getTableCatalog() {
		return this.currentTable.getGivenId().getCat();
	}
	public String getTableSchema() {
		return this.currentTable.getGivenId().getSch();
	}

	/**
	 * Nombre completamente qualificado excluyendo indicar catalogo y esquema cuando
	 * tienen los valores por defecto del esquema de esta tabla
	 */
	public String getDefaultQualifiedTableName() {
		return this.currentTable.getGivenId().getDefaultQualifiedTableName(this.getCatalog(), this.getSchema());
	}

	/**
	 * Nombre global, completamente cualificado tal y como han sido obtenidos los
	 * datos de Metadata
	 */
	public String getGlobalName() {
		return this.currentTable.getGlobalId().getFullQualifiedTableName();
	}

	protected void setTableGivenId(TableIdentifier table) {
		this.currentTable.setGivenId(table);
	}
	protected void setTableGivenName(String tableName) {
		this.currentTable.getGivenId().setTab(tableName);
	}
	protected void setTableGlobalId(TableIdentifier id) {
		this.currentTable.setGlobalId(id);
	}
	protected void setTableTypeTable() {
		this.currentTable.setTableType("table");
	}
	protected void setTableTypeView() {
		this.currentTable.setTableType("view");
	}
	protected void setTableTypeUdt() {
		this.currentTable.setTableType("type");
	}

	protected void setTableCatalogSchema(String catalog, String schema) {
		this.currentTable.setCatalog(catalog == null ? "" : catalog);
		this.currentTable.setSchema(schema == null ? "" : schema);
	}

	protected void addColumn(SchemaColumn col) {
		this.currentTable.getColumns().add(col);
	}

	/** Reset de todos los atributos de una tabla */
	protected void resetAttributes() {
		currentTable = new SchemaTable(this);
	}

	/**
	 * Lee la lista de todas las tablas, vistas y/o tipos definidos por el usuario
	 * (ROW en sql92) de la base de datos cuyo nombre comienza por el string indicado.
	 */
	public abstract List<String> getTableList(boolean includeTables, boolean includeViews, boolean includeTypes,
			String startingWith);

	/**
	 * Lee la lista de todas las tablas y/o vistas de la base de datos cuyo nombre
	 * comienza por el string indicado.
	 */
	public abstract List<String> getTableList(boolean includeTables, boolean includeViews, String startingWith);

	/**
	 * Lee la lista de todas las tablas y/o vistas de la base de datos.
	 */
	public abstract List<String> getTableList(boolean includeTables, boolean includeViews);

	/**
	 * Lee todos los metadatos de la tabla o vista indicada manteniendolso
	 * inernamente para que puedan ser consultados
	 */
	public abstract SchemaTable readTable(String tabName, boolean throwExceptionIfNotFound);

	public SchemaTable readTable(String tabName) {
		return readTable(tabName, true);
	}

	/**
	 * Obtiene la lista de tablas formada por las de entrada (tables) y todas las
	 * que estas referencian (recursivamente). Incluye indistintamente tablas y vistas
	 */
	public List<String> getTableListAndDependent(List<String> tables) {
		// examina cada tabla de la lista y para cada una de sus columnas, incluye
		// en la lista las tablas referenciadas por esta (evitando duplicados)
		int pos = 0;
		while (pos < tables.size()) {
			// examina cada tabla de la lista el limite superior se determinara en cada
			// momento, pues se pueden anyadir columnas a la lista
			this.readTable(tables.get(pos));
			for (int i = 0; i < this.getColumnCount(); i++) {
				// si tiene clave ajena prosigue
				if (!this.getColumn(i).getForeignKey().equals("")) {
					String fTable = this.getColumn(i).getForeignTable();
					// recorre la lista de tablas hasta la posicion examinada actual
					// y si no la encuentra la anyade, (evitando duplicados)
					if (!JavaCs.containsIgnoreCase(tables, fTable)) // NOSONAR
						tables.add(fTable);
				}
			}
			pos++;
		}
		return tables;
	}

	/**
	 * Dada una lista de tablas, obtiene otra lista con las mismas tablas ordenadas
	 * segun sus dependencias maestro-detalle (primero las maestros); NOTA: Causa
	 * excepcion si existen ciclos, en este caso utilizar SchemaSorter directamente
	 * indicando noFollowConstraint para evitar los ciclos
	 */
	public List<String> getTableListInOrder(List<String> tables) {
		return new SchemaSorter(this).sort(tables);
	}

	// Existen otros metodos que declaran como tipo SchemaReader pero el objeto que
	// usan es SchemaReaderLiveJdbc o SchemaReaderJdbc
	// Estas clases son las que pueden implementar estos metodos para consulta de la base de datos
	// No se declaran como abstractos pues hay otros readers (e.g. SchemaReaderXml)
	// que no tienen acceso a base de datos
	public ResultSet query(String sql) {
		throw new SchemaException("Query method is only applicable to subclasses of SchemaReader");
	}
	public void execute(String sql) {
		throw new SchemaException("Execute sql method is only applicable to subclasses of SchemaReader");
	}
	public void execute(List<String> sqls) {
		throw new SchemaException("Execute list of sql method is only applicable to subclasses of SchemaReader");
	}
	public void closeResultSet(ResultSet rs) {
		throw new SchemaException("Close ResultSet method is only applicable to subclasses of SchemaReader");
	}

}
