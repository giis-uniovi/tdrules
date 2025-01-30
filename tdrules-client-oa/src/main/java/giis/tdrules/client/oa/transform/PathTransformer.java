package giis.tdrules.client.oa.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

/**
 * Determines the path info required by the POST and PUT operations associated to the
 * entities in the TdSchema (with a request body that corresponds to some entity
 * in the schema).
 * 
 * These operations are scanned and stored internally during instantiation
 * and can be later used during the transformation of an entity.
 */
public class PathTransformer {
	protected static final Logger log = LoggerFactory.getLogger(PathTransformer.class);

	// Holds the path related data from the oaSchema for each entity in the schema
	// (organized by http method)
	Map<String, List<EntityPath>> entityPaths = new TreeMap<>();

	public class EntityPath {
		String entityName;
		String path;
	}

	public PathTransformer(Map<String, PathItem> pathItems) {
		entityPaths.put("post", new ArrayList<>());
		entityPaths.put("put", new ArrayList<>());
		this.initialize(pathItems);
	}

	private void initialize(Map<String, PathItem> pathItems) {
		log.debug("Initialize endpoint paths:");
		if (pathItems == null)
			return;
		for (Entry<String, PathItem> pathSchema : pathItems.entrySet()) {
			addEntityPath("post", pathSchema.getKey(), pathSchema.getValue());
			addEntityPath("put", pathSchema.getKey(), pathSchema.getValue());
		}
	}

	// Adds an entity path object if it corresponds to a method that accepts a body
	// with media type application/json
	private void addEntityPath(String method, String oaKey, PathItem oaPath) {
		log.trace("Check {} endpoint {}", method, oaKey);
		Operation operation = null;
		if ("post".equals(method))
			operation = oaPath.getPost();
		else if ("put".equals(method))
			operation = oaPath.getPut();

		String ref = getRequestBodyEntityRef(operation);
		if (ref != null) {
			log.trace("Found {}, body ref: {}", method, ref);
			String entityName = ref.replace("#/components/schemas/", "");
			log.debug("Add {} with path {} to entity {}", method, oaKey, entityName);
			EntityPath entityPath = new EntityPath();
			entityPath.entityName = entityName;
			entityPath.path = oaKey;
			this.entityPaths.get(method).add(entityPath);
		}
	}
	private String getRequestBodyEntityRef(Operation operation) {
		if (operation == null)
			return null;
		RequestBody body = operation.getRequestBody();
		if (body == null)
			return null;
		MediaType media = body.getContent().get("application/json");
		if (media == null)
			return null;
		Schema<?> schema = media.getSchema();
		if (schema == null)
			return null;
		return schema.get$ref();
	}

	/**
	 * Finds the paths that correspond to a given entity and method and adds the
	 * corresponding Ddl items to the model
	 */
	void addDdls(TdEntity entity, String method) {
		for (EntityPath entityPath : this.entityPaths.get(method)) {
			if (entityPath.entityName.equalsIgnoreCase(entity.getName())) {
				entity.addDdlsItem(new Ddl().command(method).query(entityPath.path));
				// note that there could be more than one item for a given entity and method.
			}
		}
	}

}
