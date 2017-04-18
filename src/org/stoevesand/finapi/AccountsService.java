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
import org.stoevesand.finapi.model.Token;
import org.stoevesand.findow.model.Account;
import org.stoevesand.findow.model.ErrorHandler;

public class AccountsService {

	static final String URL = "https://sandbox.finapi.io/api/v1/accounts";

	public static List<Account> searchAccounts(String userToken, int connectionId) throws ErrorHandler {

		Vector<Account> accounts = new Vector<Account>();

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", userToken);
		if (connectionId > 0) {
			webTarget = webTarget.queryParam("bankConnectionIds", "" + connectionId);
		}
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			throw eh;
		}

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

	public static void refreshAccount(String userToken, Account account) throws ErrorHandler {

		Vector<Account> accounts = new Vector<Account>();

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL + "/" + account.getSourceId());
		webTarget = webTarget.queryParam("access_token", userToken);
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(status, output);
			throw eh;
		}

		try {
			JSONObject jo = new JSONObject(output);
			account.update(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void deleteAccount(String userToken, Account account) throws ErrorHandler {

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL + "/" + account.getSourceId());
		webTarget = webTarget.queryParam("access_token", userToken);
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.delete();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(status, output);
			throw eh;
		}

	}

}
