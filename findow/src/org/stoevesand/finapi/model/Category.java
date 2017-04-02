package org.stoevesand.finapi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "CATEGORIES")
public class Category {

	private Long id;
	private String name;
	private transient String parentName;

	public Category() {
		id = null;
		name = "NONE";
		parentName = "NO PARENT";
	}

	public Category(JSONObject jo) {
		// this.jo = jo;
		try {
			id = jo.getLong("id");
			name = jo.getString("name");
			parentName = jo.getString("parentName");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Category(String name, String parentName) {
		id = null;
		this.name = name;
		this.parentName = parentName;
	}

	@Id
	@Column(name = "CATEGORY_ID")
	@JsonIgnore
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	@Column(name = "PARENT_NAME")
	public String getParentName() {
		return parentName;
	}

}
