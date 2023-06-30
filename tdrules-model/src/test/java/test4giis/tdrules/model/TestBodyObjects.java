package test4giis.tdrules.model;

import org.junit.Test;

import giis.tdrules.model.io.SqlRulesXmlSerializer;
import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;
import giis.tdrules.openapi.model.SqlParam;
import giis.tdrules.openapi.model.SqlParametersBody;
import giis.tdrules.openapi.model.SqlRulesBody;
import giis.tdrules.openapi.model.SqlTableListBody;

/**
 * Additional objects in request body
 */
public class TestBodyObjects extends Base {

	@Test
	public void testTableListBody() {
		// not using fluent setters to keep C# compatibility
		SqlTableListBody tables = new SqlTableListBody();
		tables.addTablesItem("tab1");
		tables.addTablesItem("tab2");
		String xml = new SqlRulesXmlSerializer().serialize(tables);
		String expected = readFile("serialize-tables-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		SqlTableListBody tables2 = new SqlRulesXmlSerializer().deserializeTables(xml);
		String xml2 = new SqlRulesXmlSerializer().serialize(tables2);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testParametersBody() {
		SqlParam param0 = new SqlParam();
		param0.setName("arg0");
		param0.setValue("val0");
		SqlParam param1 = new SqlParam();
		param1.setName("arg1");
		param1.setValue("val1");
		SqlParametersBody tables = new SqlParametersBody();
		tables.addParametersItem(param0);
		tables.addParametersItem(param1);
		String xml = new SqlRulesXmlSerializer().serialize(tables);
		String expected = readFile("serialize-parameters-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		SqlParametersBody tables2 = new SqlRulesXmlSerializer().deserializeParameters(xml);
		String xml2 = new SqlRulesXmlSerializer().serialize(tables2);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testSqlRulesBody() {
		DbColumn col = new DbColumn();
		col.setName("col1");
		DbTable tab = new DbTable();
		tab.setName("tab1");
		tab.addColumnsItem(col);
		DbSchema schema = new DbSchema();
		schema.addTablesItem(tab);
		SqlRulesBody tables = new SqlRulesBody();
		tables.setSchema(schema);
		tables.setSql("select 1");
		tables.setOptions("opt0 opt1");
		String xml = new SqlRulesXmlSerializer().serialize(tables);
		String expected = readFile("serialize-sqlrules-body.xml");
		va.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));
		// no implementada deserializacion
	}

}
