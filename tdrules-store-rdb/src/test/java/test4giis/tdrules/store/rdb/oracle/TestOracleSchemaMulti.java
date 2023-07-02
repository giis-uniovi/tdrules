package test4giis.tdrules.store.rdb.oracle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import giis.tdrules.store.rdb.SchemaReader;
import giis.tdrules.store.rdb.SchemaReaderJdbc;
import test4giis.tdrules.store.rdb.TestSqlserverSchemaMulti;

/**
 * Pruebas especificas de oracle (ademas de las heredadas):
 * Cuando se involucran varios esquemas un usuario
 * privilegiado puede tener acceso a otros esquemas de otros usuarios
 */
public class TestOracleSchemaMulti extends TestSqlserverSchemaMulti {
	//esquemas de cada uno de los usuarios
	private final String SCHEMA0="test4in2testDB0"; //SOLO PARA OTORGAR PERMISOS
	private final String SCHEMA1=TEST_DBNAME1;
	private final String SCHEMA2=TEST_DBNAME2;
	
	public TestOracleSchemaMulti() {
		this.dbmsname="oracle";
		this.storesUpperCase=true;
		this.enableCheck="ENABLE";
	}
	/**
	 * Conexion a base de datos para el usuario cuyo numero coincide con el del esquema
	 * En Oracle:
	 * Test4In2testDB1: privilegiado (DBA), puede acceder a las tablas de otro esquema, 
	 * Test4In2testDB2: sin privilegios, no puede acceder
	 * Test4In2testDB0: privilegiado (DBA), usado solo para hacer grant references al usuario 1
	 * @throws IOException 
	 */
	protected Connection newData(int usr) throws SQLException {
		String dbName="";
		if (usr==0) dbName=this.SCHEMA0;
		else if (usr==1) dbName=this.SCHEMA1;
		else if (usr==2) dbName=this.SCHEMA2;
		return getConnection(dbName);
	}
	/** 
	 * Crea todas las tablas y vistas a utilizar, haciendo un borrado previo
	 * @throws IOException 
	 */
	protected void createTablesAndViews() throws SQLException {
		//Si da problemas de quota de algun usuario al crear tablas se ejecutara el siguiente
		//comando desde system
		//alter user <user_name> quota unlimited on <tablespace_name>;
		//ALTER USER TEST4IN2TESTDB1 QUOTA UNLIMITED ON USERS
		//Las tablas xx estan en ambos esquemas, las 1 y las 2 cada uno en el suyo

 		Connection db=newData(1);
		//primero elimina las constraints de las fks
		try {execute(db, "ALTER TABLE TOSM12 DROP CONSTRAINT FK1"); } catch (Exception e) {}
		try {execute(db, "ALTER TABLE TOSM1 DROP CONSTRAINT FK2"); } catch (Exception e) {}
		db.close();
		//luego crea las tablas de esquema 2, alguna es referenciada por esquema 1
		db=newData(2);
		executeNotThrow(db, "DROP TABLE TOSM12");
		executeNotThrow(db, "DROP TABLE TOSM2");
		executeNotThrow(db, "DROP VIEW TOSM12V");
		execute(db, "CREATE TABLE TOSM12 (COL12 VARCHAR(1), COLCHK CHAR(1) CHECK (COLCHK in ('A','B')) ENABLE)");
		execute(db, "CREATE TABLE TOSM2 (COL2 NUMBER(2,1), PRIMARY KEY(COL2))");
		execute(db, "CREATE VIEW TOSM12V AS SELECT COL12 V2COL12 FROM TOSM12");
		db.close();
		db=newData(1);
		executeNotThrow(db, "DROP TABLE TOSM12");
		executeNotThrow(db, "DROP TABLE TOSM1");
		executeNotThrow(db, "DROP TABLE TOSMX1");
		executeNotThrow(db, "DROP VIEW TOSM12V");
		executeNotThrow(db, "DROP VIEW TOSM12W");

		//Las tablas del esquema 1 tienen fks, en TOSM12 referencia a su mismo esquema y en TOSM1 al otro
		execute(db, "CREATE TABLE TOSM12 (COL12 NUMBER(4,1), COL12F NUMBER(4,1) , COLCHK CHAR(1), CHECK (COLCHK in ('Y','N')) ENABLE)");
		execute(db, "CREATE TABLE TOSM1 (COL1 NUMBER(4,1), PRIMARY KEY (COL1), COL1F NUMBER(2,1) )");
		//Ahora crea las FKs
		execute(db, "ALTER TABLE TOSM12 ADD CONSTRAINT FK1 FOREIGN KEY(COL12F) REFERENCES TOSM1(COL1)");
		//y las vistas
		execute(db, "CREATE VIEW TOSM12V AS SELECT COL12 V1COL12 FROM TOSM12");
		execute(db, "CREATE VIEW TOSM12W (WCOL12F) AS SELECT COL12F FROM TOSM12");
		db.close();
		
		//Para crear la constraint entre el esquema 1 y el dos el usuario 1 debe tener privilegios
		//de references en el esquema 2, pero no se los puede otorgar el mismo, con lo que
		//utilizo el usuario 0 que se los otorga y luego el 1 ya puede anyadir la constraint
		db=newData(0);
		execute(db, "GRANT REFERENCES ON test4in2testDB2.TOSM2 TO test4in2testDB1");
		db.close();
		db=newData(1);
		//GRANT REFERENCES on test4in2testdb2.tosm2 TO TEST4IN2TESTDB1
		execute(db, "ALTER TABLE TOSM1 ADD CONSTRAINT FK2 FOREIGN KEY (COL1F) REFERENCES test4in2testDB2.TOSM2(COL2)");
		db.close();
		}
		
	/**
	 * Visibilidad con un usuario respecto del esquema propio y de otro diferente,
	 * especificando siempre el esquema respecto del que se busca.
	 * Cada usuario debe ver solo lo del esquema especificado, y en el no privilegiado
	 * solo aquello de su propio esquema
	 * @throws IOException 
	 */
	@Test
	public void testViewEachOtherWithSchema() throws SQLException {
		createTablesAndViews();
		Connection db=newData(1);
		//Usuario privilegiado en su esquema
		SchemaReader mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);
		mr.readTable("tosm12");
		mr.readTable("tosm1");
		try { mr.readTable("tosm2"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: tosm2",e.getMessage()); }
		//Usuario privilegiado en el otro esquema
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA2);
		mr.readTable("tosm12");
		try { mr.readTable("tosm1"); fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: tosm1",e.getMessage()); }
		mr.readTable("tosm2");
		db.close();
		
		//Usuario no privilegiado en su esquema
		db=newData(2);
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA2);
		mr.readTable("tosm12");
		try { mr.readTable("tosm1"); fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: tosm1",e.getMessage()); }
		mr.readTable("tosm2");
		//Usuario no privilegiado en el otro esquema
		//a diferencia del anterior no puede ver nada pues es propiedad del otro usuario
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);
		try { mr.readTable("tosm12"); fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: tosm12",e.getMessage()); }
		try { mr.readTable("tosm1"); fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: tosm1",e.getMessage()); }
		try { mr.readTable("tosm2"); fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: tosm2",e.getMessage()); }
		db.close();
	}

	/**
	 * Visibilidad con un usuario respecto del esquema propio y de otro diferente,
	 * NO especificando siempre el esquema respecto del que se busca.
	 * El usuario privilegiado puede ver ambos esquemas, salvo las tablas que estan en los dos
	 * que provocan excepcion puesto que no se puede resolver cual es la tabla a buscar
	 * y el no privilegiado solo aquello de su propio esquema
	 * @throws IOException 
	 */
	@Test
	public void testViewEachOtherWithoutSchema() throws SQLException {
		createTablesAndViews();
		Connection db=newData(1);
		//Usuario privilegiado en su esquema
		//no puede ver la tabla que esta en ambos porque hay dos visibles y no se puede resolver
		SchemaReader mr=new SchemaReaderJdbc(db,"","");
		try { mr.readTable("tosm12"); fail("se esperaba excepcion"); } 
		catch (Exception e) { assertContains("SchemaReaderJdbc.setTableType: Found more than one table or view: tosm12",e.getMessage()); }
		mr.readTable("tosm1");
		mr.readTable("tosm2"); 
		db.close();
		
		//Usuario no privilegiado en su esquema
		//puede ver la tabla compartida porque es propia (y la otra no es visible)
		//no puede ver la tabla del otro esquema
		db=newData(2);
		mr=new SchemaReaderJdbc(db,"","");
		mr.readTable("tosm12");
		try { mr.readTable("tosm1"); fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: tosm1",e.getMessage()); }
		mr.readTable("tosm2");
		db.close();
	}
	
	/**
	 * Lista de tablas de un esquema, comprobaciones similares a las anteriores
	 * pero respecto de la lectura de la lista de tablas
	 * @throws IOException 
	 */
	@Test
	public void testGetTableList() throws SQLException {
		createTablesAndViews();
		//desde el usuario 2 se ven solo las tablas cuando esta en esquema 2, 
		//y no se ve nada cuando esta en 1 pues no tiene permisos
		Connection db=newData(2);
		SchemaReader mr=new SchemaReaderJdbc(db,"",this.SCHEMA2);
		String tables = "," + listToString(mr.getTableList(true, false),",") + ",";
		assertContains(",TOSM2,", tables);
		assertContains(",TOSM12,", tables);
		assertDoesNotContain(",TOSM1,", tables);
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);
		tables = "," + listToString(mr.getTableList(true, false),",") + ",";
		assertDoesNotContain(",TOSM1,", tables);
		assertDoesNotContain(",TOSM2,", tables);
		assertDoesNotContain(",TOSM12,", tables);
		db.close();
		
		//Especificando esquema, desde el usuario 1 (privilegiado) se ven las tablas que estan en cada uno
		db=newData(1);
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);
		tables = "," + listToString(mr.getTableList(true, false),",") + ",";
		assertContains(",TOSM1,", tables);
		assertContains(",TOSM12,", tables);
		assertDoesNotContain(",TOSM2,", tables);
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA2);
		tables = "," + listToString(mr.getTableList(true, false),",") + ",";
		assertContains(",TOSM2,", tables);
		assertContains(",TOSM12,", tables);
		assertDoesNotContain(",TOSM1,", tables);
		db.close();
		
		//Cuando no se especifica un esquema, desde el usuario 2 se ve lo mismo
		//Pero la lista de tablas se debe restringir para evitar obtener todas las tablas del sistema que tardan minutos en obtenerse
		db=newData(2);
		mr=new SchemaReaderJdbc(db,"","");
		tables = "," + listToString(mr.getTableList(true, false, "TOSM"),",") + ",";
		assertContains(",TOSM2,", tables);
		assertContains(",TOSM12,", tables);
		assertDoesNotContain(",TOSM1,", tables);
		db.close();
		
		//Desde el usuario 1 se sigue pudiendo ver la tabla del esquema 2
		db=newData(1);
		mr=new SchemaReaderJdbc(db,"","");
		tables = "," + listToString(mr.getTableList(true, false, "TOSM2"),",") + ",";
		assertContains(",TOSM2,", tables);
		db.close();
		
		//Pero si se limita menos, hay tablas que estan en varios esquemas y causa excepcion por tabla duplicada
		db=newData(1);
		mr=new SchemaReaderJdbc(db,"",""); 
		try { mr.getTableList(true, false, "TOSM"); fail("Se esperaba una excepcion"); }
		catch (Throwable e) {assertContains("SchemaReaderJdbc.getTableList: Found more than one table or view",e.getMessage()); }
		db.close();
	}
	
	/**
	 * Visibilidad de tablas cuando se especifica un nombre cualificado de schema
	 * (en Oracle XE solo se maneja un catalogo, no se puede probar a nivel de este)
	 * @throws IOException 
	 */
	@Test
	public void testReadQualifiedTable() throws SQLException {
		createTablesAndViews();
		//Usuario privilegiado en su esquema ve las tablas que existen y estan correctamente qualificadas
		//Prueba cada una de las tablas en ambos esquemas cuando se define en el contexto
		//del esquema propio, el ajeno
		//y los datos principales de columna y tipo para asegurarse de que son los correctos
		Connection db=newData(1);
		//con esquema 1 por defecto, 
		assertR(db,this.SCHEMA1, this.SCHEMA1+".tosm12", "TOSM12", "TOSM12", this.SCHEMA1+".TOSM12", "COL12","NUMBER");
		assertR(db,this.SCHEMA1, this.SCHEMA1+".tosm1",  "TOSM1",  "TOSM1", this.SCHEMA1+".TOSM1",  "COL1","NUMBER");
		assertR(db,this.SCHEMA1, this.SCHEMA2+".tosm12", "TOSM12", this.SCHEMA2+".TOSM12", this.SCHEMA2+".TOSM12", "COL12","VARCHAR2");
		assertR(db,this.SCHEMA1, this.SCHEMA2+".tosm2",  "TOSM2",  this.SCHEMA2+".TOSM2",  this.SCHEMA2+".TOSM2",  "COL2","NUMBER");
		//con esquema 2 por defecto
		assertR(db,this.SCHEMA2, this.SCHEMA1+".tosm12", "TOSM12", this.SCHEMA1+".TOSM12", this.SCHEMA1+".TOSM12", "COL12","NUMBER");
		assertR(db,this.SCHEMA2, this.SCHEMA1+".tosm1",  "TOSM1",  this.SCHEMA1+".TOSM1", this.SCHEMA1+".TOSM1",  "COL1","NUMBER");
		assertR(db,this.SCHEMA2, this.SCHEMA2+".tosm12", "TOSM12", "TOSM12", this.SCHEMA2+".TOSM12", "COL12","VARCHAR2");
		assertR(db,this.SCHEMA2, this.SCHEMA2+".tosm2",  "TOSM2",  "TOSM2",  this.SCHEMA2+".TOSM2",  "COL2","NUMBER");
		//con ningun esquema por defecto, se localizan correctamente las tablas
		//pero el valor default esta qualificado siempre
		assertR(db,"", this.SCHEMA1+".tosm12", "TOSM12", this.SCHEMA1+".TOSM12", this.SCHEMA1+".TOSM12", "COL12","NUMBER");
		assertR(db,"", this.SCHEMA1+".tosm1",  "TOSM1",  this.SCHEMA1+".TOSM1",  this.SCHEMA1+".TOSM1",  "COL1","NUMBER");
		assertR(db,"", this.SCHEMA2+".tosm12", "TOSM12", this.SCHEMA2+".TOSM12", this.SCHEMA2+".TOSM12", "COL12","VARCHAR2");
		assertR(db,"", this.SCHEMA2+".tosm2",  "TOSM2",  this.SCHEMA2+".TOSM2",  this.SCHEMA2+".TOSM2",  "COL2","NUMBER");
		
		//Tablas sin qualificar
		//con esquema 1 por defecto, 
		//la tabla tosm12 que se ve es la del esquema por defecto
		//se ve porque el tipo de datos de la columna es el que corresponde y el full qualified
		//Solo se buscan las tablas en este esquema, las otras daran error (se ha probado anteriormente)
		assertR(db,this.SCHEMA1, "tosm12", "TOSM12", "TOSM12", this.SCHEMA1+".TOSM12", "COL12","NUMBER");
		assertR(db,this.SCHEMA1, "tosm1",  "TOSM1",  "TOSM1", this.SCHEMA1+".TOSM1",  "COL1","NUMBER");
		assertR(db,this.SCHEMA1, "tosm12", "TOSM12", "TOSM12", this.SCHEMA1+".TOSM12", "COL12","NUMBER");
		//con esquema 2 por defecto
		assertR(db,this.SCHEMA2, "tosm12", "TOSM12", "TOSM12", this.SCHEMA2+".TOSM12", "COL12","VARCHAR2");
		assertR(db,this.SCHEMA2, "tosm12", "TOSM12", "TOSM12", this.SCHEMA2+".TOSM12", "COL12","VARCHAR2");
		assertR(db,this.SCHEMA2, "tosm2",  "TOSM2",  "TOSM2",  this.SCHEMA2+".TOSM2",  "COL2","NUMBER");
		
		//con ningun esquema por defecto, valores default y full sin qualificar
		//no incluyo TOSM12 pues causa error al haber dos tablas iguales que son visibles
		assertR(db,"", "tosm1",  "TOSM1",  "TOSM1",  "TOSM1",  "COL1","NUMBER");
		assertR(db,"", "tosm2",  "TOSM2",  "TOSM2",  "TOSM2",  "COL2","NUMBER");
		
		//y tablas que existen en un esquema pero que son cualificadas con otro distinto causan excepcion
		SchemaReader mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);			
		try { mr.readTable(this.SCHEMA1 + ".tosm2");  fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: test4in2testDB1.tosm2",e.getMessage()); }
		try { mr.readTable(this.SCHEMA2 + ".tosm1");  fail("se esperaba excepcion"); } 
		catch (Exception e) { assertEquals("SchemaReaderJdbc.setTableType: Can't find table or view: test4in2testDB2.tosm1",e.getMessage()); }

		db.close();
	}
	
	/**
	 * Lecutra de metadatos correspondientes a vistas. 
	 * Las vistas se intentan leer usando getMetaData del resultset correspondiente, 
	 * por ello la visibilidad de acceso podria ser distinta de la de las tablas
	 * @throws IOException 
	 */
	@Test
	public void testReadView() throws SQLException {
		createTablesAndViews();
		//desde el usuario 1 con/sin qualificar
		Connection db=newData(1);
		//cualificado 
		assertR(db,this.SCHEMA1, this.SCHEMA1+".tosm12v", "TOSM12V", "TOSM12V", this.SCHEMA1+".TOSM12V", "V1COL12","NUMBER");
		assertR(db,this.SCHEMA1, this.SCHEMA1+".tosm12w",  "TOSM12W",  "TOSM12W", this.SCHEMA1+".TOSM12W",  "WCOL12F","NUMBER");
		assertR(db,this.SCHEMA1, this.SCHEMA2+".tosm12v", "TOSM12V", this.SCHEMA2+".TOSM12V", this.SCHEMA2+".TOSM12V", "V2COL12","VARCHAR2");
		//sin cualificar
		assertR(db,this.SCHEMA1, "tosm12v", "TOSM12V", "TOSM12V", this.SCHEMA1+".TOSM12V", "V1COL12","NUMBER");
		assertR(db,this.SCHEMA1, "tosm12w", "TOSM12W", "TOSM12W", this.SCHEMA1+".TOSM12W",  "WCOL12F","NUMBER");
		assertR(db,this.SCHEMA2, "tosm12v", "TOSM12V", "TOSM12V", this.SCHEMA2+".TOSM12V", "V2COL12","VARCHAR2");
		//con ningun esquema por defecto, 
		assertR(db,"", this.SCHEMA1+".tosm12v", "TOSM12V", this.SCHEMA1+".TOSM12V", this.SCHEMA1+".TOSM12V", "V1COL12","NUMBER");
		assertR(db,"", "tosm12w",               "TOSM12W", "TOSM12W",               "TOSM12W",  "WCOL12F","NUMBER");
		assertR(db,"", this.SCHEMA2+".tosm12v", "TOSM12V", this.SCHEMA2+".TOSM12V", this.SCHEMA2+".TOSM12V", "V2COL12","VARCHAR2");
		db.close();
	}

	/** 
	 * Assert de todos los datos leidos con un esquema por defecto y un nombre de tabla 
	 * leyendo con getTable y los valores de Default y Full qualified
	 * comprobando tambien el nombre y tipo de la columna 0 */
	private void assertR(Connection db, String defaultSchema, String tableName, String expName, String expDefault, String expFull, String expColName, String expColType) {
		SchemaReader mr=new SchemaReaderJdbc(db,"",defaultSchema);
		mr.readTable(tableName); 
		assertEquals(expName.toUpperCase(),mr.getTableName().toUpperCase()); 
		assertEquals(expDefault.toUpperCase(),mr.getDefaultQualifiedTableName().toUpperCase()); 
		assertEquals(expFull.toUpperCase(),mr.getFullQualifiedTableName().toUpperCase());
		assertEquals(expColName,mr.getColumn(0).getColName());
		assertEquals(expColType,mr.getColumn(0).getDataType()); 
	}

	/**
	 * Qualificacion de claves ajenas leidas en una tabla, en funcion de si la columna que referenciada
	 * coincide o con la que referencia (catalogo/esquema)
	 * @throws IOException 
	 */
	@Test
	public void testReadForeignKey() throws SQLException {
		createTablesAndViews();
		//para cada forma de crear el schema reader (esquema 1, 2, sin esquema) 
		//hay que examinar cada una de las dos claves ajenas
		//FK1: tosm12 a tosm1
		//FK2: tosm1 a test4in2testdb2.tosm2
		//usa nombres cualificados para asegurarse que se accede a la tabla correcta
		//la fk siempre es la columna 1
		Connection db=newData(1);
		SchemaReader mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);
		mr.readTable(this.SCHEMA1+".tosm12");
		assertEquals("FK1",mr.getColumn(1).getForeignKeyName());
		assertEquals("TOSM1.COL1",mr.getColumn(1).getForeignKey());
		mr.readTable(this.SCHEMA1+".tosm1");
		assertEquals("FK2",mr.getColumn(1).getForeignKeyName());
		//debe ser qualificado pues apunta a esquema diferente
		assertEquals("TEST4IN2TESTDB2.TOSM2.COL2",mr.getColumn(1).getForeignKey());

		//cuando estoy en el esquema 2 es al reves, la fk interna debe se qualificada con esquema1
		//y la fk externa no pues referencia al esquema por defecto
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA2);
		mr.readTable(this.SCHEMA1+".tosm12");
		assertEquals("FK1",mr.getColumn(1).getForeignKeyName());
		assertEquals("TEST4IN2TESTDB1.TOSM1.COL1",mr.getColumn(1).getForeignKey());
		mr.readTable(this.SCHEMA1+".tosm1");
		assertEquals("FK2",mr.getColumn(1).getForeignKeyName());
		//debe ser qualificado pues apunta a esquema diferente
		assertEquals("TOSM2.COL2",mr.getColumn(1).getForeignKey());

		//cuando no hay esquema por defecto, en teoria todas las fks estarian qualificadas
		//pero es mas logico que se cualifiquen solo aquellas que hacen referencia a
		//la FK solo cuando esta en un esquema diferente al de la PK principal,
		//dejando el resto sin cualificar 
		mr=new SchemaReaderJdbc(db,"","");
		mr.readTable(this.SCHEMA1+".tosm12");
		assertEquals("FK1",mr.getColumn(1).getForeignKeyName());
		assertEquals("TOSM1.COL1",mr.getColumn(1).getForeignKey());
		mr.readTable(this.SCHEMA1+".tosm1");
		assertEquals("FK2",mr.getColumn(1).getForeignKeyName());
		//debe ser qualificado pues apunta a esquema diferente
		assertEquals("TEST4IN2TESTDB2.TOSM2.COL2",mr.getColumn(1).getForeignKey());

		db.close();
	}

	/**
	 * Lectura de condiciones check in en tablas con el mismo nombre, para asegurar que se lee de la tabla correcta
	 * @throws IOException 
	 */
	@Test
	public void testReadCheckConstrains() throws SQLException {
		createTablesAndViews();
		//SELECT * FROM TEST2IN2TESTDB2.USER_CONSTRAINTS WHERE SEARCH_CONDITION IS NOT NULL AND TABLE_NAME='TOSM12'
		//TOSM12 tienen constraint check de igual nombre en ambos esquemas, pero de valor distinto.
		//Tiene que leerse siempre la correcta (en el primer esquema columna 2 en segundo esquema columna 1
		Connection db=newData(1);
		SchemaReader mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);
		mr.readTable(this.SCHEMA1+".tosm12"); assertEquals("('Y','N')",mr.getColumn(2).getCheckInConstraint());
		//En el caso de las constraints cada uno ve las suyas, luego se veran las anteriores en vez de A,B que son las del esquema 2
		//mr.readTable(this.SCHEMA2+".tosm12"); assertEquals("('A','B')",mr.getColumn(1).checkInConstraint);
		mr.readTable(this.SCHEMA2+".tosm12"); assertEquals("('Y','N')",mr.getColumn(1).getCheckInConstraint());

		mr=new SchemaReaderJdbc(db,"",this.SCHEMA2);
		mr.readTable(this.SCHEMA1+".tosm12"); assertEquals("('Y','N')",mr.getColumn(2).getCheckInConstraint());
		//En el caso de las constraints cada uno ve las suyas, luego se veran las anteriores en vez de A,B que son las del esquema 2
		//pues depende del usuario que se ha conectado a la bd
		//mr.readTable(this.SCHEMA2+".tosm12"); assertEquals("('A','B')",mr.getColumn(1).checkInConstraint);
		mr.readTable(this.SCHEMA2+".tosm12"); assertEquals("('Y','N')",mr.getColumn(1).getCheckInConstraint());
		db.close();

		//Y el usuario 2 vera las suyas
		db=newData(2);
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA1);
		mr.readTable(this.SCHEMA2+".tosm12"); assertEquals("('A','B')",mr.getColumn(1).getCheckInConstraint());
		mr=new SchemaReaderJdbc(db,"",this.SCHEMA2);
		mr.readTable(this.SCHEMA2+".tosm12"); assertEquals("('A','B')",mr.getColumn(1).getCheckInConstraint());

		db.close();
	}

}
