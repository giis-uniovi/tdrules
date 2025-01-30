package giis.tdrules.store.loader.gen;

/**
 * Used for compatibility with legacy code: always returns a placeholder (?) as the last uid generated
 */
public class LegacyUidGen implements IUidGen {

	@Override
	public void reset() {
		// nothing to reset in this instance
	}
	
	@Override
	public String getLast(String entityName, String attrName) {
		return "?";
	}

}
