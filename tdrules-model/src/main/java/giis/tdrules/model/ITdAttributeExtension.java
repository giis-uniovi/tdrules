package giis.tdrules.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import giis.tdrules.model.shared.EntityTypes;
import giis.tdrules.model.shared.ModelException;
import giis.tdrules.openapi.model.TdAttribute;

/**
 * Extends the OpenApi generated TdAttribute model using default implementations in this interface
 * (the name of this interface must be defined as the x-implements vendor extension)
 */
public interface ITdAttributeExtension {

	// Methods from the generated model that are used here

	public String getNotnull();
	public String getReadonly();
	public String getUid();
	public String getAutoincrement();
	public String getDefaultvalue();
	public String getRid();
	public ITdAttributeExtension notnull(String value);
	public ITdAttributeExtension readonly(String value);
	public String getCompositetype();
	public void setCompositetype(String compositeType);

	// Default implementations to extend the generated model

	@JsonIgnore
	default boolean isNotnull() {
		return "true".equals(getNotnull());
	}

	@JsonIgnore
	default ITdAttributeExtension notnull(boolean value) {
		return notnull(value ? "true" : "false");
	}

	@JsonIgnore
	default boolean isNullable() {
		return !"true".equals(getNotnull());
	}

	@JsonIgnore
	default boolean isReadonly() {
		return "true".equals(getReadonly());
	}

	@JsonIgnore
	default ITdAttributeExtension readonly(boolean value) {
		return readonly(value ? "true" : "false");
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

	// NOTE: asuming that attribute is last component after separator
	// (a quoted literal with dot inside will not work fine)
	/**
	 * Given a rid property, returns the part that represents either the entity or the
	 * attribute (according to getEntityPart parameter),
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
	
	// Convenience functions to set and check the composite type
	
	@JsonIgnore
	default boolean isType() {
		return EntityTypes.DT_TYPE.equalsIgnoreCase(getCompositetype());
	}

	@JsonIgnore
	default boolean isArray() {
		return EntityTypes.DT_ARRAY.equalsIgnoreCase(getCompositetype());
	}

	@JsonIgnore
	default TdAttribute setType() {
		setCompositetype(EntityTypes.DT_TYPE);
		return (TdAttribute) this;
	}

	@JsonIgnore
	default TdAttribute setArray() {
		setCompositetype(EntityTypes.DT_ARRAY);
		return (TdAttribute) this;
	}

}
