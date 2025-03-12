package giis.tdrules.model.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import giis.portable.xml.tiny.XNode;

/**
 * Metodos comunes para serializacion/deserializacion de modelos a xml;
 */
public abstract class BaseXmlSerializer {
	
	protected String setAttribute(String key, String value) {
		if (value==null || value.equals(""))
			return "";
		return " " + key + "=\"" + XNode.encodeAttribute(value) + "\"";
	}
	protected String setAttribute(String key, boolean value) {
		return setAttribute(key, value ? "true" : "false");
	}
	protected String setElemAttribute(int indentation, String key, String value) {
		if (value==null || value.equals(""))
			return "";
		//la indentacion <0 es sin salto de linea, 0 con salto de linea, >0 anyadiendo los espacios indicados 
		String indent=indentation<0 ? "" : "\n";
		indent+=indentation<=0 ? "" : new String(new char[indentation]).replace("\0", " ");
		return indent + "<" + key + ">" + XNode.encodeText(value) + "</" + key + ">";
	}
	protected String setElemAttribute(String key, String value) {
		return setElemAttribute(-1, key, value); //-1 implica no salto de linea
	}
	//todos los extended attributes (Additional Properties) que se encuentran en un map
	protected String setExtendedAttributes(Map<String, String> extended) {
		if (extended==null)
			return "";
		StringBuilder sb=new StringBuilder();
		for (String key : extended.keySet()) // NOSONAR don't use entry set for .NET conversion compatibility
			sb.append(setAttribute(key, extended.get(key)));
		return sb.toString();
	}

	protected String getElemAttribute(XNode root, String key) {
		XNode elem=root.getChild(key);
		return elem==null ? "" : elem.innerText();
	}
	//devuelve la lista de todos los atributos delnodo que no coinciden con los atributos nativos
	protected List<String> getExtendedAttributeNames(XNode root, String[] nativeAttributes) {
		List<String> ret=new ArrayList<String>();
		if (root==null)
			return ret;
		//no hay atributos nativos devuelve todos los que encuentre
		List<String> allList=root.getAttributeNames();
		if (nativeAttributes.length==0)
			return allList;
		//hay algun atributo nativo que hay que excluir del valor devuelto
		for (String item: allList)
			if (!arrayContainsString(nativeAttributes, item))
				ret.add(item);
		return ret;
	}
	private boolean arrayContainsString(String[] searchIn, String searchValue) {
		for (String item: searchIn)
			if (item.equals(searchValue))
				return true;
		return false;
	}

}
