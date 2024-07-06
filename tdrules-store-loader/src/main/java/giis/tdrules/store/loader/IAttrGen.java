package giis.tdrules.store.loader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Generation of primitive attribute values according to their data type.
 */
public interface IAttrGen {
	
	static final int DEFAULT_MIN_YEAR = 2007;

	/**
	 * Resets the attribute generator to its initial values
	 */
	void reset();

	/**
	 * Generates a string with the specified maximum lenght
	 */
	String generateString(String entityName, String attrName, int maxLength);

	/**
	 * Generates a date (without time) in ISO foramat
	 */
	String generateDate();

	/**
	 * Gets the default minimum year for generated dates 
	 */
	int getMinYear();
	
	/**
	 * Sets the default minimum year for generated dates 
	 */
	IAttrGen setMinYear(int year);

	/**
	 * Generates a number as string
	 */
	String generateNumber(IConstraint constraints, String entityName, String attrName);

	/**
	 * Generates a boolean value
	 */
	boolean generateBoolean();

	/**
	 * Generates a string with one of the allowed values indicated in the parameter
	 */
	String generateCheckInConstraint(String[] allowedValues);

	/**
	 * Determines if a null value must be genrated for a given atribute with a given
	 * probability.
	 */
	boolean isRandomNull(int genNullProbability);

	// Metodos de configuracion y utilidad con implementaciones por defecto

	/**
	 * Increments the counter of objects that are generated to allow
	 * generation of deterministic and different values for each object
	 */
	default void incrementCount() {
	}

	/**
	 * Increments the attribute count to allow generation of deterministic values
	 */
	default void incrementAttrCount() {
	}

	/**
	 * Resets the attribute count
	 */
	default void resetAttrCount() {
	}

	/**
	 * Initializes the random generator with a given seed
	 */
	default IAttrGen setRandomSeed(int seed) {
		return this;
	}

	default Date getDate(int year, int month, int day) {
		return new GregorianCalendar(year, month - 1, day).getTime();
	}

	default String toDateISOString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

}