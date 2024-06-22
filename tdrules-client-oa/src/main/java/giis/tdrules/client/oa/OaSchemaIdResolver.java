package giis.tdrules.client.oa;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.model.OaExtensions;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Customizes the conventions to use to determine the ids (uid and rid) in the OA schema 
 * and sets the corresponding vendor extensions (x-pk, x-fk).
 * 
 * Follows a convention that identifies the ids when they have an special name (e.g. id),
 * and the rids by the concatenation of an entity and attribute name.
 * 
 * Allows camel case and snake case concatenations of the ids
 */
public class OaSchemaIdResolver {
	protected static final Logger log = LoggerFactory.getLogger(OaSchemaIdResolver.class);

	private String idName = "";
	private Set<String> entityExclusions = new HashSet<>();

	/**
	 * Sets the attribute name used to determine if an attribute is uid
	 */
	public OaSchemaIdResolver setIdName(String idName) {
		this.idName = idName;
		return this;
	}

	/**
	 * Sets an exclusion where the convention to do not determine ids for this entity
	 */
	public OaSchemaIdResolver excludeEntity(String entityName) {
		entityExclusions.add(entityName);
		return this;
	}

	private boolean isEntityExcluded(String entityName) {
		return entityExclusions.contains(entityName);
	}

	/**
	 * Main processing of the id resolver, traverses every entity and
	 * sets the vendor extensions for uid and rid according to 
	 * the conventions established by this object
	 */
	@SuppressWarnings("rawtypes")
	public void resolve(Map<String, Schema> oaSchemas) {
		log.debug("Check for ids by convention");
		for (Entry<String, Schema> oaSchema : oaSchemas.entrySet()) { // Entity Objects
			String entity = oaSchema.getKey();
			log.trace("Check for ids at object: {}", entity);
			@SuppressWarnings("unchecked")
			Map<String, Schema> oaProperties = oaSchema.getValue().getProperties();
			if (oaProperties == null) {
				continue;
			}
			for (Entry<String, Schema> oaProperty : oaProperties.entrySet()) { // Entity Properties
				String attribute = oaProperty.getKey();
				log.trace("Check for ids at property: {}", attribute);
				if (isUid(entity, attribute)) {
					log.debug("*found uid by convention: schema object: {} property: {}", entity, attribute);
					addExtension(oaProperty.getValue(), OaExtensions.X_PK, "true");
				} else {
					String rid = getRid(entity, attribute);
					if (!"".equals(rid)) {
						log.debug("*found rid by convention: schema object: {} property: {} rid value: {}", entity, attribute, rid);
						addExtension(oaProperty.getValue(), OaExtensions.X_FK, rid);
					}
				}
			}
		}
	}

	private void addExtension(Schema<?> oaProperty, String key, String value) {
		oaProperty.addExtension(key, value);
	}

	private boolean isUid(String entity, String attribute) {
		return attribute.equals(idName) && !isEntityExcluded(entity);
	}

	private String getRid(String entity, String attribute) {
		if (isEntityExcluded(entity))
			return "";
		// first check for rid in the form entityId - Entity.id (camel case convention)
		// and then for entity_id - entity.id (snake case)
		// note: Only syntactic check at this moment
		// pending: check if there is an entity with the appropriate uid
		String colName = attribute;
		String ridEntity = ""; // if this value becomes non empty, the rid has been found
		if (colName.endsWith(StringUtils.capitalize(idName)))
			ridEntity = StringUtils.capitalize(colName.substring(0, colName.length() - idName.length()));
		else if (colName.endsWith("_" + idName))
			ridEntity = colName.substring(0, colName.length() - idName.length() - 1);

		if (!"".equals(ridEntity))
			return ridEntity + "." + idName;

		return "";
	}

}
