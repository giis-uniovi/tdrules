package giis.tdrules.model;

public class RuleTypes {

	private RuleTypes() {
		throw new IllegalStateException("Utility class");
	}

	public static final String FPC = "fpc";
	public static final String MUTATION = "mutation";

	// Conversions for compatibility between api v3 and v4
	
	public static String normalizeV4(String ruleType) {
		return normalize(true, ruleType);
	}

	public static String normalizeV3(String ruleType) {
		return normalize(false, ruleType);
	}

	private static String normalize(boolean v4, String ruleType) {
		if (ruleType == null)
			return "";
		if (ruleType.contains(FPC))
			return v4 ? FPC : "sqlfpc";
		else if (ruleType.contains(MUTATION))
			return v4 ? MUTATION : "sqlmutation";
		return "";
	}
}
