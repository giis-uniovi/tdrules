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
    /// Contains information about the service versions
    /// </summary>
    [DataContract(Name = "VersionBody")]
    public partial class VersionBody : IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="VersionBody" /> class.
        /// </summary>
        /// <param name="serviceVersion">The version number of the running service (default to &quot;&quot;).</param>
        /// <param name="apiVersion">The version number of the api implemented by the service (default to &quot;&quot;).</param>
        /// <param name="varEnvironment">The environment where the service is executing (default to &quot;&quot;).</param>
        public VersionBody(string serviceVersion = @"", string apiVersion = @"", string varEnvironment = @"")
        {
            // use default value if no "serviceVersion" provided
            this.ServiceVersion = serviceVersion ?? @"";
            // use default value if no "apiVersion" provided
            this.ApiVersion = apiVersion ?? @"";
            // use default value if no "varEnvironment" provided
            this.VarEnvironment = varEnvironment ?? @"";
        }

        /// <summary>
        /// The version number of the running service
        /// </summary>
        /// <value>The version number of the running service</value>
        [DataMember(Name = "serviceVersion", EmitDefaultValue = false)]
        public string ServiceVersion { get; set; }
        public string GetServiceVersion() { return ServiceVersion; }
        public void SetServiceVersion(string value) { ServiceVersion=value; }

        /// <summary>
        /// The version number of the api implemented by the service
        /// </summary>
        /// <value>The version number of the api implemented by the service</value>
        [DataMember(Name = "apiVersion", EmitDefaultValue = false)]
        public string ApiVersion { get; set; }
        public string GetApiVersion() { return ApiVersion; }
        public void SetApiVersion(string value) { ApiVersion=value; }

        /// <summary>
        /// The environment where the service is executing
        /// </summary>
        /// <value>The environment where the service is executing</value>
        [DataMember(Name = "environment", EmitDefaultValue = false)]
        public string VarEnvironment { get; set; }
        public string GetEnvironment() { return VarEnvironment; }
        public void SetEnvironment(string value) { VarEnvironment=value; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class VersionBody {\n");
            sb.Append("  ServiceVersion: ").Append(ServiceVersion).Append("\n");
            sb.Append("  ApiVersion: ").Append(ApiVersion).Append("\n");
            sb.Append("  VarEnvironment: ").Append(VarEnvironment).Append("\n");
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
        IEnumerable<ValidationResult> IValidatableObject.Validate(ValidationContext validationContext)
        {
            yield break;
        }
    }

}
