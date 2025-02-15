package giis.tdrules.model.shared;

/**
 * OpenApi (specification extensions/vendor extensions) that are applicable to the model,
 * constants and other utility methods in model transformations
 */
public class OaExtensions {
	// OpenApi vendor extensions
	public static final String X_PK = "x-pk"; // the attribute is an unique object identifier (uid)
	public static final String X_FK = "x-fk"; // the attribute is a reference to an object (rid)
	
	// Names of the uid and rid that is created for arrays in a transformed model
	public static final String ARRAY_PK = "pk_xa";
	public static final String ARRAY_FK = "fk_xa";
	
	// Special data type to represent free-form objects
	public static final String FREE_FORM_OBJECT = "free-form-object";
	
	// Attribute to store an array representing the additionalProperties map
	public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
	
	
	// Keys that can be added in the extended attributes:
	
	// References to objects in the OpenAPI schema that couldn't be found
	// (stores the name of all referenced but not found entities separated by comma)
	public static final String UNDEFINED_REFS = "undefined-refs";
	
	// To allow draw the hierarchy in mermaid (see client-oa, UpstreamAttribute.java)
	public static final String MERMAID_UPSTREAM = "rid-draw-to";
	public static final String UPSTREAM = "upstream";

	private OaExtensions() {
		throw new IllegalStateException("Utility class");
	}

	// Getting the names of entities that are extracted during the transformation of an OpenApi schema
	
	public static String getFkName(String tabName, String colName) {
		return "fk_" + tabName + "_" + colName;
	}

	public static String getExtractedTypeName(String tabName, String colName) {
		return tabName + "_" + colName + "_xt";
	}

	public static String getExtractedArrayName(String tabName, String colName) {
		return tabName + "_" + colName + "_xa";
	}

}
