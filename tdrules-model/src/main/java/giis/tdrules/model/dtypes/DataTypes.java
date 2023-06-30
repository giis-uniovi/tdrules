package giis.tdrules.model.dtypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Mapeo de los tipos de datos admitidos por la base de datos:
 * Cada conjunto de tipos de datos (tal como aparecen en el ddl) se corresponde
 * con un id que representa tipos que tienen un mismo comportamiento en las 
 * manipulaciones que se realizaran para generacion de reglas y mutantes.
 * La sublcase definira los valores concretos, esta clase define los metodos comunes
 */
public abstract class DataTypes {

	public static final String OA_DBMS_VENDOR_NAME = "openapi"; //atributo dbms indica en el esquema
	
	/* Texto utilizado para indicar composites (tipos de datos no primitivos) */
	public static final String DT_TYPE = "type";
	public static final String DT_ARRAY = "array";

	/* Los diferentes ids de los tipos de datos primitivosque se mapean con los tipos en la instanciacion */
    public static final int DT_UNKNOWN=-1;
    public static final int DT_CHARACTER=0;
    public static final int DT_INTEGER=1; //sin decimales
    public static final int DT_EXACT_NUMERIC=2; //decimales fijos
    public static final int DT_APPROXIMATE_NUMERIC=3; //coma flotante
    public static final int DT_LOGICAL=4;
    public static final int DT_DATE=5;
    public static final int DT_TIME=6;
    public static final int DT_DATETIME=7;
    public static final int DT_INTERVAL=8;
    public static final int DT_BLOB=9;

    
    private Map<Integer, Set<String>> typesById;
    private Map<String, Integer> idsByType=new TreeMap<>();
    private List<String> allTypesList=new ArrayList<>();
    private String[] allTypesArray;
    
	/**
	 * Factoria que devuelve el objeto correspondiente que implementa las particularidades del dbms
	 * Por defecto devuelve los tipos genericos de las relacionales
	 */
	public static DataTypes get(String dbmsName) {
		dbmsName=dbmsName.toLowerCase();
		if (OA_DBMS_VENDOR_NAME.equals(dbmsName))
			return new OaDataTypes();
		else
			return new SqlDataTypes();
	}

    protected DataTypes() {
    	typesById = new TreeMap<>();
    	configureAllIds();
    	allTypesArray=allTypesList.toArray(new String[allTypesList.size()]);
    }
    
    /**
     * Configuracion del mapeo id-tipo de datos
     */
    protected abstract void configureAllIds();
    
    /**
     * Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
     */
    public abstract String getDefault();

    /**
     * Configura los tipos de datos correspondientes a un id
     */
    protected void configureId(int id, String[] typesOfId) {
    	typesById.put(id, new TreeSet<>(Arrays.asList(typesOfId)));
    	allTypesList.addAll(Arrays.asList(typesOfId));
    	for (String item : typesOfId)
    		idsByType.put(item, id);
    }

    /**
     * Devuelve el id de tipo de datos generico correspondiente a este keyword, 
     * en caso de no encontrarse devuelve DT_UNKNOWN
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
    	Set<String> types = typesById.get(id); //puede ser null si no hay nada en el id
    	return types==null ? new String[] {} : types.toArray(new String[types.size()]);
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
    		if (getId(dataType.toLowerCase())==id)
    			return true;
    	return false;
    }
    
}
