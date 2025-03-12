
using Java.Sql;


using NLog;
using Giis.Portable.Util;
using Giis.Tdrules.Store.Rdb;
using Giis.Visualassert;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Common definitions to be inherited by the database tests.
    /// The reference implementation of tests is Sqlserver,
    /// that is inherited to test other DBMS
    /// </summary>
    public class Base
    {
        protected static readonly Logger log = Giis.Portable.Util.NLogUtil.GetLogger(typeof(Base));
        protected static readonly string PLATFORM = Parameters.GetPlatformName();
        protected static readonly string SETUP_PATH = FileUtil.GetPath(Parameters.GetProjectRoot(), "..", "setup");
        protected static readonly string ENVIRONMENT_PROPERTIES = FileUtil.GetPath(SETUP_PATH, "environment.properties");
        protected static readonly string DATABASE_PROPERTIES = FileUtil.GetPath(SETUP_PATH, "database.properties");
        // nombres de las bases de datos a utilizar
        public static readonly string TEST_DBNAME2 = "tdstorerdb2";
        // Prefijo que se pone antes del nombre de las tablas al crear
        // necesario en SQLServer cuando se entra con un usuario no privilegiado para poner dbo como owner
        protected static string tablePrefix = "";
        // Datos principales para parametrizacion del sgbd usado en el test
        // Esta es la referencia, el resto de sgbds son clases heredadas que cambian este valor
        protected string dbmsname = "sqlserver";
        protected string dbmsproductname = "Microsoft SQL Server";
        protected bool storesUpperCase = false;
        protected bool storesLowerCase = false;
        protected VisualAssert va = new VisualAssert();
        protected static readonly string TEST_PATH_BENCHMARK = Parameters.IsJava() ? "src/test/resources" : FileUtil.GetPath(Parameters.GetProjectRoot(), "../tdrules-store-rdb/src/test/resources");
        protected static readonly string TEST_PATH_OUTPUT = Parameters.IsJava() ? "target" : FileUtil.GetPath(Parameters.GetProjectRoot(), "reports");
        
        [NUnit.Framework.SetUp]
        public virtual void SetUp()
        {
            log.Info("****** Running test: {} ******", NUnit.Framework.TestContext.CurrentContext.Test.Name);
        }

        /// <summary>
        /// Connection user and url are obtained from a properties file,
        /// password is obtained from environment, if not defined, another properties file is used as fallback
        /// </summary>
        protected virtual Connection GetConnection(string database)
        {
            log.Debug("Create connection to '{}' database", dbmsname);
            string propPrefix = "tdrules." + PLATFORM + "." + database + "." + dbmsname;
            Connection conn = DriverManager.GetConnection(new JdbcProperties().GetProp(DATABASE_PROPERTIES, propPrefix + ".url"), new JdbcProperties().GetProp(DATABASE_PROPERTIES, propPrefix + ".user"), new JdbcProperties().GetEnvVar(ENVIRONMENT_PROPERTIES, "TEST_" + dbmsname.ToUpper() + "_PWD"));

            //Avoid lock problems that make flaky tests when running concurrently with other tests (mainly for getTableList)
            if ("sqlserver".Equals(dbmsname))
                conn.SetTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            return conn;
        }

        protected virtual void Execute(Connection dbt, string sql)
        {
            Statement stmt = dbt.CreateStatement();
            try
            {
                stmt.ExecuteUpdate(sql);
            }
            finally
            {
                stmt.Dispose();
            }
        }

        protected virtual void ExecuteNotThrow(Connection dbt, string sql)
        {
            Statement stmt = dbt.CreateStatement();
            try
            {
                stmt.ExecuteUpdate(sql);
            }
            catch (SQLException e)
            {
            }
            finally
            {
                stmt.Dispose();
            }
        }

        protected virtual ResultSet Query(Connection dbt, string sql)
        {
            Statement stmt = dbt.CreateStatement();
            return stmt.ExecuteQuery(sql);
        }

        /// <summary>
        /// Cambia el case de un string leido de los metadatos en el caso de que la BD
        /// tenga esta configuracion (p.e. oracle todo en uppercase)
        /// </summary>
        public virtual string AsStored(string value)
        {
            if (storesUpperCase)
                value = value.ToUpper();
            else if (storesLowerCase)
                value = value.ToLower();
            return value;
        }

        public virtual void AssertEqualsDBObj(string expected, string actual)
        {
            NUnit.Framework.Legacy.ClassicAssert.AreEqual(AsStored(expected), actual);
        }

        public virtual void AssertContains(string expectedSubstring, string actual)
        {
            NUnit.Framework.Legacy.ClassicAssert.IsTrue(actual.Contains(expectedSubstring), "Expected substring should be contained in actual: " + actual);
        }

        public virtual void AssertDoesNotContain(string unexpectedSubstring, string actual)
        {
            NUnit.Framework.Legacy.ClassicAssert.IsFalse(actual.Contains(unexpectedSubstring), "Expected substring should not be contained in actual: " + actual);
        }

        protected virtual string GetMetadataAsString(SchemaReader sr)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("Metadata for Table: " + sr.GetTableName());
            sb.Append("  Catalog: " + sr.GetCatalog());
            sb.Append("  Schema: " + sr.GetSchema());
            if (sr.IsView())
                sb.Append("\nView SQL: ").Append(((SchemaReaderJdbc)sr).GetQuery(sr.GetCurrentTable()));
            for (int i = 0; i < sr.GetColumnCount(); i++)
            {
                SchemaColumn col = sr.GetColumn(i);
                sb.Append("\nColumn: ").Append(col.GetColName());
                sb.Append("\n  DataType: ").Append(col.GetDataType());
                sb.Append("  DataSubType: ").Append(col.GetDataSubType());
                sb.Append("  CompositeType: ").Append(col.GetCompositeType());
                sb.Append("\n  ColSize: ").Append(col.GetColSize());
                sb.Append("  DecimalDigits: ").Append(col.GetDecimalDigits());
                sb.Append("  CharacterLike: ").Append(Lower(col.IsCharacterLike()));
                sb.Append("  DateTimeLike: ").Append(Lower(col.IsDateTimeLike()));
                sb.Append("\n  NotNull: ").Append(Lower(col.IsNotNull()));
                sb.Append("  Key: ").Append(Lower(col.IsKey()));
                sb.Append("  Autoincrement: ").Append(Lower(col.IsAutoIncrement()));
                sb.Append("  DefaultValue: ").Append(col.GetDefaultValue());
            }

            return sb.ToString();
        }

        private string Lower(bool value)
        {
            return value ? "true" : "false";
        }

        protected virtual void AssertMetadata(string metadata, string fileName)
        {
            FileUtil.FileWrite(TEST_PATH_OUTPUT, fileName, metadata);
            string expected = FileUtil.FileRead(TEST_PATH_BENCHMARK, fileName);
            va.AssertEquals(expected.Replace("\r", ""), metadata.Replace("\r", ""));
        }
    }
}