package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Transaction {

	JSONObject jo = null;

	private int id = 0;
	private int parentId;
	private int accountId;
	private int amount;
	private String valueDate;
	private String finapiBookingDate;
	private String purpose;
	private String counterpartName;
	
	public int getAmount() {
		return amount;
	}

	public String getValueDate() {
		return valueDate;
	}

	public String getFinapiBookingDate() {
		return finapiBookingDate;
	}

	public String getPurpose() {
		return purpose;
	}

	public String getCounterpartName() {
		return counterpartName;
	}

	public Transaction(JSONObject jo) {
		this.jo = jo;
		try {
			id = jo.getInt("id");
			// parentId = jo.getInt("parentId");
			accountId = jo.getInt("accountId");
			amount = jo.getInt("amount");
			valueDate = jo.getString("valueDate");
			finapiBookingDate = jo.getString("finapiBookingDate");
			purpose = jo.getString("purpose");
			counterpartName = jo.getString("counterpartName");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return String.format("** %d # %s # %d # %s", id, purpose, amount, counterpartName);
	}

}
