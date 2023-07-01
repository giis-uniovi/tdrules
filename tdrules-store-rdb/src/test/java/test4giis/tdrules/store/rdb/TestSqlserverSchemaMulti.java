package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import giis.tdrules.store.ids.TableIdentifier;
import giis.tdrules.store.rdb.SchemaReaderJdbc;

/**
 * Management of multiple schemas and identifier resolution.
 * A number of tests are only implemented in oracle because of
 * its particularities when an user access multiple schemas.
 */
public class TestSqlserverSchemaMulti extends Base {
	protected String catalog = null;
	protected String schema = null;
	protected String enableCheck = ""; // algunas dbms como oracle requieren que se especifique enabled en la creacion

	@Before
	public void setUp() throws SQLException {
		super.setUp();
		// en esta clase y derivadas cada test maneja su conexion
	}

	/**
	 * Obtencion de nombres cualificados a partir de sus componentes
	 */
	@Test
	public void testGetQualifiedNames() {
		assertEquals("c.s.t", TableIdentifier.getQualifiedName("c", "s", "t"));
		assertEquals("s.t", TableIdentifier.getQualifiedName("", "s", "t"));
		assertEquals("s.t", TableIdentifier.getQualifiedName(null, "s", "t"));
		assertEquals("c..t", TableIdentifier.getQualifiedName("c", "", "t"));
		assertEquals("c..t", TableIdentifier.getQualifiedName("c", null, "t"));
		assertEquals("t", TableIdentifier.getQualifiedName("", "", "t"));
		assertEquals("t", TableIdentifier.getQualifiedName(null, null, "t"));
		try {
			assertEquals("c.s.", TableIdentifier.getQualifiedName("c", "s", ""));
			fail("Se esperaba excepcion");
		} catch (Throwable e) {
			assertEquals("SchemaTableIdentifier.getQualifiedName: table name is empty", e.getMessage());
		}
		try {
			assertEquals("c.s.", TableIdentifier.getQualifiedName("c", "s", null));
			fail("Se esperaba excepcion");
		} catch (Throwable e) {
			assertEquals("SchemaTableIdentifier.getQualifiedName: table name is empty", e.getMessage());
		}
	}

	/**
	 * Obtencion de identificadores completos y por defecto teniendo en cuenta una
	 * especificacion dada de catalogo y esquema. Identificadores incluidos con
	 * diferente cantidad de componentes
	 */
	@Test
	public void testSchemaIdentifiers() throws SQLException {
		Connection db = getConnection(TEST_DBNAME2); // solo para dar contexto a la instancia de QualifiedTableName
		// tabla sola, sin cualificar, conbinaciones en especificacion de
		// catalogo/esquema por defecto
		assertQ(db, "c.s.n", "n", "c", "s", "n");
		assertQ(db, "s.n", "n", "", "s", "n");
		assertQ(db, "c..n", "n", "c", "", "n");
		assertQ(db, "n", "n", "", "", "n");

		// tabla qualificada:
		// Catalogo y esquema por defecto se especifican,
		assertQ(db, "a.b.n", "a.b.n", "c", "s", "a.b.n");
		assertQ(db, "c.b.n", "b.n", "c", "s", "b.n");
		assertQ(db, "a.s.n", "a..n", "c", "s", "a..n");
		// Catalogo por defecto no se especifican y esquema si
		assertQ(db, "a.b.n", "a.b.n", "", "s", "a.b.n");
		assertQ(db, "b.n", "b.n", "", "s", "b.n");
		assertQ(db, "a.s.n", "a..n", "", "s", "a..n");
		// Catalogo por defecto se especifica y esquema no
		assertQ(db, "a.b.n", "a.b.n", "c", "", "a.b.n");
		assertQ(db, "c.b.n", "b.n", "c", "", "b.n");
		assertQ(db, "a..n", "a..n", "c", "", "a..n");

		// otros tests con cero o mas componentes (error)
		try {
			new TableIdentifier("c", "s", "", false);
		} catch (Throwable e) {
			assertEquals("Quotation.splitQuotedRight: Name is empty", e.getMessage());
		}
		try {
			new TableIdentifier("c", "s", "a.b.c.d", false);
		} catch (Throwable e) {
			assertEquals("Quotation.splitQuotedRight: Name has more than 3 componentes: a.b.c.d", e.getMessage());
		}

		db.close();
	}

	// asserts correspondientes a obtener el nombre de una tabla full/default
	public void assertQ(Connection db, String expectedFull, String expectedDefault, String defaultCat,
			String defaultSch, String name) {
		// SchemaTableIdentifier
		TableIdentifier si = new TableIdentifier(defaultCat, defaultSch, name, false);
		assertEquals(expectedFull, si.getFullQualifiedTableName());
		assertEquals(expectedDefault, si.getDefaultQualifiedTableName(defaultCat, defaultSch));

		// QualifiedTableName, debe tener en cuenta que los identificadores en oracle
		// son mayusculas y en sqlserver minusculas
		SchemaReaderJdbc mr = new SchemaReaderJdbc(db, defaultCat, defaultSch); // idem
		SchemaReaderJdbc.QualifiedTableName qt = mr.getNewQualifiedTableName(defaultCat, defaultSch, name);
		assertEquals(asStored(expectedFull), qt.getFullQualifiedTableName());
		assertEquals(asStored(expectedDefault), qt.getDefaultQualifiedTableName(defaultCat, defaultSch));
	}

}
