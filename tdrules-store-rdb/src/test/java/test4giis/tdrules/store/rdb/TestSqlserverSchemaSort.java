package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import giis.portable.util.JavaCs;
import giis.tdrules.store.rdb.SchemaException;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.tdrules.store.rdb.SchemaSorter;
import giis.tdrules.store.stypes.StoreType;

/**
 * Ordering of tables according their master-detail relationships
 */
public class TestSqlserverSchemaSort extends Base {
	protected String catalog = null;
	protected String schema = null;
	protected StoreType dbms;
	protected String enableCheck = ""; // algunas dbms como oracle requieren que se especifique enabled en la creacion
	protected Connection dbt;

	@Before
	@Override
	public void setUp() throws SQLException {
		super.setUp();
		dropTableShapes();
		dbms = StoreType.get(dbmsname);
		dbt = getConnection(TEST_DBNAME2);
	}

	// Estos tests utilizan muchas tablas que se crearan en el momento de la ejecucion de cada test
	// El borrado se realiza para todas las tablas, como hay relaciones recursivas, las elimina antes
	protected void dropTableShapes() throws SQLException {
		Connection db = getConnection(TEST_DBNAME2);
		executeNotThrow(db, "alter table dg0 drop constraint fk_dg0_dg0");
		executeNotThrow(db, "alter table dgd drop constraint fk_dgd_dgm");
		executeNotThrow(db, "alter table dg1 drop constraint fk_dg1_dgm");
		executeNotThrow(db, "alter table dgx drop constraint fk_dgx_dgd");
		executeNotThrow(db, "alter table dg2 drop constraint fk_dg2_dgd");
		executeNotThrow(db, "alter table dgm drop constraint fk_dgm_dg2");
		executeNotThrow(db, "alter table dgm drop constraint fk_dgm_dg0");
		executeNotThrow(db, "alter table dgd drop constraint fk_dgd_dgm");
		executeNotThrow(db, "alter table dgd drop constraint fk_dgd_dg1");
		executeNotThrow(db, "alter table dgd drop constraint fk_dgd_dg0");
		executeNotThrow(db, "alter table dg3 drop constraint fk_dg3_dg1");
		executeNotThrow(db, "alter table dg3 drop constraint fk_dg3_dg2");
		executeNotThrow(db, "drop table dg0");
		executeNotThrow(db, "drop table dg1");
		executeNotThrow(db, "drop table dg2");
		executeNotThrow(db, "drop table dgd");
		executeNotThrow(db, "drop table dgm");
		executeNotThrow(db, "drop table dgx");
		executeNotThrow(db, "drop table dg3");
	}

	protected void createTableShapes(boolean useDiamond, boolean extendMasterDetail, boolean recursive,
			boolean shortCycle, boolean longCycle) throws SQLException {
		Connection db = getConnection(TEST_DBNAME2);
		// relacion lineal maestro dgm detalle dgd a traves de dg0
		execute(db, "create table dgm (Pk1 int, primary key(Pk1), Fk1 int not null)");
		execute(db, "create table dg0 (Pk1 int, primary key(Pk1), Fk1 int not null, Fk2 int not null)");
		execute(db, "create table dgd (Pk1 int, primary key(Pk1), Fk1 int not null, Fk2 int not null)");
		execute(db, "alter table dgd add constraint FK_DGD_DG0 foreign key(Fk1) references dg0(pk1)");
		execute(db, "alter table dg0 add constraint FK_DG0_DG1 foreign key(Fk1) references dgm(pk1)");
		if (useDiamond) { // dg1 en paralelo a dg0, se forma un diamante
			execute(db, "create table dg1 (Pk1 int, primary key(Pk1), Fk1 int not null)");
			execute(db, "alter table dgd add constraint FK_DGD_DG1 foreign key(Fk2) references dg1(pk1)");
			execute(db, "alter table dg1 add constraint FK_DG1_DGM foreign key(Fk1) references dgm(pk1)");
		}
		if (extendMasterDetail) { 
			// extiende con otras tablas por encima (dg2) y debajo (dgx), con un detalle
			// adicional en dg2 (dg3), v invertida como master de todos
			execute(db, "create table dg2 (Pk1 int, primary key(Pk1), Fk1 int)");
			execute(db, "alter table dgm add constraint FK_DGM_DG2 foreign key(Fk1) references dg2(pk1)");
			execute(db, "create table dg3 (Pk1 int, primary key(Pk1), Fk1 int)");
			execute(db, "alter table dg3 add constraint FK_DG3_DG2 foreign key(Fk1) references dg2(pk1)");
			// un detalle adicional de todos
			execute(db, "create table dgx (Pk1 int, primary key(Pk1), Fk1 int)");
			execute(db, "alter table dgx add constraint FK_DGX_DGD foreign key(Fk1) references dgd(pk1)");
		}
		if (recursive) {
			execute(db, "alter table dg0 add constraint FK_DG0_DG0 foreign key(Fk2) references dg0(pk1)");
		}
		if (shortCycle) {
			execute(db, "alter table dgm add constraint FK_DGM_DG0 foreign key(Fk1) references dg0(pk1)");
		}
		if (longCycle) {
			execute(db, "alter table dg2 add constraint FK_DG2_DGd foreign key(Fk1) references dgd(pk1)");
		}
		db.close();
	}

	// modelos progresivamente mas largos, empezando por tres tablas alineadas,
	// luego diamante y luego prolongacion del diamante

	@Test
	public void testTablesLinearModel() throws SQLException {
		this.createTableShapes(false, false, false, false, false);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		// todas las permutaciones
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dg0", "dgd" }));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dg0", "dgm", "dgd" }));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dgd", "dg0" }));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgd", "dg0", "dgm" }));
	}

	@Test
	public void testTablesDiamondModel() throws SQLException {
		this.createTableShapes(true, false, false, false, false);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		// orden directo e inverso
		assertEquals("[dgm, dg0, dg1, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dg0", "dg1", "dgd" }));
		assertEquals("[dgm, dg0, dg1, dgd]", orderTablesAsString(sr, new String[] { "dgd", "dg1", "dg0", "dgm" }));
		// intercambia extremos y en orden inverso
		assertEquals("[dgm, dg0, dg1, dgd]", orderTablesAsString(sr, new String[] { "dg0", "dgm", "dgd", "dg1" }));
		assertEquals("[dgm, dg1, dg0, dgd]", orderTablesAsString(sr, new String[] { "dg1", "dgd", "dgm", "dg0" })); 
		// dg1 dg0 pueden estar en cualquier orden
		
		// intercambios internos que estan al mismo nivel
		assertEquals("[dgm, dg1, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dg1", "dg0", "dgd" }));
	}

	@Test
	public void testTablesLongModel() throws SQLException {
		this.createTableShapes(true, true, false, false, false);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		// todo ordenado
		assertEquals("[dg2, dg3, dgm, dg0, dg1, dgd, dgx]",
				orderTablesAsString(sr, new String[] { "dg2", "dg3", "dgm", "dg0", "dg1", "dgd", "dgx" }));
		// orden al reves, no igual que anterior, pero tambien de detalle a maestro (dg1
		// y dg0 son intercambialbes, dg3 no tiene detalles)
		assertEquals("[dg2, dgm, dg0, dg1, dgd, dgx, dg3]",
				orderTablesAsString(sr, new String[] { "dgx", "dgd", "dg1", "dg0", "dgm", "dg3", "dg2" }));
		// ordenado pero con extremos en el medio (otro orden en dg3 dgm pero tambien
		// valido)
		assertEquals("[dg2, dgm, dg3, dg0, dg1, dgd, dgx]",
				orderTablesAsString(sr, new String[] { "dgm", "dg2", "dg3", "dg0", "dg1", "dgx", "dgd" }));
	}

	// Ciclos y recursividad, empezando por recursividad, ciclo corto en dos tablas,
	// y ciclo mas largo
	// Utiliza los mismos esquemas que los tests anteriores anyadiendo los ciclos
	// Para que funcionen bien los ciclos habria que proporcionar informacion sobre
	// las relaciones que rompen estos ciclos

	@Test
	public void testTablesLinearModelWithRecursive() throws SQLException {
		this.createTableShapes(false, false, true, false, false);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dg0", "dgd" }));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dg0", "dgm", "dgd" }));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dgd", "dg0" }));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgd", "dg0", "dgm" }));
	}

	@Test
	public void testTablesLinearModelWithShortCycle() throws SQLException {
		this.createTableShapes(false, false, false, true, false);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		// Como hay un ciclo causa excepcion al detectarlo si no se configura la
		// constraint a excluir en la busqueda
		try {
			assertEquals("[dg0, dgm, dgd]", orderTablesAsString(sr, new String[] { "dg0", "dgm", "dgd" }));
			fail("Deberia producirse una excepcion");
		} catch (SchemaException e) {
			assertEquals("Too many recusive levels when trying to sort tables", e.getMessage());
		}
		// excluyendo la constraint que marca el ciclo se comporta como en el grupo de
		// pruebas iniciales sin ciclos
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dg0", "dgd" }, "FK_dgm_dg0"));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dg0", "dgm", "dgd" }, "FK_dgm_dg0"));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgm", "dgd", "dg0" }, "FK_dgm_dg0"));
		assertEquals("[dgm, dg0, dgd]", orderTablesAsString(sr, new String[] { "dgd", "dg0", "dgm" }, "FK_dgm_dg0"));
	}

	@Test
	public void testTablesLongModelWithLongCycle() throws SQLException {
		this.createTableShapes(true, true, false, false, true);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		// Como hay un ciclo causa excepcion al detectarlo si no se configura la
		// constraint a excluir en la busqueda
		try {
			assertEquals("[dg2, dgd, dgm, dg0, dg1, dgx, dg3]",
					orderTablesAsString(sr, new String[] { "dgx", "dgd", "dg1", "dg0", "dgm", "dg3", "dg2" }));
			fail("Deberia producirse una excepcion");
		} catch (SchemaException e) {
			assertEquals("Too many recusive levels when trying to sort tables", e.getMessage());
		}
		// excluyendo la realcion circular, aunque hay diferentes soluciones, difieren
		// en donde se coloca dg3
		assertEquals("[dg2, dg3, dgm, dg0, dg1, dgd, dgx]", orderTablesAsString(sr,
				new String[] { "dg2", "dg3", "dgm", "dg0", "dg1", "dgd", "dgx" }, "FK_dg2_dgd"));
		assertEquals("[dg2, dgm, dg0, dg1, dgd, dgx, dg3]", orderTablesAsString(sr,
				new String[] { "dgx", "dgd", "dg1", "dg0", "dgm", "dg3", "dg2" }, "FK_dg2_dgd"));
		assertEquals("[dg2, dgm, dg3, dg0, dg1, dgd, dgx]", orderTablesAsString(sr,
				new String[] { "dgm", "dg2", "dg3", "dg0", "dg1", "dgx", "dgd" }, "FK_dg2_dgd"));
	}

	// Otras situaciones

	@Test
	public void testTableNotInSchema() throws SQLException {
		this.createTableShapes(false, false, false, false, false);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		try {
			orderTablesAsString(sr, new String[] { "dgm", "xxx", "dgd" });
			fail("Deberia producirse una excepcion");
		} catch (SchemaException e) {
			assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: xxx", e.getMessage());
		}
	}

	@Test
	public void testTableIsSubsetOfSchema() throws SQLException {
		// Carga el long model, pero solo pide ordenar parte de las tablas:
		// quitara tablas directamente referenciadas (dg1, dg2) y otras no referenciadas
		// (dg3), ninguna debe salir
		this.createTableShapes(true, true, false, false, false);
		SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
		assertEquals("[dgm, dg0, dgd, dgx]", orderTablesAsString(sr, new String[] { "dgx", "dgd", "dg0", "dgm" }));
	}

	// Metodos de utilidad para obtener las tablas ordenadas

	protected String orderTablesAsString(SchemaReaderJdbc sr, String[] tables) {
		return orderTablesAsString(sr, tables, "");
	}

	protected String orderTablesAsString(SchemaReaderJdbc sr, String[] tables, String checkToExclude) {
		sr.setUseCache(true);
		List<String> allTables = new ArrayList<String>();
		for (int i = 0; i < tables.length; i++)
			allTables.add(tables[i]);
		SchemaSorter ss = new SchemaSorter(sr);
		if (!"".equals(checkToExclude)) // para las relaciones circulares
			ss.noFollowConstraint(checkToExclude);
		List<String> orderedTables = ss.sort(allTables);
		return JavaCs.deepToString(JavaCs.toArray(orderedTables)).toLowerCase();
	}

}
