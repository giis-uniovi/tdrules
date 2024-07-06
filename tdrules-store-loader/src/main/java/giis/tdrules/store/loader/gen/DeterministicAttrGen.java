package giis.tdrules.store.loader.gen;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.store.loader.IAttrGen;
import giis.tdrules.store.loader.IConstraint;

/**
 * Generates primitive attribute values in a reproducible sequence, taking into
 * account the the number of objects already created for each entity and the
 * relative position of the attribute in each entity.
 */
public class DeterministicAttrGen implements IAttrGen {
	private static final Logger log = LoggerFactory.getLogger(DeterministicAttrGen.class);
	// Coordinates of object and attributes to get different values (but a reproducible sequence)
	private int generatedItemCount = 0;
	private int generatedAttrCount = 0;
	private int minYear = DEFAULT_MIN_YEAR;

	@Override
	public void reset() {
		generatedItemCount = 0;
		generatedAttrCount = 0;
	}

	@Override
	public String generateString(String entityName, String attrName, int maxLength) {
		String s = String.valueOf(this.generatedAttrCount + 1 + this.generatedItemCount * 100);
		return limitString(s, maxLength);
	}

	protected String limitString(String s, int maxLength) {
		if (maxLength > 0 && s.length() > maxLength)
			s = s.substring(s.length() - maxLength, s.length());
		return s;
	}

	@Override
	public String generateDate() {
		int day = this.generatedAttrCount + this.generatedItemCount;
		Date date = getDate(getMinYear() + day / 365, (day / 30) % 12 + 1, day % 30 + 1);
		return toDateISOString(date);
	}

	@Override
	public String generateNumber(IConstraint constraints, String entityName, String attrName) {
		String gen = String.valueOf(this.generatedAttrCount + 1 + this.generatedItemCount * 100);
		if (constraints != null) {
			String previousGen = gen;
			gen = constraints.apply(gen);
			log.debug("Apply constraint: {} to: {}={}. Constrained value: {}", constraints, attrName, previousGen, gen);
		}
		return gen;
	}

	@Override
	public boolean generateBoolean() {
		return (this.generatedAttrCount + this.generatedItemCount) % 2 == 0;
	}

	@Override
	public String generateCheckInConstraint(String[] allowedValues) {
		return String
				.valueOf(allowedValues[(this.generatedAttrCount + this.generatedItemCount) % allowedValues.length]);
	}

	@Override
	public boolean isRandomNull(int genNullProbability) {
		// Although generation is determinostic, if probability is 100%, the return will be always true
		return genNullProbability == 100;
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
	public void incrementCount() {
		generatedItemCount++;
	}

	@Override
	public void incrementAttrCount() {
		generatedAttrCount++;
	}

	@Override
	public void resetAttrCount() {
		generatedAttrCount = 0;
	}

}
