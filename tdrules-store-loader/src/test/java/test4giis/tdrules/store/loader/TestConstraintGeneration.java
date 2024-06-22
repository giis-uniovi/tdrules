package test4giis.tdrules.store.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.IConstraint;
import giis.tdrules.store.loader.gen.ConstraintInteger;
import giis.tdrules.store.loader.gen.RandomAttrGen;
import giis.tdrules.store.loader.oa.OaLocalAdapter;
import giis.tdrules.store.loader.sql.SqlLocalAdapter;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

/**
 * Generation of values with numeric constraints
 */
public class TestConstraintGeneration {

	protected VisualAssert va=new VisualAssert().setFramework(Framework.JUNIT4);

	// Exercises data generator and constraint factory
	// - constraints in table/type
	// - case insensitive
	// - integer: with/without constraints
	@Test
	public void testConstraintsGenerateInteger() {
		TdEntity main = new TdEntity().name("main")
				.addAttributesItem(new TdAttribute().name("I1").datatype("int32").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I2").datatype("int64").notnull("true"))
				.addAttributesItem(new TdAttribute().name("composite").datatype("object").compositetype("type").notnull("true"));
		main.addChecksItem(new TdCheck().attribute("i2").constraint("i2<=100"));
		main.addChecksItem(new TdCheck().attribute("i2").constraint("i2>5"));
		// i2 takes 2, 102 without constraints

		// adds a composite entity as an attribute to cg1
		TdEntity type = new TdEntity().name("object").entitytype("type")
				.addAttributesItem(new TdAttribute().name("inner").datatype("int32").notnull("true"));
		type.addChecksItem(new TdCheck().attribute("inner").constraint("inner>=6"));
		type.addChecksItem(new TdCheck().attribute("inner").constraint("inner<105"));
		main.addAttributesItem(
				new TdAttribute().name("composite").datatype("object").compositetype("type").notnull("true"));
		// inner takes 5, 105 without constraints

		TdSchema model = new TdSchema().storetype("openapi").addEntitiesItem(type).addEntitiesItem(main);

		DataLoader dtg = new DataLoader(model, new OaLocalAdapter());
		dtg.load("main", "");
		dtg.load("main", "");
		va.assertEquals("'main':{'I1':1,'I2':97,'composite':{'inner':104}}\n'main':{'I1':101,'I2':7,'composite':{'inner':6}}".replace("'", "\""),
				dtg.getDataAdapter().getAllAsString(), "IntegerConstraints", "constraints-integer.html");
	}
	
	// - decimal: with/without constraints
	// Exactly the same test as for integer, but everything is scaled (divided by 10)
	@Test
	public void testConstraintsGenerateDecimal() {
		TdEntity main = new TdEntity().name("main")
				.addAttributesItem(new TdAttribute().name("I1").datatype("float").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I2").datatype("number").notnull("true"))
				.addAttributesItem(new TdAttribute().name("composite").datatype("object").compositetype("type").notnull("true"));
		main.addChecksItem(new TdCheck().attribute("i2").constraint("i2<=10.0"));
		main.addChecksItem(new TdCheck().attribute("i2").constraint("i2>0.5"));

		TdEntity type = new TdEntity().name("object").entitytype("type")
				.addAttributesItem(new TdAttribute().name("inner").datatype("double").notnull("true"));
		type.addChecksItem(new TdCheck().attribute("inner").constraint("inner>=0.6"));
		type.addChecksItem(new TdCheck().attribute("inner").constraint("inner<10.5"));
		main.addAttributesItem(
				new TdAttribute().name("composite").datatype("object").compositetype("type").notnull("true"));

		TdSchema model = new TdSchema().storetype("openapi").addEntitiesItem(type).addEntitiesItem(main);

		DataLoader dtg = new DataLoader(model, new OaLocalAdapter());
		dtg.load("main", "");
		dtg.load("main", "");
		va.assertEquals("'main':{'I1':0.1,'I2':9.7,'composite':{'inner':10.4}}\n'main':{'I1':10.1,'I2':0.7,'composite':{'inner':0.6}}".replace("'", "\""),
				dtg.getDataAdapter().getAllAsString(), "DecimalConstraints", "constraints-decimal.html");
	}
	
	// - invalid constraint ignored: left / op / number
	@Test
	public void testConstraintsGenerateInvalidInteger() {
		TdEntity main = new TdEntity().name("main")
				.addAttributesItem(new TdAttribute().name("I1").datatype("int32").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I2").datatype("int64").notnull("true"));
		main.addChecksItem(new TdCheck().attribute("i1").constraint("i2-i1<=0"));
		main.addChecksItem(new TdCheck().attribute("i1").constraint("i1=500"));
		main.addChecksItem(new TdCheck().attribute("i2").constraint("i2<='a'"));

		TdSchema model = new TdSchema().storetype("openapi").addEntitiesItem(main);

		DataLoader dtg = new DataLoader(model, new OaLocalAdapter());
		dtg.load("main", "");
		dtg.load("main", "");
		va.assertEquals(
				"'main':{'I1':1,'I2':2}\n'main':{'I1':101,'I2':102}".replace("'", "\""),
				dtg.getDataAdapter().getAllAsString(), "InvalidIntegerConstraints", "constraints-integer-invalid.html");
	}

	// - exact numeric (rdb): 
	//   - without decimals: size empty, only precision, precision and scale
	//   - with decimals (precision and scale>0)
	// - approximate numeric (float)
	@Test
	public void testConstraintsGenerateRdb() {
		TdEntity main = new TdEntity().name("main")
				.addAttributesItem(new TdAttribute().name("I1").datatype("number").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I2").datatype("number").size("7").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I3").datatype("number").size("7,0").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I4").datatype("number").size("7,1").notnull("true")) // generates decimals
				.addAttributesItem(new TdAttribute().name("I5").datatype("float").notnull("true")); // idem
		main.addChecksItem(new TdCheck().attribute("i1").constraint("i1>10"));
		main.addChecksItem(new TdCheck().attribute("i1").constraint("i1<=100"));
		main.addChecksItem(new TdCheck().attribute("i2").constraint("i2>10"));
		main.addChecksItem(new TdCheck().attribute("i2").constraint("i2<=100"));
		main.addChecksItem(new TdCheck().attribute("i3").constraint("i3>10"));
		main.addChecksItem(new TdCheck().attribute("i3").constraint("i3<=100"));
		main.addChecksItem(new TdCheck().attribute("i4").constraint("i4>10"));
		main.addChecksItem(new TdCheck().attribute("i4").constraint("i4<=100"));
		main.addChecksItem(new TdCheck().attribute("i5").constraint("i5>10"));
		main.addChecksItem(new TdCheck().attribute("i5").constraint("i5<=100"));

		TdSchema model = new TdSchema().storetype("postgres").addEntitiesItem(main);

		DataLoader dtg = new DataLoader(model, new SqlLocalAdapter("postgres"));
		dtg.load("main", "");
		dtg.load("main", "");
		va.assertEquals(
				"INSERT INTO main (I1, I2, I3, I4, I5) VALUES (91, 92, 93, 90.4, 90.5)\n"
				+ "INSERT INTO main (I1, I2, I3, I4, I5) VALUES (11, 12, 13, 10.4, 10.5)",
				dtg.getDataAdapter().getAllAsString(), "RdbNumericConstraints", "constraints-rdb-numeric.html");
	}
	
	// Low level tests to exercise constraint instances
	
	@Test
	public void testDeterministicIntegerLimitsWhenValuesOutOfRange() {
		// min 0
		IConstraint ct = new ConstraintInteger().add(">=", "0").add("<=", "2");
		assertLimitedValues(new int[] { 0, 1, 2, 0, 1, 2, 0, 1, 2 }, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 }, ct);
		assertLimitedValues(new int[] { 2, 1, 0, 2, 1, 0, 2, 1 }, new int[] { -1, -2, -3, -4, -5, -6, -7, -8 }, ct);
		// min >0
		ct = new ConstraintInteger().add(">=", "1").add("<=", "3");
		assertLimitedValues(new int[] { 1, 2, 3, 1, 2, 3, 1, 2, 3 }, new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, ct);
		assertLimitedValues(new int[] { 3, 2, 1, 3, 2, 1, 3, 2 }, new int[] { 0, -1, -2, -3, -4, -5, -6, -7 }, ct);
		// min <0
		ct = new ConstraintInteger().add(">=", "-1").add("<=", "1");
		assertLimitedValues(new int[] { -1, 0, 1, -1, 0, 1, -1, 0, 1 }, new int[] { -1, 0, 1, 2, 3, 4, 5, 6, 7 }, ct);
		assertLimitedValues(new int[] { 1, 0, -1, 1, 0, -1, 1, 0 }, new int[] { -2, -3, -4, -5, -6, -7, -8, -9 }, ct);

		ct = new ConstraintInteger().add(">=", "0").add("<=", "99");
		assertLimitedValues(new int[] { 0, 1, 55, 99, 0, 1, 55, 99, 0, 1 }, new int[] { 0, 1, 55, 99, 100, 101, 155, 199, 200, 201 }, ct);
		assertLimitedValues(new int[] { 99, 45, 1, 0, 99, 45, 1, 0, 99 }, new int[] { -1, -55, -99, -100, -101, -155, -199, -200, -201 }, ct);
	}

	@Test
	public void testDeterministicIntegerBoundariesRelationalExpression() {
		int[] expected = new int[] { 0, 1, 2, 0, 1, 2, 0, 1, 2 };
		int[] actual = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
		// one excluding limit
		IConstraint ct = new ConstraintInteger().add(">", "-1").add("<=", "2");
		assertLimitedValues(expected, actual, ct);
		ct = new ConstraintInteger().add(">=", "0").add("<", "3");
		assertLimitedValues(expected, actual, ct);
		// both
		ct = new ConstraintInteger().add(">", "-1").add("<", "3");
		assertLimitedValues(expected, actual, ct);

	}

	private void assertLimitedValues(int[] expected, int[] actual, IConstraint ct) {
		for (int i = 0; i < expected.length; i++)
			assertEquals("item " + i, String.valueOf(expected[i]), ct.apply(String.valueOf(actual[i])));
	}
	
	@Test
	public void testDeterministicIntegerDefaultLimits() {
		IConstraint ct = new ConstraintInteger().add(">=", "11").add("<=", "21");
		assertEquals("[Min: 11, Max: 21]", ct.toString()); // baseline

		ct = new ConstraintInteger().add(">=", "11");
		ct.apply("15"); // requried to set lazy defaults
		assertEquals("[Min: 11, Max: 1010]", ct.toString());
		
		// alghough no min limit, set to zero
		ct = new ConstraintInteger().add("<=", "21");
		ct.apply("15");
		assertEquals("[Min: 0, Max: 21]", ct.toString());
		
		// no min limit, but max is negative
		ct = new ConstraintInteger().add("<=", "-10");
		ct.apply("15");
		assertEquals("[Min: -1009, Max: -10]", ct.toString());
		
		ct = new ConstraintInteger();
		ct.apply("15");
		assertEquals("[Min: 0, Max: 999]", ct.toString());
	}
	
	@Test
	public void testRandomIntegerLimits() {
		// ConstraintInteger does not have functions for random, generation should check min and max values
		// Use a RandomAttrGen instance and a loop to ensure all values are generated
		RandomAttrGen generator=new RandomAttrGen();
		IConstraint ct = new ConstraintInteger().add(">=", "-1").add("<=", "2");
		//4 values, 25% probability each, check that at least half of this probability is met
		int[] counters=new int[] { 0,0,0,0};
		for (int i=0; i<100; i++) {
			String svalue=generator.generateNumber(ct, "entity", "attribte");
			int value=Integer.parseInt(svalue);
			System.out.println(value);
			assertTrue("iteration "+i, value>=-1);
			assertTrue("iteration "+i, value<=2);
			counters[value+1]++;
		}
		assertTrue(counters[0]>=12);
		assertTrue(counters[1]>=12);
		assertTrue(counters[2]>=12);
		assertTrue(counters[3]>=12);
	}

}
