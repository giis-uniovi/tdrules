using Java.Util;
using Giis.Portable.Xml.Tiny;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Giis.Tdrules.Model.IO
{
    /// <summary>
    /// Metodos comunes para serializacion/deserializacion de modelos a xml;
    /// </summary>
    public abstract class BaseXmlSerializer
    {
        protected virtual string SetAttribute(string key, string value)
        {
            if (value == null || value.Equals(""))
                return "";
            return " " + key + "=\"" + XNode.EncodeAttribute(value) + "\"";
        }

        protected virtual string SetAttribute(string key, bool value)
        {
            return SetAttribute(key, value ? "true" : "false");
        }

        protected virtual string SetElemAttribute(int indentation, string key, string value)
        {
            if (value == null || value.Equals(""))
                return "";

            //la indentacion <0 es sin salto de linea, 0 con salto de linea, >0 anyadiendo los espacios indicados 
            string indent = indentation < 0 ? "" : "\n";
            indent += indentation <= 0 ? "" : new string (new char[indentation]).Replace("\0", " ");
            return indent + "<" + key + ">" + XNode.EncodeText(value) + "</" + key + ">";
        }

        protected virtual string SetElemAttribute(string key, string value)
        {
            return SetElemAttribute(-1, key, value); //-1 implica no salto de linea
        }

        //todos los extended attributes (Additional Properties) que se encuentran en un map
        protected virtual string SetExtendedAttributes(Dictionary<string, string> extended)
        {
            if (extended == null)
                return "";
            StringBuilder sb = new StringBuilder();
            foreach (string key in extended.Keys)
                sb.Append(SetAttribute(key, extended[key]));
            return sb.ToString();
        }

        protected virtual string GetElemAttribute(XNode root, string key)
        {
            XNode elem = root.GetChild(key);
            return elem == null ? "" : elem.InnerText();
        }

        //devuelve la lista de todos los atributos delnodo que no coinciden con los atributos nativos
        protected virtual IList<string> GetExtendedAttributeNames(XNode root, string[] nativeAttributes)
        {
            IList<string> ret = new List<string>();
            if (root == null)
                return ret;

            //no hay atributos nativos devuelve todos los que encuentre
            IList<string> allList = root.GetAttributeNames();
            if (nativeAttributes.Length == 0)
                return allList;

            //hay algun atributo nativo que hay que excluir del valor devuelto
            foreach (string item in allList)
                if (!ArrayContainsString(nativeAttributes, item))
                    ret.Add(item);
            return ret;
        }

        private bool ArrayContainsString(string[] searchIn, string searchValue)
        {
            foreach (string item in searchIn)
                if (item.Equals(searchValue))
                    return true;
            return false;
        }
    }
}