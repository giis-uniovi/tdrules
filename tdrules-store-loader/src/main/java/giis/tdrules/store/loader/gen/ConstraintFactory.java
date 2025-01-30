package giis.tdrules.store.loader.gen;

import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.model.shared.ModelUtil;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdEntity;

/**
 * For an attribute, processes the constraints included in the model to create
 * the appropriate instance to handle these constraints according to the
 * attribute data type.
 */
public class ConstraintFactory {

	private static final Logger log = LoggerFactory.getLogger(ConstraintFactory.class);
	private IConstraint constraint = null;

	public String toString() {
		return constraint == null ? "null" : constraint.toString();
	}

	public IConstraint getConstraint() {
		return constraint;
	}

	public ConstraintFactory(IDataAdapter dataAdapter, TdEntity entity, TdAttribute attribute) {
		// constraints related to the attribute are stored in the model at the entity level in TdCheck
		for (TdCheck check : ModelUtil.safe(entity.getChecks())) {
			if (dataAdapter.isNumber(attribute.getDatatype())
					&& attribute.getName().equalsIgnoreCase(check.getAttribute())) {
				boolean hasDecimals = dataAdapter.hasDecimals(attribute.getDatatype(), attribute.getSize());
				log.trace("Processing relational constraints for: {}, attribute: {}, constraint: {}",
						attribute.getName(), check.getAttribute(), check.getConstraint());
				String[] splitted = this.getRelationalNumericConstraint(check, attribute.getName());
				if (splitted.length == 0)
					continue;
				log.trace("Found numeric constraint and parsed, items: {}", Arrays.asList(splitted).toString());
				if (constraint == null)
					constraint = hasDecimals ? new ConstraintDecimal() : new ConstraintInteger();
				constraint.add(splitted[1], splitted[2]);
			}
		}
	}

	// Check if constraint is relational (attribute ROP number) and matches the attribute 
	// If matches returns the three components in an array, if not, null
	private String[] getRelationalNumericConstraint(TdCheck constraint, String attribute) {
		String[] splitted = splitRelationalConstraint(constraint.getConstraint());
		if (splitted.length == 0)
			return new String[0];
		if (!attribute.equalsIgnoreCase(splitted[0].trim()))
			return new String[0];
		// Validates the number. Note that this can appear in a scientific notation
		// when the openapi model contains limits on numbers with decimals,
		// so that isParseable can't recognize it. Using isCreatable.
		if (!NumberUtils.isCreatable(splitted[2]))
			return new String[0];
		// Additionally, if number was in scientific notation (not parseable), other transformations 
		// from string to number will fail, removes the scientific notation.
		if (!NumberUtils.isParsable(splitted[2]))
			splitted[2] = String.valueOf(Double.valueOf(splitted[2]).longValue());
		return new String[] { splitted[0].trim(), splitted[1], splitted[2].trim() };
	}

	private String[] splitRelationalConstraint(String expression) {
		for (String op : new String[] { "<=", ">=", "<", ">" }) {
			String[] splitted = expression.split(op);
			if (splitted.length == 2)
				return new String[] { splitted[0], op, splitted[1] };
		}
		return new String[0];
	}

}
