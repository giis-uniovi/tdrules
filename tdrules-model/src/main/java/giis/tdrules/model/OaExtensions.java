package giis.tdrules.model;

/**
 * Extensiones openapi (specification extensions/vendor extensions) aplicables al modelo
 * y otros convenios para el nombrado de elementos del modelo 
 * transformados a partir del esquema openapi
 */
public class OaExtensions {
	//Extensiones OpenApi
	public static final String X_PK = "x-pk"; //es una clave primaria (identificador unico del objeto)
	public static final String X_FK = "x-fk"; //es una clave ajena (identificador unico de un objeto referenciado)
	
	//Claves fk y pk que se establecen para las entidades que representan arrays en el modelo transformado
	public static final String ARRAY_PK = "pk_xa";
	public static final String ARRAY_FK = "fk_xa";
	
	private OaExtensions() {
		throw new IllegalStateException("Utility class");
	}

	//Funciones para denominar las entidades extraidas del esquema openapi
	
	public static String getFkName(String tabName, String colName) {
		return "fk_" + tabName + "_" + colName;
	}

	public static String getExtractedTypeName(String tabName, String colName) {
		return tabName + "_" + colName + "_xt";
	}

	public static String getExtractedArrayName(String tabName, String colName) {
		return tabName + "_" + colName + "_xa";
	}

}
