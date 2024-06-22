package test4giis.tdrules.store.loader.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import giis.tdrules.store.loader.DataLoader;

public class TestOracleSqlLoad extends TestSqlserverSqlLoad {

	public TestOracleSqlLoad() {
		this.dbmsname = "oracle";
	}

	protected String getDateTimeForSql() {
		return "timestamp '2007-01-07 05:06:07'";
	}

	/**
	 * Tras crear las tablas crea la secuencia para simular los campos identity de
	 * SQLServer en dgm y dgd
	 */
	@Override
	protected void createTablesAndViews() throws SQLException {
		super.createTablesAndViews();
		Connection dbt = getConnection(TEST_DBNAME); // modo conectado para que se cierre la conexion
		try {
			execute(dbt, "drop sequence seq_ggm");
		} catch (Throwable e) {
		}
		try {
			execute(dbt, "drop sequence seq_ggd");
		} catch (Throwable e) {
		}
		execute(dbt, "create sequence seq_ggm");
		execute(dbt, "create sequence seq_ggd");
		execute(dbt, "CREATE OR REPLACE TRIGGER trg_ggm BEFORE INSERT ON ggm "
				+ "FOR EACH ROW BEGIN SELECT seq_ggm.NEXTVAL INTO :new.Pk1 FROM dual; END;");
		execute(dbt, "CREATE OR REPLACE TRIGGER trg_ggd BEFORE INSERT ON ggd "
				+ "FOR EACH ROW BEGIN SELECT seq_ggd.NEXTVAL INTO :new.Pk1 FROM dual; END;");
		dbt.close();
	}

	/**
	 * Constraints checkIn del esquema, solo en oracle en el que este incluye la
	 * restriccion como atributo, el resto de sgbd incluyen todas las constraints
	 * pero tienen sintaxis diferentes
	 */
	@Test
	public void testCheckIn() throws SQLException {
		this.createTablesAndViews();
		// cambia la tabla dg1 para poner una columna con constraint check in
		executeNotThrow(db, "drop table Gg1");
		execute(db, "create table Gg1 (Pk1 int, ccin char(3) not null check (ccin in ('SSS','NNN')))");

		DataLoader dg = getLiveGenerator();
		dg.load("gg1", "pk1", "1");

		ResultSet rs = query(db, "select * from gg1");
		assertTrue(rs.next());
		assertEquals(1, rs.getInt("pk1"));
		String cin = rs.getString("ccin");
		assertTrue(cin.equals("SSS") || cin.equals("NNN")); // este tiene un check in solo son posible sdos valores
		assertFalse(rs.wasNull());
	}

}
