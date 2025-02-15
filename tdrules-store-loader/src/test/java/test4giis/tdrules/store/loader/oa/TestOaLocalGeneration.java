package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.gen.DictionaryAttrGen;
import giis.tdrules.store.loader.oa.OaLocalAdapter;
import giis.tdrules.openapi.model.TdEntity;

/**
 * Integration of DataGenerator with a local adapter.
 * Checks the resulting json object as string.

 * Most of the tests have been done for sql,
 * here only the data generated using an OaAdapter is checked
 * 
 * - Model variants to test:
 *   basic with different types, master/detail, composite type and composite array
 * - AttrGen to test: deterministic and dictionary
 */
public class TestOaLocalGeneration extends Base {

	// Default generator for tests, using default UidGen
	protected DataLoader getGenerator(TdSchema model) {
		return new DataLoader(model, new OaLocalAdapter());
	}

	// Assert expected outputs (replaces single by double quotes)
	protected void assertRequests(String expected, String actual) {
		assertEquals(json(expected), actual.trim());
	}
	
	// Basic json generation:
	// - different data types
	// - nullable/not nullable
	// - default configuration/nullable configuration
	// - specified null/not null in number/string
	// - generated null values in number/string

	protected TdSchema getDataTypesModel() {
		TdEntity gg1=new TdEntity().name("Gg1")
				.addAttributesItem(new TdAttribute().name("Pk1").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("I1").datatype("int32"))
				.addAttributesItem(new TdAttribute().name("I2").datatype("int64").notnull("true"))
				.addAttributesItem(new TdAttribute().name("C1").datatype("string"))
				.addAttributesItem(new TdAttribute().name("C2").datatype("string").notnull("true"))
				.addAttributesItem(new TdAttribute().name("D1").datatype("date"))
				.addAttributesItem(new TdAttribute().name("D2").datatype("date").notnull("true"));
		return new TdSchema().storetype("openapi")
				.addEntitiesItem(gg1);
	}
	
	@Test
	public void testDataTypes() {
		DataLoader dtg = getGenerator(getDataTypesModel());
		dtg.load("gg1", ""); // by default generates non specified attributes, including nullables
		dtg.setGenerateNullable(false); // exclude nullables
		dtg.load("gg1", "");
		dtg.setGenerateNullable(true); // return to default
		dtg.load("gg1", "i1=null,c1=null");
		dtg.setNullProbability(100); // set all nullable to null
		dtg.load("gg1", "");
		String expected = "'gg1':{'Pk1':1,'I1':2,'I2':3,'C1':'4','C2':'5','D1':'2007-01-06','D2':'2007-01-07'}"
				+ "\n'gg1':{'Pk1':101,'I2':103,'C2':'105','D2':'2007-01-08'}"
				+ "\n'gg1':{'Pk1':201,'I1':null,'I2':203,'C1':null,'C2':'205','D1':'2007-01-08','D2':'2007-01-09'}"
				+ "\n'gg1':{'Pk1':301,'I1':null,'I2':303,'C1':null,'C2':'305','D1':null,'D2':'2007-01-10'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
		
	// Generation of sequential uids and references from dependent entities using symbolic values:
	// - reference determined as by the last insertion/other previous insertion
	// - uid and reference with different values
	// - continue uid generation or an entity after generation of a previous entity
	// Checks data generated by each entity/after all entities

	protected TdSchema getMasterDetailModel() {
		TdEntity master=new TdEntity().name("master")
				.addAttributesItem(new TdAttribute().name("pk").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string").notnull("true"));
		TdEntity detail=new TdEntity().name("detail")
				.addAttributesItem(new TdAttribute().name("pk").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("fk").datatype("string").rid("master.pk").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string").notnull("true"));
		return new TdSchema().storetype("openapi")
				.addEntitiesItem(master)
				.addEntitiesItem(detail);
	}
	
	@Test
	public void testGenerateMasterDetail() {
		DataLoader dtg=getGenerator(getMasterDetailModel());
		dtg.load("master", "pk=@mpk1");
		dtg.load("master", "pk=@mpk2");
		dtg.load("detail", "fk=@mpk2,pk=@dpk1");
		dtg.load("detail", "fk=@mpk1,pk=@dpk2");
		dtg.load("master", "pk=@mpk3");
		String expected="'master':{'pk':1,'value':'2'}"
				+ "\n'master':{'pk':2,'value':'102'}"
				+ "\n'detail':{'pk':1,'fk':'2','value':'203'}"
				+ "\n'detail':{'pk':2,'fk':'1','value':'303'}"
				+ "\n'master':{'pk':3,'value':'402'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testGenerateMasterDetailDict() {
		DataLoader dtg=getGenerator(getMasterDetailModel()).setAttrGen(new DictionaryAttrGen()
				.with("master", "value").dictionary("aa", "bb", "cc")
				.with("detail", "value").dictionary("xx", "yy") );
		dtg.load("master", "pk=@mpk1");
		dtg.load("master", "pk=@mpk2");
		dtg.load("detail", "fk=@mpk2,pk=@dpk1");
		dtg.load("detail", "fk=@mpk1,pk=@dpk2");
		dtg.load("master", "pk=@mpk3");
		String expected="'master':{'pk':1,'value':'aa'}"
				+ "\n'master':{'pk':2,'value':'bb'}"
				+ "\n'detail':{'pk':1,'fk':'2','value':'xx'}"
				+ "\n'detail':{'pk':2,'fk':'1','value':'yy'}"
				+ "\n'master':{'pk':3,'value':'cc'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}

	// Attributes that are composite object (type):
	// - composite with different data types
	// - composite with nullable/not nullable
	// - nesting: composite contains another object
	// - specification of null/not null values for the composite (currently with postgres notation)
	// - composite position last/not last
	// - nullable property of composite: not generated/null specified/null generated (no impl)
	// Some model modifications to test:
	// - readonly in primitive/composite/primitive inside composite
	
	protected TdSchema getObjectTypeModel(boolean nested) {
		TdEntity type2=new TdEntity().name("object2").entitytype("type")
				.addAttributesItem(new TdAttribute().name("key2").datatype("integer").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value2").datatype("string"));
		TdEntity type=new TdEntity().name("object").entitytype("type")
				.addAttributesItem(new TdAttribute().name("key").datatype("integer").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string"));
		if (nested)
			type.addAttributesItem(new TdAttribute().name("composite2").datatype("object2").compositetype("type").notnull("true"));
		TdEntity parent=new TdEntity().name("main")
				.addAttributesItem(new TdAttribute().name("pk1").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("composite").datatype("object").compositetype("type").notnull("true"))
				.addAttributesItem(new TdAttribute().name("last").datatype("string").uid("true").notnull("true"));
		TdSchema model=new TdSchema().storetype("openapi");
		if (nested)
			model.addEntitiesItem(type2);
		model.addEntitiesItem(type);
		model.addEntitiesItem(parent);
		return model;
	}
	
	@Test
	public void testGenerateTypeNotNull() {
		DataLoader dtg=getGenerator(getObjectTypeModel(false));
		dtg.setGenerateNullable(false); // do not generate values of nullables
		dtg.load("main", "");
		dtg.setGenerateNullable(true); // return to default
		dtg.load("main", "");
		dtg.load("main", "(composite).key=999,(composite).value=null");
		dtg.load("main", "composite::key=999,last=xxx"); //checks that oa notation is supported
		dtg.setNullProbability(100); // forces null value
		dtg.load("main", "");
		String expected="'main':{'pk1':1,'composite':{'key':2},'last':'5'}"
				+"\n'main':{'pk1':101,'composite':{'key':102,'value':'103'},'last':'105'}"
				+"\n'main':{'pk1':201,'composite':{'key':999,'value':null},'last':'205'}"
				+"\n'main':{'pk1':301,'composite':{'key':999,'value':'303'},'last':'xxx'}"
				+"\n'main':{'pk1':401,'composite':{'key':402,'value':null},'last':'405'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testGenerateTypeNotNullDict() {
		DataLoader dtg=getGenerator(getObjectTypeModel(false)).setAttrGen(new DictionaryAttrGen()
				.with("main", "last").dictionary("aa", "bb", "cc")
				.with("composite", "value").dictionary("xx", "yy") );
		dtg.setGenerateNullable(false); // do not generate values of nullables
		dtg.load("main", "");
		dtg.setGenerateNullable(true); // return to default
		dtg.load("main", "");
		dtg.load("main", "(composite).key=999,(composite).value=null");
		dtg.load("main", "composite::key=999,last=xxx"); // check oa notation is supported
		dtg.setNullProbability(100); // forces null value
		dtg.load("main", "");
		String expected="'main':{'pk1':1,'composite':{'key':2},'last':'aa'}"
				+"\n'main':{'pk1':101,'composite':{'key':102,'value':'xx'},'last':'bb'}"
				+"\n'main':{'pk1':201,'composite':{'key':999,'value':null},'last':'cc'}"
				+"\n'main':{'pk1':301,'composite':{'key':999,'value':'yy'},'last':'xxx'}"
				+"\n'main':{'pk1':401,'composite':{'key':402,'value':null},'last':'aa-1'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testGenerateTypeReadonly() {
		// Baseline
		DataLoader dtg=getGenerator(getObjectTypeModel(false));
		dtg.load("main", "");
		dtg.load("main", "composite::key=999,last=xxx");
		String expected="'main':{'pk1':1,'composite':{'key':2,'value':'3'},'last':'5'}\n"
				+ "'main':{'pk1':101,'composite':{'key':999,'value':'103'},'last':'xxx'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
		
		// Set primitive attributes as readonly
		TdSchema schema = getObjectTypeModel(false);
		schema.getEntity("object").getAttribute("value").readonly("true"); // value in composite
		schema.getEntity("main").getAttribute("last").readonly("true"); //last
		dtg=getGenerator(schema);
		dtg.load("main", "");
		dtg.load("main", "composite::key=999,last=xxx");
		expected="'main':{'pk1':1,'composite':{'key':2}}\n"
				+ "'main':{'pk1':101,'composite':{'key':999}}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
		
		// Set composite attribute as readonly.
		// Note that internally the data is generated (last keeps the sequence)
		schema = getObjectTypeModel(false);
		schema.getEntity("main").getAttribute("composite").readonly("true"); //composite
		dtg=getGenerator(schema);
		dtg.load("main", "");
		dtg.load("main", "composite::key=999,last=xxx");
		expected="'main':{'pk1':1,'last':'5'}\n"
				+ "'main':{'pk1':101,'last':'xxx'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());		
	}

	@Test
	public void testGenerateTypeNullable() {
		TdSchema model=getObjectTypeModel(false);
		model.getEntity("main").getAttribute("composite").notnull(false);
		DataLoader dtg=getGenerator(model);
		dtg.setGenerateNullable(false); // do not generate values of nullables
		dtg.load("main", "");
		dtg.load("main", "composite=null");
		dtg.setNullProbability(100); // forces null
		dtg.setGenerateNullable(true); // return to default
		dtg.load("main", "");
		//notar que en la primera y segunda last valdra 3 en generacion determinista porque no se han procesado los detalles
		//pero en la tercera last valdra 5 porque se han generado los detalles y para el null se ha hecho un tratamiento particular (writeCompositeType)
		String expected="'main':{'pk1':1,'last':'3'}" 
				+"\n'main':{'pk1':101,'composite':null,'last':'103'}"
				+"\n'main':{'pk1':201,'composite':null,'last':'205'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}

	protected TdSchema getObjectTypeModelWithFk() {
		// adds a reference attribute in the default model
		TdSchema model=getObjectTypeModel(false);
		model.getEntity("object").getAttribute("key").rid("master.key");
		// and the referenced entity
		TdEntity master=new TdEntity().name("master")
				.addAttributesItem(new TdAttribute().name("key").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype("string").notnull("true"));
		model.addEntitiesItem(master);
		return model;
	}
	
	@Test
	public void testGenerateTypeNested() {
		// same as testGenerateTypeNotNull but with nested object
		DataLoader dtg=getGenerator(getObjectTypeModel(true));
		dtg.setGenerateNullable(false); // do not generate values of nullables
		dtg.load("main", "");
		dtg.setGenerateNullable(true); // return to default
		dtg.load("main", "");
		dtg.load("main", "(composite).key=999,(composite).value=null");
		dtg.load("main", "composite::key=999,last=xxx");
		dtg.setNullProbability(100);
		dtg.load("main", "");
		String expected="'main':{'pk1':1,'composite':{'key':2,'composite2':{'key2':4}},'last':'8'}"
				+ "\n'main':{'pk1':101,'composite':{'key':102,'value':'103','composite2':{'key2':104,'value2':'105'}},'last':'108'}"
				+ "\n'main':{'pk1':201,'composite':{'key':999,'value':null,'composite2':{'key2':204,'value2':'205'}},'last':'208'}"
				+ "\n'main':{'pk1':301,'composite':{'key':999,'value':'303','composite2':{'key2':304,'value2':'305'}},'last':'xxx'}"
				+ "\n'main':{'pk1':401,'composite':{'key':402,'value':null,'composite2':{'key2':404,'value2':null}},'last':'408'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}

	// Complemementary scenario  for a composite (hybrid between master/detail and composite):
	// The object type is not defined inline, but in a different openapi schema object that has an uid.
	// Generation here is as always, but the entity that define the structure of the composite has
	// a rid referencing the uid. This rid must appear in the generated attribute and its contents
	// must match with the referenced object.
	
	@Test
	public void testGenerateTypeWithFk() {
		DataLoader dtg=getGenerator(getObjectTypeModelWithFk());
		dtg.load("master", "key=@mpk1");
		dtg.load("master", "key=@mpk2");
		dtg.load("main", "composite::key=@mpk2,pk1=@dpk1"); // check that oa notation is allowed
		dtg.load("main", "(composite).key=@mpk1,pk1=@dpk2");
		String expected="'master':{'key':1,'value':'2'}"
				+ "\n'master':{'key':2,'value':'102'}"
				+ "\n'main':{'pk1':1,'composite':{'key':2,'value':'102'},'last':'205'}"
				+ "\n'main':{'pk1':2,'composite':{'key':1,'value':'2'},'last':'305'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testGenerateTypeWithFkDict() {
		DataLoader dtg=getGenerator(getObjectTypeModelWithFk()).setAttrGen(new DictionaryAttrGen()
				.with("master", "value").dictionary("mm", "nn", "oo")
				.with("main", "last").dictionary("aa", "bb", "cc")
				.with("composite", "value").dictionary("xx", "yy") 
				);
		dtg.load("master", "key=@mpk1");
		dtg.load("master", "key=@mpk2");
		dtg.load("main", "composite::key=@mpk2,pk1=@dpk1"); //comprueba que admite notacion oa
		dtg.load("main", "(composite).key=@mpk1,pk1=@dpk2");
		//anyade uno adicional para comprobar que se mantiene la secuencia del diccionario
		dtg.load("master", "key=@mpk3");
		//se ve que el composite tiene propiedades que coinciden con los del master correspondiente
		String expected="'master':{'key':1,'value':'mm'}"
				+ "\n'master':{'key':2,'value':'nn'}"
				+ "\n'main':{'pk1':1,'composite':{'key':2,'value':'nn'},'last':'aa'}"
				+ "\n'main':{'pk1':2,'composite':{'key':1,'value':'mm'},'last':'bb'}"
				+ "\n'master':{'key':3,'value':'oo'}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	// Generation of attributes that are arrays (primitive and non primitive)
	
	protected TdSchema getObjectArrayModel() {
		TdEntity main=new TdEntity().name("main").entitytype("table")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("arrcol").datatype("main_arrcol_xa").compositetype("array").subtype("string").notnull("true"));
		TdEntity array=new TdEntity().name("main_arrcol_xa").entitytype("array")
				.addAttributesItem(new TdAttribute().name(OaExtensions.ARRAY_PK).datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name(OaExtensions.ARRAY_FK).datatype("integer").rid("main.id").notnull("true"))
				.addAttributesItem(new TdAttribute().name("arrcol").datatype("string").notnull("true"));
		TdEntity mainobj=new TdEntity().name("mainobj").entitytype("table")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("arrcol").datatype("main_arrcolobj_xa").compositetype("array").subtype("object").notnull("true"));
		TdEntity arrayobj=new TdEntity().name("main_arrcolobj_xa").entitytype("array")
				.addAttributesItem(new TdAttribute().name(OaExtensions.ARRAY_PK).datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name(OaExtensions.ARRAY_FK).datatype("integer").rid("mainobj.id").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value1").datatype("string").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value2").datatype("integer").notnull("true"));
		return new TdSchema().storetype("openapi")
				.addEntitiesItem(main)
				.addEntitiesItem(array)
				.addEntitiesItem(mainobj)
				.addEntitiesItem(arrayobj);
	}
	
	// Base scenario, primitive array, 0,1,2 items
	// Note that although the entity containing the array items has a rid to their container, 
	@Test
	public void testGenerateArrayPrimitive() {
		DataLoader dtg=getGenerator(getObjectArrayModel());
		// 2 items
		dtg.load("main_arrcol_xa", "fk_xa=@main1");
		dtg.load("main_arrcol_xa", "fk_xa=@main1");
		dtg.load("main", "id=@main1");
		// 1 item
		dtg.load("main_arrcol_xa", "fk_xa=@main2");
		dtg.load("main", "id=@main2");
		// no items
		dtg.load("main", "id=@main3");
		String expected="'main':{'id':1,'arrcol':['3','103']}\n"
				+ "'main':{'id':2,'arrcol':['303']}\n"
				+ "'main':{'id':3,'arrcol':[]}";
		String actual=dtg.getDataAdapter().getAllAsString();
		assertRequests(expected, actual);
	}
	
	@Test
	public void testGenerateArrayPrimitiveDict() {
		DataLoader dtg=getGenerator(getObjectArrayModel()).setAttrGen(new DictionaryAttrGen()
				.with("main_arrcol_xa", "arrcol").dictionary("xx", "yy") 
				);

		dtg.load("main_arrcol_xa", "fk_xa=@main1");
		dtg.load("main_arrcol_xa", "fk_xa=@main1");
		dtg.load("main", "id=@main1");

		dtg.load("main_arrcol_xa", "fk_xa=@main2");
		dtg.load("main", "id=@main2");

		dtg.load("main", "id=@main3");
		String expected="'main':{'id':1,'arrcol':['xx','yy']}\n"
				+ "'main':{'id':2,'arrcol':['xx-1']}\n"
				+ "'main':{'id':3,'arrcol':[]}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	// Same scenario, with object array
	@Test
	public void testGenerateArrayObject() {
		DataLoader dtg=getGenerator(getObjectArrayModel());

		dtg.load("main_arrcolobj_xa", "fk_xa=@main1");
		dtg.load("main_arrcolobj_xa", "fk_xa=@main1");
		dtg.load("mainobj", "id=@main1");

		dtg.load("main_arrcolobj_xa", "fk_xa=@main2");
		dtg.load("mainobj", "id=@main2");

		dtg.load("mainobj", "id=@main3");
		String expected="'mainobj':{'id':1,'arrcol':[{'value1':'3','value2':4},{'value1':'103','value2':104}]}\n"
				+ "'mainobj':{'id':2,'arrcol':[{'value1':'303','value2':304}]}\n"
				+ "'mainobj':{'id':3,'arrcol':[]}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testGenerateArrayObjectDict() {
		DataLoader dtg=getGenerator(getObjectArrayModel()).setAttrGen(new DictionaryAttrGen()
				.with("main_arrcolobj_xa", "value1").dictionary("xx", "yy", "zz") 
				.with("main_arrcolobj_xa", "value2").mask("999{}")
				);

		dtg.load("main_arrcolobj_xa", "fk_xa=@main1");
		dtg.load("main_arrcolobj_xa", "fk_xa=@main1");
		dtg.load("mainobj", "id=@main1");

		dtg.load("main_arrcolobj_xa", "fk_xa=@main2");
		dtg.load("mainobj", "id=@main2");

		dtg.load("mainobj", "id=@main3");
		String expected="'mainobj':{'id':1,'arrcol':[{'value1':'xx','value2':9994},{'value1':'yy','value2':999104}]}\n"
				+ "'mainobj':{'id':2,'arrcol':[{'value1':'zz','value2':999304}]}\n"
				+ "'mainobj':{'id':3,'arrcol':[]}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	// Special datatypes: additionalProperties
	// They are handled as arrays, using the same schema model, but with a few changes:
	// (1) matching keys are string (2) name of array column should be additionalProperties
	
	@Test
	public void testGenerateAdditionalPropsPrimitive() {
		TdSchema schema = getObjectArrayModel();
		schema.getEntity("main").getAttribute("id").datatype("string");
		schema.getEntity("main").getAttribute("arrcol").name(OaExtensions.ADDITIONAL_PROPERTIES);
		schema.getEntity("main_arrcol_xa").getAttribute(OaExtensions.ARRAY_PK).datatype("string");

		DataLoader dtg = getGenerator(schema);
		dtg.load("main_arrcol_xa", "fk_xa=@main1");
		dtg.load("main_arrcol_xa", "fk_xa=@main1");
		dtg.load("main", "id=@main1");

		String expected = "'main':{'id':'1','additionalProperties':{'1':'3','2':'103'}}";
		String actual = dtg.getDataAdapter().getAllAsString();
		assertRequests(expected, actual);
	}
	
	@Test
	public void testGenerateAdditionalPropsObject() {
		TdSchema schema = getObjectArrayModel();
		schema.getEntity("mainobj").getAttribute("id").datatype("string");
		schema.getEntity("mainobj").getAttribute("arrcol").name(OaExtensions.ADDITIONAL_PROPERTIES);
		schema.getEntity("main_arrcolobj_xa").getAttribute(OaExtensions.ARRAY_PK).datatype("string");

		DataLoader dtg = getGenerator(schema);
		dtg.load("main_arrcolobj_xa", "fk_xa=@main1");
		dtg.load("main_arrcolobj_xa", "fk_xa=@main1");
		dtg.load("mainobj", "id=@main1");

		String expected = "'mainobj':{'id':'1','additionalProperties':"
				+ "{'1':{'value1':'3','value2':4},'2':{'value1':'103','value2':104}}}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	// Special datatypes: free form objects
	
	protected TdSchema getSpecialDatatypesModel() {
		TdEntity freeForm = new TdEntity().name("freeform").entitytype("table")
				.addAttributesItem(new TdAttribute().name("id").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("value").datatype(OaExtensions.FREE_FORM_OBJECT).notnull("true"));
		return new TdSchema().storetype("openapi")
				.addEntitiesItem(freeForm);
	}
	
	@Test
	public void testGenerateFreeFormObject() {
		DataLoader dtg = getGenerator(getSpecialDatatypesModel());
		dtg.load("freeform", "");
		dtg.load("freeform", "");
		// generates a string value, that is added to the output as key (generated) value object
		String expected = "'freeform':{'id':1,'value':{'generated':'2'}}\n"
				+ "'freeform':{'id':101,'value':{'generated':'102'}}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}
	
	@Test
	public void testGenerateFreeFormObjectDict() {
		DataLoader dtg = getGenerator(getSpecialDatatypesModel())
				.setAttrGen(new DictionaryAttrGen().with("freeform", "value").dictionary(
						"{\"key\":\"ab\",\"val\":111}", 
						"{\"key\":\"cd\",\"val\":222}", 
						"{\"key\":\"ef\",\"val\":333}"));
		dtg.load("freeform", "");
		dtg.load("freeform", "");
		// uses the object represented in the dictionary
		String expected = "'freeform':{'id':1,'value':{'key':'ab','val':111}}\n"
				+ "'freeform':{'id':101,'value':{'key':'cd','val':222}}";
		assertRequests(expected, dtg.getDataAdapter().getAllAsString());
	}

}
