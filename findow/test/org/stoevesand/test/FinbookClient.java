package org.stoevesand.finapi.client;

import java.util.List;
import java.util.Vector;

import org.stoevesand.finapi.AccountsService;
import org.stoevesand.finapi.BankConnectionsService;
import org.stoevesand.finapi.BanksService;
import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.MandatorAdminService;
import org.stoevesand.finapi.TokenService;
import org.stoevesand.finapi.TransactionsService;
import org.stoevesand.finapi.model.Account;
import org.stoevesand.finapi.model.Bank;
import org.stoevesand.finapi.model.BankConnection;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.finapi.model.Transaction;
import org.stoevesand.finapi.model.User;
import org.stoevesand.finapi.model.UserInfo;
import org.stoevesand.findow.rest.RestServer10;

public class FinbookClient {

	static final String client_id = "7fbb36a6-e886-41fc-9a0a-3ead413cddb8";
	static final String client_secret = "3122d123-fdeb-498c-93c4-5eda3c10d396";
	static final String data_decryption_key = "e0c82a81c6886460f109fe5348c58884";

	// finAPI SANDBOX Admin-Client:
	static final String admin_client_id = "05e712de-dbb9-4b31-9889-ead3c151c54f";
	static final String admin_client_secret = "f3e251db-be41-46cc-a438-ecd55e4a7abc";
	static final String admin_data_decryption_key = "eeb4561adf992fc44313468e4035ccac";

	public static void main(String[] args) {

		FinbookClient fb = new FinbookClient();
		fb.run();
	}

	void run() {

		RestServer10 bs = new RestServer10();

		// bs.createUser("jan", "test12345!");
		// bs.deleteUser("jan");

		Token clientToken = null;
		Token adminToken = null;
		try {
			clientToken = TokenService.requestClientToken(client_id, client_secret);
			adminToken = TokenService.requestClientToken(admin_client_id, admin_client_secret);
		} catch (ErrorHandler e1) {
			e1.printStackTrace();
		}

		List<UserInfo> users = MandatorAdminService.getUsers(adminToken);
		for (UserInfo user : users) {
			System.out.println(user);
		}

		List<Bank> banks = BanksService.searchBanks(clientToken, "hamburg");
		for (Bank bank : banks) {
			System.out.println(bank);
		}

		Bank haspa = BanksService.getBank(clientToken, 273718);
		System.out.println("Bank: " + haspa);

		// User user = UsersService.createUser(clientToken);
		User user = new User("da7eb4f2-d301-4b87-9260-4ce94041bdd7", "43a78d96-47a0-49a7-b11d-f01030177141");
		System.out.println("User: " + user);

		// if (user != null)
		// return;

		Token userToken = null;
		try {
			userToken = user.getToken(clientToken);
		} catch (ErrorHandler e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("User Token: " + userToken);

		List<BankConnection> connections = listConnections(userToken);

		if (connections.size() == 0) {
			importConnection(userToken);
			connections = listConnections(userToken);
		}

		if (connections.size() > 0) {
			for (BankConnection conn : connections) {
				if (conn.waitUntilReady(userToken)) {
					getAccounts(userToken, 7);
				} else {
					System.out.println("Connection wurde nicht fertig.");
				}
			}
		}
	}

	public List<BankConnection> listConnections(Token userToken) {
		List<BankConnection> list = new Vector<BankConnection>();
		try {
			list = BankConnectionsService.getBankConnections(userToken.getToken());
			System.out.println("Connections: " + list.size());
			for (BankConnection connection : list) {
				System.out.println(connection);
			}
		} catch (ErrorHandler e) {
			e.printStackTrace();
		}

		return list;
	}

	public BankConnection importConnection(Token userToken) {
		BankConnection connection = null;
		try {
			connection = BankConnectionsService.importConnection(userToken.getToken(), 273718, "1180741694", "xxxxx");
		} catch (ErrorHandler e) {
			e.printErrors();
		}
		// System.out.println("BC: " + connection);
		return connection;
	}

	public List<Transaction> getAccounts(Token userToken, int days) {
		System.out.println("ACCOUNTS:");
		List<Transaction> transactions = null;

		try {
			List<Account> accounts = AccountsService.searchAccounts(userToken.getToken(), 0);
			for (Account account : accounts) {
				System.out.println("**");
				System.out.println(account);
				transactions = TransactionsService.searchTransactions(userToken.getToken(), account, days);
				for (Transaction transaction : transactions) {
					System.out.println(transaction);
				}
			}
		} catch (ErrorHandler e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return transactions;
	}

}