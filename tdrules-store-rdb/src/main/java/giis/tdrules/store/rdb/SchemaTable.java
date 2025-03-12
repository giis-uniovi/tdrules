package giis.tdrules.store.rdb;

import java.util.ArrayList;
import java.util.List;

import giis.portable.util.JavaCs;
import giis.tdrules.store.ids.TableIdentifier;

public class SchemaTable {
	private List<SchemaColumn> columns;
	private List<SchemaForeignKey> fks; // claves ajenas
	private List<SchemaForeignKey> incomingFks; // claves ajenas de otras tablas que referencian a esta
	private List<SchemaCheckConstraint> checkConstraints; // checks aplicables a las columnas de esta tabla
	private TableIdentifier givenId; // nombre de la tabla actual tal como se ha indicado al buscarla
	private TableIdentifier globalId; // nombre de la tabla actual con todos los datos de catalogo y esquema
											// encontrados en los metadatos
	private String tableType; // tipo de tabla: table, view, type (udt o row-set)
	private String catalog = ""; // nombre del catalogo tal y como figura en los metadatos
	private String schema = "";
	private SchemaReader schemaReader = null; // el SchemaReader desde el que se ha leido la tabla

	public SchemaTable(SchemaReader sReader) {
		this.schemaReader = sReader;
		this.columns = new ArrayList<SchemaColumn>();
		this.fks = new ArrayList<SchemaForeignKey>();
		this.incomingFks = new ArrayList<SchemaForeignKey>();
		this.checkConstraints = new ArrayList<SchemaCheckConstraint>();
		this.givenId = new TableIdentifier("", "", "", "", "", false);
		this.globalId = new TableIdentifier("", "", "", "", "", false);
		this.tableType = "";
	}

	public List<SchemaColumn> getColumns() { return columns; }
	public TableIdentifier getGivenId() { return givenId; }
	public TableIdentifier getGlobalId() { return globalId; }
	public String getTableType() { return tableType; }
	public String getCatalog() { return catalog; }
	public String getSchema() { return schema; }
	public SchemaReader getSchemaReader() { return schemaReader; }
	
	public void setGivenId(TableIdentifier givenId) { this.givenId = givenId; }
	public void setGlobalId(TableIdentifier globalId) { this.globalId = globalId; }
	public void setTableType(String tableType) { this.tableType = tableType; }
	public void setCatalog(String catalog) { this.catalog = catalog; }
	public void setSchema(String schema) { this.schema = schema; }
	public void setSchemaReader(SchemaReader schemaReader) { this.schemaReader = schemaReader; }
	
	@Override
	public String toString() { 
		return this.getName(); 
	}
	public String getName() {
		return this.givenId.getDefaultQualifiedTableName(givenId.getCat(), givenId.getSch());
	}
	public String getGlobalName() {
		return this.getGlobalId().getFullQualifiedTableName();
	}
	public boolean isTable() { 
		return this.tableType.equals("table"); 
	}
	public boolean isView() { 
		return this.tableType.equals("view"); 
	}
	public boolean isType() { 
		return this.tableType.equals("type"); 
	}

	public String[] getColumnNames() {
		String[] names = new String[this.columns.size()];
		for (int i = 0; i < names.length; i++)
			names[i] = this.columns.get(i).getColName();
		return names;
	}

	/**
	 * Devuelve la estructura de clave ajena correspondiente a este nombre, null si
	 * no se encuentra
	 */
	public SchemaForeignKey getFK(String name) {
		for (int i = 0; i < this.fks.size(); i++) {
			SchemaForeignKey fk = this.fks.get(i);
			if (JavaCs.equalsIgnoreCase(fk.getName(), name))
				return fk;
		}
		return null;
	}

	public List<SchemaForeignKey> getFKs() {
		return this.fks;
	}

	/**
	 * Devuelve todas las claves ajenas entrantes (de tablas que referencian a
	 * esta). A diferencia de las FKs de salida, estas no guardan como objeto la
	 * tabla origen ni los valores de las columnas pues su uso es solamente para
	 * localizar nombres de tablas que se relacionan con esta. Ademas se obtienen
	 * solamente cuando el reader ha sido configurado para obtenerlas (solo
	 * implementado para Jdbc)
	 */
	public List<SchemaForeignKey> getIncomingFKs() {
		return this.incomingFks;
	}
	public void addCheckConstraint(SchemaCheckConstraint check) {
		this.checkConstraints.add(check);
	}
	public List<SchemaCheckConstraint> getCheckConstraints() {
		return this.checkConstraints;
	}

}
