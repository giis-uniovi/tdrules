using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Client.Rdb
{
    public class DbException : Exception
    {
        //private static readonly long serialVersionUID = 5671164449308159998;
        public DbException(string message, Exception cause) : base(message + (cause == null ? "" : ". Caused by: " + cause.ToString()), cause)
        {
        }
    }
}