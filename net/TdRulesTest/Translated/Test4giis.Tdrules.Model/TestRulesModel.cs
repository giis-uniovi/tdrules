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
		public static SqlRules GetRules()
		{
			SqlRules rules = new SqlRules();
			rules.SetRulesClass("sqlfpc");
			rules.SetVersion("1.2.3");
			rules.SetEnvironment("development");
			rules.SetSummary(SingletonMap("count", "2"));
			rules.SetSql("select * from t where a>'x'");
			rules.SetParsedsql("SELECT * FROM t WHERE a > 'x'");
			// rules.setError("this is a rules error");
			SqlRule rule = new SqlRule();
			rule.SetSummary(SingletonMap("count", "2"));
			rule.SetId("1");
			rule.SetCategory("S");
			rule.SetMaintype("T");
			rule.SetSubtype("FF");
			rule.SetLocation("1.w.1.[WHERE a > 'x']");
			rule.SetEquivalent("true");
			rule.SetSql("SELECT * FROM t WHERE NOT(a > 'x')");
			rule.SetDescription("-- Some row where condition is false");
			rule.SetError("this is a rule error");
			rules.AddRulesItem(rule);
			rules.AddRulesItem(new SqlRule());
			return rules;
		}

		/// <exception cref="System.IO.IOException"/>
		[Test]
		public virtual void TestRulesSerializeXml()
		{
			SqlRules rules = GetRules();
			string xml = new SqlRulesXmlSerializer().Serialize(rules);
			WriteFile("serialize-sqlfpc.xml", xml);
			string expectedXml = ReadFile("serialize-sqlfpc.xml").Trim();
			va.AssertEquals(expectedXml.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			// check that serialization is reversible
			rules = new SqlRulesXmlSerializer().Deserialize(xml);
			string xml2 = new SqlRulesXmlSerializer().Serialize(rules);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestRulesAdditionalPropertiesSummary()
		{
			// propiedades adicionales que constituyen el resumen de ejeucion de reglas
			// se serializan en el tag del contenedor de la propiedad summary
			// se conserva el orden de insercion
			SqlRules model = new SqlRules();
			model.SetRulesClass("sqlfpc");
			model.PutSummaryItem("error", "0");
			model.PutSummaryItem("count", "2");
			string xml = new SqlRulesXmlSerializer().Serialize(model);
			AssertContains("<sqlfpc error=\"0\" count=\"2\">", xml);
			SqlRules model2 = new SqlRulesXmlSerializer().Deserialize(xml);
			NUnit.Framework.Assert.AreEqual("0", ModelUtil.Safe(model2.GetSummary(), "error"));
			NUnit.Framework.Assert.AreEqual("2", ModelUtil.Safe(model2.GetSummary(), "count"));
			NUnit.Framework.Assert.AreEqual(string.Empty, ModelUtil.Safe(model2.GetSummary(), "dead"));
			// Vuelvo a primer modelo, un nuevo atributo que debe salir siempre antes que
			// los anteriores
			model.PutSummaryItem("dead", "1");
			xml = new SqlRulesXmlSerializer().Serialize(model);
			AssertContains("<sqlfpc error=\"0\" count=\"2\" dead=\"1\">", new SqlRulesXmlSerializer().Serialize(model));
		}

		[Test]
		public virtual void TestRuleAdditionalPropertiesSummary()
		{
			SqlRules model = new SqlRules();
			model.SetRulesClass("sqlfpc");
			SqlRule rule = new SqlRule();
			rule.SetId("1");
			rule.PutSummaryItem("error", "0");
			rule.PutSummaryItem("count", "2");
			model.AddRulesItem(rule);
			string xml = new SqlRulesXmlSerializer().Serialize(model);
			AssertContains("<fpcrule error=\"0\" count=\"2\">", xml);
			SqlRules model2 = new SqlRulesXmlSerializer().Deserialize(xml);
			SqlRule rule2 = model2.GetRules()[0];
			NUnit.Framework.Assert.AreEqual("0", ModelUtil.Safe(rule2.GetSummary(), "error"));
			NUnit.Framework.Assert.AreEqual("2", ModelUtil.Safe(rule2.GetSummary(), "count"));
			NUnit.Framework.Assert.AreEqual(string.Empty, ModelUtil.Safe(rule2.GetSummary(), "dead"));
		}
	}
}
