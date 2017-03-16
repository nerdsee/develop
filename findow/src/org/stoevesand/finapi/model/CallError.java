package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CallError {

	private String message;
	private String type;
	private String code;

	public CallError(JSONObject json_error) {
		try {
			message = json_error.getString("message");
			code = json_error.getString("code");
			type = json_error.getString("type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String toString() {
		return String.format("%s (%s,%s)", message, code, type);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
