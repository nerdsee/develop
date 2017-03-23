package org.stoevesand.finapi.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Category {

	int id;
	String name;
	String parentName;

	public Category() {
		id = -1;
		name = "NONE";
		parentName = "NO PARENT";
	}

	public Category(JSONObject jo) {
		// this.jo = jo;
		try {
			id = jo.getInt("id");
			name = jo.getString("name");
			parentName = jo.getString("parentName");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getParentName() {
		return parentName;
	}

	
	
}
