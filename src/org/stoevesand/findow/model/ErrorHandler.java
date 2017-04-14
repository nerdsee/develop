package org.stoevesand.findow.model;

import java.util.List;
import java.util.Vector;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.CallError;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonIgnoreProperties({ "stackTrace", "localizedMessage", "message", "cause", "suppressed" })
@JsonRootName(value = "error")
public class ErrorHandler extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7996550252908124993L;
	
	List<CallError> errors = new Vector<CallError>();
	String response;
	private int status;

	public ErrorHandler(String response) {
		this.response = response;
		try {
			JSONObject jo = new JSONObject(response);
			JSONArray json_errors = jo.getJSONArray("errors");
			for (int i = 0; i < json_errors.length(); i++) {
				JSONObject json_account = json_errors.getJSONObject(i);
				CallError error = new CallError(json_account);
				errors.add(error);
			}
		} catch (JSONException e) {
		}

	}

	public ErrorHandler(int status, String response) {
		this.status = status;
		this.response = response;
		try {
			JSONObject jo = new JSONObject(response);
			JSONArray json_errors = jo.getJSONArray("errors");
			for (int i = 0; i < json_errors.length(); i++) {
				JSONObject json_account = json_errors.getJSONObject(i);
				CallError error = new CallError(json_account);
				errors.add(error);
			}
		} catch (JSONException e) {
		}
	}

	public int getStatus() {
		return status;
	}

	@JsonIgnore
	public String getResponse() {
		return response;
	}

	@JsonGetter
	public List<CallError> getErrors() {
		return errors;
	}

	public void printErrors() {
		for (CallError error : errors) {
			System.out.println(error);
		}
	}

}
