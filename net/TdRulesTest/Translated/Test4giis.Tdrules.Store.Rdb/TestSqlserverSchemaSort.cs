using Java.Sql;
using NUnit.Framework;
using Giis.Portable.Util;
using Giis.Tdrules.Store.Rdb;
using Giis.Tdrules.Store.Stypes;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
/////// THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT ///////

namespace Test4giis.Tdrules.Store.Rdb
{
    /// <summary>
    /// Ordering of tables according their master-detail relationships
    /// </summary>
    public class TestSqlserverSchemaSort : Base
    {
        protected string catalog = null;
        protected string schema = null;
        protected StoreType dbms;
        protected string enableCheck = ""; // algunas dbms como oracle requieren que se especifique enabled en la creacion
        protected Connection dbt;
        [NUnit.Framework.SetUp]
        public override void SetUp()
        {
            base.SetUp();
            DropTableShapes();
            dbms = StoreType.Get(dbmsname);
            dbt = GetConnection(TEST_DBNAME2);
        }

        // Estos tests utilizan muchas tablas que se crearan en el momento de la ejecucion de cada test
        // El borrado se realiza para todas las tablas, como hay relaciones recursivas, las elimina antes
        protected virtual void DropTableShapes()
        {
            Connection db = GetConnection(TEST_DBNAME2);
            ExecuteNotThrow(db, "alter table dg0 drop constraint fk_dg0_dg0");
            ExecuteNotThrow(db, "alter table dgd drop constraint fk_dgd_dgm");
            ExecuteNotThrow(db, "alter table dg1 drop constraint fk_dg1_dgm");
            ExecuteNotThrow(db, "alter table dgx drop constraint fk_dgx_dgd");
            ExecuteNotThrow(db, "alter table dg2 drop constraint fk_dg2_dgd");
            ExecuteNotThrow(db, "alter table dgm drop constraint fk_dgm_dg2");
            ExecuteNotThrow(db, "alter table dgm drop constraint fk_dgm_dg0");
            ExecuteNotThrow(db, "alter table dgd drop constraint fk_dgd_dgm");
            ExecuteNotThrow(db, "alter table dgd drop constraint fk_dgd_dg1");
            ExecuteNotThrow(db, "alter table dgd drop constraint fk_dgd_dg0");
            ExecuteNotThrow(db, "alter table dg3 drop constraint fk_dg3_dg1");
            ExecuteNotThrow(db, "alter table dg3 drop constraint fk_dg3_dg2");
            ExecuteNotThrow(db, "drop table dg0");
            ExecuteNotThrow(db, "drop table dg1");
            ExecuteNotThrow(db, "drop table dg2");
            ExecuteNotThrow(db, "drop table dgd");
            ExecuteNotThrow(db, "drop table dgm");
            ExecuteNotThrow(db, "drop table dgx");
            ExecuteNotThrow(db, "drop table dg3");
        }

        protected virtual void CreateTableShapes(bool useDiamond, bool extendMasterDetail, bool recursive, bool shortCycle, bool longCycle)
        {
            Connection db = GetConnection(TEST_DBNAME2);

            // relacion lineal maestro dgm detalle dgd a traves de dg0
            Execute(db, "create table dgm (Pk1 int, primary key(Pk1), Fk1 int not null)");
            Execute(db, "create table dg0 (Pk1 int, primary key(Pk1), Fk1 int not null, Fk2 int not null)");
            Execute(db, "create table dgd (Pk1 int, primary key(Pk1), Fk1 int not null, Fk2 int not null)");
            Execute(db, "alter table dgd add constraint FK_DGD_DG0 foreign key(Fk1) references dg0(pk1)");
            Execute(db, "alter table dg0 add constraint FK_DG0_DG1 foreign key(Fk1) references dgm(pk1)");
            if (useDiamond)
            {

                // dg1 en paralelo a dg0, se forma un diamante
                Execute(db, "create table dg1 (Pk1 int, primary key(Pk1), Fk1 int not null)");
                Execute(db, "alter table dgd add constraint FK_DGD_DG1 foreign key(Fk2) references dg1(pk1)");
                Execute(db, "alter table dg1 add constraint FK_DG1_DGM foreign key(Fk1) references dgm(pk1)");
            }

            if (extendMasterDetail)
            {

                // extiende con otras tablas por encima (dg2) y debajo (dgx), con un detalle
                // adicional en dg2 (dg3), v invertida como master de todos
                Execute(db, "create table dg2 (Pk1 int, primary key(Pk1), Fk1 int)");
                Execute(db, "alter table dgm add constraint FK_DGM_DG2 foreign key(Fk1) references dg2(pk1)");
                Execute(db, "create table dg3 (Pk1 int, primary key(Pk1), Fk1 int)");
                Execute(db, "alter table dg3 add constraint FK_DG3_DG2 foreign key(Fk1) references dg2(pk1)");

                // un detalle adicional de todos
                Execute(db, "create table dgx (Pk1 int, primary key(Pk1), Fk1 int)");
                Execute(db, "alter table dgx add constraint FK_DGX_DGD foreign key(Fk1) references dgd(pk1)");
            }

            if (recursive)
            {
                Execute(db, "alter table dg0 add constraint FK_DG0_DG0 foreign key(Fk2) references dg0(pk1)");
            }

            if (shortCycle)
            {
                Execute(db, "alter table dgm add constraint FK_DGM_DG0 foreign key(Fk1) references dg0(pk1)");
            }

            if (longCycle)
            {
                Execute(db, "alter table dg2 add constraint FK_DG2_DGd foreign key(Fk1) references dgd(pk1)");
            }

            db.Dispose();
        }

        // modelos progresivamente mas largos, empezando por tres tablas alineadas,
        // luego diamante y luego prolongacion del diamante
        [Test]
        public virtual void TestTablesLinearModel()
        {
            this.CreateTableShapes(false, false, false, false, false);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);

            // todas las permutaciones
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dg0", "dgd" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dg0", "dgm", "dgd" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dgd", "dg0" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgd", "dg0", "dgm" }));
        }

        [Test]
        public virtual void TestTablesDiamondModel()
        {
            this.CreateTableShapes(true, false, false, false, false);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);

            // orden directo e inverso
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dg1, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dg0", "dg1", "dgd" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dg1, dgd]", OrderTablesAsString(sr, new string[] { "dgd", "dg1", "dg0", "dgm" }));

            // intercambia extremos y en orden inverso
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dg1, dgd]", OrderTablesAsString(sr, new string[] { "dg0", "dgm", "dgd", "dg1" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg1, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dg1", "dgd", "dgm", "dg0" }));

            // dg1 dg0 pueden estar en cualquier orden
            // intercambios internos que estan al mismo nivel
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg1, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dg1", "dg0", "dgd" }));
        }

        [Test]
        public virtual void TestTablesLongModel()
        {
            this.CreateTableShapes(true, true, false, false, false);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);

            // todo ordenado
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg2, dg3, dgm, dg0, dg1, dgd, dgx]", OrderTablesAsString(sr, new string[] { "dg2", "dg3", "dgm", "dg0", "dg1", "dgd", "dgx" }));

            // orden al reves, no igual que anterior, pero tambien de detalle a maestro (dg1
            // y dg0 son intercambialbes, dg3 no tiene detalles)
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg2, dgm, dg0, dg1, dgd, dgx, dg3]", OrderTablesAsString(sr, new string[] { "dgx", "dgd", "dg1", "dg0", "dgm", "dg3", "dg2" }));

            // ordenado pero con extremos en el medio (otro orden en dg3 dgm pero tambien
            // valido)
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg2, dgm, dg3, dg0, dg1, dgd, dgx]", OrderTablesAsString(sr, new string[] { "dgm", "dg2", "dg3", "dg0", "dg1", "dgx", "dgd" }));
        }

        // Ciclos y recursividad, empezando por recursividad, ciclo corto en dos tablas,
        // y ciclo mas largo
        // Utiliza los mismos esquemas que los tests anteriores anyadiendo los ciclos
        // Para que funcionen bien los ciclos habria que proporcionar informacion sobre
        // las relaciones que rompen estos ciclos
        [Test]
        public virtual void TestTablesLinearModelWithRecursive()
        {
            this.CreateTableShapes(false, false, true, false, false);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dg0", "dgd" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dg0", "dgm", "dgd" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dgd", "dg0" }));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgd", "dg0", "dgm" }));
        }

        [Test]
        public virtual void TestTablesLinearModelWithShortCycle()
        {
            this.CreateTableShapes(false, false, false, true, false);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);

            // Como hay un ciclo causa excepcion al detectarlo si no se configura la
            // constraint a excluir en la busqueda
            try
            {
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg0, dgm, dgd]", OrderTablesAsString(sr, new string[] { "dg0", "dgm", "dgd" }));
                NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
            }
            catch (SchemaException e)
            {
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("Too many recusive levels when trying to sort tables", e.Message);
            }


            // excluyendo la constraint que marca el ciclo se comporta como en el grupo de
            // pruebas iniciales sin ciclos
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dg0", "dgd" }, "FK_dgm_dg0"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dg0", "dgm", "dgd" }, "FK_dgm_dg0"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgm", "dgd", "dg0" }, "FK_dgm_dg0"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd]", OrderTablesAsString(sr, new string[] { "dgd", "dg0", "dgm" }, "FK_dgm_dg0"));
        }

        [Test]
        public virtual void TestTablesLongModelWithLongCycle()
        {
            this.CreateTableShapes(true, true, false, false, true);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);

            // Como hay un ciclo causa excepcion al detectarlo si no se configura la
            // constraint a excluir en la busqueda
            try
            {
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg2, dgd, dgm, dg0, dg1, dgx, dg3]", OrderTablesAsString(sr, new string[] { "dgx", "dgd", "dg1", "dg0", "dgm", "dg3", "dg2" }));
                NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
            }
            catch (SchemaException e)
            {
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("Too many recusive levels when trying to sort tables", e.Message);
            }


            // excluyendo la realcion circular, aunque hay diferentes soluciones, difieren
            // en donde se coloca dg3
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg2, dg3, dgm, dg0, dg1, dgd, dgx]", OrderTablesAsString(sr, new string[] { "dg2", "dg3", "dgm", "dg0", "dg1", "dgd", "dgx" }, "FK_dg2_dgd"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg2, dgm, dg0, dg1, dgd, dgx, dg3]", OrderTablesAsString(sr, new string[] { "dgx", "dgd", "dg1", "dg0", "dgm", "dg3", "dg2" }, "FK_dg2_dgd"));
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dg2, dgm, dg3, dg0, dg1, dgd, dgx]", OrderTablesAsString(sr, new string[] { "dgm", "dg2", "dg3", "dg0", "dg1", "dgx", "dgd" }, "FK_dg2_dgd"));
        }

        // Otras situaciones
        [Test]
        public virtual void TestTableNotInSchema()
        {
            this.CreateTableShapes(false, false, false, false, false);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
            try
            {
                OrderTablesAsString(sr, new string[] { "dgm", "xxx", "dgd" });
                NUnit.Framework.Legacy.ClassicAssert.Fail("Deberia producirse una excepcion");
            }
            catch (SchemaException e)
            {
                NUnit.Framework.Legacy.ClassicAssert.AreEqual("SchemaReaderJdbc.setTableType: Can't find table or view: xxx", e.Message);
            }
        }

        [Test]
        public virtual void TestTableIsSubsetOfSchema()
        {

            // Carga el long model, pero solo pide ordenar parte de las tablas:
            // quitara tablas directamente referenciadas (dg1, dg2) y otras no referenciadas
            // (dg3), ninguna debe salir
            this.CreateTableShapes(true, true, false, false, false);
            SchemaReaderJdbc sr = new SchemaReaderJdbc(dbt);
            NUnit.Framework.Legacy.ClassicAssert.AreEqual("[dgm, dg0, dgd, dgx]", OrderTablesAsString(sr, new string[] { "dgx", "dgd", "dg0", "dgm" }));
        }

        // Metodos de utilidad para obtener las tablas ordenadas
        protected virtual string OrderTablesAsString(SchemaReaderJdbc sr, string[] tables)
        {
            return OrderTablesAsString(sr, tables, "");
        }

        protected virtual string OrderTablesAsString(SchemaReaderJdbc sr, string[] tables, string checkToExclude)
        {
            sr.SetUseCache(true);
            IList<string> allTables = new List<string>();
            for (int i = 0; i < tables.Length; i++)
                allTables.Add(tables[i]);
            SchemaSorter ss = new SchemaSorter(sr);
            if (!"".Equals(checkToExclude))
                ss.NoFollowConstraint(checkToExclude);
            IList<string> orderedTables = ss.Sort(allTables);
            return JavaCs.DeepToString(JavaCs.ToArray(orderedTables)).ToLower();
        }
    }
}