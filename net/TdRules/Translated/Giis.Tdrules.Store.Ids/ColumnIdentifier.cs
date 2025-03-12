using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Store.Ids
{
    /// <summary>
    /// Metodos de utilidad para manejo de identificadores de columna, que incluyen
    /// opcionalmente el identifcador de tabla
    /// </summary>
    public class ColumnIdentifier : SimpleIdentifier
    {
        private TableIdentifier tabId = null; // tabla opcional
        private string col = ""; // nombre de la columna
        /// <summary>
        /// constructor del objeto columna con a partir del nombre completo en la forma
        /// [catalog.[schema.[table.]column]]]
        /// </summary>
        public ColumnIdentifier(string defCat, string defSch, string name, bool processQuotes)
        {

            // parte en todos los componentes
            string[] comp = Quotation.SplitQuotedRight(name, '"', '"', '.', 4);

            // la columna es el ultimo componente
            this.col = ProcessIdentifier(comp[3], processQuotes);

            // el resto es la tabla, opcional si al menos existe el tercero
            if (!comp[2].Equals(""))
                this.tabId = new TableIdentifier(defCat, defSch, comp[0], comp[1], comp[2], processQuotes);
        }

        public virtual string GetCol()
        {
            return col;
        }

        public virtual TableIdentifier GetTabId()
        {
            return tabId;
        }

        public virtual bool IsQualifiedByTable()
        {
            return this.tabId != null;
        }

        /// <summary>
        /// Nombre qualificado excluyendo catalogo y esquema cuando tienen los valores
        /// por defecto de catalgo y esquema indicados (si cat coincide con el parametro,
        /// se omite, idem para sch)
        /// </summary>
        public virtual string GetDefaultQualifiedColumnName(string defCat, string defSch)
        {
            string qTable = "";
            if (this.tabId != null)
                qTable = this.tabId.GetDefaultQualifiedTableName(defCat, defSch) + ".";
            return qTable + this.col;
        }
    }
}