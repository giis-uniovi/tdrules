package test4giis.tdrules.store.loader.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.tdrules.store.rdb.JdbcProperties;

public class Base {
	Logger log = LoggerFactory.getLogger(this.getClass());

	protected static final String PLATFORM = Parameters.getPlatformName();
	private static final String SETUP_PATH = FileUtil.getPath(Parameters.getProjectRoot(), "..", "setup");
	private static final String ENVIRONMENT_PROPERTIES = FileUtil.getPath(SETUP_PATH, "environment.properties");
	private static final String DATABASE_PROPERTIES = FileUtil.getPath(SETUP_PATH, "database.properties");

	public static final String TEST_DBNAME = "tdloadrdb";
	
	protected String dbmsname = "sqlserver";
	
	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws SQLException {
		log.debug("*** Running test: {}", testName.getMethodName());
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
		// Try to avoid lock problems that make flaky tests when running concurrently
		// with other tests (mainly for getTableList)
		if ("sqlserver".equals(dbmsname))
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return conn;
	}

	protected void execute(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		stmt.execute(sql);
	}

	protected void executeNotThrow(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		try {
			stmt.execute(sql);
		} catch (SQLException e) { // NOSONAR
		}
	}

	protected ResultSet query(Connection dbt, String sql) throws SQLException {
		Statement stmt = dbt.createStatement();
		return stmt.executeQuery(sql);
	}

	public String queryCsv(Connection dbt, String sql, String separator) throws SQLException {
		ResultSet rs = null;
		rs = query(dbt, sql);
		StringBuilder s = new StringBuilder();
		int colCount = rs.getMetaData().getColumnCount();
		while (rs.next()) {
			if (s.length() != 0)
				s.append("\n"); // separa lineas
			for (int i = 0; i < colCount; i++) {
				String value = rs.getString(i + 1);
				s.append((i == 0 ? "" : separator) + (value == null ? "NULL" : value));
			}
		}
		rs.close();
		return s.toString();
	}

}
