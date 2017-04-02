package org.stoevesand.findow.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.TransactionsService;
import org.stoevesand.finapi.UsersService;
import org.stoevesand.finapi.model.FinapiUser;
import org.stoevesand.finapi.model.Transaction;
import org.stoevesand.finapi.model.TransactionList;
import org.stoevesand.findow.loader.DataLoader;
import org.stoevesand.findow.model.CategorySum;
import org.stoevesand.findow.model.User;
import org.stoevesand.findow.persistence.PersistanceManager;

import io.swagger.annotations.Api;

@Path("/transactions")
@Api(value="transactions")
public class RestTransactions {

	@Path("/")
	@GET
	@Produces("application/json")
	public String getTransactions(@HeaderParam("userToken") String userToken, @HeaderParam("accountId") int accountId, @HeaderParam("days") int days) {
		String result = "";

		try {
			DataLoader.updateTransactions(userToken, 7);

			// User laden
			FinapiUser finapiUser = UsersService.getUser(userToken);
			User user = PersistanceManager.getInstance().getUserByExternalName(finapiUser.getId());
			
			List<Transaction> transactions = PersistanceManager.getInstance().getTx(user, days);
			result = RestUtils.generateJsonResponse(transactions, "transactions");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

	@Path("/categorized")
	@GET
	@Produces("application/json")
	public String getTransactionsCat(@HeaderParam("userToken") String userToken, @HeaderParam("accountId") int accountId, @HeaderParam("days") int days) {
		String result = "";

		try {
			List<CategorySum> cs = PersistanceManager.getInstance().getCategorySummary();
			result = RestUtils.generateJsonResponse(cs, "categorySummary");
		} catch (Exception e) {
			result = RestUtils.generateJsonResponse(Response.UNKNOWN);
		}
		return result;
	}

}