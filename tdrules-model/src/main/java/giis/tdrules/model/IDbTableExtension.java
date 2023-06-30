package giis.tdrules.model;

import static giis.tdrules.model.ModelUtil.safe;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import giis.tdrules.openapi.model.DbColumn;

/**
 * Extends the OpenApi generated DbTable model using default implementations in this interface
 * (the name of this interface must be defined as the x-implements vendor extension)
 */
public interface IDbTableExtension {

	// Methods from the generated model that are used here

	public List<DbColumn> getColumns();

	// Default implementations to extend the generated model

	/**
	 * Gets all columns that are fk to another column
	 */
	@JsonIgnore
	default List<DbColumn> getFks() {
		List<DbColumn> fks=new ArrayList<>();
		for (DbColumn column : safe(getColumns()))
			if (!"".equals(column.getFk()))
				fks.add(column);
		return fks;
	}

	/**
	 * Gets all unique values of fk properties
	 */
	@JsonIgnore
	default List<String> getUniqueFks() {
		Set<String> fks = new LinkedHashSet<>();
		for (DbColumn column : safe(getColumns()))
			if (!"".equals(column.getFk()))
				fks.add(column.getFk());
		return new ArrayList<>(fks);
	}

	/**
	 * Gets all unique names of fk properties
	 */
	@JsonIgnore
	default List<String> getUniqueFkNames() {
		Set<String> fks = new LinkedHashSet<>();
		for (DbColumn column : safe(getColumns()))
			if (!"".equals(column.getFkname()))
				fks.add(column.getFkname());
		return new ArrayList<>(fks);
	}

	/**
	 * Gets the Pk of a DbTable (null if there are no PKs); currently, if there more
	 * than one pk, returns the first one
	 */
	@JsonIgnore
	default DbColumn getPk() {
		for (DbColumn column : safe(getColumns()))
			if (column.isPk())
				return column;
		return null;
	}

}
