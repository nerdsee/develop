package org.stoevesand.brain;

import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.persistence.BrainDB;

@ManagedBean
@SessionScoped
public class LibraryView {

	private static Logger log = LogManager.getLogger(LibraryView.class);

	private MenuModel model;

	@ManagedProperty(value = "#{brainSystem}")
	private BrainSystem brainSystem;

	@ManagedProperty(value = "#{brainSession}")
	private BrainSession brainSession;

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

	private Topic currentTopic = null;
	private Topic parentTopic = null;
	private Topic rootTopic = null;

	private String lessonCode = "";

	private Vector<Topic> topics = null;
	private Vector<LessonWrapper> lessons = null;

	public Vector<LessonWrapper> getLessons() {
		return lessons;
	}

	@PostConstruct
	public void init() {
		initTopics();
		refreshModel();
	}

	void refreshModel() {
		model = new DefaultMenuModel();

		DefaultSubMenu firstSubmenu;
		
		if (currentTopic != null) {
			topics = currentTopic.getSubTopics();
			firstSubmenu = new DefaultSubMenu(currentTopic.getText());
		} else {
			topics = new Vector<Topic>();
			firstSubmenu = new DefaultSubMenu("none");
		}

		// First submenu

		if (parentTopic != null) {
			DefaultMenuItem up = new DefaultMenuItem("up");
			up.setCommand("#{libraryView.selectTopic(" + parentTopic.getId() + ")}");
			up.setUpdate("topicMenu, libraryDT");
			firstSubmenu.addElement(up);
		}

		for (Topic topic : topics) {
			DefaultMenuItem item = new DefaultMenuItem(topic.getText());
			item.setCommand("#{libraryView.selectTopic(" + topic.getId() + ")}");
			item.setUpdate("topicMenu, libraryDT");
			firstSubmenu.addElement(item);
		}

		// Lessons zum aktuellen Topic laden
		lessons = loadLibrary();

		model.addElement(firstSubmenu);
	}

	private void initTopics() {
		rootTopic = brainSystem.getBrainDB().getTopicTree(brainSession.getCurrentLocale().getLanguage());
		currentTopic = rootTopic;
		// parentTopic = rootTopic;
		log.debug("ROOT: " + currentTopic.getText() + " - " + currentTopic.getId());
	}

	public Vector<LessonWrapper> loadLibrary() {

		log.debug("getLibrary.start.");
		Vector<Lesson> lessons;
		Vector<LessonWrapper> ret = new Vector<LessonWrapper>();
		BrainDB brainDB = brainSystem.getBrainDB();
		try {
			if (currentTopic != null) { // auswahl nach Topic: Sprachen,
										// Allgemeinbildung, ...
				lessons = brainDB.getLessons(currentTopic); // currentLocale.getLanguage()
			}

			else if ((lessonCode != null) && (lessonCode.length() > 1)) {
				lessons = brainDB.getLessonsByCode(lessonCode);
			} else { // leere Liste erzeugen
				lessons = new Vector<Lesson>();
			}

			for (Lesson lesson : lessons) {
				LessonWrapper lw = new LessonWrapper();
				lw.lesson = lesson;
				User currentUser = brainSession.getCurrentUser();
				if (currentUser != null) {
					lw.subscribed = currentUser.hasLesson(lesson);
					lw.owner = lesson.isOwner(currentUser); // cu.getName().equals("steffi");
				}
				ret.add(lw);
			}
		} catch (DBException e) {
			e.printStackTrace();
		}

		return ret;
	}

	/*
	 * private void loadTopics() { rootTopic =
	 * brainSystem.getBrainDB().getTopicTree(currentLocale.getLanguage());
	 * currentTopic = rootTopic; log.debug("ROOT: " + currentTopic.getText() +
	 * " - " + currentTopic.getId()); } private Topic findTopic(long id) {
	 * return findTopic(rootTopic, id); } private Topic findTopic(Topic
	 * startTopic, long id) { if (startTopic.getId() == id) return startTopic;
	 * for (Topic topic : startTopic.getSubTopics()) { Topic resTopic =
	 * findTopic(topic, id); if (resTopic != null) return resTopic; } return
	 * null; } public Topic getRootTopic() { return rootTopic; } void
	 * selectCurrentTopic() { try { FacesContext context =
	 * FacesContext.getCurrentInstance(); boolean cat = selectedByCategory();
	 * HttpServletRequest request = (HttpServletRequest)
	 * context.getExternalContext().getRequest(); String uri =
	 * request.getRequestURI(); int pos1 = uri.indexOf("topic_"); if ((pos1 >=
	 * 0) && !cat) { int pos2 = uri.indexOf(".jsf"); String sid =
	 * uri.substring(pos1 + 6, pos2); long id = Long.valueOf(sid); currentTopic
	 * = findTopic(id); filterCat = ""; } } catch (Exception e) { log.error(e);
	 * currentTopic = rootTopic; } unloadLibrary(); } public
	 * ListDataModel<Topic> getTopicPath() { selectCurrentTopic(); Vector<Topic>
	 * tpp = new Vector<Topic>(); if (currentTopic != null)
	 * currentTopic.getTopicPath(tpp); else rootTopic.getTopicPath(tpp);
	 * topicPath = new ListDataModel<Topic>(tpp); return topicPath; } public
	 * ListDataModel<LessonWrapper> loadLibrary() { selectCurrentTopic();
	 * log.debug("getLibrary.start."); Vector<Lesson> lessons;
	 * Vector<LessonWrapper> ret = new Vector<LessonWrapper>(); BrainDB brainDB
	 * = brainSystem.getBrainDB(); try { if (currentTopic != null) { // auswahl
	 * nach Topic: Sprachen, Allgemeinbildung, ... lessons =
	 * brainDB.getLessons(currentTopic); // currentLocale.getLanguage() } else
	 * if ((lessonCode != null) && (lessonCode.length() > 1)) { // Auswahl nach
	 * dem Freischaltcode lessons = brainDB.getLessonsByCode(lessonCode); } else
	 * if ((filterCat != null) && (filterCat.length() > 1)) { // Auswahl nach
	 * der Kategorie (Tag Cloud) lessons =
	 * brainDB.getLessonsByFilter(filterCat); } else { // leere Liste erzeugen
	 * lessons = new Vector<Lesson>(); } for (Lesson lesson : lessons) {
	 * LessonWrapper lw = new LessonWrapper(); lw.lesson = lesson; if
	 * (currentUser != null) { lw.subscribed = currentUser.hasLesson(lesson);
	 * lw.owner = lesson.isOwner(currentUser); // cu.getName().equals("steffi");
	 * } ret.add(lw); } } catch (DBException e) { e.printStackTrace(); } library
	 * = new ListDataModel<LessonWrapper>(ret); log.debug("getLibrary.end.");
	 * return library; } public ListDataModel<LessonWrapper> getLibrary() { if
	 * (library == null) { library = loadLibrary(); } return library; } public
	 * String subscribeLesson() { LessonWrapper lw2 = (LessonWrapper)
	 * getLibrary().getRowData(); return subscribeLesson(lw2); } public
	 * MenuModel getTopicPathMenu() { selectCurrentTopic(); Vector<Topic> tpp =
	 * new Vector<Topic>(); if (currentTopic != null)
	 * currentTopic.getTopicPath(tpp); else rootTopic.getTopicPath(tpp);
	 * MenuModel model = new DefaultMenuModel(); for (Topic t : tpp) {
	 * DefaultMenuItem item = new DefaultMenuItem(t.getText());
	 * item.setUrl("/lib/topic_" + t.getId() + ".jsf"); model.addElement(item);
	 * } return model; } public Topic getCurrentTopic() { return currentTopic; }
	 * public Topic getCurrentTopicDirect() { selectCurrentTopic(); return
	 * currentTopic; } public ListDataModel<LessonWrapper> getLessonLibrary() {
	 * return getLibrary(); } public String editLesson() { LessonWrapper lw2 =
	 * (LessonWrapper) getLessonLibrary().getRowData(); log.debug(
	 * "Edit Lesson: " + lw2.getLesson().getDescription()); try { //
	 * userItem.editItem(); //
	 * storeBeanFromFacesContext("CurrentLesson",context, lw2.getLesson());
	 * setCurrentLesson(lw2.getLesson()); loadItemList(true); } catch (Exception
	 * e) { e.printStackTrace(); return "fatal_DB"; } return "editlesson"; }
	 * public String deleteLesson() { LessonWrapper lw2 = (LessonWrapper)
	 * getLessonLibrary().getRowData(); log.debug("Delete Lesson: " +
	 * lw2.getLesson().getDescription()); try {
	 * brainSystem.getBrainDB().deleteLesson(lw2.getLesson()); unloadLibrary();
	 * } catch (Exception e) { e.printStackTrace(); return "fatal_DB"; } return
	 * "editlesson"; } ListDataModel<Topic> topics = null; ListDataModel<Topic>
	 * topicPath = null; public int getTopicCount() { return topics == null ? 0
	 * : topics.getRowCount(); } public String selectPathTopic() { currentTopic
	 * = (Topic) topicPath.getRowData(); filterCat = ""; return null; } public
	 * String selectTopic() { currentTopic = (Topic) topics.getRowData();
	 * filterCat = ""; return null; } public String deleteOwnerLesson() { try {
	 * brainSystem.getBrainDB().deleteLesson(getCurrentLesson()); } catch
	 * (Exception e) { e.printStackTrace(); return "fatal_DB"; }
	 * setOwnerLibrary(null); return "editlesson"; } public String
	 * subscribeOwnerLesson() { if (ownerLibrary != null) { try { LessonWrapper
	 * lw2 = (LessonWrapper) ownerLibrary.getRowData(); UserLesson userLesson =
	 * null; if (lw2.getIsSubscribed()) userLesson =
	 * currentUser.getUserLessonByLessonID(lw2.lesson.getId()); else userLesson
	 * = currentUser.subscribeLesson(lw2.lesson); //
	 * storeBeanFromFacesContext("CurrentUserLesson",context, userLesson);
	 * setCurrentUserLesson(userLesson); userLesson.getNextUserItem(); } catch
	 * (DBException e) { e.printStackTrace(); return "fatal_DB"; } return
	 * "lesson"; } else { log.error("OwnerLesson is null."); } return null; }
	 */

	public String getLessonCode() {
		return lessonCode;
	}

	public void setLessonCode(String lessonCode) {
		this.lessonCode = lessonCode;
	}

	public String selectLessonCode() {
		library = null;
		currentTopic=null;
		refreshModel();

		return "lib";
	}

	ListDataModel<LessonWrapper> library = null;
	ListDataModel<LessonWrapper> ownerLibrary = null;

	public void setOwnerLibrary(ListDataModel<LessonWrapper> ownerLibrary) {
		this.ownerLibrary = ownerLibrary;
	}

	public void unloadLibrary() {
		library = null;
		ownerLibrary = null;
	}

	public ListDataModel<LessonWrapper> getOwnerLibrary() {
		if (ownerLibrary == null)
			loadOwnerLibrary();
		return ownerLibrary;
	}

	public ListDataModel<LessonWrapper> loadOwnerLibrary() {
		log.debug("getOwnerLibrary.start.");
		Vector<Lesson> lessons;
		Vector<LessonWrapper> ret = new Vector<LessonWrapper>();
		User currentUser = brainSession.getCurrentUser();
		BrainDB brainDB = brainSystem.getBrainDB();
		try {
			lessons = brainDB.getOwnerLessons(currentUser); // currentLocale.getLanguage()
			for (Lesson lesson : lessons) {
				LessonWrapper lw = new LessonWrapper();
				lw.lesson = lesson;
				if (currentUser != null) {
					lw.subscribed = currentUser.hasLesson(lesson);
					lw.owner = lesson.isOwner(currentUser);
					// cu.getName().equals("steffi");
					lw.group = lesson.isGroup(currentUser);
				}
				ret.add(lw);
			}
		} catch (DBException e) { // TODO Auto-generated catch
			e.printStackTrace();
		}
		ownerLibrary = new ListDataModel<LessonWrapper>(ret);
		log.debug("getLibrary.end.");
		return ownerLibrary;
	}

	public String newLesson() {
		User currentUser = brainSession.getCurrentUser();
		try {
			Lesson lesson = new Lesson();
			lesson.setOwner(currentUser);
			lesson.setTitle("HALLO");
			lesson.modify();
			brainSession.setCurrentLesson(lesson);
			setOwnerLibrary(null);
			brainSession.setItemList(null);
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}
		return "editlessonmeta";
	}

	public String editOwnerLesson() {
		if (ownerLibrary != null) {
			LessonWrapper lw2 = (LessonWrapper) ownerLibrary.getRowData();
			log.debug("Edit Owner Lesson: " + lw2.getLesson().getDescription());
			try {
				// userItem.editItem();
				// storeBeanFromFacesContext("CurrentLesson", context,
				// lw2.getLesson());
				brainSession.setCurrentLesson(lw2.getLesson());
				brainSession.loadItemList(true);
			} catch (Exception e) {
				e.printStackTrace();
				return "fatal_DB";
			}
			return "editlessonmeta";
		} else {
			log.error("OwnerLesson is null.");
		}
		return null;
	}

	public String editLessonUpload() {
		if (ownerLibrary != null) {
			LessonWrapper lw2 = (LessonWrapper) ownerLibrary.getRowData();
			log.debug("Edit Owner Lesson for Upload: " + lw2.getLesson().getDescription());
			try {
				brainSession.setCurrentLesson(lw2.getLesson());
				brainSession.loadItemList(true);
			} catch (Exception e) {
				e.printStackTrace();
				return "fatal_DB";
			}
			return "editlessonupload";
		} else {
			log.error("OwnerLesson is null.");
		}
		return null;
	}

	// ##############################################

	public MenuModel getModel() {
		return model;
	}

	public void selectTopic(int i) {

		if ((parentTopic != null) && (parentTopic.getId() == i)) {
			currentTopic = parentTopic;
			parentTopic = currentTopic.getParentTopic();
			refreshModel();
		} else {

			for (Topic topic : topics) {
				if (topic.getId() == i) {
					parentTopic = currentTopic;
					currentTopic = topic;
					refreshModel();
				}

			}
		}

		addMessage("Success", "Data saved: " + i);
	}

	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
}