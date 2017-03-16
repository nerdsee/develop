package org.stoevesand.finapi;

import java.util.List;
import java.util.Vector;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.Account;
import org.stoevesand.finapi.model.Token;

public class AccountsService {

	static final String URL = "https://sandbox.finapi.io/api/v1/accounts";

	public static List<Account> searchAccounts(Token userToken, int connectionId) {

		Vector<Account> accounts = new Vector<Account>();

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", userToken.getToken());
		if (connectionId > 0) {
			webTarget = webTarget.queryParam("bankConnectionIds", "" + connectionId);
		}
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		// webTarget.register(FilterForExampleCom.class);
		// WebTarget resourceWebTarget = webTarget.path("resource");

		// WebTarget helloworldWebTarget = resourceWebTarget.path("helloworld");
		// WebTarget helloworldWebTargetWithQueryParam =
		// helloworldWebTarget.queryParam("greeting", "Hi World!");
		// helloworldWebTargetWithQueryParam.request(MediaType.TEXT_PLAIN_TYPE);
		// invocationBuilder.header("some-header", "true");
		// System.out.println(response.getStatus());
		// System.out.println(response.readEntity(String.class));

		// Create Jersey client
		// ClientConfig clientConfig = new DefaultClientConfig();
		// Client client = Client.create(clientConfig);
		// WebResource webResourcePost = client.resource(URL);
		// webResourcePost = webResourcePost.queryParam("access_token",
		// userToken.getToken());
		// if (connectionId > 0) {
		// webResourcePost = webResourcePost.queryParam("bankConnectionIds", ""
		// + connectionId);
		// }
		// ClientResponse response =
		// webResourcePost.accept("application/json").get(ClientResponse.class);

		// String output = response.getEntity(String.class);
		// System.out.println(output);

		try {
			JSONObject jo = new JSONObject(output);
			JSONArray json_accounts = jo.getJSONArray("accounts");

			for (int i = 0; i < json_accounts.length(); i++) {
				JSONObject json_account = json_accounts.getJSONObject(i);
				Account account = new Account(json_account);
				accounts.add(account);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return accounts;

	}

	public static Account getAccount(Token userToken, int accountId) {

		Account account = null;

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL + "/" + accountId);
		webTarget = webTarget.queryParam("access_token", userToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		// Create Jersey client
		// ClientConfig clientConfig = new DefaultClientConfig();
		// Client client = Client.create(clientConfig);

		// WebResource webResourcePost = client.resource(URL + "/" + accountId);
		// ClientResponse response = webResourcePost.queryParam("access_token",
		// userToken.getToken()).accept("application/json").get(ClientResponse.class);

		// String output = response.getEntity(String.class);
		// System.out.println(output);

		try {
			JSONObject jo = new JSONObject(output);
			account = new Account(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return account;

	}

}
