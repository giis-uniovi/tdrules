using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Model.Shared
{
    public class RuleTypes
    {
        private RuleTypes()
        {
            throw new InvalidOperationException("Utility class");
        }

        public static readonly string FPC = "fpc";
        public static readonly string MUTATION = "mutation";
        // Conversions for compatibility between api v3 and v4
        public static string NormalizeV4(string ruleType)
        {
            return Normalize(true, ruleType);
        }

        public static string NormalizeV3(string ruleType)
        {
            return Normalize(false, ruleType);
        }

        private static string Normalize(bool v4, string ruleType)
        {
            if (ruleType == null)
                return "";
            if (ruleType.Contains(FPC))
                return v4 ? FPC : "sqlfpc";
            else if (ruleType.Contains(MUTATION))
                return v4 ? MUTATION : "sqlmutation";
            return "";
        }
    }
}