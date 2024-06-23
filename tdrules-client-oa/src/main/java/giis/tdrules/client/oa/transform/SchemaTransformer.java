package giis.tdrules.client.oa.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.client.oa.OaSchemaLogger;
import giis.tdrules.model.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Converts a previously parsed OpenApi schema specification into the TbSchema
 * model needed to generate FPC Coverage Rules
 */
@SuppressWarnings("rawtypes")
public class SchemaTransformer {
	protected static final Logger log = LoggerFactory.getLogger(SchemaTransformer.class);

	private Map<String, Schema> oaSchemas; // parsed OpenApi source
	private TdSchema tdSchema; // target schema
	private CompositeTransformer ct; // manages transformation of composites (objects and arrays)
	private UpstreamAttribute upstreamAttr;
	private OaSchemaLogger oaLogger; // specialized logger to store important messages

	public SchemaTransformer(Map<String, Schema> oaSchemas, OaSchemaLogger oaLogger) {
		this.oaSchemas = oaSchemas;
		this.oaLogger = oaLogger;
		this.ct = new CompositeTransformer(this);
		this.tdSchema = new TdSchema().storetype("openapi");
		this.upstreamAttr = new UpstreamAttribute(this.tdSchema);
	}

	/**
	 * Gets the TdSchema object that has been converted
	 */
	public TdSchema getTdSchema() {
		return this.tdSchema;
	}

	/**
	 * Internal entrypoint to transform a map of OpenApi schema objects to the
	 * TdSchema model (to be called from the client api)
	 */
	public SchemaTransformer transform() {
		for (Entry<String, Schema> oaSchema : oaSchemas.entrySet()) {
			log.debug("Transform OA schema object: {}", oaSchema.getKey());
			TdEntity entity = getEntity(oaSchema.getKey(), oaSchema.getValue(), null);
			addEntity(entity);
		}
		// When an array is created, the rid can't be determined if his immediate upstream is a nested object,
		// and a recursive search on all upstreams can't be made because upstream entities are created later.
		// This second step gets the rid of arrays that may be missing
		for (TdEntity entity : tdSchema.getEntities())
			if (entity.isArray() && entity.getRids().isEmpty())
				fillMissingRid(entity);
		// Remove the extended upstream attribute, as it is not part of the current model
		for (TdEntity entity : tdSchema.getEntities())
			upstreamAttr.removeUpstream(entity);
		return this;
	}

	// This is called in a second main step to set rids that where not found
	// in the immediate upstream of the array at the array creation
	void fillMissingRid(TdEntity entity) {
		log.debug("Try to link to missing Rid for array: {}", entity.getName());
		TdEntity upstreamWithUid = upstreamAttr.findUpstreamWithUid(entity);
		if (upstreamWithUid == null)
			oaLogger.warn("Can't get the rid for array {} because it has not any upstream with uid", entity.getName());
		else {
			ct.linkArrayToContainerEntity(entity, upstreamWithUid);
			// If the referenced entity is not the adjacent upstream, the mermaid drawing will show incorrect relations.
			// Sets an extended attribute to indicate the correct entity to link
			upstreamAttr.setMermaidRid(entity);
		}
	}

	TdEntity getEntity(String name, Schema<?> oaSchema, TdEntity upstream) {
		TdEntity entity = createNewEntity(name, upstream);
		Map<String, Schema> oaAttributes = oaSchema.getProperties();
		if (oaAttributes == null) {
			oaLogger.warn("Open Api schema for {} does not have any property, generated entity will be empty", name);
			return entity;
		}
		// Gitlab rp issue #12: As the processing of each attribute is recursive,
		// if an object (eg. array), is declared before the uid of the entity,
		// when creating the new entity for this object, the uid couldn't be found.
		// Process in order: first the uids, next the rids and finally other attributes
		List<String> uids = new ArrayList<>();
		List<String> rids = new ArrayList<>();
		this.collectAttributeIds(oaAttributes, uids, rids);
		for (Entry<String, Schema> oaAttribute : oaAttributes.entrySet())
			if (uids.contains(oaAttribute.getKey()))
				addAttribute(oaAttribute, entity);
		for (Entry<String, Schema> oaProperty : oaAttributes.entrySet())
			if (!uids.contains(oaProperty.getKey()) && rids.contains(oaProperty.getKey()))
				addAttribute(oaProperty, entity);
		for (Entry<String, Schema> oaProperty : oaAttributes.entrySet())
			if (!uids.contains(oaProperty.getKey()) && !rids.contains(oaProperty.getKey()))
				addAttribute(oaProperty, entity);

		return entity;
	}

	// returns a new empty entity (initially as object entity)
	TdEntity createNewEntity(String name, TdEntity upstream) {
		String upstreamName = upstream == null ? "null" : upstream.getName();
		log.debug("Entity: {}, Upstream: {}", name, upstreamName);
		TdEntity entity = new TdEntity().name(name).setObject();
		upstreamAttr.setUpstream(entity, upstreamName);
		return entity;
	}

	void addEntity(TdEntity entity) {
		log.debug("*add entity if does not exists {}", entity.getName());
		tdSchema.addEntitiesItemIfNotExist(entity);
	}

	TdAttribute getAttribute(String name, Schema<?> oaProperty, TdEntity entity) {
		log.trace("  property: {}", name);
		TdAttribute attribute = new TdAttribute().name(name);
		setAttributeType(oaProperty, attribute, entity);
		setAttributeDescriptors(oaProperty, attribute, entity);
		setAttributeIds(oaProperty, attribute, entity);
		return attribute;
	}

	private void addAttribute(Entry<String, Schema> oaAttribute, TdEntity entity) {
		TdAttribute attribute = getAttribute(oaAttribute.getKey(), oaAttribute.getValue(), entity);
		entity.addAttributesItem(attribute);
	}
	
	//Main processing for each property of the schema object to transform into a TdAttribute
	
	private void setAttributeType(Schema<?> oaProperty, TdAttribute attribute, TdEntity entity) {
		attribute.datatype(OaUtil.getOaDataType(oaProperty.getType(), oaProperty.getFormat()));
		// Special case for refs: Parser should have been invoked with ResolveFully,
		// but it looks that there are some bugs
		// (eg: https://github.com/swagger-api/swagger-parser/issues/1538)
		if (oaProperty.get$ref() != null) {
			handleOaRef(oaProperty, attribute, entity);
		} else if (OaUtil.isObject(oaProperty)) { // same than rdb (type)
			ct.extractType(oaProperty, "", attribute, entity);
		} else if (OaUtil.isArray(oaProperty)) { // create new detail entity
			ct.extractArray(oaProperty, "", attribute, entity);
		}
	}

	private void handleOaRef(Schema<?> oaProperty, TdAttribute attribute, TdEntity entity) {
		log.debug("*handle reference {}", attribute.getName()); // extract object type
		Schema<?> refProperty = resolveOaRef(oaProperty);
		TdEntity refEntity = getEntity(refProperty.getName(), refProperty, entity);
		TdAttribute pk = refEntity.getUid();
		// When an property is defined as an external ref, nullable is unknown
		// Should the original oaProperty be nullable?
		if (pk != null) {
			// como tiene pk, el tipo extraido debe cambiar pk por fk a la tabla maestra
			TdEntity type = ct.extractType(refProperty, refEntity.getName(), attribute, entity);
			TdAttribute typeAttr = type.getUid();
			typeAttr.rid(composeReference(refEntity.getName(), refEntity.getUid().getName())).uid("");
		} else {
			ct.extractType(refProperty, refEntity.getName(), attribute, entity);
		}
	}

	String composeReference(String entity, String attribute) {
		return OaUtil.quoteIfNeeded(entity) + "." + OaUtil.quoteIfNeeded(attribute);
	}

	// Obtains the model of the referenced object
	Schema resolveOaRef(Schema<?> objectModel) {
		log.debug("*resolve oaRef: {}", objectModel.get$ref());
		String name = objectModel.get$ref().replace("#/components/schemas/", "");
		objectModel = oaSchemas.get(name); // replaces with the resolved object
		objectModel.name(name);
		return objectModel;
	}
		
	//Additional processing to transform oa property into attribute
	
	@SuppressWarnings("unchecked")
	private void setAttributeDescriptors(Schema oaProperty, TdAttribute attribute, TdEntity entity) {
		attribute.notnull(OaUtil.oaBoolean(oaProperty.getNullable()) ? "" : "true");
		attribute.readonly(OaUtil.oaBoolean(oaProperty.getReadOnly()) ? "true" : "");
		attribute.size(oaProperty.getMaxLength() == null ? "" : String.valueOf(oaProperty.getMaxLength()));
		attribute.defaultvalue(oaProperty.getDefault() == null ? "" : oaProperty.getDefault().toString());
		if (oaProperty.getEnum() != null)
			attribute.checkin(OaUtil.oaEnumString(oaProperty.getEnum()));
		if (oaProperty.getMinimum() != null) {
			String rop = oaProperty.getExclusiveMinimum() == Boolean.TRUE ? ">" : ">=";
			entity.addChecksItem(new TdCheck().name("chkmin_" + entity.getName() + "_" + attribute.getName())
					.attribute(attribute.getName()).constraint(attribute.getName() + rop + oaProperty.getMinimum()));
		}
		if (oaProperty.getMaximum() != null) {
			String rop = oaProperty.getExclusiveMaximum() == Boolean.TRUE ? "<" : "<=";
			entity.addChecksItem(new TdCheck().name("chkmax_" + entity.getName() + "_" + attribute.getName())
					.attribute(attribute.getName()).constraint(attribute.getName() + rop + oaProperty.getMaximum()));
		}
	}
	
	// After Gitlab issue #12, we need to collect the uids and rids before transforming each entity
	private void collectAttributeIds(Map<String, Schema> oaAttributes, List<String> uids, List<String> rids) {
		for (Entry<String, Schema> oaAttribute : oaAttributes.entrySet())
			this.collectAttributeIds(oaAttribute.getValue(), oaAttribute.getKey(), uids, rids);
		log.debug("  entity uids: {}, rids: {}", uids, rids);
	}

	private void collectAttributeIds(Schema<?> oaAttribute, String name, List<String> uids, List<String> rids) {
		Map<String, Object> extensions = oaAttribute.getExtensions();
		if (extensions == null)
			return;
		for (Entry<String, Object> oaExtension : extensions.entrySet()) {
			if (OaExtensions.X_PK.equals(oaExtension.getKey()))
				uids.add(name);
			else if (OaExtensions.X_FK.equals(oaExtension.getKey()))
				rids.add(name);
		}
	}

	// Note: this has similar structure to the above methods, refactor?
	// this could receive the uids and rids determined before,
	// but this should require passing additional parameters over the call tree
	private void setAttributeIds(Schema<?> oaProperty, TdAttribute attribute, TdEntity entity) {
		Map<String, Object> extensions = oaProperty.getExtensions();
		if (extensions == null)
			return;
		for (Entry<String, Object> oaExtension : extensions.entrySet()) {
			log.debug("*update uids/rids: {}, column: {}", oaExtension.getKey(), attribute.getName());
			if (OaExtensions.X_PK.equals(oaExtension.getKey()))
				attribute.uid("true"); // does not check the value, the presence of x-pk is enough
			else if (OaExtensions.X_FK.equals(oaExtension.getKey())) {
				attribute.rid(oaExtension.getValue().toString());
				attribute.ridname(OaExtensions.getFkName(entity.getName(), attribute.getName()));
			}
		}
	}

}
