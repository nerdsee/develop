package org.stoevesand.brain.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSession;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;

@ManagedBean(name="LastUserItem")
@SessionScoped
@XmlRootElement(name = "useritem")
@XmlAccessorType( XmlAccessType.NONE )
public class UserItem extends AbstractContent{

	private static Logger log = LogManager.getLogger(UserItem.class);

	private final int MAX_COUNTED_LEVEL = 5;
	private final int MAX_SHOWN_LEVEL = 5;

	@XmlElement
	long id = 0;
	long itemId = 0;

	@XmlElement
	int level = 0;
	int previous_level = 0;
	@XmlElement
	int wright = 0;
	@XmlElement
	int wrong = 0;
	Date last = null;
	Date next = null;
	private long userLessonId;
	private Item item = null;

	public UserItem() {
	}

	public UserItem(long id, long itemId) {
		this.id = id;
		this.itemId = itemId;
	}

	public long getItemId() {
		return itemId;
	}

	public Date getLast() {
		return last;
	}

	public long getLevel() {
		return level;
	}

	public long getShowLevel() {
		return level < MAX_SHOWN_LEVEL ? level : MAX_SHOWN_LEVEL;
	}

	public Date getNext() {
		return next;
	}

	public UserItem(ResultSet rs) throws SQLException {
		this.id = rs.getLong("id");
		this.itemId = rs.getLong("itemID");
		this.level = rs.getInt("level");
		this.wright = rs.getInt("wright");
		this.wrong = rs.getInt("wrong");
		this.last = rs.getTimestamp("last");
		this.next = rs.getTimestamp("next");
		this.userLessonId = rs.getLong("userlessonID");
	}

	public UserItem(UserItem current) {
		this.id = current.id;
		this.itemId = current.itemId;
		this.level = current.level;
		this.last = current.last;
		this.next = current.next;
		this.wright = current.wright;
		this.wrong = current.wrong;
		this.userLessonId = current.userLessonId;
	}

	public UserLesson getUserLesson() throws DBException {
		UserLesson userLesson=null;
		try {
			userLesson = BrainSession.getBrainSession().getCurrentUserLesson();
		} catch(Exception e) {
			User cu = BrainSession.getBrainSession().getCurrentUser();
			userLesson = BrainSystem.getBrainSystem().getBrainDB().getUserLesson(cu, userLessonId);
		}
		log.debug("getUserLesson: " + userLesson);
		return userLesson;
	}

	// für den aufruf aus der REST API
	public UserLesson getUserLesson(User user) throws DBException {
		UserLesson userLesson=null;
		try {
			userLesson = BrainSystem.getBrainSystem().getBrainDB().getUserLesson(user, userLessonId);
		} catch(Exception e) {
		}
		log.debug("getUserLesson (API): " + userLesson);
		return userLesson;
	}

	public long getId() {
		return id;
	}

	@XmlElement
	public Item getItem() throws DBException {
		if (item == null) {
			item = BrainSystem.getBrainSystem().getBrainDB().getItem(itemId);
		}
		log.debug("getItem: " + item);
		return item;
	}

	public boolean checkAnswerText(String answerText) throws DBException {
		Item item = getItem();

		if (item.equals(answerText)) {
			log.debug("correct");
			learned();
			return true;
		} else {
			log.debug("falsch");
			forgotten();
			return false;
		}
	}

	/**
	 * Diese Funktion wird von der API aufgerufen
	 * @throws DBException
	 */
	public void knowAnswer() throws DBException {
		learned();
	}
	// REST API
	public void knowAnswer(User user) throws DBException {
		learned(user);
	}

	/**
	 * Diese Funktion wird von der API aufgerufen
	 * @throws DBException
	 */
	public void failAnswer() throws DBException {
		forgotten();
	}
	// REST API
	public void failAnswer(User user) throws DBException {
		forgotten(user);
	}

	// public String forgotAnswer() {
	// answerText = "<ohne Eingabe>";
	// log.debug("falsch");
	// forgotten();
	// return "wrong";
	// }

	public void forceAnswer() throws DBException {
		log.debug("answer forced");
		UserLesson userLesson = getUserLesson();
		userLesson.getUserStats().invalidate();
		level = previous_level;
		wrong--;
		wright++;
		learned();
	}

	public void editItem() throws DBException {
		log.debug("edit item");
		Item item = getItem();
		item.modify();
	}

	public void deactivate() throws DBException {
		BrainSystem.getBrainSystem().getBrainDB().deactivateUserItem(this);
	}

	private void forgotten() throws DBException {
		UserLesson userLesson = getUserLesson();
		forgotten(userLesson);
	}

	// REST API
	private void forgotten(User user) throws DBException {
		UserLesson userLesson = getUserLesson(user);
		forgotten(userLesson);
	}

	private void forgotten(UserLesson userLesson) throws DBException {
		wrong++;
		
		if (level == 0) {
			int level0 = userLesson.getLevel0();
			if (level0 <= userLesson.getMaxItemsLevel0()) {
				userLesson.activateUserItem();
			}
		}
		
		previous_level = level;
		userLesson.getUserStats().invalidate();
		level = 0;

		long breakTime = userLesson.getBreakTime(level);
		last = new Date(System.currentTimeMillis());
		next = new Date(System.currentTimeMillis() + (breakTime));
		update();
	}

	private void learned() throws DBException {
		UserLesson userLesson = getUserLesson();
		learned(userLesson);
	}

	private void learned(User user) throws DBException {
		UserLesson userLesson = getUserLesson(user);
		learned(userLesson);
	}

	private void learned(UserLesson userLesson) throws DBException {

		wright++;

		if (level == 0) {
			int level0 = userLesson.getLevel0();
			if (level0 <= userLesson.getMaxItemsLevel0()) {
				userLesson.activateUserItem();
			}
		}

		if (level < MAX_COUNTED_LEVEL)
			userLesson.getUserStats().invalidate();
		level++;

		long breakTime = userLesson.getBreakTime(level < MAX_COUNTED_LEVEL ? level : MAX_COUNTED_LEVEL);
		last = new Date(System.currentTimeMillis());
		next = new Date(System.currentTimeMillis() + (breakTime));
		update();
	}

	private void update() throws DBException {
		BrainSystem.getBrainSystem().getBrainDB().update(this);
	}

	public String toXML() {
		return toXML(true);
	}

	public String toXML(boolean showAnswers) {
		StringBuffer buffer = new StringBuffer();
		// buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		buffer.append("<useritem id=\"" + getId() + "\">");
		try {
			buffer.append(getItem().toXML(showAnswers));
		} catch (DBException e) {
			e.printStackTrace();
		}
		buffer.append("</useritem>");
		return buffer.toString();
	}

	public String toJSON(boolean showAnswers) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{ ");
		buffer.append(" \"id\" : " + getId() + ", ");
		buffer.append(" \"level\" : " + getLevel() + ", ");
		buffer.append(" \"item\" : ");
		try {
			buffer.append(getItem().toJSON(showAnswers));
		} catch (DBException e) {
			e.printStackTrace();
		}
		buffer.append("}");
		return buffer.toString();
	}

	public int getWright() {
		return wright;
	}

	public int getWrong() {
		return wrong;
	}

	public void storeComment(String comment, String commentType) throws DBException {
		BrainSystem.getBrainSystem().getBrainDB().storeComment(this, comment, commentType);
		log.debug("commentItem: (" + commentType + ") " + comment);
	}
}
