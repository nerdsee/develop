package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class JSONUtils {

	public static double getDouble(JSONObject jo, String key) {
		double ret = 0;
		try {
			ret = jo.getDouble(key);
		} catch (JSONException e) {
			System.out.println("Cannot read from JSON: " + key);
		}
		return ret;
	}

	public static int getInt(JSONObject jo, String key) {
		int ret = 0;
		try {
			ret = jo.getInt(key);
		} catch (JSONException e) {
			System.out.println("Cannot read from JSON: " + key);
		}
		return ret;
	}

	public static String getString(JSONObject jo, String key) {
		String ret = "";
		try {
			ret = jo.getString(key);
		} catch (JSONException e) {
			System.out.println("Cannot read from JSON: " + key);
		}
		return ret;
	}

	public static long getLong(JSONObject jo, String key) {
		long ret = 0L;
		try {
			ret = jo.getLong(key);
		} catch (JSONException e) {
			System.out.println("Cannot read from JSON: " + key);
		}
		return ret;
	}

}
