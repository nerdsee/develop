package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.TokenService;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "userinfo")
public class UserInfo {
	public String getRegistrationDate() {
		return registrationDate;
	}

	public String getDeletionDate() {
		return deletionDate;
	}

	public String getLastActiveDate() {
		return lastActiveDate;
	}

	String id = "";

	@JsonGetter
	public String getId() {
		return id;
	}

	private String registrationDate;
	private String deletionDate;
	private String lastActiveDate;

	public UserInfo(JSONObject json_user) {
		try {
			id = json_user.getString("userId");
			registrationDate = json_user.getString("registrationDate");
			deletionDate = json_user.getString("deletionDate");
			lastActiveDate = json_user.getString("lastActiveDate");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String toString() {
		return String.format("ID: %s", id);
	}

}
