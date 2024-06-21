/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Tdrules.Model;
using Giis.Tdrules.Model.IO;
using Giis.Tdrules.Openapi.Model;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Model
{
	/// <summary>Serialization and deserialization of a DbSchema</summary>
	public class TestSchemaModel : Base
	{
		// For each object (entity, attribute, check, etc)
		// - value specified / not specified / non primitive object empty
		// - xml serialization / deserialization (check that it is reversible)
		// getters and setters are exercised during xml serialization
		// json serialization tested in separate class
		// Only for check schema sintax: schema must not be semantically correct
		// Base schema for test
		public static TdSchema GetSchema()
		{
			// not using fluent setters to keep C# compatibility
			// not needed a real schema, only to fill all properties
			TdEntity tab1 = new TdEntity();
			tab1.SetName("clirdb1");
			tab1.SetEntitytype(EntityTypes.DtTable);
			tab1.SetSubtype("voidsubtype");
			TdAttribute col1 = new TdAttribute();
			col1.SetName("col11");
			col1.SetDatatype("int");
			col1.SetCompositetype("voidctype");
			col1.SetSubtype("voidstype");
			col1.SetSize("10");
			col1.SetUid("true");
			col1.SetAutoincrement("true");
			col1.SetNotnull("true");
			col1.SetReadonly("true");
			col1.SetRid("voidt.voidc");
			col1.SetRidname("voidfkname");
			col1.SetCheckin("1,2,3,4,5,6");
			col1.SetDefaultvalue("1");
			// extended are placed as additional attributes in attribute node
			col1.SetExtended(SingletonMap("ckey", "cvalue"));
			tab1.AddAttributesItem(col1);
			Ddl ddl = new Ddl();
			ddl.SetCommand("create");
			ddl.SetQuery("create table clirdb1 (col11 int not null primary key, col12 varchar(16))");
			tab1.AddDdlsItem(ddl);
			TdCheck check = new TdCheck();
			check.SetAttribute("col22");
			check.SetName("checkname");
			check.SetConstraint("([col22]>(0))");
			tab1.AddChecksItem(check);
			// extended are placed as attributes of the entity node
			tab1.SetExtended(SingletonMap("tkey", "tvalue"));
			// empty or absent properties
			// attribute with empty properties
			// empty extended behave like non existing extended
			TdAttribute col2 = new TdAttribute();
			col2.SetName("col12");
			col2.SetExtended(new Dictionary<string, string>());
			tab1.AddAttributesItem(col2);
			// attribute without details
			TdAttribute col3 = new TdAttribute();
			col3.SetName("col13");
			tab1.AddAttributesItem(col3);
			// entity with empty properties
			TdEntity tab2 = new TdEntity();
			tab2.SetName("tab2");
			tab2.AddAttributesItem(new TdAttribute());
			tab2.AddDdlsItem(new Ddl());
			tab2.AddChecksItem(new TdCheck());
			// empty extended behave like non existing extended
			tab2.SetExtended(new Dictionary<string, string>());
			// entity without properties
			TdEntity tab3 = new TdEntity();
			tab3.SetName("tab3");
			TdSchema schema = new TdSchema();
			schema.SetCatalog("database");
			schema.SetSchema("dbo");
			schema.SetStoretype("sqlserver");
			schema.AddEntitiesItem(tab1);
			schema.AddEntitiesItem(tab2);
			schema.AddEntitiesItem(tab3);
			return schema;
		}

		[Test]
		public virtual void TestSchemaSerializeXml()
		{
			TdSchema dbSchema = GetSchema();
			string xml = new TdSchemaXmlSerializer().Serialize(dbSchema);
			WriteFile("serialize-schema.xml", xml);
			string expectedXml = ReadFile("serialize-schema.xml").Trim();
			va.AssertEquals(expectedXml.Replace("\r", string.Empty), xml.Replace("\r", string.Empty));
			// check that serialization is reversible
			dbSchema = new TdSchemaXmlSerializer().Deserialize(xml);
			string xml2 = new TdSchemaXmlSerializer().Serialize(dbSchema);
			va.AssertEquals(xml, xml2);
		}

		[Test]
		public virtual void TestSummaryAdditionalPropertiesAndNative()
		{
			// Cuando hay mezcla de propiedades nativas y adicionales,
			// la deserializacion a xml solo anyade las adicionales en el campo correspondiente
			// Esto ocurre en el esquema (en rules todos los atributos estan bajo elementos)
			TdSchema model = new TdSchema();
			TdEntity entity = new TdEntity();
			entity.SetName("tname");
			entity.SetEntitytype("ttype");
			entity.PutExtendedItem("enname", "tenname");
			entity.PutExtendedItem("esname", "tesname");
			model.AddEntitiesItem(entity);
			string xml = new TdSchemaXmlSerializer().Serialize(model);
			AssertContains("<table name=\"tname\" type=\"ttype\" enname=\"tenname\" esname=\"tesname\">", xml);
			// Deserializa para comprobar que se tienen los mismos atributos
			// (y los nativos no estan en los adicionales)
			TdSchema model2 = new TdSchemaXmlSerializer().Deserialize(xml);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("tname", model2.GetEntities()[0].GetName());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("ttype", model2.GetEntities()[0].GetEntitytype());
			IDictionary<string, string> extended = model2.GetEntities()[0].GetExtended();
			NUnit.Framework.Legacy.ClassicAssert.IsNotNull(extended);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(2, extended.Count);
			// no se ha mezclado con los nativos
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("tenname", extended["enname"]);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("tesname", extended["esname"]);
		}
	}
}
