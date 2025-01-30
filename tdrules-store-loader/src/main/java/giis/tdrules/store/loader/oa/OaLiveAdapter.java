package giis.tdrules.store.loader.oa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	protected OaBasicAuthStore authStore;
	protected ApiWriter apiWriter = new ApiWriter();

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
	}

	@Override
	public void endWrite() {
		super.endWrite();
		this.lastResponse = "";
		String json = super.getLast();
		resolver.setSchemaModel(model);
		String path = resolver.getEndpointPath(this.currentEntity);
		if (path == null) {
			log.warn("endWrite: empty path, no post sent, payload: {}", json);
			return;
		}
		boolean usePut = resolver.usePut(this.currentEntity);
		String url = composeUrl(this.serverUrl, path);
		log.debug("endWrite: sending {} to url {}", usePut ? "PUT" : "POST", url);

		apiWriter.reset();
		if (authStore != null) // Store or set credentials, if applicable
			authStore.processAuthentication(this.currentEntity, json, apiWriter);
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
