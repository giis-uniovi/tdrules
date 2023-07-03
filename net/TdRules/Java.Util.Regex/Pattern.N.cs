using System.Text.RegularExpressions;

namespace Java.Util.Regex
{
    public class Pattern
    {
        public const RegexOptions CaseInsensitive = RegexOptions.IgnoreCase;

        private System.Text.RegularExpressions.Regex CompiledPattern;

        public static Pattern Compile(string regularExpression, RegexOptions options)
        {
            Pattern thisPattern = new Pattern();
            thisPattern.CompiledPattern = new System.Text.RegularExpressions.Regex(regularExpression,
                RegexOptions.Compiled | options);
            return thisPattern;
        }
        public Matcher Matcher(string text)
        {
            MatchCollection Matches = CompiledPattern.Matches(text);
            return new Matcher(Matches);
        }
    }
}
