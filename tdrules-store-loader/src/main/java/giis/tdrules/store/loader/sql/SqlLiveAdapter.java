package giis.tdrules.store.loader.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.store.loader.gen.IUidGen;
import giis.tdrules.store.loader.shared.LoaderException;

/**
 * A live adapter to generate and execute sql statements to populate a database.
 */
public class SqlLiveAdapter extends SqlLocalAdapter {
	private static final Logger log = LoggerFactory.getLogger(SqlLiveAdapter.class);
	private Connection conn;

	public SqlLiveAdapter(Connection conn, String dbmsName) {
		super(dbmsName);
		this.conn = conn;
	}

	@Override
	public IUidGen getDefaultUidGen() {
		return new SqlLiveUidGen(this.conn);
	}

	@Override
	public String getLastUid(IUidGen uidGen, String entityName, String attributeName) {
		return uidGen.getLast(entityName, attributeName);
	}

	@Override
	public void endWrite() {
		super.endWrite();
		String sql = super.getLast();
		log.trace("endWrite, RUN: {}", sql);
		this.execute(sql);
	}

	public void execute(String sql) {
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			throw new LoaderException(e);
		}
	}

}
