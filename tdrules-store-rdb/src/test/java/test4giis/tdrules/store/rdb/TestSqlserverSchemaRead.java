package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.tdrules.store.rdb.JdbcProperties;
import giis.tdrules.store.rdb.SchemaForeignKey;
import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.tdrules.store.rdb.SchemaTable;

/**
 * Reading basic metadata (columns, fks, views...)
 */
public class TestSqlserverSchemaRead extends Base {
	// Nombres de catalogo utilizados para el acceso a los metadatos
	// en sqlserver no se utilizan ya que se accede a las tablas de una base de datos
	protected String catalog = null;
	protected String schema = null;
	protected Connection dbt;

	// prefijo de catalogo y esquema para tablas completamente cualificadas
	protected String myCatalogSchema1 = TEST_DBNAME1.toLowerCase() + ".dbo.";
	protected String myCatalogSchema2 = TEST_DBNAME2.toLowerCase() + ".dbo.";

	// definiciones de tipos de datos cuyo nombre es diferente en otras plataformas
	protected String INT = "int";
	protected String IDENTITY = "int identity";
	protected int INT_precision = 0;
	protected String DECIMAL = "decimal";
	protected String BIT = "bit";
	protected int BIT_precision = 0;
	protected String VARCHAR = "varchar";
	protected String stringConcat = "+";
	protected String concatCharType = "varchar";
	protected int concatCharSize = 14;

	// creacion de tablas para pruebas
	protected String sTab1 = "create table " + tablePrefix + "stab1 (col11 int not null primary key, "
			+ "col12 char(3) not null, col13 varchar(16))";
	// tabla con dos fks, una a una clave y otra a una no clave
	protected String sTab2 = "create table " + tablePrefix + "stab2 (col21 decimal(8,4), col22 int default (22), "
			+ "col23 bit, " + "PRIMARY KEY (col21,col22), "
			+ "CONSTRAINT ref_stab1_col11 FOREIGN KEY(col22) REFERENCES stab1(col11) " + ")";

	// tabla con una fk no clave, y pk identity
	protected String getsTab3() {
		return "create table " + tablePrefix + "stab3 (colpk " + IDENTITY + ", colfk int, " + "PRIMARY KEY (colpk), "
				+ "CONSTRAINT ref_stab1_col11fk FOREIGN KEY(colfk) REFERENCES stab1(col11) " + ")";
	}

	// dos vistas, simple/compleja con/sin argumentos
	protected String sView1 = "create view " + tablePrefix + "view1 (v11,v12,v13) as "
			+ "select col11,col12,col13 from stab1";

	protected String getsView2() {
		return "create view " + tablePrefix + "view2 as " + "select t2.col21, t2.col21*col11 as v22, "
				+ "cast(t1.col12 as varchar(13)) as v23 ,t1.col12 " + stringConcat + " 'hello world' as v24 "
				+ "from stab1 t1 left join stab2 t2 on t2.col22=t1.col11";
	}

	protected void createTablesAndViews() throws SQLException {
		Connection dbt = getConnection(TEST_DBNAME2); // modo conectado para que se cierre la conexion
		execute(dbt, sTab1);
		execute(dbt, sTab2);
		execute(dbt, getsTab3());
		execute(dbt, sView1);
		execute(dbt, getsView2());
		dbt.close();
	}

	protected void dropTablesAndViews() throws SQLException {
		Connection dbt = getConnection(TEST_DBNAME2); // modo conectado para que se cierre la conexion
		executeNotThrow(dbt, "drop view view2");
		executeNotThrow(dbt, "drop view view1");
		executeNotThrow(dbt, "drop table stab33");
		executeNotThrow(dbt, "drop table stab3");
		executeNotThrow(dbt, "drop table stab2");
		executeNotThrow(dbt, "drop table stab1");
		dbt.close();
	}

	@Before
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
		java.sql.DatabaseMetaData md = dbt.getMetaData();
		assertEquals(dbmsproductname, md.getDatabaseProductName());
		assertFalse(mr.getDbmsType().toString().equals("postgres") ? !md.storesLowerCaseIdentifiers()
				: md.storesLowerCaseIdentifiers());
		assertFalse(md.storesLowerCaseQuotedIdentifiers());
		assertFalse(mr.getDbmsType().toString().equals("oracle") ? !md.storesUpperCaseIdentifiers()
				: md.storesUpperCaseIdentifiers());
		assertFalse(md.storesUpperCaseQuotedIdentifiers());
	}

	/**
	 * List de tablas y/o vistas, no entra en los metadatos de cada una
	 */
	@Test
	public void testReadListTableAndView() throws SQLException {
		createTablesAndViews();
		// comprueba los nombres, de estas tablas que se han creado (puede haber mas)
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		List<String> lst = mr.getTableList(true, false); // tablas
		String lstString = "," + listToString(lst, ",").toLowerCase() + ",";
		assertContains(",stab1,", lstString);
		assertContains(",stab2,", lstString);
		assertContains(",stab3,", lstString);
		assertDoesNotContain(",view1,", lstString);

		lst = mr.getTableList(false, true); // vistas
		lstString = "," + listToString(lst, ",").toLowerCase() + ",";
		assertContains(",view1,", lstString);
		assertContains(",view2,", lstString);
		assertDoesNotContain(",stab1,", lstString);

		lst = mr.getTableList(true, true); // vistas
		lstString = "," + listToString(lst, ",").toLowerCase() + ",";
		assertContains(",stab1,", lstString);
		assertContains(",view1,", lstString);
	}

	/**
	 * Lectura de metadatos de columnas en tablas, lectura secuencial de varias tablas
	 * con comprobacion detallada de los metadatos
	 */
	@Test
	public void testReadMetadataInTables() throws SQLException {
		createTablesAndViews();
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.readTable("stab1");
		assertEquals(true, mr.isTable());
		assertEquals(false, mr.isView());
		assertEqualsDBObj("stab1", mr.getTableName());
		assertEqualsDBObj("col11", mr.getColumn(0).getColName());
		assertEqualsDBObj("col11", mr.getColumn(0).toString());
		assertEqualsDBObj(this.INT, mr.getColumn(0).getDataType());
		assertEquals(this.INT_precision, mr.getColumn(0).getColSize());
		assertEquals(0, mr.getColumn(0).getDecimalDigits());
		assertEquals("", mr.getColumn(0).getForeignKey());
		assertEquals("", mr.getColumn(0).getCheckInConstraint());
		assertEquals(true, mr.getColumn(0).isKey());
		assertEquals(true, mr.getColumn(0).isNotNull());
		assertEqualsDBObj("col12", mr.getColumn(1).getColName());
		assertEqualsDBObj("char", mr.getColumn(1).getDataType());
		assertEquals(3, mr.getColumn(1).getColSize());
		assertEquals(0, mr.getColumn(1).getDecimalDigits());
		assertEquals(false, mr.getColumn(1).isKey());
		assertEquals(true, mr.getColumn(1).isNotNull());
		assertEquals("", mr.getColumn(1).getForeignKey());
		assertEquals("", mr.getColumn(1).getCheckInConstraint());
		assertEqualsDBObj("col13", mr.getColumn(2).getColName());
		assertEqualsDBObj(this.VARCHAR, mr.getColumn(2).getDataType());
		// comprueba como se ven los string al pasar a sql
		assertEquals("'Abc'", mr.getColumn(1).getAsSqlString("Abc"));
		assertEquals("'A''c'", mr.getColumn(1).getAsSqlString("A'c"));
		assertEquals("NULL", mr.getColumn(1).getAsSqlString(null));

		assertEquals(16, mr.getColumn(2).getColSize());
		assertEquals(0, mr.getColumn(2).getDecimalDigits());
		assertEquals(false, mr.getColumn(2).isKey());
		assertEquals(false, mr.getColumn(2).isNotNull());
		assertEquals("", mr.getColumn(2).getForeignKey());
		assertEquals("", mr.getColumn(2).getCheckInConstraint());
	}
	@Test
	public void testReadMetadataInTables2() throws SQLException {
		createTablesAndViews();
		// Segunda tabla
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.readTable("stab2");
		assertEquals(true, mr.isTable());
		assertEquals(false, mr.isView());
		assertEqualsDBObj("stab2", mr.getTableName());
		assertEqualsDBObj("col21", mr.getColumn(0).getColName());
		assertEqualsDBObj(this.DECIMAL, mr.getColumn(0).getDataType());
		assertEquals(8, mr.getColumn(0).getColSize());
		assertEquals(4, mr.getColumn(0).getDecimalDigits());
		assertEquals(true, mr.getColumn(0).isKey());
		assertEquals(true, mr.getColumn(0).isNotNull());
		assertEquals("", mr.getColumn(0).getForeignKey());
		assertEquals("", mr.getColumn(0).getCheckInConstraint());
		assertEquals("", mr.getColumn(0).getDefaultValue());
		assertEqualsDBObj("col22", mr.getColumn(1).getColName());
		assertEqualsDBObj(this.INT, mr.getColumn(1).getDataType());
		assertEquals(this.INT_precision, mr.getColumn(1).getColSize());
		assertEquals(0, mr.getColumn(1).getDecimalDigits());
		assertEquals(true, mr.getColumn(1).isKey());
		assertEquals(true, mr.getColumn(1).isNotNull());
		assertEqualsDBObj("stab1.col11", mr.getColumn(1).getForeignKey());
		assertEqualsDBObj("ref_stab1_col11", mr.getColumn(1).getForeignKeyName());
		assertEquals("", mr.getColumn(1).getCheckInConstraint());
		assertEquals("22", mr.getColumn(1).getDefaultValue());
		assertEqualsDBObj("col23", mr.getColumn(2).getColName());
		assertEqualsDBObj(this.BIT, mr.getColumn(2).getDataType());
		assertEquals(this.BIT_precision, mr.getColumn(2).getColSize());
		assertEquals(0, mr.getColumn(2).getDecimalDigits());
		assertEquals(false, mr.getColumn(2).isKey());
		assertEquals(false, mr.getColumn(2).isNotNull());
		assertEquals("", mr.getColumn(2).getForeignKey());
		assertEquals("", mr.getColumn(2).getCheckInConstraint());
		assertEquals("", mr.getColumn(2).getDefaultValue());

		// tercera tabla, fk que no es pk y pk identity, solo compruebo claves
		mr.readTable("stab3");
		assertEquals(true, mr.isTable());
		assertTrue(mr.getColumn(0).isKey());
		assertFalse(mr.getColumn(0).isForeignKey());
		if (!mr.getDbmsType().isOracle())
			assertTrue(mr.getColumn(0).isAutoIncrement());
		else
			assertFalse(mr.getColumn(0).isAutoIncrement());
		assertFalse(mr.getColumn(1).isKey());
		assertTrue(mr.getColumn(1).isForeignKey());
		assertFalse(mr.getColumn(1).isAutoIncrement());
	}

	@Test
	public void testReadMetadataInViews() throws SQLException {
		createTablesAndViews();
		SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.readTable("view1");
		assertEquals(false, mr.isTable());
		assertEquals(true, mr.isView());
		assertEqualsDBObj("view1", mr.getTableName());
		assertEqualsDBObj("v11", mr.getColumn(0).getColName());
		assertEqualsDBObj(this.INT, mr.getColumn(0).getDataType());
		// el number en oracle se ve con precision 38
		assertEquals((mr.getDbmsType().isOracle() ? 38 : this.INT_precision), mr.getColumn(0).getColSize());
		assertEquals(0, mr.getColumn(0).getDecimalDigits());
		assertEquals(false, mr.getColumn(0).isKey());
		assertEquals("", mr.getColumn(0).getForeignKey());
		assertEquals("", mr.getColumn(0).getCheckInConstraint());
		assertEquals(!mr.isPostgres(), mr.getColumn(0).isNotNull()); // postgres no puede determinar si es notnull
		assertEqualsDBObj("v12", mr.getColumn(1).getColName());
		assertEqualsDBObj("char", mr.getColumn(1).getDataType());
		assertEquals(3, mr.getColumn(1).getColSize());
		assertEquals(0, mr.getColumn(1).getDecimalDigits());
		assertEquals(false, mr.getColumn(1).isKey());
		assertEquals(!mr.isPostgres(), mr.getColumn(1).isNotNull()); // postgres no puede determinar si es notnull
		assertEquals("", mr.getColumn(1).getForeignKey());
		assertEquals("", mr.getColumn(1).getCheckInConstraint());
		assertEqualsDBObj("v13", mr.getColumn(2).getColName());
		assertEqualsDBObj(this.VARCHAR, mr.getColumn(2).getDataType());
		assertEquals(16, mr.getColumn(2).getColSize());
		assertEquals(0, mr.getColumn(2).getDecimalDigits());
		assertEquals(false, mr.getColumn(2).isKey());
		assertEquals(false, mr.getColumn(2).isNotNull());
		assertEquals("", mr.getColumn(2).getForeignKey());
		assertEquals("", mr.getColumn(2).getCheckInConstraint());
	}
	@Test
	public void testReadMetadataInViews2() throws SQLException {
		createTablesAndViews();
		// Segunda vista, ya no compruebo foreign ni constraint
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.readTable("view2");

		assertEquals(false, mr.isTable());
		assertEquals(true, mr.isView());
		assertEqualsDBObj("view2", mr.getTableName());
		assertEqualsDBObj("col21", mr.getColumn(0).getColName()); // el nombre de la columna base pues no se especifico
																	// alias
		assertEqualsDBObj(this.DECIMAL, mr.getColumn(0).getDataType());
		assertEquals(8, mr.getColumn(0).getColSize());
		assertEquals(4, mr.getColumn(0).getDecimalDigits());
		assertEquals(false, mr.getColumn(0).isKey()); // col21 es key en la tabla base, pero de la vista no salen claves
		assertEquals(false, mr.getColumn(0).isNotNull()); // nullable pues el dato base lo es
		assertEqualsDBObj("v22", mr.getColumn(1).getColName());
		assertEqualsDBObj(this.DECIMAL, mr.getColumn(1).getDataType()); // resultado de decimal y de int es decimal

		// aunque el tipo de dato era decimal(8,4) en la vista aparece como 19,4 en
		// SQLServer y 0 en Oracle y postgres
		assertEquals((mr.getDbmsType().isOracle() || mr.getDbmsType().isPostgres() ? 0 : 19),
				mr.getColumn(1).getColSize());
		assertEquals((mr.getDbmsType().isOracle() || mr.getDbmsType().isPostgres() ? 0 : 4),
				mr.getColumn(1).getDecimalDigits());
		assertEquals(false, mr.getColumn(1).isKey());
		assertEquals(false, mr.getColumn(1).isNotNull()); // uno es nullable y otro no, es nullable

		assertEqualsDBObj("v23", mr.getColumn(2).getColName());
		assertEqualsDBObj(this.VARCHAR, mr.getColumn(2).getDataType()); // cast de char a varchar es varchar
		assertEquals(13, mr.getColumn(2).getColSize());
		assertEquals(0, mr.getColumn(2).getDecimalDigits());
		assertEquals(false, mr.getColumn(2).isKey());
		assertEquals(false, mr.getColumn(2).isNotNull());

		assertEqualsDBObj("v24", mr.getColumn(3).getColName());
		assertEqualsDBObj(this.concatCharType, mr.getColumn(3).getDataType());
		assertEquals(this.concatCharSize, mr.getColumn(3).getColSize()); // el tamanyo es 14 (hello world tiene 11 mas
																			// los 3 del char)
		assertEquals(0, mr.getColumn(3).getDecimalDigits());
		assertEquals(false, mr.getColumn(3).isKey());
		// esta concatenacion es no nullable en sqlserver, pero nullable en oracle
		assertEquals((mr.getDbmsType().isOracle() || mr.getDbmsType().isPostgres() ? false : true),
				mr.getColumn(3).isNotNull());
	}

	/**
	 * Consulta de la query de creacion de una vista
	 */
	@Test
	public void testReaderViewDescription() throws SQLException {
		this.createTablesAndViews();
		SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.readTable("View1");
		assertTrue(mr.isView());
		String expectedView = asStored("create view " + tablePrefix + "view1 (v11,v12,v13) as")
				+ " select col11,col12,col13 from stab1";
		if (mr.isPostgres())
			expectedView = "CREATE VIEW view1 (v11,v12,v13) AS SELECT stab1.col11 AS v11,\n"
					+ "    stab1.col12 AS v12,\n" + "    stab1.col13 AS v13\n" + "   FROM stab1;";
		assertEquals(expectedView, mr.getQuery(mr.getCurrentTable()));

		// comprueba ante cambio de la vista, mayusculas, minusculas y espacios, solo en
		// sqlserver
		// en oracle o postgres no se usa alter view con esta sintaxis
		if (mr.isSQLServer()) {
			execute(dbt, "  alter   view   view1   As   Select   col13, col12, col11   from   stab1  ");
			mr.readTable("View1");
			assertTrue(mr.isView());
			assertEquals("col13", mr.getColumn(0).getColName());
			assertEquals("col12", mr.getColumn(1).getColName());
			assertEquals("col11", mr.getColumn(2).getColName());
			assertEquals(asStored("CREATE   view   " + tablePrefix + "view1   As")
					+ "   Select   col13, col12, col11   from   stab1", mr.getQuery(mr.getCurrentTable()));
		}

		// Las vistas que se guardan en INFORMATION_SCHEMA tienen un limite de 4000
		// caracteres, si son mayores
		// se guarda null. Para cada SGBD se deberan usar los metodos especificos de la
		// plataforma para evitar este problema
		execute(dbt, "drop view view1");
		String createViewMain = "create view view1 (col11,col12,col13) as ";
		String createViewSql = "select col11,col12,col13 from stab1";
		String addView = " \nunion all select col11,col12,col13 from stab1";
		int numRepeats = 10000 / addView.length();
		for (int i = 0; i < numRepeats; i++)
			createViewSql += addView;
		execute(dbt, createViewMain + createViewSql);
		mr.readTable("View1");
		assertTrue(mr.isView());
		assertEquals("col11", mr.getColumn(0).getColName().toLowerCase());
		assertEquals("col12", mr.getColumn(1).getColName().toLowerCase());
		assertEquals("col13", mr.getColumn(2).getColName().toLowerCase());
		createViewMain = asStored(createViewMain);
		if (mr.isPostgres()) {
			createViewMain = "CREATE VIEW view1 (col11,col12,col13) AS ";
			createViewSql = "SELECT stab1.col11,\n" + "    stab1.col12,\n" + "    stab1.col13\n" + "   FROM stab1";
			addView = "\nUNION ALL\n" + " SELECT stab1.col11,\n" + "    stab1.col12,\n" + "    stab1.col13\n"
					+ "   FROM stab1";
			for (int i = 0; i < numRepeats; i++)
				createViewSql += addView;
			createViewSql += ";";
		}
		assertEquals(createViewMain + createViewSql, mr.getQuery(mr.getCurrentTable()));
	}

	@Test
	public void testReaderTableDoesNotExist() {
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		try {
			mr.readTable("tablenotexists");
			fail("Se deberia haber producido una excepcion");
		} catch (giis.tdrules.store.rdb.SchemaException e) {
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
	 * Lectura de FKs
	 */
	@Test
	public void testGetFKs() throws SQLException {
		execute(dbt, sTab1);
		execute(dbt, sTab2);
		SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		mr.setUseIncomingFKs(true); // para que tambien obtenga claves ajenas entrantes a las tablas
		// Busca fks salientes de stab2 (una) y stab1 (cero)
		SchemaTable stab2 = mr.readTable("stab2");
		assertEquals(1, stab2.getFKs().size());
		SchemaForeignKey fk = stab2.getFKs().get(0);
		assertEquals((myCatalogSchema2 + "stab2 CONSTRAINT ref_stab1_col11 FOREIGN KEY(col22) REFERENCES "
				+ myCatalogSchema2 + "stab1(col11)").toLowerCase(), fk.toString().toLowerCase());
		assertEquals("ref_stab1_col11", fk.getName().toLowerCase());
		assertEquals("stab2", fk.getFromTable().getName().toLowerCase());
		assertEquals("stab2", fk.getFromTableIdentifier().getTab().toLowerCase());
		assertEquals("col22", listToString(fk.getFromColumnNames(), ",").toLowerCase());
		assertEquals("col11", listToString(fk.getToColumnNames(), ",").toLowerCase());
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
	public void testDependentFKs() throws SQLException {
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
