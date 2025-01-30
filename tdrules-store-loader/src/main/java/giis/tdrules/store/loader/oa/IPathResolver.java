package giis.tdrules.store.loader.oa;

import giis.tdrules.openapi.model.TdSchema;

/**
 * Determines the url and path to create an object through an api call.
 */
public interface IPathResolver {

	/**
	 * Sets a a schema model that can be used to determine the paths.
	 */
	default IPathResolver setSchemaModel(TdSchema model) {
		// override if the resolver needs a model
		return this;
	}

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