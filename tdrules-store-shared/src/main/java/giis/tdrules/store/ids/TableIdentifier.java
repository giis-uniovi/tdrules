package giis.tdrules.store.ids;

import giis.portable.util.JavaCs;

/**
 * Metodos de utilidad para manejo de identificadores de tabla y columna en un
 * esquema, qualificados y sin qualificar
 */
public class TableIdentifier extends SimpleIdentifier {
	private String cat = "";
	private String sch = "";
	private String tab = "";
	private boolean processQuotes = false;

	/**
	 * Instanciacion realizada a partir de los metadatos, forma el objeto con el
	 * catalogo y esquema indicados, si no existen aplica los establecidos por
	 * defecto
	 */
	public TableIdentifier(String defCat, String defSch, String catalog, String schema, String table,
			boolean processQuotes) {
		this.processQuotes = processQuotes;
		fill(defCat, defSch, catalog, schema, table);
	}

	public TableIdentifier(String defCat, String defSch, String name, boolean processQuotes) {
		this.processQuotes = processQuotes;
		String[] comp;
		if (name.contains("["))
			comp = Quotation.splitQuotedRight(name, '[', ']', '.', 3);
		else
			comp = Quotation.splitQuotedRight(name, '"', '"', '.', 3);
		fill(defCat, defSch, comp[0], comp[1], comp[2]);
	}

	public TableIdentifier(String name, boolean processQuotes) {
		this("", "", name, processQuotes);
	}

	public String getCat() {
		return cat;
	}

	public String getSch() {
		return sch;
	}

	public String getTab() {
		return tab;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public void setSch(String sch) {
		this.sch = sch;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	@Override
	public String toString() {
		return "cat=" + cat + ", sch=" + sch + ", tab=" + tab;
	}

	private void fill(String defCat, String defSch, String catalog, String schema, String table) {
		this.cat = processIdentifier(catalog, processQuotes);
		this.sch = processIdentifier(schema, processQuotes);
		this.tab = processIdentifier(table, processQuotes);
		// rellena con los valores por defecto si no se habian especificado
		// sch/cat no sera nulo pues ha sido procesado antes
		if (this.cat.equals(""))
			this.cat = processIdentifier(defCat, processQuotes);
		if (this.sch.equals(""))
			this.sch = processIdentifier(defSch, processQuotes);
	}

	/**
	 * nombre completamente qualificado, rellenando con los valores por defecto de
	 * catalogo y esquema
	 */
	public String getFullQualifiedTableName(String defCat, String defSch) {
		defCat = processIdentifier(defCat, processQuotes);
		defSch = processIdentifier(defSch, processQuotes);
		String catalog = (this.cat.equals("") ? defCat : this.cat);
		String schema = (this.sch.equals("") ? defSch : this.sch);
		return getQualifiedName(catalog, schema, this.tab);
	}

	/** nombre completamente qualificado, */
	public String getFullQualifiedTableName() {
		return getQualifiedName(this.cat, this.sch, this.tab);
	}

	/**
	 * nombre qualificado excluyendo catalogo y esquema cuando tienen los valores
	 * por defecto de catalgo y esquema indicados
	 */
	public String getDefaultQualifiedTableName(String defCat, String defSch) {
		defCat = processIdentifier(defCat, processQuotes);
		defSch = processIdentifier(defSch, processQuotes);
		String catalog = (JavaCs.equalsIgnoreCase(this.cat, defCat) ? "" : this.cat);
		String schema = (JavaCs.equalsIgnoreCase(this.sch, defSch) ? "" : this.sch);
		return getQualifiedName(catalog, schema, this.tab);
	}

	/**
	 * Obtiene el nombre completamente qualificado con los datos pasados como
	 * parametro
	 */
	public static String getQualifiedName(String catalog, String schema, String table) {
		if (catalog == null)
			catalog = "";
		if (schema == null)
			schema = "";
		if (table == null)
			table = "";
		if (table.equals(""))
			throw new RuntimeException("SchemaTableIdentifier.getQualifiedName: table name is empty"); // NOSONAR
		String name = "";
		if (!catalog.equals("") && !schema.equals(""))
			name += catalog + "." + schema + ".";
		else if (catalog.equals("") && !schema.equals(""))
			name += schema + ".";
		else if (!catalog.equals("") && schema.equals(""))
			name += catalog + "..";
		name += table;
		return name;
	}

}
