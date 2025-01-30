package giis.tdrules.client.oa.shared;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Records significant errors and warnings produced during schema transformation
 * that produce schemas potentially problematic when used for data generation
 */
public class OaSchemaLogger {

	protected Logger standardLogger;
	private List<String> logRecords;

	public OaSchemaLogger(@SuppressWarnings("rawtypes") Class clazz) {
		logRecords = new ArrayList<>();
		standardLogger = LoggerFactory.getLogger(clazz);
	}

	public void warn(String message, Object... args) {
		logRecords.add("WARN  " + format(message, args));
		standardLogger.error(message, args);
	}

	private String format(String message, Object... args) {
		return String.format(message.replace("{}", "%s"), args);
	}

	public List<String> getLogs() {
		return logRecords;
	}

	@Override
	public String toString() {
		return String.join("\n", logRecords);
	}

}
