/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using Sharpen;

namespace Giis.Tdrules.Store.Rdb
{
	[System.Serializable]
	public class SchemaException : Exception
	{
		private const long serialVersionUID = -4155612383247919170L;

		public SchemaException(Exception e)
			: base("Schema Exception", e)
		{
		}

		public SchemaException(string message)
			: base(message)
		{
		}

		public SchemaException(string message, Exception cause)
			: base(message + (cause == null ? string.Empty : ". Caused by: " + cause.ToString()), cause)
		{
		}
	}
}
