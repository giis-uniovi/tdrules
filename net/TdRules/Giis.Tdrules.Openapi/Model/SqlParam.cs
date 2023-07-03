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
    /// Represents a query parameter and its value
    /// </summary>
    [DataContract(Name = "SqlParam")]
    public partial class SqlParam : IEquatable<SqlParam>, IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="SqlParam" /> class.
        /// </summary>
        /// <param name="name">the name of the parameter (default to &quot;&quot;).</param>
        /// <param name="value">the value that is assigned to the parameter (default to &quot;&quot;).</param>
        public SqlParam(string name = @"", string value = @"")
        {
            // use default value if no "name" provided
            this.Name = name ?? @"";
            // use default value if no "value" provided
            this.Value = value ?? @"";
        }

        /// <summary>
        /// the name of the parameter
        /// </summary>
        /// <value>the name of the parameter</value>
        [DataMember(Name = "name", EmitDefaultValue = false)]
        public string Name { get; set; }
        public string GetName() { return Name; }
        public void SetName(string value) { Name=value; }

        /// <summary>
        /// the value that is assigned to the parameter
        /// </summary>
        /// <value>the value that is assigned to the parameter</value>
        [DataMember(Name = "value", EmitDefaultValue = false)]
        public string Value { get; set; }
        public string GetValue() { return Value; }
        public void SetValue(string value) { Value=value; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class SqlParam {\n");
            sb.Append("  Name: ").Append(Name).Append("\n");
            sb.Append("  Value: ").Append(Value).Append("\n");
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
            return this.Equals(input as SqlParam);
        }

        /// <summary>
        /// Returns true if SqlParam instances are equal
        /// </summary>
        /// <param name="input">Instance of SqlParam to be compared</param>
        /// <returns>Boolean</returns>
        public bool Equals(SqlParam input)
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
                    this.Value == input.Value ||
                    (this.Value != null &&
                    this.Value.Equals(input.Value))
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
                if (this.Value != null)
                {
                    hashCode = (hashCode * 59) + this.Value.GetHashCode();
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
