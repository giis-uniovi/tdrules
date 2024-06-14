package giis.tdrules.model;

/**
 * OpenApi (specification extensions/vendor extensions) that are applicable to the model,
 * constants and other utility methods in model transformations
 */
public class OaExtensions {
	// OpenApi extensions
	public static final String X_PK = "x-pk"; // the attribute is an unique object identifier (uid)
	public static final String X_FK = "x-fk"; // the attribute is a reference to an object (rid)
	
	// Names of the uid and rid that is created for arrays in a transformed model
	public static final String ARRAY_PK = "pk_xa";
	public static final String ARRAY_FK = "fk_xa";
	
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
