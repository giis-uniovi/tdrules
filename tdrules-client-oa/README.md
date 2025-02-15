# tdrules-client-oa

Transforms an OpenAPI data schema into a TdSchema model as indicated in the [main README](../README.md):

```Java
OaSchemaApi api = new OaSchemaApi(spec);
TdSchema schemaModel = api.getSchema();
```

or

```Java
TdSchema schemaModel = new OaSchemaApi(spec).getSchema();
```

By default, all entities under the `componentes.schemas` are transformed.
The api can be configured to transform only the entities that are request or response in POST or PUT operations and their dependents, e.g.

```Java
TdSchema schemaModel = new OaSchemaApi(spec).setOnlyEntitiesInPaths(true).getSchema();
```

## Configuration and other features

The required configuration to transform the model is explained below:

### Set the unique identifiers of entities

Identify the properties in the OpenAPI schema that uniquely identify each schema object (uid) and the references between objects (rid):
- Add the custom vendor extension `x-pk=true` for each uid.
- Add the custom vendor extension `x-fk=<entity>.<referencedEntity>` for each rid.

### Automatic setup of unique identifiers

A common pattern is to use a convention to identify the uids (e.g. using the name `id`). If so, you can inject an instance of an id resolver
that sets the appropriate custom vendor extensions. For example:

```Java
OaSchemaApi api = new OaSchemaApi(spec).setIdResolver(new OaSchemaIdResolver().setIdName("id"));
```

- Each attribute named `id` is set as uid
- Each attribute named `nameId` (camel case) or `name_id` (snake case) is set as rid referencing other entity with name `name` and uid `id`
- The convention using prefixed uids is also supported, e.g. `nameId` or `name_id` is set as uid provided that `name` is the entity that contains the attribute
- Matching entity names is case insensitive to match attributes starting with lowercase with referenced entities starting with uppercase

Schema objects that do not follow this convention can be excluded by using the id resolver method `.excludeEntity("entity_to_exclude")`.

### Path parameters

There is another common pattern where the the rids are not included in the request body, but included as path parameters in a POST request.
For example, to create `entity` that references `master`: `POST /myapi/entity_path/{master_id}`

If the path parameter section for `master_id` includes the vendor extension `x-fk=master.id` 
or there is a id resolver configured for `id`, 
the property `master_id` referencing `master.id` will be added to the request schema model.

### Other features

**Filters**: To filter-out some schema objects and/or properties, a filter can be injected. For example

```Java
OaSchemaApi api = new OaSchemaApi(spec).setFilter(new OaSchemaFilter().add("internal*", "*").add("*", "_link*"));
```

This will exclude all objects with a name that starts with `internal` and all attributes in every objet where the name starts with `_link`.

**Mermaid**: To have a graphical UML-like representation of a TdRules model, you can get its Mermaid representation as a string, that can be pasted in a Mermaid wiewer or editor (e.g. https://mermaid.live/). For example

```Java
TdSchema schemaModel = new OaSchemaApi(spec).getSchema();
String mermaid = new MermaidWriter(schemaModel).getMermaid();
```

gets a Mermaid representation of the model, where
- Entities are drawn as classes and include the post and put paths as methods
- References between entities (via rid and uid) are drawn as an association with * cardinality at the referring side
- Nested objects are drawn as classes, connected to their containing entity by a composition relationship
- Arrays, are displayed similarly, with * cardinality
- When object and arrays are defined by a $ref in `component.schemas`, this entity and the object/array entity  are connected with a realization relationship
- References to entities that are not in the schema are drawn as notes

The MermaidWriter accepts a few configuration settings:
- `setLeftToRight()` : Draws from left to right (default is top to bottom) for a better display of large diagrams.
- `setGroupEntitiesInPath(boolean link, boolean box)`: Connects with a dashed line (link) and/or places inside a box the entities that are in the same path and method for a better visual arrangement of related entities.

## Summary of transformations and data types

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

Special data types:
- Free-form objects: they are represented as a datatype=free-form-object and handled like a primitive datatype
- additionalProperties: they are included as an attribute `additionalProperties` that contains an array with the keys and values
  
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
- additionalProperties: SUPPORTED, handled as an array
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
