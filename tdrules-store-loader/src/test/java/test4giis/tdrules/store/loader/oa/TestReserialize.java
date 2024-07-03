package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.store.loader.oa.Reserializer;

public class TestReserialize {

	@Test
	public void testReserializeData() {
		String output="{'ent1':[{'attr111':111, 'attr112':112}, {'attr121':121, 'attr122':122}], "
				+ "'ent2':[], "
				+ "'ent3':[{'attr311':311}, {}]"
				+ "}";
		String expected="'ent1':{'attr111':111,'attr112':112}\n"
				+ "'ent1':{'attr121':121,'attr122':122}\n"
				+ "'ent3':{'attr311':311}\n"
				+ "'ent3':{}\n";
		output = output.replace("'", "\"");
		expected=expected.replace("'", "\"");
		String actual=new Reserializer().reserializeData(output);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testReserialize() {
		String output="[{'attr111':111, 'attr112':112}, {}, {'attr121':121}]";
		String expected="{'attr111':111,'attr112':112}\n"
				+ "{}\n"
				+ "{'attr121':121}\n";
		output = output.replace("'", "\"");
		expected=expected.replace("'", "\"");
		String actual=new Reserializer().reserializeList(output);
		assertEquals(expected, actual);
	}
	
}
