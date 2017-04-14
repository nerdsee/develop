package org.stoevesand.findow.bankingapi;

import java.util.List;

import org.stoevesand.findow.model.Account;
import org.stoevesand.findow.model.Bank;
import org.stoevesand.findow.model.ErrorHandler;

public interface BankingAPI {

	public List<Account> importAccount(String userToken, int bankId, String bankingUserId, String bankingPin) throws ErrorHandler;

	public ApiUser createUser(String username, String password) throws ErrorHandler;

	public void deleteUser(String userToken) throws ErrorHandler;

	public List<Bank> searchBanks(String search);

}
