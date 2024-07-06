package giis.tdrules.store.loader.gen;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import giis.tdrules.store.loader.IConstraint;

/**
 * Generates deterministic values from a set of dictionaries configured for
 * coordinates defined by pairs entity/attribute; it also provides some
 * formatting options (currently, only masking and padding).
 * Values for attributes with no dictionary configured are generated
 * using a DeterministicAttrGen.
 * 
 * To configure, use the 'with' method selects to select the coordinates that
 * are going to be configured at each moment, 'dictionary' 'mask' establish the
 * desired configuration. All configuration methods are fluent.
 */
public class DictionaryAttrGen extends DeterministicAttrGen {

	private SortedMap<String, DictionaryContainer> containers = new TreeMap<>();
	private DictionaryContainer currentConfiguringContainer;

	//Stores all configurations for a given coordinate
	public class DictionaryContainer {
		private String[] values;
		private int lastIndex = -1;
		
		private String mask = "";
		
		private char padChar = ' ';
		private int padSize = 0;
		
		// Additional constraint, even if the schema does not specifies limits
		ConstraintInteger intConstraint = null;
		
		public void reset() {
			lastIndex = -1;
		}

		public boolean hasValues() {
			return values != null;
		}
		
		public boolean hasMask() {
			return !"".equals(mask);
		}
		private String maskValue(String value) {
			return hasMask() ? mask.replace("{}", value) : value;
		}
		
		public boolean hasPad() {
			return padSize>0;
		}
		private String padValue(String value) {
			String padFormat = "%1$" + padSize + "s";
			return hasPad() ? String.format(padFormat, value).replace(' ', padChar) : value;
		}
		
		@Override
		public String toString() {
			return Arrays.asList(values).toString();
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (Map.Entry<String, DictionaryContainer> entry : this.containers.entrySet())
			entry.getValue().reset();
	}
	
	public DictionaryAttrGen with(String entityName, String attrName) {
		String key = getKey(entityName, attrName);
		currentConfiguringContainer = containers.get(key); //NOSONAR
		if (currentConfiguringContainer == null) {
			currentConfiguringContainer = new DictionaryContainer();
			containers.put(key, currentConfiguringContainer);
		}
		return this;
	}

	// Dictionary configuration
	// currently it does not check if 'with' was previously called

	/**
	 * Sets the dictionary with values to be generated for last coordinates
	 * specified by the 'with' method, only applicable to strings
	 */
	public DictionaryAttrGen dictionary(String... values) {
		currentConfiguringContainer.values = values;
		return this;
	}

	/**
	 * Sets a mask to enhance presentation of a value (applicable to strings and
	 * numbers); a mask is in the form xxx{}yyy where {} is the placeholder that will
	 * contain the generated value
	 */
	public DictionaryAttrGen mask(String mask) {
		currentConfiguringContainer.mask = mask;
		return this;
	}

	/**
	 * Sets a char and total size to apply pad left to the generated string (applicable to numbers)
	 */
	public DictionaryAttrGen padLeft(char padChar, int padSize) {
		currentConfiguringContainer.padChar = padChar;
		currentConfiguringContainer.padSize = padSize;
		return this;
	}

	/**
	 * Sets an interval to generated values (applicable to numbers),
	 * even if the schema does not specify min/max
	 */
	public DictionaryAttrGen setInterval(int min, int max) {
		currentConfiguringContainer.intConstraint = new ConstraintInteger();
		currentConfiguringContainer.intConstraint.add(">=", String.valueOf(min));
		currentConfiguringContainer.intConstraint.add("<=", String.valueOf(max));
		return this;
	}

	// Internal methods to manage the dictionary

	// returns null if no dictionary container found for the coordinates
	private DictionaryContainer getDictionary(String entityName, String attrName) {
		return containers.get(getKey(entityName, attrName));
	}

	private String getNewStringFromDictionary(DictionaryContainer container) {
		if (container.values == null)
			return null;
		container.lastIndex++;
		// if all strings have been generated, recycles the dictionary
		int actualIndex = container.lastIndex % container.values.length;
		int actualCycle = container.lastIndex / container.values.length;

		String value = container.values[actualIndex];
		if (actualCycle > 0) //to generate different values even if recycled
			value += "-" + actualCycle;
		return value;
	}

	private String getKey(String entityName, String attrName) {
		return entityName.toLowerCase() + "." + attrName.toLowerCase();
	}

	// Overriden methods

	@Override
	public String generateString(String entityName, String attrName, int maxLength) {
		DictionaryContainer container = getDictionary(entityName, attrName);
		String value;
		if (container != null && container.hasValues())
			value = getNewStringFromDictionary(container);
		else
			value = super.generateString(entityName, attrName, maxLength);

		if (container != null)
			value = container.padValue(value);

		if (container != null)
			value = container.maskValue(value);

		return limitString(value, maxLength);
	}

	@Override
	public String generateNumber(IConstraint constraints, String entityName, String attrName) {
		DictionaryContainer container = getDictionary(entityName, attrName);
		String value = super.generateNumber(constraints, entityName, attrName);
		
		if (container != null && container.intConstraint != null)
			value = container.intConstraint.apply(value);

		if (container != null)
			value = container.padValue(value);

		if (container != null)
			value = container.maskValue(value);
		return value;
	}

}
