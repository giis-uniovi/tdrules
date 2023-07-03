/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Portable.Util;
using NLog;
using Sharpen;

namespace Giis.Tdrules.Store.Rdb
{
	/// <summary>
	/// Ordenacion de las tablas de un esquema: Provee de un metodo sort() para
	/// realizar la ordenacion de un conjunto de tablasen el orden maestro a detalle;
	/// si hay ciclos utilizar noFollowConstraint() para excluir las constraints que
	/// causan los ciclos
	/// </summary>
	public class SchemaSorter
	{
		private static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(Giis.Tdrules.Store.Rdb.SchemaSorter));

		private SchemaReader sr;

		private IList<string> excludeConstraints = new List<string>();

		/// <summary>
		/// Instancia esta clase con un SchemaReader, se recomienda que utilice cache
		/// porque el algoritmo de ordenacion realiza multiples busquedas sobre las
		/// tablas, lo cual puede decrecer sustancialmente el rendimiento en algunos SGBD
		/// </summary>
		public SchemaSorter(SchemaReader sr)
		{
			this.sr = sr;
			if (!sr.GetUseCache())
			{
				Giis.Portable.Util.NLogUtil.Warn(log, "Cache is disabled for the current schema reader. Sorting may be too slow");
			}
		}

		/// <summary>
		/// Especifica el nombre de una constraint de integridad referencial que no debe
		/// tenerse en cuenta al navegar por las fks de las tablas durante la ordenacion
		/// (para evitar ciclos); se pueden concatener varias llamadas a este metodo para
		/// anyadir mas de una constraint
		/// </summary>
		public virtual Giis.Tdrules.Store.Rdb.SchemaSorter NoFollowConstraint(string constraintName)
		{
			excludeConstraints.Add(constraintName.ToLower());
			return this;
		}

		// fluent to concatenate if more than one constraint
		/// <summary>
		/// Dada una lista de tablas, obtiene otra lista con las mismas tablas ordenadas
		/// segun sus dependencias maestro-detalle (primero las maestros); Si hay
		/// referencias circulares causara excepcion, en este caso usar
		/// noFollowConstraint para romper los ciclos
		/// </summary>
		public virtual IList<string> Sort(IList<string> tables)
		{
			// NOSONAR
			log.Debug("*** Begin sort tables: " + tables.ToString());
			// si hay ciclos causara execpion cuando se llege a este numero de llamadas
			// recursivas
			int maxLevel = tables.Count * 2;
			// lista donde se almacenaran las tablas
			IList<string> orderedTables = new List<string>();
			// para cada una de las tablas, acumula esta y sus dependientes en orden inverso
			for (int i = 0; i < tables.Count; i++)
			{
				log.Trace("Sorting table: " + tables[i]);
				string tName = tables[i];
				GetTableAndDependentInOrder(1, maxLevel, tName, orderedTables, tables);
			}
			log.Debug("      End sort tables: " + orderedTables.ToString());
			return orderedTables;
		}

		/// <summary>
		/// Acumula en orderedTables todas las tablas dependientes de tName
		/// (recursivamente de maestro a detalle)
		/// </summary>
		private void GetTableAndDependentInOrder(int level, int maxLevel, string tName, IList<string> orderedTables, IList<string> originalTables)
		{
			log.Trace(level + " target: " + tName + " current table list: " + orderedTables.ToString());
			if (level > maxLevel)
			{
				Giis.Portable.Util.NLogUtil.Error(log, "Too many recursive levels, the most probable reason is that schema has circular references");
				throw new SchemaException("Too many recusive levels when trying to sort tables");
			}
			// creo un objeto de esquema para leer los datos de la tabla
			// causara excepcion si la tabla no existe
			sr.ReadTable(tName);
			string tableName = sr.GetTableName();
			// a partir de ahora usa nombre tal y como se lee del esquema
			// localizando las tablas dependientes a traves de FKs
			// Las guarda en una lista para que posteriores llamadas a metodos
			// que utilizan el mismo esquema no cambien el contexto de la tabla
			// actual del mismo
			IList<string> dependentTables = new List<string>();
			for (int i = 0; i < sr.GetColumnCount(); i++)
			{
				// si hay una columna que tiene clave ajena
				if (!sr.GetColumn(i).GetForeignKey().Equals(string.Empty) && !sr.GetColumn(i).GetForeignTable().Equals(tName) && JavaCs.ContainsIgnoreCase(originalTables, sr.GetColumn(i).GetForeignTable()) && !excludeConstraints.Contains(sr.GetColumn(i).GetForeignKeyName().ToLower()))
				{
					// solo si la tabla dependiente no es la que esta en tratamiento
					// (evita tener en cuenta relaciones recursivas entre una tabla hacia ella misma)
					// y esta incluida dentro del conjunto de todas las tablas a buscar
					// y no esta en la lista de exclusion de constraints
					dependentTables.Add(sr.GetColumn(i).GetForeignTable());
				}
			}
			// Para cada una de las tablas dependientes intenta cargarlas recursivamente
			for (int i_1 = 0; i_1 < dependentTables.Count; i_1++)
			{
				GetTableAndDependentInOrder(level + 1, maxLevel, dependentTables[i_1], orderedTables, originalTables);
			}
			// una vez resueltas todas las dependientes cargo esta (si no lo estaba ya)
			if (!JavaCs.ContainsIgnoreCase(orderedTables, tableName))
			{
				orderedTables.Add(tableName);
			}
			log.Trace(level + " target: " + tName + "   final table list: " + orderedTables.ToString());
		}
	}
}
