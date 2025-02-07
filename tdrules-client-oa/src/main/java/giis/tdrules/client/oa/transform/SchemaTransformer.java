package giis.tdrules.client.oa.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.client.oa.shared.OaSchemaLogger;
import giis.tdrules.client.oa.shared.OaUtil;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdCheck;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Converts a previously parsed OpenApi schema specification into the TbSchema
 * model needed to generate FPC Coverage Rules
 */
@SuppressWarnings("rawtypes")
public class SchemaTransformer {
	protected static final Logger log = LoggerFactory.getLogger(SchemaTransformer.class);

	private Map<String, Schema> oaSchemas; // parsed OpenApi source for component/schemas
	private Map<String, PathItem> oaPaths; // parsed OpenApi source for paths
	private TdSchema tdSchema; // target schema
	private CompositeTransformer ct; // manages transformation of composites (objects and arrays)
	private UpstreamAttribute upstreamAttr;
	private OaSchemaLogger oaLogger; // specialized logger to store important messages

	public SchemaTransformer(Map<String, Schema> oaSchemas, Map<String, PathItem> oaPaths, OaSchemaLogger oaLogger) {
		this.oaSchemas = oaSchemas;
		this.oaPaths = oaPaths;
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
	public SchemaTransformer transform(boolean onlyEntitiesInPaths) {
		// Before processing every item in the schema, gets an additional transformer
		// to store the required information about paths (endpoint paths, path parameters)
		PathTransformer pathTransformer = new PathTransformer(oaPaths, oaLogger);
		
		for (Entry<String, Schema> oaSchema : oaSchemas.entrySet()) {
			if (onlyEntitiesInPaths && !pathTransformer.containsEntity(oaSchema.getKey())) {
				log.trace("Skip OA schema object: {} as it is not in any relevant path", oaSchema.getKey());
				continue;
			}
			log.debug("Transform OA schema object: {}", oaSchema.getKey());
			TdEntity entity = getEntity(oaSchema.getKey(), oaSchema.getValue(), pathTransformer, null);
			// This entity must store the Ddls that indicate the paths, if any
			pathTransformer.addDdls(entity, "post");
			pathTransformer.addDdls(entity, "put");
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
			oaLogger.warn(log, "Can't get the rid for array {} because it has not any upstream with uid", entity.getName());
		else {
			ct.linkArrayToContainerEntity(entity, upstreamWithUid);
			// If the referenced entity is not the adjacent upstream, the mermaid drawing will show incorrect relations.
			// Sets an extended attribute to indicate the correct entity to link
			upstreamAttr.setMermaidRid(entity);
		}
	}

	TdEntity getEntity(String name, Schema<?> oaSchema, PathTransformer pathTransformer, TdEntity upstream) {
		TdEntity entity = createNewEntity(name, upstream);
		Map<String, Schema> oaAttributes = oaSchema.getProperties();
		if (oaAttributes == null) {
			oaLogger.warn(log, "Open Api schema for {} does not have any property, generated entity will be empty", name);
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
		
		// create an attribute for each post path parameter that is rid
		addPathAttributes(pathTransformer, entity);
		
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
		log.trace("*Add entity if does not exists {}", entity.getName());
		tdSchema.addEntitiesItemIfNotExist(entity);
	}

	TdAttribute getAttribute(String name, Schema<?> oaProperty, TdEntity entity) {
		log.trace("  property: {}", name);
		TdAttribute attribute = new TdAttribute().name(name);
		setAttributeType(oaProperty, attribute, entity);
		setAttributeDescriptors(oaProperty, attribute, entity);
		setAttributeIds(oaProperty.getExtensions(), attribute, entity);
		return attribute;
	}

	private void addAttribute(Entry<String, Schema> oaAttribute, TdEntity entity) {
		TdAttribute attribute = getAttribute(oaAttribute.getKey(), oaAttribute.getValue(), entity);
		entity.addAttributesItem(attribute);
	}
	
	private void addPathAttributes(PathTransformer pathTransformer, TdEntity entity) {
		if (pathTransformer != null) {
			List<Parameter> oaParams = pathTransformer.getPathParams(entity.getName());
			for (Parameter oaParam : oaParams) {
				if (OaUtil.isObject(oaParam.getSchema()) || OaUtil.isArray(oaParam.getSchema())) {
					log.debug("  ignore non primitive attribute {} from path parameters", oaParam.getName());
				}
				else if (oaParam.getExtensions() == null || !oaParam.getExtensions().containsKey(OaExtensions.X_FK)) {
					log.debug("  ignore non rid attribute {} from path parameters", oaParam.getName());
				} else {
					log.debug("  add attribute {} from path parameters", oaParam.getName());
					TdAttribute attr = this.getAttribute(oaParam.getName(), oaParam.getSchema(), entity);
					// When the second param in getAttribute is a property in the schema,
					// the method gets all extensions, but when it is a parameter,
					// the schema is unable to get extensions, they must be taken from the parameter
					setAttributeIds(oaParam.getExtensions(), attr, entity);
					entity.addAttributesItem(attr);
				}
			}
		}
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
		if (refProperty == null) {
			handleUndefinedOaRef(entity, oaProperty.get$ref());
			return;
		}
		TdEntity refEntity = getEntity(refProperty.getName(), refProperty, null, entity);
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

	// Obtains the model of the referenced object, may be null if not found
	Schema resolveOaRef(Schema<?> objectModel) {
		log.debug("*resolve oaRef: {}", objectModel.get$ref());
		String ref = objectModel.get$ref();
		String name = ref.replace("#/components/schemas/", "");
		objectModel = oaSchemas.get(name); // replaces with the resolved object
		// If not found, creates a log entry instead of fail.
		// As the returned value will be null, the caller should manage this situation
		if (objectModel == null)
			oaLogger.warn(log, "Can't resolve oaRef: {}", ref);
		else
			objectModel.name(name);
		return objectModel;
	}
	// when a ref can't be resolved, this method can be used to add
	// the names of the unresolved refs to the entity extended attributes
	void handleUndefinedOaRef(TdEntity entity, String ref) {
		String name = ref.replace("#/components/schemas/", "");
		String current = entity.getExtendedItem(OaExtensions.UNDEFINED_REFS);
		String updated = (current == null ? "" : current + ",") + name;
		entity.putExtendedItem(OaExtensions.UNDEFINED_REFS, updated);
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
	private void setAttributeIds(Map<String, Object> extensions, TdAttribute attribute, TdEntity entity) {
		if (extensions == null)
			return;
		for (Entry<String, Object> oaExtension : extensions.entrySet()) {
			log.trace("Update uids/rids: {}, column: {}", oaExtension.getKey(), attribute.getName());
			if (OaExtensions.X_PK.equals(oaExtension.getKey()))
				attribute.uid("true"); // does not check the value, the presence of x-pk is enough
			else if (OaExtensions.X_FK.equals(oaExtension.getKey())) {
				attribute.rid(oaExtension.getValue().toString());
				attribute.ridname(OaExtensions.getFkName(entity.getName(), attribute.getName()));
			}
		}
	}

}
