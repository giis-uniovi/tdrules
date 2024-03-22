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
    /// Represents the input data to the coverage rules generator
    /// </summary>
    [DataContract(Name = "TdRulesBody")]
    public partial class TdRulesBody : IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="TdRulesBody" /> class.
        /// </summary>
        /// <param name="query">The query to generate the coverage rules (default to &quot;&quot;).</param>
        /// <param name="schema">schema.</param>
        /// <param name="options">A set of additional options to modify the behaviour of the rule generation (strings separated by space). Allowed values for FPC are documented in https://in2test.lsi.uniovi.es/tdrules/api-doc.html. Some of them are dependent of the kind of rules to be generated. &lt;br/&gt;Example. &#x60;lang&#x3D;en noboundary&#x60; specifies that the generated fpc coverage rules must contain an english description of the rule and rules for checking boundary values must not be generated. (default to &quot;&quot;).</param>
        public TdRulesBody(string query = @"", TdSchema schema = default(TdSchema), string options = @"")
        {
            // use default value if no "query" provided
            this.Query = query ?? @"";
            this.Schema = schema;
            // use default value if no "options" provided
            this.Options = options ?? @"";
        }

        /// <summary>
        /// The query to generate the coverage rules
        /// </summary>
        /// <value>The query to generate the coverage rules</value>
        [DataMember(Name = "query", EmitDefaultValue = false)]
        public string Query { get; set; }
        public string GetQuery() { return Query; }
        public void SetQuery(string value) { Query=value; }

        /// <summary>
        /// Gets or Sets Schema
        /// </summary>
        [DataMember(Name = "schema", EmitDefaultValue = false)]
        public TdSchema Schema { get; set; }
        public TdSchema GetSchema() { return Schema; }
        public void SetSchema(TdSchema value) { Schema=value; }

        /// <summary>
        /// A set of additional options to modify the behaviour of the rule generation (strings separated by space). Allowed values for FPC are documented in https://in2test.lsi.uniovi.es/tdrules/api-doc.html. Some of them are dependent of the kind of rules to be generated. &lt;br/&gt;Example. &#x60;lang&#x3D;en noboundary&#x60; specifies that the generated fpc coverage rules must contain an english description of the rule and rules for checking boundary values must not be generated.
        /// </summary>
        /// <value>A set of additional options to modify the behaviour of the rule generation (strings separated by space). Allowed values for FPC are documented in https://in2test.lsi.uniovi.es/tdrules/api-doc.html. Some of them are dependent of the kind of rules to be generated. &lt;br/&gt;Example. &#x60;lang&#x3D;en noboundary&#x60; specifies that the generated fpc coverage rules must contain an english description of the rule and rules for checking boundary values must not be generated.</value>
        [DataMember(Name = "options", EmitDefaultValue = false)]
        public string Options { get; set; }
        public string GetOptions() { return Options; }
        public void SetOptions(string value) { Options=value; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class TdRulesBody {\n");
            sb.Append("  Query: ").Append(Query).Append("\n");
            sb.Append("  Schema: ").Append(Schema).Append("\n");
            sb.Append("  Options: ").Append(Options).Append("\n");
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
