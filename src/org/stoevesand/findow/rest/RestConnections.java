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
import org.stoevesand.finapi.UsersService;
import org.stoevesand.finapi.model.BankConnection;
import org.stoevesand.finapi.model.FinapiUser;
import org.stoevesand.findow.loader.DataLoader;
import org.stoevesand.findow.model.Account;
import org.stoevesand.findow.model.ErrorHandler;
import org.stoevesand.findow.model.User;
import org.stoevesand.findow.persistence.PersistanceManager;

import io.swagger.annotations.Api;

@Path("/connections")
@Api(value = "connections")
@Deprecated
public class RestConnections {

	@Path("/")
	@GET
	@Produces("application/json")
	@Deprecated
	public String listConnections(@HeaderParam("userToken") String userToken) {
		String result = "";
		try {
			List<BankConnection> list = BankConnectionsService.getBankConnections(userToken);
			result = RestUtils.generateJsonResponse(list, "connections");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/{connectionId}")
	@DELETE
	@Produces("application/json")
	@Deprecated
	public String deleteConnection(@HeaderParam("userToken") String userToken, @PathParam("connectionId") int connectionId) {
		String result = "";
		try {
			result = BankConnectionsService.deleteBankConnection(userToken, connectionId);
			PersistanceManager.getInstance().deleteAccounts(connectionId);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/")
	@POST
	@Produces("application/json")
	@Deprecated
	public String importConnection(@HeaderParam("userToken") String userToken, @HeaderParam("bankId") int bankId, @HeaderParam("bankingUserId") String bankingUserId, @HeaderParam("bankingPin") String bankingPin) {
		String result = "";
		try {
			BankConnection connection = BankConnectionsService.importConnection(userToken, bankId, bankingUserId, bankingPin);
			result = RestUtils.generateJsonResponse(connection);
			// initial die Umsätze laden
			DataLoader.updateTransactions(userToken, 60);

			// User laden
			FinapiUser finapiUser = UsersService.getUser(userToken);
			User user = PersistanceManager.getInstance().getUserByExternalName(finapiUser.getId());

			// Accounts laden
			List<Account> accounts = AccountsService.searchAccounts(userToken, 0);

			// Den aktuellen User zuweisen
			for (Account account : accounts) {
				account.setUser(user);
			}

			// Accounts persistieren
			PersistanceManager.getInstance().storeAccounts(accounts);

		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		// System.out.println("BC: " + connection);
		return result;
	}

}