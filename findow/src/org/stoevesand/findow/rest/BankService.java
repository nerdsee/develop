package org.stoevesand.findow.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.stoevesand.finapi.BanksService;
import org.stoevesand.finapi.TokenService;
import org.stoevesand.finapi.client.FinbookClient;
import org.stoevesand.finapi.model.Bank;
import org.stoevesand.finapi.model.Token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class BankService {

	static final String client_id = "7fbb36a6-e886-41fc-9a0a-3ead413cddb8";
	static final String client_secret = "3122d123-fdeb-498c-93c4-5eda3c10d396";
	static final String data_decryption_key = "e0c82a81c6886460f109fe5348c58884";

	Token clientToken;

	public BankService() {
		clientToken = TokenService.requestClientToken(client_id, client_secret);
	}

	@Path("/banks/{search}")
	@GET
	@Produces("application/json")
	public String getBank(@PathParam("search") String search) {

		validateToken();

		List<Bank> banks = BanksService.searchBanks(clientToken, search);

		String result = "";
		try {
			result = new ObjectMapper().writeValueAsString(banks);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return result;
	}

	private void validateToken() {
		if (!clientToken.isValid()) {
			System.out.println("Refresh Token.");
			clientToken = TokenService.requestClientToken(client_id, client_secret);
		} else {
			System.out.println("Token still valid.");
		}

	}
}