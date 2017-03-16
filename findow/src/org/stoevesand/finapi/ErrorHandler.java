package org.stoevesand.finapi;

import java.util.List;
import java.util.Vector;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.stoevesand.finapi.model.CallError;

public class ErrorHandler extends Exception {
	
	List<CallError> errors = new Vector<CallError>();
	
	public ErrorHandler(Response response) {
		String output = response.readEntity(String.class);
		try {
			JSONObject jo = new JSONObject(output);
			
			JSONArray json_errors = jo.getJSONArray("errors");
			for (int i = 0; i < json_errors.length(); i++) {
				JSONObject json_account = json_errors.getJSONObject(i);
				CallError error = new CallError(json_account);
				errors.add(error);
			}

			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void printErrors() {
		for (CallError error : errors) {
			System.out.println(error);
		}
	}

}
