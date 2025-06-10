package giis.tdrules.store.loader.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LoaderUtil {

	private LoaderUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Returns true if the string representing a value can represent a 
	 * Zerocode or Postman placeholder/variable
	 */
	public static boolean canBeVariablePlaceholder(String value) {
		return (value != null && (
				(value.startsWith("${$.") && value.endsWith("}")) // Zerocode token
				|| (value.startsWith("{{") && value.endsWith("}}")) // Postman variable
				));
	}

	public static ObjectNode cloneObjectNode(ObjectNode node) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return (ObjectNode) mapper.readTree(node.toString());
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
	}
}
