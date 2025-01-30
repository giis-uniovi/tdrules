package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.oa.IPathResolver;
import giis.tdrules.store.loader.oa.OaPathResolver;

/**
 * Determination of the endopint path using/not using the model
 */
public class TestOaPathResolver {

	@Test
	public void testResolveFallbackWithoutModel() {
		IPathResolver resolver=new OaPathResolver();
		// Standard resolution, using the lowercase name of the entity
		assertEquals("/entity1", resolver.getEndpointPath("Entity1"));
		assertEquals("/entity2", resolver.getEndpointPath("Entity2"));
	}
	
	// Endpoint can't be located: 
	//   no ddls / no post ddl / no entity
	@Test
	public void testResolveFallbackWithModel() {
		TdSchema schema=new TdSchema().storetype("openapi")
				.addEntitiesItem(new TdEntity().name("Entity0"))
				.addEntitiesItem(new TdEntity().name("Entity1")
						.addDdlsItem(new Ddl().command("put").query("/my/entity1put")));
		
		IPathResolver resolver=new OaPathResolver().setSchemaModel(schema);
		// If no ddls resolves with the entity name
		assertEquals("/entity0", resolver.getEndpointPath("Entity0"));
		assertEquals("/entity1", resolver.getEndpointPath("Entity1"));
		assertEquals("/entityx", resolver.getEndpointPath("Entityx"));
	}
	
	// Endpoint can be located:
	//   single post / first post then put / reverse / more than one post
	@Test
	public void testResolveRight() {
		TdSchema schema=new TdSchema().storetype("openapi")
		.addEntitiesItem(new TdEntity().name("Entity2")
				.addDdlsItem(new Ddl().command("post").query("/my/entity2post")))
		.addEntitiesItem(new TdEntity().name("Entity3")
				.addDdlsItem(new Ddl().command("post").query("/my/entity3post"))
				.addDdlsItem(new Ddl().command("put").query("/my/entity3put")))
		.addEntitiesItem(new TdEntity().name("Entity4")
				.addDdlsItem(new Ddl().command("put").query("/my/entity4put"))
				.addDdlsItem(new Ddl().command("post").query("/my/entity4post")))
		.addEntitiesItem(new TdEntity().name("Entity5")
				.addDdlsItem(new Ddl().command("post").query("/my/entity5post1"))
				.addDdlsItem(new Ddl().command("post").query("/my/entity5post2"))
				.addDdlsItem(new Ddl().command("post").query("/my/entity5post3")));
		
		IPathResolver resolver=new OaPathResolver().setSchemaModel(schema);
		assertEquals("/my/entity2post", resolver.getEndpointPath("Entity2"));
		assertEquals("/my/entity3post", resolver.getEndpointPath("Entity3"));
		assertEquals("/my/entity4post", resolver.getEndpointPath("Entity4"));
		assertEquals("/my/entity5post1", resolver.getEndpointPath("Entity5"));
	}

}
