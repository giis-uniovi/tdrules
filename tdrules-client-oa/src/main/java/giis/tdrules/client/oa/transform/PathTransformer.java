package giis.tdrules.client.oa.transform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.client.oa.shared.OaSchemaLogger;
import giis.tdrules.client.oa.shared.TransformException;
import giis.tdrules.model.shared.ModelUtil;
import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

/**
 * Tuples of paths, path parameters, operations and entities (both request and response)
 * from the OpenAPI model, indexed by entity name.
 * Currently, does not track if each tuple is related to a request or response
 * 
 * These operations are scanned and stored internally during instantiation
 * and can be later used during the transformation of an entity to determine
 * the endpoint where POST for entities must be sent
 */
public class PathTransformer {
	private static final String POST = "post";
	private static final String PUT = "put";
	private static final String REQUEST = "request";
	private static final String RESPONSE = "response";

	protected static final Logger log = LoggerFactory.getLogger(PathTransformer.class);

	// Holds the path related data from the oaSchema for each entity in the schema
	// (organized by the entity name)
	private EntityPaths entityPaths = new EntityPaths();
	private OaSchemaLogger oaLogger;
	
	public class EntityPaths {
		Map<String, List<EntityPath>> epaths = new LinkedHashMap<>();
		
		public List<EntityPath> get(String entityName) {
			List<EntityPath> items = epaths.get(entityName);
			return items == null ? new ArrayList<>() : items;
		}
		public void add(EntityPath epath) {
			List<EntityPath> items = epaths.get(epath.entityName);
			if (items == null) { //initialize entry if it is new
				items = new ArrayList<>();
				epaths.put(epath.entityName, items);
			}			
			if (!containedIn(epath, items)) // Add only if not duplicated
				items.add(epath);
		}
		private boolean containedIn(EntityPath epath, List<EntityPath> epaths) {
			for (EntityPath item : epaths) 
				if (epath.entityName.equals(item.entityName) && epath.method.equals(item.method) && epath.path.equals(item.path))
					return true;
			return false;
		}
		public boolean containsEntity(String entityName) {
			return epaths.containsKey(entityName);
		}
	}

	public class EntityPath {
		String entityName;
		String method;
		String path;
		List<Parameter> oaParams;
	}

	public PathTransformer(Map<String, PathItem> pathItems, OaSchemaLogger oaLogger) {
		this.oaLogger = oaLogger;
		this.initialize(pathItems);
	}

	private void initialize(Map<String, PathItem> pathItems) {
		log.debug("Initialize endpoint paths:");
		if (pathItems == null)
			return;
		for (Entry<String, PathItem> pathSchema : pathItems.entrySet()) {
			addEntityPath(POST, REQUEST, pathSchema.getKey(), pathSchema.getValue());
			addEntityPath(PUT, REQUEST, pathSchema.getKey(), pathSchema.getValue());
			addEntityPath(POST, RESPONSE, pathSchema.getKey(), pathSchema.getValue());
			addEntityPath(PUT, RESPONSE, pathSchema.getKey(), pathSchema.getValue());
		}
	}
	
	public boolean containsEntity(String entityName) {
		return entityPaths.containsEntity(entityName);
	}

	// Adds an entity path object if it corresponds to a method that accepts a body
	// with media type application/json
	private void addEntityPath(String method, String requestOrResponse, String oaKey, PathItem oaPath) {
		String pathString = method + " " + oaKey + " (" + requestOrResponse + ")";
		log.trace("Check {}", pathString);
		Operation operation = getOaOperation(oaPath, method);
		if (operation == null)
			return;

		String ref = getBodyEntityRef(pathString, operation, requestOrResponse);
		if (ref != null) {
			log.trace("Found {}, body ref: {}", method, ref);
			String entityName = ref.replace("#/components/schemas/", "");
			log.debug("Add {} with path {} to entity {}", method, oaKey, entityName);
			EntityPath entityPath = new EntityPath();
			entityPath.entityName = entityName;
			entityPath.method = method;
			entityPath.path = oaKey;
			entityPath.oaParams = operation.getParameters();
			this.entityPaths.add(entityPath);
		}
	}

	private String getBodyEntityRef(String pathString, Operation operation, String requestOrResponse) {
		Content content = getOaBodyContent(pathString, operation, requestOrResponse);
		if (content == null)
			return null;
		MediaType media = getOaMediaType(pathString, content);
		if (media == null)
			return (String) warnAndReturnNull("Can't find an application/json media type for {}", pathString);
		Schema<?> schema = media.getSchema();
		if (schema == null)
			return (String) warnAndReturnNull("Can't find a schema definition for {}", pathString);
		String ref = schema.get$ref();
		if (ref == null)
			return (String) warnAndReturnNull("Can't find a schema with a valid ref for {}", pathString);
		return ref;
	}
	
	private Operation getOaOperation(PathItem oaPath, String method) {
		if (POST.equalsIgnoreCase(method))
			return oaPath.getPost();
		else if (PUT.equalsIgnoreCase(method))
			return oaPath.getPut();
		else
			throw new TransformException("Operation not handled for method " + method);
	}
	private Content getOaBodyContent(String pathString, Operation operation, String requestOrResponse) {
		if (REQUEST.equalsIgnoreCase(requestOrResponse)) {
			RequestBody body = operation.getRequestBody();
			return body == null 
				? (Content) warnAndReturnNull("Can't find the request body content for {}", pathString)
				: body.getContent();
		} else if (RESPONSE.equalsIgnoreCase(requestOrResponse)) {
			// must select the first 2xx response using this method, null if not found
			return getOaResponseContent(pathString, operation);
		} else
			throw new TransformException("Get body not handled for " + requestOrResponse);
	}
	private MediaType getOaMediaType(String pathString, Content content) {
		MediaType media = content.get("application/json");
		if (media == null) {
			// Some specifications (market) do not specify media type for each response,
			// but they are read as a media type range */* (could be also application/*), try this
			media = content.get("*/*"); 
			if (media != null)
				warnAndReturnNull("Accepting media type range */* as media type for {}", pathString);

		}
		return media;
	}
	private Content getOaResponseContent(String pathString, Operation operation) {
		ApiResponses responses = operation.getResponses();
		if (responses == null)
			return (Content) warnAndReturnNull("Can't find the responses for {}", pathString);
		for (Entry<String, ApiResponse> response : responses.entrySet())
			if (response.getKey().substring(0, 1).equals("2")) { // first 2XX response
				Content content = response.getValue().getContent();
				return content == null
					? (Content) warnAndReturnNull("Can't find the {} response content for {}", response.getKey(), pathString)
					: content;
			}
		return (Content) warnAndReturnNull("Can't find any 2XX response for {}", pathString);
	}
	
	/**
	 * Finds the paths that correspond to a given entity and method and adds the
	 * corresponding Ddl items to the model
	 */
	void addDdls(TdEntity entity, String method) {
		for (EntityPath entityPath : this.entityPaths.get(entity.getName())) {
			if (entityPath.entityName.equalsIgnoreCase(entity.getName()) && entityPath.method.equals(method)) {
				entity.addDdlsItem(new Ddl().command(method).query(entityPath.path));
				// note that there could be more than one item for a given entity and method.
			}
		}
	}
	
	/**
	 * Returns a list with all path parameters that are associated to the post of an entity
	 */
	List<Parameter> getPathParams(String entityName) {
		List<Parameter> oaParams = new ArrayList<>();
		for (EntityPath entityPath : entityPaths.get(entityName)) {
			if (entityPath.entityName.equalsIgnoreCase(entityName) && entityPath.method.equals(POST)) {
				for (Parameter oaParam : ModelUtil.safe(entityPath.oaParams)) {
					if (oaParam.getIn().equals("path")) {
						oaParams.add(oaParam);
					}
				}
			}
		}
		return oaParams;
	}

	private Object warnAndReturnNull(String message, Object... args) {
		oaLogger.warn(log, message, args);
		return null;
	}

}
