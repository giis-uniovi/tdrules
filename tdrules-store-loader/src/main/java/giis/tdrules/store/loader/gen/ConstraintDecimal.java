package giis.tdrules.store.loader.gen;

import java.math.BigDecimal;

/**
 * Limits the possible values that a decimal number can take according the attribute constraints
 * (configure the constraints with the add method).
 * NOTE: Generation is done using the same methods than for integer numbers, 
 * but the resulting values are scaled (divided by 10 to obtain one decimal).
 */
public class ConstraintDecimal extends ConstraintInteger {
	
	// When adding constraints, the constraint is scaled (mutiplied by 10).
	// Later, the DataGenerator will be responsible to revert the scale of the resulting number
	// If the scaled value still has decimals, the superclass sets the nearest limit that satisfies the constraint.
	@Override
	public IConstraint add(String rop, String value) {
		super.add(rop, scaleDecimal(value));
		return this;
	}

	private String scaleDecimal(String value) {
		return new BigDecimal(value).movePointRight(1).toPlainString();
	}

}
