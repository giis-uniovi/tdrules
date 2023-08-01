package giis.tdrules.client;

import giis.portable.util.FileUtil;

/**
 * Manages activation/deactivation and status checking of the cache.
 * Not ensures concurrent behaviour
 */
public class TdRulesCacheManager {

	private boolean useCache = false; // cache is activated
	private String cacheLocation = ""; // folder where cache is stored (if activated)
	// First time that cache is set, gets the version from the service 
	// (stored statically to do not repeat again)
	// Version is part of the stored cache location name, therefore, 
	// if payloads for a request was cached, is invalidated if running again with different version
	private static String version = "";
	private String endpoint = ""; // to know where the service is

	public TdRulesCacheManager(String endpoint) {
		this.endpoint = endpoint;
	}

	// resets version memory, only for test
	public static void reset() {
		version = "";
	}

	public void setCache(String location) {
		this.useCache = !"".equals(coalesce(location));
		// Gets version (only first time) to put as part of the cache folder
		if ("".equals(version)) {
			version = new TdRulesApi(endpoint).getVersion().getServiceVersion(); // NOSONAR only for non concurrent
		}
		this.cacheLocation = this.useCache ? FileUtil.getPath(location, version) : "";
	}

	public boolean useCache() {
		return this.useCache;
	}

	public TdRulesCache getCache(String endpoint, Object request) {
		return useCache() ? new TdRulesCache(this.cacheLocation, endpoint, request) : null;
	}

	private String coalesce(String value) {
		return value == null ? "" : value.trim();
	}

}
