using Giis.Portable.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Client
{
    /// <summary>
    /// Manages activation/deactivation and status checking of the cache.
    /// Not ensures concurrent behaviour
    /// </summary>
    public class TdRulesCacheManager
    {
        private bool useCache = false; // cache is activated
        private string cacheLocation = ""; // folder where cache is stored (if activated)
        // First time that cache is set, gets the version from the service 
        // (stored statically to do not repeat again)
        // Version is part of the stored cache location name, therefore, 
        // if payloads for a request was cached, is invalidated if running again with different version
        private static string version = "";
        private string endpoint = ""; // to know where the service is
        public TdRulesCacheManager(string endpoint)
        {
            this.endpoint = endpoint;
        }

        // resets version memory, only for test
        public static void Reset()
        {
            version = "";
        }

        public virtual void SetCache(string location)
        {
            this.useCache = !"".Equals(Coalesce(location));
            if (!this.useCache)
                return;

            // Gets version (only first time) to put as part of the cache folder
            if ("".Equals(version))
            {
                version = new TdRulesApi(endpoint).GetVersion().GetServiceVersion(); // NOSONAR only for non concurrent
            }

            this.cacheLocation = this.useCache ? FileUtil.GetPath(location, version) : "";
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
            return value == null ? "" : value.Trim();
        }
    }
}