using System;

namespace Java.Sql
{
    /**
     * Wrapper simple para compatibilidad, implementa un numero muy reducido de metodos
     */
    public class Date
    {
        private DateTime date;
        
        private Date(DateTime date) //en java no existe esta forma de instanciar
        {
            this.date = date;
        }
        /**
         * Obtiene el objeto Date a partir de un string formato ISO
         */
        public static Date ValueOf(String stringDate)
        {
            return new Date(DateTime.Parse(stringDate));
        }
        /**
         * Obtiene la representacion formato ISO de la fecha
         */
        public override string ToString()
        {
            return date.ToString("yyyy-MM-dd");
        }
    }
}
