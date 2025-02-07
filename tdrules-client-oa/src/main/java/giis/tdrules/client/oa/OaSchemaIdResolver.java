package giis.tdrules.client.oa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.client.oa.transform.PathTransformer;
import giis.tdrules.model.shared.OaExtensions;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

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
	
	// stores entities that have resolved their id to be used when setting values to resolved rids
	class ResolvedId {
		String name;
		String idName;
		ResolvedId(String name, String idName) {
			this.name = name;
			this.idName = idName;
		}
	}
	private Map<String, ResolvedId> resolved = new HashMap<>();

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
	public void resolve(Map<String, Schema> oaSchemas, PathTransformer pathTransformer) {
		resolve(oaSchemas, pathTransformer, true, false);
		resolve(oaSchemas, pathTransformer, false, true);
	}

	@SuppressWarnings("rawtypes")
	public void resolve(Map<String, Schema> oaSchemas, PathTransformer pathTransformer, boolean resolveUid, boolean resolveRid) {
		log.debug("Check for {} by convention", resolveUid ? "uids" : "rids");
		for (Entry<String, Schema> oaSchema : oaSchemas.entrySet()) { // Entity Objects
			String entity = oaSchema.getKey();
			@SuppressWarnings("unchecked")
			Map<String, Schema> oaProperties = oaSchema.getValue().getProperties();
			if (oaProperties == null || isEntityExcluded(entity)) {
				continue;
			}
			for (Entry<String, Schema> oaProperty : oaProperties.entrySet()) { // Entity Properties
				String attribute = oaProperty.getKey();
				log.trace("Check for ids at property: {}", attribute);
				if (resolveUid)
					processUid(oaProperty.getValue(), entity, attribute);
				if (resolveRid) {
					processRid(oaProperty.getValue(), entity, attribute);
					processPathRid(pathTransformer.getPathParams(entity), entity);
				}
			}
		}
	}
	
	private void processPathRid(List<Parameter> parameters, String entity) {
		for (Parameter parameter : parameters) {
			processRid(parameter, entity, parameter.getName());
		}
	}

	private void processUid(Schema<?> oaProperty, String entity, String attribute) {
		if (isUid(entity, attribute)) {
			log.debug("Found uid by convention: schema object: {} property: {}", entity, attribute);
			oaProperty.addExtension(OaExtensions.X_PK, "true");
			// Records the basic data for this entity, it will be used to determine the destination of rids
			// The key is lowercase to better location from the potential rids
			resolved.put(entity.toLowerCase(), new ResolvedId(entity, attribute));
		}
	}

	private void processRid(Schema<?> oaProperty, String entity, String attribute) {
		String ridValue = getMatchingRidEntity(entity, attribute);
		if (!"".equals(ridValue)) {
			log.debug("Found rid in schema by convention: schema object: {} property: {} rid value: {}", entity, attribute, ridValue);
			oaProperty.addExtension(OaExtensions.X_FK, ridValue);
		}
	}

	// Extensions on attributes that are specified in the yaml file
	// are retrieved using getExtensions method on the schema object.
	// But the Parameter object has an inner schema object, the getExtensions method must
	// be executed on the parameter object, not on its schema object.
	// This seems to be an inconsisent behaviour.
	// To manage both the extensions specified in the ayml and the extensions created by this
	// id resolver, we store the created extensions in the parameter object
	private void processRid(Parameter parameter, String entity, String attribute) {
		String ridValue = getMatchingRidEntity(entity, attribute);
		if (!"".equals(ridValue)) {
			log.debug("Found rid in path by convention: schema object: {} property: {} rid value: {}", entity, attribute, ridValue);
			parameter.addExtension(OaExtensions.X_FK, ridValue);
		}
	}

	private boolean isUid(String entity, String attribute) {
		if (attribute.equals(idName))
			return true;
		// #346 Allow prefixed uid in the form name_id/nameId, provided that name is the current entity
		String ridEntity = getSyntaxMatchingRidEntity(attribute);
		return ridEntity != null && entity.equalsIgnoreCase(ridEntity);
	}

	private String getMatchingRidEntity(String entity, String attribute) {
		// if this value becomes non empty, a potential rid has been found
		String ridEntity = getSyntaxMatchingRidEntity(attribute);
		if (ridEntity == null)
			return "";
		
		// But the value can not match exactly the entity (e.g. entity starts with uppercase, but ridEntity does not):
		// Lookup in the stored entities with uids to get the exact value of the referenced value entity.id
		ResolvedId resolvedId = resolved.get(ridEntity.toLowerCase());
		if (resolvedId == null)
			return "";
		
		// this case is a prefixed uid, not rid
		if (entity.equalsIgnoreCase(ridEntity)) 
			return "";

		return resolvedId.name + "." + resolvedId.idName;
	}
	
	private String getSyntaxMatchingRidEntity(String attribute) {
		// first check for rid in the form entityId - Entity.id (camel case convention)
		// and then for entity_id - entity.id (snake case)
		// returns the entity name, null if no match
		if (attribute.endsWith(StringUtils.capitalize(idName)))
			return StringUtils.capitalize(attribute.substring(0, attribute.length() - idName.length()));
		else if (attribute.endsWith("_" + idName))
			return attribute.substring(0, attribute.length() - idName.length() - 1);
		else
			return null; // no match

	}

}
