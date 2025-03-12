using Java.Util;
using NLog;
using Giis.Portable.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Some utilities to read jdbc connection values from external files or
    /// environment variables
    /// </summary>
    public class JdbcProperties
    {
        private static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(JdbcProperties));
        /// <summary>
        /// Gets a value from an environment variable, if not defined reads from a
        /// fallback properties file
        /// </summary>
        public virtual string GetEnvVar(string fallbackFileName, string name)
        {
            log.Debug("Get '" + name + "' from environment (with fallback)");
            string value = JavaCs.GetEnvironmentVariable(name);
            if (value == null)
                value = GetProp(fallbackFileName, name);
            return value;
        }

        /// <summary>
        /// Gets a value from an properties file
        /// </summary>
        public virtual string GetProp(string fileName, string name)
        {
            log.Debug("Get '" + name + "' property from file '" + fileName + "'");
            Properties prop = new PropertiesFactory().GetPropertiesFromFilename(fileName);
            if (prop == null)
                throw new SchemaException("Can't read properties file '" + fileName + "'");
            string value = prop.GetProperty(name);
            if (value == null)
                throw new SchemaException("Can't get '" + name + "' from properties file '" + fileName + "'");
            else if ("".Equals(value))
                log.Warn("Property '" + name + "' is empty");
            return value;
        }
    }
}