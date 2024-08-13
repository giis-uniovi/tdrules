package giis.tdrules.client.oa;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

/**
 * Determines the path required by the POST and PUT operations associated to the
 * entities in the TdSchema (with a request body that corresponds to some entity
 * in the schema). These operations are included in the schema of each entity in
 * the Ddls attribute.
 */
public class OaSchemaPathResolver {
	protected static final Logger log = LoggerFactory.getLogger(OaSchemaPathResolver.class);

	/**
	 * Main processing of the path resolver, stores the paths in the Ddls atrribute of entities
	 */
	public void resolve(Map<String, PathItem> pathItems, TdSchema tdSchema) {
		log.debug("Check for endpoint paths");
		if (pathItems == null)
			return;
		for (Entry<String, PathItem> pathSchema : pathItems.entrySet()) {
			String ref = getRequestBodyRef("post", pathSchema.getKey(), pathSchema.getValue());
			if (ref != null)
				addEntityDdlCommand(tdSchema, "post", pathSchema.getKey(), ref);
			// Search in put also
			ref = getRequestBodyRef("put", pathSchema.getKey(), pathSchema.getValue());
			if (ref != null)
				addEntityDdlCommand(tdSchema, "put", pathSchema.getKey(), ref);
		}
	}

	private void addEntityDdlCommand(TdSchema tdSchema, String method, String path, String oaRef) {
		String entityName = oaRef.replace("#/components/schemas/", "");
		log.debug("Add {} with path {} to Entity {}", method, path, entityName);
		TdEntity entity = tdSchema.getEntityOrNull(entityName);
		if (entity == null) {
			log.debug("No associated entity found");
			return;
		}
		Ddl ddl = new Ddl().command(method).query(path);
		entity.addDdlsItem(ddl);
	}

	private String getRequestBodyRef(String method, String oaKey, PathItem oaPath) {
		log.trace("Check {} endpoint {}", method, oaKey);
		Operation operation = null;
		if ("post".equals(method))
			operation = oaPath.getPost();
		else if ("put".equals(method))
			operation = oaPath.getPut();

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
		String ref = schema.get$ref();
		if (ref == null)
			return null;
		log.trace("Found post, body ref: {}", ref);
		return ref;
	}

}
