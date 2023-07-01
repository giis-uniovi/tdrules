package test4giis.tdrules.store.ids;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import giis.tdrules.store.ids.TableIdentifier;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;

//Detailed combinations, do not convert to net
@RunWith(JUnitParamsRunner.class)
public class TestTableIdentifierCombinations {

	@Test
	@Parameters({
		"x, y, null, null, t, x.y.t",
		"null, null, c, s, t, c.s.t",
		", , c, s, t,  c.s.t",
		", , , s, t,     s.t",
		", , c, , t,    c..t",
		", , , , t,        t",
		"x, , c, s, t, c.s.t",
		"x, ,  , s, t, x.s.t",
		"x, , c,  , t,  c..t",
		"x, ,  ,  , t,  x..t",
		", y, c, s, t, c.s.t",
		", y,  , s, t,   s.t",
		", y, c,  , t, c.y.t",
		", y,  ,  , t,   y.t",
		"x, y, c, s, t, c.s.t",
		"x, y,  , s, t, x.s.t",
		"x, y, c,  , t, c.y.t",
		"x, y,  ,  , t, x.y.t",
	})
	public void testSchemaTableIdentifierAllParam(@Nullable String defCat, @Nullable String defSch,
			@Nullable String catalog, @Nullable String schema, String table, String fullQualified) {
		TableIdentifier sti=new TableIdentifier(defCat, defSch, catalog, schema, table, false);
		assertEquals(fullQualified, sti.getFullQualifiedTableName());
	}

	@Test
	@Parameters({
		", ,  c.s.t,   c.s.t",
		"x, ,   s.t,   x.s.t",
		", y,  c..t,   c.y.t",
		", ,      t,       t",
		"x, ,     t,    x..t",
		", y,     t,     y.t",
		"x, y,     t,   x.y.t",
		"null, null, t, t",
	})
	public void testSchemaTableIdentifierQualifiedTable(@Nullable String defCat, @Nullable String defSch,
			String table, String fullQualified) {
		TableIdentifier sti=new TableIdentifier(defCat, defSch, table, false);
		assertEquals(fullQualified, sti.getFullQualifiedTableName());
	}

}
