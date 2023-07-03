/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Tdrules.Model.IO;
using Giis.Tdrules.Openapi.Model;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Model
{
	/// <summary>Serialization and deserialization of a DbSchema</summary>
	public class TestSchemaModel : Base
	{
		// For each object (tables, column, check, etc)
		// - value specified / not specified / non primitive object empty
		// - xml serialization / deserialization (check that it is reversible)
		// getters and setters are exercised during xml serialization
		// json serialization tested in separate class
		// Only for check schema sintax: schema must not be semantically correct
		// Base schema for test
		public static DbSchema GetSchema()
		{
			// not using fluent setters to keep C# compatibility
			// not needed a real schema, only to fill all properties
			DbTable tab1 = new DbTable();
			tab1.SetName("clirdb1");
			tab1.SetTabletype("table");
			DbColumn col1 = new DbColumn();
			col1.SetName("col11");
			col1.SetDatatype("int");
			col1.SetCompositetype("voidctype");
			col1.SetSubtype("voidstype");
			col1.SetSize("10");
			col1.SetKey("true");
			col1.SetAutoincrement("true");
			col1.SetNotnull("true");
			col1.SetFk("voidt.voidc");
			col1.SetFkname("voidfkname");
			col1.SetCheckin("1,2,3,4,5,6");
			col1.SetDefaultvalue("1");
			// extended are placed as additional attributes in column node
			col1.SetExtended(SingletonMap("ckey", "cvalue"));
			tab1.AddColumnsItem(col1);
			Ddl ddl = new Ddl();
			ddl.SetCommand("create");
			ddl.SetSql("create table clirdb1 (col11 int not null primary key, col12 varchar(16))");
			tab1.AddDdlsItem(ddl);
			DbCheck check = new DbCheck();
			check.SetColumn("col22");
			check.SetName("checkname");
			check.SetConstraint("([col22]>(0))");
			tab1.AddChecksItem(check);
			// extended are placed as attributes of the table node
			tab1.SetExtended(SingletonMap("tkey", "tvalue"));
			// empty or absent properties
			// column with empty properties
			// empty extended behave like non existing extended
			DbColumn col2 = new DbColumn();
			col2.SetName("col12");
			col2.SetExtended(new Dictionary<string, string>());
			tab1.AddColumnsItem(col2);
			// column without details
			DbColumn col3 = new DbColumn();
			col3.SetName("col13");
			tab1.AddColumnsItem(col3);
			// table with empty properties
			DbTable tab2 = new DbTable();
			tab2.SetName("tab2");
			tab2.AddColumnsItem(new DbColumn());
			tab2.AddDdlsItem(new Ddl());
			tab2.AddChecksItem(new DbCheck());
			// empty extended behave like non existing extended
			tab2.SetExtended(new Dictionary<string, string>());
			// table without properties
			DbTable tab3 = new DbTable();
			tab3.SetName("tab3");
			DbSchema schema = new DbSchema();
			schema.SetCatalog("database");
			schema.SetSchema("dbo");
			schema.SetDbms("sqlserver");
			schema.AddTablesItem(tab1);
			schema.AddTablesItem(tab2);
			schema.AddTablesItem(tab3);
			return schema;
		}

		[Test]
		public virtual void TestSchemaSerializeXml()
		{
			DbSchema dbSchema = GetSchema();
			string xml = new DbSchemaXmlSerializer().Serialize(dbSchema);
			WriteFile("serialize-schema.xml", xml);
			string expectedXml = ReadFile("serialize-schema.xml").Trim();
			va.AssertEquals(expectedXml.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			// check that serialization is reversible
			dbSchema = new DbSchemaXmlSerializer().Deserialize(xml);
			string xml2 = new DbSchemaXmlSerializer().Serialize(dbSchema);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestSummaryAdditionalPropertiesAndNative()
		{
			// Cuando hay mezcla de propiedades nativas y adicionales,
			// la deserializacion a xml solo anyade las adicionales en el campo correspondiente
			// Esto ocurre en el esquema (en rules todos los atributos estan bajo elementos)
			DbSchema model = new DbSchema();
			DbTable table = new DbTable();
			table.SetName("tname");
			table.SetTabletype("ttype");
			table.PutExtendedItem("enname", "tenname");
			table.PutExtendedItem("esname", "tesname");
			model.AddTablesItem(table);
			string xml = new DbSchemaXmlSerializer().Serialize(model);
			AssertContains("<table name=\"tname\" type=\"ttype\" enname=\"tenname\" esname=\"tesname\">", xml);
			// Deserializa para comprobar que se tienen los mismos atributos
			// (y los nativos no estan en los adicionales)
			DbSchema model2 = new DbSchemaXmlSerializer().Deserialize(xml);
			NUnit.Framework.Assert.AreEqual("tname", model2.GetTables()[0].GetName());
			NUnit.Framework.Assert.AreEqual("ttype", model2.GetTables()[0].GetTabletype());
			IDictionary<string, string> extended = model2.GetTables()[0].GetExtended();
			NUnit.Framework.Assert.IsNotNull(extended);
			NUnit.Framework.Assert.AreEqual(2, extended.Count);
			// no se ha mezclado con los nativos
			NUnit.Framework.Assert.AreEqual("tenname", extended["enname"]);
			NUnit.Framework.Assert.AreEqual("tesname", extended["esname"]);
		}
	}
}
