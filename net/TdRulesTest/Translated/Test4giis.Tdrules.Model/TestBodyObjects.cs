/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Giis.Tdrules.Model.IO;
using Giis.Tdrules.Openapi.Model;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Model
{
	/// <summary>Additional objects in request body</summary>
	public class TestBodyObjects : Base
	{
		[Test]
		public virtual void TestTableListBody()
		{
			// not using fluent setters to keep C# compatibility
			QueryEntitiesBody tables = new QueryEntitiesBody();
			tables.AddEntitiesItem("tab1");
			tables.AddEntitiesItem("tab2");
			string xml = new TdRulesXmlSerializer().Serialize(tables);
			string expected = ReadFile("serialize-entities-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			QueryEntitiesBody tables2 = new TdRulesXmlSerializer().DeserializeEntities(xml);
			string xml2 = new TdRulesXmlSerializer().Serialize(tables2);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestParametersBody()
		{
			QueryParam param0 = new QueryParam();
			param0.SetName("arg0");
			param0.SetValue("val0");
			QueryParam param1 = new QueryParam();
			param1.SetName("arg1");
			param1.SetValue("val1");
			QueryParametersBody tables = new QueryParametersBody();
			tables.AddParametersItem(param0);
			tables.AddParametersItem(param1);
			string xml = new TdRulesXmlSerializer().Serialize(tables);
			string expected = ReadFile("serialize-parameters-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			QueryParametersBody tables2 = new TdRulesXmlSerializer().DeserializeParameters(xml);
			string xml2 = new TdRulesXmlSerializer().Serialize(tables2);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestSqlRulesBody()
		{
			TdAttribute col = new TdAttribute();
			col.SetName("col1");
			TdEntity tab = new TdEntity();
			tab.SetName("tab1");
			tab.AddAttributesItem(col);
			TdSchema schema = new TdSchema();
			schema.AddEntitiesItem(tab);
			TdRulesBody tables = new TdRulesBody();
			tables.SetSchema(schema);
			tables.SetQuery("select 1");
			tables.SetOptions("opt0 opt1");
			string xml = new TdRulesXmlSerializer().Serialize(tables);
			string expected = ReadFile("serialize-rules-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
		}
		// no implementada deserializacion
	}
}
