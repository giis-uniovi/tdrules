using Giis.Tdrules.Openapi.Model;

namespace Giis.Tdrules.Client
{
	public class TdRulesApi : Giis.Tdrules.Openapi.Api.SqlrulesApi
	{
		private static string PRODUCTION_ENDPOINT = "https://in2test.lsi.uniovi.es/sqlrules/api/v3";

		/// <summary>
		/// Instancia el api para la url del servicio rest indicado en el endpoint
		/// </summary>
		public TdRulesApi(string endpoint) 
			: base(endpoint == null || endpoint.Trim()=="" ? PRODUCTION_ENDPOINT : endpoint)
		{
		}

		/// <summary>
		/// Instancia el api para la url del servicio rest en produccion en el servidor del grup GIIS
		/// </summary>
		public TdRulesApi() : this("")
		{
		}

        /// <summary>
        /// Obtiene las reglas de cobertura para un esquema y query
        /// </summary>
        public SqlRules GetRules(DbSchema schema, string query, string options)
        {
            options = options == null ? "" : options;
            return base.RulesPost(new SqlRulesBody(query, schema, options));
        }

        /// <summary>
        ///  Obtiene los mutantes para un esquema y query
        /// </summary>
        public SqlRules GetMutants(DbSchema schema, string query, string options)
        {
            options = options == null ? "" : options;
            return base.MutantsPost(new SqlRulesBody(query, schema, options));
        }

    }
}
