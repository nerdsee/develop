package org.stoevesand.brain;

import java.sql.ResultSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.apache.commons.lang3.StringEscapeUtils;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.DBUtil;

public class Category {

	private String text;
	private String locale;
	private long id;

	private int count = -1;

	public int getCount() {
		return count;
	}

	private int size;

	public Category(ResultSet rs) {
		super();

		this.id = DBUtil.getLong(rs, "id");
		this.text = DBUtil.getString(rs, "text");
		this.locale = DBUtil.getString(rs, "locale");
		this.count = DBUtil.getInt(rs, "cx");
	}

	public boolean getIsActive() {
		String cat = "";
		// TODO: heilmachen
		// String cat = brainSession.getFilterCat();
		return cat.equals(text);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean equals(String filterCat) {
		return text.equals(filterCat);
	}

	// public int getCount() {
	// if (count == -1) {
	// try {
	// BrainDB brainDB = brainSystem.getBrainDB();
	// count = brainDB.getCategoryItemCount(this);
	// } catch (DBException e) {
	// e.printStackTrace();
	// }
	// }
	// return count;
	// }

	public long getID() {
		return id;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public String getEscapedText() {
		return StringEscapeUtils.escapeJava(getText());
	}
}
