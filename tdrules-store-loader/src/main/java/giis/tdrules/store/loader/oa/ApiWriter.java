package giis.tdrules.store.loader.oa;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import giis.tdrules.store.loader.LoaderException;

/**
 * Utilidades para envio (y recepcion) de datos a un api
 */
public class ApiWriter {
	// Headers to sent (in addition to Content-type: application/json)
	private List<String[]> headers = new ArrayList<>();

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
	
    public HttpResponse post(String url, String requestBody, boolean usePut) {
    	HttpEntityEnclosingRequestBase post = usePut ? new HttpPut(url) : new HttpPost(url);
		try { //el post requiere confeccionar el body como StringEntity
			StringEntity stringEntity = new StringEntity(requestBody);
			post.getRequestLine();
			post.setEntity(stringEntity);
		} catch (UnsupportedEncodingException e) {
            throw new LoaderException(e);
 		}
        return apiExecute(post);
    }
    
    public HttpResponse get(String url) {
        HttpGet get = new HttpGet(url);
    	return apiExecute(get);
    }

    public HttpResponse delete(String url) {
    	HttpDelete delete=new HttpDelete(url);
    	return apiExecute(delete);
    }
    
    private HttpResponse apiExecute(HttpUriRequest request) {
        HttpClient client = HttpClientBuilder.create().build();
        request.setHeader("Content-type", "application/json");
        for (String[] header : this.headers) // adds other headers if configured
        	request.setHeader(header[0], header[1]);
        org.apache.http.HttpResponse response=null;

        try {
            response=client.execute(request);
        } catch (Exception e) {
            throw new LoaderException(e);
        }
        return response;
    }

    //utilidades para recuperar la informacion del HttpResponse
    
    public int getStatus(HttpResponse response) {
    	return response.getStatusLine().getStatusCode();
    }
    public String getReason(HttpResponse response) {
    	return response.getStatusLine().getReasonPhrase();
    }
    public String getBody(HttpResponse response) {
		String body;
		try {
			body = EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			throw new LoaderException(e);
		}
		return body;
    }
}
