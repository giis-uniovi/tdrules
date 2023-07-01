package giis.tdrules.store.ids;

import java.util.ArrayList;
import java.util.List;

import giis.portable.util.JavaCs;

/**
 * Manejo de strings delimitados por comillas u otros caracterse
 */
public class Quotation {
	private Quotation() {
		throw new IllegalAccessError("Utility class");
	}

	/**
	 * Determina si un string esta entre los caracteres openQuote y closeQuote
	 */
	public static boolean isQuoted(String str, char openQuote, char closeQuote) {
		return str.length() >= 2 && str.charAt(0) == openQuote && str.charAt(str.length() - 1) == closeQuote;
	}

	/**
	 * Elimina quotation si un string esta entre los caracteres openQuote y
	 * closeQuote
	 */
	public static String removeQuotes(String str, char openQuote, char closeQuote) {
		if (str.length() > 0 && str.charAt(0) == openQuote)
			str = JavaCs.substring(str, 1);
		if (str.length() > 0 && str.charAt(str.length() - 1) == closeQuote)
			str = JavaCs.substring(str, 0, str.length() - 1);
		return str;
	}

	/**
	 * Devuelve un array de strings resultado de dividir el string pasado en
	 * palabras, teniendo en cuenta que un texto entre comillas forma una sola
	 * palabra
	 */
	public static String[] splitQuoted(String str, char openQuote, char closeQuote, char separator) {
		char[] sepArray = new char[1];
		sepArray[0] = separator;
		return splitQuoted(str, openQuote, closeQuote, sepArray);
	}

	/**
	 * Realiza un split de un string similar a splitQuoted pero alineando a la
	 * derecha el ultimo item, dentro de un array de longitud fija especificada por
	 * size. Usado para obtener componentes de tablas y columnas sin/con qualificar
	 * (hace trim de los nombres)
	 */
	public static String[] splitQuotedRight(String str, char openQuote, char closeQuote, char separator, int size) {
		String[] dest = new String[size];
		for (int i = 0; i < dest.length; i++)
			dest[i] = "";
		// parte en componentes y coloca cada uno en su lugar ajustados hacia el ultimo
		if ("".equals(str))
			throw new RuntimeException("Quotation.splitQuotedRight: Name is empty"); // NOSONAR
		String[] comp = splitQuoted(str, openQuote, closeQuote, separator);
		if (comp.length > size)
			throw new RuntimeException( // NOSONAR
					"Quotation.splitQuotedRight: Name has more than " + size + " componentes: " + str);
		for (int i = 0 + size - comp.length; i < dest.length; i++)
			dest[i] = comp[i - (size - comp.length)].trim();
		return dest;
	}

	/**
	 * Split usando espacio como separador, evita devolver items vacios cuando hay
	 * mas de un espacio
	 */
	public static String[] splitQuotedWords(String str, char openQuote, char closeQuote) {
		// hace un splitQuoted respecto de blancos y espacions
		String[] scw1 = Quotation.splitQuoted(str, openQuote, closeQuote, new char[] { ' ', '\t', '\n', '\r' });
		// Cuando hay varios espacios seguidos oriiginan strings vacios, los elimino
		// declara como ArrayList para evitar problemas en traduccion a csharp
		ArrayList<String> scw2 = new ArrayList<>();
		for (int i = 0; i < scw1.length; i++)
			if (!scw1[i].trim().equals(""))
				scw2.add(scw1[i]);
		return JavaCs.toArray(scw2);
	}

	/**
	 * Metodo general comun para ralizar un split de un string que puede incluir
	 * comillas en un array de strings de acuerdo con los separadores indicados. El
	 * contenido entre comillas es tratado como una unidad no separable aunque
	 * contenga caracteres que coinciden con el separador
	 * 
	 * @param str        string original
	 * @param openQuote  caracter de comilla (apertura)
	 * @param closeQuote caracter de comilla (cierre)
	 * @param separator  separadores en base a los que se hace el split
	 * @return cada uno de los strings que han sido separados
	 */
	public static String[] splitQuoted(String str, char openQuote, char closeQuote, char[] separator) {
		boolean inQuote = false; // dentro de un texto: con comillas o fuera
		List<Integer> beginChar = new ArrayList<>(); // posiciones de inicio de cada string resultado
		List<Integer> endChar = new ArrayList<>(); // posiciones de fin+1 de cada string resultado
		int beginCharIndex = 0; // posicion inicial del string en curso para recordar al final
		// Recorre cada caracter y lo procesa, determinando las listas que determinaran
		// el inicio y fin
		// de cada uno de los strings del resultado
		for (int i = 0; i < str.length(); i++) {
			char current = str.charAt(i);
			boolean isSeparator = false; // indica si el current caracter es un separador
			for (int j = 0; j < separator.length; j++) {
				if (current == separator[j])
					isSeparator = true;
			}
			if (inQuote && current == closeQuote)
				inQuote = false;
			else if (!inQuote && current == openQuote)
				inQuote = true;
			else if (isSeparator && !inQuote) { // fin del string si hay separador no en quote
				beginChar.add(beginCharIndex);
				endChar.add(i);
				beginCharIndex = i + 1;
			}
		}
		// si me quedo en estado inQuote ha habido una comilla sin cerrar
		if (inQuote)
			throw new RuntimeException("Str.splitQuoted: Quote not closed in string " + str); // NOSONAR
		// al final siempre me quedara un string que procesar, aunque sea vacio
		beginChar.add(beginCharIndex);
		endChar.add(str.length());

		// ahora crea la lista con todos los substring resultado
		String[] res = new String[beginChar.size()];
		for (int i = 0; i < res.length; i++) {
			int begin = beginChar.get(i).intValue();
			int end = endChar.get(i).intValue();
			String partial = JavaCs.substring(str, begin, end);
			res[i] = partial;
		}
		return res;
	}

}
