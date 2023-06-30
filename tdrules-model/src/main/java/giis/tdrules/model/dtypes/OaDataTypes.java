package giis.tdrules.model.dtypes;

public class OaDataTypes extends DataTypes {

	protected OaDataTypes() {
		super();
	}
	
    /**
     * Configuracion del mapeo id-tipo de datos
     */
    @Override
    protected void configureAllIds() {
    	configureId(DT_CHARACTER, new String[] {"string"});
    	configureId(DT_INTEGER, new String[] {"integer","int32","int64"});
    	configureId(DT_EXACT_NUMERIC, new String[] {});
    	configureId(DT_APPROXIMATE_NUMERIC, new String[] {"number","float","double"});
    	configureId(DT_LOGICAL, new String[] {"boolean"});
    	configureId(DT_DATE, new String[] {"date"});
    	configureId(DT_TIME, new String[] {});
    	configureId(DT_DATETIME, new String[] {"date-time"});
    	configureId(DT_INTERVAL, new String[] {});
    	configureId(DT_BLOB, new String[] {"byte","binary"});
    }
    /**
     * Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar
     */
    @Override
    public String getDefault() {
    	return "integer";
    }

}
