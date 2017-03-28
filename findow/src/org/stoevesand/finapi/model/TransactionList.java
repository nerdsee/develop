package org.stoevesand.finapi.model;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TransactionList {

	List<Transaction> transactions;
	private int income;
	private int spending;
	private int balance;

	public TransactionList(JSONObject jo) {
		try {

			JSONArray json_txs = jo.getJSONArray("transactions");

			for (int i = 0; i < json_txs.length(); i++) {
				JSONObject json_account = json_txs.getJSONObject(i);
				Transaction transaction = new Transaction(json_account);
				transactions.add(transaction);
			}

			income = jo.getInt("income");
			spending = jo.getInt("spending");
			balance = jo.getInt("balance");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public int getIncome() {
		return income;
	}

	public int getSpending() {
		return spending;
	}

	public int getBalance() {
		return balance;
	}

}
