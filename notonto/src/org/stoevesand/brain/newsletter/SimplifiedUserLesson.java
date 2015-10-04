package org.stoevesand.brain.newsletter;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.logging.Logger;

public class SimplifiedUserLesson {

	private static Logger log = Logger.getLogger(SimplifiedUserLesson.class);
	private long id;
	private long lessonId;
	private String description;
	private int available=0;

	private int[] levels = { 0, 0, 0, 0, 0, 0, 0 };

	final static int MAX_LEVELS = 5;

	public SimplifiedUserLesson(ResultSet rs) throws SQLException {
		super();
		this.id = rs.getLong("ulid");
		this.lessonId = rs.getLong("lid");
		this.description = rs.getString("description");
	}

	public long getId() {
		return id;
	}

	public long getLessonId() {
		return lessonId;
	}

	public String getDescription() {
		return description;
	}

	public void setLevel(ResultSet rs) {
		try {
			int level = rs.getInt("level");

			levels[level < MAX_LEVELS ? level : MAX_LEVELS] += rs.getInt("cx");
		} catch (Exception e) {
			log.error("setLevel", e);
		}
	}

	public int getLevel(int i) {
		int ret = 0;
		try {
			ret = levels[i];
		} catch (Exception e) {
			log.error("getLevel", e);
		}
		return ret;
	}

	public int getScore() {
		int ret=levels[1]+2*levels[2]+3*levels[3]+4*levels[4]+5*levels[5];
		return ret;
	}

	public void setAvailable(int ret) {
		available=ret;
	}
	public int getAvailable() {
		return available;
	}
}
