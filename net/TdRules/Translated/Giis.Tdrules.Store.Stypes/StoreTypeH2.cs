/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Sharpen;

namespace Giis.Tdrules.Store.Stypes
{
	public class StoreTypeH2 : StoreType
	{
		private IDictionary<string, string> aliasMap = null;

		protected internal StoreTypeH2(string dbms)
			: base(dbms)
		{
		}

		public override bool IsH2()
		{
			return true;
		}

		/// <summary>
		/// Los campos autoincrementales se establecen mediante un tipo especifico
		/// (identity) en H2
		/// </summary>
		public override string GetDataTypeIdentity(string baseType)
		{
			return "identity";
		}

		/// <summary>
		/// Cuando se usan los metadatos, la V2 ha cambiado el tipo de datos interno que
		/// se obitene, se mapea aqui.
		/// </summary>
		/// <remarks>
		/// Cuando se usan los metadatos, la V2 ha cambiado el tipo de datos interno que
		/// se obitene, se mapea aqui. Tabla de equivalencias en
		/// http://www.h2database.com/html/datatypes.html
		/// </remarks>
		public override string MapAliasToDataType(string alias)
		{
			if (aliasMap == null)
			{
				// confecciona el mapping si no esta definido
				aliasMap = new Dictionary<string, string>();
				aliasMap["CHARACTER VARYING"] = "VARCHAR";
			}
			// devuelve el mapeo si existe, si no el mismo valor recibido
			if (aliasMap.ContainsKey(alias))
			{
				return aliasMap[alias];
			}
			return alias;
		}

		/// <summary>
		/// Determina la clausula que se puede anyadir al final de una query limitar el
		/// maximo numero de filas devueltas (vacio si no existe en este SGBD)
		/// </summary>
		public override string GetMaxRowsLimitClause(long maxRows)
		{
			return "LIMIT " + maxRows;
		}
	}
}
