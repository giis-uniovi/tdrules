package giis.tdrules.client.oa.shared;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

/**
 * Records significant errors and warnings produced during schema transformation
 * that produce schemas potentially problematic when used for data generation
 */
public class OaSchemaLogger {

	private List<String> logRecords;

	public OaSchemaLogger() {
		logRecords = new ArrayList<>();
	}

	public void warn(Logger logger, String message, Object... args) {
		logRecords.add("WARN  " + format(message, args));
		logger.error(message, args);
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
