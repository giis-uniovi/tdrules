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
	public void testQueryEntityListBody() {
		// not using fluent setters to keep C# compatibility
		QueryEntitiesBody entities = new QueryEntitiesBody();
		entities.addEntitiesItem("tab1");
		entities.addEntitiesItem("tab2");
		String xml = new TdRulesXmlSerializer().serialize(entities);
		String expected = readFile("serialize-entities-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		QueryEntitiesBody entities2 = new TdRulesXmlSerializer().deserializeEntities(xml);
		String xml2 = new TdRulesXmlSerializer().serialize(entities2);
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
		QueryParametersBody entities = new QueryParametersBody();
		entities.addParametersItem(param0);
		entities.addParametersItem(param1);
		String xml = new TdRulesXmlSerializer().serialize(entities);
		String expected = readFile("serialize-parameters-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		QueryParametersBody entities2 = new TdRulesXmlSerializer().deserializeParameters(xml);
		String xml2 = new TdRulesXmlSerializer().serialize(entities2);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testFpcRulesBody() {
		TdAttribute attr = new TdAttribute();
		attr.setName("col1");
		TdEntity entity = new TdEntity();
		entity.setName("tab1");
		entity.addAttributesItem(attr);
		TdSchema schema = new TdSchema();
		schema.addEntitiesItem(entity);
		TdRulesBody entities = new TdRulesBody();
		entities.setSchema(schema);
		entities.setQuery("select 1");
		entities.setOptions("opt0 opt1");
		String xml = new TdRulesXmlSerializer().serialize(entities);
		String expected = readFile("serialize-rules-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));
		// deserialization not implemented
	}

}
