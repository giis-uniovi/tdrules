package giis.tdrules.store.loader.gen;

import giis.tdrules.store.loader.IConstraint;

/**
 * Limits the possible values that a decimal number can take according the attribute constraints
 * (configure the constraints with the add method).
 * NOTE: Generation is done using the same methods than for integer numbers, 
 * but the resulting values are scaled (divided by 10 to obtain one decimal).
 */
public class ConstraintDecimal extends ConstraintInteger {
	
	// When adding constraints, the constraint is scaled (mutiplied by 10).
	// Later, the DataGenerator will be responsible to revert the scale of the resulting number
	// No more transformations are needed.
	@Override
	public IConstraint add(String rop, String value) {
		super.add(rop, scaleDecimalToInt(value));
		return this;
	}

	private String scaleDecimalToInt(String value) {
		return String.valueOf(Math.round(Double.parseDouble(value) * 10));
	}

}
