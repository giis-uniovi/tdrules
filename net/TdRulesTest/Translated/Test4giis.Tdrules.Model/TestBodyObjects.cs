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
		public virtual void TestQueryEntityListBody()
		{
			// not using fluent setters to keep C# compatibility
			QueryEntitiesBody entities = new QueryEntitiesBody();
			entities.AddEntitiesItem("tab1");
			entities.AddEntitiesItem("tab2");
			string xml = new TdRulesXmlSerializer().Serialize(entities);
			string expected = ReadFile("serialize-entities-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			QueryEntitiesBody entities2 = new TdRulesXmlSerializer().DeserializeEntities(xml);
			string xml2 = new TdRulesXmlSerializer().Serialize(entities2);
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
			QueryParametersBody entities = new QueryParametersBody();
			entities.AddParametersItem(param0);
			entities.AddParametersItem(param1);
			string xml = new TdRulesXmlSerializer().Serialize(entities);
			string expected = ReadFile("serialize-parameters-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			QueryParametersBody entities2 = new TdRulesXmlSerializer().DeserializeParameters(xml);
			string xml2 = new TdRulesXmlSerializer().Serialize(entities2);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestFpcRulesBody()
		{
			TdAttribute attr = new TdAttribute();
			attr.SetName("col1");
			TdEntity entity = new TdEntity();
			entity.SetName("tab1");
			entity.AddAttributesItem(attr);
			TdSchema schema = new TdSchema();
			schema.AddEntitiesItem(entity);
			TdRulesBody entities = new TdRulesBody();
			entities.SetSchema(schema);
			entities.SetQuery("select 1");
			entities.SetOptions("opt0 opt1");
			string xml = new TdRulesXmlSerializer().Serialize(entities);
			string expected = ReadFile("serialize-rules-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
		}
		// deserialization not implemented
	}
}
