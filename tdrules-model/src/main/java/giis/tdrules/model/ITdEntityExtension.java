package giis.tdrules.model;

import static giis.tdrules.model.ModelUtil.safe;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import giis.tdrules.openapi.model.TdAttribute;

/**
 * Extends the OpenApi generated DbTable model using default implementations in this interface
 * (the name of this interface must be defined as the x-implements vendor extension)
 */
public interface ITdEntityExtension {

	// Methods from the generated model that are used here

	public List<TdAttribute> getAttributes();

	// Default implementations to extend the generated model

	/**
	 * Gets all attributes that are an rid to another attribute
	 */
	@JsonIgnore
	default List<TdAttribute> getRids() {
		List<TdAttribute> rids=new ArrayList<>();
		for (TdAttribute attribute : safe(getAttributes()))
			if (!"".equals(attribute.getRid()))
				rids.add(attribute);
		return rids;
	}

	/**
	 * Gets all unique values of rid properties
	 */
	@JsonIgnore
	default List<String> getUniqueRids() {
		Set<String> rids = new LinkedHashSet<>();
		for (TdAttribute attribute : safe(getAttributes()))
			if (!"".equals(attribute.getRid()))
				rids.add(attribute.getRid());
		return new ArrayList<>(rids);
	}

	/**
	 * Gets all unique names of rid properties
	 */
	@JsonIgnore
	default List<String> getUniqueRidNames() {
		Set<String> rids = new LinkedHashSet<>();
		for (TdAttribute attribute : safe(getAttributes()))
			if (!"".equals(attribute.getRidname()))
				rids.add(attribute.getRidname());
		return new ArrayList<>(rids);
	}

	/**
	 * Gets the attribute that is uid of a TdTable (null if there are no ridss);
	 * currently, if there more than one uid, returns the first one
	 */
	@JsonIgnore
	default TdAttribute getUid() {
		for (TdAttribute attribute : safe(getAttributes()))
			if (attribute.isUid())
				return attribute;
		return null;
	}
	
	/**
	 * Gets an attribute by name (case insensitive), null if not found
	 */
	@JsonIgnore
	default TdAttribute getAttribute(String name) {
		for (TdAttribute attribute : safe(getAttributes()))
			if (attribute.getName().equalsIgnoreCase(name))
				return attribute;
		return null;
	}

}
