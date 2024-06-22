package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import java.util.Base64;

import org.junit.Test;

import giis.tdrules.store.loader.oa.ApiWriter;
import giis.tdrules.store.loader.oa.OaBasicAuthStore;

/**
 * Authentication using an auth provider
 */
public class TestOaAuthentication {

	// Credentials can be found:
	// - consumers with same/different username
	// - entity is not consumer
	@Test
	public void testBasicAuthenticationValid() {
		OaBasicAuthStore authenticator = new OaBasicAuthStore()
				.setProvider("eprov", "username", "password")
				.addConsumer(new String[] { "econs" }, "user")
				.addConsumer(new String[] { "econs2", "econs3" }, "user23");

		// Set the passwords
		ApiWriter writer = new ApiWriter();
		authenticator.processAuthentication("eprov", "{ 'id':1, 'username':'usr1', 'password':'abc'}".replace("'", "\""), writer);
		assertNoAuthHeader(writer);
		assertEquals(1, authenticator.getCredentialCount());
		authenticator.processAuthentication("eprov", "{ 'id':1, 'username':'usr2', 'password':'def'}".replace("'", "\""), writer);
		assertNoAuthHeader(writer);
		assertEquals(2, authenticator.getCredentialCount());
		authenticator.processAuthentication("eprov", "{ 'id':1, 'username':'usr3', 'password':'ghi'}".replace("'", "\""), writer);
		assertNoAuthHeader(writer);
		assertEquals(3, authenticator.getCredentialCount());

		// Valid credential
		writer = new ApiWriter();
		authenticator.processAuthentication("econs3", "{ 'id':13, 'user23':'usr3'}".replace("'", "\""), writer);
		assertAuthHeader(writer, "usr3", "ghi");

		writer = new ApiWriter();
		authenticator.processAuthentication("econs2", "{ 'id':12, 'user23':'usr2'}".replace("'", "\""), writer);
		assertAuthHeader(writer, "usr2", "def");

		writer = new ApiWriter();
		authenticator.processAuthentication("econs", "{ 'id':11, 'user':'usr1'}".replace("'", "\""), writer);
		assertAuthHeader(writer, "usr1", "abc");

		// No credential
		writer = new ApiWriter();
		authenticator.processAuthentication("noauth", "{ 'id':10, 'user':'usr1'}".replace("'", "\""), writer);
		assertNoAuthHeader(writer);
	}

	// Consumer data can't find credentials (does not throws exception, does not add headers)
	// - Username attribute not found
	// - Password value has not been stored
	@Test
	public void testBasicAuthenticationInvalid() {
		OaBasicAuthStore authenticator = new OaBasicAuthStore()
				.setProvider("eprov", "username", "password")
				.addConsumer(new String[] { "econs" }, "user");
		ApiWriter writer = new ApiWriter();
		authenticator.processAuthentication("eprov", "{ 'id':1, 'username':'usr1', 'password':'abc'}".replace("'", "\""), writer);
		assertEquals(1, authenticator.getCredentialCount());

		// No username attribute
		writer = new ApiWriter();
		authenticator.processAuthentication("econs", "{ 'id':11, 'owner':'usr1'}".replace("'", "\""), writer);
		assertNoAuthHeader(writer);

		// No password stored
		writer = new ApiWriter();
		authenticator.processAuthentication("econs", "{ 'id':12, 'user':'usr2'}".replace("'", "\""), writer);
		assertNoAuthHeader(writer);
	}

	// Invalid configuration (does not throws exception, does not add headers)
	// - Username attribute not found
	// - Password attribute not found
	@Test
	public void testBasicAuthenticationInvalidConfig() {
		OaBasicAuthStore authenticator = new OaBasicAuthStore()
				.setProvider("eprov", "username", "password")
				.addConsumer(new String[] { "econs" }, "user");
		ApiWriter writer = new ApiWriter();
		authenticator.processAuthentication("eprov", "{ 'id':1, 'username':'usr1'}".replace("'", "\""), writer);
		assertEquals(0, authenticator.getCredentialCount());
		authenticator.processAuthentication("eprov", "{ 'id':1, 'password':'abc'}".replace("'", "\""), writer);
		assertEquals(0, authenticator.getCredentialCount());
	}

	private void assertAuthHeader(ApiWriter writer, String user, String password) {
		String credentialToEncode = user + ":" + password;
		String header = "Basic " + Base64.getEncoder().encodeToString(credentialToEncode.getBytes());
		assertEquals("Header for " + credentialToEncode + " is not Authorization", "Authorization", writer.getHeaders().get(0)[0]);
		assertEquals("Credential for " + credentialToEncode + " does not match", header, writer.getHeaders().get(0)[1]);
	}

	private void assertNoAuthHeader(ApiWriter writer) {
		assertEquals("Authhorization header should not exist", 0, writer.getHeaders().size());
	}
}
