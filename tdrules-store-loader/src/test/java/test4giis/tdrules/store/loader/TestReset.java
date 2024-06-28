package test4giis.tdrules.store.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.LoaderException;
import giis.tdrules.store.loader.IAttrGen;
import giis.tdrules.store.loader.gen.DictionaryAttrGen;
import giis.tdrules.store.loader.gen.RandomAttrGen;
import giis.tdrules.store.loader.oa.OaLocalAdapter;
import giis.tdrules.store.loader.sql.SqlLocalAdapter;

/**
 * Generated values should be the same after a reset of the data generator
 */
public class TestReset {
	
	// Test situations and where tested (* means here)
	// - IUidGen
	//   - SequentialUidGen 	*MainOa, *MainSql
	//   - OaLiveUidGen			...loader.oa.TestOaLiveGenerator
	//   - SqlLiveUidGen		...loader.sql.TestGenerator
	//   - LegacyUidGen			not tested, no reset
	// - IDataAdapter
	//   - OaAdapter 			*MainOa
	//   - OaLiveAdapter		...loader.oa.TestOaLiveGenerator
	//   - SqlAdapter 			*MainSql
	//   - SqlLiveAdapter		...loader.sql.TestGenerator
	// - IAttrGen
	//   - DeterministicAttrGen *MainOa, *MainSql
	//   - RandomAttrGen		*MainOaRandomAttrGen
	//	 - null probability 	*MainOaRandomAttrGen
	//   - DictionaryAttrGen 	*Dictionary
	// - Constraints
	//   - ConstraintInteger 	*MainOa, *MainSql
	//   - ConstraintDecimal 	*MainOa, *MainSql
	
	@Test
	public void testResetMainOa() {
		DataLoader dtg = new DataLoader(getDataTypesModel(), new OaLocalAdapter());
		String expected = "\"gg1\":{\"Pk\":1,\"I1\":2,\"I2\":98,\"F1\":0.4,\"F2\":9.5,\"C1\":\"6\",\"C2\":\"7\",\"D1\":\"2007-01-08\"}\n"
				+ "\"gg1\":{\"Pk\":101,\"I1\":102,\"I2\":8,\"F1\":10.4,\"F2\":1.5,\"C1\":\"106\",\"C2\":\"107\",\"D1\":\"2007-01-09\"}\n"
				+ "\"gg1\":{\"Pk\":201,\"I1\":202,\"I2\":13,\"F1\":20.4,\"F2\":2.5,\"C1\":\"206\",\"C2\":\"207\",\"D1\":\"2007-01-10\"}";
		//NOTA Cuando no se especifica valor simbolico, el sequential key gen es omitido y se genera un numero normal
		//esto se ve de forma mas clara en el random
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
		dtg.reset();
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testResetMainSql() {
		DataLoader dtg = new DataLoader(getDataTypesModel(), new SqlLocalAdapter("postgres"));
		String expected = "INSERT INTO gg1 (Pk, I1, I2, F1, F2, C1, C2, D1) VALUES (1, 2, 98, 0.4, 9.5, 6, 7, '2007-01-08')\n"
				+ "INSERT INTO gg1 (Pk, I1, I2, F1, F2, C1, C2, D1) VALUES (101, 102, 8, 10.4, 1.5, 106, 107, '2007-01-09')\n"
				+ "INSERT INTO gg1 (Pk, I1, I2, F1, F2, C1, C2, D1) VALUES (201, 202, 13, 20.4, 2.5, 206, 207, '2007-01-10')";
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
		dtg.reset();
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testResetDictionary() {
		DictionaryAttrGen dict = new DictionaryAttrGen()
				.with("gg1", "c1").dictionary("aaa", "bbb", "ccc", "ddd", "eee")
				.with("gg1", "c2").dictionary("xxx", "yyy", "zzz", "ttt", "uuu");
		DataLoader dtg = new DataLoader(getDataTypesModel(), new OaLocalAdapter()).setAttrGen(dict);
		String expected = "\"gg1\":{\"Pk\":1,\"I1\":2,\"I2\":98,\"F1\":0.4,\"F2\":9.5,\"C1\":\"aaa\",\"C2\":\"xxx\",\"D1\":\"2007-01-08\"}\n"
				+ "\"gg1\":{\"Pk\":101,\"I1\":102,\"I2\":8,\"F1\":10.4,\"F2\":1.5,\"C1\":\"bbb\",\"C2\":\"yyy\",\"D1\":\"2007-01-09\"}\n"
				+ "\"gg1\":{\"Pk\":201,\"I1\":202,\"I2\":13,\"F1\":20.4,\"F2\":2.5,\"C1\":\"ccc\",\"C2\":\"zzz\",\"D1\":\"2007-01-10\"}";
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
		dtg.reset();
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		dtg.load("gg1", "");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testResetMainOARandomAttrGen() {
		TdSchema model = getDataTypesModel(); // must set all nullable (except pk)
		for (int i=1 ; i < model.getEntities().get(0).getAttributes().size(); i++)
			model.getEntities().get(0).getAttributes().get(i).notnull("false");
		IAttrGen attrgen = new RandomAttrGen().setRandomSeed(123);
		// includes random null probability
		DataLoader dtg = new DataLoader(model, new OaLocalAdapter())
				.setAttrGen(attrgen).setNullProbability(40);
		String expected = "\"gg1\":{\"Pk\":1,\"I1\":150,\"I2\":10,\"F1\":35.7,\"F2\":null,\"C1\":null,\"C2\":\"n\",\"D1\":null}\n"
				+ "\"gg1\":{\"Pk\":2,\"I1\":null,\"I2\":71,\"F1\":null,\"F2\":9.1,\"C1\":\"a\",\"C2\":null,\"D1\":null}\n"
				+ "\"gg1\":{\"Pk\":3,\"I1\":null,\"I2\":null,\"F1\":37.8,\"F2\":3.9,\"C1\":\"f\",\"C2\":\"d\",\"D1\":\"2007-10-21\"}\n"
				+ "\"gg1\":{\"Pk\":4,\"I1\":353,\"I2\":null,\"F1\":null,\"F2\":6.9,\"C1\":\"v\",\"C2\":\"s\",\"D1\":null}\n"
				+ "\"gg1\":{\"Pk\":5,\"I1\":850,\"I2\":81,\"F1\":null,\"F2\":null,\"C1\":\"a\",\"C2\":null,\"D1\":null}";
		dtg.load("gg1", "pk=@mpk1");
		dtg.load("gg1", "pk=@mpk2");
		dtg.load("gg1", "pk=@mpk3");
		dtg.load("gg1", "pk=@mpk4");
		dtg.load("gg1", "pk=@mpk5");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
		dtg.reset();
		dtg.load("gg1", "pk=@mpk1");
		dtg.load("gg1", "pk=@mpk2");
		dtg.load("gg1", "pk=@mpk3");
		dtg.load("gg1", "pk=@mpk4");
		dtg.load("gg1", "pk=@mpk5");
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testResetMainOARandomAttrGenThrowsIfNoSeed() {
		IAttrGen attrgen = new RandomAttrGen();
		DataLoader dtg = new DataLoader(getDataTypesModel(), new OaLocalAdapter()).setAttrGen(attrgen);
		dtg.load("gg1", "");
		assertThrows(LoaderException.class, () -> {
			dtg.reset();
		});
	}

	protected TdSchema getDataTypesModel() {
		TdEntity gg1 = new TdEntity().name("Gg1")
				.addAttributesItem(new TdAttribute().name("Pk").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I1").datatype("integer").notnull("true")) // compatible oa/sql
				.addAttributesItem(new TdAttribute().name("I2").datatype("integer").notnull("true"))
				.addAttributesItem(new TdAttribute().name("F1").datatype("double").notnull("true"))
				.addAttributesItem(new TdAttribute().name("F2").datatype("float").notnull("true"))
				.addAttributesItem(new TdAttribute().name("C1").datatype("string").notnull("true"))
				.addAttributesItem(new TdAttribute().name("C2").datatype("string").notnull("true"))
				.addAttributesItem(new TdAttribute().name("D1").datatype("date").notnull("true"));
		gg1.addChecksItem(new TdCheck().attribute("i2").constraint("i2<=100"))
				.addChecksItem(new TdCheck().attribute("i2").constraint("i2>5"))
				.addChecksItem(new TdCheck().attribute("f2").constraint("f2<=10"))
				.addChecksItem(new TdCheck().attribute("f2").constraint("f2>1"));
		return new TdSchema().storetype("openapi").addEntitiesItem(gg1);
	}

}
