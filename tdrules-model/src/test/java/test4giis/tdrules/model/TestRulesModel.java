package test4giis.tdrules.model;

import static giis.tdrules.model.ModelUtil.safe;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.model.io.SqlRulesXmlSerializer;
import giis.tdrules.openapi.model.SqlRule;
import giis.tdrules.openapi.model.SqlRules;

/**
 * Serialization and deserialization of rules
 */
public class TestRulesModel extends Base {

	// Similar approach than DbSchema
	
	public static SqlRules getRules() {
		SqlRules rules = new SqlRules();
		rules.setRulesClass("sqlfpc");
		rules.setVersion("1.2.3");
		rules.setEnvironment("development");
		rules.setSummary(singletonMap("count", "2"));
		rules.setSql("select * from t where a>'x'");
		rules.setParsedsql("SELECT * FROM t WHERE a > 'x'");
		// rules.setError("this is a rules error");

		SqlRule rule = new SqlRule();
		rule.setSummary(singletonMap("count", "2"));
		rule.setId("1");
		rule.setCategory("S");
		rule.setMaintype("T");
		rule.setSubtype("FF");
		rule.setLocation("1.w.1.[WHERE a > 'x']");
		rule.setEquivalent("true");
		rule.setSql("SELECT * FROM t WHERE NOT(a > 'x')");
		rule.setDescription("-- Some row where condition is false");
		rule.setError("this is a rule error");
		rules.addRulesItem(rule);

		rules.addRulesItem(new SqlRule());

		return rules;
	}

	@Test
	public void testRulesSerializeXml() throws IOException {
		SqlRules rules = getRules();
		String xml = new SqlRulesXmlSerializer().serialize(rules);
		writeFile("serialize-sqlfpc.xml", xml);
		String expectedXml = readFile("serialize-sqlfpc.xml").trim();
		va.assertEquals(expectedXml.replace("\r", ""), xml.replace("\r", ""));

		// check that serialization is reversible
		rules = new SqlRulesXmlSerializer().deserialize(xml);
		String xml2 = new SqlRulesXmlSerializer().serialize(rules);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testRulesAdditionalPropertiesSummary() {
		// propiedades adicionales que constituyen el resumen de ejeucion de reglas
		// se serializan en el tag del contenedor de la propiedad summary
		// se conserva el orden de insercion
		SqlRules model = new SqlRules();
		model.setRulesClass("sqlfpc");
		model.putSummaryItem("error", "0");
		model.putSummaryItem("count", "2");
		String xml = new SqlRulesXmlSerializer().serialize(model);
		assertContains("<sqlfpc error=\"0\" count=\"2\">", xml);

		SqlRules model2 = new SqlRulesXmlSerializer().deserialize(xml);
		assertEquals("0", safe(model2.getSummary(), "error"));
		assertEquals("2", safe(model2.getSummary(), "count"));
		assertEquals("", safe(model2.getSummary(), "dead"));

		// Vuelvo a primer modelo, un nuevo atributo que debe salir siempre antes que
		// los anteriores
		model.putSummaryItem("dead", "1");
		xml = new SqlRulesXmlSerializer().serialize(model);
		assertContains("<sqlfpc error=\"0\" count=\"2\" dead=\"1\">", new SqlRulesXmlSerializer().serialize(model));
	}

	@Test
	public void testRuleAdditionalPropertiesSummary() {
		SqlRules model = new SqlRules();
		model.setRulesClass("sqlfpc");
		SqlRule rule = new SqlRule();
		rule.setId("1");
		rule.putSummaryItem("error", "0");
		rule.putSummaryItem("count", "2");
		model.addRulesItem(rule);
		String xml = new SqlRulesXmlSerializer().serialize(model);
		assertContains("<fpcrule error=\"0\" count=\"2\">", xml);

		SqlRules model2 = new SqlRulesXmlSerializer().deserialize(xml);
		SqlRule rule2 = model2.getRules().get(0);
		assertEquals("0", safe(rule2.getSummary(), "error"));
		assertEquals("2", safe(rule2.getSummary(), "count"));
		assertEquals("", safe(rule2.getSummary(), "dead"));
	}

}
