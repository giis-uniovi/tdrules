package giis.tdrules.client.oa;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.client.oa.shared.OaSchemaLogger;
import giis.tdrules.client.oa.transform.SchemaTransformer;
import giis.tdrules.openapi.model.TdSchema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Api to generate a TdSchema transformed from an OpenAPI Specification 
 * to be used in FPC Coverage Rules.
 * 
 * Parser: 
 *  - (last updated 2019) https://github.com/OpenAPITools/swagger-parser
 *  - (last updated https://github.com/swagger-api/swagger-parser
 * Specification: 2022
 *   - Basic structure: https://swagger.io/docs/specification/basic-structure/
 *   - Keywords: https://swagger.io/docs/specification/data-models/keywords/
 * Models:
 *   https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-models/src/main/java/io/swagger/v3/oas/models/OpenAPI.java
 *   https://github.com/swagger-api/swagger-core/tree/master/modules/swagger-models
 */
public class OaSchemaApi {
	protected static final Logger log=LoggerFactory.getLogger(OaSchemaApi.class);

	private String location;
	private OaSchemaIdResolver idResolver;
	private OaSchemaFilter filter;
	//custom logger for parse operations
	private OaSchemaLogger oaLogger= new OaSchemaLogger(this.getClass());
	
	/**
	 * Creates an instance for a given location (url or file) that contains the specification
	 * (note: does not check if location exists)
	 */
	public OaSchemaApi(String location) {
		this.location=location;
		this.idResolver = null;
		this.filter = null;
	}
	
	/**
	 * Sets an id resolver to customize the default uid and rid determination
	 * that uses the custom vendor extensions
	 */
	public OaSchemaApi setIdResolver(OaSchemaIdResolver resolver) {
		this.idResolver = resolver;
		return this;
	}

	/**
	 * Sets an OaSchemaFilter to remove entities and attributes from the Open Api Schema
	 * before transformation
	 */
	public OaSchemaApi setFilter(OaSchemaFilter filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * Gets the TdSchema object with all tables defined at the current location
	 * (note: fails if location is invalid or contains an invalid specification)
	 */
	@SuppressWarnings("rawtypes")
	public TdSchema getSchema() {
		OpenAPI openApi = parseOpenApi();

		Map<String, Schema> oaSchemas = getOaSchemas(openApi);
		Map<String, PathItem> oaPaths = getOaPaths(openApi);
		log.info("Parse and transform OpenApi spec, location: {}", location);

		// Before transform: applies filters to the entities and attributes in the oaSchema
		if (filter != null)
			oaSchemas = this.filter.filter(oaSchemas);

		// Before transform: resolves the uids and rids of each schema object.
		// If no resolver is configured, it will rely on the vendor extensions
		// already included in the schema
		if (idResolver != null)
			idResolver.resolve(oaSchemas);

		// Main transformation to get the DbSchema
		SchemaTransformer transformer = new SchemaTransformer(oaSchemas, oaPaths, oaLogger);
		transformer.transform();
		if (!"".equals(oaLogger.toString()))
			log.warn("Schema transformation finished with errors or warnings: \n{}", oaLogger);

		return transformer.getTdSchema();
	}

	public String getOaLogs() {
		return this.oaLogger.toString();
	}

	private OpenAPI parseOpenApi() {
		ParseOptions parseOptions = new ParseOptions();
		parseOptions.setResolve(true); // needed to resolve references to schema objects
		return new OpenAPIV3Parser().read(location, null, parseOptions);
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Schema> getOaSchemas(OpenAPI api) {
		return api.getComponents().getSchemas();
	}

	private Map<String, PathItem> getOaPaths(OpenAPI api) {
		return api.getPaths();
	}

}
