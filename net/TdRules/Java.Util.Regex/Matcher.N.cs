using System.Text.RegularExpressions;

namespace Java.Util.Regex
{
    public class Matcher
    {
        private MatchCollection Matches;

        public Matcher(MatchCollection Matches)
        {
            this.Matches = Matches;
        }
        public bool Find()
        {
            return this.Matches.Count > 0;
        }
    }
}
