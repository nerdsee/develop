package org.stoevesand.findow;

import java.util.List;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.stoevesand.finapi.AccountsService_Old;
import org.stoevesand.finapi.BankConnectionsService;
import org.stoevesand.finapi.BanksService;
import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.TokenService;
import org.stoevesand.finapi.TransactionsService;
import org.stoevesand.finapi.client.FinbookClient;
import org.stoevesand.finapi.model.Account;
import org.stoevesand.finapi.model.Bank;
import org.stoevesand.finapi.model.BankConnection;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.finapi.model.Transaction;
import org.stoevesand.finapi.model.User;

@ManagedBean
@SessionScoped
public class FindowSession {

	static final String client_id = "7fbb36a6-e886-41fc-9a0a-3ead413cddb8";
	static final String client_secret = "3122d123-fdeb-498c-93c4-5eda3c10d396";
	static final String data_decryption_key = "e0c82a81c6886460f109fe5348c58884";

	private String bankSearch = "";

	private String username = "STARTS";
	private User jan = new User("da7eb4f2-d301-4b87-9260-4ce94041bdd7", "43a78d96-47a0-49a7-b11d-f01030177141");
	private User johannes = new User("bef881e3-c5da-4dbe-833c-99695e9a6f8d", "1477ad5f-b993-417d-829d-d552a44c5b34");;

	private Token clientToken = null;
	private List<BankConnection> connections = null;
	private List<Transaction> transactions;
	private List<Bank> banks = new Vector<Bank>();
	private List<FindowUser> users = new Vector<FindowUser>();

	private FindowUser currentUser = null;

	private String connectionPin = "";
	private String accountPin = "";
	private String accountNumber = "";
	private BankConnection currentConnection = null;

	public List<FindowUser> getUsers() {
		return users;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@PostConstruct
	public void init() {

		clientToken = TokenService.requestClientToken(client_id, client_secret);
		// User user = UsersService.createUser(clientToken);

		users.add(new FindowUser("Jan", jan));
		users.add(new FindowUser("Johannes", johannes));
	}

	public String doRefresh(int days) {
		int connectionId = currentConnection.getId();
		Token userToken = currentUser.getUser().getToken(clientToken);
		if (connections.size() > 0) {
			for (BankConnection conn : connections) {
				if (conn.getId() == connectionId) {
					if (conn.waitUntilReady(userToken)) {
						transactions = getAccounts(userToken, connectionId, days);
					} else {
						System.out.println("Connection wurde nicht fertig.");
					}
				}
			}
		}
		return null;
	}

	public String doChooseUser(String id) {
		for (FindowUser user : users) {
			if (id.equals(user.getId())) {
				currentUser = user;
				connections = listConnections(currentUser.getUser().getToken(clientToken));
				return "user";
			}
		}
		return null;
	}

	public String doChooseConnection(int id) {

		for (BankConnection conn : connections) {
			if (conn.getId() == id) {
				currentConnection = conn;
				if (conn.getPin() != null) {
					// die PIN wurde in dieser Session schon eingegeben
					doRefresh(1);
					return "transactions";
				} else if ((connectionPin != null) && (connectionPin.length() > 2)) {
					// hat noch keine PIN, es wurde aber einen eingegeben
					try {
						BankConnection bc = BankConnectionsService.updateConnection(currentUser.getUser().getToken(clientToken), conn.getId(), connectionPin);
						connections = listConnections(currentUser.getUser().getToken(clientToken));
						if (bc != null) {
							
							// jetzt die PIN für diese Session noch merken:
							for (BankConnection newconn : connections) {
								if (newconn.getId() == bc.getId()) {
									newconn.setPin(connectionPin);
								}
							}

							return "transactions";
						}
					} catch (ErrorHandler e) {
						e.printErrors();
					} finally {
						connectionPin = null;
					}
					connectionPin = null;
					return "user";
				} else {
					return "enterpin";
				}
			}
		}

		return "user";
	}

	private int bankID = 0;
	private Bank currentBank;

	public String doChooseBank(int id) {
		bankID = id;

		for (Bank bank : banks) {
			if (bank.getId() == id) {
				currentBank = bank;
			}
		}

		if (currentBank != null) {
			return "importconnection";
		}

		return null;
	}

	public String doDeleteConnection(int id) {

		try {
			BankConnectionsService.deleteConnection(currentUser.getUser().getToken(clientToken), id);
			connections = listConnections(currentUser.getUser().getToken(clientToken));
		} catch (ErrorHandler e) {
			e.printErrors();
		}
		return "user";
	}

	public String doImportConnection() {
		BankConnection bc = null;
		try {
			bc = BankConnectionsService.importConnection(currentUser.getUser().getToken(clientToken), getCurrentBank().getId(), accountNumber, accountPin);
			if (bc != null) {
				connections = listConnections(currentUser.getUser().getToken(clientToken));
				
				// jetzt die PIN für diese Session noch merken:
				for (BankConnection newconn : connections) {
					if (newconn.getId() == bc.getId()) {
						newconn.setPin(accountPin);
					}
				}

				return "user";
			}
		} catch (ErrorHandler e) {
			e.printErrors();
		}
		return null;
	}

	public List<Transaction> getAccounts(Token userToken, int connectionId, int days) {
		System.out.println("ACCOUNTS:");
		List<Transaction> transactions = null;

		List<Account> accounts = AccountsService_Old.searchAccounts(userToken, connectionId);
		for (Account account : accounts) {
			System.out.println("**");
			System.out.println(account);
			transactions = TransactionsService.searchTransactions(userToken, account, days);
			for (Transaction transaction : transactions) {
				System.out.println(transaction);
			}
		}
		return transactions;
	}

	public static List<BankConnection> listConnections(Token userToken) {
		List<BankConnection> list = BankConnectionsService.getBankConnections(userToken);
		return list;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public List<BankConnection> getConnections() {
		return connections;
	}

	public FindowUser getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(FindowUser currentUser) {
		this.currentUser = currentUser;
	}

	public String getBankSearch() {
		return bankSearch;
	}

	public void setBankSearch(String bankSearch) {
		this.bankSearch = bankSearch;

		if ((bankSearch != null) && (bankSearch.length() > 2)) {
			banks = BanksService.searchBanks(clientToken, bankSearch);
		}

	}

	public List<Bank> getBanks() {
		return banks;
	}

	public void setBanks(List<Bank> banks) {
		this.banks = banks;
	}

	public Bank getCurrentBank() {
		return currentBank;
	}

	public void setCurrentBank(Bank currentBank) {
		this.currentBank = currentBank;
	}

	public String getAccountPin() {
		return accountPin;
	}

	public void setAccountPin(String accountPin) {
		this.accountPin = accountPin;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public BankConnection getCurrentConnection() {
		return currentConnection;
	}

	public void setCurrentConnection(BankConnection currentConnection) {
		this.currentConnection = currentConnection;
	}

	public String getConnectionPin() {
		return connectionPin;
	}

	public void setConnectionPin(String connectionPin) {
		this.connectionPin = connectionPin;
	}
}
