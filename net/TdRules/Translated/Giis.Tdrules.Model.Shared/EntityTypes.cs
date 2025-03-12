using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Model.Shared
{
    public class EntityTypes
    {
        private EntityTypes()
        {
            throw new InvalidOperationException("Utility class");
        }

        public static readonly string DT_TABLE = "table";
        public static readonly string DT_VIEW = "view";
        public static readonly string DT_TYPE = "type";
        public static readonly string DT_ARRAY = "array";
    }
}