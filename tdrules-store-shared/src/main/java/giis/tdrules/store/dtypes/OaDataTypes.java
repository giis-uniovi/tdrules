package giis.tdrules.store.dtypes;

import java.util.List;

public class OaDataTypes extends DataTypes {

	public OaDataTypes() {
		super();
	}

	/**
	 * Configuracion del mapeo id-tipo de datos
	 */
	@Override
	protected void configureAllIds(List<String> allTypes) {
		configureId(allTypes, DT_CHARACTER, new String[] { "string" });
		configureId(allTypes, DT_INTEGER, new String[] { "integer", "int32", "int64" });
		configureId(allTypes, DT_EXACT_NUMERIC, new String[] {});
		configureId(allTypes, DT_APPROXIMATE_NUMERIC, new String[] { "number", "float", "double" });
		configureId(allTypes, DT_LOGICAL, new String[] { "boolean" });
		configureId(allTypes, DT_DATE, new String[] { "date" });
		configureId(allTypes, DT_TIME, new String[] {});
		configureId(allTypes, DT_DATETIME, new String[] { "date-time" });
		configureId(allTypes, DT_INTERVAL, new String[] {});
		configureId(allTypes, DT_BLOB, new String[] { "byte", "binary" });
	}

	/**
	 * Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
	 */
	@Override
	public String getDefault() {
		return "integer";
	}

}
