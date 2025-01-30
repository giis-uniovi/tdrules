package giis.tdrules.store.loader.gen;

import giis.tdrules.store.loader.shared.LoaderException;

/**
 * Generates random primitive attribute values
 */
public class RandomAttrGen implements IAttrGen {
	private RandomGenerator randomGenerator;
	private int minYear = DEFAULT_MIN_YEAR;
	private int seed = -1; // set to other value if calling setRandomSeed

	public RandomAttrGen() {
		this.randomGenerator = new RandomGenerator();
	}

	@Override
	public void reset() {
		if (seed != -1)
			setRandomSeed(seed);
		else
			throw new LoaderException("RandomAttrGen.reset not allowed if no seed is set");
	}

	@Override
	public String generateString(String entityName, String attrName, int maxLength) {
		return randomGenerator.getString(maxLength > 3 ? 3 : maxLength);
	}

	@Override
	public String generateDate() {
		return toDateISOString(randomGenerator.getDate(getMinYear(), getMinYear()+2));
	}

	@Override
	public String generateNumber(IConstraint constraints, String entityName, String attrName) {
		if (constraints == null)
			return String.valueOf(randomGenerator.getInt(900) + 100);
		// uses the range specified by the constraints
		int min = constraints.getMin();
		int max = constraints.getMax();
		int value = randomGenerator.getInt(max - min + 1);
		value = value + min;
		return String.valueOf(value);
	}

	@Override
	public boolean generateBoolean() {
		return randomGenerator.getInt(1) == 0;
	}

	@Override
	public String generateCheckInConstraint(String[] allowedValues) {
		return allowedValues[randomGenerator.getInt(allowedValues.length)];
	}

	@Override 
	public int getMinYear() {
		return minYear;
	}
	
	@Override
	public IAttrGen setMinYear(int year) {
		this.minYear = year;
		return this;
	}

	@Override
	public boolean isRandomNull(int genNullProbability) {
		if (genNullProbability > 0) {
			int n = randomGenerator.getInt(100);
			if (n < genNullProbability)
				return true;
		}
		return false;
	}

	@Override
	public IAttrGen setRandomSeed(int seed) {
		this.randomGenerator.initialize(seed);
		this.seed = seed; // remember to apply on reset
		return this;
	}

}
