/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System;
using System.Collections.Generic;
using Giis.Portable.Util;
using Sharpen;

namespace Giis.Tdrules.Store.Ids
{
	/// <summary>Manejo de strings delimitados por comillas u otros caracterse</summary>
	public class Quotation
	{
		private Quotation()
		{
			throw new InvalidOperationException("Utility class");
		}

		/// <summary>Determina si un string esta entre los caracteres openQuote y closeQuote</summary>
		public static bool IsQuoted(string str, char openQuote, char closeQuote)
		{
			return str.Length >= 2 && str[0] == openQuote && str[str.Length - 1] == closeQuote;
		}

		/// <summary>
		/// Elimina quotation si un string esta entre los caracteres openQuote y
		/// closeQuote
		/// </summary>
		public static string RemoveQuotes(string str, char openQuote, char closeQuote)
		{
			if (str.Length > 0 && str[0] == openQuote)
			{
				str = JavaCs.Substring(str, 1);
			}
			if (str.Length > 0 && str[str.Length - 1] == closeQuote)
			{
				str = JavaCs.Substring(str, 0, str.Length - 1);
			}
			return str;
		}

		/// <summary>
		/// Devuelve un array de strings resultado de dividir el string pasado en
		/// palabras, teniendo en cuenta que un texto entre comillas forma una sola
		/// palabra
		/// </summary>
		public static string[] SplitQuoted(string str, char openQuote, char closeQuote, char separator)
		{
			char[] sepArray = new char[1];
			sepArray[0] = separator;
			return SplitQuoted(str, openQuote, closeQuote, sepArray);
		}

		/// <summary>
		/// Realiza un split de un string similar a splitQuoted pero alineando a la
		/// derecha el ultimo item, dentro de un array de longitud fija especificada por
		/// size.
		/// </summary>
		/// <remarks>
		/// Realiza un split de un string similar a splitQuoted pero alineando a la
		/// derecha el ultimo item, dentro de un array de longitud fija especificada por
		/// size. Usado para obtener componentes de tablas y columnas sin/con qualificar
		/// (hace trim de los nombres)
		/// </remarks>
		public static string[] SplitQuotedRight(string str, char openQuote, char closeQuote, char separator, int size)
		{
			string[] dest = new string[size];
			for (int i = 0; i < dest.Length; i++)
			{
				dest[i] = string.Empty;
			}
			// parte en componentes y coloca cada uno en su lugar ajustados hacia el ultimo
			if (string.Empty.Equals(str))
			{
				throw new Exception("Quotation.splitQuotedRight: Name is empty");
			}
			// NOSONAR
			string[] comp = SplitQuoted(str, openQuote, closeQuote, separator);
			if (comp.Length > size)
			{
				throw new Exception("Quotation.splitQuotedRight: Name has more than " + size + " componentes: " + str);
			}
			// NOSONAR
			for (int i_1 = 0 + size - comp.Length; i_1 < dest.Length; i_1++)
			{
				dest[i_1] = comp[i_1 - (size - comp.Length)].Trim();
			}
			return dest;
		}

		/// <summary>
		/// Split usando espacio como separador, evita devolver items vacios cuando hay
		/// mas de un espacio
		/// </summary>
		public static string[] SplitQuotedWords(string str, char openQuote, char closeQuote)
		{
			// hace un splitQuoted respecto de blancos y espacions
			string[] scw1 = Giis.Tdrules.Store.Ids.Quotation.SplitQuoted(str, openQuote, closeQuote, new char[] { ' ', '\t', '\n', '\r' });
			// Cuando hay varios espacios seguidos oriiginan strings vacios, los elimino
			// declara como ArrayList para evitar problemas en traduccion a csharp
			List<string> scw2 = new List<string>();
			for (int i = 0; i < scw1.Length; i++)
			{
				if (!scw1[i].Trim().Equals(string.Empty))
				{
					scw2.Add(scw1[i]);
				}
			}
			return JavaCs.ToArray(scw2);
		}

		/// <summary>
		/// Metodo general comun para ralizar un split de un string que puede incluir
		/// comillas en un array de strings de acuerdo con los separadores indicados.
		/// </summary>
		/// <remarks>
		/// Metodo general comun para ralizar un split de un string que puede incluir
		/// comillas en un array de strings de acuerdo con los separadores indicados. El
		/// contenido entre comillas es tratado como una unidad no separable aunque
		/// contenga caracteres que coinciden con el separador
		/// </remarks>
		/// <param name="str">string original</param>
		/// <param name="openQuote">caracter de comilla (apertura)</param>
		/// <param name="closeQuote">caracter de comilla (cierre)</param>
		/// <param name="separator">separadores en base a los que se hace el split</param>
		/// <returns>cada uno de los strings que han sido separados</returns>
		public static string[] SplitQuoted(string str, char openQuote, char closeQuote, char[] separator)
		{
			bool inQuote = false;
			// dentro de un texto: con comillas o fuera
			IList<int> beginChar = new List<int>();
			// posiciones de inicio de cada string resultado
			IList<int> endChar = new List<int>();
			// posiciones de fin+1 de cada string resultado
			int beginCharIndex = 0;
			// posicion inicial del string en curso para recordar al final
			// Recorre cada caracter y lo procesa, determinando las listas que determinaran
			// el inicio y fin
			// de cada uno de los strings del resultado
			for (int i = 0; i < str.Length; i++)
			{
				char current = str[i];
				bool isSeparator = false;
				// indica si el current caracter es un separador
				for (int j = 0; j < separator.Length; j++)
				{
					if (current == separator[j])
					{
						isSeparator = true;
					}
				}
				if (inQuote && current == closeQuote)
				{
					inQuote = false;
				}
				else
				{
					if (!inQuote && current == openQuote)
					{
						inQuote = true;
					}
					else
					{
						if (isSeparator && !inQuote)
						{
							// fin del string si hay separador no en quote
							beginChar.Add(beginCharIndex);
							endChar.Add(i);
							beginCharIndex = i + 1;
						}
					}
				}
			}
			// si me quedo en estado inQuote ha habido una comilla sin cerrar
			if (inQuote)
			{
				throw new Exception("Str.splitQuoted: Quote not closed in string " + str);
			}
			// NOSONAR
			// al final siempre me quedara un string que procesar, aunque sea vacio
			beginChar.Add(beginCharIndex);
			endChar.Add(str.Length);
			// ahora crea la lista con todos los substring resultado
			string[] res = new string[beginChar.Count];
			for (int i_1 = 0; i_1 < res.Length; i_1++)
			{
				int begin = beginChar[i_1];
				int end = endChar[i_1];
				string partial = JavaCs.Substring(str, begin, end);
				res[i_1] = partial;
			}
			return res;
		}
	}
}
