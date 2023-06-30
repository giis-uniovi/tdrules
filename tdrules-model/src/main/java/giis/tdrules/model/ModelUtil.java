package giis.tdrules.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utilidades para mayor seguridad en el manejo de los modelos:
 * Uso de strings (por defecto nunca son nulos):
 *  - Java: en el esquema para swagger todos los strings se ponen con valor por defecto "" por lo que no hay problema
 *  - Netcore: la generacion omite estos valores, pero se hace un postprocesamiento para evitar el problema
 * Uso de arrays (listas)
 *  - Java: swagger omite valores por defecto, por lo que 
 *    para iterar siempre se utilizara el metodo safe() para evitar comprobaciones de nulls
 *  - Netcore: igual, aunque la implementación de safe es distinta
 * Uso de maps (additionalParams)
 *  - Java: para actualiar elementos se usa el metodo del objeto putXxxItem, pero para leer
 *    se usara el metodo safe() que admite valores nulos en el map
 *  - Net: Mediante postprocesamiento se crea el metodo putXxxitem, y al leer
 *    se usara el metodo safe(), con una implementacion distinta que en java
 */
public class ModelUtil {
	private ModelUtil() {
		throw new IllegalStateException("Utility class");
	}
	/**
	 * Devuelve una lista vacia si es nula para poder iterar con seguridad
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> safe(List<T> nullableList) {
        return nullableList == null ? (List<T>)Collections.emptyList() : nullableList;
	}
	/**
	 * Devuelve un map vacio si es nul para poder iterar con seguridad
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> safe(Map<String, String> nullableMap) {
        return (Map<String, String>) (nullableMap == null ? Collections.emptyMap() : nullableMap);
	}
	
	/**
	 * Devuelve el valor correspondiente a la clave si existe este valor y el diccionario, si no devuelve vacio,
	 * utilizado para leer atributos extendidos que se encuentran en el modelo
	 */
	public static String safe(Map<String, String> map, String key) {
		// do not use previous safe method to have net compatibility
		if (map==null)
			return "";
		else {
			String value=null;
			if (map.containsKey(key))
				value=map.get(key);
			return value==null ? "" : map.get(key);
		}
	}

}
