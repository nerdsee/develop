package org.stoevesand.finapi;

import java.util.List;
import java.util.Vector;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.Account;
import org.stoevesand.finapi.model.BankConnection;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.finapi.model.Transaction;
import org.stoevesand.finapi.model.User;

public class BankConnectionsService {

	static final String URL = "https://sandbox.finapi.io/api/v1/bankConnections";

	public static BankConnection importConnection(String userToken, int bankId, String bankingUserId, String bankingPin) throws ErrorHandler {
		BankConnection bc = null;
		
		
		Client client = ClientBuilder.newClient();

		String message = generateImportConnectionMessage(bankId, bankingUserId, bankingPin);

		WebTarget webTarget = client.target(URL + "/import");
		webTarget = webTarget.queryParam("access_token", userToken);
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		invocationBuilder = invocationBuilder.accept("application/json");
		Response response = invocationBuilder.post(Entity.json(message), Response.class);
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 201) {
			ErrorHandler eh = new ErrorHandler(output);
			throw eh;
		}

		try {
			JSONObject jo = new JSONObject(output);
			bc = new BankConnection(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return bc;
	}

	public static boolean deleteConnection(Token userToken, int id) throws ErrorHandler {

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL + "/" + id);
		webTarget = webTarget.queryParam("access_token", userToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.delete();
		String output = response.readEntity(String.class);
		
		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("getBankConnections failed: " + status);
			throw eh;
		}

		return true;

	}

	public static List<BankConnection> getBankConnections(String userToken) throws ErrorHandler {

		Vector<BankConnection> connections = new Vector<BankConnection>();

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

		System.out.println("Status: " + response.getStatus());
		System.out.println(output);

		try {
			JSONObject jo = new JSONObject(output);
			JSONArray json_txs = jo.getJSONArray("connections");

			for (int i = 0; i < json_txs.length(); i++) {
				JSONObject json_account = json_txs.getJSONObject(i);
				BankConnection connection = new BankConnection(json_account);
				connections.add(connection);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return connections;

	}

	/**
	 * @param userToken
	 * @param id
	 * @param bankConnection
	 *            Wenn eine Connection übergeben wird, wird sie aktualisiert.
	 *            Ansonsten wird eine neues Connection Objekt erzeugt
	 * @return
	 */
	public static BankConnection getBankConnection(Token userToken, int id, BankConnection bankConnection) {

		
		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL + "/" + id);
		webTarget = webTarget.queryParam("access_token", userToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("getBankConnection failed: " + status);
			eh.printErrors();
			return null;
		}

		try {
			JSONObject jo = new JSONObject(output);
			if (bankConnection == null) {
				bankConnection = new BankConnection(jo);
			} else {
				bankConnection.update(jo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return bankConnection;

	}

	private static String generateImportConnectionMessage(int bankId, String bankingUserId, String bankingPin) {

		String ret = "";

		try {
			JSONObject jo = new JSONObject();
			jo.put("bankId", bankId);
			jo.put("bankingUserId", bankingUserId);
			jo.put("bankingPin", bankingPin);
			ret = jo.toString();
			// System.out.println("JO: " + jo.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static BankConnection updateConnection(Token userToken, int bankConnectionId, String connectionPin) throws ErrorHandler {
		BankConnection bc = null;

		Client client = ClientBuilder.newClient();

		String message = generateUpdateConnectionMessage(bankConnectionId, connectionPin);

		WebTarget webTarget = client.target(URL + "/update");
		webTarget = webTarget.queryParam("access_token", userToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		invocationBuilder = invocationBuilder.accept("application/json");
		Response response = invocationBuilder.post(Entity.json(message), Response.class);
		String output = response.readEntity(String.class);
		
		// Create Jersey client
		//WebResource webResourcePost = client.resource(URL + "/update");
		//ClientResponse response = webResourcePost.queryParam("access_token", userToken.getToken()).accept("application/json").type("application/json").post(ClientResponse.class, message);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			throw eh;
		}

		System.out.println(output);

		try {
			JSONObject jo = new JSONObject(output);
			bc = new BankConnection(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return bc;
	}

	private static String generateUpdateConnectionMessage(int bankConnectionId, String bankingPin) {

		String ret = "";

		try {
			JSONObject jo = new JSONObject();
			jo.put("bankConnectionId", bankConnectionId);
			jo.put("bankingPin", bankingPin);
			ret = jo.toString();
			// System.out.println("JO: " + jo.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return ret;
	}

}
