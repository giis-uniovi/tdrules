using System.Collections;
using System.Collections.Generic;
using System.Linq;

namespace Sharpen
{
    public class Stub
    {
    }

    public static class Runtime
    {
        public static bool EqualsIgnoreCase(string thisString, string anotherString)
        {
            return thisString.Equals(anotherString, System.StringComparison.CurrentCultureIgnoreCase);
        }
    }

    public class Collections
    {
        public static bool AddAll<T>(ICollection<T> list, IEnumerable toAdd)
        {
            foreach (T t in toAdd)
                list.Add(t);
            return true;
        }
        public static U[] ToArray<T, U>(ICollection<T> list, U[] res) where T : U
        {
            if (res.Length < list.Count)
                res = new U[list.Count];

            int n = 0;
            foreach (T t in list)
                res[n++] = t;

            if (res.Length > list.Count)
                res[list.Count] = default(T);
            return res;
        }
    }
    public class Arrays
    {
        public static List<T> AsList<T>(params T[] array)
        {
            return array.ToList<T>();
        }
    }
}
