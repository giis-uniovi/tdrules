package giis.tdrules.store.loader.oa;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import giis.tdrules.store.loader.LoaderException;
import giis.tdrules.store.loader.IUidGen;

/**
 * An UidGen for openapi that gets the last autogenerated uid after a POST
 * operation that returns the content of the object created.
 */

public class OaLiveUidGen implements IUidGen {

	private String lastResponse = "";

	public String getLast(String entityName, String attrName) {
		Map<String, Object> map = serialize(this.lastResponse);
		Object value = map.get(attrName);
		if (value == null)
			throw new LoaderException("getLast: no generated value for entity: " + entityName + " - attribute: "
					+ attrName + " - Last response: " + this.lastResponse);
		return value.toString();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> serialize(String json) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, Map.class);
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
	}

	@Override
	public void reset() {
		lastResponse = "";
		// Note that the backend generated keys will not be reset by this method
	}

	@Override
	public void setLastResponse(String entityName, String response) {
		this.lastResponse = response;
	}

}