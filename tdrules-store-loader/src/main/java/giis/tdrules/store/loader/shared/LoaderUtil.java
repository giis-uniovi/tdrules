package giis.tdrules.store.loader.shared;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
	
	/**
	 * Joins two object nodes given by their json representation 
	 * by copying all properties from the source to target.
	 * Repeated attributes in source override those in tarjet
	 */
	public static String copyObjectNodeInto(String sourceJson, String targetJson) {
		try {
			// deserialize the json into a map to proceed the copy
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<Map<String, Object>> type = new TypeReference<Map<String, Object>>() { };
			Map<String, Object> target = mapper.readValue(targetJson, type);
			Map<String, Object> source = mapper.readValue(sourceJson, type);

			for (Map.Entry<String, Object> entry : source.entrySet())
				target.put(entry.getKey(), entry.getValue());

			targetJson = mapper.writeValueAsString(target);
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
		return targetJson;
	}
	
}
