package org.stoevesand.brain.model;

import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.logging.Logger;
import org.stoevesand.util.DBUtil;

@XmlRootElement(name = "answer")
@XmlAccessorType(XmlAccessType.NONE)
public class Answer {

	private static Logger log = Logger.getLogger(Answer.class);

	@XmlElement
	private String text = null;

	@XmlElement
	private String phonetic = null;

	private boolean visible;

	@XmlElement
	private int type;

	public boolean isVisible() {
		return visible;
	}

	Answer() {
	}

	public Answer(String text) {
		if ((text != null) && (text.trim().length() > 0)) {
			if (text.charAt(0) == '~') {
				this.visible = false;
				this.text = (text.length() > 1) ? text.substring(1) : "";
			} else {
				this.visible = true;
				this.text = text;
			}
			this.type = 0;
		}
	}

	public Answer(String text, boolean visible, int type) {
		this.text = text;
		this.visible = visible;
		this.type = type;
	}

	public Answer(ResultSet rs) {
		this.text = DBUtil.getString(rs, "text");
		this.visible = DBUtil.getBoolean(rs, "visible");
		this.type = DBUtil.getInt(rs, "type");
		this.text = pinyinFix(this.text);
		splitPhonetic(this.text);
	}

	private void splitPhonetic(String t) {
		try {
			String patternStr = "(.*)(\\[.*\\])";
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(t);
			boolean matchFound = matcher.find();
			if (matchFound) { // Get all groups or this match
				text = matcher.group(1).trim();
				phonetic = matcher.group(2);
			}
		} catch (Exception e) {
			log.debug("exc in splitphonetic: " + e);
		}
	}

	private String pinyinFix(String text) {
		text = text.replace('ă', 'ǎ');
		text = text.replace('ĕ', 'ě');
		text = text.replace('ĭ', 'ǐ');
		text = text.replace('ŏ', 'ǒ');
		text = text.replace('ŭ', 'ǔ');
		return text;
	}

	public String getTextWithPhonetic() {
		String ret = text;
		if (phonetic != null)
			ret += " " + phonetic;
		return ret;
	}

	public void setTextWithPhonetic(String t) {
		splitPhonetic(t);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text.trim();
	}

	public int getType() {
		return type;
	}

	public String getPhonetic() {
		return phonetic == null ? "" : phonetic;
	}

	public boolean isEmpty() {
		return text.length() == 0;
	}

	public String toJSON() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append(" \"text\" : \"" + getText() + "\", ");
		buffer.append(" \"phonetic\" : \"" + getPhonetic() + "\", ");
		buffer.append(" \"type\" : " + getType() + " ");
		buffer.append("}");
		return buffer.toString();
	}

	public boolean equals(Object a) {
		if (a instanceof Answer) {
			return text.equals(((Answer) a).getText());
		} else
			return false;
	}

	public String toString() {
		return "Answer[" + text + "]";
	}

}
