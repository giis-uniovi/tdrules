package giis.tdrules.model;

public class EntityTypes {

	private EntityTypes() {
		throw new IllegalStateException("Utility class");
	}

	public static final String DT_TABLE = "table";
	public static final String DT_VIEW = "view";
	public static final String DT_TYPE = "type";
	public static final String DT_ARRAY = "array";
}
