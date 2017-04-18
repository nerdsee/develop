package org.stoevesand.brain.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.faces.model.SelectItem;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSession;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.Category;
import org.stoevesand.brain.Group;
import org.stoevesand.brain.UserScore;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.persistence.BrainDB;
import org.stoevesand.util.DBUtil;

@XmlRootElement(name = "lesson")
@XmlAccessorType(XmlAccessType.NONE)
public class Lesson {

	private static Logger log = LogManager.getLogger(Lesson.class);

	private BrainSystem brainSystem;
	private BrainSession brainSession;

	@XmlElement
	long id = 0;
	long ownerId = 0;
	long ownergroupId = 0;

	String title = "";
	String title_tmp = "";

	String icon = "";

	String description = "";
	String description_tmp = "";

	String abst = "";
	String abst_tmp = "";

	String tagsDE = "";
	String tagsES = "";
	String tagsEN = "";

	@XmlElement
	String qlang = "";
	@XmlElement
	String alang = "";

	String keyboardLayout = "DE";
	// int subscribedUsers;

	int publicLesson;
	String code = "";

	private int maxitems;

	int lessonType = 0;
	/**
	 * 0: private 1: public 2:friends
	 */
	static final int VISIBILITY_PRIVATE = 0;
	static final int VISIBILITY_PUBLIC = 1;
	static final int VISIBILITY_FRIENDS = 2;
	int visibility = 0;

	Vector<Item> items = null;
	Vector<Item> filteredItems = null;
	Vector<Category> categories = null;

	boolean dirty = false;

	private Group group;

	// private boolean ownerLesson;

	private User owner;

	public boolean isOwnerLesson() {
		return ownergroupId == 0;
	}

	public Lesson(ResultSet rs) throws SQLException {
		this.id = DBUtil.getLong(rs, "lessonid");
		this.ownerId = DBUtil.getLong(rs, "ownerID");
		this.ownergroupId = DBUtil.getLong(rs, "ownergroupID");

		this.title = DBUtil.getString(rs, "title");
		this.description = DBUtil.getString(rs, "description");
		this.abst = DBUtil.getString(rs, "abstract");
		this.keyboardLayout = DBUtil.getString(rs, "keyboardLayout");
		this.lessonType = DBUtil.getInt(rs, "type");
		this.maxitems = DBUtil.getInt(rs, "maxitems");
		this.icon = DBUtil.getString(rs, "icon");
		this.publicLesson = DBUtil.getInt(rs, "public");
		this.code = DBUtil.getString(rs, "code");
		// this.subscribedUsers = rs.getInt("cx");

		this.title_tmp = this.title;
		this.description_tmp = this.description;
		this.abst_tmp = this.abst;

		this.qlang = DBUtil.getString(rs, "qlang");
		this.alang = DBUtil.getString(rs, "alang");
		
		init();
	}

	public Lesson() {
		icon = "allg";
		qlang = "de";
		alang = "de";
		dirty = true;

		init();
	}

	private void init() {
		brainSystem = BrainSystem.getBrainSystem();
		brainSession = BrainSession.getBrainSession();
	}

	public void store() throws DBException {
		if (dirty) {
			BrainDB db = brainSystem.getBrainDB();

			db.addLesson(this);
			db.storeCategories(this, tagsDE, "de");
			db.storeCategories(this, tagsEN, "en");
			db.storeCategories(this, tagsES, "es");
			loadCategories();
			log.debug("Lesson: " + title + "(" + id + ")");

		}
		dirty = false;
	}

	public Vector<UserScore> getTop5() {

		BrainDB brainDB = brainSystem.getBrainDB();
		Vector<UserScore> top5 = brainDB.getLessonTop5(this);
		return top5;

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void addItem(Item item) {
		items.add(item);
	}

	public void addItemAt(Item item, int index) {
		if (items == null) {
			items = new Vector<Item>();
		}
		items.add(index, item);
	}

	@XmlElement
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Vector<Item> getItems() {
		if (items == null) {
			loadItems();
		}
		return items;
	}

	public Vector<Category> getCategories() {
		if (categories == null) {
			loadCategories();
		}
		return categories;
	}

	public Vector<Item> getFilteredItems(String filter) {
		loadItems(filter);
		return filteredItems;
	}

	public void loadItems(String filter) {
		BrainDB db = brainSystem.getBrainDB();
		try {
			filteredItems = db.getItems(this, filter);
		} catch (DBException e) {
			e.printStackTrace();
		}
	}

	public void loadItems() {
		BrainDB db = brainSystem.getBrainDB();
		try {
			items = db.getItems(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
	}

	public void loadCategories() {
		BrainDB db = brainSystem.getBrainDB();
		try {
			categories = db.getCategories(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
	}

	public int getItemCount() {
		int ret = 0;
		BrainDB db = brainSystem.getBrainDB();
		try {
			ret = db.getItemCount(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public int getSubscriberCount() {
		int ret = 0;
		BrainDB db = brainSystem.getBrainDB();
		try {
			ret = db.getSubscriberCount(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String getUsageCount() {
		String ret = "";
		BrainDB db = brainSystem.getBrainDB();
		try {
			ret = db.getLessonLevels(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public int getItemCountSlow() {
		return getItems().size();
	}

	public void setKeyboardLayout(String keyboardLayout) {
		this.keyboardLayout = keyboardLayout;
	}

	public String getKeyboardLayout() {
		return keyboardLayout;
	}

	public int getSubscribedUsers() {
		return 0; // subscribedUsers;
	}

	public String saveAction() {
		storeData();

		return "editlesson";
	}

	public void storeData() {
		this.title_tmp = this.title;
		this.description_tmp = this.description;
		this.abst_tmp = this.abst;

		dirty = true;

		try {
			store();

			// An dieser Stelle müssen die Category Maps im System und der
			// Session gelöscht werden, damit sie neu geladen werden.
			brainSystem.unloadCategories();
			brainSession.unloadLocalCategories();

		} catch (DBException e) {
			e.printStackTrace();
		}

	}

	public String rollbackAction() {
		this.title = this.title_tmp;
		this.description = this.description_tmp;
		this.abst = this.abst_tmp;

		return "editlesson";
	}

	public String getTagsDE() {
		return getTags("de");
	}

	public String getTagsEN() {
		return getTags("en");
	}

	public String getTagsES() {
		return getTags("es");
	}

	public String getTags(String country) {
		StringBuffer retb = new StringBuffer();
		for (Category c : getCategories()) {
			if (c.getLocale().equals(country)) {
				retb.append(c.getText());
				retb.append(" ");
			}
		}
		return retb.toString();
	}

	public void setTagsDE(String tags) {
		this.tagsDE = tags.trim();
	}

	public void setTagsEN(String tags) {
		this.tagsEN = tags.trim();
	}

	public void setTagsES(String tags) {
		this.tagsES = tags.trim();
	}

	public String getAbstract() {
		return abst;
	}

	public void setAbstract(String abst) {
		this.abst = abst;
	}

	public int getLessonType() {
		return lessonType;
	}

	public void setLessonType(int lessonType) {
		this.lessonType = lessonType;
	}

	public int getMaxItemsLevel0() {
		if (maxitems > 0)
			return maxitems;

		return brainSystem.getMaxItemsLevel0();

	}

	public boolean hasCategory(String filterCat) {
		for (Category cat : getCategories()) {
			if (cat.equals(filterCat))
				return true;
		}
		return false;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isPublicLesson() {
		return publicLesson == 1;
	}

	public boolean hasCode(String lessonCode) {
		if ((code != null) && (code.length() > 0) && (lessonCode.equals(getPrefix() + code)))
			return true;
		return false;
	}

	public String getPrefix() {
		String ret = "";
		if (isOwnerLesson())
			ret = getOwner().getPrefix();
		else {
			Group group = getGroup();
			ret = group.getPrefix();
		}
		return ret;
	}

	// public void setPrefix(String prefix) {
	// long prefixId=0;
	// try {
	// prefixId=Long.parseLong(prefix);
	// } catch(NumberFormatException nfe) {
	// prefixId=0;
	// }
	// }

	public String getQlang() {
		return qlang;
	}

	public void setQlang(String qlang) {
		this.qlang = qlang;
	}

	public String getAlang() {
		return alang;
	}

	public void setAlang(String alang) {
		this.alang = alang;
	}

	public boolean isOwner(User cu) {
		return ((cu.getId() == ownerId) || cu.getIsAdmin());
	}

	public boolean isGroup(User cu) {
		return (cu.getGroupID() == getOwnergroupId());
	}

	public void modify() {
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Deprecated
	public String getCodeSuffix() {
		// String ret="";
		// int prefixLen = getGroup().getPrefix().length();
		// if (prefixLen < 0)
		// prefixLen = 0;
		// if (code.length()>=prefixLen)
		// ret=code.substring(prefixLen);
		return getCode();
	}

	@Deprecated
	public void setCodeSuffix(String codeSuffix) {
		// String prefix = getGroup().getPrefix();
		this.code = codeSuffix;
	}

	public Group getGroup() {
		BrainDB db = brainSystem.getBrainDB();
		if (group == null) {
			try {
				if (ownergroupId > 0)
					group = db.getGroup(ownergroupId);
				else {
					long id = brainSession.getCurrentUser().getGroupID();
					System.out.println("GID: " + id);
					group = db.getGroup(id);
				}
			} catch (DBException e) {
				e.printStackTrace();
			}
		}
		return group;
	}

	public User getOwner() {
		BrainDB db = brainSystem.getBrainDB();
		if (owner == null) {
			try {
				owner = db.getUser(getOwnerId());
			} catch (DBException e) {
				e.printStackTrace();
			}
		}
		return owner;
	}

	public int getVisibility() {
		if ((code != null) && (code.trim().length() > 0))
			return VISIBILITY_FRIENDS;
		else
			return VISIBILITY_PRIVATE;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public int getHighestChapter() {
		int ret = 0;
		BrainDB db = brainSystem.getBrainDB();
		try {
			ret = db.getHighestChapter(this);
		} catch (DBException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void setOwner(User cu) {
		setOwnerId(cu.getId());
		// setOwnergroupId(cu.getGroupID());
	}

	public long getOwnergroupId() {
		return ownergroupId;
	}

	public void setOwnergroupId(long ownergroupId) {
		this.ownergroupId = ownergroupId;
	}

	// to force a reload of all items from db
	public void reset() {
		items = null;
	}

	public Item getItemByExtID(long extId) {
		for (Item item : getItems()) {
			if (item.getExtId() == extId)
				return item;
		}
		return null;
	}

	// ******************************
	// Code Selection

	private List<SelectItem> prefixList = null;

	public List<SelectItem> getPrefixList() {
		if (prefixList == null) {
			prefixList = new ArrayList<SelectItem>();
			String ownerPrefix = getOwner().getPrefix();
			if (ownerPrefix != null)
				addNewItemToList(ownerPrefix, "0");
			Group group = getGroup();
			if (group != null)
				addNewItemToList(group.getPrefix(), "" + group.getId());
		}
		return prefixList;
	}

	public int getPrefixListSize() {
		return getPrefixList().size();
	}

	public void addNewItemToList(String label, String value) {
		SelectItem si = new SelectItem();
		si.setLabel(label);
		si.setValue(value);
		prefixList.add(si);
	}

	public String toJSON() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append(" \"id\" : ").append(getId()).append(", ");
		buffer.append(" \"description\" : \"" + getDescription() + "\", ");
		buffer.append(" \"qlang\" : \"" + getQlang() + "\", ");
		buffer.append(" \"alang\" : \"" + getAlang() + "\" ");
		buffer.append("}");
		return buffer.toString();
	}

}
