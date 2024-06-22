package test4giis.tdrules.store.loader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.oa.OaLocalAdapter;

/**
 * Basic resolution of symbolic keys
 * - Different entities reference the same symbolic value (inverted V model)
 * - A single entity references different symbolic values (V model with its own key)
 * - Keys in entity reference symbolic values (V model with key composed of references)
 * - Symbol resolution is case insensitive
 */
public class TestSymbolicResolution {
	
	protected TdSchema getModel() {
		// Incomplete structure with master and detail entities, references are set in
		// each test
		TdEntity main1 = new TdEntity().name("main1")
				.addAttributesItem(new TdAttribute().name("pk").datatype("integer").uid("true"));
		TdEntity main2 = new TdEntity().name("main2")
				.addAttributesItem(new TdAttribute().name("pk").datatype("integer").uid("true"));
		TdEntity detail1 = new TdEntity().name("detail1")
				.addAttributesItem(new TdAttribute().name("pk").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string"));
		TdEntity detail2 = new TdEntity().name("detail2")
				.addAttributesItem(new TdAttribute().name("pk").datatype("integer").uid("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string"));
		TdSchema model = new TdSchema().addEntitiesItem(main1).addEntitiesItem(main2).addEntitiesItem(detail1)
				.addEntitiesItem(detail2);
		return model;
	}

	@Test
	public void testDifferentEntitiesSameSymbolValue() {
		TdSchema model = getModel();
		model.getEntity("detail1").addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("main1.pk"));
		model.getEntity("detail2").addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("main1.pk"));
		DataLoader dtg = new DataLoader(model, new OaLocalAdapter());
		dtg.load("main1", "pk=@main1PK0"); // check symbol resolution case insensitive
		dtg.load("main1", "pk=@main1PK1");
		dtg.load("detail1", "fk1=@Main1pk1");
		dtg.load("detail2", "fk1=@Main1pk1");
		dtg.load("detail2", "fk1=@Main1pk0");
		String expected = "\"main1\":{\"pk\":1}\n" + "\"main1\":{\"pk\":2}\n"
				+ "\"detail1\":{\"pk\":201,\"value\":\"202\",\"fk1\":2}\n"
				+ "\"detail2\":{\"pk\":301,\"value\":\"302\",\"fk1\":2}\n"
				+ "\"detail2\":{\"pk\":401,\"value\":\"402\",\"fk1\":1}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testSingleEntityDifferentSymbols() {
		TdSchema model = getModel();
		model.getEntity("detail1").addAttributesItem(new TdAttribute().name("fk1").datatype("integer").rid("main1.pk"));
		model.getEntity("detail1").addAttributesItem(new TdAttribute().name("fk2").datatype("integer").rid("main2.pk"));
		DataLoader dtg = new DataLoader(model, new OaLocalAdapter());
		dtg.load("main1", "pk=@main1pk1");
		dtg.load("main2", "pk=@main2pk1");
		dtg.load("main2", "pk=@main2pk2");
		dtg.load("detail1", "fk1=@main1pk1, fk2=@main2pk2");
		String expected = "\"main1\":{\"pk\":1}\n" 
				+ "\"main2\":{\"pk\":1}\n" 
				+ "\"main2\":{\"pk\":2}\n"
				+ "\"detail1\":{\"pk\":301,\"value\":\"302\",\"fk1\":1,\"fk2\":2}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

	@Test
	public void testEntityReferencesAreKey() {
		TdSchema model = getModel();
		model.getEntity("detail1").getAttributes().remove(0);
		model.getEntity("detail1")
				.addAttributesItem(new TdAttribute().name("fk1").datatype("integer").uid("true").rid("main1.pk"))
				.addAttributesItem(new TdAttribute().name("fk2").datatype("integer").uid("true").rid("main2.pk"));
		DataLoader dtg = new DataLoader(model, new OaLocalAdapter());
		dtg.load("main1", "pk=@main1pk");
		dtg.load("main2", "pk=@main2pk0"); // to have different values in referenced symbols
		dtg.load("main2", "pk=@main2pk");
		dtg.load("detail1", "fk1=@main1pk, fk2=@main2pk");
		dtg.load("detail1", "fk1=@main1pk, fk2=@main2pk0");
		String expected = "\"main1\":{\"pk\":1}\n" 
				+ "\"main2\":{\"pk\":1}\n" 
				+ "\"main2\":{\"pk\":2}\n"
				+ "\"detail1\":{\"value\":\"301\",\"fk1\":1,\"fk2\":2}\n"
				+ "\"detail1\":{\"value\":\"401\",\"fk1\":1,\"fk2\":1}";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString());
	}

}
