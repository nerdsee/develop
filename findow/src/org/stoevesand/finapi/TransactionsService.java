package org.stoevesand.finapi;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.stoevesand.finapi.model.Transaction;

public class TransactionsService {

	static final String URL = "https://sandbox.finapi.io/api/v1/transactions";

	public static List<Transaction> searchTransactions(Token userToken, Account account, int days) {

		Vector<Transaction> transactions = new Vector<Transaction>();

		long minDateMillis = System.currentTimeMillis();
		minDateMillis = minDateMillis - (days*24*60*60*1000);
		Date minDate = new Date(minDateMillis);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String minBankBookingDate = formatter.format(minDate);

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", userToken.getToken());
		webTarget = webTarget.queryParam("minBankBookingDate", minBankBookingDate);
		webTarget = webTarget.queryParam("accountIds", new Integer(account.getId()).toString());
		webTarget = webTarget.queryParam("view", "userView");
		//webTarget = webTarget.queryParam("isNew", "false");

		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(response);
			System.out.println("searchTransactions failed: " + status);
			eh.printErrors();
			return null;
		}

		try {
			JSONObject jo = new JSONObject(output);
			JSONArray json_txs = jo.getJSONArray("transactions");

			for (int i = 0; i < json_txs.length(); i++) {
				JSONObject json_account = json_txs.getJSONObject(i);
				Transaction transaction = new Transaction(json_account);
				transactions.add(transaction);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return transactions;

	}

}
