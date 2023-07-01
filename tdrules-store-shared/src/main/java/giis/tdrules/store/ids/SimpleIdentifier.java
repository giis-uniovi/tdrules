package giis.tdrules.store.ids;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import giis.portable.util.JavaCs;

/**
 * Clase base de la que heredan los identificadores de columna y esquema. Solo
 * contiene un metodo estatico para preprocesar identificadores simples que
 * usaran las clases derivadas
 */
public class SimpleIdentifier {
	private static Pattern pattern;

	protected SimpleIdentifier() {
	}

	/**
	 * Preprocesa el identificador pasado como parametro eliminando blancos Si se
	 * indica processQuotes transforma las comillas de forma que si el id tiene
	 * espacios en blanco siempre se pone entrecomillado, y si no los tiene, nunca
	 */
	public static String processIdentifier(String sid, boolean processQuotes) {
		String id = sid == null ? "" : sid.trim();
		if (processQuotes) {
			// Si hay comillas las quita y hace un trim
			boolean quotedWithBrackets = false;
			if (isQuoted(id)) {
				if (id.charAt(0) == '"')
					id = Quotation.removeQuotes(id, '"', '"');
				else if (id.charAt(0) == '[') {
					id = Quotation.removeQuotes(id, '[', ']');
					quotedWithBrackets = true;
				}
				id = id.trim(); // puede haber espacios entre comillas y nombre
			}
			// Vuelve a poner comillas si el id lo necesita
			boolean needsQuote = needsQuotation(id);
			if (needsQuote && quotedWithBrackets)
				id = getQuotedName(id, "[");
			else if (needsQuote) // cualquiera que tenga espacios lo pone entre comillas
				id = getQuotedName(id, "\"");
		}
		return id;
	}

	private static synchronized boolean needsQuotation(String name) {
		if (pattern == null)
			pattern = Pattern.compile("[^a-z0-9_\\$#]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}

	public static boolean isQuoted(String name) {
		return Quotation.isQuoted(name, '"', '"') || Quotation.isQuoted(name, '[', ']');
	}

	public static String getQuote(String name) {
		if (name.contains("\"")) {
			return "\"";
		} else if (name.contains("[")) {
			return "[";
		} else
			return "";
	}

	public static String getQuotedName(String name, String quoteValue) {
		name = name.trim();
		if (!isQuoted(name))
			return quoteValue + name + ("[".equals(quoteValue) ? "]" : quoteValue);
		return name;
	}

	public static String getLastComponent(String name) {
		if (name.contains(".")) {
			String[] components = JavaCs.splitByDot(name);
			return components[components.length - 1].trim();
		}
		return name.trim();
	}

}
