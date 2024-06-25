package giis.tdrules.store.stypes;

/**
 * Although non relational, Cassandra can be managed if using a jdbc compatible driver or wrapper
 */
public class StoreTypeCassandra extends StoreType {
	protected StoreTypeCassandra(String dbms) {
		super(dbms);
	}

	@Override
	public boolean isCassandra() {
		return true;
	}

}
