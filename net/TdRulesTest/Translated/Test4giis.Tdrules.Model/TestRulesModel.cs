/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Tdrules.Model;
using Giis.Tdrules.Model.IO;
using Giis.Tdrules.Openapi.Model;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Model
{
	/// <summary>Serialization and deserialization of rules</summary>
	public class TestRulesModel : Base
	{
		// Similar approach than DbSchema
		public static TdRules GetRules()
		{
			TdRules rules = new TdRules();
			rules.SetRulesClass(RuleTypes.Fpc);
			rules.SetVersion("1.2.3");
			rules.SetEnvironment("development");
			rules.SetSummary(SingletonMap("count", "2"));
			rules.SetQuery("select * from t where a>'x'");
			rules.SetParsedquery("SELECT * FROM t WHERE a > 'x'");
			// rules.setError("this is a rules error");
			TdRule rule = new TdRule();
			rule.SetSummary(SingletonMap("count", "2"));
			rule.SetId("1");
			rule.SetCategory("S");
			rule.SetMaintype("T");
			rule.SetSubtype("FF");
			rule.SetLocation("1.w.1.[WHERE a > 'x']");
			rule.SetEquivalent("true");
			rule.SetQuery("SELECT * FROM t WHERE NOT(a > 'x')");
			rule.SetDescription("-- Some row where condition is false");
			rule.SetError("this is a rule error");
			rules.AddRulesItem(rule);
			rules.AddRulesItem(new TdRule());
			return rules;
		}

		/// <exception cref="System.IO.IOException"/>
		[Test]
		public virtual void TestRulesSerializeXml()
		{
			TdRules rules = GetRules();
			string xml = new TdRulesXmlSerializer().Serialize(rules);
			WriteFile("serialize-fpc.xml", xml);
			string expectedXml = ReadFile("serialize-fpc.xml").Trim();
			va.AssertEquals(expectedXml.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			// check that serialization is reversible
			rules = new TdRulesXmlSerializer().Deserialize(xml);
			string xml2 = new TdRulesXmlSerializer().Serialize(rules);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestRulesAdditionalPropertiesSummary()
		{
			// propiedades adicionales que constituyen el resumen de ejeucion de reglas
			// se serializan en el tag del contenedor de la propiedad summary
			// se conserva el orden de insercion
			TdRules model = new TdRules();
			model.SetRulesClass(RuleTypes.Fpc);
			model.PutSummaryItem("error", "0");
			model.PutSummaryItem("count", "2");
			string xml = new TdRulesXmlSerializer().Serialize(model);
			AssertContains("<sqlfpc error=\"0\" count=\"2\">", xml);
			TdRules model2 = new TdRulesXmlSerializer().Deserialize(xml);
			NUnit.Framework.Assert.AreEqual("0", ModelUtil.Safe(model2.GetSummary(), "error"));
			NUnit.Framework.Assert.AreEqual("2", ModelUtil.Safe(model2.GetSummary(), "count"));
			NUnit.Framework.Assert.AreEqual(string.Empty, ModelUtil.Safe(model2.GetSummary(), "dead"));
			// Vuelvo a primer modelo, un nuevo atributo que debe salir siempre antes que
			// los anteriores
			model.PutSummaryItem("dead", "1");
			xml = new TdRulesXmlSerializer().Serialize(model);
			AssertContains("<sqlfpc error=\"0\" count=\"2\" dead=\"1\">", new TdRulesXmlSerializer().Serialize(model));
		}

		[Test]
		public virtual void TestRuleAdditionalPropertiesSummary()
		{
			TdRules model = new TdRules();
			model.SetRulesClass(RuleTypes.Fpc);
			TdRule rule = new TdRule();
			rule.SetId("1");
			rule.PutSummaryItem("error", "0");
			rule.PutSummaryItem("count", "2");
			model.AddRulesItem(rule);
			string xml = new TdRulesXmlSerializer().Serialize(model);
			AssertContains("<fpcrule error=\"0\" count=\"2\">", xml);
			TdRules model2 = new TdRulesXmlSerializer().Deserialize(xml);
			TdRule rule2 = model2.GetRules()[0];
			NUnit.Framework.Assert.AreEqual("0", ModelUtil.Safe(rule2.GetSummary(), "error"));
			NUnit.Framework.Assert.AreEqual("2", ModelUtil.Safe(rule2.GetSummary(), "count"));
			NUnit.Framework.Assert.AreEqual(string.Empty, ModelUtil.Safe(rule2.GetSummary(), "dead"));
		}
	}
}
