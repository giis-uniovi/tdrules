/*
 * SQLRules API - Evaluation of test coverage for SQL database queries
 *
 * A set of services to evaluate the coverage of SQL database queries. Coverage criteria are implemented in a set of rules, that when evaluated with respect to a given database determine the coverage of the database with respect to the query. Two kind of coverage rules are generated, Full Predicate Coverage rules for SQL (SQLFpc) and Mutants for SQL (SQLMutation)
 *
 * The version of the OpenAPI document: 3.1.0
 * Generated by: https://github.com/openapitools/openapi-generator.git
 */


using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.IO;
using System.Runtime.Serialization;
using System.Text;
using System.Text.RegularExpressions;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;
using System.ComponentModel.DataAnnotations;
using OpenAPIDateConverter = Giis.Tdrules.Openapi.Client.OpenAPIDateConverter;

namespace Giis.Tdrules.Openapi.Model
{
    /// <summary>
    /// Represents the schema of the database. The xml or json for the current database can be extracted using a jdbc connection with the [XDBSchema application](https://in2test.lsi.uniovi.es/sqltools/xdbschema)
    /// </summary>
    [DataContract(Name = "DbSchema")]
    public partial class DbSchema : IEquatable<DbSchema>, IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="DbSchema" /> class.
        /// </summary>
        /// <param name="catalog">The name of the database catalog as returned by jdbc (default to &quot;&quot;).</param>
        /// <param name="schema">The name of the database schema as returned by jdbc (default to &quot;&quot;).</param>
        /// <param name="dbms">A string representing the database vendor name returned by jdbc, eg. &#x60;postgres&#x60;, &#x60;oracle&#x60;, &#x60;sqlserver&#x60;. To be used by applications to handle vendor specific database features (default to &quot;&quot;).</param>
        /// <param name="tables">The set of tables included in this schema.</param>
        public DbSchema(string catalog = @"", string schema = @"", string dbms = @"", List<DbTable> tables = default(List<DbTable>))
        {
            // use default value if no "catalog" provided
            this.Catalog = catalog ?? @"";
            // use default value if no "schema" provided
            this.Schema = schema ?? @"";
            // use default value if no "dbms" provided
            this.Dbms = dbms ?? @"";
            this.Tables = tables;
        }

        /// <summary>
        /// The name of the database catalog as returned by jdbc
        /// </summary>
        /// <value>The name of the database catalog as returned by jdbc</value>
        [DataMember(Name = "catalog", EmitDefaultValue = false)]
        public string Catalog { get; set; }
        public string GetCatalog() { return Catalog; }
        public void SetCatalog(string value) { Catalog=value; }

        /// <summary>
        /// The name of the database schema as returned by jdbc
        /// </summary>
        /// <value>The name of the database schema as returned by jdbc</value>
        [DataMember(Name = "schema", EmitDefaultValue = false)]
        public string Schema { get; set; }
        public string GetSchema() { return Schema; }
        public void SetSchema(string value) { Schema=value; }

        /// <summary>
        /// A string representing the database vendor name returned by jdbc, eg. &#x60;postgres&#x60;, &#x60;oracle&#x60;, &#x60;sqlserver&#x60;. To be used by applications to handle vendor specific database features
        /// </summary>
        /// <value>A string representing the database vendor name returned by jdbc, eg. &#x60;postgres&#x60;, &#x60;oracle&#x60;, &#x60;sqlserver&#x60;. To be used by applications to handle vendor specific database features</value>
        [DataMember(Name = "dbms", EmitDefaultValue = false)]
        public string Dbms { get; set; }
        public string GetDbms() { return Dbms; }
        public void SetDbms(string value) { Dbms=value; }

        /// <summary>
        /// The set of tables included in this schema
        /// </summary>
        /// <value>The set of tables included in this schema</value>
        [DataMember(Name = "tables", EmitDefaultValue = false)]
        public List<DbTable> Tables { get; set; }
        public List<DbTable> GetTables() { return Tables; }
        public void SetTables(List<DbTable> value) { Tables=value; }
        public void AddTablesItem(DbTable item) { if (this.Tables == null) this.Tables = new List<DbTable>(); this.Tables.Add(item); }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class DbSchema {\n");
            sb.Append("  Catalog: ").Append(Catalog).Append("\n");
            sb.Append("  Schema: ").Append(Schema).Append("\n");
            sb.Append("  Dbms: ").Append(Dbms).Append("\n");
            sb.Append("  Tables: ").Append(Tables).Append("\n");
            sb.Append("}\n");
            return sb.ToString();
        }

        /// <summary>
        /// Returns the JSON string presentation of the object
        /// </summary>
        /// <returns>JSON string presentation of the object</returns>
        public virtual string ToJson()
        {
            return Newtonsoft.Json.JsonConvert.SerializeObject(this, Newtonsoft.Json.Formatting.Indented);
        }

        /// <summary>
        /// Returns true if objects are equal
        /// </summary>
        /// <param name="input">Object to be compared</param>
        /// <returns>Boolean</returns>
        public override bool Equals(object input)
        {
            return this.Equals(input as DbSchema);
        }

        /// <summary>
        /// Returns true if DbSchema instances are equal
        /// </summary>
        /// <param name="input">Instance of DbSchema to be compared</param>
        /// <returns>Boolean</returns>
        public bool Equals(DbSchema input)
        {
            if (input == null)
            {
                return false;
            }
            return 
                (
                    this.Catalog == input.Catalog ||
                    (this.Catalog != null &&
                    this.Catalog.Equals(input.Catalog))
                ) && 
                (
                    this.Schema == input.Schema ||
                    (this.Schema != null &&
                    this.Schema.Equals(input.Schema))
                ) && 
                (
                    this.Dbms == input.Dbms ||
                    (this.Dbms != null &&
                    this.Dbms.Equals(input.Dbms))
                ) && 
                (
                    this.Tables == input.Tables ||
                    this.Tables != null &&
                    input.Tables != null &&
                    this.Tables.SequenceEqual(input.Tables)
                );
        }

        /// <summary>
        /// Gets the hash code
        /// </summary>
        /// <returns>Hash code</returns>
        public override int GetHashCode()
        {
            unchecked // Overflow is fine, just wrap
            {
                int hashCode = 41;
                if (this.Catalog != null)
                {
                    hashCode = (hashCode * 59) + this.Catalog.GetHashCode();
                }
                if (this.Schema != null)
                {
                    hashCode = (hashCode * 59) + this.Schema.GetHashCode();
                }
                if (this.Dbms != null)
                {
                    hashCode = (hashCode * 59) + this.Dbms.GetHashCode();
                }
                if (this.Tables != null)
                {
                    hashCode = (hashCode * 59) + this.Tables.GetHashCode();
                }
                return hashCode;
            }
        }

        /// <summary>
        /// To validate all properties of the instance
        /// </summary>
        /// <param name="validationContext">Validation context</param>
        /// <returns>Validation Result</returns>
        IEnumerable<System.ComponentModel.DataAnnotations.ValidationResult> IValidatableObject.Validate(ValidationContext validationContext)
        {
            yield break;
        }
    }

}