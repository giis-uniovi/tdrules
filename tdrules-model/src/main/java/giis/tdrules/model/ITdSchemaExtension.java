package giis.tdrules.model;

import static giis.tdrules.model.ModelUtil.safe;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Extends the OpenApi generated DbSchema model using default implementations in this interface
 * (the name of this interface must be defined as the x-implements vendor extension)
 */
public interface ITdSchemaExtension {

	// Methods from the generated model that are used here

	public TdSchema addEntitiesItem(TdEntity table);
	public List<TdEntity> getEntities();

	// Default implementations to extend the generated model

	/**
	 * Adds a DbTable preventing adding duplicated tables (by name, case insensistive)
	 */
	@JsonIgnore
	default ITdSchemaExtension addEntitiesItemIfNotExist(TdEntity entity) {
		TdEntity existingEntity = getEntityOrNull(entity.getName());
		if (existingEntity == null) // anyade la tabla si no existe
			addEntitiesItem(entity);
		return this;
	}

	/**
	 * Gets all names for TdTables in the model
	 */
	@JsonIgnore
	default List<String> getEntityNames() {
		return getEntityNames(true, true, true);
	}

	@JsonIgnore
	default List<String> getEntityNames(boolean includeTables, boolean includeViews, boolean includeTypes) {
		List<String> names = new ArrayList<>();
		for (TdEntity table : safe(getEntities()))
			// NOTA: considera los arrays (solo aparecen en OA) como si fueran tablas
			// Pendiente incluir en tests
			if (includeTables && EntityTypes.DT_TABLE.equals(table.getEntitytype())
					|| includeTables && EntityTypes.DT_ARRAY.equals(table.getEntitytype())
					|| includeViews && EntityTypes.DT_VIEW.equals(table.getEntitytype())
					|| includeTypes && EntityTypes.DT_TYPE.equals(table.getEntitytype()))
				names.add(table.getName());
		return names;
	}

	/**
	 * Returns a TdEntity by name (case insensitive), throws an exception if not found
	 */
	@JsonIgnore
	default TdEntity getEntity(String name) {
		TdEntity entity = getEntityOrNull(name);
		if (entity == null)
			throw new ModelException("Can't find any table in the schema with name " + name);
		return entity;
	}

	/**
	 * Returns a TdEntity by name (case insensitive), returns null if not found
	 */
	@JsonIgnore
	default TdEntity getEntityOrNull(String name) {
		for (TdEntity entity : safe(getEntities()))
			if (entity.getName().equalsIgnoreCase(name.trim()))
				return entity;
		return null;
	}

}
