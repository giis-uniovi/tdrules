using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;

namespace OpenApiPostprocess
{
    /**
     * Anyade setters y getters a las propiedades de los modelos
     * para compatibilidad con el codigo generado desde java
     * Ejecutar desde la carpeta de la solucion con;
     * dotnet run --project OpenApiPostprocess/OpenApiPostprocess.csproj
     */
    class Program
    {
        private static string path = "";
        static void Main(string[] args)
        {
            //requiere primer argumento qeu indica el path
            path = args[0];
            System.Console.WriteLine("Processing openapi generted model at: " + path);
            Program prog = new Program();
            prog.processFile("TdCheck.cs");
            prog.processFile("TdAttribute.cs");
            prog.processFile("TdSchema.cs");
            prog.processFile("TdEntity.cs");
            prog.processFile("Ddl.cs");
            prog.processFile("QueryParam.cs");
            prog.processFile("QueryParametersBody.cs");
            prog.processFile("TdRules.cs");
            prog.processFile("TdRule.cs");
            prog.processFile("TdRulesBody.cs");
            prog.processFile("QueryEntitiesBody.cs");
            prog.processFile("VersionBody.cs");
        }

        public void processFile(string filename)
        {
            string file = Path.Combine(path + "/" + filename);
            System.Console.WriteLine("Add setters and getters: " + file);
            string[] source = File.ReadAllLines(file);
            IList<string> dest = new List<string>();
            foreach (string line in source)
            {
                dest.Add(line);
                //parse to search pattern:         public string Cotype { get; set; }
                //if dictionary, type contains <string, string>, remove the space before split
                string[] comp = line.Trim().Replace("<string, string>", "<string,string>").Split(" ");
                if (comp.Length>6 && comp[0]== "public" && comp[3] == "{" && comp[4] == "get;" && comp[5] == "set;" && comp[6] == "}")
                {
                    string type = comp[1];
                    string name = comp[2];
                    string getter = "        public " + type + " Get" + SetterName(name) + "() { return " + name + "; }";
                    string setter = "        public void Set" + SetterName(name) + "(" +type+" value) { "+name+"=value; }";
                    //Swagger no tiene en cuenta los valores por defecto en netcore!!!
                    //pero con openapi generator no es necesario
                    //lo pone para todos los strings pues es el convenio general usado
                    //if (type == "string") 
                    //    dest.Add("        = \"\";");
                    dest.Add(getter);
                    dest.Add(setter);
                    //System.Console.WriteLine(getter);
                    //System.Console.WriteLine(setter);
                    //Genera metodo para anyadir items (por compatibilidad con codigo traducido de java)
                    //public void AddColumnsItem(Column item) { if (this.Columns == null) this.Columns = new List<Column>(); this.Columns.Add(item); }
                    if (type.StartsWith("List<")) 
                    {
                        string itemtype = type.Replace("List<", "").Replace(">", "");
                        string adder = "        public void Add" + SetterName(name) + "Item(" + itemtype + " item) {"
                            + " if (this." + name + " == null) this." + name+ " = new " + type + "();"
                            + " this." + name + ".Add(item); }";
                        dest.Add(adder);
                        //System.Console.WriteLine(adder);
                    }
                    else if (type.Contains("Dictionary"))
                    {
                        string adder = "        public void Put" + SetterName(name) + "Item(string key, string summaryItem) {"
                            + " if (this." + name + " == null) this." + name + " = new " + type + "();"
                            + " this." + name + "[key]=summaryItem; }";
                        dest.Add(adder);
                        //System.Console.WriteLine(adder);
                    }
                }
            }
            WriteAllLines(file, dest);
        }
        private string SetterName(string name)
        {
            //las palabras reservadas originan propiedades con _ delante, quita este del nombre cuando genera setters y getters
            if (name.StartsWith("_"))
                name = name.Substring(1, name.Length-1);
            return name;
        }
        // File.WriteAllLines sets LF endings even on windows, we need the system ending to push the changes only
        private void WriteAllLines(string file, IList<string> dest) {
            StringWriter sw = new StringWriter();
            for (int i = 0; i < dest.Count; i++)
            {
                //if (i != 0)
                //    sw.Write(Environment.NewLine);
                sw.Write(dest[i] + Environment.NewLine);
            }
            File.WriteAllText(file, sw.ToString());
        }
    }
}
