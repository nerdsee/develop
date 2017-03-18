package org.stoevesand.findow.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.stoevesand.finapi.AccountsService;
import org.stoevesand.finapi.BankConnectionsService;
import org.stoevesand.finapi.BanksService;
import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.MandatorAdminService;
import org.stoevesand.finapi.TokenService;
import org.stoevesand.finapi.TransactionsService;
import org.stoevesand.finapi.UsersService;
import org.stoevesand.finapi.model.Account;
import org.stoevesand.finapi.model.Bank;
import org.stoevesand.finapi.model.BankConnection;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.finapi.model.Transaction;
import org.stoevesand.finapi.model.User;
import org.stoevesand.finapi.model.UserInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Path("/")
public class RestServer10 {

	static final String client_id = "7fbb36a6-e886-41fc-9a0a-3ead413cddb8";
	static final String client_secret = "3122d123-fdeb-498c-93c4-5eda3c10d396";
	static final String data_decryption_key = "e0c82a81c6886460f109fe5348c58884";

	// finAPI SANDBOX Admin-Client:
	static final String admin_client_id = "05e712de-dbb9-4b31-9889-ead3c151c54f";
	static final String admin_client_secret = "f3e251db-be41-46cc-a438-ecd55e4a7abc";
	static final String admin_data_decryption_key = "eeb4561adf992fc44313468e4035ccac";

	Token clientToken;
	Token adminToken;

	public RestServer10() {
		try {
			clientToken = TokenService.requestClientToken(client_id, client_secret);
			adminToken = TokenService.requestClientToken(admin_client_id, admin_client_secret);
		} catch (ErrorHandler e) {
			e.printStackTrace();
		}
	}

	@Path("/banks/{search}")
	@GET
	@Produces("application/json")
	public String getBank(@PathParam("search") String search) {

		validateToken();

		List<Bank> banks = BanksService.searchBanks(clientToken, search);

		String result = generateJsonResponse(banks, "banks");

		return result;
	}

	@Path("/users")
	@GET
	@Produces("application/json")
	public String getUserInfos() {

		validateAdminToken();

		List<UserInfo> userInfos = MandatorAdminService.getUsers(adminToken);

		String result = generateJsonResponse(userInfos, "userinfos");

		return result;
	}

	@Path("/user/{id}")
	@GET
	@Produces("application/json")
	public String getUser(@PathParam("id") String id, @HeaderParam("password") String password) {

		String result = "";
		validateToken();

		User user = new User(id, password);
		try {
			Token userToken = user.getToken(clientToken);
			result = generateJsonResponse(userToken);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/user/{id}")
	@POST
	@Produces("application/json")
	public String createUser(@PathParam("id") String id, @HeaderParam("password") String password) {

		String result = "";

		validateToken();

		try {
			User user = UsersService.createUser(clientToken, id, password);
			Token userToken = user.getToken(clientToken);

			result = generateJsonResponse(userToken);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/user/{id}")
	@DELETE
	@Produces("application/json")
	public String deleteUser(@PathParam("id") String id) {

		validateAdminToken();

		String result = "";
		try {
			result = MandatorAdminService.deleteUser(adminToken, id);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/connections")
	@GET
	@Produces("application/json")
	public String listConnections(@HeaderParam("userToken") String userToken) {
		String result = "";
		try {
			List<BankConnection> list = BankConnectionsService.getBankConnections(userToken);
			result = generateJsonResponse(list, "connections");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/connections")
	@POST
	@Produces("application/json")
	public String importConnection(@HeaderParam("userToken") String userToken, @HeaderParam("bankId") int bankId, @HeaderParam("bankingUserId") String bankingUserId, @HeaderParam("bankingPin") String bankingPin) {
		String result = "";
		try {
			BankConnection connection = BankConnectionsService.importConnection(userToken, bankId, bankingUserId, bankingPin);
			result = generateJsonResponse(connection);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		// System.out.println("BC: " + connection);
		return result;
	}

	@Path("/accounts")
	@GET
	@Produces("application/json")
	public String getAccounts(@HeaderParam("userToken") String userToken) {
		String result = "";

		try {
			List<Account> accounts = AccountsService.searchAccounts(userToken, 0);
			result = generateJsonResponse(accounts, "accounts");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

	@Path("/transactions")
	@GET
	@Produces("application/json")
	public String getTransactions(@HeaderParam("userToken") String userToken, @HeaderParam("accountId") int accountId, @HeaderParam("days") int days) {
		String result = "";

		try {
			List<Transaction> transactions = TransactionsService.searchTransactions(userToken, accountId, days);
			result = generateJsonResponse(transactions, "transactions");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

	private String generateJsonResponse(Object element, String rootName) {
		String result = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
			if (rootName == null) {
				result = mapper.writeValueAsString(element);
			} else {
				result = mapper.writer().withRootName(rootName).writeValueAsString(element);
			}
		} catch (JsonProcessingException e) {
			result = "{\"error\" : \"Something went wrong: " + e + "\"}";
		}
		return result;
	}

	private String generateJsonResponse(Object element) {
		return generateJsonResponse(element, null);
	}

	private void validateToken() {
		if (!clientToken.isValid()) {
			System.out.println("Refresh Token.");
			try {
				clientToken = TokenService.requestClientToken(client_id, client_secret);
			} catch (ErrorHandler e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Token still valid.");
		}

	}

	private void validateAdminToken() {
		if (!adminToken.isValid()) {
			System.out.println("Refresh AdminToken.");
			try {
				adminToken = TokenService.requestClientToken(admin_client_id, admin_client_secret);
			} catch (ErrorHandler e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("AdminToken still valid.");
		}

	}
}