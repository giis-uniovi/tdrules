package test4giis.tdrules.model;

import org.junit.Test;

import giis.tdrules.model.io.ModelJsonSerializer;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdRules;

public class TestModelJsonSerialization extends Base {

	@Test
	public void testSchemaSerializeJson() {
		TdSchema dbSchema = TestSchemaModel.getSchema();
		String json = new ModelJsonSerializer().serialize(dbSchema, true);
		writeFile("serialize-schema.json", json);
		String expectedJson = readFile("serialize-schema.json").trim();
		va.assertEquals(expectedJson.replace("\r", ""), json.replace("\r", ""));

		// check that serialization is reversible
		dbSchema = (TdSchema) new ModelJsonSerializer().deserialize(json, TdSchema.class);
		String json2 = new ModelJsonSerializer().serialize(dbSchema, true);
		va.assertEquals(json, json2);
	}

	@Test
	public void testRulesSerializeJson() {
		TdRules rules = TestRulesModel.getRules(false);
		String json = new ModelJsonSerializer().serialize(rules, true);
		writeFile("serialize-fpc.json", json);
		String expectedJson = readFile("serialize-fpc.json").trim();
		va.assertEquals(expectedJson.replace("\r", ""), json.replace("\r", ""));

		// check that serialization is reversible
		rules = (TdRules) new ModelJsonSerializer().deserialize(json, TdRules.class);
		String json2 = new ModelJsonSerializer().serialize(rules, true);
		va.assertEquals(json, json2);
	}

	// same as before, but query and rules have parameters
	@Test
	public void testRulesSerializeJsonParam() {
		TdRules rules = TestRulesModel.getRules(true);
		String json = new ModelJsonSerializer().serialize(rules, true);
		writeFile("serialize-fpc-param.json", json);
		String expectedJson = readFile("serialize-fpc-param.json").trim();
		va.assertEquals(expectedJson.replace("\r", ""), json.replace("\r", ""));

		// check that serialization is reversible
		rules = (TdRules) new ModelJsonSerializer().deserialize(json, TdRules.class);
		String json2 = new ModelJsonSerializer().serialize(rules, true);
		va.assertEquals(json, json2);
	}

}
