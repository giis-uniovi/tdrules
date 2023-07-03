/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using Sharpen;

namespace Giis.Tdrules.Client.Rdb
{
	[System.Serializable]
	public class DbException : Exception
	{
		private const long serialVersionUID = 5671164449308159998L;

		public DbException(string message, Exception cause)
			: base(message + (cause == null ? string.Empty : ". Caused by: " + cause.ToString()), cause)
		{
		}
	}
}
