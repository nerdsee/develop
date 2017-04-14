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
import org.stoevesand.findow.model.Bank;
import org.stoevesand.findow.model.ErrorHandler;

public class BanksService {

	static final String URL = "https://sandbox.finapi.io/api/v1/banks";

	public static List<Bank> searchBanks(Token clientToken, String search) {

		Vector<Bank> banks = new Vector<Bank>();
		
		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", clientToken.getToken());
		webTarget = webTarget.queryParam("search", search);
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("searchBanks failed: " + status);
			eh.printErrors();
			return null;
		}

		try {
			JSONObject jo = new JSONObject(output);
			JSONArray json_banks = jo.getJSONArray("banks");

			for (int i = 0; i < json_banks.length(); i++) {
				JSONObject json_bank = json_banks.getJSONObject(i);
				Bank bank = new Bank(json_bank);
				banks.add(bank);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return banks;

	}

	public static Bank getBank(Token clientToken, int bankId) {

		Bank bank = null;

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL + "/" + bankId);
		webTarget = webTarget.queryParam("access_token", clientToken.getToken());
		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("getBank failed: " + status);
			eh.printErrors();
			return null;
		}
		
		try {
			JSONObject jo = new JSONObject(output);
			bank = new Bank(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return bank;

	}

}
