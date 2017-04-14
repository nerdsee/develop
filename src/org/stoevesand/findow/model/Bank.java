package org.stoevesand.findow.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonGetter;

public class Bank {

	int id = 0;
	String name = "";

	@JsonGetter("id")
	public int getId() {
		return id;
	}

	@JsonGetter("name")
	public String getName() {
		return name;
	}

	@JsonGetter("blz")
	public String getBlz() {
		return blz;
	}

	@JsonGetter("bic")
	public String getBic() {
		return bic;
	}

	private String blz;
	private String bic;

	public Bank(JSONObject json_bank) {
		try {
			id = json_bank.getInt("id");
			name = json_bank.getString("name");
			blz = json_bank.getString("blz");
			bic = json_bank.getString("bic");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String toString() {
		return String.format("%s (%d)", name, id);
	}

}
