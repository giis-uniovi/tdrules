package giis.tdrules.store.loader.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import giis.tdrules.store.loader.LoaderException;
import giis.tdrules.store.loader.IUidGen;

/**
 * An UidGen for relational databases that gets the last autogenerated uid after
 * inserting a row
 */
public class SqlLiveUidGen implements IUidGen {

	private Connection conn;

	public SqlLiveUidGen(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void reset() {
		// Note: the backend generated keys will not be reset by this method
	}

	@Override
	public String getNew(String entityName, String attrName) {
		return null;
	}

	@Override
	public String getLast(String entityName, String attrName) {
		String sql = "select " + attrName + " from " + entityName + " order by " + attrName + " desc";
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			rs.next();
			long colValue = rs.getLong(attrName);
			return String.valueOf(colValue);
		} catch (SQLException e) {
			throw new LoaderException(e);
		}
	}

}
