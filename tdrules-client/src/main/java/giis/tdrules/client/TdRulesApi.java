package giis.tdrules.client;

import giis.tdrules.openapi.invoker.ApiClient;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.VersionBody;
import giis.tdrules.openapi.model.QueryEntitiesBody;
import giis.tdrules.openapi.model.QueryParametersBody;
import giis.tdrules.openapi.model.TdRules;
import giis.tdrules.openapi.model.TdRulesBody;

/**
 * Client api to access the rule and mutant services, extends the generated api
 * to provide a simpler interface to basic operations
 * 
 * Optional: Uses a cache to locally store the responses to the requests,
 * (see TdRulesCache). At the moment, only for sequential access to the services
 */
public class TdRulesApi extends giis.tdrules.openapi.api.TdRulesApi { // NOSONAR The purpose is to shadow the generated api
	public static final String DEFAULT_ENDPOINT = "https://in2test.lsi.uniovi.es/tdrules/api/v4";

	private TdRulesCacheManager cacheMgr;

	/**
	 * New instance with a given service url
	 */
	public TdRulesApi(String endpoint) {
		super(new ApiClient().setBasePath("".equals(coalesce(endpoint)) ? DEFAULT_ENDPOINT : endpoint));
		cacheMgr = new TdRulesCacheManager(super.getApiClient().getBasePath());
	}

	/**
	 * New instance to the default production service
	 */
	public TdRulesApi() {
		this("");
	}

	public TdRulesApi setCache(String location) {
		cacheMgr.setCache(location);
		return this;
	}

	/**
	 * Gets the fpc rules for a query executed under the specified schema
	 */
	public TdRules getRules(TdSchema schema, String query, String options) {
		TdRulesBody request = new TdRulesBody(); // don't use fluent for C# compatibility
		request.setSchema(schema);
		request.setQuery(query);
		request.setOptions(coalesce(options));
		TdRulesCache cache = cacheMgr.getCache("rulesPost", request);

		if (cacheMgr.useCache() && cache.hit())
			return (TdRules) cache.getPayload(TdRules.class);
		TdRules result = super.rulesPost(request);
		if (cacheMgr.useCache())
			cache.putPayload(result);
		return result;
	}

	/**
	 * Gets the mutants for a query executed under the specified schema
	 */
	public TdRules getMutants(TdSchema schema, String query, String options) {
		TdRulesBody request = new TdRulesBody(); // don't use fluent for C# compatibility
		request.setSchema(schema);
		request.setQuery(query);
		request.setOptions(coalesce(options));
		TdRulesCache cache = cacheMgr.getCache("mutantsPost", request);

		if (cacheMgr.useCache() && cache.hit())
			return (TdRules) cache.getPayload(TdRules.class);
		TdRules result = super.mutantsPost(request);
		if (cacheMgr.useCache())
			cache.putPayload(result);
		return result;
	}

	public QueryEntitiesBody getEntities(String sql) {
		TdRulesCache cache = cacheMgr.getCache("queryEntitiesPost", sql);
		if (cacheMgr.useCache() && cache.hit())
			return (QueryEntitiesBody) cache.getPayload(QueryEntitiesBody.class);
		QueryEntitiesBody result = super.queryEntitiesPost(sql);
		if (cacheMgr.useCache())
			cache.putPayload(result);
		return result;
	}

	public QueryParametersBody getParameters(String sql) {
		TdRulesCache cache = cacheMgr.getCache("queryParametersPost", sql);
		if (cacheMgr.useCache() && cache.hit())
			return (QueryParametersBody) cache.getPayload(QueryParametersBody.class);
		QueryParametersBody result = super.queryParametersPost(sql);
		if (cacheMgr.useCache())
			cache.putPayload(result);
		return result;
	}
	
	public VersionBody getVersion() {
		return super.versionGet();
	}

	private static String coalesce(String value) {
		return value == null ? "" : value.trim();
	}

}
