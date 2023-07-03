/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Sharpen;

namespace Giis.Tdrules.Store.Stypes
{
	/// <summary>A few features of mysql, not thoroughly tested.</summary>
	public class StoreTypeSqlite : StoreType
	{
		protected internal StoreTypeSqlite(string dbms)
			: base(dbms)
		{
		}

		public override bool IsSqlite()
		{
			return true;
		}

		public override bool SupportsBracketQuotation()
		{
			return true;
		}

		/// <summary>
		/// Los campos autoincrementales en sqlite se identifican anyadiendo un sufijo
		/// autoincrement al tipo base de la clave, este es el que devuelve este metodo
		/// </summary>
		public override string GetDataTypeIdentitySuffix()
		{
			return "autoincrement";
		}

		/// <summary>
		/// Determina la clausula que se puede anyadir al final de una query limitar el
		/// maximo numero de filas devueltas (vacio si no existe en este SGBD)
		/// </summary>
		public override string GetMaxRowsLimitClause(long maxRows)
		{
			return "LIMIT " + maxRows;
		}

		/// <summary>
		/// Restricciones de la sintaxis de Sqlite: no soporta right joins, y requiere
		/// tener groupby si hay having
		/// </summary>
		public override bool GetSupportsHavingWithoutGroupBy()
		{
			return false;
		}

		public override bool GetSupportsRightJoin()
		{
			return false;
		}
	}
}
