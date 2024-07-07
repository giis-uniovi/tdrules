package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.gen.DictionaryAttrGen;
import giis.tdrules.store.loader.oa.OaLocalAdapter;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Test data generation both with and without dictionaries is in
 * TestOaLocalGeneration; here generation when there are collisions between
 * specified values for a string attribute and values in the dictionary is
 * tested (this causes these items removed from the dictionary)
 */
@RunWith(JUnitParamsRunner.class)
public class TestOaDictionaryCollisions extends Base {

	protected TdSchema getDataTypesModel() {
		TdEntity entity = new TdEntity().name("ent")
				.addAttributesItem(new TdAttribute().name("pk").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("str").datatype("string").notnull("true"));
		return new TdSchema().storetype("openapi").addEntitiesItem(entity);
	}

	protected DataLoader getGenerator(TdSchema model) {
		return new DataLoader(model, new OaLocalAdapter())
				.setAttrGen(new DictionaryAttrGen().with("ent", "str").dictionary("aa", "bb", "cc"));
	}

	@Test
	@Parameters({ "_;xx;_;_;_;_ , aa;xx;bb;cc;aa-1;bb-1, no collision",
			"bb;_;_;_;_;_ , bb;aa;cc;aa-1;bb-1;cc-1, spec before collision index",
			"_;bb;_;_;_;_ , aa;bb;cc;aa-1;bb-1;cc-1, spec at collision index",
			"_;aa;_;_;_;_ , aa;aa;bb;cc;aa-1;bb-1, spec after collision index (can't avoid)",
			"_;_;_;aa;_;_ , aa;bb;cc;aa;aa-1;bb-1,   spec after collision index at recycle position",

			"bb;_;bb;_;_;_ , bb;aa;bb;cc;aa-1;bb-1, multiple spec before collision index",
			"_;bb;_;bb;_;_ , aa;bb;cc;bb;aa-1;bb-1, multiple spec at collision index",

			"aa;_;_;_;_;_ , aa;bb;cc;aa-1;bb-1;cc-1, collision at first index",
			"_;_;cc;_;_;_ , aa;bb;cc;aa-1;bb-1;cc-1, collision at last index",

			"aa-1;_;_;_;_;_ , aa-1;aa;bb;cc;aa-1;bb-1, exclude collision with recycled items",
			"_;_;_;aa-1;_;_ , aa;bb;cc;aa-1;aa-1;bb-1, exclude collision with recycled items",
			})
	public void testDictionaryCollisionWithSpecValue(String specs, String outs, String message) {
		String[] spec = specs.split(";");
		String[] out = outs.split(";");
		DataLoader dtg = getGenerator(getDataTypesModel());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < Math.max(spec.length, out.length); i++) {
			spec[i] = spec[i].trim();
			out[i] = out[i].trim();
			dtg.load("ent", ("_".equals(spec[i]) ? "" : "str=" + spec[i]));
			sb.append("\"ent\":{\"pk\":" + (i * 100 + 1) + ",\"str\":\"" + out[i] + "\"}\n");
		}
		assertEquals(message, sb.toString().trim(), dtg.getDataAdapter().getAllAsString());
	}
	
}
