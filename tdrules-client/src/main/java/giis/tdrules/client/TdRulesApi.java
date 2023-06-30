package giis.tdrules.client;

import giis.tdrules.openapi.invoker.ApiClient;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.SqlRules;
import giis.tdrules.openapi.model.SqlRulesBody;

/**
 * El api para acceso a los servicios de generacion de reglas de cobertura sql (SqlRules);
 * extiend el api generado para proporcionar una interfaz mas simple a operaciones basicas.
 */
public class TdRulesApi extends giis.tdrules.openapi.api.SqlrulesApi {
	private static final String PRODUCTION_ENDPOINT = "https://in2test.lsi.uniovi.es/sqlrules/api/v3";

	/**
	 * Instancia el api para la url del servicio rest indicado en el endpoint
	 */
	public TdRulesApi(String endpoint) {
		super(new ApiClient().setBasePath(endpoint==null || "".equals(endpoint.trim()) ? PRODUCTION_ENDPOINT : endpoint));
	}

	/**
	 * Instancia el api para la url del servicio rest en produccion en el servidor del grup GIIS
	 */
	public TdRulesApi() {
		this("");
	}
	
	/**
	 * Obtiene las reglas de cobertura para un esquema y query
	 */
	public SqlRules getRules(DbSchema schema, String query, String options) {
		options = options==null ? "" : options;
		return super.rulesPost(new SqlRulesBody().schema(schema).sql(query).options(options));
	}

	/**
	 * Obtiene las los mutantes para un esquema y query
	 */
	public SqlRules getMutants(DbSchema schema, String query, String options) {
		options = options==null ? "" : options;
		return super.mutantsPost(new SqlRulesBody().schema(schema).sql(query).options(options));
	}
	
}
