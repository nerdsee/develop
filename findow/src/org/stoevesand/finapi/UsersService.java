package org.stoevesand.finapi;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.finapi.model.User;

public class UsersService {

	static final String URL = "https://sandbox.finapi.io/api/v1/users";

	public static User createUser(Token clientToken, String id, String password) throws ErrorHandler {
		User user = null;

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
			user = new User(json_user);
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
