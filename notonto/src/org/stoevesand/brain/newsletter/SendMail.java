package org.stoevesand.brain.newsletter;

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

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


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

public class SendMail {

	// private static final String SMTP_HOST_NAME = "mail.stoevesand.org";
	// private static final String SMTP_AUTH_USER = "jan@stoevesand.org";
	// private static final String SMTP_AUTH_PWD = "labskaus1234";
	// private static final String emailFromAddress = "notonto@stoevesand.org";

	// private static String emailMsgTxt = "Confirmation Code: ";
	// private static final String emailSubjectTxt = "Order Confirmation Subject";

	// Add List of Email address to who email needs to be sent to
	private static final String[] emailList = { "test@stoevesand.org" };

	public static void sendConfirmationMail(String emailAddress, String emailSubjectTxt, String plainText, String htmlText) {
		try {

			MailConfig bc = MailConfig.getInstance();

			SendMail smtpMailSender = new SendMail();
			smtpMailSender.postMail(emailAddress, emailSubjectTxt, plainText, htmlText, bc.getEmailFromAddress());
			System.out.println("Sucessfully Sent mail to User");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void postMail(String recipient, String subject, String plainText, String htmlText, String from) throws MessagingException {
		boolean debug = false;
		MailConfig bc = MailConfig.getInstance();

		//System.out.println("BC: " + bc);
		
		// Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", bc.getSMTP_HOST_NAME());
		props.put("mail.smtp.auth", "true");

		Authenticator auth = new SMTPAuthenticator();
		Session session = Session.getDefaultInstance(props, auth);

		session.setDebug(debug);

		MimeMessage msg = new MimeMessage(session);
		Multipart multipart = new MimeMultipart("alternative");

		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);

		InternetAddress[] addressTo = new InternetAddress[1];
		addressTo[0] = new InternetAddress(recipient);
		msg.setRecipients(Message.RecipientType.TO, addressTo);
		msg.setSubject(subject, "utf-8");
		msg.setSentDate(new Date());

		MimeBodyPart msgPartPlain = new MimeBodyPart();
		msgPartPlain.setText(plainText, "utf-8");
		// msg.setContent(message, "text/plain");
		multipart.addBodyPart(msgPartPlain);
		
		MimeBodyPart msgPartHTML = new MimeBodyPart();
		//msgPartHTML.setContent(htmlText, "text/html; charset=iso-8859-15");
		msgPartHTML.setContent(htmlText, "text/html; charset=utf-8");
		multipart.addBodyPart(msgPartHTML);

		msg.setContent(multipart);
		
		Transport.send(msg);
	}

	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP
	 * server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {

		public PasswordAuthentication getPasswordAuthentication() {
			MailConfig bc = MailConfig.getInstance();

			String username = bc.getSMTP_AUTH_USER();
			String password = bc.getSMTP_AUTH_PWD();
			return new PasswordAuthentication(username, password);
		}
	}

}
