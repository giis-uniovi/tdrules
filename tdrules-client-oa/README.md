# tdrules-client-oa

Transforms an OpenAPI data schema into a TdSchema model as indicated in the [main README](../README.md):

```Java
TdSchema schemaModel = new OaSchemaApi(spec).getSchema();
```

## Transformations and data types

Primitive data types (specfied in https://swagger.io/specification/, Specification - Data Types):

- Does not check the type name
- if property includes a format that is included in the standard, the format name is used as the type name
- Formats not included in the standard are ignored

Composite data types. They are transformed as indicated below:

- Inline objects in a property:
  - Extracts the object by creating a an entity with the contents of the object
  - The extracted entity is given datatype=type, subtype=referenced-entity
  - The attribute that contained this object is kept, but is given datatype=name-of-extracted-object, compositetype=type
- Reference to objects in a property: Resolves the objects and proceeds as before
- Arrays of objects (inline or references): 
  - Extracts the object as before and creates an uid
  - The extracted entity is given datatype=table (not type)
  - Link to the entitie by creating a rid
  - The attribute that has contains this object is given datatype=name-of-extracted-entity, compositetype=array
- Arrays of primitive data types: Creates an entity with the attribute and proceeds as for arrrays of objects
  
## Other transformations and features

- Use the custom vendor extensions (x-pk, x-fk) o identify the attributes that are uid or rid.
- Set Unique identifier (uid and rid) conventions to avoid modify the schema with custom vendor extensions:
  `OaSchemaIdResolver` can be injected into the `OaSchemaApi` to set a convention to determine the uid's and rid's. 
  A sublcass can be used to a more detailed customization.
- Filtering: `OaSchemaFilter` can be injected into the `OaSchemaApi` to prevent some entities and attributes 
  be generated in the resulting TdSchema.
- Graphical representation of the resulting TdSchema: `MermaidWriter` transforms the TdSchema model into a 
  Mermaid string that can be visualized or included in a markdown document.

## Supported keywords

Note on identifiers: currently only chars/digits/underscore. If including other chars, they must be double quoted

Grouped according to the standard specification:
https://swagger.io/docs/specification/data-models/keywords/

Supported Keywords: These keywords have the same meaning as in JSON Schema:

- title: n/a
- pattern
- required: Still not supported
- enum: SUPPORTED, enum values are converted to the attribute checkin property. Tested with string/integer values
- minimum: SUPPORTED, generates a check constraint on the entity
- maximum: SUPPORTED, generates a check constraint on the entity
- exclusiveMinimum: SUPPORTED, generates a check constraint on the entity
- exclusiveMaximum: SUPPORTED, generates a check constraint on the entity
- multipleOf
- minLength
- maxLength: SUPPORTED, sets the size of the attribute to this value
- minItems
- maxItems
- uniqueItems
- minProperties
- maxProperties

These keywords are supported with minor differences:

- type: SUPPORTED, see data types above
- format: PARTIALLY SUPPORTED: only standard formats are used as the name of the data type
- description: n/a
- items: SUPPORTED, see Composite data types above
- properties: SUPPORTED, each property is converted in an attribute
- additionalProperties: n/a
- default: PARTIALLY SUPPORTED, converted to the default attribute of the column.
  As the convention is that null and empty are handled in the same way, an empty string can't dos not generate default
- allOf, oneOf, anyOf: planned

Additional Keywords: Open API schemas can also use the following keywords that are not part of JSON Schema:

- deprecated: n/a
- example: n/a
- externalDocs: n/a
- nullable: SUPPORTED, if nullable=false or ommited, the attribute is given notnull=true
- readOnly: SUPPORTED
- writeOnly:
- xml: n/a
