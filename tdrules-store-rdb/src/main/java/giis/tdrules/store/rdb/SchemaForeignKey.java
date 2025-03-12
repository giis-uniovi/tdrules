package giis.tdrules.store.rdb;

import java.util.ArrayList;
import java.util.List;

import giis.tdrules.store.ids.TableIdentifier;

public class SchemaForeignKey {
	private SchemaTable fromTable;
	private String name;
	private TableIdentifier fromTableIdentifier;
	private TableIdentifier toTableIdentifier;
	private List<String> fromColumnNames = new ArrayList<String>();
	private List<String> toColumnNames = new ArrayList<String>();

	public SchemaForeignKey(SchemaTable fromTable, String name, TableIdentifier fromTableIdentifier,
			TableIdentifier toTableIdentifier) {
		this.fromTable = fromTable;
		this.name = name;
		this.fromTableIdentifier = fromTableIdentifier;
		this.toTableIdentifier = toTableIdentifier;
	}

	public SchemaTable getFromTable() {
		return fromTable;
	}
	public String getName() {
		return name;
	}
	public TableIdentifier getFromTableIdentifier() {
		return fromTableIdentifier;
	}
	public TableIdentifier getToTableIdentifier() {
		return toTableIdentifier;
	}
	public List<String> getFromColumnNames() {
		return fromColumnNames;
	}
	public List<String> getToColumnNames() {
		return toColumnNames;
	}

	public void setFromTable(SchemaTable fromTable) {
		this.fromTable = fromTable;
	}
	public String getFromTableShortName() {
		return this.fromTableIdentifier.getTab();
	}
	public String getToTableShortName() {
		return this.toTableIdentifier.getTab();
	}
	public String getFromTableFullName() {
		return this.fromTableIdentifier.getFullQualifiedTableName();
	}
	public String getToTableFullName() {
		return this.toTableIdentifier.getFullQualifiedTableName();
	}

	public void addColumn(String fromColumnName, String toColumnName) {
		this.fromColumnNames.add(fromColumnName);
		this.toColumnNames.add(toColumnName);
	}

	@Override
	public String toString() {
		return getFromTableFullName() + " CONSTRAINT " + name + " FOREIGN KEY(" + joinElements(fromColumnNames, ",")
				+ ") REFERENCES " + getToTableFullName() + "(" + joinElements(toColumnNames, ",") + ")";
	}

	// Concatena elementos de la lista con el separador indicado
	private String joinElements(List<String> elements, String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < elements.size(); i++)
			sb.append((i == 0 ? "" : separator) + elements.get(i));
		return sb.toString();
	}
	
}
