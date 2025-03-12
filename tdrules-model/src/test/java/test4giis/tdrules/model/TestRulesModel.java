package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import giis.tdrules.model.io.TdRulesXmlSerializer;
import giis.tdrules.model.shared.ModelUtil;
import giis.tdrules.model.shared.RuleTypes;
import giis.tdrules.openapi.model.TdRule;
import giis.tdrules.openapi.model.TdRules;

/**
 * Serialization and deserialization of rules
 */
public class TestRulesModel extends Base {

	// Similar approach than DbSchema
	
	public static TdRules getRules() {
		TdRules rules = new TdRules();
		rules.setRulesClass(RuleTypes.FPC);
		rules.setVersion("1.2.3");
		rules.setEnvironment("development");
		rules.setSummary(singletonMap("count", "2"));
		rules.setQuery("select * from t where a>'x'");
		rules.setParsedquery("SELECT * FROM t WHERE a > 'x'");
		// rules.setError("this is a rules error");

		TdRule rule = new TdRule();
		rule.setSummary(singletonMap("count", "2"));
		rule.setId("1");
		rule.setCategory("S");
		rule.setMaintype("T");
		rule.setSubtype("FF");
		rule.setLocation("1.w.1.[WHERE a > 'x']");
		rule.setEquivalent("true");
		rule.setQuery("SELECT * FROM t WHERE NOT(a > 'x')");
		rule.setDescription("-- Some row where condition is false");
		rule.setError("this is a rule error");
		rules.addRulesItem(rule);

		rules.addRulesItem(new TdRule());

		return rules;
	}

	@Test
	public void testRulesSerializeXml() throws IOException {
		TdRules rules = getRules();
		String xml = new TdRulesXmlSerializer().serialize(rules);
		writeFile("serialize-fpc.xml", xml);
		String expectedXml = readFile("serialize-fpc.xml").trim();
		va.assertEquals(expectedXml.replace("\r", ""), xml.replace("\r", ""));

		// check that serialization is reversible
		rules = new TdRulesXmlSerializer().deserialize(xml);
		String xml2 = new TdRulesXmlSerializer().serialize(rules);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testRulesAdditionalPropertiesSummary() {
		// propiedades adicionales que constituyen el resumen de ejeucion de reglas
		// se serializan en el tag del contenedor de la propiedad summary
		// se conserva el orden de insercion
		TdRules model = new TdRules();
		model.setRulesClass(RuleTypes.FPC);
		model.putSummaryItem("error", "0");
		model.putSummaryItem("count", "2");
		String xml = new TdRulesXmlSerializer().serialize(model);
		assertContains("<sqlfpc error=\"0\" count=\"2\">", xml);

		TdRules model2 = new TdRulesXmlSerializer().deserialize(xml);
		assertEquals("0", ModelUtil.safe(model2.getSummary(), "error"));
		assertEquals("2", ModelUtil.safe(model2.getSummary(), "count"));
		assertEquals("", ModelUtil.safe(model2.getSummary(), "dead"));

		// Vuelvo a primer modelo, un nuevo atributo que debe salir siempre antes que
		// los anteriores
		model.putSummaryItem("dead", "1");
		xml = new TdRulesXmlSerializer().serialize(model);
		assertContains("<sqlfpc error=\"0\" count=\"2\" dead=\"1\">", xml);
	}

	@Test
	public void testRuleAdditionalPropertiesSummary() {
		TdRules model = new TdRules();
		model.setRulesClass(RuleTypes.FPC);
		TdRule rule = new TdRule();
		rule.setId("1");
		rule.putSummaryItem("error", "0");
		rule.putSummaryItem("count", "2");
		model.addRulesItem(rule);
		String xml = new TdRulesXmlSerializer().serialize(model);
		assertContains("<fpcrule error=\"0\" count=\"2\">", xml);

		TdRules model2 = new TdRulesXmlSerializer().deserialize(xml);
		TdRule rule2 = model2.getRules().get(0);
		assertEquals("0", ModelUtil.safe(rule2.getSummary(), "error"));
		assertEquals("2", ModelUtil.safe(rule2.getSummary(), "count"));
		assertEquals("", ModelUtil.safe(rule2.getSummary(), "dead"));
	}

}
