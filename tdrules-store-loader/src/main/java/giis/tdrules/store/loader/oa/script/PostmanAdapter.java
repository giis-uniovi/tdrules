package giis.tdrules.store.loader.oa.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import giis.tdrules.store.loader.gen.IUidGen;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.shared.LoaderUtil;

/**
 * A live adapter that overrides the api call processing to internally store all 
 * generated  objects and return a Postman collection when calling getAllAsString()
 */
public class PostmanAdapter extends OaLiveAdapter {
	private static final Logger log = LoggerFactory.getLogger(PostmanAdapter.class);
	
	// The attribute to be assigned to each variable must be stores to generate the 
	// javascript post-request when writing the collection
	private Map<String, String> postResponseDefs = new HashMap<>();
	// Variables are set as string in the json structure, but those that contain numeric values
	// must be unquoted when writing the collection
	private Set<String> postResponseUnquoted = new HashSet<>();

	public PostmanAdapter(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getLastUid(IUidGen uidGen, String entityName, String attributeName, String attributeType) {
		// Does not has a response, instead, last uid is variable.
		// Variables are stored in a map that points to the value that must be assigned in the post-response
		String uid = "{{" + current.id + "}}";
		postResponseDefs.put(current.id, attributeName);
		// variables will be written as strings to populate the json,
		// but numbers are remembered to be unquoted when writing collection string
		if (isNumber(attributeType))
			postResponseUnquoted.add(current.id);

		// Processing of nested objects that have an uid require generating a symbol with the
		// object contents at its creation to be included by the container object.
		// These symbols are created by the loader using the response, but this is not available
		// in script adapters. Creates a response in basis of the generated data and the uid variable
		ObjectNode res = LoaderUtil.cloneObjectNode(current.generated);
		res.put(attributeName, uid);
		current.responseString = res.toString();

		log.debug("Zerocode token referencing last uid: {}. Response attribute: {}. Estimated response {}", 
				uid, attributeName, current.responseString);
		return uid;
	}

	@Override
	public void beginWrite(String entityName) {
		super.beginWrite(entityName);
		log.debug("Begin postman collection item: {}", current.id);
		this.rewriter.setEncode(false); // here, path parametrs are zerocode tokens
	}

	@Override
	protected void processApiCall() {
		// not using live api
	}
	
	/**
	 * Returns a string with the zerocode scenario
	 */
	@Override
	public String getAllAsString() {
		PostmanWriter writer = new PostmanWriter(allGenerated, postResponseDefs, postResponseUnquoted);
		return writer.getAsString();
	}

}
