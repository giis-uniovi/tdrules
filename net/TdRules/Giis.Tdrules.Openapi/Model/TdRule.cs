/*
 * TdRules API - Test Data Coverage Evaluation
 *
 * A set of services to evaluate the coverage of test data.  Coverage criteria are implemented in a set of rules, that when evaluated with respect to a given data store determine the coverage of the data store with respect to the query. Two kind of coverage rules are generated, Full Predicate Coverage (FPC) Rules and SQL Mutants. 
 *
 * The version of the OpenAPI document: 4.0.2
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
    /// Represents a single coverage rule. In RDB, this query can be executed against the database to determine if it is covered
    /// </summary>
    [DataContract(Name = "TdRule")]
    public partial class TdRule : IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="TdRule" /> class.
        /// </summary>
        /// <param name="summary">A map of additional properties to store information about the evaluation of the rule.</param>
        /// <param name="id">Unique identifier of this rule in a set of rules (default to &quot;&quot;).</param>
        /// <param name="category">Top level classification of this rule (default to &quot;&quot;).</param>
        /// <param name="maintype">Second level classification of this rule (default to &quot;&quot;).</param>
        /// <param name="subtype">Third level classification of this rule (default to &quot;&quot;).</param>
        /// <param name="location">Identification about the place of the query that has been considered to generate the rule (default to &quot;&quot;).</param>
        /// <param name="equivalent">Only for mutants, indicates if this is an equivalent mutant (default to &quot;&quot;).</param>
        /// <param name="query">The query expression that describes this rule (default to &quot;&quot;).</param>
        /// <param name="description">An human readable textual description of what this rule represents (default to &quot;&quot;).</param>
        /// <param name="error">This field can be used to store runtime errors when generating or evaluating this rule (default to &quot;&quot;).</param>
        public TdRule(Dictionary<string, string> summary = default(Dictionary<string, string>), string id = @"", string category = @"", string maintype = @"", string subtype = @"", string location = @"", string equivalent = @"", string query = @"", string description = @"", string error = @"")
        {
            this.Summary = summary;
            // use default value if no "id" provided
            this.Id = id ?? @"";
            // use default value if no "category" provided
            this.Category = category ?? @"";
            // use default value if no "maintype" provided
            this.Maintype = maintype ?? @"";
            // use default value if no "subtype" provided
            this.Subtype = subtype ?? @"";
            // use default value if no "location" provided
            this.Location = location ?? @"";
            // use default value if no "equivalent" provided
            this.Equivalent = equivalent ?? @"";
            // use default value if no "query" provided
            this.Query = query ?? @"";
            // use default value if no "description" provided
            this.Description = description ?? @"";
            // use default value if no "error" provided
            this.Error = error ?? @"";
        }

        /// <summary>
        /// A map of additional properties to store information about the evaluation of the rule
        /// </summary>
        /// <value>A map of additional properties to store information about the evaluation of the rule</value>
        [DataMember(Name = "summary", EmitDefaultValue = false)]
        public Dictionary<string, string> Summary { get; set; }
        public Dictionary<string,string> GetSummary() { return Summary; }
        public void SetSummary(Dictionary<string,string> value) { Summary=value; }
        public void PutSummaryItem(string key, string summaryItem) { if (this.Summary == null) this.Summary = new Dictionary<string,string>(); this.Summary[key]=summaryItem; }

        /// <summary>
        /// Unique identifier of this rule in a set of rules
        /// </summary>
        /// <value>Unique identifier of this rule in a set of rules</value>
        [DataMember(Name = "id", EmitDefaultValue = false)]
        public string Id { get; set; }
        public string GetId() { return Id; }
        public void SetId(string value) { Id=value; }

        /// <summary>
        /// Top level classification of this rule
        /// </summary>
        /// <value>Top level classification of this rule</value>
        [DataMember(Name = "category", EmitDefaultValue = false)]
        public string Category { get; set; }
        public string GetCategory() { return Category; }
        public void SetCategory(string value) { Category=value; }

        /// <summary>
        /// Second level classification of this rule
        /// </summary>
        /// <value>Second level classification of this rule</value>
        [DataMember(Name = "maintype", EmitDefaultValue = false)]
        public string Maintype { get; set; }
        public string GetMaintype() { return Maintype; }
        public void SetMaintype(string value) { Maintype=value; }

        /// <summary>
        /// Third level classification of this rule
        /// </summary>
        /// <value>Third level classification of this rule</value>
        [DataMember(Name = "subtype", EmitDefaultValue = false)]
        public string Subtype { get; set; }
        public string GetSubtype() { return Subtype; }
        public void SetSubtype(string value) { Subtype=value; }

        /// <summary>
        /// Identification about the place of the query that has been considered to generate the rule
        /// </summary>
        /// <value>Identification about the place of the query that has been considered to generate the rule</value>
        [DataMember(Name = "location", EmitDefaultValue = false)]
        public string Location { get; set; }
        public string GetLocation() { return Location; }
        public void SetLocation(string value) { Location=value; }

        /// <summary>
        /// Only for mutants, indicates if this is an equivalent mutant
        /// </summary>
        /// <value>Only for mutants, indicates if this is an equivalent mutant</value>
        [DataMember(Name = "equivalent", EmitDefaultValue = false)]
        public string Equivalent { get; set; }
        public string GetEquivalent() { return Equivalent; }
        public void SetEquivalent(string value) { Equivalent=value; }

        /// <summary>
        /// The query expression that describes this rule
        /// </summary>
        /// <value>The query expression that describes this rule</value>
        [DataMember(Name = "query", EmitDefaultValue = false)]
        public string Query { get; set; }
        public string GetQuery() { return Query; }
        public void SetQuery(string value) { Query=value; }

        /// <summary>
        /// An human readable textual description of what this rule represents
        /// </summary>
        /// <value>An human readable textual description of what this rule represents</value>
        [DataMember(Name = "description", EmitDefaultValue = false)]
        public string Description { get; set; }
        public string GetDescription() { return Description; }
        public void SetDescription(string value) { Description=value; }

        /// <summary>
        /// This field can be used to store runtime errors when generating or evaluating this rule
        /// </summary>
        /// <value>This field can be used to store runtime errors when generating or evaluating this rule</value>
        [DataMember(Name = "error", EmitDefaultValue = false)]
        public string Error { get; set; }
        public string GetError() { return Error; }
        public void SetError(string value) { Error=value; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class TdRule {\n");
            sb.Append("  Summary: ").Append(Summary).Append("\n");
            sb.Append("  Id: ").Append(Id).Append("\n");
            sb.Append("  Category: ").Append(Category).Append("\n");
            sb.Append("  Maintype: ").Append(Maintype).Append("\n");
            sb.Append("  Subtype: ").Append(Subtype).Append("\n");
            sb.Append("  Location: ").Append(Location).Append("\n");
            sb.Append("  Equivalent: ").Append(Equivalent).Append("\n");
            sb.Append("  Query: ").Append(Query).Append("\n");
            sb.Append("  Description: ").Append(Description).Append("\n");
            sb.Append("  Error: ").Append(Error).Append("\n");
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
