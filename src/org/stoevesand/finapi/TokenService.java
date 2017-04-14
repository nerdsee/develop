package org.stoevesand.finapi;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.findow.model.ErrorHandler;

public class TokenService {

	static final String POST_URL = "https://sandbox.finapi.io/oauth/token";
	static final long VALIDITY_BUFFER_SECONDS = 200;

	public static Token requestUserToken(Token clientToken, String username, String password) throws ErrorHandler {
		//System.out.println("Request User Token.");

		Token user_token = null;
		
		Client client = ClientBuilder.newClient();

		MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
		formData.add("client_id", clientToken.getId());
		formData.add("client_secret", clientToken.getSecret());
		formData.add("username", username);
		formData.add("password", password);
		formData.add("grant_type", "password");

		WebTarget webTarget = client.target(POST_URL);
		//webTarget = webTarget.queryParam("access_token", userToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.post(Entity.form(formData), Response.class);
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("requestUserToken failed: " + status);
			eh.printErrors();
			throw eh;
		}

		try {
			JSONObject jo = new JSONObject(output);
			user_token = new Token(username, password, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return user_token;

	}

	public static Token requestClientToken(String client_id, String client_secret) throws ErrorHandler {
		//System.out.println("Request Token.");

		Token client_token = null;

		Client client = ClientBuilder.newClient();
		
		MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
		formData.add("client_id", client_id);
		formData.add("client_secret", client_secret);
		formData.add("grant_type", "client_credentials");

		WebTarget webTarget = client.target(POST_URL);
		//webTarget = webTarget.queryParam("access_token", userToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.post(Entity.form(formData), Response.class);
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("requestClientToken failed: " + status);
			eh.printErrors();
			throw eh;
		}

		try {
			JSONObject jo = new JSONObject(output);
			client_token = new Token(client_id, client_secret, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return client_token;

	}
}
