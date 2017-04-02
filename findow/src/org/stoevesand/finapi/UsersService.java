package org.stoevesand.finapi;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.FinapiUser;
import org.stoevesand.finapi.model.Token;

public class UsersService {

	static final String URL = "https://sandbox.finapi.io/api/v1/users";

	public static FinapiUser getUser(String userToken) throws ErrorHandler {

		FinapiUser user = null;

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", userToken);
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);
		
		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("getBankConnections failed: " + status);
			eh.printErrors();
			throw eh;
		}

		try {
			JSONObject json_user = new JSONObject(output);
			user = new FinapiUser(json_user);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return user;

	}

	public static FinapiUser createUser(Token clientToken, String id, String password) throws ErrorHandler {
		FinapiUser user = null;

		Client client = ClientBuilder.newClient();

		String message = generateCreateUserMessage(id, password);
		
		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", clientToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		invocationBuilder = invocationBuilder.accept("application/json");
		Response response = invocationBuilder.post(Entity.json(message), Response.class);
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 201) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("createUser failed: " + status);
			eh.printErrors();
			throw eh;
		}

		try {
			JSONObject json_user = new JSONObject(output);
			user = new FinapiUser(json_user);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return user;
	}

	private static String generateCreateUserMessage(String id, String password) {

		String ret = "";

		try {
			JSONObject jo = new JSONObject();

			if (id != null) {
				jo.put("id", id);
			}

			if (password != null) {
				jo.put("password", password);
			}

			ret = jo.toString();
			System.out.println("IDS: " + ret);
			// System.out.println("JO: " + jo.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return ret;
	}

}
