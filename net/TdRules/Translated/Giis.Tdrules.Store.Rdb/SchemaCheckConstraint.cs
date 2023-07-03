/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Sharpen;

namespace Giis.Tdrules.Store.Rdb
{
	public class SchemaCheckConstraint
	{
		private string name;

		private string column;

		private string constraint;

		public virtual string GetName()
		{
			return name;
		}

		public virtual void SetName(string name)
		{
			this.name = name;
		}

		public virtual string GetColumn()
		{
			return column;
		}

		public virtual void SetColumn(string column)
		{
			this.column = column;
		}

		public virtual string GetConstraint()
		{
			return constraint;
		}

		public virtual void SetConstraint(string constraint)
		{
			this.constraint = constraint;
		}
	}
}
