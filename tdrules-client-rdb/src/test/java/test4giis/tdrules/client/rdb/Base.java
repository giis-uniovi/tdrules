package test4giis.tdrules.client.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.Before;

import giis.portable.util.FileUtil;
import giis.portable.util.Parameters;
import giis.portable.util.PropertiesFactory;

public class Base {
	
	protected Properties config;

	@Before
	public void setUp() throws SQLException {
		//para soportar java y net:
		//en java la configuracion esta en la raiz del multimodule project
		//y en net en la raiz del proyecto (pero como sqlrules.properties)
		String fileName;
		if (Parameters.isJava())
			fileName = FileUtil.getPath(Parameters.getProjectRoot(), "..", "sqlrules.properties");
		else
			fileName = FileUtil.getPath(Parameters.getProjectRoot(), "sqlrules-net.properties");
		config = new PropertiesFactory().getPropertiesFromFilename(fileName);
	}

	protected Connection getConnection(String dbms, String database) throws SQLException {
		return DriverManager.getConnection(
				config.getProperty("sqlrules." + dbms + "." + database + ".url"),
				config.getProperty("sqlrules." + dbms + "." + database + ".user"),
				config.getProperty("sqlrules." + dbms + "." + database + ".password"));
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
