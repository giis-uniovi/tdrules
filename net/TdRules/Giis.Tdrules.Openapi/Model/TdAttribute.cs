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
    /// Represents an attribute in an entity. To generate coverage rules, at least &#x60;name&#x60; and &#x60;dtype&#x60; must be specified
    /// </summary>
    [DataContract(Name = "TdAttribute")]
    public partial class TdAttribute : IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="TdAttribute" /> class.
        /// </summary>
        /// <param name="name">The name of this attribute (default to &quot;&quot;).</param>
        /// <param name="datatype">The data type name of this attribute. (default to &quot;&quot;).</param>
        /// <param name="compositetype">If the data type is not primitive this attibute holds &#x60;array&#x60; or &#x60;type&#x60;, NOTE, If this field is &#x60;array&#x60; then the &#x60;datatype&#x60; holds the type of each array item, If this field is &#x60;type&#x60; refers the an entity type (default to &quot;&quot;).</param>
        /// <param name="subtype">An optional modifier of the data type, eg. given a &#x60;DATETIME WITH TIMEZONE&#x60; in an RDB, this field stores &#x60;WITH TIMEZONE&#x60; and the &#x60;datatype&#x60; field stores &#x60;DATETIME&#x60; (default to &quot;&quot;).</param>
        /// <param name="size">An optional size of the data type of this attribute. eg. given a &#x60;DECIMAL(10,2)&#x60; in a RDB, this field stores the value &#x60;10,2&#x60; (default to &quot;&quot;).</param>
        /// <param name="uid">If this value is &#x60;true&#x60;, the attribute is the unique identifier (or part thereof) of the data stored (default to &quot;&quot;).</param>
        /// <param name="autoincrement">If this value is &#x60;true&#x60;, the value of the attribute is autogenerated in the backend when a new instance is created (default to &quot;&quot;).</param>
        /// <param name="notnull">If this value is &#x60;true&#x60;, the attribute is not nullable (default to &quot;&quot;).</param>
        /// <param name="varReadonly">If this value is &#x60;true&#x60;, the attribute should not be sent as part of a request to update the data store (default to &quot;&quot;).</param>
        /// <param name="rid">If this value is non empty, represents a reference to an attribute in another entity (e.g. foreign key in RDB). This value must be in the form &#x60;&lt;entity-name&gt;.&lt;attribute-name&gt;&#x60; (default to &quot;&quot;).</param>
        /// <param name="ridname">An optional name to refer this &#39;rid&#39; (default to &quot;&quot;).</param>
        /// <param name="checkin">A list of allowed literals to constraint the possible values of this attribute, e.g. &#x60;&#39;Y&#39;,&#39;N&#39;&#x60; represents a field that can have only these two values (default to &quot;&quot;).</param>
        /// <param name="defaultvalue">If this value is non empty, indicates the default value applicable to this attribute (default to &quot;&quot;).</param>
        /// <param name="extended">A map of additional attribute properties to store information required by other applications.</param>
        public TdAttribute(string name = @"", string datatype = @"", string compositetype = @"", string subtype = @"", string size = @"", string uid = @"", string autoincrement = @"", string notnull = @"", string varReadonly = @"", string rid = @"", string ridname = @"", string checkin = @"", string defaultvalue = @"", Dictionary<string, string> extended = default(Dictionary<string, string>))
        {
            // use default value if no "name" provided
            this.Name = name ?? @"";
            // use default value if no "datatype" provided
            this.Datatype = datatype ?? @"";
            // use default value if no "compositetype" provided
            this.Compositetype = compositetype ?? @"";
            // use default value if no "subtype" provided
            this.Subtype = subtype ?? @"";
            // use default value if no "size" provided
            this.Size = size ?? @"";
            // use default value if no "uid" provided
            this.Uid = uid ?? @"";
            // use default value if no "autoincrement" provided
            this.Autoincrement = autoincrement ?? @"";
            // use default value if no "notnull" provided
            this.Notnull = notnull ?? @"";
            // use default value if no "varReadonly" provided
            this.Readonly = varReadonly ?? @"";
            // use default value if no "rid" provided
            this.Rid = rid ?? @"";
            // use default value if no "ridname" provided
            this.Ridname = ridname ?? @"";
            // use default value if no "checkin" provided
            this.Checkin = checkin ?? @"";
            // use default value if no "defaultvalue" provided
            this.Defaultvalue = defaultvalue ?? @"";
            this.Extended = extended;
        }

        /// <summary>
        /// The name of this attribute
        /// </summary>
        /// <value>The name of this attribute</value>
        [DataMember(Name = "name", EmitDefaultValue = false)]
        public string Name { get; set; }
        public string GetName() { return Name; }
        public void SetName(string value) { Name=value; }

        /// <summary>
        /// The data type name of this attribute.
        /// </summary>
        /// <value>The data type name of this attribute.</value>
        [DataMember(Name = "datatype", EmitDefaultValue = false)]
        public string Datatype { get; set; }
        public string GetDatatype() { return Datatype; }
        public void SetDatatype(string value) { Datatype=value; }

        /// <summary>
        /// If the data type is not primitive this attibute holds &#x60;array&#x60; or &#x60;type&#x60;, NOTE, If this field is &#x60;array&#x60; then the &#x60;datatype&#x60; holds the type of each array item, If this field is &#x60;type&#x60; refers the an entity type
        /// </summary>
        /// <value>If the data type is not primitive this attibute holds &#x60;array&#x60; or &#x60;type&#x60;, NOTE, If this field is &#x60;array&#x60; then the &#x60;datatype&#x60; holds the type of each array item, If this field is &#x60;type&#x60; refers the an entity type</value>
        [DataMember(Name = "compositetype", EmitDefaultValue = false)]
        public string Compositetype { get; set; }
        public string GetCompositetype() { return Compositetype; }
        public void SetCompositetype(string value) { Compositetype=value; }

        /// <summary>
        /// An optional modifier of the data type, eg. given a &#x60;DATETIME WITH TIMEZONE&#x60; in an RDB, this field stores &#x60;WITH TIMEZONE&#x60; and the &#x60;datatype&#x60; field stores &#x60;DATETIME&#x60;
        /// </summary>
        /// <value>An optional modifier of the data type, eg. given a &#x60;DATETIME WITH TIMEZONE&#x60; in an RDB, this field stores &#x60;WITH TIMEZONE&#x60; and the &#x60;datatype&#x60; field stores &#x60;DATETIME&#x60;</value>
        [DataMember(Name = "subtype", EmitDefaultValue = false)]
        public string Subtype { get; set; }
        public string GetSubtype() { return Subtype; }
        public void SetSubtype(string value) { Subtype=value; }

        /// <summary>
        /// An optional size of the data type of this attribute. eg. given a &#x60;DECIMAL(10,2)&#x60; in a RDB, this field stores the value &#x60;10,2&#x60;
        /// </summary>
        /// <value>An optional size of the data type of this attribute. eg. given a &#x60;DECIMAL(10,2)&#x60; in a RDB, this field stores the value &#x60;10,2&#x60;</value>
        [DataMember(Name = "size", EmitDefaultValue = false)]
        public string Size { get; set; }
        public string GetSize() { return Size; }
        public void SetSize(string value) { Size=value; }

        /// <summary>
        /// If this value is &#x60;true&#x60;, the attribute is the unique identifier (or part thereof) of the data stored
        /// </summary>
        /// <value>If this value is &#x60;true&#x60;, the attribute is the unique identifier (or part thereof) of the data stored</value>
        [DataMember(Name = "uid", EmitDefaultValue = false)]
        public string Uid { get; set; }
        public string GetUid() { return Uid; }
        public void SetUid(string value) { Uid=value; }

        /// <summary>
        /// If this value is &#x60;true&#x60;, the value of the attribute is autogenerated in the backend when a new instance is created
        /// </summary>
        /// <value>If this value is &#x60;true&#x60;, the value of the attribute is autogenerated in the backend when a new instance is created</value>
        [DataMember(Name = "autoincrement", EmitDefaultValue = false)]
        public string Autoincrement { get; set; }
        public string GetAutoincrement() { return Autoincrement; }
        public void SetAutoincrement(string value) { Autoincrement=value; }

        /// <summary>
        /// If this value is &#x60;true&#x60;, the attribute is not nullable
        /// </summary>
        /// <value>If this value is &#x60;true&#x60;, the attribute is not nullable</value>
        [DataMember(Name = "notnull", EmitDefaultValue = false)]
        public string Notnull { get; set; }
        public string GetNotnull() { return Notnull; }
        public void SetNotnull(string value) { Notnull=value; }

        /// <summary>
        /// If this value is &#x60;true&#x60;, the attribute should not be sent as part of a request to update the data store
        /// </summary>
        /// <value>If this value is &#x60;true&#x60;, the attribute should not be sent as part of a request to update the data store</value>
        [DataMember(Name = "readonly", EmitDefaultValue = false)]
        public string Readonly { get; set; }
        public string GetReadonly() { return Readonly; }
        public void SetReadonly(string value) { Readonly=value; }

        /// <summary>
        /// If this value is non empty, represents a reference to an attribute in another entity (e.g. foreign key in RDB). This value must be in the form &#x60;&lt;entity-name&gt;.&lt;attribute-name&gt;&#x60;
        /// </summary>
        /// <value>If this value is non empty, represents a reference to an attribute in another entity (e.g. foreign key in RDB). This value must be in the form &#x60;&lt;entity-name&gt;.&lt;attribute-name&gt;&#x60;</value>
        [DataMember(Name = "rid", EmitDefaultValue = false)]
        public string Rid { get; set; }
        public string GetRid() { return Rid; }
        public void SetRid(string value) { Rid=value; }

        /// <summary>
        /// An optional name to refer this &#39;rid&#39;
        /// </summary>
        /// <value>An optional name to refer this &#39;rid&#39;</value>
        [DataMember(Name = "ridname", EmitDefaultValue = false)]
        public string Ridname { get; set; }
        public string GetRidname() { return Ridname; }
        public void SetRidname(string value) { Ridname=value; }

        /// <summary>
        /// A list of allowed literals to constraint the possible values of this attribute, e.g. &#x60;&#39;Y&#39;,&#39;N&#39;&#x60; represents a field that can have only these two values
        /// </summary>
        /// <value>A list of allowed literals to constraint the possible values of this attribute, e.g. &#x60;&#39;Y&#39;,&#39;N&#39;&#x60; represents a field that can have only these two values</value>
        [DataMember(Name = "checkin", EmitDefaultValue = false)]
        public string Checkin { get; set; }
        public string GetCheckin() { return Checkin; }
        public void SetCheckin(string value) { Checkin=value; }

        /// <summary>
        /// If this value is non empty, indicates the default value applicable to this attribute
        /// </summary>
        /// <value>If this value is non empty, indicates the default value applicable to this attribute</value>
        [DataMember(Name = "defaultvalue", EmitDefaultValue = false)]
        public string Defaultvalue { get; set; }
        public string GetDefaultvalue() { return Defaultvalue; }
        public void SetDefaultvalue(string value) { Defaultvalue=value; }

        /// <summary>
        /// A map of additional attribute properties to store information required by other applications
        /// </summary>
        /// <value>A map of additional attribute properties to store information required by other applications</value>
        [DataMember(Name = "extended", EmitDefaultValue = false)]
        public Dictionary<string, string> Extended { get; set; }
        public Dictionary<string,string> GetExtended() { return Extended; }
        public void SetExtended(Dictionary<string,string> value) { Extended=value; }
        public void PutExtendedItem(string key, string summaryItem) { if (this.Extended == null) this.Extended = new Dictionary<string,string>(); this.Extended[key]=summaryItem; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class TdAttribute {\n");
            sb.Append("  Name: ").Append(Name).Append("\n");
            sb.Append("  Datatype: ").Append(Datatype).Append("\n");
            sb.Append("  Compositetype: ").Append(Compositetype).Append("\n");
            sb.Append("  Subtype: ").Append(Subtype).Append("\n");
            sb.Append("  Size: ").Append(Size).Append("\n");
            sb.Append("  Uid: ").Append(Uid).Append("\n");
            sb.Append("  Autoincrement: ").Append(Autoincrement).Append("\n");
            sb.Append("  Notnull: ").Append(Notnull).Append("\n");
            sb.Append("  Readonly: ").Append(Readonly).Append("\n");
            sb.Append("  Rid: ").Append(Rid).Append("\n");
            sb.Append("  Ridname: ").Append(Ridname).Append("\n");
            sb.Append("  Checkin: ").Append(Checkin).Append("\n");
            sb.Append("  Defaultvalue: ").Append(Defaultvalue).Append("\n");
            sb.Append("  Extended: ").Append(Extended).Append("\n");
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
