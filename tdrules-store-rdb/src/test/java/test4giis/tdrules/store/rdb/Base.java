package test4giis.tdrules.store.rdb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.tdrules.store.rdb.JdbcProperties;
import giis.tdrules.store.rdb.SchemaColumn;
import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.visualassert.Framework;
import giis.visualassert.VisualAssert;

/**
 * Common definitions to be inherited by the database tests.
 * The reference implementation of tests is Sqlserver,
 * that is inherited to test other DBMS
 */
public class Base {
	protected static final Logger log = LoggerFactory.getLogger(Base.class);
	
	protected static final String PLATFORM = Parameters.getPlatformName();
	protected static final String SETUP_PATH = FileUtil.getPath(Parameters.getProjectRoot(), "..", "setup");
	protected static final String ENVIRONMENT_PROPERTIES = FileUtil.getPath(SETUP_PATH, "environment.properties");
	protected static final String DATABASE_PROPERTIES = FileUtil.getPath(SETUP_PATH, "database.properties");

	// nombres de las bases de datos a utilizar
	public static final String TEST_DBNAME2 = "tdstorerdb2";
	// Prefijo que se pone antes del nombre de las tablas al crear
	// necesario en SQLServer cuando se entra con un usuario no privilegiado para poner dbo como owner
	protected static String tablePrefix = "";

	// Datos principales para parametrizacion del sgbd usado en el test
	// Esta es la referencia, el resto de sgbds son clases heredadas que cambian este valor
	protected String dbmsname = "sqlserver";
	protected String dbmsproductname = "Microsoft SQL Server";
	protected boolean storesUpperCase = false;
	protected boolean storesLowerCase = false;
	
    protected VisualAssert va=new VisualAssert().setFramework(Framework.JUNIT4);

	protected static final String TEST_PATH_BENCHMARK = Parameters.isJava()
			? "src/test/resources"
			: FileUtil.getPath(Parameters.getProjectRoot(), "../tdrules-store-rdb/src/test/resources");
	protected static final String TEST_PATH_OUTPUT = Parameters.isJava()
			? "target"
			: FileUtil.getPath(Parameters.getProjectRoot(), "reports");
	

	@Rule public TestName testName = new TestName();
	
	@Before
	public void setUp() throws SQLException {
		log.info("****** Running test: {} ******", testName.getMethodName());
	}

	/**
	 * Connection user and url are obtained from a properties file,
	 * password is obtained from environment, if not defined, another properties file is used as fallback
	 */
	protected Connection getConnection(String database) throws SQLException {
		log.debug("Create connection to '{}' database", dbmsname);
		String propPrefix = "tdrules." + PLATFORM + "." + database + "." + dbmsname;
		Connection conn = DriverManager.getConnection(
				new JdbcProperties().getProp(DATABASE_PROPERTIES, propPrefix + ".url"), 
				new JdbcProperties().getProp(DATABASE_PROPERTIES, propPrefix + ".user"),
				new JdbcProperties().getEnvVar(ENVIRONMENT_PROPERTIES, "TEST_" + dbmsname.toUpperCase() + "_PWD"));
		//Avoid lock problems that make flaky tests when running concurrently with other tests (mainly for getTableList)
		if ("sqlserver".equals(dbmsname))
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return conn;
	}

	protected void execute(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		try {
			stmt.executeUpdate(sql);
		} finally {
			stmt.close();
		}
	}

	protected void executeNotThrow(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// fail silently
		} finally {
			stmt.close();
		}
	}

	protected ResultSet query(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		return stmt.executeQuery(sql);
	}

	/**
	 * Cambia el case de un string leido de los metadatos en el caso de que la BD
	 * tenga esta configuracion (p.e. oracle todo en uppercase)
	 */
	public String asStored(String value) {
		if (storesUpperCase)
			value = value.toUpperCase();
		else if (storesLowerCase)
			value = value.toLowerCase();
		return value;
	}

	public void assertEqualsDBObj(String expected, String actual) {
		assertEquals(asStored(expected), actual);
	}

	public void assertContains(String expectedSubstring, String actual) {
		assertThat(actual, CoreMatchers.containsString(expectedSubstring));
	}

	public void assertDoesNotContain(String unexpectedSubstring, String actual) {
		assertThat(actual, CoreMatchers.not(CoreMatchers.containsString(unexpectedSubstring)));
	}

	protected String getMetadataAsString(SchemaReader sr) {
		StringBuilder sb = new StringBuilder();
		sb.append("Metadata for Table: " + sr.getTableName());
		sb.append("  Catalog: " + sr.getCatalog());
		sb.append("  Schema: " + sr.getSchema());
		if (sr.isView())
			sb.append("\nView SQL: ").append(((SchemaReaderJdbc) sr).getQuery(sr.getCurrentTable()));
		for (int i = 0; i < sr.getColumnCount(); i++) {
			SchemaColumn col = sr.getColumn(i);
			sb.append("\nColumn: ").append(col.getColName());
			sb.append("\n  DataType: ").append(col.getDataType());
			sb.append("  DataSubType: ").append(col.getDataSubType());
			sb.append("  CompositeType: ").append(col.getCompositeType());
			sb.append("\n  ColSize: ").append(col.getColSize());
			sb.append("  DecimalDigits: ").append(col.getDecimalDigits());

			sb.append("  CharacterLike: ").append(lower(col.isCharacterLike()));
			sb.append("  DateTimeLike: ").append(lower(col.isDateTimeLike()));

			sb.append("\n  NotNull: ").append(lower(col.isNotNull()));
			sb.append("  Key: ").append(lower(col.isKey()));
			sb.append("  Autoincrement: ").append(lower(col.isAutoIncrement()));
			sb.append("  DefaultValue: ").append(col.getDefaultValue());
		}
		return sb.toString();
	}
	private String lower(boolean value) {
		return value ? "true" : "false";
	}

	protected void assertMetadata(String metadata, String fileName) {
		FileUtil.fileWrite(TEST_PATH_OUTPUT, fileName, metadata);
		String expected = FileUtil.fileRead(TEST_PATH_BENCHMARK, fileName);
		va.assertEquals(expected.replace("\r", ""), metadata.replace("\r", ""));
	}

}
