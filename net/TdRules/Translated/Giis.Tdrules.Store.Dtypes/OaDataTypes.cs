/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Sharpen;

namespace Giis.Tdrules.Store.Dtypes
{
	public class OaDataTypes : DataTypes
	{
		protected internal OaDataTypes()
			: base()
		{
		}

		/// <summary>Configuracion del mapeo id-tipo de datos</summary>
		protected internal override void ConfigureAllIds()
		{
			ConfigureId(DtCharacter, new string[] { "string" });
			ConfigureId(DtInteger, new string[] { "integer", "int32", "int64" });
			ConfigureId(DtExactNumeric, new string[] {  });
			ConfigureId(DtApproximateNumeric, new string[] { "number", "float", "double" });
			ConfigureId(DtLogical, new string[] { "boolean" });
			ConfigureId(DtDate, new string[] { "date" });
			ConfigureId(DtTime, new string[] {  });
			ConfigureId(DtDatetime, new string[] { "date-time" });
			ConfigureId(DtInterval, new string[] {  });
			ConfigureId(DtBlob, new string[] { "byte", "binary" });
		}

		/// <summary>Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar</summary>
		public override string GetDefault()
		{
			return "integer";
		}
	}
}
