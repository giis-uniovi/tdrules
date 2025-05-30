using Giis.Portable.Xml.Tiny;
using Giis.Tdrules.Model.Shared;
using Giis.Tdrules.Openapi.Model;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Model.IO
{
    /// <summary>
    /// Custom xml serialization/deserialization of a rules model
    /// 
    /// Model is v4, but xml still reads and writes using the old v3 api notation.
    /// </summary>
    public class TdRulesXmlSerializer : BaseXmlSerializer
    {
        private static readonly string XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        private static readonly string VERSION = "version";
        private static readonly string DEVELOPMENT = "development";
        private static readonly string PARSEDSQL = "parsedsql";
        private static readonly string ERROR = "error";
        public virtual TdRules Deserialize(string xml)
        {
            XNode xtdrules = new XNode(xml);
            TdRules tdrules = new TdRules();
            string rulesClass = RuleTypes.NormalizeV4(xtdrules.Name());
            tdrules.SetRulesClass(rulesClass);
            tdrules.SetVersion(GetElemAttribute(xtdrules, VERSION));
            if (xtdrules.GetChild(VERSION) != null)
                tdrules.SetEnvironment(xtdrules.GetChild(VERSION).GetChild(DEVELOPMENT) == null ? "" : DEVELOPMENT);
            foreach (string attr in GetExtendedAttributeNames(xtdrules, new string[] { }))
                tdrules.PutSummaryItem(attr, xtdrules.GetAttribute(attr));
            tdrules.SetQuery(GetElemAttribute(xtdrules, "sql"));
            tdrules.SetParsedquery(GetElemAttribute(xtdrules, PARSEDSQL));
            tdrules.SetError(GetElemAttribute(xtdrules, ERROR));
            string ruleTag = RulesClassToRuleTag(rulesClass);
            XNode xrules = xtdrules.GetChild(ruleTag + "s");
            if (xrules == null)
                return tdrules;
            foreach (XNode rnode in xrules.GetChildren(ruleTag))
            {
                TdRule rule = new TdRule();
                foreach (string attr in GetExtendedAttributeNames(rnode, new string[] { }))
                    rule.PutSummaryItem(attr, rnode.GetAttribute(attr));
                rule.SetId(GetElemAttribute(rnode, "id"));
                rule.SetCategory(GetElemAttribute(rnode, "category"));
                rule.SetMaintype(GetElemAttribute(rnode, "type"));
                rule.SetSubtype(GetElemAttribute(rnode, "subtype"));
                rule.SetLocation(GetElemAttribute(rnode, "location"));
                rule.SetEquivalent(rnode.GetChild("equivalent") == null ? "" : "true");
                rule.SetQuery(GetElemAttribute(rnode, "sql"));
                rule.SetDescription(GetElemAttribute(rnode, "description"));
                rule.SetError(GetElemAttribute(rnode, ERROR));
                tdrules.AddRulesItem(rule);
            }

            return tdrules;
        }

        private string RulesClassToRuleTag(string rulesClass)
        {
            return RuleTypes.FPC.Equals(RuleTypes.NormalizeV4(rulesClass)) ? "fpcrule" : "mutant";
        }

        public virtual string Serialize(TdRules sqr)
        {
            StringBuilder sb = new StringBuilder();
            string rulesClass = sqr.GetRulesClass();
            sb.Append(XML_HEADER).Append("\n<" + RuleTypes.NormalizeV3(rulesClass)).Append(SetExtendedAttributes(sqr.GetSummary())).Append(">");
            sb.Append("\n<version>").Append(sqr.GetVersion()).Append(DEVELOPMENT.Equals(sqr.GetEnvironment()) ? "<development/>" : "").Append("</version>");
            sb.Append(SetElemAttribute(0, "sql", sqr.GetQuery())).Append(SetElemAttribute(0, PARSEDSQL, sqr.GetParsedquery()));
            sb.Append(SetElemAttribute(0, ERROR, sqr.GetError()));
            string ruleTag = RulesClassToRuleTag(rulesClass);
            if ("".Equals(sqr.GetError()))
            {

                //no muestra tag si ha habido error generando
                sb.Append("\n<" + ruleTag + "s>");
                foreach (TdRule rule in ModelUtil.Safe(sqr.GetRules()))
                    sb.Append("\n").Append(Serialize(rule, ruleTag));
                sb.Append("\n</" + ruleTag + "s>");
            }

            sb.Append("\n</" + RuleTypes.NormalizeV3(rulesClass) + ">");
            return sb.ToString();
        }

        public virtual string Serialize(TdRule rule, string ruleTag)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("  <" + ruleTag).Append(SetExtendedAttributes(rule.GetSummary())).Append(">");
            sb.Append(SetElemAttribute("id", rule.GetId())).Append(SetElemAttribute("category", rule.GetCategory())).Append(SetElemAttribute("type", rule.GetMaintype())).Append(SetElemAttribute("subtype", rule.GetSubtype())).Append(SetElemAttribute("location", rule.GetLocation())).Append("true".Equals(rule.GetEquivalent()) ? "\n    <equivalent/>" : "").Append(SetElemAttribute(4, "sql", rule.GetQuery())).Append(SetElemAttribute(4, "description", rule.GetDescription())).Append(SetElemAttribute(4, ERROR, rule.GetError())).Append("\n  </" + ruleTag + ">");
            return sb.ToString();
        }

        //Deserializacion de otros objetos obtenidos de los servicios TdRules
        //Aunque en v2 se devuelve una estructura similar a las reglas que incluye version, entorno, etc.
        //para la v3 y 4 se simplificara, y solo se tiene el error (uno solo) o el contenido objeto a devolver
        public virtual QueryEntitiesBody DeserializeEntities(string xml)
        {
            XNode xentities = new XNode(xml);
            QueryEntitiesBody entities = new QueryEntitiesBody();
            entities.SetError(GetElemAttribute(xentities, ERROR));
            foreach (XNode xentity in xentities.GetChildren("table"))
                entities.AddEntitiesItem(XNode.DecodeText(xentity.InnerText()));
            return entities;
        }

        public virtual QueryParametersBody DeserializeParameters(string xml)
        {
            XNode xparams = new XNode(xml);
            QueryParametersBody sparams = new QueryParametersBody();
            sparams.SetError(GetElemAttribute(xparams, ERROR));
            XNode paramNode = xparams.GetChild("parameters");
            if (paramNode == null)
                return sparams;
            foreach (XNode xparam in paramNode.GetChildren("parameter"))
            {
                QueryParam param = new QueryParam();
                param.SetName(xparam.GetAttribute("name"));
                param.SetValue(xparam.GetAttribute("value"));
                sparams.AddParametersItem(param);
            }

            return sparams;
        }

        public virtual string Serialize(QueryEntitiesBody model)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(XML_HEADER).Append("\n<sqltables>");
            if ("".Equals(model.GetError()))
                foreach (string entity in ModelUtil.Safe(model.GetEntities()))
                    sb.Append(SetElemAttribute(0, "table", entity));
            else
                sb.Append(SetElemAttribute(0, ERROR, model.GetError()));
            sb.Append("\n</sqltables>");
            return sb.ToString();
        }

        public virtual string Serialize(QueryParametersBody model)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(XML_HEADER).Append("\n<sqlparameters>");
            if ("".Equals(model.GetError()))
            {
                sb.Append("\n<parameters>");
                foreach (QueryParam param in ModelUtil.Safe(model.GetParameters()))
                    sb.Append("\n<parameter").Append(SetAttribute("name", param.GetName())).Append(SetAttribute("value", param.GetValue())).Append(" />");
                sb.Append("\n</parameters>").Append(SetElemAttribute(0, PARSEDSQL, model.GetParsedquery()));
            }
            else
            {
                sb.Append(SetElemAttribute(0, ERROR, model.GetError()));
            }

            sb.Append("\n</sqlparameters>");
            return sb.ToString();
        }

        public virtual string Serialize(TdRulesBody sqb)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("<body>");
            sb.Append(SetElemAttribute(0, "sql", sqb.GetQuery()));
            if (sqb.GetSchema() != null)
                sb.Append("\n").Append(new TdSchemaXmlSerializer().Serialize(sqb.GetSchema()));
            sb.Append(SetElemAttribute(0, "options", sqb.GetOptions()));
            sb.Append("\n</body>");
            return sb.ToString();
        }
    }
}