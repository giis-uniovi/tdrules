using Java.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Model.Shared
{
    /// <summary>
    /// Utilidades para mayor seguridad en el manejo de los modelos:
    /// Uso de strings (por defecto nunca son nulos):
    ///  - Java: en el esquema para swagger todos los strings se ponen con valor por defecto "" por lo que no hay problema
    ///  - Netcore: la generacion omite estos valores, pero se hace un postprocesamiento para evitar el problema
    /// Uso de arrays (listas)
    ///  - Java: swagger omite valores por defecto, por lo que
    ///    para iterar siempre se utilizara el metodo safe() para evitar comprobaciones de nulls
    ///  - Netcore: igual, aunque la implementación de safe es distinta
    /// Uso de maps (additionalParams)
    ///  - Java: para actualiar elementos se usa el metodo del objeto putXxxItem, pero para leer
    ///    se usara el metodo safe() que admite valores nulos en el map
    ///  - Net: Mediante postprocesamiento se crea el metodo putXxxitem, y al leer
    ///    se usara el metodo safe(), con una implementacion distinta que en java
    /// </summary>
    public class ModelUtil
    {
        private ModelUtil()
        {
            throw new InvalidOperationException("Utility class");
        }

        /// <summary>
        /// Devuelve una lista vacia si es nula para poder iterar con seguridad
        /// </summary>
        public static IList<T> Safe<T>(IList<T> nullableList)
        {
            return nullableList == null ? (IList<T>)new List<T>() : nullableList;
        }

        /// <summary>
        /// Devuelve un map vacio si es nul para poder iterar con seguridad
        /// </summary>
        public static Dictionary<string, string> Safe(Dictionary<string, string> nullableMap)
        {
            return (Dictionary<string, string>)(nullableMap == null ? new Dictionary<string, string>() : nullableMap);
        }

        /// <summary>
        /// Devuelve el valor correspondiente a la clave si existe este valor y el diccionario, si no devuelve vacio,
        /// utilizado para leer atributos extendidos que se encuentran en el modelo
        /// </summary>
        public static string Safe(Dictionary<string, string> map, string key)
        {

            // do not use previous safe method to have net compatibility
            if (map == null)
                return "";
            else
            {
                string value = null;
                if (map.ContainsKey(key))
                    value = map[key];
                return value == null ? "" : map[key];
            }
        }
    }
}