package giis.tdrules.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.portable.util.FileUtil;
import giis.portable.util.JavaCs;
import giis.tdrules.model.io.ModelJsonSerializer;

/**
 * Local storage cache for payloads sent to TdRules.
 * 
 * Calls to api endpoints instantiat this class.
 * First call to an endpoint with a request stores the response for this request.
 * Succesive calls, check if a response for this request already exists in the cache using 'hit':
 * - If true, it will return the object 'getPayload' with the cached response
 *   (a cast may be necessary).
 * - If false, call to the real endpoint and save the response to
 *   the cache by calling 'putPayload'
 */
public class TdRulesCache {
	private static final Logger log = LoggerFactory.getLogger(TdRulesCache.class);

	private ModelJsonSerializer serializer;
	String endpoint;
	String payload;
	String hash;
	String cacheFile;
	String hit;

	public TdRulesCache(String cacheFolder, String endpoint, Object request) {
		serializer = new ModelJsonSerializer();
		this.endpoint = endpoint;
		this.payload = serializer.serialize(request, true);
		this.hash = JavaCs.getHash(payload);
		this.ensureCacheFolder(cacheFolder, endpoint);
		this.cacheFile = getCacheFile(cacheFolder, endpoint, hash);
		this.hit = FileUtil.fileRead(cacheFile, false);
		log.debug("Cache {} {} hit: {}", endpoint, hash, this.hit != null);
	}

	/**
	 * Determines if there is a cached response for the request indicated at the instantiation
	 */
	public boolean hit() {
		return this.hit != null;
	}

	/**
	 * Gets the cached response of a given request stored in the cache ('hit' should be true)
	 */
	@SuppressWarnings("rawtypes")
	public Object getPayload(Class clazz) {
		return serializer.deserialize(hit, clazz);
	}

	/**
	 * Saves to the cache the response payload of a given request
	 */
	public void putPayload(Object result) {
		FileUtil.fileWrite(cacheFile, serializer.serialize(result, true));
		log.debug("Cache {} {} update: {}", endpoint, hash, result);
	}

	private void ensureCacheFolder(String cacheFolder, String endpoint) {
		FileUtil.createDirectory(FileUtil.getPath(cacheFolder, endpoint));
	}

	private String getCacheFile(String cacheFolder, String endpoint, String hash) {
		return FileUtil.getPath(cacheFolder, endpoint, hash + ".json");
	}

}
