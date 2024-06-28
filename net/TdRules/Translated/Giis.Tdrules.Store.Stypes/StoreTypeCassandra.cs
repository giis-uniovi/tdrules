/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Sharpen;

namespace Giis.Tdrules.Store.Stypes
{
	/// <summary>Although non relational, Cassandra can be managed if using a jdbc compatible driver or wrapper</summary>
	public class StoreTypeCassandra : StoreType
	{
		protected internal StoreTypeCassandra(string dbms)
			: base(dbms)
		{
		}

		public override bool IsCassandra()
		{
			return true;
		}
	}
}
