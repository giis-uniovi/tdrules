package giis.tdrules.store.loader.oa;

/**
 * Determines the url and path to create an object through an api call.
 */
public interface IPathResolver {

	/**
	 * Sets the url (without path) of the service
	 */
	IPathResolver setServerUrl(String url);
	
	/**
	 * Sets an alternative ApiWriter used to send requests
	 */
	IPathResolver setApiWriter(ApiWriter writer);

	/**
	 * Geths the ApiWriter used to send requests
	 * (this is the default ApiWriter if none has been set)
	 */
	ApiWriter getApiWriter();

	/**
	 * Determines the url where a POST to create an object must be sent.
	 */
	String getEndpointPath(String entityName);

	/**
	 * Although post is the appropriate method to create entities, some APIs may use
	 * put; this method can be overriden by a custom path resolver to indicate the
	 * entities that require put (return true)
	 */
	default boolean usePut(String entityName) {
		return false;
	}

}