using Java.Util;
using Giis.Portable.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Dtypes
{
    /// <summary>
    /// Manages the variability related to the data types used by Data Stores
    /// by means of an internal mapping to generic data types.
    /// 
    /// Behaves as a factory (similar to StoreTypes)
    /// </summary>
    public abstract class DataTypes
    {
        public static readonly string OA_DBMS_VENDOR_NAME = "openapi"; // atributo dbms indica en el esquema
        /*
         * Los diferentes ids de los tipos de datos primitivosque se mapean con los
         * tipos en la instanciacion
         */
        public static readonly int DT_UNKNOWN = -1;
        public static readonly int DT_CHARACTER = 0;
        public static readonly int DT_INTEGER = 1; // sin decimales
        public static readonly int DT_EXACT_NUMERIC = 2; // decimales fijos
        public static readonly int DT_APPROXIMATE_NUMERIC = 3; // coma flotante
        public static readonly int DT_LOGICAL = 4;
        public static readonly int DT_DATE = 5;
        public static readonly int DT_TIME = 6;
        public static readonly int DT_DATETIME = 7;
        public static readonly int DT_INTERVAL = 8;
        public static readonly int DT_BLOB = 9;
        private Map<int, String[]> typesById = new TreeMap<int, String[]>();
        private Map<string, int> idsByType = new TreeMap<string, int>();
        private string[] allTypesArray;
        /// <summary>
        /// Factoria que devuelve el objeto correspondiente que implementa las
        /// particularidades del dbms Por defecto devuelve los tipos genericos de las
        /// relacionales
        /// </summary>
        public static DataTypes Get(string dbmsName)
        {
            dbmsName = dbmsName.ToLower();
            if (OA_DBMS_VENDOR_NAME.Equals(dbmsName))
                return new OaDataTypes();
            else
                return new SqlDataTypes();
        }

        protected DataTypes()
        {
            IList<string> allTypes = new List<string>();
            ConfigureAllIds(allTypes);
            allTypesArray = JavaCs.ToArray(allTypes);
        }

        /// <summary>
        /// Configuracion del mapeo id-tipo de datos
        /// </summary>
        protected abstract void ConfigureAllIds(IList<string> allTypesList);
        /// <summary>
        /// Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
        /// </summary>
        public abstract string GetDefault();
        /// <summary>
        /// Configura los tipos de datos correspondientes a un id
        /// </summary>
        protected virtual void ConfigureId(IList<string> allTypes, int id, string[] typesOfId)
        {
            typesById.Put(id, typesOfId);
            foreach (string item in typesOfId)
            {
                idsByType.Put(item, id);
                allTypes.Add(item);
            }
        }

        /// <summary>
        /// Devuelve el id de tipo de datos generico correspondiente a este keyword, en
        /// caso de no encontrarse devuelve DT_UNKNOWN
        /// </summary>
        public virtual int GetId(string name)
        {
            name = name.ToLower();
            if (idsByType.ContainsKey(name))
                return idsByType[name];
            else
                return DT_UNKNOWN;
        }

        /// <summary>
        /// Devuelve los tipos de datos para un id dado (no comprueba si el id es valido)
        /// </summary>
        public virtual String[] GetTypes(int id)
        {
            string[] types = typesById[id]; // puede ser null si no hay nada en el id
            return types == null ? new string[]
            {
            }

            : types;
        }

        /// <summary>
        /// Devuelve un array unidimensional con todos los tipos de datos
        /// </summary>
        public virtual String[] GetAll()
        {
            return allTypesArray;
        }

        /// <summary>
        /// Determina si un tipo de datos se corresponde con alguno de los id indicados
        /// </summary>
        public virtual bool IsOneOf(string dataType, int[] ids)
        {
            foreach (int id in ids)
                if (GetId(dataType.ToLower()) == id)
                    return true;
            return false;
        }
    }
}