package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Transaction {

	JSONObject jo = null;

	private int id = 0;
	private int parentId;
	private int accountId;
	private int amount;
	private String valueDate;
	private String bookingDate;
	private String purpose;
	private String counterpartName;
	private Category category;

	private String type;
	
	public int getAmount() {
		return amount;
	}

	public int getId() {
		return id;
	}

	public int getParentId() {
		return parentId;
	}

	public int getAccountId() {
		return accountId;
	}

	public String getType() {
		return type;
	}

	public String getValueDate() {
		return valueDate;
	}

	public String getBookingDate() {
		return bookingDate;
	}

	public String getPurpose() {
		return purpose;
	}

	public String getCounterpartName() {
		return counterpartName;
	}
	
	public Category getCategory() {
		return category;
	}

	public Transaction(JSONObject jo) {
		this.jo = jo;
		try {
			id = jo.getInt("id");
			// parentId = jo.getInt("parentId");
			accountId = jo.getInt("accountId");
			amount = jo.getInt("amount");
			valueDate = jo.getString("valueDate");
			bookingDate = jo.getString("finapiBookingDate");
			purpose = jo.getString("purpose");
			counterpartName = jo.getString("counterpartName");
			type = jo.getString("type");
			
			JSONObject jocat = jo.getJSONObject("category");
			if (jocat!=null) {
				category = new Category(jocat);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@JsonIgnore
	public String toString() {
		return String.format("** %d # %s # %d # %s", id, purpose, amount, counterpartName);
	}

}
