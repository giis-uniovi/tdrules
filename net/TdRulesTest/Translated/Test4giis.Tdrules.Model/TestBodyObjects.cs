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
			SqlTableListBody tables = new SqlTableListBody();
			tables.AddTablesItem("tab1");
			tables.AddTablesItem("tab2");
			string xml = new SqlRulesXmlSerializer().Serialize(tables);
			string expected = ReadFile("serialize-tables-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			SqlTableListBody tables2 = new SqlRulesXmlSerializer().DeserializeTables(xml);
			string xml2 = new SqlRulesXmlSerializer().Serialize(tables2);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestParametersBody()
		{
			SqlParam param0 = new SqlParam();
			param0.SetName("arg0");
			param0.SetValue("val0");
			SqlParam param1 = new SqlParam();
			param1.SetName("arg1");
			param1.SetValue("val1");
			SqlParametersBody tables = new SqlParametersBody();
			tables.AddParametersItem(param0);
			tables.AddParametersItem(param1);
			string xml = new SqlRulesXmlSerializer().Serialize(tables);
			string expected = ReadFile("serialize-parameters-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			SqlParametersBody tables2 = new SqlRulesXmlSerializer().DeserializeParameters(xml);
			string xml2 = new SqlRulesXmlSerializer().Serialize(tables2);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestSqlRulesBody()
		{
			DbColumn col = new DbColumn();
			col.SetName("col1");
			DbTable tab = new DbTable();
			tab.SetName("tab1");
			tab.AddColumnsItem(col);
			DbSchema schema = new DbSchema();
			schema.AddTablesItem(tab);
			SqlRulesBody tables = new SqlRulesBody();
			tables.SetSchema(schema);
			tables.SetSql("select 1");
			tables.SetOptions("opt0 opt1");
			string xml = new SqlRulesXmlSerializer().Serialize(tables);
			string expected = ReadFile("serialize-sqlrules-body.xml");
			va.AssertEquals(expected.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
		}
		// no implementada deserializacion
	}
}
