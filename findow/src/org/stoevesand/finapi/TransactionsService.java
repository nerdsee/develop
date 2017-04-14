package org.stoevesand.finapi;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.findow.model.Account;
import org.stoevesand.finapi.model.TransactionList;

public class TransactionsService {

	static final String URL = "https://sandbox.finapi.io/api/v1/transactions";

	public static TransactionList searchTransactions(String userToken, Long accountId, int days) throws ErrorHandler {

		TransactionList ret = null;

		long minDateMillis = System.currentTimeMillis();
		minDateMillis = minDateMillis - (days * 24 * 60 * 60 * 1000);
		Date minDate = new Date(minDateMillis);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String minBankBookingDate = formatter.format(minDate);

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(URL);
		webTarget = webTarget.queryParam("access_token", userToken);
		webTarget = webTarget.queryParam("perPage", 400);
		webTarget = webTarget.queryParam("minBankBookingDate", minBankBookingDate);
		
		// only use valid accountId
		if (accountId != null) {
			webTarget = webTarget.queryParam("accountIds", accountId.toString());
		}
		
		webTarget = webTarget.queryParam("view", "userView");
		// webTarget = webTarget.queryParam("isNew", "false");

		Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.accept("application/json");
		Response response = invocationBuilder.get();
		String output = response.readEntity(String.class);

		int status = response.getStatus();
		if (status != 200) {
			ErrorHandler eh = new ErrorHandler(output);
			System.out.println("searchTransactions failed: " + status);
			eh.printErrors();
			throw eh;
		}

		try {
			JSONObject jo = new JSONObject(output);
			ret = new TransactionList(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return ret;

	}

}
