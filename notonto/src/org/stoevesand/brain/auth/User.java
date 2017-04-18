package org.stoevesand.brain.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSession;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.DBUtil;

@ManagedBean
@SessionScoped
public class User {
	private static Logger log = LogManager.getLogger(User.class);

	@ManagedProperty(value = "#{brainSystem}")
	private BrainSystem brainSystem;

	// @ManagedProperty(value = "#{brainSession}")
	// private BrainSession brainSession;

	public void setBrainSystem(BrainSystem bs) {
		this.brainSystem = bs;
	}

	// public void setBrainSession(BrainSession bs) {
	// this.brainSession = bs;
	// }

	final static int USER_TYPE_ADMIN = 9;
	final static int USER_TYPE_TEACHER = 7;
	final static int USER_TYPE_USER = 0;

	String name = "";
	String nick = null;
	String password = "";
	String passtmp = "";
	String unlockString = "";
	String statusLang = "de";
	String prefix = "";

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	private boolean unlocked = false;
	// Vector<UserLesson> lessons = null;
	long id = 0;
	long groupID = 0;
	short type = 0;

	int statusMailFreq = 0;

	public int getStatusMailFreq() {
		return statusMailFreq;
	}

	public void setStatusMailFreq(int statusMailFreq) {
		this.statusMailFreq = statusMailFreq;
	}

	Date registerDate = null;
	Date lastLoginDate = null;

	boolean dirty = false;
	boolean loggedIn = false;
	private boolean valid = false;

	public boolean isValid() {
		return valid;
	}

	public User() {
	}

	public User(long id, String name) {
		this.id = id;
		this.name = name;
		this.type = USER_TYPE_USER;
	}

	public void initUser(String name, String password, String unlockString, boolean unlocked) {
		this.id = -1;
		this.name = name;
		this.password = password;
		this.unlockString = unlockString;
		this.setUnlocked(unlocked);
		this.registerDate = new Date();
		this.type = USER_TYPE_USER;
		dirty = true;
	}

	public void store() throws DBException {
		if (dirty) {
			BrainDB db = brainSystem.getBrainDB();
			db.addUser(this);
		}
		dirty = false;
	}

	public void unlock() throws DBException {
		BrainDB db = brainSystem.getBrainDB();
		db.unlockUser(this);
	}

	public void initUser(ResultSet rs) throws SQLException {
		this.id = DBUtil.getLong(rs, "id");
		this.name = DBUtil.getString(rs, "name");
		this.nick = DBUtil.getString(rs, "nick");
		this.password = DBUtil.getString(rs, "password");
		this.passtmp = DBUtil.getString(rs, "passtmp");
		this.unlockString = DBUtil.getString(rs, "unlocktext");
		this.setUnlocked(DBUtil.getBoolean(rs, "unlocked"));
		this.registerDate = DBUtil.getTimestamp(rs, "register_date");
		this.lastLoginDate = DBUtil.getTimestamp(rs, "lastlogin_date");
		this.type = DBUtil.getShort(rs, "usertype");
		this.groupID = DBUtil.getLong(rs, "groupID");
		this.statusMailFreq = DBUtil.getInt(rs, "newsletter");
		this.statusLang = DBUtil.getString(rs, "status_lang");
		this.prefix = DBUtil.getString(rs, "prefix");

		this.valid = true;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getUnlocked() {
		return isUnlocked();
	}

	Vector<UserLesson> userLessons = null;

	public Vector<UserLesson> getLessons() {
		try {
			if ((userLessons == null) || (userLessons.size() == 0))
				userLessons = brainSystem.getBrainDB().getUserLessons(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return userLessons;
	}

	public UserLesson subscribeLesson(Lesson lesson) throws DBException {
		UserLesson ret = null;

		UserLesson ul = brainSystem.getBrainDB().getUserLessonByLessonID(this, lesson.getId());

		if (ul == null) {
			ret = brainSystem.getBrainDB().subscribeLesson(this, lesson);
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Sie haben diese Lektion bereits abonniert."));
		}

		userLessons = null;
		return ret;
	}

	public boolean getIsAdmin() {
		return (type == USER_TYPE_ADMIN);
	}

	public boolean getIsTeacher() {
		return (type == USER_TYPE_ADMIN) || (type == USER_TYPE_TEACHER);
	}

	public void unsubscribeLesson(UserLesson lesson) throws DBException {
		// UserLesson userLesson =

		brainSystem.getBrainDB().unsubscribeLesson(this, lesson);
		// lessons.add(userLesson);
		// brainSession.loadLibrary();
		userLessons = null;
	}

	public void unsubscribeLesson(Lesson lesson) throws DBException {
		// UserLesson userLesson =
		brainSystem.getBrainDB().unsubscribeLesson(this, lesson);
		// lessons.add(userLesson);
		// brainSession.loadLibrary();
		userLessons = null;
	}

	public int getAvailable() {
		int ret = 0;
		try {
			ret = brainSystem.getBrainDB().getUserAvailable(this);
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public int getScore() {
		int ret = 0;
		try {
			ret = BrainSystem.getBrainSystem().getBrainDB().getUserScore(this);
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	// TODO: schlechter code, besser gleich an die DB
	public boolean hasLesson(Lesson lesson) {
		try {
			Vector<UserLesson> lessons = getLessons();
			Iterator<UserLesson> it = lessons.iterator();
			while (it.hasNext()) {
				UserLesson userLesson = it.next();
				if (userLesson.getLesson().getId() == lesson.getId())
					return true;
			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public UserLesson getUserLesson(long ulid) {
		UserLesson userLesson = null;
		try {
			userLesson = brainSystem.getBrainDB().getUserLesson(this, ulid);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return userLesson;
	}

	public UserLesson getUserLessonByLessonID(long lid) {
		UserLesson userLesson = null;
		try {
			userLesson = brainSystem.getBrainDB().getUserLessonByLessonID(this, lid);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return userLesson;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasstmp() {
		return passtmp;
	}

	public void setPasstmp(String passtmp) {
		this.passtmp = passtmp;
	}

	public String getUnlock() {
		return unlockString;
	}

	public void storeScore() throws DBException {
		brainSystem.getBrainDB().storeUserScore(this, getScore());
	}

	public Date getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public void storeLastLogin() throws DBException {
		lastLoginDate = new Date();
		brainSystem.getBrainDB().storeLastLogin(this);
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public long getGroupID() {
		return groupID;
	}

	public void setGroupID(long groupID) {
		this.groupID = groupID;
	}

	public String getStatusLang() {
		return statusLang;
	}

	public void setStatusLang(String statusLang) {
		this.statusLang = statusLang;
	}

	public void loadUser(String username) {
		try {
			brainSystem.getBrainDB().loadUser(this, username);
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}

	public void acceptInvitation(String inviteeEmail, String inviteeCode) {

		Vector<Lesson> lessons;
		try {
			lessons = brainSystem.getBrainDB().getLessonsByCode(inviteeCode);
			if (lessons != null) {
				for (Lesson lesson : lessons) {
					subscribeLesson(lesson);
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Info", "Diese Lektion ist unbekannt."));
			}
		} catch (DBException e) {
			e.printStackTrace();
		}
		return;
	}

	public void acceptPasstmp() {
		try {
			brainSystem.getBrainDB().acceptPasstmp(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return;
	}

}
