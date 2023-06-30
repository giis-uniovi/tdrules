package test4giis.tdrules.model;

import static giis.tdrules.model.ModelUtil.safe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import giis.tdrules.openapi.model.DbSchema;
import giis.tdrules.openapi.model.DbTable;
import giis.tdrules.openapi.model.SqlRules;

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
		SqlRules model = new SqlRules();
		// los campos texto tienen valor por defecto de string vacio, no hay problemas con nulos
		assertEquals("", model.getSql());
		model.setSql("select 1");
		assertEquals("select 1", model.getSql());
	}

	@Test
	public void testSafeArrayAccess() {
		DbSchema model = new DbSchema();
		// los arrays son nulos por defecto (no funciona la inicializacion en .net)
		// pero se puede acceder de forma segura con ModelUtil.safe
		// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
		// En 6.5.0 vuelve a devolver null, pero hay un parametro que se puede poner a true
		// (creo que es containerDefaultToNull, pero mantengo el comportamiento por defecto)
		assertNull(model.getTables());
		assertEquals(0, safe(model.getTables()).size());
		// Tambien se puede anyadir un elemento de forma segura (nativo en modelo java,
		// creado con un postprocesamiento en .net)
		model.addTablesItem(new DbTable());
		assertEquals(1, safe(model.getTables()).size());
		assertEquals(1, model.getTables().size());
	}

	@Test
	public void testSafePutAndGetExistingItems() {
		SqlRules model = new SqlRules();
		// los campos de diccionario como summary tambien estan nulos por defecto
		// se puede anyadir sin problemas con el mentodo put* nativo (que se crea en
		// .net reprocesando los modelos)
		// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
		assertEquals(0, model.getSummary().size());
		model.putSummaryItem("key2", "value2");
		model.putSummaryItem("key1", "value1");
		assertEquals(2, model.getSummary().size());
		assertEquals("value1", safe(model.getSummary(), "key1"));
		assertEquals("value2", safe(model.getSummary(), "key2"));

		// los elementos con clave repetida sustituyen el valor
		model.putSummaryItem("key2", "value3");
		assertEquals(2, model.getSummary().size());
		assertEquals("value3", safe(model.getSummary(), "key2"));
	}

	@Test
	public void testSummaryGetNotExistingItems() {
		SqlRules model = new SqlRules();
		// lectura de un item no existente cuando no existe el summary
		// En openapi-generator 6.2.1 devolvia null, en 6.3.0 devuelve vacio
		assertEquals(0, model.getSummary().size());
		assertEquals("", safe(model.getSummary(), "key1"));

		// lectura de un item no existente cuando existe el summary
		model.putSummaryItem("key1", "value1");
		assertEquals("value1", safe(model.getSummary(), "key1"));
		assertEquals("", safe(model.getSummary(), "key2"));
	}

}
