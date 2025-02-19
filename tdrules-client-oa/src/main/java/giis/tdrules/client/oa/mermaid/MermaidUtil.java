package giis.tdrules.client.oa.mermaid;

public class MermaidUtil {

	private MermaidUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Replace characters not allowed in mermaid names by underscore
	 */
	public static String alpha(String str) {
		return str.replaceAll("[^A-Za-z0-9\\-]", "_");
	}
	
	public static String alphaCsvNote(String str) {
		return str.replaceAll("[^A-Za-z0-9,\\[\\]\\-]", "_"); // avoids replacing comma and brackets
	}

}
