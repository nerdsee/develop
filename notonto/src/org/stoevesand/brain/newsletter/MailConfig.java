package org.stoevesand.brain.newsletter;

public class MailConfig {

	private static MailConfig _instance = null;

	public static MailConfig getInstance() {
		if (_instance == null) {
			_instance = new MailConfig();
		}
		return _instance;
	}

	/**
	 * Email config data
	 */
	private String SMTP_HOST_NAME = "";
	private String SMTP_AUTH_USER = "";
	private String SMTP_AUTH_PWD = "";
	private String SMTP_EMAIL_FROM = "";

	public String getSMTP_HOST_NAME() {
		return SMTP_HOST_NAME;
	}

	public void setSMTP_HOST_NAME(String smtp_host_name) {
		SMTP_HOST_NAME = smtp_host_name;
	}

	public String getSMTP_AUTH_USER() {
		return SMTP_AUTH_USER;
	}

	public void setSMTP_AUTH_USER(String smtp_auth_user) {
		SMTP_AUTH_USER = smtp_auth_user;
	}

	public String getSMTP_AUTH_PWD() {
		return SMTP_AUTH_PWD;
	}

	public void setSMTP_AUTH_PWD(String smtp_auth_pwd) {
		SMTP_AUTH_PWD = smtp_auth_pwd;
	}

	public String getEmailFromAddress() {
		return SMTP_EMAIL_FROM;
	}

	public void setEmailFromAddress(String emailFromAddress) {
		this.SMTP_EMAIL_FROM = emailFromAddress;
	}

	public String getSMTP_EMAIL_FROM() {
		return SMTP_EMAIL_FROM;
	}

	public void setSMTP_EMAIL_FROM(String smtp_email_from) {
		SMTP_EMAIL_FROM = smtp_email_from;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(255);
		buf.append("[");
		buf.append(SMTP_HOST_NAME);
		buf.append("][");
		buf.append(SMTP_AUTH_USER);
		buf.append("][");
		buf.append(SMTP_AUTH_PWD);
		buf.append("][");
		buf.append(SMTP_EMAIL_FROM);
		buf.append("]");

		return buf.toString();
	}
}
