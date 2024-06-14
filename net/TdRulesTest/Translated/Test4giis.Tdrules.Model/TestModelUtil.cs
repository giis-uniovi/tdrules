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
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, ModelUtil.Safe((IList<string>)null).Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, ModelUtil.Safe((IDictionary<string, string>)null).Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, ModelUtil.Safe((IDictionary<string, string>)null, "unknownkey"));
		}

		//Test model properties with potential null values
		[Test]
		public virtual void TestFieldAccess()
		{
			TdRules model = new TdRules();
			// los campos texto tienen valor por defecto de string vacio, no hay problemas con nulos
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, model.GetQuery());
			model.SetQuery("select 1");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("select 1", model.GetQuery());
		}

		[Test]
		public virtual void TestSafeArrayAccess()
		{
			TdSchema model = new TdSchema();
			// los arrays son nulos por defecto (no funciona la inicializacion en .net)
			// pero se puede acceder de forma segura con ModelUtil.safe
			// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
			// En 6.5.0 vuelve a devolver null, pero hay un parametro que se puede poner a true
			// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/java.md
			// containerDefaultToNull=false by default
			// En 7.5.0 vuelve a devolver array vacio que parece que es lo correcto
			// Se debe usar siempre el metodo safe para evitar problemas porque esto parece algo inestable
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, ModelUtil.Safe(model.GetEntities()).Count);
			// Tambien se puede anyadir un elemento de forma segura (nativo en modelo java,
			// creado con un postprocesamiento en .net)
			model.AddEntitiesItem(new TdEntity());
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(1, ModelUtil.Safe(model.GetEntities()).Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(1, model.GetEntities().Count);
		}

		[Test]
		public virtual void TestSafePutAndGetExistingItems()
		{
			TdRules model = new TdRules();
			// los campos de diccionario como summary tambien estan nulos por defecto
			// se puede anyadir sin problemas con el mentodo put* nativo (que se crea en
			// .net reprocesando los modelos)
			// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, ModelUtil.Safe(model.GetSummary()).Count);
			model.PutSummaryItem("key2", "value2");
			model.PutSummaryItem("key1", "value1");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(2, ModelUtil.Safe(model.GetSummary()).Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("value1", ModelUtil.Safe(model.GetSummary(), "key1"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("value2", ModelUtil.Safe(model.GetSummary(), "key2"));
			// los elementos con clave repetida sustituyen el valor
			model.PutSummaryItem("key2", "value3");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(2, ModelUtil.Safe(model.GetSummary()).Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("value3", ModelUtil.Safe(model.GetSummary(), "key2"));
		}

		[Test]
		public virtual void TestSummaryGetNotExistingItems()
		{
			TdRules model = new TdRules();
			// lectura de un item no existente cuando no existe el summary
			// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
			// en 7.1.0 para puntonet vielve a devolver null
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(0, ModelUtil.Safe(model.GetSummary()).Count);
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, ModelUtil.Safe(model.GetSummary(), "key1"));
			// lectura de un item no existente cuando existe el summary
			model.PutSummaryItem("key1", "value1");
			NUnit.Framework.Legacy.ClassicAssert.AreEqual("value1", ModelUtil.Safe(model.GetSummary(), "key1"));
			NUnit.Framework.Legacy.ClassicAssert.AreEqual(string.Empty, ModelUtil.Safe(model.GetSummary(), "key2"));
		}
	}
}
