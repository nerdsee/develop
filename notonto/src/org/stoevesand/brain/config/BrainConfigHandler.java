package org.stoevesand.brain.config;

import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.persistence.BrainDBFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BrainConfigHandler extends DefaultHandler {

	long id = 0;
	StringBuffer textBuffer = new StringBuffer();

	String dbname = null;
	String user = null;
	String pass = null;
	String dbcs = "";
	String text = null;

	BrainSystem brainSystem=null;
	BrainConfig bc = null;

	public BrainConfigHandler(BrainSystem brainSystem) {
		this.brainSystem = brainSystem;
		this.bc = BrainConfig.getInstance();
	}

	public BrainConfigHandler() {
		this.brainSystem = null;
		this.bc = BrainConfig.getInstance();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("break")) {
			String breakLevel = attributes.getValue("level");
			int ibreakLevel = Integer.parseInt(breakLevel);
			String breakTime = attributes.getValue("time");

			if (brainSystem != null)
				brainSystem.setBreakTime(ibreakLevel, breakTime);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("max_items_level0")) {
			String temp = textBuffer.toString().trim();
			if (brainSystem != null)
				brainSystem.setMaxItemsLevel0(Integer.parseInt(temp));
		}
		if (qName.equals("name")) {
			dbname = textBuffer.toString().trim();
		}
		if (qName.equals("user")) {
			user = textBuffer.toString().trim();
		}
		if (qName.equals("pass")) {
			pass = textBuffer.toString().trim();
		}
		if (qName.equals("cs")) {
			dbcs = textBuffer.toString().trim();
		}
		if (qName.equals("SMTP_AUTH_PWD")) {
			text = textBuffer.toString().trim();
			bc.setSMTP_AUTH_PWD(text);
		}
		if (qName.equals("SMTP_AUTH_USER")) {
			text = textBuffer.toString().trim();
			bc.setSMTP_AUTH_USER(text);
		}
		if (qName.equals("SMTP_HOST_NAME")) {
			text = textBuffer.toString().trim();
			bc.setSMTP_HOST_NAME(text);
		}
		if (qName.equals("SMTP_EMAIL_FROM")) {
			text = textBuffer.toString().trim();
			bc.setSMTP_EMAIL_FROM(text);
		}
		if (qName.equals("datasource")) {
			BrainDBFactory.getInstance().setCredentials(dbcs, dbname, user, pass);
		}
		textBuffer = new StringBuffer();
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		textBuffer.append(new String(ch, start, length));
	}
}
