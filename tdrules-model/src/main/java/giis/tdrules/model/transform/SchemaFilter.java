package giis.tdrules.model.transform;

import java.util.List;

import giis.tdrules.model.shared.ModelUtil;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Filters entities and attributes of a TdSchema that match with given patterns.
 * After instantiation, the user must add the patterns to filter using the add
 * methods and then call the filter method that performs the appropriate
 * removals.
 */
public class SchemaFilter extends SchemaFilterMatcher<SchemaFilter> {

	private TdSchema schema;

	public SchemaFilter(TdSchema schema) {
		this.schema = schema;
	}

	/**
	 * Modifies a TdSchema by applying removing the entities and/or attributes
	 * according to the patterns indicated configured by the add method and returns
	 * the model to allow fluent style calls.
	 */
	public TdSchema filter() {
		// Check reverse because this is destructive
		List<TdEntity> entities = ModelUtil.safe(schema.getEntities());
		for (int i = entities.size() - 1; i >= 0; i--) {
			if (matchItems(entities.get(i).getName(), "*")) {
				entities.remove(i);
			} else { // attribute removal
				List<TdAttribute> attributes = ModelUtil.safe(entities.get(i).getAttributes());
				for (int j = attributes.size() - 1; j >= 0; j--)
					if (matchItems(entities.get(i).getName(), attributes.get(j).getName()))
						attributes.remove(j);
			}
		}
		return schema; // same instance, but has been modified
	}

}
