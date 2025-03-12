package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.tdrules.store.rdb.JdbcProperties;
import giis.tdrules.store.rdb.SchemaException;
import giis.tdrules.store.rdb.SchemaForeignKey;
import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.tdrules.store.rdb.SchemaTable;

/**
 * Reading advanced metadata (FKs, quoted identifiers...)
 */
public class TestSqlserverSchemaRead extends Base {
	// Nombres de catalogo utilizados para el acceso a los metadatos
	// en sqlserver no se utilizan ya que se accede a las tablas de una base de datos
	protected String catalog = null;
	protected String schema = null;
	protected Connection dbt;

	// prefijo de catalogo y esquema para tablas completamente cualificadas
	protected String myCatalogSchema2 = TEST_DBNAME2.toLowerCase() + ".dbo.";

	// creacion de tablas para pruebas
	protected String sTab1 = "create table " + tablePrefix + "stab1 (col11 int not null primary key, "
			+ "col12 char(3) not null, col13 varchar(16))";
	// tabla con dos fks, una a una clave y otra a una no clave
	protected String sTab2 = "create table " + tablePrefix + "stab2 (col21 decimal(8,4), col22 int default (22), "
			+ "col23 bit, " + "PRIMARY KEY (col21,col22), "
			+ "CONSTRAINT ref_stab1_col11 FOREIGN KEY(col22) REFERENCES stab1(col11) " + ")";

	protected void createTablesAndViews() throws SQLException {
		Connection dbt = getConnection(TEST_DBNAME2); // modo conectado para que se cierre la conexion
		execute(dbt, sTab1);
		execute(dbt, sTab2);
		dbt.close();
	}

	protected void dropTablesAndViews() throws SQLException {
		Connection dbt = getConnection(TEST_DBNAME2); // modo conectado para que se cierre la conexion
		executeNotThrow(dbt, "drop table stab33");
		executeNotThrow(dbt, "drop table stab2");
		executeNotThrow(dbt, "drop table stab1");
		dbt.close();
	}

	@Before
	@Override
	public void setUp() throws SQLException {
		super.setUp();
		// Not all tests need call createTablesAndViews, but drop before to ensure a cleaner setup
		this.dropTablesAndViews();
		dbt = getConnection(TEST_DBNAME2);
	}

	@After
	public void tearDown() throws SQLException {
		dbt.close();
	}

	@Test
	public void testReadDbmsMetadata() throws SQLException {
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		// tipo de base de datos
		assertEquals(dbmsname, mr.getDbmsType().toString());
		// identificacion de la plataforma
		String propPrefix = "tdrules." + PLATFORM + "." + TEST_DBNAME2 + "." + dbmsname;
		String metaDbms = new JdbcProperties().getProp(DATABASE_PROPERTIES, propPrefix + ".meta.dbms");
		String metaDriver = new JdbcProperties().getProp(DATABASE_PROPERTIES, propPrefix + ".meta.driver");
		assertContains(metaDbms, mr.getPlatformInfo());
		assertContains(metaDriver, mr.getPlatformInfo());
		// otros datos de DatabaseMetaData que estan en las versiones para .NET
		DatabaseMetaData md = dbt.getMetaData();
		assertEquals(dbmsproductname, md.getDatabaseProductName());
		assertFalse(mr.getDbmsType().toString().equals("postgres") ? !md.storesLowerCaseIdentifiers()
				: md.storesLowerCaseIdentifiers());
		assertFalse(md.storesLowerCaseQuotedIdentifiers());
		assertFalse(mr.getDbmsType().toString().equals("oracle") ? !md.storesUpperCaseIdentifiers()
				: md.storesUpperCaseIdentifiers());
		assertFalse(md.storesUpperCaseQuotedIdentifiers());
	}

	@Test
	public void testReaderTableDoesNotExist() {
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		try {
			mr.readTable("tablenotexists");
			fail("Se deberia haber producido una excepcion");
		} catch (SchemaException e) {
			assertEquals("schemareaderjdbc.settabletype: can't find table or view: tablenotexists",
					e.getMessage().toLowerCase());
		}
	}

	/**
	 * Comportamiento con identificadores mayusculas y minusculas
	 */
	@Test
	public void testCaseIdentifiers() throws SQLException {
		executeNotThrow(dbt, "drop table " + tablePrefix + "STab33");
		execute(dbt, "create table " + tablePrefix + "STab33 (coL33 int) ");
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		// leo indiferentemente en mayusculas o minusculas
		mr.readTable("sTab33");
		mr.readTable("StaB33");
		mr.readTable("stab33");
		mr.readTable("STAB33");
		// leo el nombre de la tabla y la columna
		mr.readTable("STab33");
		assertEqualsDBObj("STab33", mr.getTableName());
		assertEqualsDBObj("coL33", mr.getColumn(0).getColName());
		// y esta no existe
		try {
			mr.readTable("STabl33");
			fail("se deberia haber producido una excepcion");
		} catch (Exception e) {
			assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: STabl33".toLowerCase(),
					e.getMessage().toLowerCase());
		}
	}

	/**
	 * Comportamiento con identificadores con comillas
	 */
	@Test
	public void testQuotedIdentifiers() throws SQLException {
		executeNotThrow(dbt, "drop table " + tablePrefix + "\"Tab Cuatro\"");
		execute(dbt, "create table " + tablePrefix + "\"Tab Cuatro\" (coL44 int, \"Columna 55\" char(1)) ");
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		// leo indiferentemente en mayusculas o minusculas
		// En Oracle y postgres los quoted strings son case sensitives y por tanto solo
		// puedo localizar el primero
		mr.readTable("\"Tab Cuatro\"");
		if (mr.getDbmsType().isOracle() || mr.getDbmsType().isPostgres()) {
			try {
				mr.readTable("\"tab cuatro\"");
				fail("Deberia producirse una excepcion");
			} catch (Exception e) {
				assertContains("Can't find table or view", e.getMessage());
			}
			try {
				mr.readTable("\"taB cuatrO\"");
				fail("Deberia producirse una excepcion");
			} catch (Exception e) {
				assertContains("Can't find table or view", e.getMessage());
			}
			try {
				mr.readTable("\"TAB CUATRO\"");
				fail("Deberia producirse una excepcion");
			} catch (Exception e) {
				assertContains("Can't find table or view", e.getMessage());
			}
		} else {
			mr.readTable("\"tab cuatro\"");
			mr.readTable("\"taB cuatrO\"");
			mr.readTable("\"TAB CUATRO\"");
		}

		// tambien puedo leer si no hay comillas
		// Si hay espacios en blanco se considera quoted string tambien, por lo que el
		// funcionamiento sera el mismo que antes
		mr.readTable("Tab Cuatro");
		if (mr.getDbmsType().isOracle() || mr.getDbmsType().isPostgres()) {
			try {
				mr.readTable("tab cuatro");
				fail("Deberia producirse una excepcion");
			} catch (Exception e) {
				assertContains("Can't find table or view", e.getMessage());
			}
			try {
				mr.readTable("taB cuatrO");
				fail("Deberia producirse una excepcion");
			} catch (Exception e) {
				assertContains("Can't find table or view", e.getMessage());
			}
			try {
				mr.readTable("TAB CUATRO");
				fail("Deberia producirse una excepcion");
			} catch (Exception e) {
				assertContains("Can't find table or view", e.getMessage());
			}
		} else {
			mr.readTable("tab cuatro");
			mr.readTable("taB cuatrO");
			mr.readTable("TAB CUATRO");
		}

		// leo el nombre de la tabla y las columnas (como cualquier identificador, sin comillas)
		mr.readTable("\"Tab Cuatro\"");
		assertEquals("Tab Cuatro", mr.getTableName());
		assertEqualsDBObj("coL44", mr.getColumn(0).getColName()); // esta se pasa a mayusculas en oracle
		assertEquals("Columna 55", mr.getColumn(1).getColName()); // esta no porque era quoted
		// y esta no existe
		try {
			mr.readTable("\"Tabla Cuatro\"");
			fail("se deberia haber producido una excepcion");
		} catch (Exception e) {
			assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: \"Tabla Cuatro\"".toLowerCase(),
					e.getMessage().toLowerCase());
		}
	}

	/**
	 * Comportamiento con identificadores entre corchetes (sqlserver)
	 */
	@Test
	public void testQuotedIdentifiersWithBracket() throws SQLException {
		executeNotThrow(dbt, "drop table " + tablePrefix + "[Tab Cuatrob]");
		execute(dbt, "create table " + tablePrefix + "[Tab Cuatrob] (coL44 int, [Columna 55] char(1)) ");
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		// leo indiferentemente en mayusculas o minusculas
		mr.readTable("[tab cuatrob]");
		// tambien puedo leer si no hay comillas
		mr.readTable("tab cuatrob");
		// leo el nombre de la tabla y las columnas (como cualquier identificador, sin
		// comillas)
		mr.readTable("[Tab Cuatrob]");
		assertEquals("Tab Cuatrob", mr.getTableName());
		assertEqualsDBObj("coL44", mr.getColumn(0).getColName()); // esta se pasa a mayusculas en oracle
		assertEquals("Columna 55", mr.getColumn(1).getColName()); // esta no porque era quoted
	}

	/**
	 * Lectura de metadatos basicos de las FKs (no han sido comprobados en Test*SchemaMetadata)
	 */
	@Test
	public void testFkMetadata() throws SQLException {
		if ("netcore".equals(PLATFORM) && "sqlite".equals(dbmsname))
			return; // fk features not implemented in sqlite netcore
		execute(dbt, sTab1);
		execute(dbt, sTab2);
		SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		SchemaTable stab1 = mr.readTable("stab1");
		assertTrue(stab1.getColumns().get(0).isKey());
		assertFalse(stab1.getColumns().get(1).isKey());
		assertFalse(stab1.getColumns().get(2).isKey());
		
		assertFalse(stab1.getColumns().get(0).isForeignKey());
		assertFalse(stab1.getColumns().get(1).isForeignKey());
		assertFalse(stab1.getColumns().get(2).isForeignKey());

		assertEquals("", stab1.getColumns().get(0).getForeignKey());
		assertEquals("", stab1.getColumns().get(1).getForeignKey());
		assertEquals("", stab1.getColumns().get(1).getForeignKey());
		
		assertEquals("", stab1.getColumns().get(0).getForeignKeyName());
		assertEquals("", stab1.getColumns().get(1).getForeignKeyName());
		assertEquals("", stab1.getColumns().get(1).getForeignKeyName());
		
		assertEquals("", stab1.getColumns().get(0).getForeignTable());
		assertEquals("", stab1.getColumns().get(1).getForeignTable());
		assertEquals("", stab1.getColumns().get(1).getForeignTable());
		
		assertEquals("", stab1.getColumns().get(0).getForeignKeyColumn());
		assertEquals("", stab1.getColumns().get(1).getForeignKeyColumn());
		assertEquals("", stab1.getColumns().get(1).getForeignKeyColumn());
		
		SchemaTable stab2 = mr.readTable("stab2");
		assertTrue(stab2.getColumns().get(0).isKey());
		assertTrue(stab2.getColumns().get(1).isKey());
		assertFalse(stab2.getColumns().get(2).isKey());
		
		assertFalse(stab2.getColumns().get(0).isForeignKey());
		assertTrue(stab2.getColumns().get(1).isForeignKey());
		assertFalse(stab2.getColumns().get(2).isForeignKey());

		assertEquals("", stab2.getColumns().get(0).getForeignKey());
		assertEquals(asStored("stab1.col11"), stab2.getColumns().get(1).getForeignKey());
		assertEquals(asStored("stab1.col11"), stab2.getColumns().get(1).getForeignKey());
		
		assertEquals("", stab2.getColumns().get(0).getForeignKeyName());
		assertEquals(asStored("ref_stab1_col11"), stab2.getColumns().get(1).getForeignKeyName());
		assertEquals(asStored("ref_stab1_col11"), stab2.getColumns().get(1).getForeignKeyName());
		
		assertEquals("", stab2.getColumns().get(0).getForeignTable());
		assertEquals(asStored("stab1"), stab2.getColumns().get(1).getForeignTable());
		assertEquals(asStored("stab1"), stab2.getColumns().get(1).getForeignTable());
		
		assertEquals("", stab2.getColumns().get(0).getForeignKeyColumn());
		assertEquals(asStored("col11"), stab2.getColumns().get(1).getForeignKeyColumn());
		assertEquals(asStored("col11"), stab2.getColumns().get(1).getForeignKeyColumn());
	}
	/**
	 * Lectura avanzada de FKs, leyendo tambien las incoming FKs
	 */
	@Test
	public void testFkIncomingAndOutgoing() throws SQLException {
		if ("netcore".equals(PLATFORM) && "sqlite".equals(dbmsname))
			return; // fk features not implemented in sqlite netcore
		execute(dbt, sTab1);
		execute(dbt, sTab2);
		SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.setUseIncomingFKs(true); // para que tambien obtenga claves ajenas entrantes a las tablas
		// Busca fks salientes de stab2 (una) y stab1 (cero)
		// comprueba tambien las pks
		SchemaTable stab2 = mr.readTable("stab2");
		assertEquals(1, stab2.getFKs().size());
		
		SchemaForeignKey fk = stab2.getFKs().get(0);
		assertEquals((myCatalogSchema2 + "stab2 CONSTRAINT ref_stab1_col11 FOREIGN KEY(col22) REFERENCES "
				+ myCatalogSchema2 + "stab1(col11)").toLowerCase(), fk.toString().toLowerCase());
		assertEquals("ref_stab1_col11", fk.getName().toLowerCase());
		assertEquals("stab2", fk.getFromTable().getName().toLowerCase());
		assertEquals("stab2", fk.getFromTableIdentifier().getTab().toLowerCase());
		assertEquals("col22", String.join(",", fk.getFromColumnNames()).toLowerCase());
		assertEquals("col11", String.join(",", fk.getToColumnNames()).toLowerCase());
		assertEquals("stab1", fk.getToTableIdentifier().getTab().toLowerCase());

		SchemaTable stab1 = mr.readTable("stab1");
		assertEquals(0, stab1.getFKs().size());
		// Busca fks entrantes de las mismas tablas
		// en este caso no compara las columnas pues no se almacenan
		assertEquals(1, stab1.getIncomingFKs().size());
		fk = stab1.getIncomingFKs().get(0);
		// la comparacion del string de la fk no incluye columnas puesto que en estas no
		// se guarda esta informacion
		assertEquals((myCatalogSchema2 + "stab2 CONSTRAINT ref_stab1_col11 FOREIGN KEY() REFERENCES " + myCatalogSchema2
				+ "stab1()").toLowerCase(), fk.toString().toLowerCase());
		assertEquals("ref_stab1_col11", fk.getName().toLowerCase());
		assertEquals("stab2", fk.getFromTableIdentifier().getTab().toLowerCase());
		assertEquals("stab1", fk.getToTableIdentifier().getTab().toLowerCase());

		assertEquals(0, stab2.getIncomingFKs().size());
	}

	/**
	 * Obtencion de una lista de tablas dependientes de las fks (prueba basica, la
	 * logica de recursividad se prueba en la ordenacion de fks)
	 */
	@Test
	public void testFkDependentTables() throws SQLException {
		if ("netcore".equals(PLATFORM) && "sqlite".equals(dbmsname))
			return; // fk features not implemented in sqlite netcore
		execute(dbt, sTab1);
		execute(dbt, sTab2);
		SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		// Busca tabla 2, se debe encontrar tabla 1
		List<String> tables = new ArrayList<String>();
		tables.add("stab2");
		tables = mr.getTableListAndDependent(tables);
		assertEquals(2, tables.size());
		assertEquals("stab2", tables.get(0));
		assertEqualsDBObj("stab1", (tables.get(1))); // se compara con assertEqualsDBObj pues stab1 provendra del modelo
		// busca tabla 1, no encuentra nada pues no tiene fks
		tables = new ArrayList<String>();
		tables.add("stab1");
		tables = mr.getTableListAndDependent(tables);
		assertEquals(1, tables.size());
		assertEquals("stab1", tables.get(0));
	}

}
