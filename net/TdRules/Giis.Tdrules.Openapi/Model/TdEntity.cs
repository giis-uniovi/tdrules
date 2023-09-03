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
    /// Represents a entity in the data store. To generate coverage rules, at least &#x60;name&#x60; must be specified
    /// </summary>
    [DataContract(Name = "TdEntity")]
    public partial class TdEntity : IEquatable<TdEntity>, IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="TdEntity" /> class.
        /// </summary>
        /// <param name="name">The name of this entity (default to &quot;&quot;).</param>
        /// <param name="entitytype">The type of this entity: - &#x60;table&#x60;: The entity is meant to store data - &#x60;array&#x60;: The entity represents the data stored by each item in a collection - &#x60;type&#x60;: The entity describes the structure of a composite object (object inside of another object) - &#x60;view&#x60;: The entity is a virtual table to present data stored in other entities  (default to &quot;&quot;).</param>
        /// <param name="subtype">An optional modifier of the entity type (default to &quot;&quot;).</param>
        /// <param name="extended">A map of additional entity properties to store information required by other applications.</param>
        /// <param name="attributes">The collection with the attributes of this entity.</param>
        /// <param name="checks">The set of constraints that the attributes of this entity must satisfy.</param>
        /// <param name="ddls">Store type dependent operations to manipulate the data or the schema in the data store: - In RDB stores, it may include the statements to create or drop tables. - In openapi data stores, it may include the endpoints to post or delete objects. .</param>
        public TdEntity(string name = @"", string entitytype = @"", string subtype = @"", Dictionary<string, string> extended = default(Dictionary<string, string>), List<TdAttribute> attributes = default(List<TdAttribute>), List<TdCheck> checks = default(List<TdCheck>), List<Ddl> ddls = default(List<Ddl>))
        {
            // use default value if no "name" provided
            this.Name = name ?? @"";
            // use default value if no "entitytype" provided
            this.Entitytype = entitytype ?? @"";
            // use default value if no "subtype" provided
            this.Subtype = subtype ?? @"";
            this.Extended = extended;
            this.Attributes = attributes;
            this.Checks = checks;
            this.Ddls = ddls;
        }

        /// <summary>
        /// The name of this entity
        /// </summary>
        /// <value>The name of this entity</value>
        [DataMember(Name = "name", EmitDefaultValue = false)]
        public string Name { get; set; }
        public string GetName() { return Name; }
        public void SetName(string value) { Name=value; }

        /// <summary>
        /// The type of this entity: - &#x60;table&#x60;: The entity is meant to store data - &#x60;array&#x60;: The entity represents the data stored by each item in a collection - &#x60;type&#x60;: The entity describes the structure of a composite object (object inside of another object) - &#x60;view&#x60;: The entity is a virtual table to present data stored in other entities 
        /// </summary>
        /// <value>The type of this entity: - &#x60;table&#x60;: The entity is meant to store data - &#x60;array&#x60;: The entity represents the data stored by each item in a collection - &#x60;type&#x60;: The entity describes the structure of a composite object (object inside of another object) - &#x60;view&#x60;: The entity is a virtual table to present data stored in other entities </value>
        [DataMember(Name = "entitytype", EmitDefaultValue = false)]
        public string Entitytype { get; set; }
        public string GetEntitytype() { return Entitytype; }
        public void SetEntitytype(string value) { Entitytype=value; }

        /// <summary>
        /// An optional modifier of the entity type
        /// </summary>
        /// <value>An optional modifier of the entity type</value>
        [DataMember(Name = "subtype", EmitDefaultValue = false)]
        public string Subtype { get; set; }
        public string GetSubtype() { return Subtype; }
        public void SetSubtype(string value) { Subtype=value; }

        /// <summary>
        /// A map of additional entity properties to store information required by other applications
        /// </summary>
        /// <value>A map of additional entity properties to store information required by other applications</value>
        [DataMember(Name = "extended", EmitDefaultValue = false)]
        public Dictionary<string, string> Extended { get; set; }
        public Dictionary<string,string> GetExtended() { return Extended; }
        public void SetExtended(Dictionary<string,string> value) { Extended=value; }
        public void PutExtendedItem(string key, string summaryItem) { if (this.Extended == null) this.Extended = new Dictionary<string,string>(); this.Extended[key]=summaryItem; }

        /// <summary>
        /// The collection with the attributes of this entity
        /// </summary>
        /// <value>The collection with the attributes of this entity</value>
        [DataMember(Name = "attributes", EmitDefaultValue = false)]
        public List<TdAttribute> Attributes { get; set; }
        public List<TdAttribute> GetAttributes() { return Attributes; }
        public void SetAttributes(List<TdAttribute> value) { Attributes=value; }
        public void AddAttributesItem(TdAttribute item) { if (this.Attributes == null) this.Attributes = new List<TdAttribute>(); this.Attributes.Add(item); }

        /// <summary>
        /// The set of constraints that the attributes of this entity must satisfy
        /// </summary>
        /// <value>The set of constraints that the attributes of this entity must satisfy</value>
        [DataMember(Name = "checks", EmitDefaultValue = false)]
        public List<TdCheck> Checks { get; set; }
        public List<TdCheck> GetChecks() { return Checks; }
        public void SetChecks(List<TdCheck> value) { Checks=value; }
        public void AddChecksItem(TdCheck item) { if (this.Checks == null) this.Checks = new List<TdCheck>(); this.Checks.Add(item); }

        /// <summary>
        /// Store type dependent operations to manipulate the data or the schema in the data store: - In RDB stores, it may include the statements to create or drop tables. - In openapi data stores, it may include the endpoints to post or delete objects. 
        /// </summary>
        /// <value>Store type dependent operations to manipulate the data or the schema in the data store: - In RDB stores, it may include the statements to create or drop tables. - In openapi data stores, it may include the endpoints to post or delete objects. </value>
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
            sb.Append("class TdEntity {\n");
            sb.Append("  Name: ").Append(Name).Append("\n");
            sb.Append("  Entitytype: ").Append(Entitytype).Append("\n");
            sb.Append("  Subtype: ").Append(Subtype).Append("\n");
            sb.Append("  Extended: ").Append(Extended).Append("\n");
            sb.Append("  Attributes: ").Append(Attributes).Append("\n");
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
            return this.Equals(input as TdEntity);
        }

        /// <summary>
        /// Returns true if TdEntity instances are equal
        /// </summary>
        /// <param name="input">Instance of TdEntity to be compared</param>
        /// <returns>Boolean</returns>
        public bool Equals(TdEntity input)
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
                    this.Entitytype == input.Entitytype ||
                    (this.Entitytype != null &&
                    this.Entitytype.Equals(input.Entitytype))
                ) && 
                (
                    this.Subtype == input.Subtype ||
                    (this.Subtype != null &&
                    this.Subtype.Equals(input.Subtype))
                ) && 
                (
                    this.Extended == input.Extended ||
                    this.Extended != null &&
                    input.Extended != null &&
                    this.Extended.SequenceEqual(input.Extended)
                ) && 
                (
                    this.Attributes == input.Attributes ||
                    this.Attributes != null &&
                    input.Attributes != null &&
                    this.Attributes.SequenceEqual(input.Attributes)
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
                if (this.Entitytype != null)
                {
                    hashCode = (hashCode * 59) + this.Entitytype.GetHashCode();
                }
                if (this.Subtype != null)
                {
                    hashCode = (hashCode * 59) + this.Subtype.GetHashCode();
                }
                if (this.Extended != null)
                {
                    hashCode = (hashCode * 59) + this.Extended.GetHashCode();
                }
                if (this.Attributes != null)
                {
                    hashCode = (hashCode * 59) + this.Attributes.GetHashCode();
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
