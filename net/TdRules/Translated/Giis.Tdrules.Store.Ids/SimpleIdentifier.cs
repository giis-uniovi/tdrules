using Java.Util.Regex;
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
    /// Clase base de la que heredan los identificadores de columna y esquema. Solo
    /// contiene un metodo estatico para preprocesar identificadores simples que
    /// usaran las clases derivadas
    /// </summary>
    public class SimpleIdentifier
    {
        private static Pattern pattern;
        protected SimpleIdentifier()
        {
        }

        /// <summary>
        /// Preprocesa el identificador pasado como parametro eliminando blancos Si se
        /// indica processQuotes transforma las comillas de forma que si el id tiene
        /// espacios en blanco siempre se pone entrecomillado, y si no los tiene, nunca
        /// </summary>
        public static string ProcessIdentifier(string sid, bool processQuotes)
        {
            string id = sid == null ? "" : sid.Trim();
            if (processQuotes)
            {

                // Si hay comillas las quita y hace un trim
                bool quotedWithBrackets = false;
                if (IsQuoted(id))
                {
                    if (JavaCs.CharAt(id, 0) == '"')
                        id = Quotation.RemoveQuotes(id, '"', '"');
                    else if (JavaCs.CharAt(id, 0) == '[')
                    {
                        id = Quotation.RemoveQuotes(id, '[', ']');
                        quotedWithBrackets = true;
                    }

                    id = id.Trim(); // puede haber espacios entre comillas y nombre
                }


                // Vuelve a poner comillas si el id lo necesita
                bool needsQuote = NeedsQuotation(id);
                if (needsQuote && quotedWithBrackets)
                    id = GetQuotedName(id, "[");
                else if (needsQuote)
                    id = GetQuotedName(id, "\"");
            }

            return id;
        }

        private static bool NeedsQuotation(string name)
        {
            lock (typeof(SimpleIdentifier))
            {
                if (pattern == null)
                    pattern = Pattern.Compile("[^a-z0-9_\\$#]", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.Matcher(name);
                return matcher.Find();
            }
        }

        public static bool IsQuoted(string name)
        {
            return Quotation.IsQuoted(name, '"', '"') || Quotation.IsQuoted(name, '[', ']');
        }

        public static string GetQuote(string name)
        {
            if (name.Contains("\""))
            {
                return "\"";
            }
            else if (name.Contains("["))
            {
                return "[";
            }
            else
                return "";
        }

        public static string GetQuotedName(string name, string quoteValue)
        {
            name = name.Trim();
            if (!IsQuoted(name))
                return quoteValue + name + ("[".Equals(quoteValue) ? "]" : quoteValue);
            return name;
        }

        public static string GetLastComponent(string name)
        {
            if (name.Contains("."))
            {
                string[] components = JavaCs.SplitByDot(name);
                return components[components.Length - 1].Trim();
            }

            return name.Trim();
        }
    }
}