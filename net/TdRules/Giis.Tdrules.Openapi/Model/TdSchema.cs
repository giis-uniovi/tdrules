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
    /// Represents the structure of the data store.
    /// </summary>
    [DataContract(Name = "TdSchema")]
    public partial class TdSchema : IValidatableObject
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="TdSchema" /> class.
        /// </summary>
        /// <param name="catalog">An optional logical namespace that can contain multiple schemas (e.g. the name of the database catalog as returned by JDBC in RDBMS) (default to &quot;&quot;).</param>
        /// <param name="schema">An optional name to uniquely identify the data store in a catalog (default to &quot;&quot;).</param>
        /// <param name="storetype">A string representing the store type. It is used by the applications to handle the variability of different data stores: - In an RDB is the database vendor name returned by jdbc, eg. &#x60;postgres&#x60;, &#x60;oracle&#x60;, &#x60;sqlserver&#x60; - In OpenApi is the string &#x60;openapi&#x60;  (default to &quot;&quot;).</param>
        /// <param name="entities">The set of entities that compose this schema.</param>
        public TdSchema(string catalog = @"", string schema = @"", string storetype = @"", List<TdEntity> entities = default(List<TdEntity>))
        {
            // use default value if no "catalog" provided
            this.Catalog = catalog ?? @"";
            // use default value if no "schema" provided
            this.Schema = schema ?? @"";
            // use default value if no "storetype" provided
            this.Storetype = storetype ?? @"";
            this.Entities = entities;
        }

        /// <summary>
        /// An optional logical namespace that can contain multiple schemas (e.g. the name of the database catalog as returned by JDBC in RDBMS)
        /// </summary>
        /// <value>An optional logical namespace that can contain multiple schemas (e.g. the name of the database catalog as returned by JDBC in RDBMS)</value>
        [DataMember(Name = "catalog", EmitDefaultValue = false)]
        public string Catalog { get; set; }
        public string GetCatalog() { return Catalog; }
        public void SetCatalog(string value) { Catalog=value; }

        /// <summary>
        /// An optional name to uniquely identify the data store in a catalog
        /// </summary>
        /// <value>An optional name to uniquely identify the data store in a catalog</value>
        [DataMember(Name = "schema", EmitDefaultValue = false)]
        public string Schema { get; set; }
        public string GetSchema() { return Schema; }
        public void SetSchema(string value) { Schema=value; }

        /// <summary>
        /// A string representing the store type. It is used by the applications to handle the variability of different data stores: - In an RDB is the database vendor name returned by jdbc, eg. &#x60;postgres&#x60;, &#x60;oracle&#x60;, &#x60;sqlserver&#x60; - In OpenApi is the string &#x60;openapi&#x60; 
        /// </summary>
        /// <value>A string representing the store type. It is used by the applications to handle the variability of different data stores: - In an RDB is the database vendor name returned by jdbc, eg. &#x60;postgres&#x60;, &#x60;oracle&#x60;, &#x60;sqlserver&#x60; - In OpenApi is the string &#x60;openapi&#x60; </value>
        [DataMember(Name = "storetype", EmitDefaultValue = false)]
        public string Storetype { get; set; }
        public string GetStoretype() { return Storetype; }
        public void SetStoretype(string value) { Storetype=value; }

        /// <summary>
        /// The set of entities that compose this schema
        /// </summary>
        /// <value>The set of entities that compose this schema</value>
        [DataMember(Name = "entities", EmitDefaultValue = false)]
        public List<TdEntity> Entities { get; set; }
        public List<TdEntity> GetEntities() { return Entities; }
        public void SetEntities(List<TdEntity> value) { Entities=value; }
        public void AddEntitiesItem(TdEntity item) { if (this.Entities == null) this.Entities = new List<TdEntity>(); this.Entities.Add(item); }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("class TdSchema {\n");
            sb.Append("  Catalog: ").Append(Catalog).Append("\n");
            sb.Append("  Schema: ").Append(Schema).Append("\n");
            sb.Append("  Storetype: ").Append(Storetype).Append("\n");
            sb.Append("  Entities: ").Append(Entities).Append("\n");
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
