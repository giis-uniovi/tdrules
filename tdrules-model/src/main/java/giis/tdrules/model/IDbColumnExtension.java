package giis.tdrules.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Extends the OpenApi generated DbColumn model using default implementations in this interface
 * (the name of this interface must be defined as the x-implements vendor extension)
 */
public interface IDbColumnExtension {

	// Methods from the generated model that are used here

	public String getNotnull();
	public String getKey();
	public String getAutoincrement();
	public String getDefaultvalue();
	public String getFk();

	// Default implementations to extend the generated model

	@JsonIgnore
	default boolean isNotnull() {
		return "true".equals(getNotnull());
	}

	@JsonIgnore
	default boolean isNullable() {
		return !"true".equals(getNotnull());
	}

	@JsonIgnore
	default boolean isPk() {
		return "true".equals(getKey());
	}

	@JsonIgnore
	default boolean isAutoincrement() {
		return "true".equals(getAutoincrement());
	}

	@JsonIgnore
	default boolean hasDefaultvalue() {
		return !"".equals(getDefaultvalue());
	}

	@JsonIgnore
	default boolean isFk() {
		return !"".equals(getFk());
	}

	@JsonIgnore
	default String getFkTable() {
		return getForeignTableOrColumn(getFk(), true);
	}

	@JsonIgnore
	default String getFkColumn() {
		return getForeignTableOrColumn(getFk(), false);
	}

	// OJO: estoy suponiendo que la columna es el ultimo componente separando por puntos
	// (si hay un literal entre comillas con un punto no funcionara correctamente)
	/**
	 * Given a fk property, returns the part that represents either the table or the
	 * column (according to getTablePart parameter),
	 */
	@JsonIgnore
	default String getForeignTableOrColumn(String fk, boolean getTablePart) {
		if (fk == null || "".equals(fk.trim())) // no hay fk
			return "";
		int dotPosition = fk.lastIndexOf('.');
		if (dotPosition < 0)
			throw new ModelException("Foreign key " + fk + " should have at least two components separated by a dot");
		return getTablePart ? fk.substring(0, dotPosition) : fk.substring(dotPosition + 1, fk.length());
	}

}
