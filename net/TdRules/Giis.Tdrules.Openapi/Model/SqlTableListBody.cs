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
    /// Represents a list of tables that are used in a query
    /// </summary>
    [DataContract(Name = "SqlTableListBody")]
    public partial class SqlTableListBody : IEquatable<SqlTableListBody>, IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="SqlTableListBody" /> class.
        /// </summary>
        /// <param name="sql">The sql that contains this list of tables (default to &quot;&quot;).</param>
        /// <param name="error">If empty, the service successfully obtained this object, if not, indicates the error occurred (default to &quot;&quot;).</param>
        /// <param name="tables">The list of tables used by this query.</param>
        public SqlTableListBody(string sql = @"", string error = @"", List<string> tables = default(List<string>))
        {
            // use default value if no "sql" provided
            this.Sql = sql ?? @"";
            // use default value if no "error" provided
            this.Error = error ?? @"";
            this.Tables = tables;
        }

        /// <summary>
        /// The sql that contains this list of tables
        /// </summary>
        /// <value>The sql that contains this list of tables</value>
        [DataMember(Name = "sql", EmitDefaultValue = false)]
        public string Sql { get; set; }
        public string GetSql() { return Sql; }
        public void SetSql(string value) { Sql=value; }

        /// <summary>
        /// If empty, the service successfully obtained this object, if not, indicates the error occurred
        /// </summary>
        /// <value>If empty, the service successfully obtained this object, if not, indicates the error occurred</value>
        [DataMember(Name = "error", EmitDefaultValue = false)]
        public string Error { get; set; }
        public string GetError() { return Error; }
        public void SetError(string value) { Error=value; }

        /// <summary>
        /// The list of tables used by this query
        /// </summary>
        /// <value>The list of tables used by this query</value>
        [DataMember(Name = "tables", EmitDefaultValue = false)]
        public List<string> Tables { get; set; }
        public List<string> GetTables() { return Tables; }
        public void SetTables(List<string> value) { Tables=value; }
        public void AddTablesItem(string item) { if (this.Tables == null) this.Tables = new List<string>(); this.Tables.Add(item); }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class SqlTableListBody {\n");
            sb.Append("  Sql: ").Append(Sql).Append("\n");
            sb.Append("  Error: ").Append(Error).Append("\n");
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
            return this.Equals(input as SqlTableListBody);
        }

        /// <summary>
        /// Returns true if SqlTableListBody instances are equal
        /// </summary>
        /// <param name="input">Instance of SqlTableListBody to be compared</param>
        /// <returns>Boolean</returns>
        public bool Equals(SqlTableListBody input)
        {
            if (input == null)
            {
                return false;
            }
            return 
                (
                    this.Sql == input.Sql ||
                    (this.Sql != null &&
                    this.Sql.Equals(input.Sql))
                ) && 
                (
                    this.Error == input.Error ||
                    (this.Error != null &&
                    this.Error.Equals(input.Error))
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
                if (this.Sql != null)
                {
                    hashCode = (hashCode * 59) + this.Sql.GetHashCode();
                }
                if (this.Error != null)
                {
                    hashCode = (hashCode * 59) + this.Error.GetHashCode();
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
