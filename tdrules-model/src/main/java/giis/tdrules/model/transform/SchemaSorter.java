package giis.tdrules.model.transform;

import static giis.tdrules.model.ModelUtil.safe;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.model.EntityTypes;
import giis.tdrules.model.ModelException;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Ordering of the entities in a schema: Provides a sort() function to sort a
 * set of entities in master to detail order (an entity D is detail of M if D
 * has an rid to M). If detects a cycle throws and exception. You can use
 * noFollowConstraint() to exclude the references that cause the cycles.
 * 
 * Includes an additional operation to get all dependent entities (masters)
 * given a set of tables.
 * 
 * NOTE: Module tdrules-store-rdb contains an old version of this class that
 * works only for RDBMS and does not depend on the model (it is kept for
 * compatibility with legacy applications)
 */
public class SchemaSorter {
	private static final Logger log = LoggerFactory.getLogger(SchemaSorter.class);
	private TdSchema schema;
	private List<String> excludeConstraints = new ArrayList<>();
	private boolean arrayHoist = false;
	
	public SchemaSorter(TdSchema schema) {
		this.schema = schema;
	}

	/**
	 * Specifies the name of a referential integrity constraint (ridname) that will
	 * be ignored during sorting (to avoid cycles). Several calls to this function
	 * can be concatenated to add more than one constraint
	 */
	public SchemaSorter noFollowConstraint(String constraintName) {
		excludeConstraints.add(constraintName.toLowerCase());
		return this; // fluent to concatenate if more than one constraint
	}

	/**
	 * When array hoist is specified, the entities derived from arrays are prepended
	 * to the entity that contains the array, despite the array rid references the
	 * entity. Used to allow data generation from the schema.
	 */
	public SchemaSorter setArrayHoist() {
		this.arrayHoist = true;
		return this;
	}
	
	/**
	 * Overwrite this method if need another approach to get entities
	 * (e.g. lazy load of entities in rdb)
	 */
	protected TdEntity getEntity(String entityName) {
		return schema.getEntity(entityName);
	}

	/**
	 * Given a list of entities, gets another list including the same tables sorted
	 * according to their master-detail dependencies (masters first). If there are
	 * circular references it will cause an exception, in this case use
	 * noFollowConstraint() to break the cycles
	 */
	public List<String> sort(List<String> entityNames) { // NOSONAR
		log.debug("*** Begin sort tables: {}", entityNames);
		// If there is any cycle, throws an exception when the recursion depth equals this number
		int maxLevel = entityNames.size() * 2;

		List<String> orderedEntities = new ArrayList<>();
		// for each entity, collects this entity and its dependents, in reverse order
		for (int i = 0; i < entityNames.size(); i++) {
			log.trace("Sorting entity: {}", entityNames.get(i));
			String name = entityNames.get(i);
			getTableAndDependentInOrder(1, maxLevel, name, orderedEntities, entityNames);
		}
		log.debug("      End sort entities: {}", orderedEntities);
		return orderedEntities;
	}

	/**
	 * Collects all entities that depend on a given entity name (recurse from master
	 * to detail). including composite types as if they where master entities
	 * (although there is no rid to them).
	 */
	private void getTableAndDependentInOrder(int level, int maxLevel, String entityName, 
			List<String> orderedEntities, List<String> originalEntities) {
		log.trace("{} target: {} current entity list: {}", level, entityName, orderedEntities);
		if (level > maxLevel) {
			log.error("Too many recursive levels, the most probable reason is that schema has circular references");
			throw new ModelException("Too many recusive levels when trying sort entities");
		}
		// Entity with this name should exist, let throw exception if does not.
		TdEntity currentEntity = this.getEntity(entityName);
		List<TdAttribute> attributes = safe(currentEntity.getAttributes());

		// Locates and stores all dependent entities of the current entity
		List<String> dependentEntities = new ArrayList<>();
		for (int i = 0; i < attributes.size(); i++) {
			// If there is a dependency because of this attribute, adds the dependent entity
			String dependency = getDependency(entityName, attributes.get(i), originalEntities);
			if (dependency != null)
				dependentEntities.add(dependency);
		}
		// Recursive processing of dependent tables
		for (int i = 0; i < dependentEntities.size(); i++)
			getTableAndDependentInOrder(level + 1, maxLevel, dependentEntities.get(i), orderedEntities,
					originalEntities);
		// As here all dependencies has been resolved, loads this entity in the ordered list (if not already)
		if (!containsIgnoreCase(orderedEntities, currentEntity.getName()))
			orderedEntities.add(currentEntity.getName());
		log.trace("{} target: {}   final entity list: {}", level, entityName, orderedEntities);
	}

	/**
	 * Rules to determine a dependency between entities: For a target attribute of
	 * an entity returns the name of the dependent entity, null if there is no dependency
	 */
	private String getDependency(String entityName, TdAttribute attribute, List<String> originalEntities) {
		// Determine simple dependencies by a rid
		boolean dependentByFk = attribute.isRid() // must contain a reference
				// only if the dependent entity is not the current entity (avoid recursive references)
				&& !attribute.getRidEntity().equals(entityName)
				// and there is a true reference (handled in separate method)
				&& attributeReferencesAnyOf(entityName, attribute, originalEntities)
				// but not excluded to break cycles
				&& !excludeConstraints.contains(attribute.getRidname().toLowerCase());
		if (dependentByFk)
			return attribute.getRidEntity();

		// Composite types do not have a rid relation. In this case the dependent entity 
		// name is stored in the datatype
		if (EntityTypes.DT_TYPE.equals(attribute.getCompositetype()))
			return attribute.getDatatype();

		// Similar case when hoisting is set, an array attribute references its derived array entity
		if (this.arrayHoist && EntityTypes.DT_ARRAY.equals(attribute.getCompositetype()))
			return attribute.getDatatype();

		return null;
	}

	// Subrules to determine if the rid of an attribute is referencing one of the entities in a list
	private boolean attributeReferencesAnyOf(String entityName, TdAttribute attribute, List<String> entities) {
		String refName = attribute.getRidEntity();
		boolean references = containsIgnoreCase(entities, refName);
		if (!references || !this.arrayHoist)
			return references;

		// When there is a reference and hoist, we can find two cases:
		// 1. the referenced entity is the entity from which the array is derived: no reference
		// 2. else: confirm reference
		TdEntity refEntity = this.getEntity(refName);

		// if referenced entity contains an attribute that is a composite array
		// and datatype is the name of the array, we are in case 1:
		for (TdAttribute attr : refEntity.getAttributes()) {
			if (EntityTypes.DT_ARRAY.equals(attr.getCompositetype()) && entityName.equals(attr.getDatatype()))
				return false;
		}
		// Note that reference from the entity that contains the array to the array is not handled here
		// case 2:
		return references;
	}

	/**
	 * Gets the list of entities made up of the input entities and all those that
	 * are referenced by them by a rid (recursively), excluding composite types
	 */
	public List<String> getWithDependent(List<String> entities) {
		log.debug("*** Begin entity dependencies: {}", entities);
		// iterates until all dependencies are found, each iteration may lead to an
		// increase of the number of entities (uppper limit of the iteration)
		int pos = 0;
		while (pos < entities.size()) {
			log.trace("Get dependendencies of entity at postion: {}", pos);
			TdEntity entity = this.getEntity(entities.get(pos));
			log.trace("  Found entity: {}", entity.getName());
			// dependents for each attribute
			List<TdAttribute> attributes = entity.getRids();
			for (int i = 0; i < attributes.size(); i++) {
				TdAttribute attribute = attributes.get(i);
				log.trace("  Found dependency, atribute: {} - rid: {}", attribute.getName(), attribute.getRid());
				String dependentEntity = attribute.getRidEntity();
				// adds if not already in the list to avoid duplicates
				if (!containsIgnoreCase(entities, dependentEntity)) // NOSONAR
					entities.add(dependentEntity);
			}
			pos++;
		}
		log.debug("  End entity dependencies: {}", entities);
		return entities;
	}

	private boolean containsIgnoreCase(List<String> values, String target) {
		for (int j = 0; j < values.size(); j++)
			if (values.get(j).equalsIgnoreCase(target))
				return true;
		return false;
	}

}
