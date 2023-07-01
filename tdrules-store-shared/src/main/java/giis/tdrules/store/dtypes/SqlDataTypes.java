package giis.tdrules.store.dtypes;

public class SqlDataTypes extends DataTypes {

	protected SqlDataTypes() {
		super();
	}

	/**
	 * Configuracion del mapeo id-tipo de datos
	 */
	@Override
	protected void configureAllIds() {
		configureId(DT_CHARACTER, new String[] { "char", "character", "varchar", "varchar2", "nchar", "nvarchar", "nvarchar2", "text", "ntext" });
		configureId(DT_INTEGER, new String[] { "int", "integer", "smallint", "bigint", "tinyint", "long", "serial", "smallserial", "bigserial" });
		configureId(DT_EXACT_NUMERIC, new String[] { "numeric", "decimal", "number", "currency", "money", "smallmoney" });
		configureId(DT_APPROXIMATE_NUMERIC, new String[] { "float", "real", "double", "binary_float", "binary_double" });
		configureId(DT_LOGICAL, new String[] { "bit", "boolean" });
		configureId(DT_DATE, new String[] { "date" });
		configureId(DT_TIME, new String[] { "time" });
		configureId(DT_DATETIME, new String[] { "timestamp", "datetime", "smalldatetime" });
		configureId(DT_INTERVAL, new String[] { "interval" });
		configureId(DT_BLOB, new String[] { "blob", "longblob", "binary", "varbinary", "image" });
	}

	/**
	 * Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
	 */
	@Override
	public String getDefault() {
		return "int";
	}

}
