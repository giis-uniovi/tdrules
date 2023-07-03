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
    /// Represents a table in the database. To generate sql coverage rules, at least &#x60;name&#x60; must be specified
    /// </summary>
    [DataContract(Name = "DbTable")]
    public partial class DbTable : IEquatable<DbTable>, IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="DbTable" /> class.
        /// </summary>
        /// <param name="name">The name of this table (default to &quot;&quot;).</param>
        /// <param name="tabletype">The type of this table as returned by jdbc, eg. &#x60;table&#x60;,&#x60;view&#x60;, &#x60;type&#x60;, NOTE, &#x60;type&#x60; tables hold User Defined Types (UDT), also called object, record or row (as named in SQL99) (default to &quot;&quot;).</param>
        /// <param name="extended">A map of additional table properties to store information required by other applications.</param>
        /// <param name="columns">The set of columns in this table.</param>
        /// <param name="checks">The set of check constraints associated to the columns of this table.</param>
        /// <param name="ddls">Sql statements (dml) to create and/or drop this table Used by applications that require manage the table included in this schema.</param>
        public DbTable(string name = @"", string tabletype = @"", Dictionary<string, string> extended = default(Dictionary<string, string>), List<DbColumn> columns = default(List<DbColumn>), List<DbCheck> checks = default(List<DbCheck>), List<Ddl> ddls = default(List<Ddl>))
        {
            // use default value if no "name" provided
            this.Name = name ?? @"";
            // use default value if no "tabletype" provided
            this.Tabletype = tabletype ?? @"";
            this.Extended = extended;
            this.Columns = columns;
            this.Checks = checks;
            this.Ddls = ddls;
        }

        /// <summary>
        /// The name of this table
        /// </summary>
        /// <value>The name of this table</value>
        [DataMember(Name = "name", EmitDefaultValue = false)]
        public string Name { get; set; }
        public string GetName() { return Name; }
        public void SetName(string value) { Name=value; }

        /// <summary>
        /// The type of this table as returned by jdbc, eg. &#x60;table&#x60;,&#x60;view&#x60;, &#x60;type&#x60;, NOTE, &#x60;type&#x60; tables hold User Defined Types (UDT), also called object, record or row (as named in SQL99)
        /// </summary>
        /// <value>The type of this table as returned by jdbc, eg. &#x60;table&#x60;,&#x60;view&#x60;, &#x60;type&#x60;, NOTE, &#x60;type&#x60; tables hold User Defined Types (UDT), also called object, record or row (as named in SQL99)</value>
        [DataMember(Name = "tabletype", EmitDefaultValue = false)]
        public string Tabletype { get; set; }
        public string GetTabletype() { return Tabletype; }
        public void SetTabletype(string value) { Tabletype=value; }

        /// <summary>
        /// A map of additional table properties to store information required by other applications
        /// </summary>
        /// <value>A map of additional table properties to store information required by other applications</value>
        [DataMember(Name = "extended", EmitDefaultValue = false)]
        public Dictionary<string, string> Extended { get; set; }
        public Dictionary<string,string> GetExtended() { return Extended; }
        public void SetExtended(Dictionary<string,string> value) { Extended=value; }
        public void PutExtendedItem(string key, string summaryItem) { if (this.Extended == null) this.Extended = new Dictionary<string,string>(); this.Extended[key]=summaryItem; }

        /// <summary>
        /// The set of columns in this table
        /// </summary>
        /// <value>The set of columns in this table</value>
        [DataMember(Name = "columns", EmitDefaultValue = false)]
        public List<DbColumn> Columns { get; set; }
        public List<DbColumn> GetColumns() { return Columns; }
        public void SetColumns(List<DbColumn> value) { Columns=value; }
        public void AddColumnsItem(DbColumn item) { if (this.Columns == null) this.Columns = new List<DbColumn>(); this.Columns.Add(item); }

        /// <summary>
        /// The set of check constraints associated to the columns of this table
        /// </summary>
        /// <value>The set of check constraints associated to the columns of this table</value>
        [DataMember(Name = "checks", EmitDefaultValue = false)]
        public List<DbCheck> Checks { get; set; }
        public List<DbCheck> GetChecks() { return Checks; }
        public void SetChecks(List<DbCheck> value) { Checks=value; }
        public void AddChecksItem(DbCheck item) { if (this.Checks == null) this.Checks = new List<DbCheck>(); this.Checks.Add(item); }

        /// <summary>
        /// Sql statements (dml) to create and/or drop this table Used by applications that require manage the table included in this schema
        /// </summary>
        /// <value>Sql statements (dml) to create and/or drop this table Used by applications that require manage the table included in this schema</value>
        [DataMember(Name = "ddls", EmitDefaultValue = false)]
        public List<Ddl> Ddls { get; set; }
        public List<Ddl> GetDdls() { return Ddls; }
        public void SetDdls(List<Ddl> value) { Ddls=value; }
        public void AddDdlsItem(Ddl item) { if (this.Ddls == null) this.Ddls = new List<Ddl>(); this.Ddls.Add(item); }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class DbTable {\n");
            sb.Append("  Name: ").Append(Name).Append("\n");
            sb.Append("  Tabletype: ").Append(Tabletype).Append("\n");
            sb.Append("  Extended: ").Append(Extended).Append("\n");
            sb.Append("  Columns: ").Append(Columns).Append("\n");
            sb.Append("  Checks: ").Append(Checks).Append("\n");
            sb.Append("  Ddls: ").Append(Ddls).Append("\n");
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
            return this.Equals(input as DbTable);
        }

        /// <summary>
        /// Returns true if DbTable instances are equal
        /// </summary>
        /// <param name="input">Instance of DbTable to be compared</param>
        /// <returns>Boolean</returns>
        public bool Equals(DbTable input)
        {
            if (input == null)
            {
                return false;
            }
            return 
                (
                    this.Name == input.Name ||
                    (this.Name != null &&
                    this.Name.Equals(input.Name))
                ) && 
                (
                    this.Tabletype == input.Tabletype ||
                    (this.Tabletype != null &&
                    this.Tabletype.Equals(input.Tabletype))
                ) && 
                (
                    this.Extended == input.Extended ||
                    this.Extended != null &&
                    input.Extended != null &&
                    this.Extended.SequenceEqual(input.Extended)
                ) && 
                (
                    this.Columns == input.Columns ||
                    this.Columns != null &&
                    input.Columns != null &&
                    this.Columns.SequenceEqual(input.Columns)
                ) && 
                (
                    this.Checks == input.Checks ||
                    this.Checks != null &&
                    input.Checks != null &&
                    this.Checks.SequenceEqual(input.Checks)
                ) && 
                (
                    this.Ddls == input.Ddls ||
                    this.Ddls != null &&
                    input.Ddls != null &&
                    this.Ddls.SequenceEqual(input.Ddls)
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
                if (this.Name != null)
                {
                    hashCode = (hashCode * 59) + this.Name.GetHashCode();
                }
                if (this.Tabletype != null)
                {
                    hashCode = (hashCode * 59) + this.Tabletype.GetHashCode();
                }
                if (this.Extended != null)
                {
                    hashCode = (hashCode * 59) + this.Extended.GetHashCode();
                }
                if (this.Columns != null)
                {
                    hashCode = (hashCode * 59) + this.Columns.GetHashCode();
                }
                if (this.Checks != null)
                {
                    hashCode = (hashCode * 59) + this.Checks.GetHashCode();
                }
                if (this.Ddls != null)
                {
                    hashCode = (hashCode * 59) + this.Ddls.GetHashCode();
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
