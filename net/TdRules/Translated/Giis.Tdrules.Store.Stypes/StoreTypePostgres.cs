/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Portable.Util;
using Sharpen;

namespace Giis.Tdrules.Store.Stypes
{
	public class StoreTypePostgres : StoreType
	{
		private IDictionary<string, string> aliasMap = null;

		protected internal StoreTypePostgres(string dbms)
			: base(dbms)
		{
		}

		public override bool IsPostgres()
		{
			return true;
		}

		/// <summary>
		/// Comando para deshabilitar todas las constraints de una tabla, usado para
		/// deshabilitar fks porque no se puede hacer facilmente de forma individual
		/// </summary>
		public override string GetDisableConstraintsCommand(string tableName)
		{
			return "ALTER TABLE " + tableName + " DISABLE TRIGGER ALL";
		}

		/// <summary>
		/// Comando para habilitar todas las constraints de una tabla, usado para
		/// habilitar fks porque no se puede hacer facilmente de forma individual
		/// </summary>
		public override string GetEnableConstraintsCommand(string tableName)
		{
			return "ALTER TABLE " + tableName + " ENABLE TRIGGER ALL";
		}

		/// <summary>
		/// Los campos autoincrementales se establecen mediante un tipo especifico
		/// (serial) en postgres
		/// </summary>
		public override string GetDataTypeIdentity(string baseType)
		{
			return "serial";
		}

		/// <summary>
		/// Query a utilizar para obtener el SQL de una vista sin limitar el numero de
		/// caracteres
		/// </summary>
		public override string GetViewDefinitionSQL(string catalog, string schema, string viewName)
		{
			// En postgres el schema que se ve es siempre public, no puede anyadirse al nombre de vista
			return "select definition from pg_views where viewname = '" + viewName + "'";
		}

		/// <summary>
		/// Cuando se usan los metadatos, postgres devuelve los nombres de tipos de datos
		/// internos, que se mapean aqui a los documentados en postgress o tipos de datos
		/// comunes en sql Tabla de equivalencias en
		/// https://www.postgresql.org/docs/9.6/datatype.html
		/// </summary>
		public override string MapAliasToDataType(string alias)
		{
			if (aliasMap == null)
			{
				// confecciona el mapping si no esta definido
				aliasMap = new Dictionary<string, string>();
				aliasMap["int2"] = "smallint";
				aliasMap["int4"] = "integer";
				aliasMap["int"] = "integer";
				aliasMap["int8"] = "bigint";
				aliasMap["serial2"] = "smallserial";
				aliasMap["serial4"] = "serial";
				aliasMap["serial8"] = "bigserial";
				aliasMap["decimal"] = "numeric";
				aliasMap["float4"] = "real";
				aliasMap["float8"] = "double precision";
				aliasMap["bpchar"] = "char";
				// no esta en la tabla, pero aparece
				aliasMap["bool"] = "boolean";
				aliasMap["timetz"] = "time with time zone";
				aliasMap["timestamptz"] = "timestamp with time zone";
			}
			// Postgres parece que anyade un underscore cuando el tipo proviene de un array, lo elimina
			if (alias.StartsWith("_"))
			{
				alias = JavaCs.Substring(alias, 1, alias.Length);
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
