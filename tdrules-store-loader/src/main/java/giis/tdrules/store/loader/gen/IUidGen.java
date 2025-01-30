package giis.tdrules.store.loader.gen;

/**
 * The implementations of this interface determine the unique identifiers (uid)
 * of generated data, they fall in one of the following categories that depend
 * on where the uid is generated:
 * 
 * - Frontend: The uid is determined during the data generation, the getNew
 * method returns the uid.
 * 
 * - Backend: The uid is determined after the data insertion in the backend server
 * or database, the getNew method returns null and the frontend can check the
 * generated uid using the getLast methods.
 */
public interface IUidGen {

	/**
	 * Resets the UidGen to its initial values
	 */
	void reset();

	/**
	 * Given an attribute (must be an uid) of an entity, creates a new unique value
	 * used to set the rids for new rows:
	 */
	default String getNew(String entityName, String attrName) {
		return null;
	}

	/**
	 * Given an attribute (must be an uid) of an entity, returns the last generated
	 * value for this attribute
	 */
	String getLast(String entityName, String attrName);

	/**
	 * Remembers the last response from the backend (used to get the last generated
	 * uid)
	 */
	default void setLastResponse(String entityName, String response) {
	}
}