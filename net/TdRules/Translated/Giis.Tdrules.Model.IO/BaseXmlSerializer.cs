/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using System.Text;
using Giis.Portable.Xml.Tiny;
using Sharpen;

namespace Giis.Tdrules.Model.IO
{
	/// <summary>Metodos comunes para serializacion/deserializacion de modelos a xml;</summary>
	public abstract class BaseXmlSerializer
	{
		protected internal virtual string SetAttribute(string key, string value)
		{
			if (value == null || value.Equals(string.Empty))
			{
				return string.Empty;
			}
			return " " + key + "=\"" + XNode.EncodeAttribute(value) + "\"";
		}

		protected internal virtual string SetAttribute(string key, bool value)
		{
			return SetAttribute(key, value ? "true" : "false");
		}

		protected internal virtual string SetElemAttribute(int indentation, string key, string value)
		{
			if (value == null || value.Equals(string.Empty))
			{
				return string.Empty;
			}
			//la indentacion <0 es sin salto de linea, 0 con salto de linea, >0 anyadiendo los espacios indicados 
			string indent = indentation < 0 ? string.Empty : "\n";
			indent += indentation <= 0 ? string.Empty : new string(new char[indentation]).Replace("\x0", " ");
			return indent + "<" + key + ">" + XNode.EncodeText(value) + "</" + key + ">";
		}

		protected internal virtual string SetElemAttribute(string key, string value)
		{
			return SetElemAttribute(-1, key, value);
		}

		//-1 implica no salto de linea
		//todos los extended attributes (Additional Properties) que se encuentran en un map
		protected internal virtual string SetExtendedAttributes(IDictionary<string, string> extended)
		{
			if (extended == null)
			{
				return string.Empty;
			}
			StringBuilder sb = new StringBuilder();
			foreach (KeyValuePair<string, string> attr in extended)
			{
				sb.Append(SetAttribute(attr.Key, attr.Value));
			}
			return sb.ToString();
		}

		protected internal virtual string GetElemAttribute(XNode root, string key)
		{
			XNode elem = root.GetChild(key);
			return elem == null ? string.Empty : elem.InnerText();
		}

		//devuelve la lista de todos los atributos delnodo que no coinciden con los atributos nativos
		protected internal virtual IList<string> GetExtendedAttributeNames(XNode root, string[] nativeAttributes)
		{
			IList<string> ret = new List<string>();
			if (root == null)
			{
				return ret;
			}
			//no hay atributos nativos devuelve todos los que encuentre
			IList<string> allList = root.GetAttributeNames();
			if (nativeAttributes.Length == 0)
			{
				return allList;
			}
			//hay algun atributo nativo que hay que excluir del valor devuelto
			foreach (string item in allList)
			{
				if (!ArrayContainsString(nativeAttributes, item))
				{
					ret.Add(item);
				}
			}
			return ret;
		}

		private bool ArrayContainsString(string[] searchIn, string searchValue)
		{
			foreach (string item in searchIn)
			{
				if (item.Equals(searchValue))
				{
					return true;
				}
			}
			return false;
		}
	}
}
