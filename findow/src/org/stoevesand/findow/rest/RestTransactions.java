package org.stoevesand.findow.rest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.TransactionsService;
import org.stoevesand.finapi.model.TransactionList;

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
			TransactionList transactions = TransactionsService.searchTransactions(userToken, accountId, days);
			result = RestUtils.generateJsonResponse(transactions, "transactionList");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

}