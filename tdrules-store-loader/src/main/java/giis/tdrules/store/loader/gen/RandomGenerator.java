package giis.tdrules.store.loader.gen;

import java.util.*;

/**
 * Random generator utility
 */
public class RandomGenerator {
	private Random generator = null; // NOSONAR not final because it could be reset several times

	/**
	 * Sets a value for the random seed
	 */
	public RandomGenerator initialize(int seed) {
		generator = new Random(seed);
		return this;
	}

	/**
	 * Returns an in between 0 and upper-1
	 */
	public int getInt(int upper) {
		if (generator == null)
			generator = new Random();
		if (upper < 1)
			upper = 1;
		return generator.nextInt(upper);
	}

	/**
	 * Returns a random lowercase string with the specified number of chars
	 */
	public String getString(int n) {
		StringBuilder s = new StringBuilder();
		if (n <= 1)
			s.append((char) ('a' + getInt('z' - 'a' + 1)));
		else {
			for (int i = 0; i < n; i++)
				s.append(getString(1));
		}
		return s.toString();
	}

	/** Returns a DateObject between the years specified */
	public Date getDate(int yearFrom, int yearTo) {
		int year = getInt(yearTo - yearFrom + 1) + yearFrom;
		int month = getInt(12) + 1;
		int day = getInt(31) + 1;
		return getDate(year, month, day);
	}

	private Date getDate(int year, int month, int day) {
		return new GregorianCalendar(year, month - 1, day).getTime();
	}
}
