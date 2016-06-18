package org.stoevesand.brain;

import java.util.List;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.ListDataModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.persistence.BrainDB;

@ManagedBean(name = "libraryFilterView")
@ViewScoped
public class LibraryFilterView {

	private static Logger log = LogManager.getLogger(LibraryFilterView.class);

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

	private String lessonCode = "";

	private List<LessonWrapper> allLessons = null;
	private List<LessonWrapper> publicLessons = null;
	private List<LessonWrapper> filteredLessons = null;

	private Vector<String> categories;

	public List<LessonWrapper> getLessons() {
		return allLessons;
	}

	public List<LessonWrapper> getFilteredLessons() {
		return filteredLessons;
	}

	public void setFilteredLessons(List<LessonWrapper> filteredLessons) {
		this.filteredLessons = filteredLessons;
	}

	@PostConstruct
	public void init() {
		refreshModel();
		categories = new Vector<String>();
		categories.add("Languages");
		categories.add("Literature");
		categories.add("Geography");
	}

	public List<String> getCategories() {
		return categories;
	}

	void refreshModel() {
		loadLibrary();
	}

	public void loadLibrary() {

		log.debug("getLibrary.start.");
		Vector<Lesson> lessons;
		allLessons = new Vector<LessonWrapper>();
		publicLessons = new Vector<LessonWrapper>();
		BrainDB brainDB = brainSystem.getBrainDB();
		try {

			lessons = brainDB.getLessons();

			for (Lesson lesson : lessons) {
				LessonWrapper lw = new LessonWrapper();
				lw.lesson = lesson;
				User currentUser = brainSession.getCurrentUser();
				if (currentUser != null) {
					lw.subscribed = currentUser.hasLesson(lesson);
					lw.owner = lesson.isOwner(currentUser); // cu.getName().equals("steffi");
				}
				allLessons.add(lw);
				if (lesson.isPublicLesson()) {
					publicLessons.add(lw);
				}
			}
		} catch (DBException e) {
			e.printStackTrace();
		}
		return;
	}

	public String getLessonCode() {
		return lessonCode;
	}

	public void setLessonCode(String lessonCode) {
		this.lessonCode = lessonCode;
	}

	public String selectLessonCode() {
		library = null;
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

}