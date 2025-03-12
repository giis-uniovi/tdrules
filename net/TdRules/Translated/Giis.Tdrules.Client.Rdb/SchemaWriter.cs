using Giis.Tdrules.Openapi.Model;
using Giis.Tdrules.Store.Rdb;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Client.Rdb
{
    /// <summary>
    /// Manages the different stages of the creation of DbSchema model object from a SchemaReader
    /// (begin, writing columns, end)
    /// </summary>
    public class SchemaWriter
    {
        protected TdSchema model;
        protected TdEntity currentTable;
        public SchemaWriter(SchemaReader sr)
        {
            model = new TdSchema();
            model.SetCatalog(sr.GetCatalog());
            model.SetSchema(sr.GetSchema());
            model.SetStoretype(sr.GetDbmsType().ToString());
        }

        public virtual void BeginWriteTable(string tabName, string tableType)
        {
            currentTable = new TdEntity();
            currentTable.SetName(tabName);
            currentTable.SetEntitytype(tableType);
        }

        public virtual void EndWriteTable()
        {
            model.AddEntitiesItem(currentTable);
            currentTable = null;
        }

        public virtual //NOSONAR all parameters needed, simpler than a builder
        void WriteColumn(string colName, string colDataType, string colSubType, int colSize, int decimalDigits, bool isKey, bool isAutoincrement, bool isNotNull, string foreignKey, string foreignKeyName, string checkIn, string defaultValue)
        {
            TdAttribute col = new TdAttribute();
            col.SetName(colName);
            col.SetDatatype(colDataType);
            col.SetSubtype(colSubType);
            string size = "";
            if (colSize > 0)
            {
                size = colSize.ToString();
                if (decimalDigits != 0)
                    size += "," + decimalDigits;
            }

            col.SetSize(size);
            if (isAutoincrement)
                col.SetAutoincrement("true");
            if (isKey)
                col.SetUid("true");
            if (isNotNull)
                col.SetNotnull("true");
            col.SetRid(foreignKey);
            col.SetRidname(foreignKeyName);
            col.SetCheckin(checkIn);
            col.SetDefaultvalue(defaultValue);
            currentTable.AddAttributesItem(col);
        }

        public virtual void WriteCheckConstraint(string colName, string constraintName, string constraint)
        {
            TdCheck check = new TdCheck();
            check.SetAttribute(colName);
            check.SetName(constraintName);
            check.SetConstraint(constraint);
            currentTable.AddChecksItem(check);
        }

        /// <summary>
        /// Returns the model that has been writen in this object
        /// </summary>
        public virtual TdSchema GetModel()
        {
            return this.model;
        }
    }
}