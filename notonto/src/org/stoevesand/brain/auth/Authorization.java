package org.stoevesand.brain.auth;

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSession;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.SendMailUsingAuthentication;
import org.stoevesand.util.StringUtils;

@ManagedBean(name = "auth")
@SessionScoped
public class Authorization {

	@ManagedProperty(value = "#{brainSystem}")
	private BrainSystem brainSystem;

	@ManagedProperty(value = "#{brainSession}")
	private BrainSession brainSession;

	@ManagedProperty(value = "#{user}")
	private User currentUser;

	public void setCurrentUser(User u) {
		this.currentUser = u;
	}

	public void setBrainSystem(BrainSystem bs) {
		this.brainSystem = bs;
	}

	public BrainSystem getBrainSystem() {
		return this.brainSystem;
	}

	public void setBrainSession(BrainSession bss) {
		this.brainSession = bss;
	}

	public BrainSession getBrainSession() {
		return this.brainSession;
	}

	private static final int PWC_NONE = 0;
	private static final int PWC_OK = 1;
	private static final int PWC_DBERROR = 2;
	private static final int PWC_NOMATCH = 3;
	private static final int PWC_EMPTY = 4;
	private static final int PWC_USED = 5;
	private static final int PWC_UNCHANGED = 6;

	// private static org.apache.log4j.Logger log =
	// LogManager.getLogger("Authorization.class");
	private static Logger log = LogManager.getLogger(Authorization.class);

	String username = "";
	String password = "";
	String passnew = "";
	String emailAddress = "";
	String unlock = "";
	String passconfirm = "";
	String passdelete = "";
	String captext = "";
	int statusCode = 0;

	String nickMsg = "";

	String nicknew = "";
	String prefixnew = "";

	public Authorization() {
		System.out.println("BrainSystem: " + brainSystem);
	}

	public String getPrefixnew() {
		return prefixnew;
	}

	public void setPrefixnew(String prefixnew) {
		this.prefixnew = prefixnew;
	}

	private boolean loggedIn;

	public String getEmail() {
		return emailAddress;
	}

	public String getUnlock() {
		return "";
	}

	public void setUnlock(String unlock) {
		this.unlock = unlock;
	}

	public void setEmail(String email) {
		this.emailAddress = email;
	}

	public String getPassconfirm() {
		return passconfirm;
	}

	public void setPassconfirm(String passconfirm) {
		this.passconfirm = passconfirm;
	}

	public String getPassdelete() {
		return passdelete;
	}

	public void setPassdelete(String passdelete) {
		this.passdelete = passdelete;
	}

	public String getCaptext() {
		return "";
	}

	public void setCaptext(String captext) {
		this.captext = captext;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String remind() {
		// FacesContext context = FacesContext.getCurrentInstance();
		BrainDB db = brainSystem.getBrainDB();

		String pw = db.getUserPassword(getEmail());

		if (pw != null) {
			String emailSubjectTxt = "Your Request at notonto.";
			String emailMsgTxt = brainSystem.getReminderText();
			emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@PASSWORD@", pw);

			// SendMailUsingAuthentication.sendConfirmationMail(email, unlock);
			SendMailUsingAuthentication.sendConfirmationMail(emailAddress, emailSubjectTxt, emailMsgTxt);
			log.info("Reminder sent: " + getEmail());
		} else
			log.error("Reminder requested: " + getEmail());

		emailAddress = "";

		return "reminded";
	}

	public String login() {

		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = ResourceBundle.getBundle("org.stoevesand.brain.i18n.MessagesBundle", context.getViewRoot().getLocale());

		String ret = "login";

		currentUser.loadUser(username);

		if (currentUser.isValid()) {

			if (currentUser.getPassword().equals(password)) {
				ret = userLoggedIn();
				return ret;
			} else {
				log.info("User wrong PW: " + currentUser.getName());
			}
		} else {
			log.info("user " + username + " invalid.");
		}

		context.addMessage(null, new FacesMessage(bundle.getString("loginfailed")));
		brainSession.incrementLoginCounter();

		return "logout";
	}

	// alles was man erledigen muss, wenn ein user richtig ist.
	public String userLoggedIn() {
		String ret = "user";
		loggedIn = true;

		try {
			currentUser.storeLastLogin();
			currentUser.storeScore();
			brainSession.login();
			log.info("User logged in: " + currentUser.getName());
			log.info("Browser: " + getBrowser());

			// Sonderfall: Invitation Pending
			if (brainSession.isInvitationPending()) {
				brainSession.setInvitationPending(false);
				currentUser.acceptInvitation(brainSession.getInviteeEmail(), brainSession.getInviteeCode());
			}
			
			// Sonderfall direkteinstieg. Wenn eine Userlesson ID übergeben
			// wurde
			// dann wird sie geladen und gleich auf die lesson.jsf verzweigt.
			String sulid = brainSession.getParameter("ulid");
			System.out.println("ULID: " + sulid);
			long ulid = 0;
			if (sulid != null) {
				try {
					ulid = Long.parseLong(sulid);
					UserLesson userLesson = (UserLesson) currentUser.getUserLesson(ulid);
					if (userLesson != null) {
						brainSession.learnLesson(userLesson);
						ret = "lesson";
					} else
						ret = "user";
				} catch (Exception e) {
				}
			}
			// Ende: Direkteinstieg
			System.out.println("RET: " + ret);
			if (currentUser.getIsAdmin()) {
				return "admin_user";
			}
		} catch (DBException e) {
			return "fatal_DB";
		}
		return ret;
	}

	private String getBrowser() {
		String browser = "unknown";
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			HttpServletRequest sr = (HttpServletRequest) context.getExternalContext().getRequest();
			browser = sr.getHeader("User-Agent");
		} catch (Exception e) {
		}
		return browser;
	}

	public String logout() {
		loggedIn = false;
		username = "";

		// try {
		// BrainSystem.getBrainSystem().getBrainSession().getFacebookClient().logout();
		// } catch(Exception e){};

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ectx = context.getExternalContext();
		HttpSession session = (HttpSession) ectx.getSession(false);
		session.invalidate();

		return "index";
	}

	public String register() {
		log.debug("register");

		BrainDB db = brainSystem.getBrainDB();
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			if (!passwordsMatch()) {
				context.addMessage("Password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Check", "Passwörter stimmen nicht überein."));
				return "register";
			}
			if (db.emailIsAlreadyUsed(emailAddress)) {
				context.addMessage("Password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Check", "Mit dieser Emailadresse ist bereits ein Konto eröffnet."));
				return "register";
			}
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		confirm();
		
		return "welcome";
	}

	public String deleteAccount() {

		String testpass = passnew;
		passnew = "";

		String ret = null;
		if (testpass.length() > 0) {
			BrainDB db = brainSystem.getBrainDB();

			try {
				if (currentUser.getPassword().equals(testpass)) {
					db.deleteAccount(currentUser);
					ret = logout();
				} else {
					brainSession.getBrainMessage().setPwErrorText("Wrong password!");
					ret = null;
				}
			} catch (DBException e) {
			}
		} else {
			brainSession.getBrainMessage().setPwErrorText("Please enter your password!");
		}

		return ret;
	}

	public String alterPassword() {

		if (passnew.length() == 0) {
			setStatusCode(PWC_EMPTY);
		} else if (passnew.equals(passconfirm)) {
			BrainDB db = brainSystem.getBrainDB();

			try {
				db.changePassword(currentUser, passnew);
			} catch (DBException e) {
				setStatusCode(PWC_DBERROR);
			}
			passnew = "";
			passconfirm = "";
			setStatusCode(PWC_OK);
		} else {
			setStatusCode(PWC_NOMATCH);
		}

		return null;
	}

	public String alterStatusMailFreq() {
		BrainDB db = brainSystem.getBrainDB();

		try {
			db.changeStatusMailFreq(currentUser);
		} catch (DBException e) {
		}
		return null;
	}

	public String alterNickname() {

		if (nicknew.length() == 0) {
			setNickMsg(brainSession.getResourceBundle().getString("msg_required"));
		} else if ((nicknew.length() > 15) || (nicknew.length() < 5)) {
			setNickMsg(brainSession.getResourceBundle().getString("msg_nicklong"));
		} else if (!nicknew.matches("[a-zA-Z0-9]*")) {
			setNickMsg(brainSession.getResourceBundle().getString("msg_nickregex"));
		} else {
			BrainDB db = brainSystem.getBrainDB();

			if (nicknew.equals(currentUser.getNick())) {
				setNickMsg("PWC_UNCHANGED");
			} else {
				try {
					if (db.checkNickname(currentUser, nicknew)) {
						currentUser.setNick(nicknew);
						db.changeNickname(currentUser, nicknew);
						brainSystem.updateTop5(currentUser);
						setStatusCode(PWC_OK);
					} else {
						setNickMsg(brainSession.getResourceBundle().getString("msg_nickused"));
					}
				} catch (DBException e) {
					setStatusCode(PWC_DBERROR);
				}
			}
		}
		nicknew = "";
		return null;
	}

	public String alterPrefix() {

		if (prefixnew.length() == 0) {
			brainSession.getBrainMessage().setPrefixErrorText(brainSession.getResourceBundle().getString("msg_required"));
		} else if ((prefixnew.length() > 5) || (prefixnew.length() < 2)) {
			brainSession.getBrainMessage().setPrefixErrorText(brainSession.getResourceBundle().getString("msg_prefixlong"));
		} else if (!prefixnew.matches("[a-zA-Z0-9]*")) {
			brainSession.getBrainMessage().setPrefixErrorText(brainSession.getResourceBundle().getString("msg_prefixregex"));
		} else {
			BrainDB db = brainSystem.getBrainDB();

			if (!prefixnew.equals(currentUser.getPrefix())) {
				try {
					if (db.checkUserPrefix(currentUser, prefixnew)) {
						currentUser.setPrefix(prefixnew);
						db.changePrefix(currentUser, prefixnew);
					} else {
						brainSession.getBrainMessage().setPrefixErrorText(brainSession.getResourceBundle().getString("msg_prefixused"));
					}
				} catch (DBException e) {
					setStatusCode(PWC_DBERROR);
				}
			}
		}
		prefixnew = "";
		return null;
	}

	// public String prepareOptions() {
	// passnew = "";
	// passconfirm = "";
	// setStatusCode(0);
	// return "options";
	// }

	public String confirm() {
		try {
			FacesContext context = FacesContext.getCurrentInstance();

			log.debug("confirm");

			unlock = randomUnlockString();

			String emailSubjectTxt = "Your Registration at notonto.";
			String emailMsgTxt = brainSystem.getRegisterText();
			emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@CODE@", unlock);

			String rcp = context.getExternalContext().getRequestContextPath();

			String unlockLink = "http://www.notonto.de" + rcp + "/unlock/" + emailAddress + "/" + unlock;
			emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@LINK@", unlockLink);

			SendMailUsingAuthentication.sendConfirmationMail(emailAddress, emailSubjectTxt, emailMsgTxt);

			currentUser.initUser(emailAddress, password, unlock, false);
			currentUser.store();
			BrainSystem.debug("bs: registeredUser -> " + currentUser.getName());

			// alles erledigen, damit der User als eingeloggt erscheint
			userLoggedIn();

		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "unlock";
	}

	public String sendCode() {
		FacesContext context = FacesContext.getCurrentInstance();
		User user = (User) context.getExternalContext().getRequestMap().get("user");

		System.out.println("sendCode -> " + user.getName());
		System.out.println("sendCode -> " + user.getUnlock());

		reconfirm(user);

		return null;
	}

	public void reconfirm(User user) {

		FacesContext context = FacesContext.getCurrentInstance();

		log.debug("reconfirm");

		String emailSubjectTxt = "Your Registration at notonto.";
		String emailMsgTxt = "### Aufgrund eines Fehler im Mailsystem wurde Ihr Freischaltcode nicht verschickt. \n";
		emailMsgTxt += "### Wir schicken Ihnen den Code daher erneut zu.\n";
		emailMsgTxt += "### Wir bitten die Verzögerung zu entschuldigen. - Ihr notonto-Team.\n\n";
		emailMsgTxt += brainSystem.getRegisterText();

		emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@CODE@", unlock);

		String rcp = context.getExternalContext().getRequestContextPath();

		String unlockLink = "http://www.notonto.de" + rcp + "/unlock/" + user.getName() + "/" + user.getUnlock();
		emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@LINK@", unlockLink);

		// SendMailUsingAuthentication.sendConfirmationMail(email, unlock);
		SendMailUsingAuthentication.sendConfirmationMail(user.getName(), emailSubjectTxt, emailMsgTxt);

		BrainSystem.debug("reconfirmation -> " + user.getName());

	}

	public String unlock() {
		log.debug("Vcode");

		// String message = "Dieser Code ist leider nicht richtig!";

		BrainDB db = brainSystem.getBrainDB();
		boolean unlocked;
		try {
			unlocked = db.unlockUser(emailAddress, unlock);

			if (!unlocked) {
				// ((UIInput) toValidate).setValid(false);
				// context.addMessage(toValidate.getClientId(context), new
				// FacesMessage(message));
				log.debug("wrong unlock key");
				return "unlock";
			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "unlocked";
	}

	String randomUnlockString() {
		String elegibleChars = "ABCDEFGHJKLMPQRSTUVWXYabcdefhjkmnpqrstuvwxy23456789";
		char[] chars = elegibleChars.toCharArray();
		int charsToPrint = 5;

		StringBuffer finalString = new StringBuffer();

		for (int i = 0; i < charsToPrint; i++) {
			double randomValue = Math.random();
			int randomIndex = (int) Math.round(randomValue * (chars.length - 1));
			char characterToShow = chars[randomIndex];
			finalString.append(characterToShow);
		}
		return finalString.toString();
	}

	public void validateEmail(FacesContext context, UIComponent toValidate, Object value) {
		log.debug("VE");
		String message = "Wrong email";
		String email = (String) value;
		if (email.indexOf('@') < 1) {
			((UIInput) toValidate).setValid(false);
			context.addMessage(toValidate.getClientId(context), new FacesMessage(message));
		}
	}

	public boolean passwordsMatch() {

		log.debug("VP: compare " + password + " - " + passconfirm);

		return password.equals(passconfirm);
	}

	public void validateRights(FacesContext context, UIComponent toValidate, Object value) {
		log.debug("VR");

		String message = "Bitte stimmen Sie den Benutzungshinweisen zu!";
		Boolean rights = (Boolean) value;
		if (!rights.booleanValue()) {
			((UIInput) toValidate).setValid(false);
			context.addMessage(toValidate.getClientId(context), new FacesMessage(message));
		}
	}

	public void validateCaptcha(FacesContext context, UIComponent toValidate, Object value) {
		log.debug("VC");

		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		String lastcap = "" + request.getSession().getAttribute("captcha");

		String message = "Wrong captcha";
		String captext = (String) value;
		if (!lastcap.trim().toUpperCase().equals(captext.trim().toUpperCase())) {
			((UIInput) toValidate).setValid(false);
			context.addMessage(toValidate.getClientId(context), new FacesMessage(message));
		}

	}

	public boolean getIsLoggedIn() {
		return loggedIn;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		FacesContext facesContext = FacesContext.getCurrentInstance();

		switch (statusCode) {
			case PWC_EMPTY:
				facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_ERROR, "PWC_EMPTY", "PWC_EMPTY"));
				break;
			case PWC_NOMATCH:
				facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_ERROR, "PWC_NOMATCH", "PWC_NOMATCH"));
				break;
			case PWC_OK:
				facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_INFO, "PWC_OK", "PWC_OK"));
				break;
			case PWC_USED:
				facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_ERROR, "PWC_USED", "PWC_USED"));
				break;
			case PWC_NONE:
				facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_ERROR, "PWC_NONE", "PWC_NONE"));
				break;
		}

		this.statusCode = statusCode;
	}

	public String getPassnew() {
		return passnew;
	}

	public void setPassnew(String passnew) {
		this.passnew = passnew;
	}

	public String getNicknew() {
		return "";
	}

	public void setNicknew(String nicknew) {
		this.nicknew = nicknew.trim();
	}

	public String getNickMsg() {
		return nickMsg;
	}

	public void setNickMsg(String nickMsg) {
		this.nickMsg = nickMsg;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage("Date", new FacesMessage(FacesMessage.SEVERITY_ERROR, nickMsg, nickMsg));

	}
}
