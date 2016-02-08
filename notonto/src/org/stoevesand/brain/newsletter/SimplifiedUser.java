package org.stoevesand.brain.newsletter;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.util.DBUtil;

public class SimplifiedUser {

	private static Logger log = LogManager.getLogger(SimplifiedUser.class);

	String name = "";
	String nick = null;
	String password = "";
	String statusLang="de";

	String unlockString = "";
	boolean unlocked = false;
	// Vector<UserLesson> lessons = null;
	long id = 0;
	long groupID = 0;
	short type = 0;

	int statusMailFreq = 0;

	
	public SimplifiedUser(ResultSet rs) throws SQLException {
		this.id = DBUtil.getLong(rs, "id");
		this.name = DBUtil.getString(rs, "name");
		this.nick = DBUtil.getString(rs, "nick");
		this.statusMailFreq = DBUtil.getInt(rs, "newsletter");
		this.statusLang = DBUtil.getString(rs, "status_lang");
	}

	public long getId() {
		return id;
	}

	public int getStatusMailFreq() {
		return statusMailFreq;
	}

	public String getName() {
		return name;
	}

	public String getNick() {
		return nick;
	}

	public String getStatusLang() {
		return statusLang;
	}

}
