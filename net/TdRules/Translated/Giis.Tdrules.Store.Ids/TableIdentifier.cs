using Giis.Portable.Util;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Ids
{
    /// <summary>
    /// Metodos de utilidad para manejo de identificadores de tabla y columna en un
    /// esquema, qualificados y sin qualificar
    /// </summary>
    public class TableIdentifier : SimpleIdentifier
    {
        private string cat = "";
        private string sch = "";
        private string tab = "";
        private bool processQuotes = false;
        /// <summary>
        /// Instanciacion realizada a partir de los metadatos, forma el objeto con el
        /// catalogo y esquema indicados, si no existen aplica los establecidos por
        /// defecto
        /// </summary>
        public TableIdentifier(string defCat, string defSch, string catalog, string schema, string table, bool processQuotes)
        {
            this.processQuotes = processQuotes;
            Fill(defCat, defSch, catalog, schema, table);
        }

        public TableIdentifier(string defCat, string defSch, string name, bool processQuotes)
        {
            this.processQuotes = processQuotes;
            string[] comp;
            if (name.Contains("["))
                comp = Quotation.SplitQuotedRight(name, '[', ']', '.', 3);
            else
                comp = Quotation.SplitQuotedRight(name, '"', '"', '.', 3);
            Fill(defCat, defSch, comp[0], comp[1], comp[2]);
        }

        public TableIdentifier(string name, bool processQuotes) : this("", "", name, processQuotes)
        {
        }

        public virtual string GetCat()
        {
            return cat;
        }

        public virtual string GetSch()
        {
            return sch;
        }

        public virtual string GetTab()
        {
            return tab;
        }

        public virtual void SetCat(string cat)
        {
            this.cat = cat;
        }

        public virtual void SetSch(string sch)
        {
            this.sch = sch;
        }

        public virtual void SetTab(string tab)
        {
            this.tab = tab;
        }

        public override string ToString()
        {
            return "cat=" + cat + ", sch=" + sch + ", tab=" + tab;
        }

        private void Fill(string defCat, string defSch, string catalog, string schema, string table)
        {
            this.cat = ProcessIdentifier(catalog, processQuotes);
            this.sch = ProcessIdentifier(schema, processQuotes);
            this.tab = ProcessIdentifier(table, processQuotes);

            // rellena con los valores por defecto si no se habian especificado
            // sch/cat no sera nulo pues ha sido procesado antes
            if (this.cat.Equals(""))
                this.cat = ProcessIdentifier(defCat, processQuotes);
            if (this.sch.Equals(""))
                this.sch = ProcessIdentifier(defSch, processQuotes);
        }

        /// <summary>
        /// nombre completamente qualificado, rellenando con los valores por defecto de
        /// catalogo y esquema
        /// </summary>
        public virtual string GetFullQualifiedTableName(string defCat, string defSch)
        {
            defCat = ProcessIdentifier(defCat, processQuotes);
            defSch = ProcessIdentifier(defSch, processQuotes);
            string catalog = (this.cat.Equals("") ? defCat : this.cat);
            string schema = (this.sch.Equals("") ? defSch : this.sch);
            return GetQualifiedName(catalog, schema, this.tab);
        }

        /// <summary>
        /// nombre completamente qualificado,
        /// </summary>
        public virtual string GetFullQualifiedTableName()
        {
            return GetQualifiedName(this.cat, this.sch, this.tab);
        }

        /// <summary>
        /// nombre qualificado excluyendo catalogo y esquema cuando tienen los valores
        /// por defecto de catalgo y esquema indicados
        /// </summary>
        public virtual string GetDefaultQualifiedTableName(string defCat, string defSch)
        {
            defCat = ProcessIdentifier(defCat, processQuotes);
            defSch = ProcessIdentifier(defSch, processQuotes);
            string catalog = (JavaCs.EqualsIgnoreCase(this.cat, defCat) ? "" : this.cat);
            string schema = (JavaCs.EqualsIgnoreCase(this.sch, defSch) ? "" : this.sch);
            return GetQualifiedName(catalog, schema, this.tab);
        }

        /// <summary>
        /// Obtiene el nombre completamente qualificado con los datos pasados como
        /// parametro
        /// </summary>
        public static string GetQualifiedName(string catalog, string schema, string table)
        {
            if (catalog == null)
                catalog = "";
            if (schema == null)
                schema = "";
            if (table == null)
                table = "";
            if (table.Equals(""))
                throw new Exception("SchemaTableIdentifier.getQualifiedName: table name is empty"); // NOSONAR
            string name = "";
            if (!catalog.Equals("") && !schema.Equals(""))
                name += catalog + "." + schema + ".";
            else if (catalog.Equals("") && !schema.Equals(""))
                name += schema + ".";
            else if (!catalog.Equals("") && schema.Equals(""))
                name += catalog + "..";
            name += table;
            return name;
        }
    }
}