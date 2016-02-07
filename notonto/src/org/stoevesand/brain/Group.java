package org.stoevesand.brain;

import java.sql.ResultSet;

import org.stoevesand.util.DBUtil;

public class Group {
	private String prefix = null;
	private long id;
	private String title = null;
	
	public Group(ResultSet rs) {
		this.id = DBUtil.getLong(rs, "id");
		this.prefix = DBUtil.getString(rs, "prefix");
		this.title = DBUtil.getString(rs, "title");
	}

	public String getPrefix() {
		return prefix;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

}
