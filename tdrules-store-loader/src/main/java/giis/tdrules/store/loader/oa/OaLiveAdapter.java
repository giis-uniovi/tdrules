package giis.tdrules.store.loader.oa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.gen.IUidGen;
import giis.tdrules.store.loader.shared.LoaderException;
import giis.tdrules.openapi.model.TdSchema;

/**
 * A Live Data Adapter to generate a json object according to an openapi model and
 * issues a POST call to the backend to insert the data.
 * 
 * Instantiation of this class requires an IPathResolver that determines the
 * appropriate endpoints at the backend.
 */
public class OaLiveAdapter extends OaLocalAdapter {
	private static final Logger log = LoggerFactory.getLogger(OaLiveAdapter.class);

	// In addition to the current object, this adapter stores the below
	protected String serverUrl;
	protected TdSchema model;
	protected IPathResolver resolver;
	protected String path; // can be null (meaning that no post will be sent)
	protected UriRewriter rewriter;
	protected OaBasicAuthStore authStore;
	protected ApiWriter apiWriter = new ApiWriter();

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
	public IDataAdapter getNewLocalAdapter() {
		// As this is a live adapter, there msut be a local adapter that does not call any api
		return new OaLocalAdapter();
	}

	@Override
	public String getLastResponse() {
		return this.current.responseString;
	}
	
	@Override
	public String getLastUid(IUidGen uidGen, String entityName, String attributeName) {
		uidGen.setLastResponse(entityName, current.responseString);
		return uidGen.getLast(entityName, attributeName);
	}

	@Override
	public void beginWrite(String entityName) {
		super.beginWrite(entityName); // the parent creates the current object
		// need to determine the path here for writing values as path parameters
		resolver.setSchemaModel(model);
		path = resolver.getEndpointPath(current.entityName);
		rewriter = new UriRewriter(path);
	}

	@Override
	public void writeValue(String dataType, String attrName, String attrValue) {
		// In all cases, current generated data includes all data to represent the entity stored,
		// but request body and path params must be handled separately when post
		super.writeValueTo(dataType, attrName, attrValue, current.generated);
		if (rewriter.hasPathParam(attrName)) {
			super.writeValueTo(dataType, attrName, attrValue, current.pathParams);
			rewriter.rewritePathParam(attrName, attrValue);
		} else {
			super.writeValueTo(dataType, attrName, attrValue, current.requestBody);
		}
	}
	
	@Override
	public void endWrite() {
		allGenerated.add(current); // same as parent
		log.debug("endWrite: entity={} Params={} Body={}", this.current.entityName, current.pathParams.toString(), current.requestBody.toString());
		String json = current.requestBody.toString();
		if (path == null) {
			log.warn("endWrite: empty path, no post sent, payload: {}", json);
			return;
		}
		boolean usePut = resolver.usePut(current.entityName);
		String url = composeUrl(this.serverUrl, this.path);
		log.debug("endWrite: sending {} to url {}", usePut ? "PUT" : "POST", url);

		apiWriter.reset();
		if (authStore != null) // Store or set credentials, if applicable
			authStore.processAuthentication(current.entityName, json, apiWriter);
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
		current.responseString = body; // to allow get the symbolic keys later
	}

	private String composeUrl(String server, String path) {
		return server + (server.endsWith("/") || path.startsWith("/") ? "" : "/") + path;
	}

}
