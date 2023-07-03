/////////////////////////////////////////////////////////////////////////////////////////////
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////
/////////////////////////////////////////////////////////////////////////////////////////////
using System.Collections.Generic;
using Giis.Portable.Util;
using Giis.Visualassert;
using NLog;
using NUnit.Framework;


using Sharpen;

namespace Test4giis.Tdrules.Model
{
	public class Base
	{
		protected internal static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(Base));

		
		

		[NUnit.Framework.SetUp]
		public virtual void SetUp()
		{
			log.Info("****** Running test: {} ******", NUnit.Framework.TestContext.CurrentContext.Test.Name );
		}

		protected internal VisualAssert va = new VisualAssert();

		protected internal static string TestPathBenchmark = Parameters.IsJava() ? "src/test/resources" : FileUtil.GetPath(Parameters.GetProjectRoot(), "../tdrules-model/src/test/resources");

		protected internal static string TestPathOutput = Parameters.IsJava() ? "target" : FileUtil.GetPath(Parameters.GetProjectRoot(), "reports");

		protected internal static Dictionary<string, string> SingletonMap(string key, string value)
		{
			Dictionary<string, string> map = new Dictionary<string, string>();
			map[key] = value;
			return map;
		}

		public virtual string ReadFile(string fileName)
		{
			return FileUtil.FileRead(TestPathBenchmark, fileName);
		}

		public virtual void WriteFile(string fileName, string content)
		{
			FileUtil.CreateDirectory(TestPathOutput);
			// ensure that folder exists
			FileUtil.FileWrite(TestPathOutput, fileName, content);
		}

		public virtual void AssertContains(string expectedSubstring, string actual)
		{
			NUnit.Framework.Assert.IsTrue(actual.Contains(expectedSubstring), "Expected substring should be contained in actual: " + actual);
		}
	}
}
