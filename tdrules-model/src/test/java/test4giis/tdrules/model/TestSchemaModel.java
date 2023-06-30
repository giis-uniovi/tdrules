package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import giis.tdrules.model.io.DbSchemaXmlSerializer;
import giis.tdrules.openapi.model.DbCheck;
import giis.tdrules.openapi.model.DbColumn;
import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;
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
	public static DbSchema getSchema() {
		// not using fluent setters to keep C# compatibility
		// not needed a real schema, only to fill all properties
		DbTable tab1 = new DbTable();
		tab1.setName("clirdb1");
		tab1.setTabletype("table");
		DbColumn col1 = new DbColumn();
		col1.setName("col11");
		col1.setDatatype("int");
		col1.setCompositetype("voidctype");
		col1.setSubtype("voidstype");
		col1.setSize("10");
		col1.setKey("true");
		col1.setAutoincrement("true");
		col1.setNotnull("true");
		col1.setFk("voidt.voidc");
		col1.setFkname("voidfkname");
		col1.setCheckin("1,2,3,4,5,6");
		col1.setDefaultvalue("1");
		// extended are placed as additional attributes in column node
		col1.setExtended(singletonMap("ckey", "cvalue"));
		tab1.addColumnsItem(col1);

		Ddl ddl = new Ddl();
		ddl.setCommand("create");
		ddl.setSql("create table clirdb1 (col11 int not null primary key, col12 varchar(16))");
		tab1.addDdlsItem(ddl);

		DbCheck check = new DbCheck();
		check.setColumn("col22");
		check.setName("checkname");
		check.setConstraint("([col22]>(0))");
		tab1.addChecksItem(check);

		// extended are placed as attributes of the table node
		tab1.setExtended(singletonMap("tkey", "tvalue"));

		// empty or absent properties

		// column with empty properties
		// empty extended behave like non existing extended
		DbColumn col2 = new DbColumn();
		col2.setName("col12");
		col2.setExtended(new HashMap<String, String>());
		tab1.addColumnsItem(col2);

		// column without details
		DbColumn col3 = new DbColumn();
		col3.setName("col13");
		tab1.addColumnsItem(col3);

		// table with empty properties
		DbTable tab2 = new DbTable();
		tab2.setName("tab2");
		tab2.addColumnsItem(new DbColumn());
		tab2.addDdlsItem(new Ddl());
		tab2.addChecksItem(new DbCheck());
		// empty extended behave like non existing extended
		tab2.setExtended(new HashMap<String, String>());

		// table without properties
		DbTable tab3 = new DbTable();
		tab3.setName("tab3");

		DbSchema schema = new DbSchema();
		schema.setCatalog("database");
		schema.setSchema("dbo");
		schema.setDbms("sqlserver");
		schema.addTablesItem(tab1);
		schema.addTablesItem(tab2);
		schema.addTablesItem(tab3);
		return schema;
	}

	@Test
	public void testSchemaSerializeXml() {
		DbSchema dbSchema = getSchema();
		String xml = new DbSchemaXmlSerializer().serialize(dbSchema);
		writeFile("serialize-schema.xml", xml);
		String expectedXml = readFile("serialize-schema.xml").trim();
		va.assertEquals(expectedXml.replace("\r", ""), xml.replace("\r", ""));

		// check that serialization is reversible
		dbSchema = new DbSchemaXmlSerializer().deserialize(xml);
		String xml2 = new DbSchemaXmlSerializer().serialize(dbSchema);
		va.assertEquals(xml, xml2);
	}

	@Test
	public void testSummaryAdditionalPropertiesAndNative() {
		// Cuando hay mezcla de propiedades nativas y adicionales,
		// la deserializacion a xml solo anyade las adicionales en el campo correspondiente
		// Esto ocurre en el esquema (en rules todos los atributos estan bajo elementos)
		DbSchema model = new DbSchema();
		DbTable table = new DbTable();
		table.setName("tname");
		table.setTabletype("ttype");
		table.putExtendedItem("enname", "tenname");
		table.putExtendedItem("esname", "tesname");
		model.addTablesItem(table);
		String xml = new DbSchemaXmlSerializer().serialize(model);
		assertContains("<table name=\"tname\" type=\"ttype\" enname=\"tenname\" esname=\"tesname\">", xml);

		// Deserializa para comprobar que se tienen los mismos atributos
		// (y los nativos no estan en los adicionales)
		DbSchema model2 = new DbSchemaXmlSerializer().deserialize(xml);
		assertEquals("tname", model2.getTables().get(0).getName());
		assertEquals("ttype", model2.getTables().get(0).getTabletype());
		Map<String, String> extended = model2.getTables().get(0).getExtended();
		assertNotNull(extended);
		assertEquals(2, extended.size()); // no se ha mezclado con los nativos
		assertEquals("tenname", extended.get("enname"));
		assertEquals("tesname", extended.get("esname"));
	}

}
