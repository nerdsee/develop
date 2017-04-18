package org.stoevesand.findow.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.stoevesand.findow.auth.Authenticator;
import org.stoevesand.findow.bankingapi.BankingAPI;
import org.stoevesand.findow.model.Account;
import org.stoevesand.findow.model.ErrorHandler;
import org.stoevesand.findow.model.User;
import org.stoevesand.findow.persistence.PersistanceManager;
import org.stoevesand.findow.server.FindowSystem;

import io.swagger.annotations.Api;

@Path("/accounts")
@Api(value = "accounts")
public class RestAccounts {

	@Path("/{id}")
	@GET
	@Produces("application/json")
	public String getAccount(@PathParam("id") String id, @HeaderParam("userToken") String userToken) {
		String result = "";

		try {
			User user = Authenticator.getUser(userToken);

			long accountId = Long.parseLong(id);

			Account account = PersistanceManager.getInstance().getAccount(user, accountId, userToken);
			result = RestUtils.generateJsonResponse(account, "account");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		} catch (NumberFormatException nfe) {
			result = RestUtils.generateJsonResponse(Response.INVALID_ID);
		}
		return result;
	}

	@Path("/{id}")
	@DELETE
	@Produces("application/json")
	public String deleteAccount(@PathParam("id") String id, @HeaderParam("userToken") String userToken) {
		String result = "";

		try {
			User user = Authenticator.getUser(userToken);

			long accountId = Long.parseLong(id);

			PersistanceManager.getInstance().deleteAccount(user, accountId, userToken);

			result = RestUtils.generateJsonResponse(Response.OK);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		} catch (NumberFormatException nfe) {
			result = RestUtils.generateJsonResponse(Response.INVALID_ID);
		}
		return result;
	}

	@Path("/")
	@GET
	@Produces("application/json")
	public String getAccounts(@HeaderParam("userToken") String userToken) {
		String result = "";

		try {
			// User laden
			User user = Authenticator.getUser(userToken);

			List<Account> accounts = PersistanceManager.getInstance().getAccounts(user, userToken);
			result = RestUtils.generateJsonResponse(accounts, "accounts");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

	@Path("/")
	@POST
	@Produces("application/json")
	public String importAccount(@HeaderParam("userToken") String userToken, @HeaderParam("bankId") int bankId, @HeaderParam("bankingUserId") String bankingUserId, @HeaderParam("bankingPin") String bankingPin) {
		String result = "";
		try {

			BankingAPI bankingAPI = FindowSystem.getBankingAPI();
			bankingAPI.importAccount(userToken, bankId, bankingUserId, bankingPin);

		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		// System.out.println("BC: " + connection);
		return result;
	}
}