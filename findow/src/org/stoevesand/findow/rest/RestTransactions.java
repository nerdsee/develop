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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/transactions")
@Api(value="transactions")
public class RestTransactions {

	@Path("/")
	@GET
	@Produces("application/json")
	public String getTransactions(@HeaderParam("userToken") String userToken, @HeaderParam("accountId") int accountId, @HeaderParam("days") int days) {
		String result = "";

		try {
			List<Transaction> transactions = TransactionsService.searchTransactions(userToken, accountId, days);
			result = RestUtils.generateJsonResponse(transactions, "transactions");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

}