package giis.tdrules.store.rdb;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.portable.util.JavaCs;

/**
 * Ordenacion de las tablas de un esquema: Provee de un metodo sort() para
 * realizar la ordenacion de un conjunto de tablasen el orden maestro a detalle;
 * si hay ciclos utilizar noFollowConstraint() para excluir las constraints que
 * causan los ciclos
 */
public class SchemaSorter {
	private static final Logger log = LoggerFactory.getLogger(SchemaSorter.class);
	private SchemaReader sr;
	private List<String> excludeConstraints = new ArrayList<String>();

	/**
	 * Instancia esta clase con un SchemaReader, se recomienda que utilice cache
	 * porque el algoritmo de ordenacion realiza multiples busquedas sobre las
	 * tablas, lo cual puede decrecer sustancialmente el rendimiento en algunos SGBD
	 */
	public SchemaSorter(SchemaReader sr) {
		this.sr = sr;
		if (!sr.getUseCache())
			log.warn("Cache is disabled for the current schema reader. Sorting may be too slow");
	}

	/**
	 * Especifica el nombre de una constraint de integridad referencial que no debe
	 * tenerse en cuenta al navegar por las fks de las tablas durante la ordenacion
	 * (para evitar ciclos); se pueden concatener varias llamadas a este metodo para
	 * anyadir mas de una constraint
	 */
	public SchemaSorter noFollowConstraint(String constraintName) {
		excludeConstraints.add(constraintName.toLowerCase());
		return this; // fluent to concatenate if more than one constraint
	}
	
	/**
	 * Dada una lista de tablas, obtiene otra lista con las mismas tablas ordenadas
	 * segun sus dependencias maestro-detalle (primero las maestros); Si hay
	 * referencias circulares causara excepcion, en este caso usar
	 * noFollowConstraint para romper los ciclos
	 */
	public List<String> sort(List<String> tables) { // NOSONAR
		log.debug("*** Begin sort tables: " + tables.toString());
		// si hay ciclos causara execpion cuando se llege a este numero de llamadas
		// recursivas
		int maxLevel = tables.size() * 2;
		// lista donde se almacenaran las tablas
		List<String> orderedTables = new ArrayList<String>();
		// para cada una de las tablas, acumula esta y sus dependientes en orden inverso
		for (int i = 0; i < tables.size(); i++) {
			log.trace("Sorting table: " + tables.get(i));
			String tName = tables.get(i);
			getTableAndDependentInOrder(1, maxLevel, tName, orderedTables, tables);
		}
		log.debug("      End sort tables: " + orderedTables.toString());
		return orderedTables;
	}

	/**
	 * Acumula en orderedTables todas las tablas dependientes de tName
	 * (recursivamente de maestro a detalle)
	 */
	private void getTableAndDependentInOrder(int level, int maxLevel, String tName, List<String> orderedTables,
			List<String> originalTables) {
		log.trace(level + " target: " + tName + " current table list: " + orderedTables.toString());
		if (level > maxLevel) {
			log.error("Too many recursive levels, the most probable reason is that schema has circular references");
			throw new SchemaException("Too many recusive levels when trying to sort tables");
		}
		// creo un objeto de esquema para leer los datos de la tabla
		// causara excepcion si la tabla no existe
		sr.readTable(tName);
		String tableName = sr.getTableName(); // a partir de ahora usa nombre tal y como se lee del esquema

		// localizando las tablas dependientes a traves de FKs
		// Las guarda en una lista para que posteriores llamadas a metodos
		// que utilizan el mismo esquema no cambien el contexto de la tabla
		// actual del mismo
		List<String> dependentTables = new ArrayList<String>();
		for (int i = 0; i < sr.getColumnCount(); i++) {
			// si hay una columna que tiene clave ajena
			if (!sr.getColumn(i).getForeignKey().equals("")
					// solo si la tabla dependiente no es la que esta en tratamiento
					// (evita tener en cuenta relaciones recursivas entre una tabla hacia ella misma)
					&& !sr.getColumn(i).getForeignTable().equals(tName)
					// y esta incluida dentro del conjunto de todas las tablas a buscar
					&& JavaCs.containsIgnoreCase(originalTables, sr.getColumn(i).getForeignTable())
					// y no esta en la lista de exclusion de constraints
					&& !excludeConstraints.contains(sr.getColumn(i).getForeignKeyName().toLowerCase()))
				dependentTables.add(sr.getColumn(i).getForeignTable());
		}
		// Para cada una de las tablas dependientes intenta cargarlas recursivamente
		for (int i = 0; i < dependentTables.size(); i++)
			getTableAndDependentInOrder(level + 1, maxLevel, dependentTables.get(i), orderedTables, originalTables);
		// una vez resueltas todas las dependientes cargo esta (si no lo estaba ya)
		if (!JavaCs.containsIgnoreCase(orderedTables, tableName))
			orderedTables.add(tableName);
		log.trace(level + " target: " + tName + "   final table list: " + orderedTables.toString());
	}

}
