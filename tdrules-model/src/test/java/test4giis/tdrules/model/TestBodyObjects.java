package test4giis.tdrules.model;

import org.junit.Test;

import giis.tdrules.model.io.TdRulesXmlSerializer;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.QueryEntitiesBody;
import giis.tdrules.openapi.model.QueryParam;
import giis.tdrules.openapi.model.QueryParametersBody;
import giis.tdrules.openapi.model.TdRulesBody;

/**
 * Additional objects in request body
 */
public class TestBodyObjects extends Base {

	@Test
	public void testTableListBody() {
		// not using fluent setters to keep C# compatibility
		QueryEntitiesBody tables = new QueryEntitiesBody();
		tables.addEntitiesItem("tab1");
		tables.addEntitiesItem("tab2");
		String xml = new TdRulesXmlSerializer().serialize(tables);
		String expected = readFile("serialize-entities-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		QueryEntitiesBody tables2 = new TdRulesXmlSerializer().deserializeEntities(xml);
		String xml2 = new TdRulesXmlSerializer().serialize(tables2);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testParametersBody() {
		QueryParam param0 = new QueryParam();
		param0.setName("arg0");
		param0.setValue("val0");
		QueryParam param1 = new QueryParam();
		param1.setName("arg1");
		param1.setValue("val1");
		QueryParametersBody tables = new QueryParametersBody();
		tables.addParametersItem(param0);
		tables.addParametersItem(param1);
		String xml = new TdRulesXmlSerializer().serialize(tables);
		String expected = readFile("serialize-parameters-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		QueryParametersBody tables2 = new TdRulesXmlSerializer().deserializeParameters(xml);
		String xml2 = new TdRulesXmlSerializer().serialize(tables2);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testSqlRulesBody() {
		TdAttribute col = new TdAttribute();
		col.setName("col1");
		TdEntity tab = new TdEntity();
		tab.setName("tab1");
		tab.addAttributesItem(col);
		TdSchema schema = new TdSchema();
		schema.addEntitiesItem(tab);
		TdRulesBody tables = new TdRulesBody();
		tables.setSchema(schema);
		tables.setQuery("select 1");
		tables.setOptions("opt0 opt1");
		String xml = new TdRulesXmlSerializer().serialize(tables);
		String expected = readFile("serialize-rules-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));
		// no implementada deserializacion
	}

}
