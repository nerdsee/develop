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

@Path("/accounts")
@Api(value="accounts")
public class RestAccounts {

	@Path("/")
	@GET
	@Produces("application/json")
	public String getAccounts(@HeaderParam("userToken") String userToken) {
		String result = "";

		try {
			List<Account> accounts = AccountsService.searchAccounts(userToken, 0);
			result = RestUtils.generateJsonResponse(accounts, "accounts");
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}
		return result;
	}

}