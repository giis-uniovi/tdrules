using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Rdb
{
    public class SchemaException : Exception
    {
        //private static readonly long serialVersionUID = -4155612383247919170;
        public SchemaException(Exception e) : base("Schema Exception", e)
        {
        }

        public SchemaException(string message) : base(message)
        {
        }

        public SchemaException(string message, Exception cause) : base(message + (cause == null ? "" : ". Caused by: " + cause.ToString()), cause)
        {
        }
    }
}