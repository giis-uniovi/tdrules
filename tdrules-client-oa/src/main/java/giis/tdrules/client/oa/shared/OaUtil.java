package giis.tdrules.client.oa.shared;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.swagger.v3.oas.models.media.Schema;

/**
 * Common utilities to extract information about items in the Open Api Schema
 */
public class OaUtil {
	private OaUtil() {
		throw new IllegalStateException("Utility class");
	}

	private static final String OBJECT = "object";
	private static final String ARRAY = "array";

	public static boolean isObject(Schema<?> oaProperty) {
		return OBJECT.equals(oaProperty.getType());
	}

	public static boolean isArray(Schema<?> oaProperty) {
		return ARRAY.equals(oaProperty.getType());
	}

	public static void setObject(Schema<?> oaProperty) {
		oaProperty.type(OBJECT);
	}
	
	public static boolean isFreeFormObject(Schema<?> oaProperty) {
		return isObject(oaProperty) && oaProperty.get$ref() == null && oaProperty.getProperties() == null;
	}

	/**
	 * returns the data type of a property, if there is a format defined in the standard
	 * the data type is the format name, 
	 */
	public static String getOaDataType(String type, String format) {
		String dataType = type;
		// if there is a format, uses its value as the data type name, only if the format is defined in the standard
		List<String> formats = Arrays.asList("int32", "int64", "float", "double", "byte", "binary", "date", "date-time", "password");
		if (formats.contains(format))
			return format;
		return dataType;
	}

	public static boolean oaBoolean(Boolean value) {
		return value != null && value == Boolean.TRUE;
	}
	
	/**
	 * Gets the object name in a ref in a form like #/components/schemas/name
	 */
	public static String getNameFromRef(String ref) {
		// Because we can find other names under components
		// the general procedure is splitting the name and get the last value
		String[] split = ref.split("/");
		return split[split.length - 1];
	}

	// all non alphanumeric characters (excluding _ & #) are quoted to avoid problems with query parser
	// (this is also included in SchemaSimpleIdentifier, temp duplicated here to avoid add dependencies
	private static Pattern pattern;

	public static synchronized String quoteIfNeeded(String name) {
		if (pattern == null)
			pattern = Pattern.compile("[^a-z0-9_\\$#]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);
		boolean needsQuote = matcher.find();
		return needsQuote ? "\"" + name + "\"" : name;
	}

	// Solutions using stream, map and collectors cause problems with generic
	// objects, this makes the join manually
	public static String oaEnumString(List<Object> lst) {
		StringBuilder sb = new StringBuilder();
		for (Object item : lst)
			sb.append(item.toString()).append(",");
		String joined = sb.toString();
		return joined.substring(0, joined.length() - 1);
	}
	
	// Returns empty map if null, to allow safe iterations over entries
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Schema> safe(Map<String, Schema> nullableMap) {
        return (Map<String, Schema>) (nullableMap == null ? Collections.emptyMap() : nullableMap);
	}

}
