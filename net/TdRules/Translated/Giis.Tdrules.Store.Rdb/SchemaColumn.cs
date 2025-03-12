using Giis.Portable.Util;
using Giis.Tdrules.Store.Ids;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Rdb
{
    public class SchemaColumn
    {
        private string colName = ""; // nombre de la ultima columna leida
        private string dataType = ""; // tipo de datos en formato string (segun BD)
        private string compositeType = ""; // Si no es primitivo indica que tipo de estructura o coleccion: array, type
        private string dataSubType = ""; // otros cualificadores como WITH TIMEZONE, o de intervalos: DAY TO SECOND,etc
        private int dataTypeCode = -1; // tipo de datos en formato entero (segun BD)
        private int colSize = 0; // tamanyo en columnas (si aplicable)
        private int decimalDigits = 0; // columnas de la parte decimal (si aplicable)
        private bool isKey = false; // es clave primaria?
        private bool isNotNull = false; // permite nulos?
        private bool isAutoIncrement = false; // columna autoincremental
        protected TableIdentifier foreignKeyTableSchemaIdentifier = null; // tabla referenciada completamenteidentificada
        protected string foreignKeyTable = ""; // tabla referenciada como clave ajena
        protected string foreignKeyColumn = ""; // columna referenciada como clave ajena
        protected string foreignKeyName = ""; // nombre con el que se guarda la FK en la BD
        // public String foreignKeyDestTable=""; //nombre de la tabla destino de la FK
        protected string checkInConstraint = ""; // Condicion de comprobacion en constraints del tipo CHECK IN condition
        protected string defaultValue = ""; // DEFAULT
        public override string ToString()
        {
            return this.GetColName();
        }

        public virtual string GetColName()
        {
            return colName;
        }

        public virtual string GetDataType()
        {
            return dataType;
        }

        public virtual string GetCompositeType()
        {
            return compositeType;
        }

        public virtual string GetDataSubType()
        {
            return dataSubType;
        }

        public virtual int GetDataTypeCode()
        {
            return dataTypeCode;
        }

        public virtual int GetColSize()
        {
            return colSize;
        }

        public virtual int GetDecimalDigits()
        {
            return decimalDigits;
        }

        public virtual bool IsKey()
        {
            return isKey;
        }

        public virtual bool IsNotNull()
        {
            return isNotNull;
        }

        public virtual bool IsAutoIncrement()
        {
            return isAutoIncrement;
        }

        public virtual void SetColName(string colName)
        {
            this.colName = colName;
        }

        public virtual void SetDataType(string dataType)
        {
            this.dataType = dataType;
        }

        public virtual void SetCompositeType(string compositeType)
        {
            this.compositeType = compositeType;
        }

        public virtual void SetDataSubType(string dataSubType)
        {
            this.dataSubType = dataSubType;
        }

        public virtual void SetDataTypeCode(int dataTypeCode)
        {
            this.dataTypeCode = dataTypeCode;
        }

        public virtual void SetColSize(int colSize)
        {
            this.colSize = colSize;
        }

        public virtual void SetDecimalDigits(int decimalDigits)
        {
            this.decimalDigits = decimalDigits;
        }

        public virtual void SetKey(bool isKey)
        {
            this.isKey = isKey;
        }

        public virtual void SetNotNull(bool isNotNull)
        {
            this.isNotNull = isNotNull;
        }

        public virtual void SetAutoIncrement(bool isAutoIncrement)
        {
            this.isAutoIncrement = isAutoIncrement;
        }

        public virtual string GetForeignKeyName()
        {
            return foreignKeyName;
        }

        public virtual string GetCheckInConstraint()
        {
            return checkInConstraint;
        }

        public virtual string GetDefaultValue()
        {
            return defaultValue;
        }

        public virtual void SetForeignKeyName(string fkName)
        {
            this.foreignKeyName = fkName;
        }

        public virtual void SetForeignKeyTable(string foreignKeyTable)
        {
            this.foreignKeyTable = foreignKeyTable;
        }

        public virtual void SetForeignKeyColumn(string foreignKeyColumn)
        {
            this.foreignKeyColumn = foreignKeyColumn;
        }

        public virtual void SetForeignKeyTableSchemaIdentifier(TableIdentifier foreignKeyTableSchemaIdentifier)
        {
            this.foreignKeyTableSchemaIdentifier = foreignKeyTableSchemaIdentifier;
        }

        public virtual void SetCheckInConstraint(string constraint)
        {
            this.checkInConstraint = constraint;
        }

        public virtual void SetDefaultValue(string value)
        {
            this.defaultValue = value;
        }

        public virtual string GetForeignTable()
        {
            return this.foreignKeyTable;
        }

        public virtual string GetForeignKeyColumn()
        {
            return this.foreignKeyColumn;
        }

        public virtual string GetForeignKey()
        {
            return (!this.GetForeignTable().Equals("") ? this.GetForeignTable() + "." : "") + this.GetForeignKeyColumn();
        }

        public virtual void SetForeignKey(string fk)
        {
            this.foreignKeyTable = "";
            this.foreignKeyColumn = "";
            this.foreignKeyTableSchemaIdentifier = null;
            if (fk == null || fk.Trim().Equals(""))
                return;

            // De todos los componentes separados por puntos saca el ultimo que es el nombre
            // de la columna y el reseto que es el nombre de la tabla
            string[] comp = Quotation.SplitQuoted(fk, '"', '"', '.');
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < comp.Length - 1; i++)
                sb.Append((i > 0 ? "." : "") + comp[i]);
            this.foreignKeyTable = sb.ToString();
            this.foreignKeyColumn = comp[comp.Length - 1];
            this.foreignKeyTableSchemaIdentifier = new TableIdentifier(this.foreignKeyTable, true);
        }

        /// <summary>
        /// Indica si el tipo de dato se puede considerar caracter
        /// </summary>
        public virtual bool IsCharacterLike()
        {
            return this.dataType.ToLower().Contains("char") || this.dataType.ToLower().Contains("text");
        }

        /// <summary>
        /// Indica si el tipo de dato se puede considerar que almacena fechas u horas)
        /// </summary>
        public virtual bool IsDateTimeLike()
        {
            return this.dataType.ToLower().Contains("date") || this.dataType.ToLower().Contains("time");
        }

        public virtual bool IsForeignKey()
        {
            return !this.GetForeignKey().Equals("");
        }

        /// <summary>
        /// Obtiene el contenido de esta columna en forma de string SQL a partir de un
        /// valor dado como String. Tiene en cuenta si es un string, fecha, etc.
        /// </summary>
        public virtual string GetAsSqlString(string value)
        {
            if (value == null)
                return "NULL";

            // Excepcion para fechas Oracle. Dependiendo de la version los tipos date vienen
            // con un offset de tiempo o no.
            // Esto es un problema si hay una fk que se basa en esta coluna (como en
            // hr.job_history) pues al insertar en la bd puede haber una excepcion
            // porque no admite insertar con el offset. En este caso se queda con los 10
            // primeros caracteres del valor, ignorando los del tiempo
            // https://forums.oracle.com/forums/thread.jspa?threadID=300473
            if (JavaCs.EqualsIgnoreCase(this.dataType, "date"))
                value = JavaCs.Substring(value, 0, 10); // asumo que son de la forma yyyy-mm-dd

            // muestra el valor con el prefijo (p.e. en fechas), comillas de apertura y
            // cierre, y transformando comillas internas
            return this.GetValuePrefix() + value.Replace("'", "''") + this.GetValueSuffix();
        }

        /// <summary>
        /// Obtiene el prefijo que se anyade a los tipos de datos de fecha/hora
        /// </summary>
        public virtual string GetDateTimeConstantPrefix()
        {
            string dt = this.dataType.ToLower();

            // valores de las constantes segun el estandar SQL
            if (dt.Equals("date") || dt.Equals("time") || dt.Equals("timestamp"))
                return dt.ToUpper() + " ";
            else
                return ""; // cualquier otro no es de tipo fecha
        }

        /// <summary>
        /// Obtiene la parte de texto que precede al valor cuando se asigna un valor a esta columna
        /// </summary>
        public virtual string GetValuePrefix()
        {
            if (this.IsCharacterLike())
                return "'";
            else if (this.IsDateTimeLike())
                return this.GetDateTimeConstantPrefix() + "'";
            else
                return "";
        }

        /// <summary>
        /// Obtiene la parte de texto que sigue al valor cuando se asigna un valor a esta columna
        /// </summary>
        public virtual string GetValueSuffix()
        {
            return this.IsCharacterLike() || this.IsDateTimeLike() ? "'" : "";
        }

        /// <summary>
        /// En algunos sgbd (sqlite) el tipo aparece con parentesis y un numero en vez de
        /// indicar este en la precision, parche para remplazar lo necesario
        /// </summary>
        public virtual void ReparseNameWithPrecision()
        {
            try
            {
                if (dataType.Contains("("))
                {
                    string all = dataType;
                    dataType = JavaCs.Substring(dataType, 0, dataType.IndexOf("("));
                    string precAndScale = JavaCs.Substring(all, all.IndexOf("("), all.Length);
                    precAndScale = Quotation.RemoveQuotes(precAndScale, '(', ')');
                    if (precAndScale.Contains(","))
                    {
                        string[] precOrScale = JavaCs.SplitByChar(precAndScale, ',');
                        colSize = JavaCs.StringToInt(precOrScale[0]);
                        decimalDigits = JavaCs.StringToInt(precOrScale[1]);
                    }
                    else
                    {
                        colSize = JavaCs.StringToInt(precAndScale);
                        decimalDigits = 0;
                    }
                }
            }
            catch (Exception e)
            {
            }
        }
    }
}