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
 * Handles the transformations for composites: types and arrays
 */
public class CompositeTransformer {
	protected static final Logger log=LoggerFactory.getLogger(CompositeTransformer.class);
	
	private SchemaTransformer st;
	
	public CompositeTransformer(SchemaTransformer st) {
		this.st = st;
	}
	
	/**
	 * Returns a new type entity extracted from an Open Api attribute that contains
	 * a nested object; the name of the new entity is in the form entity_attribute_xt.
	 */
	TdEntity extractType(Schema<?> oaObject, String refEntityName, TdAttribute attribute, TdEntity entity) {
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
	 */
	void extractArray(Schema<?> oaObject, String refEntityName, TdAttribute attribute, TdEntity entity) {
		String finalName = OaExtensions.getExtractedArrayName(entity.getName(), attribute.getName());
		log.debug("*handle object: {}, extract to array: {}", attribute.getName(), finalName); // extract-object-type
		// array is the type, the type of each element is the subtype
		Schema<?> oaItems = ((ArraySchema) oaObject).getItems();

		// resolve reference
		String ref = oaItems.get$ref();
		if (ref != null) {
			oaItems = st.resolveOaRef(oaItems);
			if (oaItems == null) {
				st.handleUndefinedOaRef(entity, ref + "[]"); // brackets to indicate array
				return;
			}
			OaUtil.setObject(oaItems);
			TdEntity refTable = st.getEntity(oaItems.getName(), oaItems, null, entity);
			refEntityName = refTable.getName();
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
		TdAttribute pkcolumn = new TdAttribute().name(OaExtensions.ARRAY_PK)
				.uid("true").notnull("true").datatype("integer");
		array.getAttributes().add(0, pkcolumn); // inserta al principio
		// Add the rid to the enclosing entity
		linkArrayToContainerEntity(array, entity);
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
		String containerUidName = st.composeReference(container.getName(), containerUid.getName());
		TdAttribute rid = new TdAttribute().name(OaExtensions.ARRAY_FK)
				.rid(containerUidName).notnull("true").datatype(containerUid.getDatatype());
		log.debug("  Add rid {}={} to array {}", containerUidName, OaExtensions.ARRAY_FK, array.getName());
		array.getAttributes().add(1, rid); // insert just after the uid
	}

}
