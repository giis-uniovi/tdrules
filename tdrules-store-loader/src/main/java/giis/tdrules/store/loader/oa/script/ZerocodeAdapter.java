package giis.tdrules.store.loader.oa.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import giis.tdrules.store.loader.gen.IUidGen;
import giis.tdrules.store.loader.oa.OaLiveAdapter;
import giis.tdrules.store.loader.shared.LoaderUtil;

/**
 * A live adapter that overrides the api call processing to internally store all 
 * generated  objects and return a Zerocode scenario when calling getAllAsString()
 */
public class ZerocodeAdapter extends OaLiveAdapter {
	private static final Logger log = LoggerFactory.getLogger(ZerocodeAdapter.class);

	public ZerocodeAdapter(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getLastUid(IUidGen uidGen, String entityName, String attributeName, String attributeType) {
		// Does not has a response, instead, last uid is a jsonpath expression on the response of the step
		String uid = "${$." + current.id + "." + "response.body." + attributeName + "}";
	
		// Processing of nested objects that have an uid require generating a symbol with the
		// object contents at its creation to be included by the container object.
		// These symbols are created by the loader using the response, but this is not available
		// in script adapters. Creates a response in basis of the generated data and the uid variable
		ObjectNode res = LoaderUtil.cloneObjectNode(current.generated);
		res.put(attributeName, uid);
		current.responseString = res.toString();

		log.debug("Zerocode token referencing last uid: {}. Estimated response {}", uid, current.responseString);
		return uid;
	}
	
	@Override
	public void beginWrite(String entityName) {
		super.beginWrite(entityName);
		log.debug("Begin zerocode step: {}", current.id);
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
		ZerocodeWriter writer = new ZerocodeWriter(allGenerated);
		return writer.getAsString();
	}

}
