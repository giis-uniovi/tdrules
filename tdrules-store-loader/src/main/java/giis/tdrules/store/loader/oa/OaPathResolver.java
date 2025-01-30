package giis.tdrules.store.loader.oa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Default implementation to determines the url and path to create an object
 * through an api call:
 * (1) If a TdModel of the OpenApi model has been configured, search it for a path. 
 *     If found, uses it.
 * (2) If not, uses a path composed by the server url, and the entity name in lowercase.
 * 
 * If there are particular cases that do not fit within the above, a subclass
 * can be used.
 */
public class OaPathResolver implements IPathResolver {
	protected static final Logger log = LoggerFactory.getLogger(OaPathResolver.class);

	protected TdSchema model = null;

	@Override
	public String getEndpointPath(String entityName) {
		// Verify that the path can be found, if not, fallback with the appropriate debug message
		if (this.model == null)
			return fallback("No model set for entity {}", entityName);

		TdEntity entity = this.model.getEntityOrNull(entityName);
		if (entity == null)
			return fallback("Entity {} not found in the model", entityName);

		// only find post, if endpoint requires put, we need a custom path resolver
		Ddl ddl = entity.getDdl("post");
		if (ddl == null) 
			return fallback("No POST operation for entity {} in the model", entityName);
		
		log.trace("Resolving endpoint path for entity {} from the model: {}", entityName, ddl.getQuery());
		return ddl.getQuery();
	}
	
	private String fallback(String cause, String entityName) {
		String path = (entityName.startsWith("/") ? "" : "/") + entityName.toLowerCase(); //NOSONAR
		log.warn(cause + ", fallback to endopint path: {}", entityName, path);
		return path;
	}

	/**
	 * Sets a a schema model that can be used to determine the endpoint.
	 */
	public IPathResolver setSchemaModel(TdSchema model) {
		this.model = model;
		return this;
	}

}
