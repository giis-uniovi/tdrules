package test4giis.tdrules.model;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;

import org.junit.Test;

import giis.tdrules.model.ModelUtil;
import giis.tdrules.model.transform.SchemaFilter;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Entity/attribute exclusions in a filter
 */
public class TestSchemaFilter extends Base {

	protected TdSchema getModel() {
		TdEntity e1 = new TdEntity().name("entityone")
				.addAttributesItem(new TdAttribute().name("key").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("link").datatype("string"))
				.addAttributesItem(new TdAttribute().name("attr_html").datatype("string"));
		TdEntity e2 = new TdEntity().name("tabletwo")
				.addAttributesItem(new TdAttribute().name("key").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("link").datatype("string"))
				.addAttributesItem(new TdAttribute().name("attr_html").datatype("string"));
		TdEntity e3 = new TdEntity().name("tablethree")
				.addAttributesItem(new TdAttribute().name("key").datatype("integer").uid("true").notnull("true"))
				.addAttributesItem(new TdAttribute().name("link").datatype("string"))
				.addAttributesItem(new TdAttribute().name("attr_html").datatype("string"));
		TdSchema model = new TdSchema().storetype("openapi").addEntitiesItem(e1).addEntitiesItem(e2).addEntitiesItem(e3);
		return model;
	}

	@Test
	public void testExactFilterWildcardEntities() {
		SchemaFilter filter = new SchemaFilter(getModel()).add("Entityone", "*").add("tableTwo", "*");
		assertEquals("tablethree: key, link, attr_html", getNames(filter.filter()));
	}

	@Test
	public void testExactFilterWildcardAttributes() {
		SchemaFilter filter = new SchemaFilter(getModel()).add("*", "link").add("tabletwo", "attr_html");
		assertEquals("entityone: key, attr_html\ntabletwo: key\ntablethree: key, attr_html", 
				getNames(filter.filter()));
	}
	
	@Test
	public void testStartsFilterEntitiesAndAttributes() {
		SchemaFilter filter = new SchemaFilter(getModel()).add("table*", "*").add("*", "attr_*");
		assertEquals("entityone: key, link", 
				getNames(filter.filter()));
	}

	@Test
	public void testEndsFilterEntitiesAndAttributes() {
		SchemaFilter filter = new SchemaFilter(getModel()).add("*one", "*").add("*", "*_html").add("tabletwo", "*ey");
		assertEquals("tabletwo: link\ntablethree: key, link", 
				getNames(filter.filter()));
	}

	@Test
	public void testWithoutEntities() {
		TdSchema schema = new TdSchema().storetype("openapi");
		SchemaFilter filter = new SchemaFilter(schema).add("*one", "*").add("*", "*_html").add("tabletwo", "*ey");
		assertEquals("", getNames(filter.filter()));
	}

	@Test
	public void testWithoutAttributes() {
		TdSchema schema = new TdSchema().storetype("openapi")
				.addEntitiesItem(new TdEntity().name("tableempty"));
		SchemaFilter filter = new SchemaFilter(schema).add("*one", "*").add("*", "*_html").add("tabletwo", "*ey");
		assertEquals("tableempty:", getNames(filter.filter()));
	}

	private String getNames(TdSchema model) {
		StringBuilder sb = new StringBuilder();
		for (TdEntity entity : ModelUtil.safe(model.getEntities())) {
			sb.append("\n").append(entity.getName()).append(": ");
			sb.append(ModelUtil.safe(entity.getAttributes()).stream()
					.map(TdAttribute::getName).collect(Collectors.joining(", ")));
		}
		return sb.toString().trim();
	}
	
}
