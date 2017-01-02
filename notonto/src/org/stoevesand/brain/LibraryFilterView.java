package org.stoevesand.brain;

import java.util.List;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.SendMailUsingAuthentication;
import org.stoevesand.util.StringUtils;

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

	private String inviteeEmail = "";

	public String getInviteeEmail() {
		return inviteeEmail;
	}

	public void setInviteeEmail(String inviteeEmail) {
		this.inviteeEmail = inviteeEmail;
	}

	private String inviteeCode = "";

	public String getInviteeCode() {
		return inviteeCode;
	}

	public void setInviteeCode(String inviteeCode) {
		this.inviteeCode = inviteeCode;
	}

	private List<LessonWrapper> allLessons = null;
	private List<LessonWrapper> publicLessons = null;
	private List<LessonWrapper> filteredLessons = null;

	private Vector<String> categories;

	public List<LessonWrapper> getLessons() {
		if ((lessonCode != null) && (lessonCode.length() > 0)) {
			return filter();
		} else {
			return publicLessons;
		}
	}

	private List<LessonWrapper> filter() {
		List<LessonWrapper> ret = new Vector<LessonWrapper>();

		for (LessonWrapper lw : allLessons) {
			if (lw.lesson.getCode().equals(lessonCode)) {
				ret.add(lw);
			}
		}

		return ret;
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

	// ##############################################

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

	/**
	 * verzweigt auf die Einladungsseite und lädt die CurrentLesson
	 * 
	 * @return
	 */
	public String invite() {
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
			return "invite";
		} else {
			log.error("OwnerLesson is null.");
		}
		return null;
	}

	public String acceptInvitation() {

		if (!brainSession.isLoggedIn()) {
			// wenn nicht eingelogged nur merken
			brainSession.rememberInvitation(inviteeEmail, inviteeCode);
		} else {
			// wenn eingeloggt die Lektion gleich hinzufügen.
			brainSession.getCurrentUser().acceptInvitation(inviteeEmail, inviteeCode);
		}

		return "user";
	}

	public String sendInvitation() {

		try {
			FacesContext context = FacesContext.getCurrentInstance();

			log.debug("confirm");

			String emailSubjectTxt = brainSession.getCurrentUser().getNick() + " hat Sie zu einer Lektion bei notonto eingeladen.";
			String emailMsgTxt = brainSystem.getInviteText();
			String code = brainSession.getCurrentLesson().getCode();
			emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@USER@", brainSession.getCurrentUser().getNick());

			String rcp = context.getExternalContext().getRequestContextPath();

			String unlockLink = "http://" + brainSystem.getServerName() + "/invite_add.jsf?lesson=" + code;
			emailMsgTxt = StringUtils.replaceSubstring(emailMsgTxt, "@LINK@", unlockLink);

			SendMailUsingAuthentication.sendConfirmationMail(inviteeEmail, emailSubjectTxt, emailMsgTxt);
		} catch (Exception e) {
			e.printStackTrace();
			return "fatal_DB";
		}

		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Die Einladung wurde an " + inviteeEmail + " verschickt."));
		return "done";
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

	public String deleteLesson() {
		if (ownerLibrary != null) {
			LessonWrapper lw2 = (LessonWrapper) ownerLibrary.getRowData();
			log.debug("Edit Owner Lesson: " + lw2.getLesson().getDescription());
			try {
				BrainDB brainDB = brainSystem.getBrainDB();
				brainDB.deleteLesson(lw2.getLesson());
				refreshModel();
			} catch (Exception e) {
				e.printStackTrace();
				return "fatal_DB";
			}
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

}