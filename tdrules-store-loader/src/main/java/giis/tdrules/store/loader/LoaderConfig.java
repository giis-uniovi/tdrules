package giis.tdrules.store.loader;

import giis.tdrules.store.loader.gen.IAttrGen;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.gen.IUidGen;
import giis.tdrules.store.loader.gen.SequentialUidGen;

/**
 * Run time parameters for data loading
 */
class LoaderConfig {

	boolean genNullable=true; // Since June 2024 these defaults to true
	boolean genDefault=true;
	int genNullProbability=0;

	IDataAdapter dataAdapter;
	IUidGen uidGen;
	IAttrGen attrGen;
	IUidGen arrayUidGen; // Only used to create array uids
	
	void reset() {
		dataAdapter.reset();
		uidGen.reset();
		attrGen.reset();
		arrayUidGen=new SequentialUidGen(); // not configurable, for internal use only
	}

}
