package giis.tdrules.store.loader.oa;

import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import giis.tdrules.store.loader.LoaderException;

/**
 * Utilities to reorganize json strings containing serialized test data to
 * facilitate the display and test result comparison. Note that the resulting
 * strings are not valid json, only for display.
 */
public class Reserializer {
	/**
	 * Returns a string containing the whole content of the data stored, suitable
	 * for display or test data comparison. Given a json string representing an
	 * object, where each property has a key with the entity name and a value with
	 * an array of the objects stored in this entity, produces a string where each
	 * line includes an entity name and a single object.
	 */
	public String reserializeData(String payload) {
		StringBuilder sb = new StringBuilder();
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			JsonNode entities = mapper.readTree(payload);
			Iterator<String> entitiesIt = entities.fieldNames();
			while (entitiesIt.hasNext()) {
				String entity = entitiesIt.next();
				JsonNode objects = entities.get(entity);
				Iterator<JsonNode> objectsIt = objects.iterator();
				while (objectsIt.hasNext()) { // a line for each object
					JsonNode object = (JsonNode) objectsIt.next();
					sb.append("\"" + entity + "\":" + object + "\n");
				}
			}
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
		return sb.toString();
	}

	/**
	 * Returns a string containing a list of objects, suitable for display or test
	 * data comparison. Given a json string representing a list of objects, produces
	 * a string where each object is in a line
	 */
	public String reserializeList(String payload) {
		StringBuilder sb = new StringBuilder();
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			JsonNode objects = mapper.readTree(payload);
			Iterator<JsonNode> objectsIt = objects.elements();
			while (objectsIt.hasNext()) {
				JsonNode object = objectsIt.next();
				sb.append(object + "\n");
			}
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
		return sb.toString();
	}

}
