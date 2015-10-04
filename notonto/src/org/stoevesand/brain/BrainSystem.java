package org.stoevesand.brain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.enterprise.inject.spi.Bean;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;

import org.jboss.logging.Logger;
import org.primefaces.context.RequestContext;
import org.stoevesand.brain.auth.Authorization;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Answer;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.model.Item;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.persistence.Administration;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.brain.persistence.BrainDBFactory;
import org.stoevesand.tools.DictMake;
import org.stoevesand.util.News;
import org.stoevesand.util.SendMailUsingAuthentication;
import org.stoevesand.util.StringUtils;

@ManagedBean
@ApplicationScoped
public class BrainSystem {

	private static Logger log = Logger.getLogger(BrainSystem.class);

	private static boolean _DEBUG_ = false;

	private static BrainSystem _instance = null;

	private BrainDB brainDB = null;

	private long breakTimes[] = new long[10];

	private String brainHomeDir;

	private int maxItemsLevel0;

	// private String answerText;

	private String invertLessonID;

	Vector<Category> categories = null;

	private String registerText;

	public static BrainSystem getBrainSystem() {
		if (_instance == null) {
			FacesContext theFacesContext = FacesContext.getCurrentInstance();
			_instance = (BrainSystem)theFacesContext.getApplication().evaluateExpressionGet(theFacesContext, "#{brainSystem}", BrainSystem.class);
		}
		return _instance;
	}

	public static BrainSystem getBrainSystemNoFaces() {
		if (_instance == null) {
			_instance = new BrainSystem();
		}
		return _instance;
	}

	private void init() {

		brainHomeDir = System.getProperty("fivetoknow.dir");

		debug("INIT.");
		File configFile = new File(brainHomeDir + "/brain_config.xml");
		Administration.loadConfig(configFile, this);

		loadTemplates();

	}

	private void loadTemplates() {

		brainHomeDir = System.getProperty("fivetoknow.dir");

		debug("INIT. ");
		File registerFile = new File(brainHomeDir + "/register.txt");

		registerText = StringUtils.loadFileToString(registerFile);

	}

	public String invertLesson() {

		long id = Long.parseLong(invertLessonID);

		Lesson lesson;
		try {
			lesson = getBrainDB().getLesson(id);

			DictMake.invertLesson(lesson);

		} catch (DBException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String loadLessons() {
		File dbfile = new File(brainHomeDir + "/lessons/radikal214-ch-de.txt.dict.xml");
		Administration.loadDB(dbfile);
		return "ok";
	}

	public String doNothing() {
		return null;
	}

	public static void debug(String msg) {
		if (_DEBUG_)
			System.out.println(msg);
	}

	public BrainSystem() {
		brainDB = BrainDBFactory.getInstance().getBrainDB();
		init();
	}

	public BrainDB getBrainDB() {
		return brainDB;
	}

	public String getNow() {
		return "" + System.currentTimeMillis();
	}

	public Vector<User> getUsers() {
		Vector<User> ret = null;
		debug("bs: getUsers()");
		try {
			ret = brainDB.getUsers();
		} catch (DBException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public Vector<Lesson> getLessons() {
		Vector<Lesson> ret = null;
		debug("bs: getLessons()");
		try {
			ret = brainDB.getLessons();
		} catch (DBException e) {
			e.printStackTrace();
		}
		return ret;
	}

	ListDataModel<User> userModel = null;

	public ListDataModel<User> getUserList() {

		// / TODO: wieder reinmachen
		// if (userModel == null) {
		Vector<User> users = new Vector<User>();

		BrainDB brainDB = getBrainDB();

		try {
			users = brainDB.getUsers();
			userModel = new ListDataModel<User>(users);
		} catch (DBException e) {
			e.printStackTrace();
		}
		// }
		return userModel;
	}

	Vector<UserScore> top5 = null;
	long lastTop5 = 0;
	// long top5Reload = 1000 * 60 * 60; // Stunde
	long top5Reload = 1000 * 60 * 10; // 10 min

	public Vector<UserScore> getTop5() {

		long now = System.currentTimeMillis();
		if (now - top5Reload > lastTop5) {
			lastTop5 = now;
			BrainDB brainDB = getBrainDB();
			top5 = brainDB.getTop5();
		}
		return top5;
	}

	public Date getLastTop5() {
		return new Date(lastTop5);
	}

	public void updateTop5(User cu) {
		for (UserScore us : getTop5()) {
			if (us.getName().equals(cu.getName())) {
				us.nick = cu.getNick();
				log.debug("nick adjusted.");
			}
		}
		// log.debug("cu: " + cu.getName());
		// for(UserScore us : getTop5()) {
		// log.debug("us: " + us.getName());
		// }

	}

	public Vector<Category> getCategories() {
		loadCategories();
		return categories;
	}

	private void loadCategories() {
		if (categories == null) {
			try {
				categories = brainDB.getCategories();
			} catch (DBException e) {
				e.printStackTrace();
			}
		}
	}

	public void unloadCategories() {
		categories = null;
	}

	public String loadUsers() {
		File dbfile = new File(brainHomeDir + "/users.xml");
		Administration.loadDB(dbfile);
		return "ok";
	}

	public void setBreakTime(int ibreakLevel, String breakTime) {

		if ((ibreakLevel >= 0) && (ibreakLevel < 10)) {
			breakTimes[ibreakLevel] = breakTimeInMillies(breakTime);
			debug("breakTimes: " + breakTimes[ibreakLevel]);
		}
	}

	/**
	 * Wandelt die Unterbrechungszeit in Textform in Millisekunden um. Erlaubt ist
	 * eine Zahl mit einem Kenner am Ende: M=Minutes, H=Hours, D=Days
	 * 
	 * @param breakTime
	 *          Unterbrechungszeit in Textform
	 * @return Zeit in Millisekunden
	 */
	public static long breakTimeInMillies(String breakTime) {
		long ret = 0;
		if (breakTime == null)
			return 0;
		if (breakTime.length() == 0)
			return 0;

		if (breakTime.length() == 1) {
			return 0;
		} else {
			String mark = breakTime.substring(breakTime.length() - 1);
			String time = breakTime.substring(0, breakTime.length() - 1);
			long ltime = Long.parseLong(time);

			if (mark.equals("M"))
				ret = 60 * 1000;
			else if (mark.equals("H"))
				ret = 60 * 60 * 1000;
			else if (mark.equals("D"))
				ret = 24 * 60 * 60 * 1000;
			else
				ret = 1000;

			ret = ret * ltime;
		}

		return ret;
	}

	/**
	 * Liefert die Unterbrechungszeit in Millisekunden für den angegebenen Level
	 * 
	 * @param level
	 * @return
	 */
	public long getBreakTime(int level) {
		return breakTimes[level];
	}

	public void setMaxItemsLevel0(int i) {
		maxItemsLevel0 = i;
	}

	public int getMaxItemsLevel0() {
		return maxItemsLevel0;
	}

	public String getCtx() {
		FacesContext context = FacesContext.getCurrentInstance();
		Object o = context.getViewRoot();
		return "[" + o + "]";
	}

	public String getRegisterText() {
		return registerText;
	}

	public String getReminderText() {
		return "Your password is @PASSWORD@ .";
	}

	public static Object accessBeanFromFacesContext(final String theBeanName) {

		FacesContext theFacesContext = FacesContext.getCurrentInstance();
		Object bean = theFacesContext.getApplication().evaluateExpressionGet(theFacesContext, "#{" + theBeanName + "}", Object.class);

		// final Object returnObject =
		// theFacesContext.getELContext().getELResolver().getValue(theFacesContext.getELContext(),
		// null, theBeanName);
		if (bean == null) {
			log.error("Bean with name " + theBeanName + " was not found. Check the faces-config.xml file if the given bean name is ok.");
		}
		return bean;
	}

	public String getContext() {
		return this.toString();
	}

	public String getInvertLessonID() {
		return invertLessonID;
	}

	public void setInvertLessonID(String invertLessonID) {
		this.invertLessonID = invertLessonID;
	}

	public String loadLists() {
		return null;
	}

	public static String findLongestCommonString(Item item, String eingabe) {
		String ret = "";
		try {
			Vector<Answer> answers = item.getAnswers();

			Iterator<Answer> it = answers.iterator();
			double q = -1;

			StringUtils su = new StringUtils();

			while (it.hasNext()) {
				StringBuffer buf = new StringBuffer();
				Answer answer = it.next();
				double lq = su.longestCommonStrings(eingabe, answer.getText(), buf);
				log.debug("analyse: " + answer.getText() + "(" + lq + ")");
				if (lq > q) {
					ret = buf.toString();
					q = lq;
				}
			}

		} catch (Throwable t) {
			ret = eingabe;
			log.debug("EXC: longestCommonString");
			t.printStackTrace();
		}
		return ret;
	}

}
