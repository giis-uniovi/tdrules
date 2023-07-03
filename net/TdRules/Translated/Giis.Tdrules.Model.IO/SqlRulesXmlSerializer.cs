/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using System.Text;
using Giis.Portable.Xml.Tiny;
using Giis.Tdrules.Model;
using Giis.Tdrules.Openapi.Model;
using Sharpen;

namespace Giis.Tdrules.Model.IO
{
	/// <summary>Custom xml serialization/deserialization of a rules model</summary>
	public class SqlRulesXmlSerializer : BaseXmlSerializer
	{
		private const string XmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

		private const string Sqlfpc = "sqlfpc";

		private const string Sqlmutation = "sqlmutation";

		private const string Version = "version";

		private const string Development = "development";

		private const string Parsedsql = "parsedsql";

		private const string Error = "error";

		public virtual SqlRules Deserialize(string xml)
		{
			XNode xsqlrules = new XNode(xml);
			SqlRules sqlrules = new SqlRules();
			string rulesClass = xsqlrules.Name();
			if ("sqlfpcws".Equals(rulesClass))
			{
				rulesClass = Sqlfpc;
			}
			else
			{
				if ("sqlmutationws".Equals(rulesClass))
				{
					rulesClass = Sqlmutation;
				}
			}
			if (!Sqlmutation.Equals(rulesClass) && !Sqlfpc.Equals(rulesClass))
			{
				throw new Exception("Root element must be sqlmutation or sqlfpc");
			}
			//NOSONAR
			sqlrules.SetRulesClass(rulesClass);
			sqlrules.SetVersion(GetElemAttribute(xsqlrules, Version));
			sqlrules.SetEnvironment(xsqlrules.GetChild(Version).GetChild(Development) == null ? string.Empty : Development);
			foreach (string attr in GetExtendedAttributeNames(xsqlrules, new string[] {  }))
			{
				sqlrules.PutSummaryItem(attr, xsqlrules.GetAttribute(attr));
			}
			sqlrules.SetSql(GetElemAttribute(xsqlrules, "sql"));
			sqlrules.SetParsedsql(GetElemAttribute(xsqlrules, Parsedsql));
			sqlrules.SetError(GetElemAttribute(xsqlrules, Error));
			string ruleTag = RulesClassToRuleTag(rulesClass);
			XNode xrules = xsqlrules.GetChild(ruleTag + "s");
			if (xrules == null)
			{
				return sqlrules;
			}
			foreach (XNode rnode in xrules.GetChildren(ruleTag))
			{
				SqlRule rule = new SqlRule();
				foreach (string attr_1 in GetExtendedAttributeNames(rnode, new string[] {  }))
				{
					rule.PutSummaryItem(attr_1, rnode.GetAttribute(attr_1));
				}
				rule.SetId(GetElemAttribute(rnode, "id"));
				rule.SetCategory(GetElemAttribute(rnode, "category"));
				rule.SetMaintype(GetElemAttribute(rnode, "type"));
				rule.SetSubtype(GetElemAttribute(rnode, "subtype"));
				rule.SetLocation(GetElemAttribute(rnode, "location"));
				rule.SetEquivalent(rnode.GetChild("equivalent") == null ? string.Empty : "true");
				rule.SetSql(GetElemAttribute(rnode, "sql"));
				rule.SetDescription(GetElemAttribute(rnode, "description"));
				rule.SetError(GetElemAttribute(rnode, Error));
				sqlrules.AddRulesItem(rule);
			}
			return sqlrules;
		}

		private string RulesClassToRuleTag(string rulesClass)
		{
			return Sqlmutation.Equals(rulesClass) ? "mutant" : "fpcrule";
		}

		public virtual string Serialize(SqlRules sqr)
		{
			StringBuilder sb = new StringBuilder();
			string rulesClass = sqr.GetRulesClass();
			sb.Append(XmlHeader).Append("\n<" + rulesClass).Append(SetExtendedAttributes(sqr.GetSummary())).Append(">");
			sb.Append("\n<version>").Append(sqr.GetVersion()).Append(Development.Equals(sqr.GetEnvironment()) ? "<development/>" : string.Empty).Append("</version>");
			sb.Append(SetElemAttribute(0, "sql", sqr.GetSql())).Append(SetElemAttribute(0, Parsedsql, sqr.GetParsedsql()));
			sb.Append(SetElemAttribute(0, Error, sqr.GetError()));
			string ruleTag = RulesClassToRuleTag(rulesClass);
			if (string.Empty.Equals(sqr.GetError()))
			{
				//no muestra tag si ha habido error generando
				sb.Append("\n<" + ruleTag + "s>");
				foreach (SqlRule rule in ModelUtil.Safe(sqr.GetRules()))
				{
					sb.Append("\n").Append(Serialize(rule, ruleTag));
				}
				sb.Append("\n</" + ruleTag + "s>");
			}
			sb.Append("\n</" + rulesClass + ">");
			return sb.ToString();
		}

		public virtual string Serialize(SqlRule rule, string ruleTag)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append("  <" + ruleTag).Append(SetExtendedAttributes(rule.GetSummary())).Append(">");
			sb.Append(SetElemAttribute("id", rule.GetId())).Append(SetElemAttribute("category", rule.GetCategory())).Append(SetElemAttribute("type", rule.GetMaintype())).Append(SetElemAttribute("subtype", rule.GetSubtype())).Append(SetElemAttribute("location", rule.GetLocation())).Append("true"
				.Equals(rule.GetEquivalent()) ? "\n    <equivalent/>" : string.Empty).Append(SetElemAttribute(4, "sql", rule.GetSql())).Append(SetElemAttribute(4, "description", rule.GetDescription())).Append(SetElemAttribute(4, Error, rule.GetError())).Append("\n  </" + ruleTag + ">");
			return sb.ToString();
		}

		//Deserializacion de otros objetos obtenidos de los servicios SqlRules
		//Aunque en v2 se devuelve una estructura similar a las reglas que incluye version, entorno, etc.
		//para la v3 se simplificara, y solo se tiene el error (uno solo) o el contenido objeto a devolver
		public virtual SqlTableListBody DeserializeTables(string xml)
		{
			XNode xtables = new XNode(xml);
			SqlTableListBody tables = new SqlTableListBody();
			tables.SetError(GetElemAttribute(xtables, Error));
			foreach (XNode xtable in xtables.GetChildren("table"))
			{
				tables.AddTablesItem(XNode.DecodeText(xtable.InnerText()));
			}
			return tables;
		}

		public virtual SqlParametersBody DeserializeParameters(string xml)
		{
			XNode xparams = new XNode(xml);
			SqlParametersBody sparams = new SqlParametersBody();
			sparams.SetError(GetElemAttribute(xparams, Error));
			XNode paramNode = xparams.GetChild("parameters");
			if (paramNode == null)
			{
				return sparams;
			}
			foreach (XNode xparam in paramNode.GetChildren("parameter"))
			{
				SqlParam param = new SqlParam();
				param.SetName(xparam.GetAttribute("name"));
				param.SetValue(xparam.GetAttribute("value"));
				sparams.AddParametersItem(param);
			}
			return sparams;
		}

		public virtual string Serialize(SqlTableListBody model)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append(XmlHeader).Append("\n<sqltables>");
			if (string.Empty.Equals(model.GetError()))
			{
				foreach (string table in ModelUtil.Safe(model.GetTables()))
				{
					sb.Append(SetElemAttribute(0, "table", table));
				}
			}
			else
			{
				sb.Append(SetElemAttribute(0, Error, model.GetError()));
			}
			sb.Append("\n</sqltables>");
			return sb.ToString();
		}

		public virtual string Serialize(SqlParametersBody model)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append(XmlHeader).Append("\n<sqlparameters>");
			if (string.Empty.Equals(model.GetError()))
			{
				sb.Append("\n<parameters>");
				foreach (SqlParam param in ModelUtil.Safe(model.GetParameters()))
				{
					sb.Append("\n<parameter").Append(SetAttribute("name", param.GetName())).Append(SetAttribute("value", param.GetValue())).Append(" />");
				}
				sb.Append("\n</parameters>").Append(SetElemAttribute(0, Parsedsql, model.GetParsedsql()));
			}
			else
			{
				sb.Append(SetElemAttribute(0, Error, model.GetError()));
			}
			sb.Append("\n</sqlparameters>");
			return sb.ToString();
		}

		public virtual string Serialize(SqlRulesBody sqb)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append("<body>");
			sb.Append(SetElemAttribute(0, "sql", sqb.GetSql()));
			if (sqb.GetSchema() != null)
			{
				sb.Append("\n").Append(new DbSchemaXmlSerializer().Serialize(sqb.GetSchema()));
			}
			sb.Append(SetElemAttribute(0, "options", sqb.GetOptions()));
			sb.Append("\n</body>");
			return sb.ToString();
		}
	}
}
