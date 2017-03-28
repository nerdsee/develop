package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Account {

	public int getBankConnectionId() {
		return bankConnectionId;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public String getSubAccountNumber() {
		return subAccountNumber;
	}

	public String getIban() {
		return iban;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	private JSONObject jo = null;

	private int id = 0;
	private int bankConnectionId;
	private String accountName;
	private String accountNumber;
	private String subAccountNumber;
	private String iban;
	private String accountHolderName;

	private String accountCurrency;

	private int accountTypeId;

	private String accountTypeName;

	private int balance;

	private int overdraft;

	private int overdraftLimit;

	private int availableFunds;

	public Account(JSONObject jo) {
		this.jo = jo;
		try {
			id = jo.getInt("id");
			bankConnectionId = jo.getInt("bankConnectionId");
			accountName = jo.getString("accountName");
			accountNumber = jo.getString("accountNumber");
			subAccountNumber = jo.getString("subAccountNumber");
			iban = jo.getString("iban");
			accountHolderName = jo.getString("accountHolderName");

			accountCurrency = jo.getString("accountCurrency");
			accountTypeId = jo.getInt("accountTypeId");
			accountTypeName = jo.getString("accountTypeName");
			balance = jo.getInt("balance");
			overdraft = jo.getInt("overdraft");
			overdraftLimit = jo.getInt("overdraftLimit");
			availableFunds = jo.getInt("availableFunds");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getAccountCurrency() {
		return accountCurrency;
	}

	public void setAccountCurrency(String accountCurrency) {
		this.accountCurrency = accountCurrency;
	}

	public int getAccountTypeId() {
		return accountTypeId;
	}

	public void setAccountTypeId(int accountTypeId) {
		this.accountTypeId = accountTypeId;
	}

	public String getAccountTypeName() {
		return accountTypeName;
	}

	public void setAccountTypeName(String accountTypeName) {
		this.accountTypeName = accountTypeName;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getOverdraft() {
		return overdraft;
	}

	public void setOverdraft(int overdraft) {
		this.overdraft = overdraft;
	}

	public int getOverdraftLimit() {
		return overdraftLimit;
	}

	public void setOverdraftLimit(int overdraftLimit) {
		this.overdraftLimit = overdraftLimit;
	}

	public int getAvailableFunds() {
		return availableFunds;
	}

	public void setAvailableFunds(int availableFunds) {
		this.availableFunds = availableFunds;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setBankConnectionId(int bankConnectionId) {
		this.bankConnectionId = bankConnectionId;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void setSubAccountNumber(String subAccountNumber) {
		this.subAccountNumber = subAccountNumber;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public int getId() {
		return id;
	}

	public String toString() {
		return String.format("%s (%d)", accountName, id);
	}

}
