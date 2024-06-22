package giis.tdrules.store.loader.gen;

import giis.tdrules.store.loader.IConstraint;

/**
 * Limits the possible values that an integer can take according the attribute constraints
 * (configure the constraints with the add method)
 */
public class ConstraintInteger implements IConstraint {
	private static final int DEFAULT_MAGNITUDE_ORDER = 999;
	
	// configured parameters, at least one should be configured
	private Integer min = null;
	private Integer max = null;
	// actual additional parameters, lazy set when calling apply
	private Integer range = null;

	@Override
	public String toString() {
		return "[Min: " + min + ", Max: " + max + "]";
	}
	
	@Override
	public Integer getMin() {
		ensureConfigured();
		return min;
	}

	@Override
	public Integer getMax() {
		ensureConfigured();
		return max;
	}

	/**
	 * Adds a constraint with a relational operator and a number
	 * (rop are less than, more than and their variants with equality)
	 */
	@Override
	public IConstraint add(String rop, String value) {
		if ("<=".equals(rop))
			max = Integer.parseInt(value);
		if ("<".equals(rop))
			max = Integer.parseInt(value) - 1;
		if (">=".equals(rop))
			min = Integer.parseInt(value);
		if (">".equals(rop))
			min = Integer.parseInt(value) + 1;
		return this;
	}

	/**
	 * Apply the constraint to limit the values of a generated value
	 */
	@Override
	public String apply(String svalue) {
		ensureConfigured();
		Integer value = Integer.parseInt(svalue);
		// uses the remainder to keep value in range
		if (value > max)
			value = Math.abs((value - min) % range) + min;
		else if (value < min)
			value = max - Math.abs((max - value) % range);
		return value.toString();
	}

	private void ensureConfigured() {
		if (range != null)
			return;
		// default values if max or min are not configured in a 3 digit range
		if (min == null && max == null) {
			min = 0;
			max = DEFAULT_MAGNITUDE_ORDER;
		} else if (min == null) {
			min = max - DEFAULT_MAGNITUDE_ORDER;
			// If no min was specified, this value of min may result in negative numbers.
			// Some schemas ommit min limits althought they should (e.g the Market benchmark, age has max but not min)
			// Compromise decision: Set min to zero unless max is very small (limit: +2)
			if (max > 2)
				min = 0;
		} else if (max == null)
			max = min + DEFAULT_MAGNITUDE_ORDER;
		range = max - min + 1;
	}

}
