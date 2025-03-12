package giis.tdrules.store.rdb;

import giis.portable.util.JavaCs;
import giis.tdrules.store.ids.Quotation;
import giis.tdrules.store.ids.TableIdentifier;

public class SchemaColumn {
	private String colName = ""; // nombre de la ultima columna leida
	private String dataType = ""; // tipo de datos en formato string (segun BD)
	private String compositeType = ""; // Si no es primitivo indica que tipo de estructura o coleccion: array, type
	private String dataSubType = ""; // otros cualificadores como WITH TIMEZONE, o de intervalos: DAY TO SECOND,etc
	private int dataTypeCode = -1; // tipo de datos en formato entero (segun BD)
	private int colSize = 0; // tamanyo en columnas (si aplicable)
	private int decimalDigits = 0; // columnas de la parte decimal (si aplicable)
	private boolean isKey = false; // es clave primaria?
	private boolean isNotNull = false; // permite nulos?
	private boolean isAutoIncrement = false; // columna autoincremental
	
	protected TableIdentifier foreignKeyTableSchemaIdentifier = null; // tabla referenciada completamenteidentificada
	protected String foreignKeyTable = ""; // tabla referenciada como clave ajena
	protected String foreignKeyColumn = ""; // columna referenciada como clave ajena
	
	protected String foreignKeyName = ""; // nombre con el que se guarda la FK en la BD
	// public String foreignKeyDestTable=""; //nombre de la tabla destino de la FK
	protected String checkInConstraint = ""; // Condicion de comprobacion en constraints del tipo CHECK IN condition
	protected String defaultValue = ""; // DEFAULT

	@Override
	public String toString() {
		return this.getColName();
	}
	public String getColName() {
		return colName;
	}
	public String getDataType() {
		return dataType;
	}
	public String getCompositeType() {
		return compositeType;
	}
	public String getDataSubType() {
		return dataSubType;
	}
	public int getDataTypeCode() {
		return dataTypeCode;
	}
	public int getColSize() {
		return colSize;
	}
	public int getDecimalDigits() {
		return decimalDigits;
	}
	public boolean isKey() {
		return isKey;
	}
	public boolean isNotNull() {
		return isNotNull;
	}

	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public void setCompositeType(String compositeType) {
		this.compositeType = compositeType;
	}
	public void setDataSubType(String dataSubType) {
		this.dataSubType = dataSubType;
	}
	public void setDataTypeCode(int dataTypeCode) {
		this.dataTypeCode = dataTypeCode;
	}

	public void setColSize(int colSize) {
		this.colSize = colSize;
	}
	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}
	public void setNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
	}
	public void setAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}

	public String getForeignKeyName() {
		return foreignKeyName;
	}
	public String getCheckInConstraint() {
		return checkInConstraint;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setForeignKeyName(String fkName) {
		this.foreignKeyName = fkName;
	}
	
	public void setForeignKeyTable(String foreignKeyTable) {
		this.foreignKeyTable = foreignKeyTable;
	}
	public void setForeignKeyColumn(String foreignKeyColumn) {
		this.foreignKeyColumn = foreignKeyColumn;
	}
	public void setForeignKeyTableSchemaIdentifier(TableIdentifier foreignKeyTableSchemaIdentifier) {
		this.foreignKeyTableSchemaIdentifier = foreignKeyTableSchemaIdentifier;
	}

	public void setCheckInConstraint(String constraint) {
		this.checkInConstraint = constraint;
	}
	public void setDefaultValue(String value) {
		this.defaultValue = value;
	}
	public String getForeignTable() {
		return this.foreignKeyTable;
	}
	public String getForeignKeyColumn() {
		return this.foreignKeyColumn;
	}
	public String getForeignKey() {
		return (!this.getForeignTable().equals("") ? this.getForeignTable() + "." : "") + this.getForeignKeyColumn();
	}
	public void setForeignKey(String fk) {
		this.foreignKeyTable = "";
		this.foreignKeyColumn = "";
		this.foreignKeyTableSchemaIdentifier = null;
		if (fk == null || fk.trim().equals(""))
			return;
		// De todos los componentes separados por puntos saca el ultimo que es el nombre
		// de la columna y el reseto que es el nombre de la tabla
		String[] comp = Quotation.splitQuoted(fk, '"', '"', '.');
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < comp.length - 1; i++)
			sb.append((i > 0 ? "." : "") + comp[i]);
		this.foreignKeyTable = sb.toString();
		this.foreignKeyColumn = comp[comp.length - 1];
		this.foreignKeyTableSchemaIdentifier = new TableIdentifier(this.foreignKeyTable, true);
	}

	/**
	 * Indica si el tipo de dato se puede considerar caracter
	 */
	public boolean isCharacterLike() {
		return this.dataType.toLowerCase().contains("char") || this.dataType.toLowerCase().contains("text");
	}
	/**
	 * Indica si el tipo de dato se puede considerar que almacena fechas u horas)
	 */
	public boolean isDateTimeLike() {
		return this.dataType.toLowerCase().contains("date") || this.dataType.toLowerCase().contains("time");
	}

	public boolean isForeignKey() {
		return !this.getForeignKey().equals("");
	}

	/**
	 * Obtiene el contenido de esta columna en forma de string SQL a partir de un
	 * valor dado como String. Tiene en cuenta si es un string, fecha, etc.
	 */
	public String getAsSqlString(String value) {
		if (value == null)
			return "NULL";
		// Excepcion para fechas Oracle. Dependiendo de la version los tipos date vienen
		// con un offset de tiempo o no.
		// Esto es un problema si hay una fk que se basa en esta coluna (como en
		// hr.job_history) pues al insertar en la bd puede haber una excepcion
		// porque no admite insertar con el offset. En este caso se queda con los 10
		// primeros caracteres del valor, ignorando los del tiempo
		// https://forums.oracle.com/forums/thread.jspa?threadID=300473
		if (JavaCs.equalsIgnoreCase(this.dataType, "date")) // fixed in SqlCore version (for all databases)
			value = JavaCs.substring(value, 0, 10); // asumo que son de la forma yyyy-mm-dd
		// muestra el valor con el prefijo (p.e. en fechas), comillas de apertura y
		// cierre, y transformando comillas internas
		return this.getValuePrefix() + value.replace("'", "''") + this.getValueSuffix();
	}

	/** Obtiene el prefijo que se anyade a los tipos de datos de fecha/hora */
	public String getDateTimeConstantPrefix() {
		String dt = this.dataType.toLowerCase();
		// valores de las constantes segun el estandar SQL
		if (dt.equals("date") || dt.equals("time") || dt.equals("timestamp"))
			return dt.toUpperCase() + " ";
		else
			return ""; // cualquier otro no es de tipo fecha
	}
	/**
	 * Obtiene la parte de texto que precede al valor cuando se asigna un valor a esta columna
	 */
	public String getValuePrefix() {
		if (this.isCharacterLike())
			return "'";
		else if (this.isDateTimeLike())
			return this.getDateTimeConstantPrefix() + "'";
		else
			return "";
	}
	/**
	 * Obtiene la parte de texto que sigue al valor cuando se asigna un valor a esta columna
	 */
	public String getValueSuffix() {
		return this.isCharacterLike() || this.isDateTimeLike() ? "'" : "";
	}

	/**
	 * En algunos sgbd (sqlite) el tipo aparece con parentesis y un numero en vez de
	 * indicar este en la precision, parche para remplazar lo necesario
	 */
	public void reparseNameWithPrecision() {
		try {
			if (dataType.contains("(")) {
				String all = dataType;
				dataType = JavaCs.substring(dataType, 0, dataType.indexOf("("));
				String precAndScale = JavaCs.substring(all, all.indexOf("("), all.length());
				precAndScale = Quotation.removeQuotes(precAndScale, '(', ')');
				if (precAndScale.contains(",")) {
					String[] precOrScale = JavaCs.splitByChar(precAndScale, ',');
					colSize = JavaCs.stringToInt(precOrScale[0]);
					decimalDigits = JavaCs.stringToInt(precOrScale[1]);
				} else {
					colSize = JavaCs.stringToInt(precAndScale);
					decimalDigits = 0;
				}
			}
		} catch (Exception e) {
			// evita fallo, deja los datos como hayan quedado
		}
	}

}
