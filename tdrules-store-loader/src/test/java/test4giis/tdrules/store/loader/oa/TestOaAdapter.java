package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.model.EntityTypes;
import giis.tdrules.store.loader.IDataAdapter;
import giis.tdrules.store.loader.oa.OaLocalAdapter;

/**
 * How the adapter writes different datatypes (primitive and composites)
 */
public class TestOaAdapter extends Base {
	
	@Test
	public void testWritePrimitive() {
		IDataAdapter adapter=new OaLocalAdapter();
		adapter.beginWrite("tab");
		adapter.writeValue("integer", "col1", "1");
		adapter.writeValue("number", "col2", "2");
		adapter.writeValue("float", "col3", "3.3");
		adapter.writeValue("double", "col4", "4.4");
		adapter.writeValue("int32", "col5", "5");
		adapter.writeValue("int64", "col6", "6");
		adapter.writeValue("string", "cola", "abcd");
		adapter.writeValue("date", "colb", "2022-01-02");
		adapter.writeValue("date-time", "colc", "2022-01-02T01:02:03");
		adapter.writeValue("boolean", "t", "true");
		adapter.writeValue("boolean", "f", "false");
		adapter.writeValue("string", "ns", null);
		adapter.writeValue("integer", "ni", null);
		adapter.writeValue("idontknow", "unknown", "XXX");
		adapter.endWrite();
		assertEquals(json("'tab':{'col1':1,'col2':2.0,'col3':3.3,'col4':4.4,'col5':5,'col6':6,"
				+ "'cola':'abcd','colb':'2022-01-02','colc':'2022-01-02T01:02:03','t':true,'f':false,'ns':null,'ni':null,'unknown':'XXX'}"), 
				adapter.getAll().get(0));
	}
	
	// Generation of composites:
	// - several in same object
	// - different objects
	// - change generated objects
	// - null/empty values
	@Test
	public void testWriteObjects() {
		IDataAdapter adapter=new OaLocalAdapter();
		adapter.beginWrite("tab");
		adapter.writeValue("integer", "col0", json("1"));
		adapter.writeValue(EntityTypes.DT_TYPE, "col1", json("{'a':1,'b':'x'}"));
		adapter.writeValue(EntityTypes.DT_TYPE, "col2", json("{'a':2,'b':'y'}"));
		adapter.endWrite();
		String expected="'tab':{'col0':1,'col1':{'a':1,'b':'x'},'col2':{'a':2,'b':'y'}}";
		assertEquals(json(expected), adapter.getAll().get(0));
		
		adapter.beginWrite("tab");
		adapter.writeValue(EntityTypes.DT_TYPE, "col1", json("{'a':3,'b':'z'}"));
		adapter.endWrite();
		expected="'tab':{'col1':{'a':3,'b':'z'}}";
		assertEquals(json(expected), adapter.getAll().get(1));
		
		adapter.beginWrite("tab1");
		adapter.writeValue(EntityTypes.DT_TYPE, "col1", json("{'aa':33,'bb':'zz'}"));
		adapter.endWrite();
		expected="'tab1':{'col1':{'aa':33,'bb':'zz'}}";
		assertEquals(json(expected), adapter.getAll().get(2));
		
		adapter.beginWrite("tabn");
		adapter.writeValue(EntityTypes.DT_TYPE, "col1", null);
		adapter.writeValue(EntityTypes.DT_TYPE, "col2", "{}");
		adapter.writeValue(EntityTypes.DT_TYPE, "col3", "");
		adapter.writeValue(EntityTypes.DT_TYPE, "col4", "  ");
		adapter.endWrite();
		expected="'tabn':{'col1':null,'col2':{},'col3':{},'col4':{}}";
		assertEquals(json(expected), adapter.getAll().get(3));
	}
	
	@Test
	public void testWriteArrayOfObjects() {
		IDataAdapter adapter=new OaLocalAdapter();
		adapter.beginWrite("tab");
		adapter.writeValue("integer", "col0", json("1"));
		adapter.writeValue(EntityTypes.DT_ARRAY, "col1", json("[{'a':1,'b':'x'},{'a':2,'b':'y'}]"));
		adapter.writeValue("object"+EntityTypes.DT_ARRAY, "col1o", json("[{'a':1,'b':'x'}]"));
		adapter.writeValue(EntityTypes.DT_ARRAY, "col2", null);
		adapter.writeValue(EntityTypes.DT_ARRAY, "col3", "[]");
		adapter.writeValue(EntityTypes.DT_ARRAY, "col4", "");
		adapter.writeValue(EntityTypes.DT_ARRAY, "col5", "  ");
		adapter.endWrite();
		String expected="'tab':{'col0':1,'col1':[{'a':1,'b':'x'},{'a':2,'b':'y'}],'col1o':[{'a':1,'b':'x'}],'col2':null,'col3':[],'col4':[],'col5':[]}";
		assertEquals(json(expected), adapter.getAll().get(0));
	}

	@Test
	public void testWriteArrayOfPrimitive() {
		IDataAdapter adapter=new OaLocalAdapter();
		adapter.beginWrite("tab");
		adapter.writeValue("integer", "col0", json("1"));
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col1i", json("[{'a':1},{'a':2}]"));
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col1s", json("[{'s':'abc'},{'s':null}]"));
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col2", null);
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col3", "[]");
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col4", "[{}]");
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col5", "[null]");
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col6", "[null,null]");
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col7", "");
		adapter.writeValue("primitive"+EntityTypes.DT_ARRAY, "col8", "  ");
		adapter.endWrite();
		String expected="'tab':{'col0':1,'col1i':[1,2],'col1s':['abc',null],'col2':null,'col3':[],'col4':[],'col5':[],'col6':[],'col7':[],'col8':[]}";
		assertEquals(json(expected), adapter.getAll().get(0));
	}

}
