/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Portable.Util;
using Sharpen;

namespace Giis.Tdrules.Client
{
	/// <summary>Manages activation/deactivation and status checking of the cache.</summary>
	/// <remarks>
	/// Manages activation/deactivation and status checking of the cache.
	/// Not ensures concurrent behaviour
	/// </remarks>
	public class TdRulesCacheManager
	{
		private bool useCache = false;

		private string cacheLocation = string.Empty;

		private static string version = string.Empty;

		private string endpoint = string.Empty;

		public TdRulesCacheManager(string endpoint)
		{
			// cache is activated
			// folder where cache is stored (if activated)
			// First time that cache is set, gets the version from the service 
			// (stored statically to do not repeat again)
			// Version is part of the stored cache location name, therefore, 
			// if payloads for a request was cached, is invalidated if running again with different version
			// to know where the service is
			this.endpoint = endpoint;
		}

		// resets version memory, only for test
		public static void Reset()
		{
			version = string.Empty;
		}

		public virtual void SetCache(string location)
		{
			this.useCache = !string.Empty.Equals(Coalesce(location));
			// Gets version (only first time) to put as part of the cache folder
			if (string.Empty.Equals(version))
			{
				version = new TdRulesApi(endpoint).GetVersion().GetServiceVersion();
			}
			// NOSONAR only for non concurrent
			this.cacheLocation = this.useCache ? FileUtil.GetPath(location, version) : string.Empty;
		}

		public virtual bool UseCache()
		{
			return this.useCache;
		}

		public virtual TdRulesCache GetCache(string endpoint, object request)
		{
			return UseCache() ? new TdRulesCache(this.cacheLocation, endpoint, request) : null;
		}

		private string Coalesce(string value)
		{
			return value == null ? string.Empty : value.Trim();
		}
	}
}
