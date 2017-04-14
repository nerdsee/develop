package org.stoevesand.findow.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.stoevesand.findow.model.ErrorHandler;
import org.stoevesand.finapi.UsersService;
import org.stoevesand.findow.model.Account;
import org.stoevesand.finapi.model.FinapiUser;
import org.stoevesand.findow.model.User;
import org.stoevesand.findow.persistence.PersistanceManager;

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
			// User laden
			FinapiUser finapiUser = UsersService.getUser(userToken);
			User user = PersistanceManager.getInstance().getUserByExternalName(finapiUser.getId());

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

	@Path("/")
	@GET
	@Produces("application/json")
	public String getAccounts(@HeaderParam("userToken") String userToken) {
		String result = "";

		try {
			// User laden
			FinapiUser finapiUser = UsersService.getUser(userToken);
			User user = PersistanceManager.getInstance().getUserByExternalName(finapiUser.getId());

			List<Account> accounts = PersistanceManager.getInstance().getAccounts(user, userToken);
			result = RestUtils.generateJsonResponse(accounts, "accounts");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

}