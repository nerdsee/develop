package org.stoevesand.brain.model;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.DBUtil;
import org.stoevesand.util.StringUtils;

import net.sourceforge.pinyin4j.PinyinFormatter;

@XmlRootElement(name = "item")
public class Item implements Comparable<Item> {

	private static Logger log = LogManager.getLogger(Item.class);

	private String text = null;
	private String comment = null;
	private Vector<Answer> answers = null;

	private long lessonId = 0;
	private long id = 0;
	private int chapter = 1;
	private boolean dirty;
	private String oldText;

	private long extId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Item(long id, long lessonId, String text) {
		this.text = text;
		this.id = id;
		this.lessonId = lessonId;
	}

	public Item(Lesson lesson) {
		this.id = -1;
		this.lessonId = lesson.getId();
		this.chapter = lesson.getHighestChapter();
		this.answers = new Vector<Answer>();
	}

	public Item(ResultSet rs) {
		this.text = DBUtil.getString(rs, "text");
		this.comment = DBUtil.getString(rs, "comment");
		this.id = DBUtil.getLong(rs, "id");
		this.extId = DBUtil.getLong(rs, "extID");
		this.lessonId = DBUtil.getLong(rs, "lessonID");
		this.chapter = DBUtil.getInt(rs, "chapter");
	}

	public Item() {
		answers = new Vector<Answer>();
		id = -1;
		dirty = true;
	}

	public Item(String text) {
		this.text = text;
		answers = new Vector<Answer>();
		id = -1;
		dirty = true;
	}

	public Item(String text, String comment, long extId, int chapter) {
		this.text = text;
		this.comment = comment;
		this.extId = extId;
		this.chapter = chapter;
		answers = new Vector<Answer>();
		dirty = true;
	}

	public void store() throws DBException {
		if (dirty) {
			BrainDB db = BrainSystem.getBrainSystem().getBrainDB();

			db.addItem(this);
			log.debug("Item: " + text + " (" + id + ")");

			// Iterator it = answers.iterator();
			// while(it.hasNext()) {
			// Answer answer = (Answer)it.next();
			// log.debug("\tAnswer: " + answer.getText() +
			// (answer.isVisible()?"":"*"));
			// }
		} else
			log.debug("not dirty");
		dirty = false;
	}

	public void delete() throws DBException {
		BrainDB db = BrainSystem.getBrainSystem().getBrainDB();
		db.deleteItem(this);
		log.debug("Delete Item: " + text + " (" + id + ")");
		dirty = false;
	}

	// public Answer getAnswer() {
	// log.debug("getAnswer " + answerId);
	// return brainSystem.getBrainDB().getAnswer(answerId);
	// }

	public String getText() {
		return text;
	}

	public String getFormattedText() {
		return getFormattedText("", false);
	}

	public String getFormattedText(String prefix, boolean toXML) {
		return StringUtils.formatWebString(text, prefix, toXML);
	}

	public void setText(String text) {
		log.debug("Text set: " + text);
		this.text = text;
	}

	public Vector<Answer> getAnswers() throws DBException {
		if (answers == null) {
			if (id < 0)
				return new Vector<Answer>();
			else
				answers = BrainSystem.getBrainSystem().getBrainDB().getAnswers(id);
		}
		return answers;
	}

	public int getAnswersSize() throws DBException {
		return getAnswers().size();
	}

	public void setAnswers(Vector<Answer> answers) {
		this.answers = answers;
	}

	public void appendAnswers(Vector<Answer> newAnswers) {
		if (this.answers == null)
			this.answers = newAnswers;
		else {
			for (Answer a : newAnswers) {
				if (!answers.contains(a))
					this.answers.add(a);
			}
		}
	}

	public long getLessonId() {
		return lessonId;
	}

	public boolean equals(String test) throws DBException {

		// log.debug("entered: " + test);
		// System.out.println("E: " + test);

		log.debug("check answers");
		Iterator<Answer> it = getAnswers().iterator();
		while (it.hasNext()) {
			Answer answer = (Answer) it.next();
			log.debug("check " + answer.getText());

			// System.out.println("A: " + answer.getText());

			if (answer.getText().equals(test))
				return true;
		}

		log.debug("false");

		return false;
	}

	public boolean equals(Item item2) {
		return (extId == item2.extId);
	}

	public String toXML() {
		return toXML(true);
	}

	public String toXML(boolean showAnswers) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<item id=\"" + getId() + "\" extid=\"" + getExtId() + "\" chapter=\"" + getChapter() + "\">");
		buffer.append("<text><![CDATA[");
		buffer.append(text);
		buffer.append("]]></text>\n");
		buffer.append("<comment><![CDATA[");
		buffer.append(comment);
		buffer.append("]]></comment>\n");

		if (showAnswers) {
			buffer.append("<answers>\n");

			try {
				for (Answer a : getAnswers()) {
					String answer = a.getText().trim();
					buffer.append("<answer type=\"" + a.getType() + "\"><![CDATA[");
					buffer.append(answer);
					buffer.append("]]></answer>\n");
				}
			} catch (DBException e) {
				log.error("failed to load answers.");
			}
			buffer.append("</answers>\n");
		}

		buffer.append("</item>\n");
		return buffer.toString();
	}

	public String toJSON(boolean showAnswers) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append(" \"id\" : ").append(getId()).append(", ");
		buffer.append(" \"text\" : \"" + getFormattedText("html:", true) + "\", ");
		buffer.append(" \"comment\" : \"" + getComment() + "\", ");
		buffer.append(" \"answers\" : [");

		try {
			Iterator<Answer> it = getAnswers().iterator();
			while (it.hasNext()) {
				Answer answer = it.next();
				buffer.append(answer.toJSON());
				if (it.hasNext())
					buffer.append(",");
			}
		} catch (DBException e) {
			e.printStackTrace();
		}

		buffer.append(" ]");
		buffer.append("}");
		return buffer.toString();
	}

	public String toString() {
		return "Item " + text;
	}

	public void addAnswer(Answer answer) {
		answers.add(answer);
	}

	public void setLessonId(long lessonId) {
		this.lessonId = lessonId;
	}

	public void modify() {
		log.debug("modify: " + this);
		oldText = text;
		dirty = true;
	}

	public String rollbackAction() {
		text = oldText;
		newAnswerText = "";
		answers = null;
		dirty = false;
		return "ok";
	}

	public String saveAction() throws DBException {
		if (id == -1)
			splitAnswers();
		cleanAnswers();
		log.debug("store: " + this);
		store();
		newAnswerText = "";
		return "ok";
	}

	private void splitAnswers() {
		if (newAnswerText.length() > 0) {
			String[] sa = newAnswerText.split("\n");
			for (int i = 0; i < sa.length; i++) {
				Answer a = new Answer(sa[i], true, 0);
				answers.add(a);
			}
			newAnswerText = "";
		}
	}

	private void cleanAnswers() throws DBException {
		Vector<Answer> na = new Vector<Answer>();
		Iterator<Answer> it = getAnswers().iterator();
		while (it.hasNext()) {
			Answer a = it.next();
			if (!a.isEmpty()) {
				na.add(a);
			}
		}
		if (newAnswerText.length() > 0) {
			Answer a = new Answer(newAnswerText, true, 0);
			na.add(a);
		}
		answers = na;
	}

	String newAnswerText = "";

	public String getNewAnswerText() {
		return newAnswerText;
	}

	public void setNewAnswerText(String newAnswerText) {
		this.newAnswerText = newAnswerText.trim();
	}

	@XmlElement(name = "phonetic")
	public String getComment() {
		String ret = "";
		if (comment != null)
			ret = PinyinFormatter.convertToneNumber2ToneMark(comment);
		return ret;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean matches(String filter) {
		filter = filter.toUpperCase();
		if (getText().toUpperCase().indexOf(filter) >= 0)
			return true;

		try {
			for (Answer a : getAnswers()) {
				if (a.getText().toUpperCase().indexOf(filter) >= 0)
					return true;
			}
		} catch (DBException e) {
			log.error("no answers found: " + e.toString());
		}

		return false;
	}

	public long getExtId() {
		return extId;
	}

	public void setExtId(long extId) {
		this.extId = extId;
	}

	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	// nur auf chapter prüfen, wenn dies explizit übergeben wurde!
	public boolean equals(Item item, boolean checkChapter) {

		if (item == null)
			return false;
		if (!text.equals(item.getText()))
			return false;
		if ((comment != null) && (item.getComment() != null))
			if (!comment.equals(item.getComment()))
				return false;

		// check answers
		try {
			if (getAnswersSize() != item.getAnswersSize())
				return false;

			for (Answer answer : getAnswers()) {
				if (!item.getAnswers().contains(answer))
					return false;
			}

		} catch (DBException e) {
			return false;
		}
		// if (t) {
		// System.out.println("answers OK");
		// }

		// check chapter
		if (checkChapter && (chapter != item.getChapter()))
			return false;
		// if (t) {
		// System.out.println("chapter OK");
		// }

		return true;
	}

	@Override
	public int compareTo(Item o) {

		if (id < o.id) {
			return -1;
		} else if (id > o.id) {
			return 1;
		} else
			return 0;
	}

}
