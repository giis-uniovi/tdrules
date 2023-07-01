package test4giis.tdrules.store.rdb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import giis.tdrules.store.rdb.SchemaCheckConstraint;
import giis.tdrules.store.rdb.SchemaException;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import giis.tdrules.store.rdb.SchemaTable;
import giis.tdrules.store.stypes.StoreType;

/**
 * Reading of check constraints
 */
public class TestSqlserverSchemaChecks extends Base {
	protected String catalog = null;
	protected String schema = null;
	protected StoreType dbms;
	protected String enableCheck = ""; // algunas dbms como oracle requieren que se especifique enabled en la creacion
	protected Connection dbt;

	@Before
	public void setUp() throws SQLException {
		super.setUp();
		dbms = StoreType.get(dbmsname);
		dbt = getConnection(TEST_DBNAME2);
		executeNotThrow(dbt, "drop table stabc");
		execute(dbt, "create table " + tablePrefix
				+ "stabc (id numeric(10) not null primary key, num numeric(10), text1 char(1)" + ", check(id>0)"
				+ enableCheck + ", check(id<num)" + enableCheck + ", check(text1 in('S','N'))" + enableCheck + ")");
	}

	@Test
	public void testReaderCheckConstraints() {
		SchemaReaderJdbc mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);
		SchemaTable tab1 = mr.readTable("stabc");
		List<SchemaCheckConstraint> checks = tab1.getCheckConstraints();
		// las comparaciones son muy diferentes segun el sgbd, guardo los datos para los
		// assets en arrays
		String[] checkColumns;
		String[] checkConstraints;
		if (mr.isOracle()) {
			// Oracle incluye las is not null (pero son filtradas por el reader), no
			// proporciona nombre de columna y los in los traduce literalmente
			checkColumns = new String[] { "", "", "" };
			checkConstraints = new String[] { "id>0", "id<num", "text1 in('S','N')" };
		} else if (mr.isSqlite()) {
			checkColumns = new String[] { "", "", "" };
			checkConstraints = new String[] { "(id>0)", "(id<num)", "(text1 in('S','N'))" };
		} else if (mr.isPostgres()) { // muestra la columna, algunas duplicadas si involucran diferentes columnas
			checkColumns = new String[] { "id", "id", "num", "text1" };
			checkConstraints = new String[] { "((id < num))", "((id > (0)::numeric))", "((id < num))",
					"((text1 = ANY (ARRAY['S'::bpchar, 'N'::bpchar])))" };
		} else if (mr.isSQLServer()) { // muestra la columna, algunas duplicadas si involucran diferentes columnas
			checkColumns = new String[] { "id", "id", "num", "text1" };
			checkConstraints = new String[] { "([id]<[num])", "([id]>(0))", "([id]<[num])",
					"([text1]='N' OR [text1]='S')" };
		} else
			throw new SchemaException("Expected values for checks not stablished for this dbms");

		assertEquals(checkConstraints.length, checks.size()); // hay tres constraints, pero una involucra dos columnas,
																// luego aparece dos veces
		for (int i = 0; i < checkConstraints.length; i++) {
			assertEquals(checkColumns[i], checks.get(i).getColumn());
			assertEquals(checkConstraints[i], checks.get(i).getConstraint());
		}

		// Comprueba los checks asociados a columnas
		// Solo se ha implementado para oracle en el caso de check in
		assertEquals("", mr.getColumn(0).getCheckInConstraint());
		assertEquals("", mr.getColumn(1).getCheckInConstraint());
		if (dbms.isOracle())
			assertEquals("('S','N')", mr.getColumn(2).getCheckInConstraint());
		else
			assertEquals("", mr.getColumn(2).getCheckInConstraint());
	}
}
