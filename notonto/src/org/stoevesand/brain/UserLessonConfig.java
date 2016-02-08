package org.stoevesand.brain;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.persistence.BrainDB;

@XmlRootElement(name = "userlessonconfig")
public class UserLessonConfig {

	HashMap<String, String> paramMap = new HashMap<String, String>();
	UserLesson parent;

	public UserLessonConfig() {
	}
	
	public UserLessonConfig(UserLesson parent) {
		this.parent = parent;
	}

	boolean getBooleanParameter(String name, boolean def) {
		boolean ret = def;
		String p = getParameter(name);
		if (p != null) {
			ret = p.equals("true");
		}
		return ret;
	}

	void setBooleanParameter(String name, boolean value) {
		setParameter(name, value ? "true" : "false");
	}

	String getParameter(String name) {
		String ret = null;

		if (paramMap.containsKey(name)) {
			ret = paramMap.get(name);
		} else {
			BrainDB brainDB = BrainSystem.getBrainSystem().getBrainDB();

			try {
				ret = brainDB.getConfigParameter(parent, name);
			} catch (DBException e) {
				e.printStackTrace();
			}
			if (ret != null)
				paramMap.put(name, ret);
		}

		return ret;
	}

	void setParameter(String name, String value) {
		BrainDB brainDB = BrainSystem.getBrainSystem().getBrainDB();

		try {
			brainDB.setConfigParameter(parent, name, value);
		} catch (DBException e) {
			e.printStackTrace();
		}
		paramMap.put(name, value);

	}

	@XmlElement
	public boolean getShowPinyin() {
		return getBooleanParameter("SHOW_PINYIN", true);
	}

	public void setShowPinyin(boolean value) {
		setBooleanParameter("SHOW_PINYIN", value);
	}

}
