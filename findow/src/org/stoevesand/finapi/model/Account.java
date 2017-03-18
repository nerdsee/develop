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
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getId() {
		return id;
	}

	public String toString() {
		return String.format("%s (%d)", accountName, id);
	}

}
