package giis.tdrules.store.loader.oa;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import giis.tdrules.store.loader.shared.LoaderException;

/**
 * Management of storable and configurable basic http authentication.
 * 
 * After creation of an instance, it must be configured and then injected into a
 * Live Adapter. Configuration is made by entities, that may have one of these
 * authentication roles: 
 * - Provider: An entity that sets an username and password on his creation.
 * - Consumers: One or more entities require a basic authentication header be sent in a post.
 * 
 * The Live Adapter will call the 'processAuthentication' method before each
 * post depending on the entity that is sent:
 * - to a provider: stores the credentials.
 * - to a consumer: gets the appropriate credentials and creates the authentication headers
 */
public class OaBasicAuthStore {
	private static final Logger log = LoggerFactory.getLogger(OaBasicAuthStore.class);

	// configuration
	private String providerEntity = "";
	private String providerUserAttr = "";
	private String providerPassAttr = "";
	private Map<String, String> consumers = new HashMap<>(); // Map of entity, userAttr
	// stored authentication
	private Map<String, String> passwords = new HashMap<>(); // Map of userName, password

	/**
	 * Registers an entity as an authentication provider. Every post sent to this
	 * entity will store the credentials specified in the user and password
	 * attributes.
	 */
	public OaBasicAuthStore setProvider(String entity, String userAttr, String passAttr) {
		this.providerEntity = entity;
		this.providerUserAttr = userAttr;
		this.providerPassAttr = passAttr;
		return this;
	}

	/**
	 * Registers a set of entities as an authentication consumer. Every post sent to
	 * one of the configured entities will get the credentials of the username
	 * indicated in the user attribute and will set up the appropriate basic http
	 * authentication header.
	 */
	public OaBasicAuthStore addConsumer(String[] entities, String userAttr) {
		for (String entity : entities)
			consumers.put(entity, userAttr);
		return this;
	}
	
	public int getCredentialCount() {
		return passwords.size();
	}

	/**
	 * This method is called before each post, checks if the entity if an
	 * authentication provider or consumer and performs the appropriate actions:
	 * - If provider, stores the credentials
	 * - If consumer, configures the writer by adding the authentication header
	 */
	public void processAuthentication(String entity, String body, ApiWriter writer) {
		if (providerEntity.equalsIgnoreCase(entity)) {
			JsonNode json = parseBody(body);
			if (!json.has(providerUserAttr) || !json.has(providerPassAttr)) {
				log.error("Provider Entity '{}' is configured to set password from user '{}' and password '{}', but some key is missing from the payload: {}",
						entity, providerUserAttr, providerPassAttr, body);
				return;
			}
			String user=json.get(providerUserAttr).asText();
			String password=json.get(providerPassAttr).asText();
			log.debug("Saving password for future authentication requests of user: '{}'", user);
			passwords.put(user, password);
			return;
		}
		String consumerUser = consumers.get(entity);
		if (consumerUser != null) {
			JsonNode json = parseBody(body);
			if (!json.has(consumerUser)) {
				log.error("Consumer Entity '{}' is configured to get password from '{}', but this key is missing from the payload: '{}'",
						entity, consumerUser, body);
				return;
			}
			String user = json.get(consumerUser).asText();
			String password=passwords.get(user);
			if (password == null) {
				log.error("Consumer Entity '{}' is configured to get password from '{}', but a password for the user '{}' has not been recorded. Payload: {}",
						entity, consumerUser, user, body);
				return;
			}
			log.debug("Using saved password of user: {}", user);
			writer.addBasicAuth(user, password);
		}
	}

	private JsonNode parseBody(String body) {
		try {
			return new ObjectMapper().readTree(body);
		} catch (JsonProcessingException e) {
			throw new LoaderException(e);
		}
	}

}
