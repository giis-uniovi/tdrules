package giis.tdrules.client.oa.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.client.oa.shared.OaUtil;
import giis.tdrules.model.shared.EntityTypes;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Handles the transformations for composites: types and arrays.
 * The main entry points are the extract* methods that set the type of
 * an attribute and proceed recursively to extract and link the entities
 * for nested objects and refs.
 * 
 * These methods return true if everything is successful to notify
 * the caller that the attribute can be added to the model,
 * false otherwise.
 */
public class CompositeTransformer {
	protected static final Logger log=LoggerFactory.getLogger(CompositeTransformer.class);
	
	private SchemaTransformer st; // to call back the SchemaTransformer
	
	public CompositeTransformer(SchemaTransformer st) {
		this.st = st;
	}
	
	/**
	 * Returns a new type entity extracted from an Open Api attribute that contains
	 * a nested object; the name of the new entity is in the form entity_attribute_xt.
	 */
	boolean extractInlineType(Schema<?> oaObject, TdAttribute attribute, TdEntity entity) {
		extractObject(OaExtensions.getExtractedTypeName(entity.getName(), attribute.getName()), 
				"", EntityTypes.DT_TYPE, oaObject, entity, attribute);
		return true;
	}

	/**
	 * Returns a new type entity extracted from an Open Api attribute that contains
	 * a referenced object; the name of the new entity is in the form entity_attribute_xt.
	 */
	boolean extractReferencedType(Schema<?> oaProperty, TdAttribute attribute, TdEntity entity) {
		log.debug("*handle type reference {}", attribute.getName()); // extract object type
		Schema<?> refProperty = resolveOaRef(oaProperty);
		if (refProperty == null) {
			handleUndefinedOaRef(entity, oaProperty.get$ref());
			return false;
		}
		TdEntity refEntity = st.getEntity(refProperty.getName(), refProperty, null, entity);
		TdAttribute pk = refEntity.getUid();
		// When an property is defined as an external ref, nullable is unknown
		// Should the original oaProperty be nullable?
		if (pk != null) {
			// si tiene pk, el tipo extraido debe cambiar pk por fk a la tabla maestra
			TdEntity type = extractObjectType(refProperty, refEntity.getName(), attribute, entity);
			TdAttribute typeAttr = type.getUid();
			typeAttr.rid(composeReference(refEntity.getName(), refEntity.getUid().getName())).uid("");
		} else { // proceeds as in inline types, but using the resolved property from the ref
			extractObjectType(refProperty, refEntity.getName(), attribute, entity);
		}
		st.addVisitedEntity(refEntity);
		return true;
	}

	private TdEntity extractObjectType(Schema<?> oaObject, String refEntityName, TdAttribute attribute, TdEntity entity) {
		return extractObject(OaExtensions.getExtractedTypeName(entity.getName(), attribute.getName()), 
				refEntityName, EntityTypes.DT_TYPE, oaObject, entity, attribute);
	}

	// Common method to extract entities from object properties (this is also be used to extract object arrays)
	private TdEntity extractObject(String finalName, String refEntityName, String compositeType, 
			Schema<?> oaObject, TdEntity entity, TdAttribute attribute) {
		log.debug("*handle object: {}, extract to: {}, name: {}", attribute.getName(), compositeType, finalName);
		TdEntity newEntity = st.getEntity(finalName, oaObject, null, entity);

		// Entities created from a composite must remember the original attribute in the subtype attribute
		newEntity.entitytype(compositeType).subtype(refEntityName);

		// The origin attribute gets the name of the new entity as data type
		attribute.datatype(finalName).compositetype(compositeType);
		st.addEntity(newEntity);
		return newEntity;
	}
	
	/**
	 * Returns a new array entity extracted from an Open Api attribute that contains an array 
	 * (of objects or primitive); the name of the new entity is in the form entity_attribute_xa.
	 * This also handles objects that are defined in refs.
	 */
	boolean extractArray(Schema<?> oaObject, TdAttribute attribute, TdEntity entity) {
		String finalName = OaExtensions.getExtractedArrayName(entity.getName(), attribute.getName());
		log.debug("*handle object: {}, extract to array: {}", attribute.getName(), finalName); // extract-object-type
		// array is the type, the type of each element is the subtype
		Schema<?> oaItems = ((ArraySchema) oaObject).getItems();

		// resolve reference
		String ref = oaItems.get$ref();
		String refEntityName = "";
		if (ref != null) {
			oaItems = resolveOaRef(oaItems);
			if (oaItems == null) {
				handleUndefinedOaRef(entity, ref + "[]"); // brackets to indicate array
				return false;
			}
			OaUtil.setObject(oaItems);
			TdEntity refTable = st.getEntity(oaItems.getName(), oaItems, null, entity);
			refEntityName = refTable.getName();
			st.addVisitedEntity(refTable);
		}
		
		TdEntity array;
		// adds subtype to allow differentiate arrays of objects from arrays of primitives
		attribute.subtype(oaItems.getType()); 
		if (OaUtil.isObject(oaItems)) { // array of objects
			array = extractObjectArray(oaItems, refEntityName, attribute, entity);
		} else { // array of primitive items
			array = extractPrimitiveArray(oaItems, attribute, entity);
		}
		// Add the uid and other descriptors
		// A regular array has an integer uid, but when it is created from
		// additionalProperties, it must be string (because it is the key of the map)
		String uidDatatype = OaExtensions.ADDITIONAL_PROPERTIES.equals(attribute.getName()) ? "string" : "integer";
		TdAttribute pkcolumn = new TdAttribute().name(OaExtensions.ARRAY_PK)
				.uid("true").notnull("true").datatype(uidDatatype);
		array.getAttributes().add(0, pkcolumn); // inserta al principio
		// Add the rid to the enclosing entity
		linkArrayToContainerEntity(array, entity);
		return true;
	}

	// uses the same method that that used for types
	private TdEntity extractObjectArray(Schema<?> oaObject, String refEntityName, TdAttribute attribute, TdEntity entity) {
		return extractObject(OaExtensions.getExtractedArrayName(entity.getName(), attribute.getName()),
				refEntityName, EntityTypes.DT_ARRAY, oaObject, entity, attribute);
	}

	private TdEntity extractPrimitiveArray(Schema<?> oaObject, TdAttribute attribute, TdEntity entity) {
		// because it is a primitive array, there is no any entity to extract,
		// first creates an entity with the primitive attribute only
		String finalName = OaExtensions.getExtractedArrayName(entity.getName(), attribute.getName());
		TdEntity newEntity = st.createNewEntity(finalName, entity);
		TdAttribute column = st.createNewAttribute(attribute.getName(), oaObject, newEntity);
		newEntity.addAttributesItem(column);

		// same as for object array, however here subtype is not set
		newEntity.setArray();
		attribute.datatype(finalName).setArray();
		st.addEntity(newEntity);
		return newEntity;
	}

	void linkArrayToContainerEntity(TdEntity array, TdEntity container) {
		TdAttribute containerUid = container.getUid();
		// If container entity is an object, its uid can be set now.
		// A second main step will be performed later to fill arrays without rid
		if (containerUid == null) {
			log.warn("Adjacent upstream {} does not have any uid, it can not be linked now to array entity {}",
					container.getName(), array.getName());
			return;
		}
		String containerUidName = composeReference(container.getName(), containerUid.getName());
		TdAttribute rid = new TdAttribute().name(OaExtensions.ARRAY_FK)
				.rid(containerUidName).notnull("true").datatype(containerUid.getDatatype());
		log.debug("  Add rid {}={} to array {}", containerUidName, OaExtensions.ARRAY_FK, array.getName());
		array.getAttributes().add(1, rid); // insert just after the uid
	}
	
	// Additional utilities to handle references
	
	// Obtains the model of the referenced object, may be null if not found
	private Schema<?> resolveOaRef(Schema<?> objectModel) {
		log.debug("*resolve oaRef: {}", objectModel.get$ref());
		String ref = objectModel.get$ref();
		String name = ref.replace("#/components/schemas/", "");
		
		objectModel = st.getOaSchemas().get(name); // replaces with the resolved object
		// If not found, creates a log entry instead of fail.
		// As the returned value will be null, the caller should manage this situation
		if (objectModel == null)
			st.getOaLogger().warn(log, "Can't resolve oaRef: {}", ref);
		else
			objectModel.name(name);
		return objectModel;
	}
	
	// when a ref can't be resolved, this method can be used to add
	// the names of the unresolved refs to the entity extended attributes
	private void handleUndefinedOaRef(TdEntity entity, String ref) {
		String name = ref.replace("#/components/schemas/", "");
		String current = entity.getExtendedItem(OaExtensions.UNDEFINED_REFS);
		String updated = (current == null ? "" : current + ",") + name;
		entity.putExtendedItem(OaExtensions.UNDEFINED_REFS, updated);
	}
	
	private String composeReference(String entity, String attribute) {
		return OaUtil.quoteIfNeeded(entity) + "." + OaUtil.quoteIfNeeded(attribute);
	}

}
