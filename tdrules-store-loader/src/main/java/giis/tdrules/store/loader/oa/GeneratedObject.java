package giis.tdrules.store.loader.oa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Model of the data that is being generated
 */
public class GeneratedObject {
	// Entity where the data adapter is currently writing
	public String entityName;
	// Json object that the data adapter is currently writing
	public ObjectNode generated;
	
	// Additional info required for some adapters, set to default empty values for others
	
	// The generated data can be distributed between body and params when writing to a live api
	protected ObjectNode requestBody;
	protected ObjectNode pathParams;
	// Response string, if any
	protected String responseString;

	public GeneratedObject(String entityName) {
		this.entityName = entityName;
		ObjectMapper mapper = new ObjectMapper();
		this.generated = mapper.createObjectNode();
		
		this.requestBody = mapper.createObjectNode();
		this.pathParams = mapper.createObjectNode();
		this.responseString = "";
	}
	
}

