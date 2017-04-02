package org.stoevesand.finapi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.annotations.GenericGenerator;
import org.stoevesand.findow.model.User;

@Entity
@Table(name = "ACCOUNTS")
public class Account {

	// internal id used for persistance
	private Long id;

	// id coming from a source system
	private Long sourceId;
	private String sourceSystem = "FINAPI";

	private User user = null;

	public Account() {

	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	@ManyToOne
	@JoinColumn(name = "USER_ID", nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "ACCOUNT_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	private int bankConnectionId;
	private String accountName;
	private String accountNumber;
	private String subAccountNumber;
	private String iban;
	private String accountHolderName;

	private String accountCurrency;

	private int accountTypeId;

	private String accountTypeName;

	private double balance;

	private int overdraft;

	private int overdraftLimit;

	private int availableFunds;

	public Account(JSONObject jo) {
		try {
			sourceId = jo.getLong("id");
			bankConnectionId = jo.getInt("bankConnectionId");
			accountName = jo.getString("accountName");
			accountNumber = jo.getString("accountNumber");
			subAccountNumber = jo.getString("subAccountNumber");
			iban = jo.getString("iban");
			accountHolderName = jo.getString("accountHolderName");

			accountCurrency = jo.getString("accountCurrency");
			accountTypeId = jo.getInt("accountTypeId");
			accountTypeName = jo.getString("accountTypeName");
			balance = jo.getDouble("balance");
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

	public double getBalance() {
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

	public String toString() {
		return String.format("%s (%d)", accountName, id);
	}

}
