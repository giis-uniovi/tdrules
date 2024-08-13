package giis.tdrules.store.loader.oa;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.openapi.model.Ddl;
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

	private String url = "";
	private TdSchema model = null;
	// To send api requests, uses this default ApiWriter to send requests to a server,
	// unless another is set
	private ApiWriter writer = new ApiWriter();

	@Override
	public IPathResolver setServerUrl(String url) {
		this.url = url;
		return this;
	}
	
	@Override 
	public IPathResolver setApiWriter(ApiWriter writer) {
		this.writer = writer;
		return this;
	}
	
	@Override
	public ApiWriter getApiWriter() {
		return this.writer;
	}

	@Override
	public String getEndpointPath(String entityName) {
		if (this.model == null) {
			log.debug("No model set, resolving endpoint for entity {} by entity name", entityName);
			return this.url + "/" + entityName.toLowerCase();
		}
		// find a path in the model
		List<Ddl> ddls = this.model.getEntity(entityName).getDdls();
		for (Ddl ddl : giis.tdrules.model.ModelUtil.safe(ddls))
			if ("post".equals(ddl.getCommand())) {
				log.trace("Resolving endpoint for entity {} from the model: {}", entityName, ddl.getQuery());
				return this.url + ddl.getQuery();
			}
		// not found uses the entity name as fallback

		log.warn("There is no POST operation for entity {} in the schema model, returning entity name as fallback");
		return this.url + "/" + entityName.toLowerCase();
	}

	/**
	 * Sets a a schema model that can be used to determine the endpoint.
	 */
	public IPathResolver setSchemaModel(TdSchema model) {
		this.model = model;
		return this;
	}

}
