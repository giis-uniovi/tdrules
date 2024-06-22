package giis.tdrules.client.oa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.model.transform.SchemaFilterMatcher;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Filters the objects of a Open Api Schema that represent entities and
 * attributes. After instantiation, the user must add the patterns of entity and
 * attribute names to filter using the add methods and then inject this filter
 * in the OaSchemaApi.
 */
public class OaSchemaFilter extends SchemaFilterMatcher<OaSchemaFilter> {
	protected static final Logger log = LoggerFactory.getLogger(OaSchemaFilter.class);

	/**
	 * Modifies the schema by applying removing the entities and/or attributes
	 * according to the patterns indicated by the add method and returns the
	 * model to allow fluent style calls.
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Schema> filter(Map<String, Schema> schema) {
		// Check reverse because this is destructive
		List<Entry<String, Schema>> entities = getSafeArrayList(schema);
		for (int i = entities.size() - 1; i >= 0; i--) {
			String entity = entities.get(i).getKey();
			if (matchItems(entity, "*")) {
				log.debug("Filter OA Entity: {}", entity);
				schema.remove(entity);
			} else {
				@SuppressWarnings("unchecked")
				Map<String, Schema> oaAttributes = entities.get(i).getValue().getProperties();
				List<Entry<String, Schema>> attributes = getSafeArrayList(oaAttributes);
				for (int j = attributes.size() - 1; j >= 0; j--) {
					String attribute = attributes.get(j).getKey();
					if (matchItems(entity, attribute)) {
						log.debug("Filter OA Attribute: {}.{}", entity, attribute);
						schema.get(entity).getProperties().remove(attribute);
					}
				}
			}
		}
		return schema; // same instance, but has been modified
	}

	// converts the map with entities or attributes to list, with null check
	@SuppressWarnings("rawtypes")
	private List<Entry<String, Schema>> getSafeArrayList(Map<String, Schema> oaMap) {
		if (oaMap == null)
			return new ArrayList<>();
		return new ArrayList<>(oaMap.entrySet());
	}

}
