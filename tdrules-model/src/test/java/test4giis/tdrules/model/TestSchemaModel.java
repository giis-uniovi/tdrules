package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import giis.tdrules.model.io.TdSchemaXmlSerializer;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.Ddl;

/**
 * Serialization and deserialization of a DbSchema
 */
public class TestSchemaModel extends Base {

	// For each object (tables, column, check, etc)
	// - value specified / not specified / non primitive object empty
	// - xml serialization / deserialization (check that it is reversible)
	// getters and setters are exercised during xml serialization
	// json serialization tested in separate class
	// Only for check schema sintax: schema must not be semantically correct

	// Base schema for test
	public static TdSchema getSchema() {
		// not using fluent setters to keep C# compatibility
		// not needed a real schema, only to fill all properties
		TdEntity tab1 = new TdEntity();
		tab1.setName("clirdb1");
		tab1.setEntitytype("table");
		tab1.setSubtype("voidsubtype");
		TdAttribute col1 = new TdAttribute();
		col1.setName("col11");
		col1.setDatatype("int");
		col1.setCompositetype("voidctype");
		col1.setSubtype("voidstype");
		col1.setSize("10");
		col1.setUid("true");
		col1.setAutoincrement("true");
		col1.setNotnull("true");
		col1.setRid("voidt.voidc");
		col1.setRidname("voidfkname");
		col1.setCheckin("1,2,3,4,5,6");
		col1.setDefaultvalue("1");
		// extended are placed as additional attributes in column node
		col1.setExtended(singletonMap("ckey", "cvalue"));
		tab1.addAttributesItem(col1);

		Ddl ddl = new Ddl();
		ddl.setCommand("create");
		ddl.setQuery("create table clirdb1 (col11 int not null primary key, col12 varchar(16))");
		tab1.addDdlsItem(ddl);

		TdCheck check = new TdCheck();
		check.setAttribute("col22");
		check.setName("checkname");
		check.setConstraint("([col22]>(0))");
		tab1.addChecksItem(check);

		// extended are placed as attributes of the table node
		tab1.setExtended(singletonMap("tkey", "tvalue"));

		// empty or absent properties

		// column with empty properties
		// empty extended behave like non existing extended
		TdAttribute col2 = new TdAttribute();
		col2.setName("col12");
		col2.setExtended(new HashMap<String, String>());
		tab1.addAttributesItem(col2);

		// column without details
		TdAttribute col3 = new TdAttribute();
		col3.setName("col13");
		tab1.addAttributesItem(col3);

		// table with empty properties
		TdEntity tab2 = new TdEntity();
		tab2.setName("tab2");
		tab2.addAttributesItem(new TdAttribute());
		tab2.addDdlsItem(new Ddl());
		tab2.addChecksItem(new TdCheck());
		// empty extended behave like non existing extended
		tab2.setExtended(new HashMap<String, String>());

		// table without properties
		TdEntity tab3 = new TdEntity();
		tab3.setName("tab3");

		TdSchema schema = new TdSchema();
		schema.setCatalog("database");
		schema.setSchema("dbo");
		schema.setStoretype("sqlserver");
		schema.addEntitiesItem(tab1);
		schema.addEntitiesItem(tab2);
		schema.addEntitiesItem(tab3);
		return schema;
	}

	@Test
	public void testSchemaSerializeXml() {
		TdSchema dbSchema = getSchema();
		String xml = new TdSchemaXmlSerializer().serialize(dbSchema);
		writeFile("serialize-schema.xml", xml);
		String expectedXml = readFile("serialize-schema.xml").trim();
		va.assertEquals(expectedXml.replace("\r", ""), xml.replace("\r", ""));

		// check that serialization is reversible
		dbSchema = new TdSchemaXmlSerializer().deserialize(xml);
		String xml2 = new TdSchemaXmlSerializer().serialize(dbSchema);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testSummaryAdditionalPropertiesAndNative() {
		// Cuando hay mezcla de propiedades nativas y adicionales,
		// la deserializacion a xml solo anyade las adicionales en el campo correspondiente
		// Esto ocurre en el esquema (en rules todos los atributos estan bajo elementos)
		TdSchema model = new TdSchema();
		TdEntity table = new TdEntity();
		table.setName("tname");
		table.setEntitytype("ttype");
		table.putExtendedItem("enname", "tenname");
		table.putExtendedItem("esname", "tesname");
		model.addEntitiesItem(table);
		String xml = new TdSchemaXmlSerializer().serialize(model);
		assertContains("<table name=\"tname\" type=\"ttype\" enname=\"tenname\" esname=\"tesname\">", xml);

		// Deserializa para comprobar que se tienen los mismos atributos
		// (y los nativos no estan en los adicionales)
		TdSchema model2 = new TdSchemaXmlSerializer().deserialize(xml);
		assertEquals("tname", model2.getEntities().get(0).getName());
		assertEquals("ttype", model2.getEntities().get(0).getEntitytype());
		Map<String, String> extended = model2.getEntities().get(0).getExtended();
		assertNotNull(extended);
		assertEquals(2, extended.size()); // no se ha mezclado con los nativos
		assertEquals("tenname", extended.get("enname"));
		assertEquals("tesname", extended.get("esname"));
	}

}
