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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.finapi.model.User;

public class UsersService {

	static final String URL = "https://sandbox.finapi.io/api/v1/users";

	public static User createUser(Token clientToken) {
		User user = null;

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", clientToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		invocationBuilder = invocationBuilder.accept("application/json");
		Response response = invocationBuilder.post(Entity.json("{}"), Response.class);
		String output = response.readEntity(String.class);
		
		
		// Create Jersey client

//		MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
//		formData.add("body", "{}");

		//WebResource webResourcePost = client.resource(URL);
//		ClientResponse response = webResourcePost.queryParam("access_token", clientToken.getToken())
//				.accept("application/json")
//				.type("application/json").post(ClientResponse.class, "{}");

//		String output = response.getEntity(String.class);
		// System.out.println(output);

		int status = response.getStatus();
		if (status != 201) {
			ErrorHandler eh = new ErrorHandler(response);
			System.out.println("createUser failed: " + status);
			eh.printErrors();
			return null;
		}
		
		try {
			JSONObject json_user = new JSONObject(output);
			user = new User(json_user);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return user;
	}

}
