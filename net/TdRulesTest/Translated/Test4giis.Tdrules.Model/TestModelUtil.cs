/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Tdrules.Model;
using Giis.Tdrules.Openapi.Model;
using NUnit.Framework;
using Sharpen;

namespace Test4giis.Tdrules.Model
{
	/// <summary>Additional methods to safe access to generated model properties</summary>
	public class TestModelUtil
	{
		[Test]
		public virtual void TestAllSafeMethods()
		{
			NUnit.Framework.Assert.AreEqual(0, ModelUtil.Safe((IList<string>)null).Count);
			NUnit.Framework.Assert.AreEqual(0, ModelUtil.Safe((IDictionary<string, string>)null).Count);
			NUnit.Framework.Assert.AreEqual(string.Empty, ModelUtil.Safe((IDictionary<string, string>)null, "unknownkey"));
		}

		//Test model properties with potential null values
		[Test]
		public virtual void TestFieldAccess()
		{
			SqlRules model = new SqlRules();
			// los campos texto tienen valor por defecto de string vacio, no hay problemas con nulos
			NUnit.Framework.Assert.AreEqual(string.Empty, model.GetSql());
			model.SetSql("select 1");
			NUnit.Framework.Assert.AreEqual("select 1", model.GetSql());
		}

		[Test]
		public virtual void TestSafeArrayAccess()
		{
			DbSchema model = new DbSchema();
			// los arrays son nulos por defecto (no funciona la inicializacion en .net)
			// pero se puede acceder de forma segura con ModelUtil.safe
			// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
			// En 6.5.0 vuelve a devolver null, pero hay un parametro que se puede poner a true
			// (creo que es containerDefaultToNull, pero mantengo el comportamiento por defecto)
			NUnit.Framework.Assert.IsNull(model.GetTables());
			NUnit.Framework.Assert.AreEqual(0, ModelUtil.Safe(model.GetTables()).Count);
			// Tambien se puede anyadir un elemento de forma segura (nativo en modelo java,
			// creado con un postprocesamiento en .net)
			model.AddTablesItem(new DbTable());
			NUnit.Framework.Assert.AreEqual(1, ModelUtil.Safe(model.GetTables()).Count);
			NUnit.Framework.Assert.AreEqual(1, model.GetTables().Count);
		}

		[Test]
		public virtual void TestSafePutAndGetExistingItems()
		{
			SqlRules model = new SqlRules();
			// los campos de diccionario como summary tambien estan nulos por defecto
			// se puede anyadir sin problemas con el mentodo put* nativo (que se crea en
			// .net reprocesando los modelos)
			// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
			NUnit.Framework.Assert.IsNull(model.GetSummary()); //desde 6.3.0 en net sigue siendo null
			model.PutSummaryItem("key2", "value2");
			model.PutSummaryItem("key1", "value1");
			NUnit.Framework.Assert.AreEqual(2, model.GetSummary().Count);
			NUnit.Framework.Assert.AreEqual("value1", ModelUtil.Safe(model.GetSummary(), "key1"));
			NUnit.Framework.Assert.AreEqual("value2", ModelUtil.Safe(model.GetSummary(), "key2"));
			// los elementos con clave repetida sustituyen el valor
			model.PutSummaryItem("key2", "value3");
			NUnit.Framework.Assert.AreEqual(2, model.GetSummary().Count);
			NUnit.Framework.Assert.AreEqual("value3", ModelUtil.Safe(model.GetSummary(), "key2"));
		}

		[Test]
		public virtual void TestSummaryGetNotExistingItems()
		{
			SqlRules model = new SqlRules();
			// lectura de un item no existente cuando no existe el summary
			// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
			NUnit.Framework.Assert.IsNull(model.GetSummary()); //desde 6.3.0 en net sigue siendo null
			NUnit.Framework.Assert.AreEqual(string.Empty, ModelUtil.Safe(model.GetSummary(), "key1"));
			// lectura de un item no existente cuando existe el summary
			model.PutSummaryItem("key1", "value1");
			NUnit.Framework.Assert.AreEqual("value1", ModelUtil.Safe(model.GetSummary(), "key1"));
			NUnit.Framework.Assert.AreEqual(string.Empty, ModelUtil.Safe(model.GetSummary(), "key2"));
		}
	}
}
