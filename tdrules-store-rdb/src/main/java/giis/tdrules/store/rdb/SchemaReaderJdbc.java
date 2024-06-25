package giis.tdrules.store.rdb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.portable.util.JavaCs;
import giis.tdrules.store.ids.Quotation;
import giis.tdrules.store.ids.TableIdentifier;

/**
 * Implementation of the SchemaReader for a live jdbc connection
 */
public class SchemaReaderJdbc extends SchemaReader {
	private static final Logger log = LoggerFactory.getLogger(SchemaReaderJdbc.class);

	private static final String TABLE = "TABLE";
	private static final String VIEW = "VIEW";
	private static final String TYPE = "TYPE";
	private static final String TABLE_CAT = "TABLE_CAT";
	private static final String TABLE_SCHEM = "TABLE_SCHEM";
	private static final String TABLE_NAME = "TABLE_NAME";
	private static final String TABLE_TYPE = "TABLE_TYPE";
	private static final String COLUMN_NAME = "COLUMN_NAME";
	private static final String DATA_TYPE = "DATA_TYPE";
	private static final String TYPE_NAME = "TYPE_NAME";
	private static final String COLUMN_SIZE = "COLUMN_SIZE";
	private static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
	private static final String NULLABLE2 = "NULLABLE";
	private static final String COLUMN_DEF = "COLUMN_DEF";
	private static final String KEY_SEQ = "KEY_SEQ";
	private static final String PK_NAME = "PK_NAME";
	private static final String FK_NAME = "FK_NAME";

	private static final String DATE = "DATE";
	private static final String DATETIME = "DATETIME";
	private static final String SMALLDATETIME = "SMALLDATETIME";
	private static final String TIME = "TIME";
	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String WITH_TIME_ZONE = "WITH TIME ZONE";
	private static final String WITH_LOCAL_TIME_ZONE = "WITH LOCAL TIME ZONE";
	private static final String INTERVAL = "INTERVAL";
	private static final String DOUBLE = "DOUBLE";
	private static final String PRECISION = "PRECISION";

	protected Connection conn = null;
	// objeto MetaData a traves del cual se conseguira la informacion
	private DatabaseMetaData metaData = null;
	// identificacion de ultimo sgbd analizado (para no repetir logs=
	private static String lastPlatformInfo = "";

	// Cache de las tablas, evita buscar en los metadatos la misma tabla multiples veces
	// Ago 2020 no hace falta el orden, solo se buscan elementos por clave
	protected Map<String, SchemaTable> tablesCache = new HashMap<>();
	// Define si se obtienen claves ajenas entrantes
	protected boolean useIncomingFKs = false;
	// Nombre original de una tabla que se esta buscando (solo usado para los mensajes de error)
	private String tableOriginalName = "";
	// Tipos de datos en los que el tamanyo (precision y size) no es aplicable,
	// algunos de sqlserver, otros de posgres (text)
	private static final String[] typesWithoutSize = 
			new String[] { "bit", "int", "bigint", "smallint", "tintyint", "real", "text" };

	// Como guarda este gestor los identificadores
	private boolean storesLowerCaseIdentifiers = false;
	private boolean storesLowerCaseQuotedIdentifiers = false;
	private boolean storesUpperCaseIdentifiers = false;
	private boolean storesUpperCaseQuotedIdentifiers = false;

	protected SchemaReaderJdbc() {
		super();
	}
	
	/** 
	 * Crea un SchemaReader a partir de una conexion jdbc abierta
	 */
	public SchemaReaderJdbc(Connection conn) {
		super();
		this.conn=conn;
		initializeMetaData();
	}
	/** 
	 * Crea un SchemaReader a partir de una conexion jdbc abierta
	 * especificando el nombre de catalogo y de esquema de la base de datos
	 * (necesario para la lectura de los metadatos en Oracle cuando un usuario
	 * tiene acceso a varios esquemas).
	 */
	public SchemaReaderJdbc(Connection conn, String catalog, String schema) {
		this(conn);
		initializeCatalogSchema(catalog,schema);
	}
	
	protected void initializeCatalogSchema(String catalog, String schema) {
		//catalog y schema deben ser string vacios si se reciben como null
		this.setCatalog(catalog==null || "".equals(catalog) ? "" : preprocessIdentifier(catalog));
		this.setSchema(schema==null || "".equals(schema) ? "" : preprocessIdentifier(schema));
	}

	protected void initializeMetaData() {
		try {
			this.metaData = conn.getMetaData();
			populatePlatformInfo();
			populateIdentifierStorage();
		} catch (SQLException e) {
			throw new SchemaException("SchemaReaderJdbc.new: Can't get the Database Product Name", e);
		}
	}
	private void populatePlatformInfo() throws SQLException {
		this.setDbmsType(this.metaData.getDatabaseProductName());
		try { // las conexiones jdbc/odbc bridge no soportan el metodo
				// getDatabaseMajorVersion(), quedara indefinida
			this.setMajorVersion(this.metaData.getDatabaseMajorVersion());
		} catch (RuntimeException e2) {
			log.error("SchemaReaderJdbc: Exception getting DatabaseMajorVersion: ", e2);
		}
		this.setPlatformInfo("Database: " + this.metaData.getDatabaseProductName() + " - Version: "
				+ this.metaData.getDatabaseProductVersion() + " - Driver: " + this.metaData.getDriverName()
				+ " - Version: " + this.metaData.getDriverVersion());
		if (!lastPlatformInfo.equals(this.getPlatformInfo())) // log solo la primera vez o si cambia el sgbd
			log.info("Database Platform Info: " + this.getPlatformInfo());
		lastPlatformInfo = this.getPlatformInfo(); // NOSONAR
	}
	private void populateIdentifierStorage() {
		// Se han visto situaciones en las que se ha cerrado la conexion y luego se
		// consulta preprocessIdentifiers
		// que necesita conocer como se almacenan los identificadores, informacion que
		// esta en los metadatos
		// y que al menos Sql Server requiere una conexion activa.
		// Carga estos datos en el momento de la inicializacion de los metadatos
		try {
			this.storesUpperCaseQuotedIdentifiers = this.metaData.storesUpperCaseQuotedIdentifiers();
			this.storesUpperCaseIdentifiers = this.metaData.storesUpperCaseIdentifiers();
			this.storesLowerCaseQuotedIdentifiers = this.metaData.storesLowerCaseQuotedIdentifiers();
			this.storesLowerCaseIdentifiers = this.metaData.storesLowerCaseIdentifiers();
		} catch (SQLException e) {
			log.warn(
					"SchemaReaderJdbc.populateIdentifierStorage: Can't determine whether database stores or lowercase identifiers: "
							+ e.getMessage());
		}
	}

	/** Limpia la cache, normalmente usado para iniciar metodos de prueba */
	public void clearCache() {
		this.tablesCache = new HashMap<>();
	}
	
	/**
	 * Especifica que se deben obtener de las tablas las claves ajenas entrantes
	 * (usada para conocer todas las tablas relacionadas en un esquema. Tener en
	 * cuenta que estas claves ajenas entrantes no se guardan completamente, solo
	 * mantienen los nombres de las tablas que se relacionan
	 */
	public void setUseIncomingFKs(boolean useIncomingFKs) {
		this.useIncomingFKs = useIncomingFKs;
	}

	/**
	 * Devuelve la lista con el nombre de todas las tablas de la base de datos en el
	 * catalogo/esquema especificados por defecto donde el nombre de la tabla
	 * comienza por el string indicado
	 */
	@Override
	public List<String> getTableList(boolean includeTables, boolean includeViews, boolean includeTypes,
			String startingWith) {
		log.trace("SchemaReaderJdbc.readTableList");
		String[] types = null; // sera null si no se seleccionan ni tablas ni vistas ni tipos
		List<String> typesList = new ArrayList<>();
		if (includeTables)
			typesList.add(TABLE);
		if (includeViews)
			typesList.add(VIEW);
		if (includeTypes)
			typesList.add(TYPE);
		// NOTA: Postgres incluye los UDTs como TYPE al buscar tablas, aunque esto no
		// esta en la documentacion
		// de jdbc metadata. Si en otros gestores no lo hacen asi, buscar tablas con la
		// query usada para buscar las columnas de los Types
		if (typesList.size() > 0) //NOSONAR
			types = JavaCs.toArray(typesList);
		List<String> ls = new java.util.ArrayList<>();
		ResultSet rs = null;
		try {
			readMetadataTables(types, ls, startingWith);
		} catch (SQLException e) {
			throw (new SchemaException("SchemaReaderJdbc.getTableList", e));
		} finally {
			closeResultSet(rs);
		}
		return ls;
	}

	@Override
	public List<String> getTableList(boolean includeTables, boolean includeViews, String startingWith) {
		return getTableList(includeTables, includeViews, false, startingWith);
	}
	@Override
	public List<String> getTableList(boolean includeTables, boolean includeViews) {
		return getTableList(includeTables, includeViews, false, "");
	}

	private void readMetadataTables(String[] types, List<String> ls, String startingWith) throws SQLException {
		ResultSet rs = this.metaData.getTables(uncoalesce(this.getCatalog()), uncoalesce(this.getSchema()),
				startingWith + "%", types);
		while (rs.next()) {
			String tableCat = rs.getString(TABLE_CAT);
			String tableSchem = rs.getString(TABLE_SCHEM);
			String tableName = rs.getString(TABLE_NAME);
			String tableType = normalizeJdbcTableType(rs.getString(TABLE_TYPE));
			// excluye tablas y vistas de sistema, que deben ignorarse
			boolean include = true;
			if (tableType.equals(VIEW) && this.getDbmsType().isSystemView(tableName))
				include = false;
			if (tableType.equals(TABLE) && this.getDbmsType().isSystemTable(tableName))
				include = false;
			// excluye esquemas de sistema si no se ha especificado un esquema por defecto
			if (coalesce(this.getSchema(), "").equals("") && this.getDbmsType().isSystemSchema(tableSchem))
				include = false;

			// Comprobacion de errores, si esta tabla que se va a anyadir ya se tenia en la
			// lista excepcion por tabla duplicaca
			if (ls.contains(tableName))
				throw new SchemaException("SchemaReaderJdbc.getTableList: Found more than one table or view: "
						+ tableName
						+ "\nTip: you may restrict the search in the metadata by specifying the schema name");
			// anyade la tabla si procede
			if (include) {
				log.trace("  Table/view ADDED: " + tableCat + " " + tableSchem + " " + tableName + " " + tableType);
				ls.add(tableName);
			} else {
				log.trace("  Table/view NOT added: " + tableCat + " " + tableSchem + " " + tableName + " " + tableType);
			}
		}
		rs.close();
	}

	// cuando lee tablas, jdbc obtiene TABLE en el campo DATA_TYPE, pero algunos
	// drivers (H2 v2) retornan BASE TABLE
	// usar este metodo siempre que se lea el tipo de tabla desde los metadatos
	private String normalizeJdbcTableType(String type) {
		return JavaCs.equalsIgnoreCase("base table", type) ? TABLE : type;
	}

	/**
	 * Preprocesa un identificador (normalmente tabla) para adecuarlo segun las
	 * caracteristicas exigidas por el SGBD y tratar comillas. Admite tambien
	 * identificadores entre corchetes (sqlserver). De esta forma los
	 * identificadores de tablas tal como estan en SQL pueden ser interpretados para
	 * buscar las tablas en el esquema
	 */
	private String preprocessIdentifier(String name) {
		name = name.trim();
		if ("".equals(name)) // puede invocarse con string vacio, evita comprobar los metadatos
			return "";
		boolean isQuoted = Quotation.isQuoted(name, '"', '"') || Quotation.isQuoted(name, '[', ']');
		// antes que nada, si es no quoted mira si tiene espacios en blanco, en cuyo
		// caso
		// se sabe que debe ser quoted, aunque no se hayan recibido las comillas
		if (!isQuoted && name.contains(" "))
			isQuoted = true;
		// Pasa a mayusculas/minusculas en los casos en que la bd almacena de esta forma
		// para permitir que se localice correctamente la tabla cuando el case no
		// coincide con el que se tiene en tabName (p.e. en oracle es mayuscula)
		if (isQuoted) {
			// primero quita las comillas
			if (name.charAt(0) == '"')
				name = Quotation.removeQuotes(name, '"', '"');
			else if (name.charAt(0) == '[')
				name = Quotation.removeQuotes(name, '[', ']');

			if (this.storesUpperCaseQuotedIdentifiers)
				name = name.toUpperCase();
			else if (this.storesLowerCaseQuotedIdentifiers)
				name = name.toLowerCase();
		} else {
			if (this.storesUpperCaseIdentifiers)
				name = name.toUpperCase();
			else if (this.storesLowerCaseIdentifiers)
				name = name.toLowerCase();
		}
		// si no se dan los casos anteriores no transforma el nombre de la tabla
		return name;
	}

	/**
	 * Lee todos los metadatos de la tabla o vista indicada
	 */
	public SchemaTable readTable(String tabName, boolean throwExceptionIfNotFound) {
		this.tableOriginalName = tabName;
		tabName = preprocessIdentifier(tabName);
		this.resetAttributes(); // elimina contenido previo
		// crea un estructura de nombre de tabla que almacena los difersos componentes
		// que ha sido recibida en la forma [[catalog.]schema.]table
		QualifiedTableName qtn = getNewQualifiedTableName(this.getCatalog(), this.getSchema(), tabName);
		log.trace("SchemaReaderJdbc.readTable " + tabName + ". " + qtn.toString());
		String cacheKey = qtn.toString();

		// Si se ha indicado que se utilice cache, busca esta tabla en la cache y si la
		// encuentra actualiza la tabla actual, si no continuara buscando
		if (this.useCache && this.tablesCache.containsKey(cacheKey)) {
			log.trace("SchemaReaderJdbc.readTable " + tabName + ". Found in the metadata cache");
			this.currentTable = this.tablesCache.get(qtn.toString());
			return this.currentTable;
		}

		// guarda este valor como nombre de la tabla
		this.setTableGivenId(qtn);
		// Lee los metadatos para determinar el tipo de tabla y el nombre global
		int foundCount = this.findTableAndSetType(qtn, throwExceptionIfNotFound);
		// Lee los metadatos, de forma diferente si se trata de tabla o vista
		if (foundCount == 1) {
			if (this.isTable())
				readBaseTable(qtn);
			else if (this.isView())
				readViewTable(qtn);
			else if (this.isType())
				readTypeTable(qtn);
			// Si se ha indicado que se utilice cache, guarda esta tabla en la cache para
			// usos posteriores
			if (this.useCache)
				this.tablesCache.put(cacheKey, this.getCurrentTable());
			return this.getCurrentTable();
		} else // foundCount==0 (no es >1 pues findTableAndSetType habra producido la
				// excepcion)
			return null;
	}
	
	/**
	 * Encapsula la informacion de un nombre de tabla cualificado con catalogo y
	 * esquema, teniendo en cuenta el catalogo y esquema definidos por defecto.
	 */
	public class QualifiedTableName extends TableIdentifier {

		public QualifiedTableName(String defCat, String defSch, String name) {
			super(defCat, defSch, name, false);
			// normaliza los componentes teniendo en cuenta las capacidades de este SGBD
			this.setCat(preprocessIdentifier(this.getCat()));
			this.setSch(preprocessIdentifier(this.getSch()));
			this.setTab(preprocessIdentifier(this.getTab()));
		}
	}

	public QualifiedTableName getNewQualifiedTableName(String defCat, String defSch, String name) {
		return new QualifiedTableName(defCat, defSch, name);
	}

	/**
	 * Localiza un tipo de tabla (tabla, vista) tal y como es obtenido de los
	 * metadatos y lo almacena en la tabla en curso junto con su nombre e
	 * informacion de esquema. Esete es el paso previo para la obtencion de
	 * informacion de una tabla puesto que permite conocer su tipo y detectar si es
	 * visible o no en el esquema
	 * 
	 * @return numero de tablas encontradas (0 si no ha sido encontrada)
	 */
	private int findTableAndSetType(TableIdentifier qtn, boolean throwExceptionIfNotFound) {
		// llama al mismo metodo usado en la busqueda de tablas para conseguir el valor
		// de la
		// columna TABLE_TYPE que es el que devuelve.
		int foundCount = 0;
		ResultSet rs = null;
		try {
			// en qTableName se tienen los nombres completos del esquema y catalogo precisos para buscar la tabla
			// ojo, aqui con Compiere daba error tras haber ejecutado muchas queries,
			// se soluciono pasando OPEN_CURSORS de 300 a 600
			rs = this.metaData.getTables(uncoalesce(qtn.getCat()), uncoalesce(qtn.getSch()), qtn.getTab(), null);
			while (rs.next()) { // debe existir una fila solamente, si no, habra una excepcion
				String tableCat = rs.getString(TABLE_CAT);
				String tableSchem = rs.getString(TABLE_SCHEM);
				String tableName = rs.getString(TABLE_NAME);
				String tableType = normalizeJdbcTableType(rs.getString(TABLE_TYPE));
				// pone el id global tal como se encuentra en el esquema
				QualifiedTableName gtn = getNewQualifiedTableName(tableCat, tableSchem, tableName);
				this.setTableGlobalId(gtn);
				this.setTableCatalogSchema(tableCat, tableSchem);
				if (tableType.equals(TABLE))
					this.setTableTypeTable();
				else if (tableType.equals(VIEW))
					this.setTableTypeView();
				else if (tableType.equals(TYPE))
					this.setTableTypeUdt();
				log.trace("  Global name: " + gtn.getFullQualifiedTableName() + " type: " + tableType);
				foundCount++;
			}
			rs.close();
			// Se ha de haber localizado exactamente una fila
			if (foundCount == 0 && throwExceptionIfNotFound) // no existe la tabla
				throw (new SchemaException(
						"SchemaReaderJdbc.setTableType: Can't find table or view: " + this.tableOriginalName));
			else if (foundCount > 1)
				throw (new SchemaException(
						"SchemaReaderJdbc.setTableType: Found more than one table or view: " + this.tableOriginalName
								+ "\nTip: you may restrict the search in the metadata by specifying the schema name"));
		} catch (SQLException e) {
			throw new SchemaException("SchemaReaderJdbc.setTableType", e);
		} finally {
			closeResultSet(rs);
		}
		return foundCount;
	}

	/**
	 * Lee todos los metadatos de la tabla fisica indicada (no vista) utilizando
	 * DatabaseMetadata. No comprueba si se trata de una vista, pero si se ejecuta
	 * sobre una vista es muy probable que los tipos de datos y atributos sean
	 * incorrectos, ya que DatabaseMetadata o obtiene correctamente los valores (al
	 * menos en oracle)
	 */
	private void readBaseTable(TableIdentifier qtn) {
		String cat = uncoalesce(qtn.getCat());
		String sch = uncoalesce(qtn.getSch());
		String tab = qtn.getTab();
		try {
			// obtiene la lista de todas las columnas
			readMetadataColumns(cat, sch, tab);

			// obtiene la lista de todas las claves primarias
			readMetadataPks(cat, sch, tab);

			// busca campos autoincrementales si no se encontraron al examinar las columnas
			updateAutoIncrementColumns();

			// Obtiene las claves ajenas (clves salientes)
			if (!this.isCassandra()) // Non relational, may fail with some jdbc drivers/wrappers (#215)
				readMetadataFks(cat, sch, tab);

			// Obtiene las claves ajenas entrantes (procedentes de otras tablas que referencian esta)
			// El procedimiento es similar a las claves ajenas (salientes), pero no obtiene informacion de columnas
			if (this.useIncomingFKs && !this.isCassandra())
				readMetadataIncomingFks(cat, sch, tab);

			// To be Deprecated: Obtiene la lista de todas las constraints de tipo CHECK IN
			// en oracle
			if (this.isOracle())
				readOracleCheckConstraints(qtn);

		} catch (SQLException e) {
			log.error("SchemaReaderJdbc.getTableList: ", e);
			throw new SchemaException("SchemaReaderJdbc.getTableList: Error reading table metadata for "
					+ qtn.getFullQualifiedTableName(), e);
		}

		// Obtiene la lista completa de check constraints, esta no causara excepcion
		readBaseTableCheckConstraints(cat, sch, tab);
	}

	private void readMetadataColumns(String cat, String sch, String tab) throws SQLException {
		ResultSet rs = this.metaData.getColumns(cat, sch, tab, "%");
		while (rs.next()) {
			String tableCat = rs.getString(TABLE_CAT);
			String tableSchem = rs.getString(TABLE_SCHEM);
			String tableName = rs.getString(TABLE_NAME);
			String columnName = rs.getString(COLUMN_NAME);
			int typeCode = rs.getInt(DATA_TYPE);
			String typeName = rs.getString(TYPE_NAME);
			int columnSize = rs.getInt(COLUMN_SIZE);
			int decimalDigits = rs.getInt(DECIMAL_DIGITS);
			int nullable = rs.getInt(NULLABLE2);
			String columnDef = rs.getString(COLUMN_DEF);
			log.trace("  Column: " + tableCat + " " + tableSchem + " " + tableName + " " + columnName + " " + typeName
					+ " " + columnSize + " " + decimalDigits + " " + nullable + " "
					+ (columnDef != null ? columnDef : ""));
			// El tipo de la tabla no se puede extraer de esta consulta, sino de la de la
			// lista de tablas. Por ello en vez de guardar un atributo se override
			// los metodos isTable e isView para que hagan la consulta necesaria para ello
			// (el tipo de tabla solo se consulta mediante metodos)

			// Datos de la columna
			SchemaColumn col = new SchemaColumn();
			col.setColName(columnName);
			col.setDataType(this.getDbmsType().mapAliasToDataType(typeName)); // usa mapeo especifico de dbms si existe
			col.setDataSubType("");
			col.setDataTypeCode(typeCode);
			// Determina campos autoincrementales obtenidos a partir del tipo de datos de la columna
			this.updateAutoIncrementColumn(col);
			col.setColSize(columnSize);
			col.setDecimalDigits(decimalDigits);
			col.setNotNull(nullable != DatabaseMetaData.columnNullable);
			// Parche para SQLite, devuelve nombres como CHAR(n) en vez de colocar n en
			// columnSize (pone un valor muy grande)
			if (this.isSqlite())
				col.reparseNameWithPrecision();

			// en los tipos compuestos hay que determinar el tipo de estructura, actualizando algunos datos
			updateCompositeType(col);

			// valor por defecto, eliminando parentesis si tiene (algunos SGBD guardan el parentesis (puede haber dos)
			if (columnDef != null) {
				col.setDefaultValue(columnDef);
				col.setDefaultValue(Quotation.removeQuotes(col.getDefaultValue(), '(', ')'));
				col.setDefaultValue(Quotation.removeQuotes(col.getDefaultValue(), '(', ')'));
			}
			// La clave se determina a continuacion, demomento la inicio a false
			col.setKey(false);
			// En aquellos tipos de datos en que no es necesario conocer
			// el tamanyo, pone los valores a cero
			correctTypeAttributes(col);
			// finalmente anyade esta columna
			this.addColumn(col);
		}
		rs.close();
	}
	
	// Esto deberia implementarse con jdbc, metodo GetAttributes (similar a getColumns)
	// pero algunos drivers no lo implementan todavia
	// ej: postgres driver 42.4, database 14.5 (updated at mid 2022).
	// Usare information_schema
	// NOTE: solo algunas pruebas en postgres, puede haber inconsistencias con getBaseTable
	private void readTypeTable(TableIdentifier qtn) {
		try {
			readUdtMetadataColumns(uncoalesce(qtn.getCat()), uncoalesce(qtn.getSch()), qtn.getTab());
		} catch (SQLException e) {
			log.error("SchemaReaderJdbc.readUdtTable: ", e);
			throw (new SchemaException( "SchemaReaderJdbc.getUdtTable: Error reading table metadata for "
					+ qtn.getFullQualifiedTableName(), e));
		}
	}

	private void readUdtMetadataColumns(String cat, String sch, String tab) throws SQLException {
		String sql = "select UDT_NAME, ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT, IS_NULLABLE, "
				+ " DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, DATETIME_PRECISION, INTERVAL_PRECISION, "
				+ "ATTRIBUTE_UDT_NAME" + " from INFORMATION_SCHEMA.ATTRIBUTES" + " where UDT_NAME='" + tab + "'"
				+ ifnn(cat, " and UDT_CATALOG='" + cat + "'")
				+ ifnn(sch, " and UDT_SCHEMA='" + sch + "'") + " order by ORDINAL_POSITION";
		ResultSet rs = this.query(sql, 0);
		while (rs.next()) {
			String tableName = rs.getString("UDT_NAME");
			String columnName = rs.getString("ATTRIBUTE_NAME");
			log.trace("  UDT Column: " + tableName + "." + columnName);
			SchemaColumn col = new SchemaColumn();
			col.setColName(rs.getString("ATTRIBUTE_NAME"));
			col.setDataType(this.getDbmsType().mapAliasToDataType(rs.getString("ATTRIBUTE_UDT_NAME")));
			this.updateCompositeType(col); // podria estar basado en otros tipos

			// precision/scale en campos diferentes segun el tipo de datos
			String[] precisions = new String[] { rs.getString("CHARACTER_MAXIMUM_LENGTH"),
					rs.getString("NUMERIC_PRECISION"), rs.getString("DATETIME_PRECISION"),
					rs.getString("INTERVAL_PRECISION") };
			int precision = 0;
			if (precisions[0] != null)
				precision = Integer.parseInt(precisions[0]);
			else if (precisions[1] != null)
				precision = Integer.parseInt(precisions[1]);
			else if (precisions[2] != null)
				precision = Integer.parseInt(precisions[2]);
			else if (precisions[3] != null)
				precision = Integer.parseInt(precisions[3]);
			col.setColSize(precision);
			int scale = rs.getInt("NUMERIC_SCALE");
			col.setDecimalDigits(scale);
			correctTypeAttributes(col);
			// No se ponen mas atributos, postgres no permite poner otras restricciones.
			// Revisar si en algun momento se realiza para otro sgbd
			this.addColumn(col);
		}
		rs.close();
	}

	private void updateCompositeType(SchemaColumn col) {
		// La existencia de tipos compuestos se determinan por el numero de tipo de dato
		// en java
		// y se guardan de momento en compositeType
		if (col.getDataTypeCode() == Types.ARRAY) {
			col.setCompositeType("array"); // el tipo de dato coincide con el de cada elemento
		} else if (col.getDataTypeCode() == Types.STRUCT) {
			col.setCompositeType("type"); // es un objeto on un user defined type (row en sql92)
			col.setColSize(0);
			col.setDecimalDigits(0);
		} else
			col.setCompositeType("");
	}

	private void readMetadataPks(String cat, String sch, String tab) throws SQLException {
		ResultSet rs = this.metaData.getPrimaryKeys(cat, sch, tab);
		while (rs.next()) {
			String tableCat = rs.getString(TABLE_CAT);
			String tableSchem = rs.getString(TABLE_SCHEM);
			if (true) {
				String tableName = rs.getString(TABLE_NAME);
				String columnName = rs.getString(COLUMN_NAME);
				String keySeq = rs.getString(KEY_SEQ);
				String pkName = rs.getString(PK_NAME);
				log.trace("  Primary key: " + tableCat + " " + tableSchem + " " + tableName + " " + columnName + " "
						+ keySeq + " " + pkName);
				// busco la columna correspondiente en la lista anterior y actualizo su valor de
				// clave
				for (int i = 0; i < this.getColumnCount(); i++)
					if (columnName.equals(this.getColumn(i).getColName()))
						this.getColumn(i).setKey(true);
			}
		}
		rs.close();
	}

	private void readMetadataFks(String cat, String sch, String tab) throws SQLException {
		ResultSet rs = this.metaData.getImportedKeys(cat, sch, tab);
		while (rs.next()) {
			String pktableCat = rs.getString("PKTABLE_CAT");
			String pktableSchem = rs.getString("PKTABLE_SCHEM");
			String pktableName = rs.getString("PKTABLE_NAME");
			String pkcolumnName = rs.getString("PKCOLUMN_NAME");
			String fktableCat = rs.getString("FKTABLE_CAT");
			String fktableSchem = rs.getString("FKTABLE_SCHEM");
			String fktableName = rs.getString("FKTABLE_NAME");
			String fkcolumnName = rs.getString("FKCOLUMN_NAME");
			String keySeq = rs.getString(KEY_SEQ);
			String fkName = rs.getString(FK_NAME);
			// solo considero fks en el esquema especificado
			log.trace("  Foreign key: " + pktableCat + " " + pktableSchem + " " + pktableName + " " + pkcolumnName + " "
					+ fktableCat + " " + fktableSchem + " " + fktableName + " " + fkcolumnName + " " + fkName + " "
					+ keySeq);
			// busco la columna a la que corresponde esta fk y actualiza el atributo en la forma tabla.columna referenciada
			for (int i = 0; i < this.getColumnCount(); i++)
				if (JavaCs.equalsIgnoreCase(fkcolumnName, this.getColumn(i).getColName())) {
					this.getColumn(i).setForeignKeyName(fkName);
					// No cualifica el nombre de la fk solo en el caso de que la tabla referenciada
					// este en el mismo catalogo/esquema y que estos coincidan con los valores
					// por defecto o no esten especificados
					if (matchPkFk(pktableCat, pktableSchem, fktableCat, fktableSchem)) {
						this.getColumn(i).foreignKeyTable = pktableName;
					} else {
						// pone el nombre completo:  Crea un objeto QualifiedTableName para obtener
						// el nombre correcto dependiendo del contexto
						TableIdentifier fkFullName = new TableIdentifier(
								this.getCatalog(), this.getSchema(), pktableCat, pktableSchem, pktableName, false);
						this.getColumn(i).foreignKeyTable = fkFullName.getDefaultQualifiedTableName(
								this.getCatalog(), this.getSchema());
					}
					this.getColumn(i).foreignKeyColumn = pkcolumnName;
					this.getColumn(i).foreignKeyTableSchemaIdentifier = new TableIdentifier(pktableCat,
							pktableSchem, pktableName, true);
				}
			// Creo/obtengo la estructura correspondiente a la FK (de uso en el paquete dbr)
			// si se crea se hace con los nombres de las tablas full qualified
			SchemaForeignKey fk = this.getCurrentTable().getFK(fkName);
			if (fk == null) {
				fk = new SchemaForeignKey(this.getCurrentTable(), fkName, this.getCurrentTable().getGlobalId(),
						new TableIdentifier(pktableCat, pktableSchem, pktableName, true));
				this.getCurrentTable().getFKs().add(fk);
			}
			// anyade los datos de las columnas referenciadas
			fk.addColumn(fkcolumnName, pkcolumnName);
		}
		rs.close();
	}

	private boolean matchPkFk(String pktableCat, String pktableSchem, String fktableCat, String fktableSchem) {
		return (JavaCs.equalsIgnoreCase(this.getCatalog(), coalesce(pktableCat, "")) || this.getCatalog().equals("") )  
				&& (JavaCs.equalsIgnoreCase(this.getSchema(), coalesce(pktableSchem, "")) || this.getSchema().equals("") ) 
				&& coalesce(pktableCat, "").equals(coalesce(fktableCat, "")) 
				&& coalesce(pktableSchem, "").equals(coalesce(fktableSchem, ""));
	}

	private void readMetadataIncomingFks(String cat, String sch, String tab) throws SQLException {
		ResultSet rs = this.metaData.getExportedKeys(cat, sch, tab);
		while (rs.next()) {
			String efktableCat = rs.getString("FKTABLE_CAT");
			String efktableSchem = rs.getString("FKTABLE_SCHEM");
			String efktableName = rs.getString("FKTABLE_NAME");
			String efkName = rs.getString(FK_NAME);
			// solo considero fks en el esquema especificado
			log.trace("  Foreign key (incoming): " + efktableCat + " " + efktableSchem + " " + efktableName + " "
					+ efkName + " ");
			// Creo/obtengo la estructura correspondiente a la FK en las incoming solamente 
			// se crea la estructura que relaciona las tablas, no hay asociacion de columnas
			SchemaForeignKey fk = new SchemaForeignKey(null, efkName,
					new TableIdentifier(efktableCat, efktableSchem, efktableName, true),
					this.getCurrentTable().getGlobalId());
			this.getCurrentTable().getIncomingFKs().add(fk);
		}
		rs.close();
	}

	private void readOracleCheckConstraints(TableIdentifier qtn) throws SQLException {
		//El sql busca en user_constraints para esta tabla aquellas con search_condition no nula
		//Dependiendo del catalogo/esquema se se debera cualificar la tabla user_constraints
		//correspondiendo al esquema de la tabla que se esta tratando
		String sql = "select * from USER_CONSTRAINTS where  table_name='";
		sql += qtn.getTab(); // no debe ser fully qualified
		sql += "' and search_condition is not null";
		ResultSet rs = query(sql);
		while (rs.next()) {
			String searchCond = rs.getString("SEARCH_CONDITION");
			searchCond = searchCond.replace("  ", " ").trim();
			// parse de la condicion para ver si es de la forma columna IS condicion
			String[] parts = JavaCs.splitByChar(searchCond, ' ');
			// Fixed in SqlCore version, supports in (..) and in(..)
			if (parts.length >= 2
					&& (parts[1].toLowerCase().startsWith("in") || parts[1].toLowerCase().startsWith("in("))) {
				// si la segunda parte comienza por in entonces es una de las constraints que interesan
				String col = parts[0]; // nombre de la columna
				// resto a partir del in, ojo, si hay varios espacios en blanco estos se convierten en uno solo
				StringBuilder cond = new StringBuilder();
				for (int i = 1; i < parts.length; i++)
					cond.append(parts[i] + " ");
				String inCondition = JavaCs.substring(cond.toString(), 2, cond.toString().length());
				// busco la columna a la que corresponde esta constraint y actualiza
				// el atributo en de checkInConstraint
				for (int i = 0; i < this.getColumnCount(); i++)
					if (JavaCs.equalsIgnoreCase(col, this.getColumn(i).getColName()))
						this.getColumn(i).setCheckInConstraint(inCondition.trim());
				log.trace("  Check in constraint: " + searchCond);
			}
		}
		closeResultSet(rs);
	}
	
	private void readBaseTableCheckConstraints(String cat, String sch, String tab) {
		// Obtencion general de las constraints de la base de datos usando
		// information_schema
		// select * from INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE cu inner join
		// INFORMATION_SCHEMA.CHECK_CONSTRAINTS c on
		// c.CONSTRAINT_NAME=cu.CONSTRAINT_NAME
		String sql;
		ResultSet rs = null;
		try { // si se usa una bd que no soporta information_schema no fallara
			sql = getBaseTableCheckSql(cat, sch, tab);
			// https://dataedo.com/kb/query/postgresql/list-check-constraints-in-database
			rs = query(sql);
			if (this.isSqlite())
				readBaseTableCheckConstraintsSqlite(rs);
			else
				readBaseTableCheckConstraintsOther(rs);
		} catch (Exception e) {
			// evita fallo
		} finally {
			closeResultSet(rs);
		}
	}
	private void readBaseTableCheckConstraintsSqlite(ResultSet rs) throws SQLException {
		if (rs.next()) { // puede que no haya informacion, p.e. si se hace sobre una vista
			List<String> lst = findCheck(rs.getString("sql"));
			for (int i = 0; i < lst.size(); i++) {
				SchemaCheckConstraint check = new SchemaCheckConstraint();
				check.setColumn("");
				check.setName("");
				check.setConstraint(lst.get(i));
				this.getCurrentTable().addCheckConstraint(check);
			}
		}
	}
	private void readBaseTableCheckConstraintsOther(ResultSet rs) throws SQLException {
		while (rs.next()) {
			SchemaCheckConstraint check = new SchemaCheckConstraint();
			check.setColumn(rs.getString(COLUMN_NAME) == null ? "" : rs.getString(COLUMN_NAME));
			check.setName(rs.getString("CONSTRAINT_NAME"));
			check.setConstraint(rs.getString("CHECK_CLAUSE"));
			if (!check.getConstraint().toLowerCase().endsWith(" is not null"))
				// omite estas constraints que se generan en el caso de oracle
				this.getCurrentTable().addCheckConstraint(check);
		}
	}

	private String getBaseTableCheckSql(String cat, String sch, String tab) {
		if (this.isOracle()) // no soporta INFORMATION_SCHEMA, incluira tambie las is null y sin nombre de
								// tabla
			// mismas columnas que con information schema pero otra query
			return "select null AS COLUMN_NAME, CONSTRAINT_NAME, SEARCH_CONDITION AS CHECK_CLAUSE from USER_CONSTRAINTS"
					+ " where TABLE_NAME='" + tab + "' and CONSTRAINT_TYPE='C' order by CONSTRAINT_NAME";
		else if (this.isSqlite())
			// diferentes columnas que con information schema, tratamiento diferente
			return "select sql from sqlite_master where type='table' and lower(name)='" + tab.toLowerCase() + "'";
		else
			return "select cu.COLUMN_NAME, cu.CONSTRAINT_NAME, c.CHECK_CLAUSE"
					+ " from INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE cu"
					+ " inner join INFORMATION_SCHEMA.CHECK_CONSTRAINTS c" + " on c.CONSTRAINT_NAME=cu.CONSTRAINT_NAME "
					+ ifnn(cat, " and c.CONSTRAINT_CATALOG=cu.CONSTRAINT_CATALOG")
					+ ifnn(sch, " and c.CONSTRAINT_SCHEMA=cu.CONSTRAINT_SCHEMA")
					+ " where TABLE_NAME='" + tab + "'"
					+ ifnn(cat, " and TABLE_CATALOG='" + cat + "'")
					+ ifnn(sch, " and TABLE_SCHEMA='" + sch + "'")
					+ " order by cu.COLUMN_NAME, c.CONSTRAINT_NAME";
	}

	public List<String> findCheck(String s) {
		List<String> check = new ArrayList<>();
		int fromIndex = 0;
		int pos = -1;
		while ((pos = s.toLowerCase().indexOf("check", fromIndex)) != -1) {
			int brackets = 0;
			boolean inside = false;
			for (int i = pos + 5; i < s.length(); i++) {
				brackets += bracketLevelIncrement(s.charAt(i));
				if (brackets == 1)
					inside = true;
				if (inside && brackets == 0) {
					check.add(JavaCs.substring(s, pos + 5, i + 1));
					fromIndex = i + 1;
					break;
				}
			}
		}
		return check;
	}

	private int bracketLevelIncrement(char c) {
		if (c == '(')
			return 1;
		else if (c == ')')
			return -1;
		else
			return 0;
	}

	/**
	 * Lee todos los metadatos de la vista indicada (no table) utilizando
	 * ResultsetMetadata. No comprueba si se trata de una vista, pero si se ejecuta
	 * sobre una taba es posible que los tipos de datos y atributos sean
	 * incorrectos, ya que en este caso se debe usar la clase especifica de
	 * DatabaseMetadata
	 */
	private void readViewTable(TableIdentifier qtn) {
		log.trace("SchemaReaderJdbc.readViewTable : table is view, reading ResultSetMetaData");
		ResultSet rs = null;
		String sqlView = "";
		try {
			// obtiene la lista de todas las columnas mediante un select (solo necesita leer una fila)
			// Nota: Utiliza queryConnected para usar un resultset estandar,
			// pues query() ha dado problemas con oracle en la BD de Compiere en vista C_DUNNING_HEADER_V
			// posiblemente por usar el CachedRowSet estandar y no el de Oracle
			// cualifica completamente la query puesto que se ejecutara una query con una conexion que puede
			// tener por defecto un esquema diferente del usado en este SchemaReaderJdbc
			sqlView = "SELECT * FROM " + qtn.getFullQualifiedTableName(this.getCatalog(), this.getSchema());
			sqlView = this.getDbmsType().getSqlLimitRows(sqlView, 0);
			rs = query(sqlView, 1);
			ResultSetMetaData rsmd = rs.getMetaData();
			log.trace("***** Reading view " + sqlView);
			// ahora determina todos los atributos de las columnas, ojo, deben ser los
			// mismos que en readBaseTable
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				SchemaColumn col = new SchemaColumn();
				col.setColName(rsmd.getColumnName(i));
				col.setDataTypeCode(rsmd.getColumnType(i));
				col.setDataType(this.getDbmsType().mapAliasToDataType(rsmd.getColumnTypeName(i)));
				this.updateAutoIncrementColumn(col);
				col.setColSize(rsmd.getPrecision(i));
				col.setDecimalDigits(rsmd.getScale(i));
				col.setNotNull(rsmd.isNullable(i) != DatabaseMetaData.columnNullable);
				// no puedo saber si es clave o clave ajena, supongo que no
				col.setKey(false);
				col.setForeignKey("");
				col.setForeignKeyName("");
				// En aquellos tipos de datos en que no es necesario conocer
				// el tamanyo, pone los valores a cero
				correctTypeAttributes(col);
				// finalmente anyade esta columna
				this.addColumn(col);
			}
			// busca campos autoincrementales si no se encontraron al examinar las columnas
			this.updateAutoIncrementColumns();
		} catch (Throwable e) { // NOSONAR
			// Si ha habido algun problema (p.e. en Oracle al leer algunas vistas del
			// sistema como p.e. DBA_ANALYZE_OBJECTS la query da error)
			// Lee los metadatos como si fuera una tabla base
			log.warn("Can't read view '" + qtn.getTab() + "' as query. Switch to reading as base table. Exception is: "
					+ e.getMessage());
			log.info(sqlView);
			readBaseTable(qtn);
		} finally {
			closeResultSet(rs);
		}
	}

	/**
	 * Procesa una columna de un tipo dado transformando el tipo de datos y/o la
	 * caracteristica de autoincremental
	 */
	private void updateAutoIncrementColumn(SchemaColumn col) {
		// Added in SqlCore version
		String identityType = this.getDbmsType().getDataTypeIdentity("");
		if (JavaCs.equalsIgnoreCase(col.getDataType(), identityType)) {
			col.setAutoIncrement(true);
			return;
		}
		// Caso particular. Al menos en SQLServer con jdbc los identity (p.e. int)
		// aparecen como int identity.
		// Revisa el tipo de datos y si la ultima palabra es identity la elimina
		String identity = this.getDbmsType().getDataTypeIdentitySuffix();
		if (!"".equals(identity) && col.getDataType().endsWith(identity)) {
			col.setDataType(
					JavaCs.substring(col.getDataType(), 0, col.getDataType().length() - identity.length()).trim());
			col.setAutoIncrement(true);
		}
	}

	/**
	 * Caso particular cuando para determinar la clave autoincremental hay una query definida para ello
	 */
	private void updateAutoIncrementColumns() throws SQLException {
		if ("".equals(this.getDbmsType().getDataTypeIdentitySql("", "")))
			return;
		
		// Obtiene la posicion de la pk, debe haber solo una
		int pkIndex = -1;
		for (int i = 0; i < this.getColumnCount(); i++)
			if (this.getColumn(i).isKey()) {
				if (pkIndex == -1)
					pkIndex = i;
				else
					return; // more than one pk, can't have autoincrement
			}
		if (pkIndex == -1)
			return;
		
		// La ejecucion de la query devolvera una fila si la clave es autoincremental
		String sql = this.getDbmsType().getDataTypeIdentitySql(this.getFullQualifiedTableName(), this.getColumn(pkIndex).getColName());
		ResultSet rs = query(sql);
		if (rs.next()) 
			this.getColumn(pkIndex).setAutoIncrement(true);
		rs.close();
	}

	/**
	 * Para una columna, modifica atributos de tamanyo y nombre para casos
	 * especiales
	 */
	private void correctTypeAttributes(SchemaColumn col) {
		col.setDataType(col.getDataType().trim());
		String dataType = col.getDataType().toUpperCase(); // siempre a mayusculas para comparar
		if (dataType.startsWith(DATETIME) || dataType.startsWith(DATE) || dataType.startsWith(SMALLDATETIME)) {
			// fecha en sqlserver, quito valores de tamanyo
			col.setColSize(0);
			col.setDecimalDigits(0);
		} else if (dataType.startsWith(TIMESTAMP)) { // el dato basico es timestamp, la
			setTimeZoneSubtypeIfNeeded(col, dataType);
			col.setDataType(TIMESTAMP); // elimina cualquier osa que vaya despues
		} else if (dataType.startsWith(TIME)) { // no qita precision pues sqlserver 2008 permite especificarla
			setTimeZoneSubtypeIfNeeded(col, dataType);
			col.setDataType(TIME);
		} else if (dataType.startsWith(INTERVAL)) { // lo que va tras INTERVAL es el subtipo
			col.setDataSubType(JavaCs.substring(col.getDataType(), INTERVAL.length()).trim());
			col.setDataType(INTERVAL);
		} else if (dataType.startsWith(DOUBLE)) {
			if (dataType.contains(PRECISION))
				col.setDataSubType(PRECISION);
			col.setDataType(DOUBLE);
		} else
			// En aquellos tipos de datos en que no es necesario conocer el tamanyo, pone los valores a cero
			resetColSizesForOtherTypes(col);
	}
	private void setTimeZoneSubtypeIfNeeded(SchemaColumn col, String dataType) {
		if (dataType.contains(WITH_TIME_ZONE)|| dataType.contains(WITH_LOCAL_TIME_ZONE))
			col.setDataSubType(WITH_TIME_ZONE);
	}
	private void resetColSizesForOtherTypes(SchemaColumn col) {
		for (int i = 0; i < typesWithoutSize.length; i++)
			if (JavaCs.equalsIgnoreCase(typesWithoutSize[i], col.getDataType())) {
				col.setColSize(0);
				col.setDecimalDigits(0);
			}
	}
	
	/**
	 * Obtiene el SQL almacenado en una tabla que representa una vista
	 */
	public String getQuery(SchemaTable thisTable) {
		// utiliza la sql definida en el DbmsType que cada dbms deberia implementar
		// si no usa por defecto utilizara una generica de information_schema, que esta
		// limitada a 4000 caracteres
		String sql = thisTable.getSchemaReader().getDbmsType().getViewDefinitionSQL(thisTable.getGlobalId().getCat(),
				thisTable.getGlobalId().getSch(), thisTable.getGlobalId().getTab());
		String qry = "";
		ResultSet rs = query(sql);
		try {
			if (!rs.next())
				throw new SchemaException(
						"SchemaReader.getQuery: Source query not found for view " + thisTable.getGlobalId().getTab());
			qry = rs.getString(1); // el primer campo es el nombre de la query
		} catch (SQLException t) {
			throw new SchemaException("SchemaReader.getQuery: Source query not found for view "
					+ thisTable.getGlobalId().getTab() + "\nuUsing query from metadata: " + sql, t);
		} finally {
			closeResultSet(rs);
		}
		// Esta query deberia ser de la forma create view ... as select ...
		// Pero algunos dbms como oracle solo guardan el select, por lo que si la query
		// obtenida empieza por select se debe anyadir el create view junto con los parametros 
		// (sacados de la tabla que representa la query en los metadatos)
		qry = qry.trim();
		if (qry.toLowerCase().startsWith("select")) {
			StringBuilder sCreate = new StringBuilder();
			sCreate.append("CREATE VIEW " + thisTable.getName() + " (");
			for (int i = 0; i < thisTable.getColumnNames().length; i++)
				sCreate.append((i == 0 ? "" : ",") + thisTable.getColumnNames()[i]);
			sCreate.append(") AS ");
			qry = sCreate.toString() + qry;
		}
		return qry;
	}

	//Queries a la base de datos representada por esta conexion
	
	public ResultSet query(String sql, int maxRows) {
		Statement stmt = null;
		try {
			stmt = this.conn.createStatement(); // NOSONAR no se puede cerrar pues si no se pierde el resultset
			// Si se ha indicado un mumero maximo de filas limita el rowset al numero
			// indicado
			if (maxRows > 0)
				stmt.setMaxRows(maxRows);
			return stmt.executeQuery(sql);
		} catch (SQLException ex) {
			closeStmt(stmt);
			throw new SchemaException("SchemaReaderJdbc.query", ex);
		}
	}
	@Override
	public ResultSet query(String sql) {
		return query(sql, 0);
	}
	@Override
	public void execute(String sql) {
		Statement stmt = null;
		try {
			stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException ex) {
			throw new SchemaException("SchemaReaderJdbc.execute", ex);
		} finally {
			closeStmt(stmt);
		}
	}
	@Override
	public void execute(List<String> sqls) {
		for (int i = 0; i < sqls.size(); i++)
			execute(sqls.get(i));
	}
	@Override
	public void closeResultSet(ResultSet rs) {
		try {
			if (rs != null) {
				Statement stmt = rs.getStatement();
				rs.close();
				closeStmt(stmt);
			}
		} catch (Exception e) {
			// no action
		}
	}
	private void closeStmt(Statement stmt) {
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			// no action
		}
	}

	private String ifnn(String check, String value) {
		return JavaCs.isEmpty(check) ? "" : value;
	}

	/**
	 * Devuelve un string de remplazo en el caso de que el string indicado sea nulo
	 */
	protected String coalesce(String str, String replacement) {
		return str == null ? replacement : str;
	}
	/**
	 * Realiza la operacion contraria al coalesce (case insensitive), si el string
	 * es igual al de remplazo devuelve nulo, si no devuelve el string
	 */
	protected String uncoalesce(String str, String replacement) {
		if (str == null)
			return null; // por si acaso el string ya es nulo
		if (JavaCs.equalsIgnoreCase(str, replacement))
			return null;
		return str;
	}
	/**
	 * Uncoalesce que devuelve espacio en blanco
	 */
	protected String uncoalesce(String str) {
		return uncoalesce(str, "");
	}

}
