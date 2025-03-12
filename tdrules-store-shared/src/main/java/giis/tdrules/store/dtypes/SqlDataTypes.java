package giis.tdrules.store.dtypes;

import java.util.List;

public class SqlDataTypes extends DataTypes {

	public SqlDataTypes() {
		super();
	}

	/**
	 * Configuracion del mapeo id-tipo de datos
	 */
	@Override
	protected void configureAllIds(List<String> allTypes) {
		configureId(allTypes, DT_CHARACTER, new String[] { "char", "character", "varchar", "varchar2", "nchar", "nvarchar", "nvarchar2", "text", "ntext" });
		configureId(allTypes, DT_INTEGER, new String[] { "int", "integer", "smallint", "bigint", "tinyint", "long", "serial", "smallserial", "bigserial" });
		configureId(allTypes, DT_EXACT_NUMERIC, new String[] { "numeric", "decimal", "number", "currency", "money", "smallmoney" });
		configureId(allTypes, DT_APPROXIMATE_NUMERIC, new String[] { "float", "real", "double", "binary_float", "binary_double" });
		configureId(allTypes, DT_LOGICAL, new String[] { "bit", "boolean" });
		configureId(allTypes, DT_DATE, new String[] { "date" });
		configureId(allTypes, DT_TIME, new String[] { "time" });
		configureId(allTypes, DT_DATETIME, new String[] { "timestamp", "datetime", "smalldatetime" });
		configureId(allTypes, DT_INTERVAL, new String[] { "interval" });
		configureId(allTypes, DT_BLOB, new String[] { "blob", "longblob", "binary", "varbinary", "image" });
	}

	/**
	 * Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
	 */
	@Override
	public String getDefault() {
		return "int";
	}

}
