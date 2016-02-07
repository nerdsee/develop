package org.stoevesand.util;

/*
 Some SMTP servers require a username and password authentication before you
 can use their Server for Sending mail. This is most common with couple
 of ISP's who provide SMTP Address to Send Mail.

 This Program gives any example on how to do SMTP Authentication
 (User and Password verification)

 This is a free source code and is provided as it is without any warranties and
 it can be used in any your code for free.

 Author : Sudhir Ancha
 */

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.stoevesand.brain.config.BrainConfig;

/*
 * To use this program, change values for the following three constants,
 * SMTP_HOST_NAME -- Has your SMTP Host Name SMTP_AUTH_USER -- Has your SMTP
 * Authentication UserName SMTP_AUTH_PWD -- Has your SMTP Authentication
 * Password Next change values for fields emailMsgTxt -- Message Text for the
 * Email emailSubjectTxt -- Subject for email emailFromAddress -- Email Address
 * whose name will appears as "from" address Next change value for "emailList".
 * This String array has List of all Email Addresses to Email Email needs to be
 * sent to. Next to run the program, execute it as follows,
 * SendMailUsingAuthentication authProg = new SendMailUsingAuthentication();
 */

public class SendMailUsingAuthentication {

	// private static final String SMTP_HOST_NAME = "mail.stoevesand.org";
	// private static final String SMTP_AUTH_USER = "jan@stoevesand.org";
	// private static final String SMTP_AUTH_PWD = "labskaus1234";
	// private static final String emailFromAddress = "notonto@stoevesand.org";

	// private static String emailMsgTxt = "Confirmation Code: ";
	// private static final String emailSubjectTxt = "Order Confirmation Subject";

	// Add List of Email address to who email needs to be sent to
	private static final String[] emailList = { "test@stoevesand.org" };

	public static void sendConfirmationMail(String emailAddress, String emailSubjectTxt, String emailMsgTxt) {
		try {

			BrainConfig bc = BrainConfig.getInstance();

			SendMailUsingAuthentication smtpMailSender = new SendMailUsingAuthentication();
			smtpMailSender.postMail(emailAddress, emailSubjectTxt, emailMsgTxt, bc.getEmailFromAddress());
			System.out.println("Sucessfully Sent mail to User");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void postMail(String recipient, String subject, String message, String from) throws MessagingException {
		boolean debug = false;
		BrainConfig bc = BrainConfig.getInstance();

		// Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", bc.getSMTP_HOST_NAME());
		props.put("mail.smtp.auth", "true");

		Authenticator auth = new SMTPAuthenticator();
		Session session = Session.getDefaultInstance(props, auth);

		session.setDebug(debug);

		// create a message
		MimeMessage msg = new MimeMessage(session);

		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);

		// InternetAddress[] addressTo = new InternetAddress[recipients.length];
		// for (int i = 0; i < recipients.length; i++)
		// {
		// addressTo[i] = new InternetAddress(recipients[i]);
		// }
		// msg.setRecipients(Message.RecipientType.TO, addressTo);

		InternetAddress[] addressTo = new InternetAddress[1];
		addressTo[0] = new InternetAddress(recipient);
		msg.setRecipients(Message.RecipientType.TO, addressTo);

		// Setting the Subject and Content Type
		msg.setSubject(subject, "utf-8");
		msg.setText(message, "utf-8");
		// msg.setContent(message, "text/plain");
		Transport.send(msg);
	}

	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP
	 * server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {

		public PasswordAuthentication getPasswordAuthentication() {
			BrainConfig bc = BrainConfig.getInstance();

			String username = bc.getSMTP_AUTH_USER();
			String password = bc.getSMTP_AUTH_PWD();
			return new PasswordAuthentication(username, password);
		}
	}

}
