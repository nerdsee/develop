package org.stoevesand.finapi;

import java.util.List;

import org.stoevesand.finapi.model.BankConnection;
import org.stoevesand.findow.auth.Authenticator;
import org.stoevesand.findow.bankingapi.ApiUser;
import org.stoevesand.findow.bankingapi.BankingAPI;
import org.stoevesand.findow.loader.DataLoader;
import org.stoevesand.findow.model.Account;
import org.stoevesand.findow.model.Bank;
import org.stoevesand.findow.model.ErrorHandler;
import org.stoevesand.findow.model.User;
import org.stoevesand.findow.persistence.PersistanceManager;
import org.stoevesand.findow.rest.RestUtils;

public class FinapiBankingAPI implements BankingAPI {

	@Override
	public List<Account> importAccount(String userToken, int bankId, String bankingUserId, String bankingPin) throws ErrorHandler {
		BankConnection connection = BankConnectionsService.importConnection(userToken, bankId, bankingUserId, bankingPin);
		String result = RestUtils.generateJsonResponse(connection);

		// initial die Umsätze laden
		DataLoader.updateTransactions(userToken, 60);

		// User laden
		User user = Authenticator.getUser(userToken);

		// Accounts laden
		List<Account> accounts = AccountsService.searchAccounts(userToken, 0);

		// Den aktuellen User zuweisen
		for (Account account : accounts) {
			account.setUser(user);
		}

		// Accounts persistieren
		PersistanceManager.getInstance().storeAccounts(accounts);
		return accounts;
	}

	private static FinapiBankingAPI _instance = null;

	private FinapiBankingAPI() {
	}

	public static BankingAPI getInstance() {
		if (_instance == null) {
			_instance = new FinapiBankingAPI();
		}
		return _instance;
	}

	@Override
	public ApiUser createUser(String username, String password) throws ErrorHandler {
		ApiUser apiUser = UsersService.createUser(TokenStore.getInstance().getClientToken(), null, null);
		return apiUser;
	}

	@Override
	public void deleteUser(String userToken) throws ErrorHandler {
		UsersService.deleteUser(userToken);
	}

	@Override
	public List<Bank> searchBanks(String search) {
		List<Bank> banks = BanksService.searchBanks(TokenStore.getInstance().getClientToken(), search);
		return banks;
	}

}
