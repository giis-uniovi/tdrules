//This is part of CORE.Net, don't edit outside this solution
using System;

namespace Java.Sql
{
    /**
     * Implementacion parcial de clase java.sql para compatibilidad con codigo traducido de Java a C#;
     * Define la excepcion de java.sql.SQLException para poder manejar consultas al estilo Java
     */
    public class SQLException : Exception
    {
        public override string Message { get; }

        public SQLException()
        {
            this.Message = "SQLException";
        }
        public SQLException(string Message)
        {
            this.Message = Message;
        }
    }
}
