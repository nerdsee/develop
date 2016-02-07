package org.stoevesand.brain;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Item;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.persistence.Administration;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.News;
import org.stoevesand.util.SendMailUsingAuthentication;

@ManagedBean
@SessionScoped
public class BrainSession {

	@ManagedProperty(value = "#{brainSystem}")
	private BrainSystem brainSystem;

	public void setBrainSystem(BrainSystem bs) {
		this.brainSystem = bs;
	}

	@ManagedProperty(value = "#{user}")
	User currentUser = null;

	public void setCurrentUser(User u) {
		this.currentUser = u;
	}

	UserLesson currentUserLesson = null;

	private static final String CAT_SELECTION = "mode_sel_cat";
	private static final int CAT_SIZES = 3;

	private static Logger log = Logger.getLogger(BrainSession.class);

	private boolean loggedIn = false;

	public boolean isLoggedIn() {
		return loggedIn;
	}

	private ListDataModel<Item> itemList = null;
	private UserItem lastUserItem = null;

	private String answerText = "";

	private String message = "";

	private String comment = "";
	private String commentType = "";

	private String filterText = "";
	private String filterCat = "";

	private int loginAttempts = 0;
	private int encoding = 0;
	private int fileFormat = 1; // default: Excel

	private final Locale LOCALE_DE = new Locale("de", "DE");
	private final Locale LOCALE_ES = new Locale("es", "ES");
	private final Locale LOCALE_EN = new Locale("en", "EN");

	// Kategorie der Sprache für die WordPress Anbindung
	private int wpCategory = 3;

	public int getWpCategory() {
		return wpCategory;
	}

	private ResourceBundle resourceBundle = null;

	public Locale currentLocale = LOCALE_DE;

	Vector<Category> localCategories = null;

	// String libLanguage = null;

	HashMap<String, String> parameters = new HashMap<String, String>(5);

	UserItem currentUserItem = null;
	Lesson currentLesson = null;
	Item currentItem = null;
	News currentNews = null;

	// LessonLoader lessonLoader = new LessonLoader();
	/*
	 * public LessonLoader getLessonLoader() { return lessonLoader; }
	 */
	public BrainSession() {
	}

	@PostConstruct
	public void init() {
		FacesContext context = FacesContext.getCurrentInstance();
		ServletRequest request = (ServletRequest) context.getExternalContext().getRequest();
		String servername = request.getServerName();

		if ("www.notonto.com".equals(servername)) {
			currentLocale = LOCALE_EN;
			wpCategory = 4; // wordpres category "deutsch"
		} else if ("localhost".equals(servername)) {
			currentLocale = LOCALE_EN;
			wpCategory = 4; // wordpres category "deutsch"
		} else {
			currentLocale = LOCALE_DE;
			wpCategory = 3; // wordpres category "deutsch"
		}
		loadResourceBundle();
		loadTopics();
		// libLanguage = null; // currentLocale.getLanguage();
	}

	private void loadTopics() {
		// TODO Auto-generated method stub
		// moved to LibraryView
	}

	public String localeDE() {
		currentLocale = LOCALE_DE;
		localCategories = null;
		loadTopics();
		loadResourceBundle();
		wpCategory = 3; // wordpres category "deutsch"
		return "index";
	}

	public String localeES() {
		currentLocale = LOCALE_ES;
		localCategories = null;
		loadTopics();
		loadResourceBundle();
		wpCategory = 3; // wordpres category "deutsch"
		return "index";
	}

	public String localeEN() {
		currentLocale = LOCALE_EN;
		localCategories = null;
		loadTopics();
		loadResourceBundle();
		wpCategory = 4; // wordpres category "english"
		return "index";
	}

	public BrainMessage getBrainMessage() {

		FacesContext theFacesContext = FacesContext.getCurrentInstance();
		BrainMessage brainMessage = (BrainMessage) theFacesContext.getApplication().evaluateExpressionGet(theFacesContext, "#{brainMessage}", BrainMessage.class);

		return brainMessage;
	}

	public static BrainSession getBrainSession() {
		FacesContext theFacesContext = FacesContext.getCurrentInstance();
		BrainSession brainSession = (BrainSession) theFacesContext.getApplication().evaluateExpressionGet(theFacesContext, "#{brainSession}", BrainSession.class);
		return brainSession;
	}

	private String sortColumn = "";

	private int pageIndex = 0;

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ListDataModel<Item> getItemList() {

		if (itemList == null) {
			log.debug("loadItemList.");
			loadItemList(false);
		}

		// log.debug("Session:" + this);
		// log.debug("LDM1: " + itemList);

		return itemList;
	}

	public void loadItemList(boolean fullReload) {
		Lesson lesson = getCurrentLesson();

		log.debug("CurrentLesson: " + lesson.getDescription());
		if (fullReload)
			lesson.loadItems();
		itemList = new ListDataModel<Item>(lesson.getFilteredItems(filterText));
		pageIndex = 0;
	}

	public int getPageIndex() {
		log.debug("getI:" + pageIndex);
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex - (pageIndex % numRows);
		log.debug("setI:" + pageIndex);
	}

	int numRows = 10;

	private String lastAnswerText = "";

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public void setLastUserItem(UserItem userItem) {
		this.lastUserItem = userItem;

	}

	public UserItem getLastUserItem() {
		return this.lastUserItem;
	}

	public String getCommentType() {
		return commentType;
	}

	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}

	public String clearComment() {
		this.comment = "";
		this.commentType = "";
		return null;
	}

	public void setLastAnswerText() {
		this.lastAnswerText = answerText;
	}

	public String getLastAnswerText() {
		return lastAnswerText;
	}

	/**
	 * function to initialise the Session after a user logged in
	 */
	public void login() {
		loggedIn = true;
	}

	LessonWrapper selectedLesson;

	public void setSelectedLesson(LessonWrapper lesson) {
		selectedLesson = lesson;
		subscribeLesson(lesson);
	}

	public LessonWrapper getSelectedLesson() {
		return selectedLesson;
	}

	public String learnLesson() {
		FacesContext context = FacesContext.getCurrentInstance();
		UserLesson userLesson = (UserLesson) context.getExternalContext().getRequestMap().get("userlesson");
		log.debug("UserLesson: " + userLesson);
		try {
			learnLesson(userLesson);
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}
		return "method";
	}

	public void learnLesson(UserLesson userLesson) throws DBException {
		setCurrentUserLesson(userLesson);
		setLastUserItem(null);
		userLesson.getNextUserItem();
	}

	public String subscribeLesson(LessonWrapper lw2) {
		try {
			UserLesson userLesson = null;
			if (lw2.getIsSubscribed())
				userLesson = currentUser.getUserLessonByLessonID(lw2.lesson.getId());
			else
				userLesson = currentUser.subscribeLesson(lw2.lesson);
			// storeBeanFromFacesContext("CurrentUserLesson",context,
			// userLesson);
			setCurrentUserLesson(userLesson);

			userLesson.getNextUserItem();

			log.debug("bs: subscribeLesson -> " + lw2.lesson.getTitle());
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "method";
	}

	public String unsubscribeLesson() {
		try {
			currentUser.unsubscribeLesson(currentUserLesson);
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "user";
	}

	public void setCategory(ActionEvent event) {
		// TODO: ersetzen
		// String cat = (String)
		// event.getComponent().getAttributes().get("cat");
		String cat = "FUNKTIONGIBTESNICHT";
		setCategory(cat);
	}

	private void setCategory(String cat) {

		// mit diesem attribut im request wird geprüft, ob aus der tag cloud
		// selektiert wurde
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		request.setAttribute(CAT_SELECTION, "cat");

		if (!filterCat.equals(cat)) {
			filterCat = cat;
		}

		//lessonCode = "";
	}

	public ListDataModel<Category> getLocalCategories() {
		if (localCategories == null) {
			loadLocalCategories();
		}
		return new ListDataModel<Category>(localCategories);
	}

	public void loadLocalCategories() {
		localCategories = new Vector<Category>();
		String lang = getCurrentLocale().getLanguage();

		// log.debug("C : " + lang);

		Vector<Category> categories = brainSystem.getCategories();
		for (Category c : categories) {
			// log.debug("CL: " + c.getLocale());
			if (c.getLocale().equals(lang)) {
				localCategories.add(c);
			}
		}

		calculateCategorySize();

	}

	public void unloadLocalCategories() {
		localCategories = null;
	}

	private void calculateCategorySize() {
		// Vector<Category> cats = new Vector<Category>();

		Collections.sort(localCategories, new Comparator<Category>() {
			public int compare(Category o1, Category o2) {
				return (o1.getCount() > o2.getCount()) ? 1 : -1;
			}
		});

		int cat_count = localCategories.size();
		int cat_sizes = CAT_SIZES;
		int cat_slots = (cat_count / cat_sizes) + ((cat_count % cat_sizes) == 0 ? 0 : 1);
		int pos = 0;
		for (Category c : localCategories) {
			c.setSize(pos / cat_slots);
			pos++;
		}

		Collections.sort(localCategories, new Comparator<Category>() {
			public int compare(Category o1, Category o2) {
				return (o1.getText().compareTo(o2.getText()));
			}
		});

	}

	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(String text) {
		if (!this.filterText.equals(text)) {
			itemList = null;
			pageIndex = 0;
			this.filterText = text;
		}
	}

	public String getFilterCat() {
		return filterCat == null ? "" : filterCat;
	}

	public void setFilterCat(String filterCat) {
		this.filterCat = filterCat;
	}

	public Locale getCurrentLocale() {
		return currentLocale;
	}

	public void loadResourceBundle() {
		resourceBundle = ResourceBundle.getBundle("org.stoevesand.brain.i18n.MessagesBundle", currentLocale);
	}

	public void incrementLoginCounter() {
		loginAttempts++;
	}

	public int getLoginAttempts() {
		return loginAttempts;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	// public void setCurrentUser(User currentUser) {
	// this.currentUser = currentUser;
	// }

	public Item getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(Item currentItem) {
		this.currentItem = currentItem;
	}

	public News getCurrentNews() {
		return currentNews;
	}

	public void setCurrentNews(News currentNews) {
		this.currentNews = currentNews;
	}

	public void setCurrentLocale(Locale currentLocale) {
		this.currentLocale = currentLocale;
	}

	public UserItem getCurrentUserItem() {
		return currentUserItem;
	}

	public void setCurrentUserItem(UserItem currentUserItem) {
		this.currentUserItem = currentUserItem;
	}

	public UserLesson getCurrentUserLesson() {
		return currentUserLesson;
	}

	public void setCurrentUserLesson(UserLesson currentUserLesson) {
		this.currentUserLesson = currentUserLesson;
	}

	// bei einem direkteinstieg kann die aktuelle UserLesson so gewählt werden
	// der Parameter ulid wird dann aus der JSP übergeben
	public boolean setCurrentUserLesson(String sulid) {
		boolean ret = true;
		try {
			System.out.println("U: " + currentUser);
			if (currentUser != null) {
				long ulid = Long.parseLong(sulid);
				UserLesson userLesson = currentUser.getUserLesson(ulid);
				System.out.println("LOAD: " + userLesson);
				if (userLesson != null)
					learnLesson(userLesson);
				else
					ret = false;
			}
		} catch (Exception e) {
			System.out.println("exc: " + e);
			ret = false;
		}
		return ret;
	}

	public Lesson getCurrentLesson() {
		return currentLesson;
	}

	public void setCurrentLesson(Lesson currentLesson) {
		this.currentLesson = currentLesson;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/*
	 * public String fileLessonUploaded() { System.out.println(
	 * "uploaded new entries."); if (lessonLoader.fileLessonUploaded(this,
	 * getCurrentLesson())) { // force a reload of all items after the upload
	 * getCurrentLesson().reset(); // unload the library unloadLibrary();
	 * itemList = null; } return "validateupload"; }
	 */

	void addItemToUserLesson() {

		BrainDB brainDB = brainSystem.getBrainDB();

		try {
			Vector<UserLesson> userLessons = brainDB.getUserLessons(currentLesson);
			for (UserLesson userLesson : userLessons) {
				log.debug("activate extra item ULID: " + userLesson.getId());
				brainDB.activateUserItemExp(userLesson);
			}
		} catch (DBException e) {
			e.printStackTrace();
		}

	}

	String trimBOM(String line) {
		byte[] bl = line.getBytes();
		if ((bl[0] == -17) && (bl[1] == -69) && (bl[2] == -65)) {
			line = line.substring(1);
		}
		// System.out.println("0: " + Character.toString((char)bl[0]));
		// System.out.println("1: " + Byte.toString(bl[1]));
		// System.out.println("2: " + Byte.toString(bl[2]));
		// byte[] bom = new byte[3];
		// bom[0] = (byte) 0xEF; -17
		// bom[1] = (byte) 0xBB; -69
		// bom[2] = (byte) 0xBF; -65
		return line;
	}

	String readToken(String[] field, int pos) {
		if (field.length > pos)
			return field[pos];
		return null;
	}

	long readLongToken(String[] field, int pos) {
		long ret = 0;
		if (field.length > pos) {
			String s = field[pos];
			try {
				ret = Long.parseLong(s);
			} catch (Exception e) {
				log.error("not a valid extid: " + s);
			}
		}
		return ret;
	}

	public String saveCurrentItem() {
		String ret = "ok";
		try {
			getCurrentItem().saveAction();
			itemList = null;
		} catch (DBException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public int getEncoding() {
		return encoding;
	}

	public void setEncoding(int encoding) {
		this.encoding = encoding;
	}

	public int getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(int fileFormat) {
		this.fileFormat = fileFormat;
	}

	public void putParameter(String key, String value) {
		parameters.put(key, value);
	}

	public String getParameter(String key) {
		return parameters.get(key);
	}

	// *********************************************************************
	// *********************************************************************
	// *********************************************************************
	// *********************************************************************
	// AUS BRAINSYSTEM

	ListDataModel<News> news = null;
	ListDataModel<News> allnews = null;

	private String markedText;
	private String localFileName;

	public Vector<Lesson> getUnsubscribedLessons() {
		Vector<Lesson> lessons;
		Vector<Lesson> ret = new Vector<Lesson>();
		try {
			lessons = brainSystem.getBrainDB().getLessons();

			Iterator<Lesson> it = lessons.iterator();
			while (it.hasNext()) {
				Lesson lesson = it.next();
				if (!currentUser.hasLesson(lesson)) {
					ret.add(lesson);
				}
			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public ListDataModel<News> getNews() {
		news = loadNews(getCurrentLocale());
		return news;
	}

	public Vector<UserLesson> getUserLessons() {
		log.debug("bs: getUserLessons()");
		return currentUser.getLessons();
	}

	// public String selectUser() {
	// FacesContext context = FacesContext.getCurrentInstance();
	// User user = (User)
	// context.getExternalContext().getRequestMap().get("user");
	//
	// // storeBeanFromFacesContext("CurrentUser",context, user);
	// setCurrentUser(user);
	//
	// log.debug("bs: selectUser -> " + user.getName());
	//
	// return "user";
	// }

	// public String infoLesson() {
	// try {
	// FacesContext context = FacesContext.getCurrentInstance();
	// // LessonWrapper lw = (LessonWrapper)
	// // context.getExternalContext().getRequestMap().get("lw");
	// LessonWrapper lw2 = (LessonWrapper) getLessonLibrary().getRowData();
	//
	// // storeBeanFromFacesContext("CurrentLesson",context, lw2.getLesson());
	// setCurrentLesson(lw2.getLesson());
	// RequestContext rc = RequestContext.getCurrentInstance();
	//
	// UIComponent ot1 = context.getViewRoot().findComponent("ot1");
	//
	// // TODO: geht noch alles, wenn das auskommentiert ist
	// // rc.addPartialTarget(ot1);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// return "fatal_DB";
	// }
	//
	// return null;
	// }

	// public String learnLesson() {
	// learnLesson();
	// return "userLesson";
	// }

	public String confLesson() {
		log.debug("bs: selectUserLesson... ");
		FacesContext context = FacesContext.getCurrentInstance();
		UserLesson userLesson = (UserLesson) context.getExternalContext().getRequestMap().get("userlesson");
		// storeBeanFromFacesContext("CurrentUserLesson",context, userLesson);
		setCurrentUserLesson(userLesson);

		return "confLesson";
	}

	public String knowAnswer() {
		log.debug("knowAnswer");
		FacesContext context = FacesContext.getCurrentInstance();
		log.debug("context: " + context);
		UserItem userItem = getCurrentUserItem();

		try {
			userItem.knowAnswer();
			setLastUserItem(userItem);
			setLastAnswerText();
			getNextUserItem();
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		setAnswerText("<ohne Eingabe>");
		log.debug("correct");

		return "lesson";
	}

	public String failAnswer() {
		UserItem userItem = getCurrentUserItem();

		try {
			userItem.failAnswer();
			getNextUserItem();
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		setAnswerText("<ohne Eingabe>");
		log.debug("wrong");

		return "lesson";
	}

	public String editItem() {
		FacesContext context = FacesContext.getCurrentInstance();

		try {
			ListDataModel<Item> ldm = getItemList();
			log.debug("LDM: " + ldm);
			Item item = (Item) ldm.getRowData();
			Integer ri = (Integer) context.getExternalContext().getRequestMap().get("rowIndex");
			log.debug("editItem: " + item.getId() + " - " + item.getText() + " (" + ri.toString() + ")");
			item.modify();
			// storeBeanFromFacesContext("CurrentItem",context, item);
			setCurrentItem(item);
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "edititem";
	}

	public String editNewItem() {
		try {
			Item item = new Item(getCurrentLesson());
			item.modify();
			// storeBeanFromFacesContext("CurrentItem",context, item);
			setCurrentItem(item);
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "newitem";
	}

	public String deleteItem() {

		try {
			Item item = (Item) getItemList().getRowData();
			item.delete();
			loadItemList(true);
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "editlesson";
	}

	public String editNews() {
		FacesContext context = FacesContext.getCurrentInstance();

		try {
			ListDataModel<News> ldm = getAllNews();
			log.debug("LDM: " + ldm);
			News news = (News) ldm.getRowData();
			Integer ri = (Integer) context.getExternalContext().getRequestMap().get("rowIndex");
			log.debug("editNews: " + news.getId() + " - " + news.getTitle() + " (" + ri.toString() + ")");
			news.modify();
			setCurrentNews(news);
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "editnews";
	}

	public String addNews() {
		try {
			News news = new News();
			news.modify();
			setCurrentNews(news);
			allnews = null;
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "editnews";
	}

	public ListDataModel<News> getAllNews() {
		if (allnews == null) {
			allnews = loadNews(null);
		}
		return allnews;
	}

	public ListDataModel<News> loadNews(Locale locale) {
		ListDataModel<News> lnews = null;
		try {
			lnews = new ListDataModel<News>(brainSystem.getBrainDB().getNews(locale));
		} catch (DBException e) {
			e.printStackTrace();
		}
		return lnews;
	}

	public String listAllItems() {
		try {
			loadItemList(true);
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "editlesson";
	}

	public String deactivateCurrentItem() {
		UserLesson userLesson = getCurrentUserLesson();
		try {
			userLesson.deactivateCurrentItem();
			getNextUserItem();
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "lesson";
	}

	/**
	 * @return
	 */
	public String storeComment() {
		UserItem userItem = getCurrentUserItem();
		try {
			userItem.storeComment(getComment(), getCommentType());
			clearComment();
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return null;
	}

	public String forceAnswer() {
		UserItem userItem = getCurrentUserItem();
		try {
			userItem.forceAnswer();
			setLastUserItem(userItem);
			setLastAnswerText();
			getNextUserItem();
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}
		return "lesson_enter";
	}

	public String checkAnswerText() {
		String ret = "lessonwrong";
		UserItem userItem = getCurrentUserItem();
		log.debug("bs checkAnswerText:" + getAnswerText());

		// if (answerText.length() > 0) {
		try {
			if (userItem.checkAnswerText(getAnswerText())) {
				ret = "lesson_enter";
				setLastUserItem(userItem);
				setLastAnswerText();
				getNextUserItem();
			} else {
				setLastUserItem(null);
				markedText = BrainSystem.findLongestCommonString(userItem.getItem(), getAnswerText());
			}
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}
		// }
		return ret;
	}

	public String leaveMessage() {

		String emailSubjectTxt = "Message from " + currentUser.getName();
		String emailMsgTxt = getMessage();
		setMessage("");

		SendMailUsingAuthentication.sendConfirmationMail("info@notonto.de", emailSubjectTxt, emailMsgTxt);

		return "user";
	}

	private void getNextUserItem() throws DBException {
		setAnswerText("");
		markedText = "";
		UserLesson userLesson = getCurrentUserLesson();
		userLesson.getNextUserItem();
	}

	public String getNextItem() {
		try {
			getNextUserItem();
		} catch (DBException e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		return "lesson_enter";
	}

	public String getMarkedText() {
		return markedText;
	}

	public void setMarkedText(String markedText) {
		this.markedText = markedText;
	}

	public String getLocalFileName() {
		return localFileName;
	}

	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}

	// action mit der getestet wird, ob schon eine Karte verfügbar ist.
	// So kann man die Zeit runterlaufen lassen.
	public String tryNextUserItem() throws DBException {
		getNextUserItem();
		return null;
	}

	public String fileLessonLocalUpload() {
		FacesContext context = FacesContext.getCurrentInstance();
		UIComponent root = context.getViewRoot();
		UIComponent fileinput = root.findComponent("localUpload:localFileName");

		File file = new File(localFileName);
		if (file != null) {

			// log.debug("f: " + file.getContentType());
			log.debug("f: " + file.getName());

			try {
				Administration.loadLesson(new FileInputStream(file));
				FacesMessage message = new FacesMessage("Successfully uploaded file " + file.getName() + " ");
				if (fileinput != null)
					context.addMessage(fileinput.getClientId(context), message);
			} catch (IOException e) {
				FacesMessage message = new FacesMessage("Error loading file " + e.getMessage());
				if (fileinput != null)
					context.addMessage(fileinput.getClientId(context), message);
				e.printStackTrace();
			}

			System.out.println("Successfully uploaded file " + file.getName() + " ");
		} else {
			FacesMessage message = new FacesMessage("File not found.");
			if (fileinput != null)
				context.addMessage(fileinput.getClientId(context), message);
		}
		return null;
	}

	public int getAvailable(UserLesson userLesson) throws DBException {
		// return userStats.getAvailable();
		return brainSystem.getBrainDB().getUserLessonAvailable(userLesson);
	}

	public void setItemList(ListDataModel<Item> il) {
		this.itemList = il;
	}

}
