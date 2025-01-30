/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using Sharpen;

namespace Giis.Tdrules.Model.Shared
{
	public class RuleTypes
	{
		private RuleTypes()
		{
			throw new InvalidOperationException("Utility class");
		}

		public const string Fpc = "fpc";

		public const string Mutation = "mutation";

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
			{
				return string.Empty;
			}
			if (ruleType.Contains(Fpc))
			{
				return v4 ? Fpc : "sqlfpc";
			}
			else
			{
				if (ruleType.Contains(Mutation))
				{
					return v4 ? Mutation : "sqlmutation";
				}
			}
			return string.Empty;
		}
	}
}
