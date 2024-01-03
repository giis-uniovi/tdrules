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
		/// Los campos autoincrementales en sqlite se identifican buscando en una query
		/// https://stopbyte.com/t/how-to-check-if-a-column-is-autoincrement-primary-key-or-not-in-sqlite/174/2
		/// Tambien se pueden determinar consultando la tabla de secuencias, pero esta vacia si no se ha insertado en la tabla
		/// </summary>
		public override string GetDataTypeIdentitySql(string tableName, string columnName)
		{
			return "SELECT 'is-autoincrement' FROM sqlite_master WHERE tbl_name='" + tableName + "' AND sql LIKE '%AUTOINCREMENT%'";
		}

		/// <summary>Query a utilizar para obtener el SQL de una vista</summary>
		public override string GetViewDefinitionSQL(string catalog, string schema, string viewName)
		{
			return "select sql from sqlite_master where type = 'view' and name = '" + viewName + "' and tbl_name = '" + viewName + "'";
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
