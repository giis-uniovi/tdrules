/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Java.Util;
using Sharpen;

namespace Giis.Tdrules.Store.Dtypes
{
	/// <summary>
	/// Manages the variability related to the data types used by Data Stores
	/// by means of an internal mapping to generic data types.
	/// </summary>
	/// <remarks>
	/// Manages the variability related to the data types used by Data Stores
	/// by means of an internal mapping to generic data types.
	/// Behaves as a factory (similar to StoreTypes)
	/// </remarks>
	public abstract class DataTypes
	{
		public const string OaDbmsVendorName = "openapi";

		public const int DtUnknown = -1;

		public const int DtCharacter = 0;

		public const int DtInteger = 1;

		public const int DtExactNumeric = 2;

		public const int DtApproximateNumeric = 3;

		public const int DtLogical = 4;

		public const int DtDate = 5;

		public const int DtTime = 6;

		public const int DtDatetime = 7;

		public const int DtInterval = 8;

		public const int DtBlob = 9;

		private IDictionary<int, ICollection<string>> typesById;

		private IDictionary<string, int> idsByType = new SortedDictionary<string, int>();

		private IList<string> allTypesList = new List<string>();

		private string[] allTypesArray;

		// atributo dbms indica en el esquema
		/*
		* Los diferentes ids de los tipos de datos primitivosque se mapean con los
		* tipos en la instanciacion
		*/
		// sin decimales
		// decimales fijos
		// coma flotante
		/// <summary>
		/// Factoria que devuelve el objeto correspondiente que implementa las
		/// particularidades del dbms Por defecto devuelve los tipos genericos de las
		/// relacionales
		/// </summary>
		public static Giis.Tdrules.Store.Dtypes.DataTypes Get(string dbmsName)
		{
			dbmsName = dbmsName.ToLower();
			if (OaDbmsVendorName.Equals(dbmsName))
			{
				return new OaDataTypes();
			}
			else
			{
				return new SqlDataTypes();
			}
		}

		protected internal DataTypes()
		{
			typesById = new SortedDictionary<int, ICollection<string>>();
			ConfigureAllIds();
			allTypesArray = Sharpen.Collections.ToArray(allTypesList, new string[allTypesList.Count]);
		}

		/// <summary>Configuracion del mapeo id-tipo de datos</summary>
		protected internal abstract void ConfigureAllIds();

		/// <summary>Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar</summary>
		public abstract string GetDefault();

		/// <summary>Configura los tipos de datos correspondientes a un id</summary>
		protected internal virtual void ConfigureId(int id, string[] typesOfId)
		{
			typesById[id] = new HashSet<string>(Arrays.AsList(typesOfId));
			Sharpen.Collections.AddAll(allTypesList, Arrays.AsList(typesOfId));
			foreach (string item in typesOfId)
			{
				idsByType[item] = id;
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
			{
				return idsByType[name];
			}
			else
			{
				return DtUnknown;
			}
		}

		/// <summary>Devuelve los tipos de datos para un id dado (no comprueba si el id es valido)</summary>
		public virtual string[] GetTypes(int id)
		{
			ICollection<string> types = typesById[id];
			// puede ser null si no hay nada en el id
			return types == null ? new string[] {  } : Sharpen.Collections.ToArray(types, new string[types.Count]);
		}

		/// <summary>Devuelve un array unidimensional con todos los tipos de datos</summary>
		public virtual string[] GetAll()
		{
			return allTypesArray;
		}

		/// <summary>Determina si un tipo de datos se corresponde con alguno de los id indicados</summary>
		public virtual bool IsOneOf(string dataType, int[] ids)
		{
			foreach (int id in ids)
			{
				if (GetId(dataType.ToLower()) == id)
				{
					return true;
				}
			}
			return false;
		}
	}
}
