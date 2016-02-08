package org.stoevesand.util;

import java.sql.ResultSet;
import java.util.Date;

import javax.faces.bean.ManagedProperty;

import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.persistence.BrainDB;

public class News {

	private static final int MAX_TEXT_LENGTH = 100;
	
  @ManagedProperty(value="#{brainSystem}")
  private BrainSystem brainSystem;

  public void setBrainSystem(BrainSystem bs) {
		this.brainSystem = bs;
	}
	
	String title = "";
	String title_o = "";
	String text = "";
	private String slug = "";
	String text_o = "";
	int type = 0;
	int type_o = 0;
	Date date = null;
	Date date_o = null;
	String locale = null;
	String typetext = null;

	public String getTypetext() {
		return typetext;
	}

	String locale_o = null;
	long id = -1;

	public News() {
		id = -1;
		date = new Date();
	}

	public News(ResultSet rs) {
		this.id = DBUtil.getLong(rs, "id");
		this.date = DBUtil.getDate(rs, "post_date");
		this.type = 1; // DBUtil.getInt(rs, "type");
		this.title = DBUtil.getString(rs, "post_title");
		this.locale = "de"; // DBUtil.getString(rs, "locale");
		this.typetext = DBUtil.getString(rs, "tn");
		this.slug = DBUtil.getString(rs, "slug");
		this.text = DBUtil.getString(rs, "post_content");
		modifyText();
	}

	private void modifyText() {
		if (text != null) {
			text = text.replaceAll("<[^>]*>", "");
			if (text.length() > MAX_TEXT_LENGTH) {
				int pos = text.indexOf(" ", MAX_TEXT_LENGTH);
				if (pos == -1)
					pos = MAX_TEXT_LENGTH;
				text = text.substring(0, pos) + " [...]";
			}
		}
	}

	public String getText() {
		return text;
	}

	public String getTitle() {
		return title;
	}

	public Date getDate() {
		return date;
	}

	public int getType() {
		return type;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void store() throws DBException {
		BrainDB db = brainSystem.getBrainDB();
		db.addNews(this);
	}

	public void modify() {
		System.out.println("modifiy: " + id);
		date_o = date;
		text_o = text;
		type_o = type;
		title_o = title;
		locale_o = locale;
	}

	public String rollbackAction() {
		System.out.println("rb: " + id);
		date = date_o;
		text = text_o;
		type = type_o;
		title = title_o;
		locale = locale_o;
		return "ok";
	}

	public String saveAction() throws DBException {
		System.out.println("store: " + id);
		store();
		return "ok";
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getSlug() {
		return slug;
	}

}