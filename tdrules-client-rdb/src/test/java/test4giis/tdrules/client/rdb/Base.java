package test4giis.tdrules.client.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
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
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected static final String PLATFORM = Parameters.getPlatformName();
	private static final String SETUP_PATH = FileUtil.getPath(Parameters.getProjectRoot(), "..", "setup");
	private static final String ENVIRONMENT_PROPERTIES = FileUtil.getPath(SETUP_PATH, "environment.properties");
	private static final String DATABASE_PROPERTIES = FileUtil.getPath(SETUP_PATH, "database.properties");
	
	protected static final String TEST_DBNAME = "tdclirdb";

	@Rule public TestName testName = new TestName();
	
	@Before
	public void setUp() throws SQLException {
		log.info("****** Running test: {} ******", testName.getMethodName());
	}

	/**
	 * Connection user and url are obtained from a properties file,
	 * password is obtained from environment, if not defined, another properties file is used as fallback
	 */
	protected Connection getConnection(String dbmsVendor, String database) throws SQLException {
		log.debug("Create connection to '{}' database", dbmsVendor);
		String propPrefix = "tdrules." + PLATFORM + "." + TEST_DBNAME + "." + dbmsVendor;
		return DriverManager.getConnection(
				new JdbcProperties().getProp(DATABASE_PROPERTIES, propPrefix + ".url"), 
				new JdbcProperties().getProp(DATABASE_PROPERTIES, propPrefix + ".user"),
				new JdbcProperties().getEnvVar(ENVIRONMENT_PROPERTIES, "TEST_" + dbmsVendor.toUpperCase() + "_PWD"));
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

}
