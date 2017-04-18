package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "token")
public class Token {

	static final long VALIDITY_BUFFER_SECONDS = 200;

	String access_token = "";
	String token_type = "";
	String expires_in = "";
	String id = "";
	String secret = "";

	long valid_until = 0;

	public Token(String id, String secret, JSONObject json_token) {
		try {
			access_token = json_token.getString("access_token");
			token_type = json_token.getString("token_type");
			expires_in = json_token.getString("expires_in");
			valid_until = System.currentTimeMillis() + (Long.parseLong(expires_in) * 1000);

			this.id = id;
			this.secret = secret;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getId() {
		return id;
	}

	public String getSecret() {
		return secret;
	}

	@JsonGetter
	public String getToken() {
		return access_token;
	}

	public boolean isValid() {
		long now = System.currentTimeMillis();
		return (now + VALIDITY_BUFFER_SECONDS) < valid_until;
	}

	public String toString() {
		return access_token;
	}

}
