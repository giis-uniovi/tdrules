
using Java.Util;


using NLog;
using Giis.Portable.Util;
using Giis.Visualassert;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Model
{
    public class Base
    {
        protected static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(Base));
        
        [NUnit.Framework.SetUp]
        public virtual void SetUp()
        {
            log.Info("****** Running test: {} ******", NUnit.Framework.TestContext.CurrentContext.Test.Name);
        }

        protected VisualAssert va = new VisualAssert();
        protected static string TEST_PATH_BENCHMARK = Parameters.IsJava() ? "src/test/resources" : FileUtil.GetPath(Parameters.GetProjectRoot(), "../tdrules-model/src/test/resources");
        protected static string TEST_PATH_OUTPUT = Parameters.IsJava() ? "target" : FileUtil.GetPath(Parameters.GetProjectRoot(), "reports");
        protected static HashMap<string, string> SingletonMap(string key, string value)
        {
            HashMap<string, string> map = new HashMap<string, string>();
            map.Put(key, value);
            return map;
        }

        public virtual string ReadFile(string fileName)
        {
            return FileUtil.FileRead(TEST_PATH_BENCHMARK, fileName);
        }

        public virtual void WriteFile(string fileName, string content)
        {
            FileUtil.CreateDirectory(TEST_PATH_OUTPUT); // ensure that folder exists
            FileUtil.FileWrite(TEST_PATH_OUTPUT, fileName, content);
        }

        public virtual void AssertContains(string expectedSubstring, string actual)
        {
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(actual.Contains(expectedSubstring), "Expected substring should be contained in actual: " + actual);
        }
    }
}