package giis.tdrules.store.ids;

/**
 * Metodos de utilidad para manejo de identificadores de columna, que incluyen
 * opcionalmente el identifcador de tabla
 */
public class ColumnIdentifier extends SimpleIdentifier {
	private TableIdentifier tabId = null; // tabla opcional
	private String col = ""; // nombre de la columna

	/**
	 * constructor del objeto columna con a partir del nombre completo en la forma
	 * [catalog.[schema.[table.]column]]]
	 */
	public ColumnIdentifier(String defCat, String defSch, String name, boolean processQuotes) {
		// parte en todos los componentes
		String[] comp = Quotation.splitQuotedRight(name, '"', '"', '.', 4);
		// la columna es el ultimo componente
		this.col = processIdentifier(comp[3], processQuotes);
		// el resto es la tabla, opcional si al menos existe el tercero
		if (!comp[2].equals(""))
			this.tabId = new TableIdentifier(defCat, defSch, comp[0], comp[1], comp[2], processQuotes);
	}

	public String getCol() {
		return col;
	}

	public TableIdentifier getTabId() {
		return tabId;
	}

	public boolean isQualifiedByTable() {
		return this.tabId != null;
	}

	/**
	 * Nombre qualificado excluyendo catalogo y esquema cuando tienen los valores
	 * por defecto de catalgo y esquema indicados (si cat coincide con el parametro,
	 * se omite, idem para sch)
	 */
	public String getDefaultQualifiedColumnName(String defCat, String defSch) {
		String qTable = "";
		if (this.tabId != null)
			qTable = this.tabId.getDefaultQualifiedTableName(defCat, defSch) + ".";
		return qTable + this.col;
	}

}
