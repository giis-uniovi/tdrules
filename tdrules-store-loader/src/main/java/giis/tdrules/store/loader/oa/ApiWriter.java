package giis.tdrules.store.loader.oa;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import giis.tdrules.store.loader.LoaderException;

/**
 * Utility to send/receive json data though an API
 */
public class ApiWriter {
	// Headers to sent (in addition to Content-type: application/json)
	private List<String[]> headers = new ArrayList<>();

	public ApiWriter reset() {
		headers = new ArrayList<>();
		return this;
	}
	
	public ApiWriter addHeader(String key, String value) {
		headers.add(new String[] { key, value });
		return this;
	}

	public List<String[]> getHeaders() {
		return headers;
	}

	public ApiWriter addBasicAuth(String user, String password) {
		String credentialToEncode = user + ":" + password;
		String encodedCredential = "Basic " + Base64.getEncoder().encodeToString(credentialToEncode.getBytes());
		return addHeader("Authorization", encodedCredential);
	}

	public ApiResponse post(String url, String requestBody, boolean usePut) {
		HttpUriRequestBase post = usePut ? new HttpPut(url) : new HttpPost(url);
		StringEntity stringEntity = new StringEntity(requestBody);
		post.getRequestUri();
		post.setEntity(stringEntity);
		return apiExecute(post);
	}

	public ApiResponse get(String url) {
		HttpGet get = new HttpGet(url);
		return apiExecute(get);
	}

	public ApiResponse delete(String url) {
		HttpDelete delete = new HttpDelete(url);
		return apiExecute(delete);
	}

	private ApiResponse apiExecute(HttpUriRequest request) {
		request.setHeader("Content-type", "application/json");
		for (String[] header : this.headers) // adds other headers if configured
			request.setHeader(header[0], header[1]);

		ApiResponse result;
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			result = client.execute(request, response -> 
				new ApiResponse(response.getCode(), response.getReasonPhrase(),
					EntityUtils.toString(response.getEntity())
				));
		} catch (Exception e) {
			throw new LoaderException(e);
		}
		return result;
	}

}
