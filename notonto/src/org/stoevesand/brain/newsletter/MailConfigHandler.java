package org.stoevesand.brain.newsletter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MailConfigHandler extends DefaultHandler {

	long id = 0;
	StringBuffer textBuffer = new StringBuffer();

	String text = null;

	MailConfig bc = null;

	public MailConfigHandler() {
		this.bc = MailConfig.getInstance();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
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
		textBuffer = new StringBuffer();
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		textBuffer.append(new String(ch, start, length));
	}
}
