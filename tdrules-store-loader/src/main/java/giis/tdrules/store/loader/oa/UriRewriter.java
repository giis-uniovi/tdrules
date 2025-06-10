package giis.tdrules.store.loader.oa;

import java.net.URLEncoder;

import giis.tdrules.store.loader.shared.LoaderException;

/**
 * Simple uri rewriting for path parameters
 */
public class UriRewriter {

	String path;
	boolean withEncode = true;

	public UriRewriter(String path) {
		this.path = path == null ? "" : path;
	}
	
	public void setEncode(boolean encode) { // allows disable encoding of values
		this.withEncode = encode;
	}

	public String getUrl() {
		return this.path;
	}

	public boolean hasPathParam(String name) {
		return path.contains("{" + name + "}");
	}

	public UriRewriter rewritePathParam(String name, String value) {
		path = path.replace("{" + name + "}", 
				withEncode ? encode(value) : value);
		return this;
	}

	private String encode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (Exception e) {
			throw new LoaderException(e);
		}
	}

}
