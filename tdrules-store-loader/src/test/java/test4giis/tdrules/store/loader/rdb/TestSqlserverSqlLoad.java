package test4giis.tdrules.store.loader.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import giis.portable.util.JavaCs;
import giis.tdrules.client.rdb.DbSchemaApi;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.gen.RandomAttrGen;
import giis.tdrules.store.loader.sql.SqlLocalAdapter;
import giis.tdrules.store.loader.sql.SqlLiveAdapter;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.tdrules.store.stypes.StoreType;

/**
 * Data load to a Relational Database through sql
 */
public class TestSqlserverSqlLoad extends Base {
	protected Connection db;
	protected StoreType dbms;

	@Before
	@Override
	public void setUp() throws SQLException {
		super.setUp();
		dropTablesAndViews();
		db = getConnection(TEST_DBNAME);
		dbms = new SchemaReaderJdbc(db).getDbmsType();
	}

	@After
	public void tearDown() throws SQLException {
		db.close();
	}

	protected String getDate1() {
		return "2008-09-01";
	}

	protected String getDate2() {
		return "'2008-09-20'";
	}

	protected void createTablesAndViews() throws SQLException {
		Connection dbt = getConnection(TEST_DBNAME);
		execute(dbt, "create table Gg0 (Pk1 int, I1 int, primary key(Pk1))");
		// tabla basica con datos pk, fk y enteros/characteres con alguno nullable,
		// fechas en dos formatos, date+time y date, uno nullable y otro no
		execute(dbt, "create table Gg1 (Pk1 int, I1 int, I2 int not null, C1 varchar(8), C2 char(1) not null, D1 "
				+ this.dbms.getDataTypeDatetime() + ", D2 " + "DATE" + " not null)");
		// tabla con otros tipos de datos para pruebas adicionales
		execute(dbt,
				"create table Gg2 (Pk1 int, c1 char(1) not null, ccin char(3) not null check (ccin in ('SSS','NNN')))");
		// una tabla maestra con identity y una detalle que apunta a la maestra
		String identityType = this.dbms.getDataTypeIdentity("int") + " " + this.dbms.getDataTypeIdentitySuffix();
		execute(dbt, "create table Ggm (Pk1 " + identityType + ", I1 int, I2 int not null, primary key(Pk1))");
		execute(dbt, "create table Ggd (Pk1 " + identityType + ", Fk1 int not null, I1 int, I2 int not null, primary key(Pk1))");
		execute(dbt, "alter table  Ggd add constraint fk_ggd_ggm foreign key (Fk1) references ggm(pk1)");
		dbt.close();
	}

	protected void createTablesAndViewsMultipleRelations(boolean useCircularRelations) throws SQLException {
		// relaciones (detalle-maestro): gg0-gg1-ggd-(ggm-gg2-(gg3 | ggd...) | gg1)
		// cadena cirucular ggd-ggm-gg2-ggd
		// relacion recursiva gg1-ggd-gg2
		Connection dbt = getConnection(TEST_DBNAME);
		execute(dbt, "create table Gg3 (Pk1 int, primary key(Pk1), Fk1 int)");
		execute(dbt, "create table Gg2 (Pk1 int, primary key(Pk1), Fk1 int, Fk2 int)");
		String identityType = this.dbms.getDataTypeIdentity("int") + " " + this.dbms.getDataTypeIdentitySuffix();
		execute(dbt, "create table Ggm (Pk1 " + identityType + ", primary key(Pk1), Fk1 int)");
		execute(dbt, "create table Ggd (Pk1 " + identityType + ", primary key(Pk1), Fk1 int, Fk2 int)");
		execute(dbt, "create table Gg1 (Pk1 int, primary key(Pk1), Fk1 int)");
		execute(dbt, "create table Gg0 (Pk1 int, primary key(Pk1), Fk1 int)");
		execute(dbt, "alter table gg2 add constraint FK_GG2_GG3 foreign key(Fk2) references gg3(pk1)");
		execute(dbt, "alter table gg2 add constraint FK_GG2_GGD foreign key(Fk1) references ggd(pk1)");
		execute(dbt, "alter table ggm add constraint FK_GGM_GG2 foreign key(Fk1) references gg2(pk1)");
		execute(dbt, "alter table ggd add constraint FK_GGD_GG1 foreign key(Fk2) references gg1(pk1)");
		execute(dbt, "alter table gg0 add constraint FK_GG0_GG1 foreign key(Fk1) references gg1(pk1)");
		if (useCircularRelations) {
			execute(dbt, "alter table gg1 add constraint FK_GG1_GGD foreign key(Fk1) references ggd(pk1)");
			execute(dbt, "alter table ggd add constraint FK_GGD_GGM foreign key(Fk1) references ggm(pk1)");
		}
		dbt.close();
	}

	protected void dropTablesAndViews() throws SQLException {
		Connection dbt = getConnection(TEST_DBNAME); // modo conectado para que se cierre la conexion
		executeNotThrow(dbt, "alter table gg2 drop constraint FK_GG2_GG3");
		executeNotThrow(dbt, "alter table gg2 drop constraint FK_GG2_GGD");
		executeNotThrow(dbt, "alter table ggm drop constraint FK_GGM_GG2");
		executeNotThrow(dbt, "alter table ggd drop constraint FK_GGD_GG1");
		executeNotThrow(dbt, "alter table gg0 drop constraint FK_GG0_GG1");
		executeNotThrow(dbt, "alter table gg1 drop constraint FK_GG1_GGD");
		executeNotThrow(dbt, "alter table ggd drop constraint FK_GGD_GGM");

		executeNotThrow(dbt, "drop table Gg3");
		executeNotThrow(dbt, "drop table Ggd");
		executeNotThrow(dbt, "drop table Ggm");
		executeNotThrow(dbt, "drop table Gg2");
		executeNotThrow(dbt, "drop table Gg1");
		executeNotThrow(dbt, "drop table Gg0");
		dbt.close();
	}

	protected DataLoader getLiveGenerator() {
		DbSchemaApi api = new DbSchemaApi(db);
		TdSchema schema = api.getSchema(Arrays.asList(new String[] { "gg1", "ggm", "ggd" }));
		IDataAdapter dataAdapter = new SqlLiveAdapter(db, schema.getStoretype());
		return new DataLoader(schema, dataAdapter);
	}

	protected DataLoader getSqlGenerator() {
		DbSchemaApi api = new DbSchemaApi(db);
		TdSchema schema = api.getSchema(Arrays.asList(new String[] { "gg1", "ggm", "ggd" }));
		IDataAdapter dataAdapter = new SqlLocalAdapter(schema.getStoretype());
		return new DataLoader(schema, dataAdapter);
	}

	/**
	 * Creacion de las sql ante diferentes tipos de datos (usa comillas solo para
	 * datos tipo char y date). Comprobacion del sql generado
	 */
	@Test
	public void testGenerateRowDataTypes() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getLiveGenerator();
		String sql1 = dtg.load("gg1", "", "");
		String sql2 = dtg.load("gg1", "", "");
		String sql3 = dtg.load("gg1", "", "");
		String day6 = this.dbms.getSqlDatetimeLiteral("2007-01-06", "");
		String day7 = this.dbms.getSqlDatetimeLiteral("2007-01-07", "");
		String day8 = this.dbms.getSqlDatetimeLiteral("2007-01-08", "");
		String day9 = this.dbms.getSqlDatetimeLiteral("2007-01-09", "");
		String expected = "INSERT INTO gg1 (Pk1, I1, I2, C1, C2, D1, D2) VALUES (1, 2, 3, '4', '5', " + day6 + ", "
				+ day7 + ")" + "\nINSERT INTO gg1 (Pk1, I1, I2, C1, C2, D1, D2) VALUES (101, 102, 103, '104', '5', "
				+ day7 + ", " + day8 + ")"
				+ "\nINSERT INTO gg1 (Pk1, I1, I2, C1, C2, D1, D2) VALUES (201, 202, 203, '204', '5', " + day8 + ", "
				+ day9 + ")";
		assertEquals(expected.toLowerCase(), (sql1 + "\n" + sql2 + "\n" + sql3).toLowerCase());
		// Compara el script completo
		assertEquals(expected.toLowerCase(), dtg.getDataAdapter().getAllAsString().toLowerCase());

		// Reset, debe producir la misma secuencia
		dtg.reset();
		dtg.load("gg1", "", "");
		dtg.load("gg1", "", "");
		dtg.load("gg1", "", "");
		assertEquals(expected.toLowerCase(), dtg.getDataAdapter().getAllAsString().toLowerCase());
	}

	// Los test de oracle incluyen testCheckIn
	// De momento no se pueden implementar aqui porque la comprobacion del checkIn solo esta disponible en oracle
	// El resto de BD generan en el esquema todas las constraints, pero con sintaxis diferentes
	// que no pueden ser trasladadas de forma directa al atributo checkIn del esquema

	/**
	 * Creacion de valores especificados por el usuario, valores no especificados
	 * que se generan aleatoriamente si son not null y valores no especificados que
	 * no se generan si son nullables Comprobacion de los datos insertados en bd
	 */
	@Test
	public void testGenerateRowUserSpecified0OnlyValue() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getLiveGenerator().setGenerateNullable(false);
		dtg.load("gg1", "pk1,i1,c1,d1", "1,10,'abc'," + getDate1());
		dtg.load(" gg1 ", " pk1 , i2 , c2 , d2 ", " 2 , 20 , x , " + getDate2());
		dtg.load("gg1", "", ""); // una tercera sin indicar ningun valor
		assertGenerateRowUserSpecified();
	}

	@Test
	public void testGenerateRowUserSpecified1KeyValue() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getLiveGenerator().setGenerateNullable(false);
		dtg.load("gg1", "pk1=1,i1=10,c1='abc',d1=" + getDate1());
		dtg.load("gg1", " pk1 = 2 , i2 = 20 , c2 = 'x' , d2=" + getDate2());
		dtg.load("gg1", ""); // una tercera sin indicar ningun valor
		assertGenerateRowUserSpecified();
	}

	@Test
	public void testGenerateRowUserSpecified2UsingDate() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getLiveGenerator().setGenerateNullable(false);
		// intercambia los tipos de datos datetime por date, manteniendo el nullable
		execute(db, "alter table gg1 drop column d1");
		execute(db, "alter table gg1 drop column d2");
		execute(db, "alter table gg1 add d1 DATE");
		execute(db, "alter table gg1 add d2 " + this.dbms.getDataTypeDatetime() + " NOT NULL");
		dtg.load("gg1", "pk1=1,i1=10,c1='abc',d1=" + getDate1());
		dtg.load("gg1", " pk1 = 2 , i2 = 20 , c2 = 'x' , d2=" + getDate2());
		dtg.load("gg1", ""); // una tercera sin indicar ningun valor
		assertGenerateRowUserSpecified();
	}

	public void assertGenerateRowUserSpecified() throws SQLException {
		ResultSet rs = query(db, "select * from gg1");
		assertTrue(rs.next());
		assertEquals(1, rs.getInt("pk1")); // primer valor generado, vale uno pues es el primer serial
		assertEquals(10, rs.getInt("i1")); // los valores introducidos tienen los valores especificados
		assertEquals("abc", rs.getString("c1"));
		assertNotEquals(0, rs.getInt("i2")); // los valores no introducidos tienen valores no nulos pues son not null
		assertFalse(rs.wasNull());
		assertNotEquals("", rs.getString("c2"));
		assertFalse(rs.wasNull());
		assertEquals("2008-09-01", JavaCs.getIsoDate(rs.getDate("D1")).substring(0, 10));
		assertNotEquals("xxxxxx", JavaCs.getIsoDate(rs.getDate("D2")).substring(0, 10)); // valor generado, no null
		assertFalse(rs.wasNull());

		assertTrue(rs.next());
		assertEquals(2, rs.getInt("pk1"));
		assertEquals(20, rs.getInt("i2"));
		assertEquals("x", rs.getString("c2"));
		assertEquals(0, rs.getInt("i1")); // los valores no introducidos tienen valores nulos pues son nullables
		assertTrue(rs.wasNull());
		assertNull(rs.getString("c1"));
		assertTrue(rs.wasNull());
		assertEquals("2008-09-20", JavaCs.getIsoDate(rs.getDate("D2")).substring(0, 10));
		rs.getDate("D1"); // valor no generado, null
		assertTrue(rs.wasNull());

		assertTrue(rs.next()); // hay una terdera fila
		assertEquals(0, rs.getInt("pk1")); // los valores no introducidos tienen valores nulos pues son nullables
		assertTrue(rs.wasNull());
		assertFalse(rs.next());
	}

	/**
	 * Generacion de datos con valores nulos especificados o generados
	 */
	@Test
	public void testGenerateNulls() throws SQLException {
		this.createTablesAndViews();
		// aprovechara para el caso de una fecha en un datetime que se pone completa,
		// incluyendo hora (debo cambiar el tipo de dato)
		execute(db, "alter table gg1 drop column d2");
		execute(db, "alter table gg1 add d2 " + this.dbms.getDataTypeDatetime() + " NOT NULL");
		// Valores especificados nulos
		DataLoader dtg = getLiveGenerator();
		dtg.load("gg1", "pk1=1, i1=NULL, c1=null, d1=NULL, d2=2007-01-07T05:06:07");
		String sql = "INSERT INTO gg1 (Pk1, I1, I2, C1, C2, D1, D2) VALUES (1, NULL, 3, NULL, '5', NULL, " + getDateTimeForSql() + ")";
		assertEquals(sql.toLowerCase(), dtg.getDataAdapter().getAllAsString().toLowerCase());

		// Valores generados nulos (configura el generador random con 100% de probabilidad de nulos,
		// y especifica los no nullables para comparcion repetible)
		dtg = getLiveGenerator().setAttrGen(new RandomAttrGen()).setNullProbability(100);
		dtg.load("gg1", "pk1=2, i2=2, c2=d, d2=2007-01-07T05:06:07");
		sql = "INSERT INTO gg1 (Pk1, I1, I2, C1, C2, D1, D2) VALUES (2, NULL, 2, NULL, 'd', NULL, " + getDateTimeForSql() + ")";
		assertEquals(sql.toLowerCase(), dtg.getDataAdapter().getAllAsString().toLowerCase());
	}

	protected String getDateTimeForSql() {
		return "'2007-01-07T05:06:07'";
	}

	/**
	 * Generacion no determinista, usando una semilla fija
	 */
	@Test
	public void testGenerateRandom() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getLiveGenerator().setGenerateNullable(false).setAttrGen(new RandomAttrGen().setRandomSeed(999));
		dtg.load("gg1", "", "");
		dtg.load("gg1", "", "");
		String date1 = this.dbms.getSqlDatetimeLiteral("2009-08-08", "");
		String date2 = this.dbms.getSqlDatetimeLiteral("2007-10-19", "");
		String sql = "INSERT INTO gg1 (I2, C2, D2) VALUES (959, 'p', " + date1 + ")\n"
				+ "INSERT INTO gg1 (I2, C2, D2) VALUES (845, 'a', " + date2 + ")";
		assertEquals(sql.toLowerCase(), dtg.getDataAdapter().getAllAsString().toLowerCase());
	}

	/**
	 * Creacion de valores con claves primarias. Las pk creadas crean un valor
	 * simbolico que se puede usar para las fk con posterioridad
	 */
	@Test
	public void testPrimaryKeys() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getLiveGenerator().setGenerateNullable(false);
		dtg.load("ggm", "pk1,i1", "@k1,10");
		dtg.load("ggm", "pk1,i2", "@k2,20");

		ResultSet rs = query(db, "select * from ggm");
		assertTrue(rs.next());
		assertEquals(1, rs.getInt("pk1"));
		assertEquals(10, rs.getInt("i1")); // los valores introducidos tienen los valores especificados
		assertNotEquals(0, rs.getInt("i2")); // los valores no introducidos tienen valores no nulos pues son not null
		assertFalse(rs.wasNull());

		assertTrue(rs.next());
		assertEquals(2, rs.getInt("pk1"));
		assertEquals(20, rs.getInt("i2"));
		assertEquals(0, rs.getInt("i1")); // los valores no introducidos tienen valores nulos pues son nullables
		assertTrue(rs.wasNull());
		assertFalse(rs.next());

		// comprueba los valores simbolicos de las pks generadas
		assertEquals("2", dtg.getSymbolicKeyValues().get("ggm.pk1.@k2"));
		assertEquals("1", dtg.getSymbolicKeyValues().get("ggm.pk1.@k1"));

		// y resultado global
		String expected = "insert into ggm (i1, i2) values (10, 3)\n" + "insert into ggm (i2) values (20)";
		assertEquals(expected, dtg.getDataAdapter().getAllAsString().toLowerCase());

		dtg.reset();
		dtg.load("ggm", "pk1,i1", "@k1,10");
		dtg.load("ggm", "pk1,i2", "@k2,20");
		assertEquals(expected.toLowerCase(), dtg.getDataAdapter().getAllAsString().toLowerCase());

		// Pero ahora se crean dos filas adicionales (el reset solo resetea el generador)
		rs = query(db, "select * from ggm");
		assertTrue(rs.next());
		assertTrue(rs.next());
		assertTrue(rs.next());
		// los nuevos valores son los especificados, pero la clave generada continua la secuencia
		assertEquals("3", dtg.getSymbolicKeyValues().get("ggm.pk1.@k1"));
		assertEquals("4", dtg.getSymbolicKeyValues().get("ggm.pk1.@k2"));
		assertEquals(3, rs.getInt("pk1"));
		assertEquals(10, rs.getInt("i1"));
		assertTrue(rs.next());
		assertEquals(4, rs.getInt("pk1"));
		assertEquals(20, rs.getInt("i2"));
		assertFalse(rs.next());
	}

	/**
	 * Creacion de valores con claves primarias y ajenas, las pk creadas crean un
	 * valor simbolico es usado para las fk.
	 */
	@Test
	public void testForeignKeys() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getLiveGenerator().setGenerateNullable(false);
		// dos maestros que generan claves, dos detalles que referencian a los anteriores
		dtg.load("ggm", "pk1,i1", "@km1,10");
		dtg.load("ggm", "pk1,i2", "@km2,20");
		dtg.load("ggd", "pk1,fk1,i1", "@kd1,@km2,100");
		dtg.load("ggd", "pk1,fk1,i2", "@kd2,@km1,200");

		// comprueba el maestro, pero ahora todas las filas en formato csv en vez de valor por valor
		// se ve que en la fila 1 i2 es autogemerado, en la fila 2 i1 es null, i2 es el valor especificado
		String rsString = queryCsv(db, "select * from ggm", ",");
		assertEquals("1,10,3\n2,NULL,20", rsString);
		assertEquals("1", dtg.getSymbolicKeyValues().get("ggm.pk1.@km1"));
		assertEquals("2", dtg.getSymbolicKeyValues().get("ggm.pk1.@km2"));

		// en el detalle se ve que en la fila 1, i2 ha sido autogenerado, y en la fila 2, i1 es null
		rsString = queryCsv(db, "select * from ggd", ",");
		assertEquals("1,2,100,204\n2,1,NULL,200", rsString);
	}

	/**
	 * Para complementar, repite el test anterior con el adaptador no live,
	 * comprueba las sqls donde se ve ? donde deberia haber un valor simbolico
	 * remplazado
	 */
	@Test
	public void testForeignKeysNoLive() throws SQLException {
		this.createTablesAndViews();
		DataLoader dtg = getSqlGenerator().setGenerateNullable(false);
		// dos maestros que generan claves, dos detalles que referencian a los anteriores
		dtg.load("ggm", "pk1,i1", "@km1,10");
		dtg.load("ggm", "pk1,i2", "@km2,20");
		dtg.load("ggd", "pk1,fk1,i1", "@kd1,@km2,100");
		dtg.load("ggd", "pk1,fk1,i2", "@kd2,@km1,200");
		String sql = "INSERT INTO ggm (I1, I2) VALUES (10, 3)\n" + "INSERT INTO ggm (I2) VALUES (20)\n"
				+ "INSERT INTO ggd (Fk1, I1, I2) VALUES (?, 100, 204)\n" + "INSERT INTO ggd (Fk1, I2) VALUES (?, 200)";
		assertEquals(sql.toLowerCase(), dtg.getDataAdapter().getAllAsString().toLowerCase());
	}
	
}
