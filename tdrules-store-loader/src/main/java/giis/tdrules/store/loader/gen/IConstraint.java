package giis.tdrules.store.loader.gen;

public interface IConstraint {

	String toString();

	Integer getMin();

	Integer getMax();

	/**
	 * Adds a constraint with a relational operator and a number
	 * (rop are less than, more than and their variants with equality)
	 */
	IConstraint add(String rop, String value);

	/**
	 * Apply the constraint to limit the values of a generated value
	 */
	String apply(String svalue);

}