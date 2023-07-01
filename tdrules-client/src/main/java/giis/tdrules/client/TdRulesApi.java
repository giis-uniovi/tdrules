package giis.tdrules.client;

import giis.tdrules.openapi.invoker.ApiClient;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.SqlRules;
import giis.tdrules.openapi.model.SqlRulesBody;

/**
 * Client api to access the rule and mutants services, extends the generated api
 * to provide a simpler interface to basic operations
 */
public class TdRulesApi extends giis.tdrules.openapi.api.SqlrulesApi {
	private static final String PRODUCTION_ENDPOINT = "https://in2test.lsi.uniovi.es/sqlrules/api/v3";

	/**
	 * New instance with a given service url
	 */
	public TdRulesApi(String endpoint) {
		super(new ApiClient()
				.setBasePath(endpoint == null || "".equals(endpoint.trim()) ? PRODUCTION_ENDPOINT : endpoint));
	}

	/**
	 * New instance to the default production service
	 */
	public TdRulesApi() {
		this("");
	}

	/**
	 * Gets the fpc rules for a query executed under the specified schema
	 */
	public SqlRules getRules(DbSchema schema, String query, String options) {
		options = options == null ? "" : options;
		return super.rulesPost(new SqlRulesBody().schema(schema).sql(query).options(options));
	}

	/**
	 * Gets the mutants for a query executed under the specified schema
	 */
	public SqlRules getMutants(DbSchema schema, String query, String options) {
		options = options == null ? "" : options;
		return super.mutantsPost(new SqlRulesBody().schema(schema).sql(query).options(options));
	}

}
