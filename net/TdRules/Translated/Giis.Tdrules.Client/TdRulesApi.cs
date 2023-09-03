/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////

using Giis.Tdrules.Openapi.Model;
using Sharpen;

namespace Giis.Tdrules.Client
{
	/// <summary>
	/// Client api to access the rule and mutant services, extends the generated api
	/// to provide a simpler interface to basic operations
	/// Optional: Uses a cache to locally store the responses to the requests,
	/// (see TdRulesCache).
	/// </summary>
	/// <remarks>
	/// Client api to access the rule and mutant services, extends the generated api
	/// to provide a simpler interface to basic operations
	/// Optional: Uses a cache to locally store the responses to the requests,
	/// (see TdRulesCache). At the moment, only for sequential access to the services
	/// </remarks>
	public class TdRulesApi : Giis.Tdrules.Openapi.Api.TdRulesApi
	{
		public const string DefaultEndpoint = "https://in2test.lsi.uniovi.es/tdrules/api/v4";

		private TdRulesCacheManager cacheMgr;

		/// <summary>New instance with a given service url</summary>
		public TdRulesApi(string endpoint)
			: base(endpoint == null || endpoint.Trim()=="" ? DefaultEndpoint : endpoint)
		{
			// NOSONAR The purpose is to shadow the generated api
			cacheMgr = new TdRulesCacheManager(endpoint == null || endpoint.Trim()=="" ? DefaultEndpoint : endpoint);
		}

		/// <summary>New instance to the default production service</summary>
		public TdRulesApi()
			: this(string.Empty)
		{
		}

		public virtual Giis.Tdrules.Client.TdRulesApi SetCache(string location)
		{
			cacheMgr.SetCache(location);
			return this;
		}

		/// <summary>Gets the fpc rules for a query executed under the specified schema</summary>
		public virtual TdRules GetRules(TdSchema schema, string query, string options)
		{
			TdRulesBody request = new TdRulesBody();
			// don't use fluent for C# compatibility
			request.SetSchema(schema);
			request.SetQuery(query);
			request.SetOptions(Coalesce(options));
			TdRulesCache cache = cacheMgr.GetCache("rulesPost", request);
			if (cacheMgr.UseCache() && cache.Hit())
			{
				return (TdRules)cache.GetPayload(typeof(TdRules));
			}
			TdRules result = base.RulesPost(request);
			if (cacheMgr.UseCache())
			{
				cache.PutPayload(result);
			}
			return result;
		}

		/// <summary>Gets the mutants for a query executed under the specified schema</summary>
		public virtual TdRules GetMutants(TdSchema schema, string query, string options)
		{
			TdRulesBody request = new TdRulesBody();
			// don't use fluent for C# compatibility
			request.SetSchema(schema);
			request.SetQuery(query);
			request.SetOptions(Coalesce(options));
			TdRulesCache cache = cacheMgr.GetCache("mutantsPost", request);
			if (cacheMgr.UseCache() && cache.Hit())
			{
				return (TdRules)cache.GetPayload(typeof(TdRules));
			}
			TdRules result = base.MutantsPost(request);
			if (cacheMgr.UseCache())
			{
				cache.PutPayload(result);
			}
			return result;
		}

		public virtual QueryEntitiesBody GetEntities(string sql)
		{
			return GetEntities(sql, string.Empty);
		}

		public virtual QueryEntitiesBody GetEntities(string sql, string storetype)
		{
			TdRulesCache cache = cacheMgr.GetCache("queryEntitiesPost", sql + "storetype=" + storetype);
			if (cacheMgr.UseCache() && cache.Hit())
			{
				return (QueryEntitiesBody)cache.GetPayload(typeof(QueryEntitiesBody));
			}
			QueryEntitiesBody result = base.QueryEntitiesPost(storetype, sql);
			if (cacheMgr.UseCache())
			{
				cache.PutPayload(result);
			}
			return result;
		}

		public virtual QueryParametersBody GetParameters(string sql)
		{
			return GetParameters(sql, string.Empty);
		}

		public virtual QueryParametersBody GetParameters(string sql, string storetype)
		{
			TdRulesCache cache = cacheMgr.GetCache("queryParametersPost", sql + "storetype=" + storetype);
			if (cacheMgr.UseCache() && cache.Hit())
			{
				return (QueryParametersBody)cache.GetPayload(typeof(QueryParametersBody));
			}
			QueryParametersBody result = base.QueryParametersPost(storetype, sql);
			if (cacheMgr.UseCache())
			{
				cache.PutPayload(result);
			}
			return result;
		}

		public virtual VersionBody GetVersion()
		{
			return base.VersionGet();
		}

		private static string Coalesce(string value)
		{
			return value == null ? string.Empty : value.Trim();
		}
	}
}
