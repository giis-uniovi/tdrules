package giis.tdrules.store.loader.oa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Model of the data that is being generated
 */
public class GeneratedObject {
	// Entity where the data adapter is currently writing
	public String entityName; // NOSONAR used like a record
	// Json object that the data adapter is currently writing
	public ObjectNode generated; // NOSONAR
	
	// Additional info required for some adapters, set to default empty values for others
	
	public String id; // NOSONAR
	public String method; // NOSONAR
	public String url; // NOSONAR

	// The generated data can be distributed between body and params when writing to a live api
	public ObjectNode requestBody; // NOSONAR
	public ObjectNode pathParams; // NOSONAR
	// Response from the api string, if any
	public String responseString; // NOSONAR

	public GeneratedObject(String entityName) {
		this.entityName = entityName;
		ObjectMapper mapper = new ObjectMapper();
		this.generated = mapper.createObjectNode();
		
		this.id = "";
		this.method = "";
		this.url = "";
		
		this.requestBody = mapper.createObjectNode();
		this.pathParams = mapper.createObjectNode();
		this.responseString = "";
	}
	
}

