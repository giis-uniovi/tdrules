package giis.tdrules.model.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects several matching expressions on entities or attributes (set up by the add method) and provides
 * a method to determine if a given entity or attribute matches any of the matching
 * expressions.
 * 
 * This class is intended to be used as parent or delegate of a filter class
 * that traverses a model (e.g. TdModel or Open Api) and applies the filter to
 * the matched entities or attributes.
 */
public class SchemaFilterMatcher<T extends SchemaFilterMatcher<T>> {
	// The simplest approach to store the patterns is using this list that contains
	// string arrays where first position represents the entity pattern and the
	// second represents the attribute pattern. A wildcard in the attribute pattern
	// means to remove the whole entity. Not optimized but good enough when there
	// are few filters
	private List<String[]> patterns = new ArrayList<>();

	/**
	 * Adds two patterns (for entity and attribute names, respectively) to be
	 * removed from the model. Allows specify exact, starts with and ends with
	 * matching using wildcards. To exclude an entire entity specify * for the
	 * attribute. Returns this object to allow fluent style calls.
	 */
	@SuppressWarnings("unchecked")
	public T add(String entityPattern, String attributePattern) {
		patterns.add(new String[] { entityPattern.toLowerCase(), attributePattern.toLowerCase() });
		return (T) this;
	}

	/**
	 * Returns true if the entity and attribute parameters match any of the
	 * configured matchers.
	 */
	public boolean matchItems(String entity, String attribute) {
		for (String[] pattern : patterns)
			if (matchName(entity, pattern[0]) && matchName(attribute, pattern[1]))
				return true;
		return false;
	}

	private boolean matchName(String name, String pattern) {
		name = name.toLowerCase();
		return pattern.equals("*") || name.equals(pattern)
				|| pattern.charAt(0) == '*' && name.endsWith(pattern.substring(1))
				|| pattern.charAt(pattern.length() - 1) == '*'
						&& name.startsWith(pattern.substring(0, pattern.length() - 1));
	}

}
