//This is part of CORE.Net, don't edit outside this solution
namespace Java.Sql
{
    public class ConnectionConstants
    {
        //Isolation levels, no los implementa, solo incluye metodo vacio en Connection para compatibilidad con java
        public static int TransactionNone = 0;
        public static int TransactionReadUncommitted = 1;
        public static int TransactionReadCommitted = 2;
        public static int TransactionRepeatableRead = 4;
        public static int TransactionSerializable = 8;
    }
}
