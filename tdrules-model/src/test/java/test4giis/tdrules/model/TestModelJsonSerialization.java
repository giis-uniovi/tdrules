package test4giis.tdrules.model;

import org.junit.Test;

import giis.tdrules.model.io.ModelJsonSerializer;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.SqlRules;

public class TestModelJsonSerialization extends Base {

	@Test
	public void testSchemaSerializeJson() {
		DbSchema dbSchema = TestSchemaModel.getSchema();
		String json = new ModelJsonSerializer().serialize(dbSchema, true);
		writeFile("serialize-schema.json", json);
		String expectedJson = readFile("serialize-schema.json").trim();
		va.assertEquals(expectedJson.replace("\r", ""), json.replace("\r", ""));

		// check that serialization is reversible
		dbSchema = (DbSchema) new ModelJsonSerializer().deserialize(json, DbSchema.class);
		String json2 = new ModelJsonSerializer().serialize(dbSchema, true);
		va.assertEquals(json, json2);
	}

	@Test
	public void testRulesSerializeJson() {
		SqlRules rules = TestRulesModel.getRules();
		String json = new ModelJsonSerializer().serialize(rules, true);
		writeFile("serialize-sqlfpc.json", json);
		String expectedJson = readFile("serialize-sqlfpc.json").trim();
		va.assertEquals(expectedJson.replace("\r", ""), json.replace("\r", ""));

		// check that serialization is reversible
		rules = (SqlRules) new ModelJsonSerializer().deserialize(json, SqlRules.class);
		String json2 = new ModelJsonSerializer().serialize(rules, true);
		va.assertEquals(json, json2);
	}

}
