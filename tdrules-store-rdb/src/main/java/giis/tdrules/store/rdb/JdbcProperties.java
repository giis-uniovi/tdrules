package giis.tdrules.store.rdb;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.portable.util.JavaCs;
import giis.portable.util.PropertiesFactory;

/**
 * Some utilities to read jdbc connection values from external files or
 * environment variables
 */
public class JdbcProperties {
	private static final Logger log = LoggerFactory.getLogger(JdbcProperties.class);

	/**
	 * Gets a value from an environment variable, if not defined reads from a
	 * fallback properties file
	 */
	public String getEnvVar(String fallbackFileName, String name) {
		log.debug("Get '" + name + "' from environment (with fallback)");
		String value = JavaCs.getEnvironmentVariable(name);
		if (value == null) // fallback, read from file (as with the source bash command)
			value = getProp(fallbackFileName, name);
		return value;
	}

	/**
	 * Gets a value from an properties file
	 */
	public String getProp(String fileName, String name) {
		log.debug("Get '" + name + "' property from file '" + fileName + "'");
		Properties prop = new PropertiesFactory().getPropertiesFromFilename(fileName);
		if (prop == null)
			throw new SchemaException("Can't read properties file '" + fileName + "'");
		String value = prop.getProperty(name);
		if (value == null)
			throw new SchemaException("Can't get '" + name + "' from properties file '" + fileName + "'");
		else if ("".equals(value))
			log.warn("Property '" + name + "' is empty");
		return value;
	}

}
