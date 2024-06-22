package giis.tdrules.store.loader.gen;

import java.util.HashMap;
import java.util.Map;

import giis.tdrules.store.loader.IUidGen;

/**
 * Generates sequential uids in the frontend
 */
public class SequentialUidGen implements IUidGen {

	// Last sequences, indexed by entity.attribute, case insensitive
	private Map<String, Long> sequences = new HashMap<>();

	@Override
	public void reset() {
		sequences = new HashMap<>();
	}

	@Override
	public String getNew(String entityName, String attrName) {
		long sequence = getSequence(entityName, attrName);
		sequence++;
		putSequence(entityName, attrName, sequence); // save last generated
		return String.valueOf(sequence);
	}

	@Override
	public String getLast(String entityName, String attrName) {
		long sequence = getSequence(entityName, attrName);
		return String.valueOf(sequence);
	}

	private long getSequence(String entityName, String attrName) {
		Long sequence = sequences.get((entityName + "." + attrName).toLowerCase());
		return sequence == null ? 0 : sequence; // if not has been created before, it is zero
	}

	private void putSequence(String entityName, String attrName, Long value) {
		sequences.put((entityName + "." + attrName).toLowerCase(), value);
	}

}
