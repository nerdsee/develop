package org.stoevesand.brain.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Header {

	static final int ERROR_OK=0;
	static final int ERROR_UNKNOWN=100;
	static final int ERROR_ITEM_UNKNOWN=110;
	public static final int ERROR_USER_UNKNOWN=111;
	public static final int ERROR_NOITEMAVAILABLE = 200;
	
	@XmlElement
	int error_code;

	@XmlElement
	String error_msg;

	@XmlElement
	String error_text;
	
	@XmlElement
	int openCurrent=0;
	
	@XmlElement
	int openTotal=0;

	public Header() {
	}
	
	public Header(int error_code, String error_msg, String error_text) {
		this.error_code=error_code;
		this.error_msg=error_msg;
		this.error_text=error_text;
	}
}
