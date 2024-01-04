/*
 * TdRules API - Test Data Coverage Evaluation
 *
 * A set of services to evaluate the coverage of test data.  Coverage criteria are implemented in a set of rules, that when evaluated with respect to a given data store determine the coverage of the data store with respect to the query. Two kind of coverage rules are generated, Full Predicate Coverage (FPC) Rules and SQL Mutants. 
 *
 * The version of the OpenAPI document: 4.0.1
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
    /// Represents a logical expression that constraint the allowed values of attributes in an entity
    /// </summary>
    [DataContract(Name = "TdCheck")]
    public partial class TdCheck : IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="TdCheck" /> class.
        /// </summary>
        [JsonConstructorAttribute]
        protected TdCheck() { }
        /// <summary>
        /// Initializes a new instance of the <see cref="TdCheck" /> class.
        /// </summary>
        /// <param name="attribute">The name of the attribute that constrained (if associated to only one) (default to &quot;&quot;).</param>
        /// <param name="name">An optional name to refer this constraint (default to &quot;&quot;).</param>
        /// <param name="constraint">The data store dependent constraint expression (required) (default to &quot;&quot;).</param>
        public TdCheck(string attribute = @"", string name = @"", string constraint = @"")
        {
            // to ensure "constraint" is required (not null)
            if (constraint == null)
            {
                throw new ArgumentNullException("constraint is a required property for TdCheck and cannot be null");
            }
            this.Constraint = constraint;
            // use default value if no "attribute" provided
            this.Attribute = attribute ?? @"";
            // use default value if no "name" provided
            this.Name = name ?? @"";
        }

        /// <summary>
        /// The name of the attribute that constrained (if associated to only one)
        /// </summary>
        /// <value>The name of the attribute that constrained (if associated to only one)</value>
        [DataMember(Name = "attribute", EmitDefaultValue = false)]
        public string Attribute { get; set; }
        public string GetAttribute() { return Attribute; }
        public void SetAttribute(string value) { Attribute=value; }

        /// <summary>
        /// An optional name to refer this constraint
        /// </summary>
        /// <value>An optional name to refer this constraint</value>
        [DataMember(Name = "name", EmitDefaultValue = false)]
        public string Name { get; set; }
        public string GetName() { return Name; }
        public void SetName(string value) { Name=value; }

        /// <summary>
        /// The data store dependent constraint expression
        /// </summary>
        /// <value>The data store dependent constraint expression</value>
        [DataMember(Name = "constraint", IsRequired = true, EmitDefaultValue = true)]
        public string Constraint { get; set; }
        public string GetConstraint() { return Constraint; }
        public void SetConstraint(string value) { Constraint=value; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class TdCheck {\n");
            sb.Append("  Attribute: ").Append(Attribute).Append("\n");
            sb.Append("  Name: ").Append(Name).Append("\n");
            sb.Append("  Constraint: ").Append(Constraint).Append("\n");
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
