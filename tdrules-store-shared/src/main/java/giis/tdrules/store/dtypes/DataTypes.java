package giis.tdrules.store.dtypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import giis.portable.util.JavaCs;

/**
 * Manages the variability related to the data types used by Data Stores
 * by means of an internal mapping to generic data types.
 * 
 * Behaves as a factory (similar to StoreTypes)
 */
public abstract class DataTypes {

	public static final String OA_DBMS_VENDOR_NAME = "openapi"; // atributo dbms indica en el esquema

	/*
	 * Los diferentes ids de los tipos de datos primitivosque se mapean con los
	 * tipos en la instanciacion
	 */
	public static final int DT_UNKNOWN = -1;
	public static final int DT_CHARACTER = 0;
	public static final int DT_INTEGER = 1; // sin decimales
	public static final int DT_EXACT_NUMERIC = 2; // decimales fijos
	public static final int DT_APPROXIMATE_NUMERIC = 3; // coma flotante
	public static final int DT_LOGICAL = 4;
	public static final int DT_DATE = 5;
	public static final int DT_TIME = 6;
	public static final int DT_DATETIME = 7;
	public static final int DT_INTERVAL = 8;
	public static final int DT_BLOB = 9;

	private Map<Integer, String[]> typesById = new TreeMap<Integer, String[]>();
	private Map<String, Integer> idsByType = new TreeMap<String, Integer>();
	private String[] allTypesArray;

	/**
	 * Factoria que devuelve el objeto correspondiente que implementa las
	 * particularidades del dbms Por defecto devuelve los tipos genericos de las
	 * relacionales
	 */
	public static DataTypes get(String dbmsName) {
		dbmsName = dbmsName.toLowerCase();
		if (OA_DBMS_VENDOR_NAME.equals(dbmsName))
			return new OaDataTypes();
		else
			return new SqlDataTypes();
	}

	protected DataTypes() {
		List<String> allTypes = new ArrayList<String>();
		configureAllIds(allTypes);
		allTypesArray = JavaCs.toArray(allTypes);
	}

	/**
	 * Configuracion del mapeo id-tipo de datos
	 */
	protected abstract void configureAllIds(List<String> allTypesList);

	/**
	 * Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
	 */
	public abstract String getDefault();

	/**
	 * Configura los tipos de datos correspondientes a un id
	 */
	protected void configureId(List<String>allTypes, int id, String[] typesOfId) {
		typesById.put(id, typesOfId);
		for (String item : typesOfId) {
			idsByType.put(item, id);
			allTypes.add(item);
		}
	}

	/**
	 * Devuelve el id de tipo de datos generico correspondiente a este keyword, en
	 * caso de no encontrarse devuelve DT_UNKNOWN
	 */
	public int getId(String name) {
		name = name.toLowerCase();
		if (idsByType.containsKey(name))
			return idsByType.get(name);
		else
			return DT_UNKNOWN;
	}

	/**
	 * Devuelve los tipos de datos para un id dado (no comprueba si el id es valido)
	 */
	public String[] getTypes(int id) {
		String[] types = typesById.get(id); // puede ser null si no hay nada en el id
		return types == null ? new String[] {} : types;
	}

	/**
	 * Devuelve un array unidimensional con todos los tipos de datos
	 */
	public String[] getAll() {
		return allTypesArray;
	}

	/**
	 * Determina si un tipo de datos se corresponde con alguno de los id indicados
	 */
	public boolean isOneOf(String dataType, int[] ids) {
		for (int id : ids)
			if (getId(dataType.toLowerCase()) == id)
				return true;
		return false;
	}
    
}
