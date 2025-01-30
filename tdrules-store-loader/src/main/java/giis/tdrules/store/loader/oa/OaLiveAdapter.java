package giis.tdrules.store.loader.oa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import giis.tdrules.store.loader.LoaderException;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.IDataAdapter;

/**
 * A Live Data Adapter to generate a json object according to an openapi model and
 * issues a POST call to the backend to insert the data.
 * 
 * Instantiation of this class requires an IPathResolver that determines the
 * appropriate endpoints at the backend.
 */
public class OaLiveAdapter extends OaLocalAdapter {
	private static final Logger log = LoggerFactory.getLogger(OaLiveAdapter.class);

	protected String serverUrl;
	protected TdSchema model;
	protected IPathResolver resolver;
	protected String path; // can be null (meaning that no post will be sent)
	protected UriRewriter rewriter;
	protected OaBasicAuthStore authStore;
	protected ApiWriter apiWriter = new ApiWriter();
	// The currentRoot (defined in the parent) is is managed with the whole content of the entity,
	// but we must handle the attributes in the body and the path parameters separately
	protected ObjectNode currentRequestBody;
	protected ObjectNode currentPathParams;


	protected String lastResponse = ""; // remembers last response to allow determine the symbolic keys

	public OaLiveAdapter(String serverUrl) {
		this.serverUrl = serverUrl;
		this.resolver = new OaPathResolver(); // default implementation, can be overriden
	}

	public OaLiveAdapter setPathResolver(IPathResolver resolver) {
		this.resolver = resolver;
		return this;
	}
	
	public OaLiveAdapter setSchemaModel(TdSchema model) {
		this.model = model;
		return this;
	}

	public OaLiveAdapter setApiWriter(ApiWriter writer) {
		this.apiWriter = writer;
		return this;
	}

	public OaLiveAdapter setAuthStore(OaBasicAuthStore authStore) {
		this.authStore = authStore;
		return this;
	}

	@Override
	public void reset() {
		super.reset();
		lastResponse = "";
	}

	@Override
	public IDataAdapter getNewLocalAdapter() {
		// As this is a live adapter, there msut be a local adapter that does not call any api
		return new OaLocalAdapter();
	}

	@Override
	public String getLastResponse() {
		return this.lastResponse;
	}

	@Override
	public void beginWrite(String entityName) {
		super.beginWrite(entityName);
		this.lastResponse = "";
		ObjectMapper mapper = new ObjectMapper();
		currentRequestBody = mapper.createObjectNode();
		currentPathParams = mapper.createObjectNode();
		// need to determine the path here for writing values as path parameters
		resolver.setSchemaModel(model);
		path = resolver.getEndpointPath(currentEntity);
		rewriter = new UriRewriter(path);
	}

	@Override
	public void writeValue(String dataType, String attrName, String attrValue) {
		// In all cases, currentRoot includes all data to represent the entity stored,
		// but request body and path params must be handled separately when post
		super.writeValueTo(dataType, attrName, attrValue, currentRoot);
		if (rewriter.hasPathParam(attrName)) {
			super.writeValueTo(dataType, attrName, attrValue, currentPathParams);
			rewriter.rewritePathParam(attrName, attrValue);
		} else {
			super.writeValueTo(dataType, attrName, attrValue, currentRequestBody);
		}
	}
	
	@Override
	public void endWrite() {
		allGenerated.add(new GeneratedObject(currentEntity, currentRoot.toString())); // same as parent
		log.debug("endWrite: entity={} Params={} Body={}", this.currentEntity, currentPathParams.toString(), currentRequestBody.toString());
		this.lastResponse = "";
		String json = currentRequestBody.toString();
		if (path == null) {
			log.warn("endWrite: empty path, no post sent, payload: {}", json);
			return;
		}
		boolean usePut = resolver.usePut(this.currentEntity);
		String url = composeUrl(this.serverUrl, this.path);
		log.debug("endWrite: sending {} to url {}", usePut ? "PUT" : "POST", url);

		apiWriter.reset();
		if (authStore != null) // Store or set credentials, if applicable
			authStore.processAuthentication(this.currentEntity, json, apiWriter);
		// from here, use the rewritten url with resolved params to post
		url = composeUrl(this.serverUrl, rewriter.getUrl()); 
		ApiResponse response = apiWriter.post(url, json, usePut);
		int status = response.getStatus();
		String reason = response.getReason();
		String body = response.getBody();
		String message = status + " " + reason + " - body: " + body;
		log.debug("endWrite: response={}", message);

		// Check the status and raises exception. Currently, only 2xx statuses are valid.
		if (status / 100 != 2) {
			String fullMessage = "endWrite: Did not completed properly, response: " + message + "\n  Posting to: "
					+ url + "\n  With payload: " + json;
			log.error(fullMessage);
			throw new LoaderException(fullMessage);
		}
		this.lastResponse = body; // to allow get the symbolic keys later
	}

	private String composeUrl(String server, String path) {
		return server + (server.endsWith("/") || path.startsWith("/") ? "" : "/") + path;
	}

}
