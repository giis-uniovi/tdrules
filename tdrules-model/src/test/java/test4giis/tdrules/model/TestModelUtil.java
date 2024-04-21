package test4giis.tdrules.model;

import static giis.tdrules.model.ModelUtil.safe;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdRules;

/**
 * Additional methods to safe access to generated model properties
 */
public class TestModelUtil {
	
	@Test
	public void testAllSafeMethods() {
		assertEquals(0, safe((List<String>)null).size());
		assertEquals(0, safe((Map<String, String>)null).size());
		assertEquals("", safe((Map<String, String>)null, "unknownkey"));
	}

	//Test model properties with potential null values
	
	@Test
	public void testFieldAccess() {
		TdRules model = new TdRules();
		// los campos texto tienen valor por defecto de string vacio, no hay problemas con nulos
		assertEquals("", model.getQuery());
		model.setQuery("select 1");
		assertEquals("select 1", model.getQuery());
	}

	@Test
	public void testSafeArrayAccess() {
		TdSchema model = new TdSchema();
		// los arrays son nulos por defecto (no funciona la inicializacion en .net)
		// pero se puede acceder de forma segura con ModelUtil.safe
		// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
		// En 6.5.0 vuelve a devolver null, pero hay un parametro que se puede poner a true
		// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/java.md
		// containerDefaultToNull=false by default
		// En 7.5.0 vuelve a devolver array vacio que parece que es lo correcto
		// Se deve usar siempre el metodo safe para evitar problemas porque esto parece algo inestable
		assertEquals(0, safe(model.getEntities()).size());
		// Tambien se puede anyadir un elemento de forma segura (nativo en modelo java,
		// creado con un postprocesamiento en .net)
		model.addEntitiesItem(new TdEntity());
		assertEquals(1, safe(model.getEntities()).size());
		assertEquals(1, model.getEntities().size());
	}

	@Test
	public void testSafePutAndGetExistingItems() {
		TdRules model = new TdRules();
		// los campos de diccionario como summary tambien estan nulos por defecto
		// se puede anyadir sin problemas con el mentodo put* nativo (que se crea en
		// .net reprocesando los modelos)
		// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
		assertEquals(0, safe(model.getSummary()).size());
		model.putSummaryItem("key2", "value2");
		model.putSummaryItem("key1", "value1");
		assertEquals(2, safe(model.getSummary()).size());
		assertEquals("value1", safe(model.getSummary(), "key1"));
		assertEquals("value2", safe(model.getSummary(), "key2"));

		// los elementos con clave repetida sustituyen el valor
		model.putSummaryItem("key2", "value3");
		assertEquals(2, safe(model.getSummary()).size());
		assertEquals("value3", safe(model.getSummary(), "key2"));
	}

	@Test
	public void testSummaryGetNotExistingItems() {
		TdRules model = new TdRules();
		// lectura de un item no existente cuando no existe el summary
		// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
		// en 7.1.0 para puntonet vielve a devolver null
		assertEquals(0, safe(model.getSummary()).size());
		assertEquals("", safe(model.getSummary(), "key1"));

		// lectura de un item no existente cuando existe el summary
		model.putSummaryItem("key1", "value1");
		assertEquals("value1", safe(model.getSummary(), "key1"));
		assertEquals("", safe(model.getSummary(), "key2"));
	}

}
