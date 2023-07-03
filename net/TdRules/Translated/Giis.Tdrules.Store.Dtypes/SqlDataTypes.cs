/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using Sharpen;

namespace Giis.Tdrules.Store.Dtypes
{
	public class SqlDataTypes : DataTypes
	{
		protected internal SqlDataTypes()
			: base()
		{
		}

		/// <summary>Configuracion del mapeo id-tipo de datos</summary>
		protected internal override void ConfigureAllIds()
		{
			ConfigureId(DtCharacter, new string[] { "char", "character", "varchar", "varchar2", "nchar", "nvarchar", "nvarchar2", "text", "ntext" });
			ConfigureId(DtInteger, new string[] { "int", "integer", "smallint", "bigint", "tinyint", "long", "serial", "smallserial", "bigserial" });
			ConfigureId(DtExactNumeric, new string[] { "numeric", "decimal", "number", "currency", "money", "smallmoney" });
			ConfigureId(DtApproximateNumeric, new string[] { "float", "real", "double", "binary_float", "binary_double" });
			ConfigureId(DtLogical, new string[] { "bit", "boolean" });
			ConfigureId(DtDate, new string[] { "date" });
			ConfigureId(DtTime, new string[] { "time" });
			ConfigureId(DtDatetime, new string[] { "timestamp", "datetime", "smalldatetime" });
			ConfigureId(DtInterval, new string[] { "interval" });
			ConfigureId(DtBlob, new string[] { "blob", "longblob", "binary", "varbinary", "image" });
		}

		/// <summary>Devuelve el tipo de datos por defecto a usar cuando no se pueda determinar</summary>
		public override string GetDefault()
		{
			return "int";
		}
	}
}
