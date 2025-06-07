package giis.tdrules.store.loader.sql;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.store.dtypes.DataTypes;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.gen.IUidGen;
import giis.tdrules.store.loader.gen.LegacyUidGen;
import giis.tdrules.store.stypes.StoreType;


/**
 * A local adapter to generate sql statements to populate a database, but
 * without actually executing the statements.
 */
public class SqlLocalAdapter implements IDataAdapter {
	private static final Logger log = LoggerFactory.getLogger(SqlLocalAdapter.class);
	protected StoreType dbmsType;
	private DataTypes types;

	public SqlLocalAdapter(String dbmsName) {
		this.dbmsType = StoreType.get(dbmsName);
		this.types = DataTypes.get(dbmsName);
	}

	// all generated sql statements
	private List<String> allGenerated = new ArrayList<>();

	// sql fragments while we are writing in a table
	private StringBuilder sql1;
	private StringBuilder sql2;
	private int columnCount;

	@Override
	public void reset() {
		allGenerated = new ArrayList<>();
	}

	@Override
	public DataTypes getDataTypes() {
		return this.types;
	}

	@Override
	public IDataAdapter getNewLocalAdapter() {
		throw new UnsupportedOperationException(
				"Generation of composite types still not implemented for relational databases");
	}

	@Override
	public IUidGen getDefaultUidGen() {
		return new LegacyUidGen();
	}

	@Override
	public String getLast() {
		return allGenerated.get(allGenerated.size() - 1);
	}

	/**
	 * The SQL local adapters don't generate uids in the backend, returns a parameter placeholder
	 * to allow compse valid insert statements
	 */
	@Override
	public String getLastUid(IUidGen uidGen, String entityName, String attributeName) {
		return "?";
	}

	@Override
	public List<String> getAll() {
		return allGenerated;
	}

	@Override
	public void beginWrite(String entityName) {
		sql1 = new StringBuilder();
		sql2 = new StringBuilder();
		columnCount = 0;
		sql1.append("INSERT INTO ").append(entityName).append(" (");
		sql2.append("VALUES (");
	}

	@Override
	public void writeValue(String dataType, String attrName, String attrValue) {
		String separator = columnCount == 0 ? "" : ", ";
		sql1.append(separator).append(attrName);
		attrValue = getColValue(dataType, attrValue);
		sql2.append(separator).append(attrValue);
		columnCount++;
	}

	@Override
	public void endWrite() {
		String sql = sql1.toString() + ") " + sql2.toString() + ")";
		allGenerated.add(sql);
		log.debug("endWrite, SQL: {}", sql);
	}

	private String getColValue(String dataType, String value) {
		if (value == null)
			return "NULL";
		else if (isDate(dataType))
			return dbmsType.getSqlDatetimeLiteral(trimSqlQuotes(value), "");
		else if (isString(dataType))
			return ensureSqlQuoted(value);
		else
			return value;
	}

	// internal utilities

	private String ensureSqlQuoted(String c) {
		c = c.trim();
		if (c.startsWith("'") && c.endsWith("'"))
			return c;
		else
			return "'" + c.replace("'", "''") + "'";
	}

	private String trimSqlQuotes(String c) {
		c = c.trim();
		c = c.startsWith("'") ? c.substring(1, c.length()) : c;
		c = c.endsWith("'") ? c.substring(0, c.length() - 1) : c;
		return c.trim();
	}

}
