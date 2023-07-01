package giis.tdrules.model;

import static giis.tdrules.model.ModelUtil.safe;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;

/**
 * Extends the OpenApi generated DbSchema model using default implementations in this interface
 * (the name of this interface must be defined as the x-implements vendor extension)
 */
public interface IDbSchemaExtension {

	// Methods from the generated model that are used here

	public DbSchema addTablesItem(DbTable table);
	public List<DbTable> getTables();

	// Default implementations to extend the generated model

	/**
	 * Adds a DbTable preventing adding duplicated tables (by name, case insensistive)
	 */
	@JsonIgnore
	default IDbSchemaExtension addTablesItemIfNotExist(DbTable table) {
		DbTable existingTable = getTableOrNull(table.getName());
		if (existingTable == null) // anyade la tabla si no existe
			addTablesItem(table);
		return this;
	}

	/**
	 * Gets all names for DBTables in the model
	 */
	@JsonIgnore
	default List<String> getTableNames() {
		return getTableNames(true, true, true);
	}

	@JsonIgnore
	default List<String> getTableNames(boolean includeTables, boolean includeViews, boolean includeTypes) {
		List<String> names = new ArrayList<>();
		for (DbTable table : safe(getTables()))
			// NOTA: considera los arrays (solo aparecen en OA) como si fueran tablas
			// Pendiente incluir en tests
			if (includeTables && TableTypes.DT_TABLE.equals(table.getTabletype())
					|| includeTables && TableTypes.DT_ARRAY.equals(table.getTabletype())
					|| includeViews && TableTypes.DT_VIEW.equals(table.getTabletype())
					|| includeTypes && TableTypes.DT_TYPE.equals(table.getTabletype()))
				names.add(table.getName());
		return names;
	}

	/**
	 * Returns a DbTable by name (case insensitive), throws an exception if not found
	 */
	@JsonIgnore
	default DbTable getTable(String name) {
		DbTable table = getTableOrNull(name);
		if (table == null)
			throw new ModelException("Can't find any table in the schema with name " + name);
		return table;
	}

	/**
	 * Returns a DbTable by name (case insensitive), returns null if not found
	 */
	@JsonIgnore
	default DbTable getTableOrNull(String name) {
		for (DbTable table : safe(getTables()))
			if (table.getName().equalsIgnoreCase(name.trim()))
				return table;
		return null;
	}

}
