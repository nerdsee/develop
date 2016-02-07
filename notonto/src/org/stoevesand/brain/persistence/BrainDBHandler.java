package org.stoevesand.brain.persistence;

import java.util.Random;
import java.util.Vector;

import org.jboss.logging.Logger;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Answer;
import org.stoevesand.brain.model.Item;
import org.stoevesand.brain.model.Lesson;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BrainDBHandler extends DefaultHandler {

	private static Logger log = Logger.getLogger(BrainDBHandler.class);

	String text = null;
	String comment = null;
	String title = null;
	String name = null;
	// long id = 0;
	// String itemId = null;
	// String lessonId = null;
	// String answerId = null;
	String userId = null;
	String answerType = "0";
	// String itemLessonId=null;
	StringBuffer textBuffer = new StringBuffer();
	String description = null;
	Lesson lesson = null;
	Item item = null;
	int itemCount = 1;
	String kl = "";

	String answer = null;
	boolean answerShow = true;

	Vector aliases = new Vector();

	BrainDB db = null;
	Random rnd = new Random();

	public BrainDBHandler() {
		BrainDBFactory fac = BrainDBFactory.getInstance();
		db = fac.getBrainDB();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("lesson")) {
			itemCount = 1;
			lesson = new Lesson();
		}
		if (qName.equals("item")) {
			item = new Item();
			String sid = attributes.getValue("extid");
			try {
				long extId = Long.parseLong(sid);
				item.setExtId(extId);
			} catch (Exception e) {
			}
			String sch = attributes.getValue("chapter");
			try {
				int chapter = Integer.parseInt(sch);
				item.setChapter(chapter);
			} catch (Exception e) {
			}
		}
		if (qName.equals("items")) {
		}
		if (qName.equals("answer")) {
			answerShow = (!"false".equals(attributes.getValue("visible")));
			answerType = attributes.getValue("type");
			if (answerType == null)
				answerType = "0";
		}
		if (qName.equals("user")) {
			userId = attributes.getValue("id");
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("text")) {
			text = textBuffer.toString().trim();
			item.setText(text);
		}
		if (qName.equals("comment")) {
			comment = textBuffer.toString().trim();
			item.setComment(comment);
		}
		if (qName.equals("title")) {
			title = textBuffer.toString().trim();
			lesson.setTitle(title);
		}
		if (qName.equals("keyboardLayout")) {
			kl = textBuffer.toString().trim();
			lesson.setKeyboardLayout(kl);
		}
		if (qName.equals("name")) {
			name = textBuffer.toString().trim();
		}
		if (qName.equals("description")) {
			description = textBuffer.toString().trim();
			lesson.setDescription(description);
		}
		if (qName.equals("type")) {
			String stype = textBuffer.toString().trim();
			int type = Integer.valueOf(stype).intValue();
			lesson.setLessonType(type);
		}
		if (qName.equals("answer")) {
			String answer = textBuffer.toString().trim();
			int type = Integer.valueOf(answerType).intValue();
			item.addAnswer(new Answer(answer, answerShow, type));
			answerType = "0";
		}
		if (qName.equals("item")) {

			int index = rnd.nextInt(itemCount++);

			log.debug("Item end:" + item.getId());

			lesson.addItemAt(item, index);
			// Item item = new Item(Long.parseLong(itemId),
			// Long.parseLong(itemLessonId), text);
			// item.setAnswerId(Long.parseLong(answerId));
			// item.setAliases(aliases);
			// db.addItem(item);

			aliases.clear();
		}
		if (qName.equals("user")) {
			User user = new User(Long.parseLong(userId), name);
			try {
				db.addUser(user);
			} catch (DBException e) {
				e.printStackTrace();
			}
			log.debug("user(" + userId + ") - " + name);
		}
		if (qName.equals("lesson")) {
			try {
				lesson.store();
			} catch (DBException e) {
				e.printStackTrace();
			}
		}

		textBuffer = new StringBuffer();
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		textBuffer.append(new String(ch, start, length));
	}
}
