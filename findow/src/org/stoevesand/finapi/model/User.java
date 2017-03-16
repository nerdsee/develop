package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.TokenService;

public class User {
	String id = "";

	public String getId() {
		return id;
	}

	String password = "";

	public User(JSONObject json_user) {
		try {
			id = json_user.getString("id");
			password = json_user.getString("password");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public User(String id, String password) {
		this.id = id;
		this.password = password;
	}

	public String toString() {
		return String.format("\"%s\", \"%s\"", id, password);
	}

	public Token getToken(Token clientToken) {
		return TokenService.requestUserToken(clientToken, id, password);
	}

}
