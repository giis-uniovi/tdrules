using Giis.Tdrules.Openapi.Model;

namespace Giis.Tdrules.Client
{
	public class TdRulesApi : Giis.Tdrules.Openapi.Api.TdRulesApi
	{
		private static string PRODUCTION_ENDPOINT = "https://in2test.lsi.uniovi.es/tdrules/api/v4";

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
        public TdRules GetRules(TdSchema schema, string query, string options)
        {
            options = options == null ? "" : options;
            return base.RulesPost(new TdRulesBody(query, schema, options));
        }

        /// <summary>
        ///  Obtiene los mutantes para un esquema y query
        /// </summary>
        public TdRules GetMutants(TdSchema schema, string query, string options)
        {
            options = options == null ? "" : options;
            return base.MutantsPost(new TdRulesBody(query, schema, options));
        }

    }
}
