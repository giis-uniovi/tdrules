package test4giis.tdrules.store.rdb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.hamcrest.CoreMatchers;
import org.junit.Before;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.portable.util.PropertiesFactory;

/**
 * Common definitions to be inherited by the database tests.
 * The reference implementation of tests is Sqlserver,
 * that is inherited to test other DBMS
 */
public class Base {
	// Datos principales para parametrizacion del sgbd usado en el test
	// Esta es la referencia, el resto de sgbds son clases heredadas que cambian este valor
	protected String dbmsname = "sqlserver";
	protected String dbmsproductname = "Microsoft SQL Server";
	protected boolean storesUpperCase = false;
	protected boolean storesLowerCase = false;

	// nombres de las bases de datos a utilizar
	public static final String TEST_DBNAME1 = "test4in2testDB1";
	public static final String TEST_DBNAME2 = "test4in2testDB2";
	// Prefijo que se pone antes del nombre de las tablas al crear
	// necesario en SQLServer cuando se entra con un usuario no privilegiado
	// para poner dbo como owner
	protected static String tablePrefix = "";

	protected Properties config;

	@Before
	public void setUp() throws SQLException {
		// para soportar java y net:
		// en java la configuracion esta en la raiz del multimodule project
		// y en net en la raiz del proyecto (pero como sqlrules-net.properties)
		String fileName;
		if (Parameters.isJava())
			fileName = FileUtil.getPath(Parameters.getProjectRoot(), "..", "sqlrules.properties");
		else
			fileName = FileUtil.getPath(Parameters.getProjectRoot(), "sqlrules-net.properties");
		config = new PropertiesFactory().getPropertiesFromFilename(fileName);
	}

	protected Connection getConnection(String database) throws SQLException {
		Connection conn = DriverManager.getConnection(config.getProperty("sqlrules." + dbmsname + "." + database + ".url"),
				config.getProperty("sqlrules." + dbmsname + "." + database + ".user"),
				config.getProperty("sqlrules." + dbmsname + "." + database + ".password"));
		//Avoid lock problems that make flaky tests when running concurrently with other tests (mainly for getTableList)
		if ("sqlserver".equals(dbmsname))
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return conn;
	}

	protected void execute(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		stmt.executeUpdate(sql);
	}

	protected void executeNotThrow(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
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

	public String listToString(List<String> lst, String separator) {
		StringWriter sw = new StringWriter();
		for (int i = 0; i < lst.size(); i++)
			sw.write((i != 0 ? separator : "") + lst.get(i));
		return sw.toString();
	}

}
