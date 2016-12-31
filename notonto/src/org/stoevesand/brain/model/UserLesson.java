package org.stoevesand.brain.model;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainMessage;
import org.stoevesand.brain.BrainSession;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.UserLessonConfig;
import org.stoevesand.brain.UserStats;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.DBUtil;

@XmlRootElement(name = "userlesson")
public class UserLesson {

	/*
	 * Neue Spalten:
	 */

	// private static final int TARGET_DATE = 1;
	private static final int INTERVALL_STD = 0;
	private static final int INTERVALL_TARGET_DATE = 1;

	private static Logger log = LogManager.getLogger(UserLesson.class);

	@XmlElement
	long id = 0;

	long userId = 0;
	long lessonId = 0;

	// boolean dirty = false;

	/**
	 * 0=standard, 1=Zieltermin
	 */
	short intervallType = 0; // 0=standard 1=Zieltermin

	public boolean isIntervallType() {
		return intervallType == INTERVALL_TARGET_DATE;
	}

	public void setIntervallType(short intervallType) {
		this.intervallType = intervallType;
		if (this.intervallType == INTERVALL_STD) {
			setRealTargetDate(null);
		}
	}

	private Lesson lesson = null;

	UserItem currentUserItem = null;
	UserStats userStats = null;
	UserLessonConfig config = new UserLessonConfig(this);

	// String breakTimeText = null;
	long breakTimes[] = new long[10];

	private long intervallUnit;

	public long getIntervallUnit() {
		return intervallUnit;
	}

	private Date realTargetDate;

	public Date getRealTargetDate() {
		return realTargetDate;
	}

	public void setRealTargetDate(Date rtd) {
		realTargetDate = rtd;
	}

	@XmlElement
	public UserLessonConfig getConfig() {
		return config;
	}

	UserLesson() {
		userStats = new UserStats(this);
	}

	public UserLesson(ResultSet rs, Lesson lesson) {
		this.id = DBUtil.getLong(rs, "ulid");
		this.lessonId = DBUtil.getLong(rs, "lessonID");
		this.userId = DBUtil.getLong(rs, "userID");
		this.intervallUnit = DBUtil.getLong(rs, "interval_unit");
		this.realTargetDate = DBUtil.getDate(rs, "target_date");
		this.intervallType = DBUtil.getShort(rs, "intervall_type");

		this.lesson = lesson;

		calcBreakTimes();
		userStats = new UserStats(this);
	}

	public UserStats getUserStats() {
		return userStats;
	}

	public long getId() {
		return id;
	}

	@XmlElement
	public Lesson getLesson() throws DBException {
		if (lesson == null)
			lesson = BrainSystem.getBrainSystem().getBrainDB().getLesson(lessonId);
		return lesson;
	}

	public void deactivateCurrentItem() throws DBException {
		currentUserItem.deactivate();
		activateUserItem();
	}

	public void activateUserItem() throws DBException {
		BrainSystem.getBrainSystem().getBrainDB().activateUserItemExp(this);
		userStats.invalidate();
	}

	public UserItem getCurrentUserItem() {
		return currentUserItem;
	}

	// public String knowAnswer() {
	// }

	public User getUser() throws DBException {
		return BrainSystem.getBrainSystem().getBrainDB().getUser(userId);
	}

	public int getLevel0() throws DBException {
		return userStats.getLevel0(this);
	}

	public int getLevel1() throws DBException {
		return userStats.getLevel1(this);
	}

	public int getLevel2() throws DBException {
		return userStats.getLevel2(this);
	}

	public int getLevel3() throws DBException {
		return userStats.getLevel3(this);
	}

	public int getLevel4() throws DBException {
		return userStats.getLevel4(this);
	}

	public int getLevel5() throws DBException {
		return userStats.getLevel5(this);
	}

	public int getScore() throws DBException {
		return userStats.getScore();
	}

	public String saveIntervalls() {
		String ret = "user";
		if (intervallType == INTERVALL_STD) {
			// targetDate = "";
			realTargetDate = null;
			store();
		} else {
			BrainMessage msg = BrainSession.getBrainSession().getBrainMessage();
			if (realTargetDate == null) {
				FacesContext facesContext = FacesContext.getCurrentInstance();
				facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bitte geben Sie ein Datum ein.", "Bitte geben Sie ein Datum ein."));
				return null;
			}

			long now = (new Date()).getTime();
			if (realTargetDate.getTime() < now) {
				FacesContext facesContext = FacesContext.getCurrentInstance();
				facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Das Datum muss mindestens morgen sein.", "Das Datum muss mindestens morgen sein."));
				return null;
			}

			long diff = realTargetDate.getTime() - now;
			intervallUnit = diff / 364;
			calcBreakTimes();
			store();
			log.info("New target date: " + realTargetDate.toString());
		}

		return ret;
	}

	private void store() {
		try {
			BrainSystem.getBrainSystem().getBrainDB().storeUserLesson(this);
		} catch (DBException e) {
			log.error("Error updating intervalls.", e);
		}
	}

	public String reset() {
		try {
			BrainSystem.getBrainSystem().getBrainDB().resetUserLesson(this);
		} catch (DBException e) {
			log.error("Failed to reset UserLesson.", e);
		}
		userStats.invalidate();
		return "user";
	}

	private void calcBreakTimes() {
		long tag = 1000 * 60 * 60 * 24;
		long min10 = 1000 * 60 * 10;
		breakTimes[1] = intervallUnit > tag ? min10 : 0;
		breakTimes[1] = intervallUnit * 1;
		breakTimes[2] = intervallUnit * 3;
		breakTimes[3] = intervallUnit * 9;
		breakTimes[4] = intervallUnit * 27;
		breakTimes[5] = intervallUnit * 81;
		breakTimes[6] = intervallUnit * 243;
		breakTimes[7] = breakTimes[6];
		breakTimes[8] = breakTimes[6];
		breakTimes[9] = breakTimes[6];
	}

	@XmlTransient
	public String getNextUserItemTimeDiff() throws DBException {
		System.out.println("gnt1");
		long now = System.currentTimeMillis();

		System.out.println("gnt2");
		long ms = BrainSystem.getBrainSystem().getBrainDB().getNextUserItemTimeDiff(id);
		System.out.println("gnt3");
		ms = ms - now;
		long sec = (ms < 1000 ? 1000 : ms) / 1000;
		long d = sec / 86400;
		sec = sec % 86400;
		long h = sec / 3600;
		sec = sec % 3600;
		long m = sec / 60;
		sec = sec % 60;

		System.out.println("TS: " + ms);

		ResourceBundle rb = null;
		if (BrainSession.getBrainSession() != null)
			rb = BrainSession.getBrainSession().getResourceBundle();
		else
			rb = ResourceBundle.getBundle("org.stoevesand.brain.i18n.MessagesBundle", new Locale("en", "EN"));

		System.out.println("bss: " + BrainSession.getBrainSession());
		System.out.println("rb: " + rb);

		String text = rb.getString("no_items_date");
		String ret = MessageFormat.format(text, d, h, m, sec);
		// String ret = "" + d + " Tag" + (d == 1 ? "" : "e") + ", " + h + ":" +
		// m +
		// ":" + sec;

		System.out.println("ret: " + ret);

		return ret;
	}

	public String toXML() {
		StringBuilder buf = new StringBuilder();
		try {
			buf.append("<UserLesson id=\"" + getId() + "\">");
			buf.append("<description>");
			buf.append(getLesson().getDescription());
			buf.append("</description>");
			buf.append("<title>");
			buf.append(getLesson().getTitle());
			buf.append("</title>");
			buf.append("<available>");
			buf.append(getAvailable());
			buf.append("</available>");
			buf.append("</UserLesson>");
		} catch (Exception e) {
		}
		return buf.toString();
	}

	public int getAvailable() {
		BrainDB db = BrainSystem.getBrainSystem().getBrainDB();
		int i = 0;
		try {
			i = db.getUserLessonAvailable(this);
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}

	public String toJSON() {
		StringBuilder buf = new StringBuilder();
		try {
			buf.append("{ ");
			buf.append(" \"id\" : " + getId() + ", ");
			buf.append(" \"description\" : \"");
			buf.append(getLesson().getDescription());
			buf.append("\", ");
			buf.append(" \"title\" : \"");
			buf.append(getLesson().getTitle());
			buf.append("\", ");
			buf.append(" \"lesson\" : ");
			buf.append(getLesson().toJSON());
			buf.append(", ");
			buf.append(" \"type\" : ");
			buf.append(getLesson().getLessonType());
			buf.append(", ");
			buf.append(" \"showPinyin\" : ");
			buf.append(getConfig().getShowPinyin() ? true : false);
			buf.append(", ");
			buf.append(" \"available\" : ");
			buf.append(getAvailable());
			buf.append(" } ");

			// BrainSession.currentUserLesson.config.showPinyin

		} catch (Exception e) {
		}
		return buf.toString();
	}

	@XmlTransient
	public String getIdList() {
		return "(" + id + ")";
	}

	@XmlTransient
	public UserItem getNextUserItem() throws DBException {
		log.debug("get next ui...");
		// FacesContext context = FacesContext.getCurrentInstance();

		// userStats.invalidate();

		currentUserItem = BrainSystem.getBrainSystem().getBrainDB().getNextUserItem(this);
		// if (context != null)
		// BrainSystem.storeBeanFromFacesContext("CurrentUserItem",context,
		// currentUserItem);
		BrainSession.getBrainSession().setCurrentUserItem(currentUserItem);
		return currentUserItem;
	}

	@XmlTransient
	public Date getNextUserItemTime() throws DBException {
		Date ret = BrainSystem.getBrainSystem().getBrainDB().getNextUserItemTime(id);
		return ret;
	}

	@XmlTransient
	public int getMaxItemsLevel0() {
		int ret = 0;

		try {
			ret = getLesson().getMaxItemsLevel0();
		} catch (DBException e) {
		}

		return ret;
	}

	public long getBreakTime(int i) {
		long breakTime = 0;
		if (intervallType == INTERVALL_TARGET_DATE) {
			breakTime = breakTimes[i];
		} else {
			breakTime = BrainSystem.getBrainSystem().getBreakTime(i);
		}
		return breakTime;
	}

}
