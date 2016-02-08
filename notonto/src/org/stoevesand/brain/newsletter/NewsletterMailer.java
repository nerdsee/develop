package org.stoevesand.brain.newsletter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.util.StringUtils;
import org.xml.sax.SAXException;

public class NewsletterMailer {

	// private static org.apache.log4j.Logger log =
	// LogManager.getLogger("Authorization.class");

	private static Logger log = LogManager.getLogger(NewsletterMailer.class);
	private final String pad = "                                                                                                            ";

	NewsletterDB db = null;

	public static void main(String[] args) {

		NewsletterMailer nm = new NewsletterMailer();

		log.info("Newsletter Mailer start: " + new Date());

		nm.run();
	}

	private String notontoServer;

	private ResourceBundle resourceBundle;

	private void run() {
		db = new NewsletterDB();
		loadConfig();
		loadResourceBundle(Locale.GERMAN);
		send();
	}

	public void send() {

		try {
			Vector<SimplifiedUser> users = db.getNewsletterUsers();

			if (users != null) {
				// String emailMsgTxt = bs.getReminderText();
				// emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@PASSWORD@",
				// pw);

				log.info("Number of users: " + users.size());

				String brainHomeDir = System.getProperty("fivetoknow.dir");

				notontoServer = System.getProperty("notonto.server");
				if ((notontoServer == null) || (notontoServer.length() == 0)) {
					notontoServer = "www.notonto.de";
				}

				for (SimplifiedUser user : users) {
					log.debug("user: " + user.getName());

					String file_pre = "newsletter_" + user.getStatusLang();
					File fileNewsletterHTML = new File(brainHomeDir + "/" + file_pre + ".html");
					File fileNewsletterTXT = new File(brainHomeDir + "/" + file_pre + ".txt");
					if ((fileNewsletterHTML != null) && (fileNewsletterTXT != null)) {
						Locale loc = getLocale(user.getStatusLang());
						loadResourceBundle(loc);

						String emailSubjectTxt = resourceBundle.getString("stat_subject");
						String newsletterHTML = StringUtils.loadFileToString(fileNewsletterHTML);
						String newsletterTXT = StringUtils.loadFileToString(fileNewsletterTXT);

						String freq = getFrequency(user);

						SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
						String s_now = df.format(new Date());

						String display_user="";
						if  (user.getNick() == null) {
							display_user=user.getName();
						} else {
							display_user=user.getNick()+" (" + user.getName() +")";
						}
						String htmlText = StringUtils.replaceSubstring(newsletterHTML, "@NICKUSER@", display_user);
						htmlText = StringUtils.replaceSubstring(htmlText, "@LESSONS@", generateLessonTableHTML(user));
						htmlText = StringUtils.replaceSubstring(htmlText, "@SERVER@", notontoServer);
						htmlText = StringUtils.replaceSubstring(htmlText, "@FREQ@", freq);
						htmlText = StringUtils.replaceSubstring(htmlText, "@EMAIL@", user.getName());
						htmlText = StringUtils.replaceSubstring(htmlText, "@DATE@", s_now);

						String txtText = StringUtils.replaceSubstring(newsletterTXT, "@NICKUSER@", display_user);
						txtText = StringUtils.replaceSubstring(txtText, "@LESSONS@", generateLessonTableTXT(user));
						txtText = StringUtils.replaceSubstring(txtText, "@SERVER@", notontoServer);
						txtText = StringUtils.replaceSubstring(txtText, "@FREQ@", freq);
						txtText = StringUtils.replaceSubstring(txtText, "@EMAIL@", user.getName());
						txtText = StringUtils.replaceSubstring(txtText, "@DATE@", s_now);

						db.updateUser(user);

						// System.out.println(txtText);
						sendNewsletterMail(user.getName(), emailSubjectTxt, txtText, htmlText);
					} else {
						log.error("user: " + user.getName());
						log.error("File not found (prefix): " + file_pre);
					}
				}

			}

		} catch (Exception e) {
			log.error("Statusmail not sent: ", e);
		}

		return;
	}

	private Locale getLocale(String statusLang) {
		Locale ret=null;
		if ("en".equals(statusLang))
			ret=new Locale("en","EN");
		else
			ret=new Locale("de","DE");
		return ret;
	}

	private String getFrequency(SimplifiedUser user) {
		String ret = "";
		switch (user.getStatusMailFreq()) {
		case 1:
			ret = resourceBundle.getString("stat_daily");
			break;
		case 7:
			ret = resourceBundle.getString("stat_weekly");
			break;
		case 30:
			ret = resourceBundle.getString("stat_monthly");
			break;
		}
		return ret;
	}

	private String generateLessonTableTXT(SimplifiedUser user) throws DBException {
		StringBuffer ret = new StringBuffer();

		ret.append("                                                                        Level\n");
		ret.append("Lektion                                  lernbereit    neu    1     2     3     4    5+   Score\n");
		ret.append("---------------------------------------- ----------   ----- ----- ----- ----- ----- ----- -----\n");

		for (SimplifiedUserLesson sul : db.getLessons(user)) {
			ret.append(fs(sul.getDescription(), 40));
			ret.append(" ");
			ret.append(fs("" + sul.getAvailable(), 10));
			ret.append("   ");
			ret.append(fs("" + sul.getLevel(0), 5, false));
			ret.append(" ");
			ret.append(fs("" + sul.getLevel(1), 5, false));
			ret.append(" ");
			ret.append(fs("" + sul.getLevel(2), 5, false));
			ret.append(" ");
			ret.append(fs("" + sul.getLevel(3), 5, false));
			ret.append(" ");
			ret.append(fs("" + sul.getLevel(4), 5, false));
			ret.append(" ");
			ret.append(fs("" + sul.getLevel(5), 5, false));
			ret.append(" ");
			ret.append(fs("" + sul.getScore(), 5, false));
			ret.append("\n");
			// ret.append("http://" + notontoServer + "/lesson.jsf?ulid=" +
			// sul.getId() + "\n");
		}

		return ret.toString();
	}

	private String fs(String text, int maxlen) {
		return fs(text, maxlen, true);
	}

	private String fs(String text, int maxlen, boolean leftalign) {
		String ret = "";
		int textlen = text.length();

		if (textlen == maxlen)
			return text;

		if (textlen > maxlen) {
			// text ist zu lang
			ret = text.substring(0, maxlen - 3) + "...";
		} else {
			if (leftalign)
				ret = text + pad.substring(0, maxlen - textlen);
			else
				ret = pad.substring(0, maxlen - textlen) + text;
		}

		return ret;
	}

	private String generateLessonTableHTML(SimplifiedUser user) throws DBException {
		StringBuffer ret = new StringBuffer();

		ret.append("<table class=\"lesson\">");
		ret.append("<tr>");
		ret.append("<th rowspan=\"2\">Lektion</th>");
		ret.append("<th rowspan=\"2\">lernbereit</th>");
		ret.append("<th width=\"30px\" colspan=\"6\">Level</th>");
		ret.append("</tr>");
		ret.append("<tr>");
		ret.append("<th width=\"30px\">neu</th>");
		ret.append("<th width=\"30px\">1</th>");
		ret.append("<th width=\"30px\">2</th>");
		ret.append("<th width=\"30px\">3</th>");
		ret.append("<th width=\"30px\">4</th>");
		ret.append("<th width=\"30px\">5+</th>");
		ret.append("<th width=\"30px\">Score</th>");
		ret.append("</tr>");

		for (SimplifiedUserLesson sul : db.getLessons(user)) {
			ret.append("<tr>");
			ret.append("<td><a href=\"http://" + notontoServer + "/lesson.jsf?ulid=" + sul.getId() + "\">" + sul.getDescription() + "</a></td>");
			ret.append("<td class=\"" + (sul.getAvailable() > 0 ? "bggreen" : "") + "\">" + sul.getAvailable() + "</td>");
			ret.append("<td>" + sul.getLevel(0) + "</td>");
			ret.append("<td>" + sul.getLevel(1) + "</td>");
			ret.append("<td>" + sul.getLevel(2) + "</td>");
			ret.append("<td>" + sul.getLevel(3) + "</td>");
			ret.append("<td>" + sul.getLevel(4) + "</td>");
			ret.append("<td>" + sul.getLevel(5) + "</td>");
			ret.append("<td>" + sul.getScore() + "</td>");
			ret.append("</tr>");
		}

		ret.append("</table>");

		return ret.toString();
	}

	public void sendNewsletterMail(String emailAddress, String emailSubjectTxt, String plainText, String htmlText) {
		try {
			SendMail smtpMailSender = new SendMail();
			smtpMailSender.postMail(emailAddress, emailSubjectTxt, plainText, htmlText, "info@notonto.de");
			log.debug("Sucessfully sent newsletter to user");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void loadConfig() {
		String brainHomeDir = System.getProperty("fivetoknow.dir");
		File configFile = new File(brainHomeDir + "/brain_config.xml");

		log.debug("Load config.");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			MailConfigHandler brainConfigHandler = new MailConfigHandler();
			SAXParser parser = factory.newSAXParser();
			parser.parse(configFile, brainConfigHandler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadResourceBundle(Locale currentLocale) {
		resourceBundle = ResourceBundle.getBundle("org.stoevesand.brain.i18n.MessagesBundle", currentLocale);
	}

}
