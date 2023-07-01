package test4giis.tdrules.store.rdb.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import test4giis.tdrules.store.rdb.TestSqlserverSchemaRead;

public class TestPostgresSchemaRead extends TestSqlserverSchemaRead {

	public TestPostgresSchemaRead() {
		this.dbmsname = "postgres";
		this.dbmsproductname = "PostgreSQL";
		this.storesLowerCase = true;
		this.stringConcat = "||";
		this.INT = "integer";
		this.IDENTITY = "serial";
		this.INT_precision = 10;
		this.DECIMAL = "numeric";
		this.concatCharType = "text";
		this.concatCharSize = 0;
		this.myCatalogSchema1 = "public.";
		this.myCatalogSchema2 = "public.";
	}

	@Override
	public void testQuotedIdentifiersWithBracket() {
		// no se hereda este test pues causa errores sintacticos
	}

	/**
	 * Estructuras compuestas (Types y arrays), solo prueba en Postgres
	 */
	@Test
	public void testComposites() throws SQLException {
		executeNotThrow(dbt, "drop table udttable");
		executeNotThrow(dbt, "drop type udttype cascade");
		execute(dbt, "create type udttype as (key decimal(10,2), value varchar(16))");
		execute(dbt, "create table udttable (arrcol decimal(11,3)[] , udt udttype)");
		SchemaReader mr = new SchemaReaderJdbc(dbt, this.catalog, this.schema);

		// Lista de tablas y udts eligiendo diferentes tipos de tabla para mostrar
		List<String> tables = mr.getTableList(false, false, true, "udt");
		assertEquals("[udttype]", tables.toString());// StrUtil.deepToString(tables));
		tables = mr.getTableList(true, true, false, "udt");
		assertEquals("[udttable]", tables.toString());// StrUtil.deepToString(tables));
		tables = mr.getTableList(true, true, true, "udt");
		assertEquals("[udttable, udttype]", tables.toString());// StrUtil.deepToString(tables));

		// Lectura de tabla y sus columnas
		mr.readTable("udttable");
		assertEquals(true, mr.isTable());
		assertEquals("udttable", mr.getTableName());
		// arrays
		assertEquals("arrcol", mr.getColumn(0).getColName());
		assertEquals("numeric", mr.getColumn(0).getDataType());
		assertEquals("array", mr.getColumn(0).getCompositeType());
		assertEquals(11, mr.getColumn(0).getColSize());
		assertEquals(3, mr.getColumn(0).getDecimalDigits());
		// Tipos definidos por el usuario (row set), el tipo es el definido por el
		// usuario, se guarda objeto
		assertEquals("udt", mr.getColumn(1).getColName());
		assertEquals("udttype", mr.getColumn(1).getDataType());
		assertEquals("type", mr.getColumn(1).getCompositeType());
		assertEquals(0, mr.getColumn(1).getColSize());
		assertEquals(0, mr.getColumn(1).getDecimalDigits());

		// Lectura de la estructura del tipo
		mr.readTable("udttype");
		assertEquals(true, mr.isType());
		assertEquals("udttype", mr.getTableName());

		// primera columna es entero
		// como postgres no permite poner restricciones en lso tipos, siempre seran
		// nullables y sin default
		assertEquals("", mr.getColumn(0).getCompositeType()); // este ya es primitivo
		assertEquals("key", mr.getColumn(0).getColName());
		assertEquals("numeric", mr.getColumn(0).getDataType());
		assertEquals(10, mr.getColumn(0).getColSize());
		assertEquals(2, mr.getColumn(0).getDecimalDigits());
		assertFalse(mr.getColumn(0).isNotNull());
		assertEquals("", mr.getColumn(0).getDefaultValue());

		// segunda columna es varchar
		assertEquals("", mr.getColumn(1).getCompositeType()); // este ya es primitivo
		assertEquals("value", mr.getColumn(1).getColName());
		assertEquals("varchar", mr.getColumn(1).getDataType());
		assertEquals(16, mr.getColumn(1).getColSize());
		assertEquals(0, mr.getColumn(1).getDecimalDigits());
		assertFalse(mr.getColumn(1).isNotNull());
		assertEquals("", mr.getColumn(1).getDefaultValue());
	}

}
