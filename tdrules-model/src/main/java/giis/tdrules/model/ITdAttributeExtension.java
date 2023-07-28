package giis.tdrules.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Extends the OpenApi generated DbColumn model using default implementations in this interface
 * (the name of this interface must be defined as the x-implements vendor extension)
 */
public interface ITdAttributeExtension {

	// Methods from the generated model that are used here

	public String getNotnull();
	public String getUid();
	public String getAutoincrement();
	public String getDefaultvalue();
	public String getRid();

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
	default boolean isUid() {
		return "true".equals(getUid());
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
	default boolean isRid() {
		return !"".equals(getRid());
	}

	@JsonIgnore
	default String getRidEntity() {
		return getRidEntityOrAttribute(getRid(), true);
	}

	@JsonIgnore
	default String getRidAttribute() {
		return getRidEntityOrAttribute(getRid(), false);
	}

	// OJO: estoy suponiendo que la columna es el ultimo componente separando por puntos
	// (si hay un literal entre comillas con un punto no funcionara correctamente)
	/**
	 * Given a rid property, returns the part that represents either the table or the
	 * column (according to getEntityPart parameter),
	 */
	@JsonIgnore
	default String getRidEntityOrAttribute(String rid, boolean getEntityPart) {
		if (rid == null || "".equals(rid.trim())) // no rid
			return "";
		int dotPosition = rid.lastIndexOf('.');
		if (dotPosition < 0)
			throw new ModelException("Referenced id " + rid + " should have at least two components separated by a dot");
		return getEntityPart ? rid.substring(0, dotPosition) : rid.substring(dotPosition + 1, rid.length());
	}

}
