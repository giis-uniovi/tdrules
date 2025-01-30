package giis.tdrules.store.loader.sql;

import static giis.tdrules.model.shared.ModelUtil.safe;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.store.dtypes.DataTypes;
import giis.tdrules.store.loader.shared.LoaderException;
import giis.tdrules.model.transform.SchemaSorter;
import giis.tdrules.store.stypes.StoreType;

/**
 * Proporciona sentencias SQL necesarias para limpiar una base de datos, eliminando
 * toda la informacion teniendo en cuenta las restricciones de FKs
 * (no disponible para openapi)
 */
public class DataCleaner {
	private static final Logger log=LoggerFactory.getLogger(DataCleaner.class);
	private TdSchema schema;
	private StoreType dbmsType;

	public DataCleaner(TdSchema sch) {
		this.schema=sch;
		if (DataTypes.OA_DBMS_VENDOR_NAME.equals(sch.getStoretype()))
			throw new LoaderException("An implementation of DataCleaner does not exist for an OpenApi schema model");
		this.dbmsType=StoreType.get(sch.getStoretype());
	}

	/**
	 * Devuelve una lista de sentencias SQL ordenadas de detalle a maestro
	 * para eliminar toda la informacion de la base de datos
	 */
	public List<String> getSQLDelete() {
		List<String> sql=new ArrayList<>();
		List<String> tables=this.schema.getEntityNames(true, false, false);
		List<String> orderedTables=new SchemaSorter(this.schema).sort(tables); //NOSONAR
		log.trace("getSQLDelete: tables in delete order: {}", orderedTables);
		//borra cada tabla en orden inverso (detalle-maestro)
		for (int i=orderedTables.size()-1; i>=0; i--)
			sql.add("DELETE FROM "+orderedTables.get(i)+";");
		return sql;
	}
	
	private boolean hasAutoincrementColumns(TdEntity table) {
		for (TdAttribute column : safe(table.getAttributes()))
			if (column.isAutoincrement()) 
				return true;
		return false;
	}
	public String getEnableIdentityCommand(TdEntity table, String schemaName) {
		if (this.hasAutoincrementColumns(table)) 
			return this.dbmsType.getEnableIdentityCommand(schemaName+"."+table.getName());
		else return "";
	}
	public String getDisableIdentityCommand(TdEntity table, String schemaName) {
		if (this.hasAutoincrementColumns(table)) 
			return dbmsType.getDisableIdentityCommand(schemaName+"."+table.getName());
		else return "";
	}
	public List<String> getDisableFkCommands(TdEntity table) {
		List<String> commands=new ArrayList<>();
		//Si el sgbd permite deshabilitar fks de forma individual devolvera un comando por cada fk
		if (dbmsType.canDisableForeignKey()) {
			for (String fk : table.getUniqueRidNames())
				commands.add(dbmsType.getDisableForeignKeyCommand(table.getName(), fk));
		} //si no (p.e. caso de postgres) se hace global a nivel de tabla
		else if (!"".equals(dbmsType.getDisableConstraintsCommand(table.getName()))) 
			commands.add(dbmsType.getDisableConstraintsCommand(table.getName()));
		return commands;
	}
	public List<String> getEnableFkCommands(TdEntity table) {
		List<String> commands=new ArrayList<>();
		if (dbmsType.canDisableForeignKey()) {
			for (String fk : table.getUniqueRidNames())
				commands.add(dbmsType.getEnableForeignKeyCommand(table.getName(), fk));
		} else if (!"".equals(dbmsType.getEnableConstraintsCommand(table.getName())))
			commands.add(dbmsType.getEnableConstraintsCommand(table.getName()));
		return commands;
	}
	
	//resto implementado previamente en sqlcore.generator no implementados de momento
	
}
