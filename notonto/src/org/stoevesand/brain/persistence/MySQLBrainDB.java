package org.stoevesand.brain.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.Category;
import org.stoevesand.brain.Group;
import org.stoevesand.brain.Topic;
import org.stoevesand.brain.UserScore;
import org.stoevesand.brain.UserStats;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Answer;
import org.stoevesand.brain.model.Item;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.util.CryptUtils;
import org.stoevesand.util.DBUtil;
import org.stoevesand.util.News;

public class MySQLBrainDB implements BrainDB {

	private static Logger log = LogManager.getLogger(MySQLBrainDB.class);

	// Vector lessons = new Vector();
	// Vector items = new Vector();
	// Vector users = new Vector();

	// String dbcs = "";
	// String dbname = null;
	// String user = null;
	// String pass = null;

	public MySQLBrainDB(String dbcs, String dbname, String user, String pass) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			// this.dbcs = dbcs;
			// this.dbname = dbname;
			// this.user = user;
			// this.pass = pass;

			// java:jboss/datasources/notonto
			try {
				Context initContext = new InitialContext();
				// Context envContext = (Context)
				// initContext.lookup("java:jboss");
				// ds = (DataSource) envContext.lookup("datasources/notonto");
				ds = (DataSource) initContext.lookup("java:jboss/datasources/notonto");
				if (ds == null) {
					log.error("Unable to aquire data source");
				}
			} catch (Exception ne) {
				throw new RuntimeException("Unable to aquire data source", ne);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addUser(User user) throws DBException {
		if (user.getId() >= 0)
			updateUser(user);
		else {
			insertUser(user);
		}
	}

	public void updateUser(User user) throws DBException {
		Connection conn = getConnection("updateUser id(" + user.getId() + ")");
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("insert into users (id,name,nick,prefix) values (?,?,?,?)");
			ps.setLong(1, user.getId());
			ps.setString(2, user.getName());
			ps.setString(3, user.getNick());
			ps.setString(4, user.getPrefix());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
		// users.add(user);
	}

	public void insertUser(User user) throws DBException {
		Connection conn = getConnection("insertUsers");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("insert into users (name,nick,password,unlocktext,unlocked,register_date,lastlogin_date,laststatus_date,nextstatus_date) values (?,?,?,?,?,?,?,?,?)");
			ps.setString(1, user.getName());
			ps.setString(2, user.getNick());
			ps.setString(3, user.getPassword());
			ps.setString(4, user.getUnlock());
			ps.setBoolean(5, user.getUnlocked());
			ps.setTimestamp(6, new Timestamp(user.getRegisterDate().getTime()));
			ps.setTimestamp(7, new Timestamp(new Date().getTime()));
			ps.setTimestamp(8, new Timestamp(new Date().getTime()));
			ps.setTimestamp(9, new Timestamp(new Date().getTime()));

			ps.executeUpdate();
			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				long newId = rs.getLong(1);
				user.setId(newId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void unlockUser(User user) throws DBException {
		unlockUser(user.getId());
	}

	public void unlockUser(long userId) throws DBException {
		Connection conn = getConnection("unlockUser ID");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set unlocked=? where id=?");
			ps.setBoolean(1, true);
			ps.setLong(2, userId);

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void addItem(Item item) throws DBException {
		if (item.getId() > 0)
			updateItem(item);
		else {
			if (item.getExtId() > 0) {

				long id = getIDforExtId(item);
				if (id > 0) {
					item.setId(id);
					updateItem(item);
				} else {
					insertItem(item);
				}
			} else {
				insertItem(item);
			}
		}
	}

	private long getIDforExtId(Item item) throws DBException {
		long ret = 0;
		Connection conn = getConnection("getIDforExtId");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select id from items where lessonID=? and extID=?");
			ps.setLong(1, item.getLessonId());
			ps.setLong(2, item.getExtId());

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getLong("id");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public void updateItem(Item item) throws DBException {

		Connection conn = getConnection("updateItem");
		PreparedStatement ps = null;
		// ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update items set lessonID=?, text=?, comment=?, chapter=? where id=?");
			ps.setLong(1, item.getLessonId());
			ps.setString(2, item.getText());
			ps.setString(3, item.getComment());
			ps.setInt(4, item.getChapter());
			ps.setLong(5, item.getId());
			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

		addAnswers(item);

		// items.add(item);
	}

	public void insertItem(Item item) throws DBException {
		Connection conn = getConnection("insertItem");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("insert into items (lessonID,text,comment,extID,chapter) values (?,?,?,?,?)");
			ps.setLong(1, item.getLessonId());
			ps.setString(2, item.getText());
			ps.setString(3, item.getComment());
			ps.setLong(4, item.getExtId());
			ps.setInt(5, item.getChapter());

			ps.executeUpdate();
			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				long newId = rs.getLong(1);
				item.setId(newId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		addAnswers(item);

		// items.add(item);
	}

	// public void addAnswer(Answer answer) throws DBException {
	// Connection conn = getConnection("addAnswer");
	// PreparedStatement ps = null;
	// try {
	// ps = conn.prepareStatement("insert into items
	// (id,lessonID,text,answer,type) values (?,?,?,?,?)");
	// ps.setString(1, answer.getText());
	// ps.execute();
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// close(ps);
	// close(conn);
	// }
	//
	// // addAliasesOfAnswer(answer);
	//
	// // items.add(answer);
	// }

	private void addAnswers(Item item) throws DBException {
		Connection conn = getConnection("addAnswers");
		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement("delete from answers where itemID=?");
			ps.setLong(1, item.getId());
			ps.execute();
			close(ps);

			Iterator<Answer> it = item.getAnswers().iterator();
			while (it.hasNext()) {
				Answer answer = it.next();
				log.debug("Alias " + answer + "\n");
				ps = conn.prepareStatement("insert into answers (itemID,text,visible,type) values (?,?,?,?)");
				ps.setLong(1, item.getId());
				ps.setString(2, answer.getTextWithPhonetic());
				ps.setBoolean(3, answer.isVisible());
				ps.setInt(4, answer.getType());
				ps.execute();
				close(ps);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
	}

	public void addLesson(Lesson lesson) throws DBException {
		if (lesson.getId() > 0) {
			updateLesson(lesson);
		} else {
			insertLesson(lesson);
		}
	}

	public void insertLesson(Lesson lesson) throws DBException {
		Connection conn = getConnection("insertLesson");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("insert into lessons (title,description,abstract,type,keyboardLayout,icon,ownerID,ownergroupID,code,qlang,alang,public) values (?,?,?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, lesson.getTitle());
			ps.setString(2, lesson.getDescription());
			ps.setString(3, lesson.getAbstract());
			ps.setInt(4, lesson.getLessonType());

			if (lesson instanceof Lesson) {
				ps.setString(5, ((Lesson) lesson).getKeyboardLayout());
			} else {
				ps.setString(5, "");
			}

			ps.setString(6, lesson.getIcon());
			ps.setLong(7, lesson.getOwnerId());
			ps.setLong(8, lesson.getOwnergroupId());
			ps.setString(9, lesson.getCode());
			ps.setString(10, lesson.getQlang());
			ps.setString(11, lesson.getAlang());
			ps.setInt(12, lesson.getVisibility());

			ps.executeUpdate();
			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				long newId = rs.getLong(1);
				lesson.setId(newId);

				// Code erzeugen und speichern
				String code = createCode(newId, lesson.getOwnerId());
				lesson.setCode(code);
				close(ps);
				ps = conn.prepareStatement("update lessons set code=? where id=?");
				ps.setString(1, lesson.getCode());
				ps.setLong(2, newId);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		Iterator<Item> it = lesson.getItems().iterator();
		while (it.hasNext()) {
			Item item = it.next();
			item.setLessonId(lesson.getId());
			item.store();
		}

	}

	private String createCode(long newId, long ownerId) {
		String ret=""+newId+"-"+ownerId;
		ret = Base64.getEncoder().encodeToString(ret.getBytes()).toUpperCase();
		return ret;
	}

	public void updateLesson(Lesson lesson) throws DBException {
		Connection conn = getConnection("updateLesson");
		PreparedStatement ps = null;

		// title,description,abstract,type,keyboardLayout,icon,ownerID,ownergroupID,code,qlang,alang,public
		try {
			ps = conn.prepareStatement("update lessons set title=?,description=?,abstract=?,icon=?,type=?,keyboardLayout=?,code=?,qlang=?,alang=?,public=?,ownergroupid=? where id=?");
			ps.setString(1, lesson.getTitle());
			ps.setString(2, lesson.getDescription());
			ps.setString(3, lesson.getAbstract());
			ps.setString(4, lesson.getIcon());
			ps.setInt(5, lesson.getLessonType());

			if (lesson instanceof Lesson) {
				ps.setString(6, ((Lesson) lesson).getKeyboardLayout());
			} else {
				ps.setString(6, "");
			}

			ps.setString(7, lesson.getCode());
			ps.setString(8, lesson.getQlang());
			ps.setString(9, lesson.getAlang());
			ps.setInt(10, lesson.getVisibility());

			ps.setLong(11, lesson.getOwnergroupId());
			ps.setLong(12, lesson.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

	}

	public Vector<User> getUsers() throws DBException {
		Vector<User> ret = new Vector<User>();
		Connection conn = getConnection("getUsers");
		PreparedStatement ps = null;
		ResultSet rs = null;
		User user = null;
		try {
			ps = conn.prepareStatement("select * from users where status=0 order by lastlogin_date desc");

			rs = ps.executeQuery();

			while (rs.next()) {
				user = new User();
				user.initUser(rs);
				ret.add(user);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Lesson> getLessons() throws DBException {
		Vector<Lesson> ret = new Vector<Lesson>();
		Connection conn = getConnection("getLessons");
		PreparedStatement ps = null;
		ResultSet rs = null;
		Lesson lesson = null;
		try {
			// ps = conn.prepareStatement("SELECT l.*, count(*) as cx FROM
			// lessons l,
			// userlessons ul where ul.lessonID=l.id group by l.id");
			ps = conn.prepareStatement("SELECT l.id as lessonid, l.* FROM lessons l order by description");

			rs = ps.executeQuery();

			while (rs.next()) {
				lesson = new Lesson(rs);
				ret.add(lesson);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Lesson> getLessons(Topic topic) throws DBException {
		Vector<Lesson> ret = new Vector<Lesson>();
		Connection conn = getConnection("getLessons " + topic);
		PreparedStatement ps = null;
		ResultSet rs = null;
		Lesson lesson = null;
		try {
			// ps = conn.prepareStatement("SELECT l.*, count(*) as cx FROM
			// lessons l,
			// userlessons ul where ul.lessonID=l.id group by l.id");
			String sql = "SELECT l.id as lessonid, l.* FROM lessons l ";
			sql += "where public=1 and (topic1=? or topic2=?) ";
			sql += "order by description ";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, topic.getId());
			ps.setLong(2, topic.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				lesson = new Lesson(rs);
				ret.add(lesson);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Lesson> getLessonsByFilter(String filter) throws DBException {
		Vector<Lesson> ret = new Vector<Lesson>();
		Connection conn = getConnection("getLessonsByFilter");
		PreparedStatement ps = null;
		ResultSet rs = null;
		Lesson lesson = null;
		try {
			// ps = conn.prepareStatement("SELECT l.*, count(*) as cx FROM
			// lessons l,
			// userlessons ul where ul.lessonID=l.id group by l.id");

			String sql = "";
			sql += "SELECT distinct l.id as lessonid, l.* FROM lessons l, les_cat lc, categories c ";
			sql += "where l.id=lc.lessonID and lc.categoryID=c.id and public=1 and text=? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, filter);
			rs = ps.executeQuery();

			while (rs.next()) {
				lesson = new Lesson(rs);
				ret.add(lesson);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Lesson> getLessonsByCode(String code) throws DBException {
		Vector<Lesson> ret = new Vector<Lesson>();
		Connection conn = getConnection("getLessonsByCode");
		PreparedStatement ps = null;
		ResultSet rs = null;
		Lesson lesson = null;
		try {
			// ps = conn.prepareStatement("SELECT l.*, count(*) as cx FROM
			// lessons l,
			// userlessons ul where ul.lessonID=l.id group by l.id");

			String sql = "";
			sql += "SELECT l.id as lessonid, l.*,prefix FROM lessons l, users u ";
			sql += "where ownergroupID=0 and l.ownerID=u.id and code=? ";
			sql += "union ";
			sql += "SELECT l.id as lessonid, l.*,prefix FROM lessons l, groups u ";
			sql += "where ownergroupID>0 and l.ownergroupID=u.id and code=? and public=2 ";
			sql += "order by description";

			ps = conn.prepareStatement(sql);
			ps.setString(1, code);
			ps.setString(2, code);

			rs = ps.executeQuery();

			while (rs.next()) {
				ResultSetMetaData meta = rs.getMetaData();
				for (int i=1; i<=meta.getColumnCount(); i++) {
					String col = meta.getColumnLabel(i);
					String nam = meta.getColumnName(i);
					String a="";
				}
				lesson = new Lesson(rs);
				ret.add(lesson);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Lesson> getOwnerLessons(User user) throws DBException {
		Vector<Lesson> ret = new Vector<Lesson>();
		if (user == null)
			return ret;
		Connection conn = getConnection("getOwnerLessons");
		PreparedStatement ps = null;
		ResultSet rs = null;
		Lesson lesson = null;
		try {
			// ps = conn.prepareStatement("SELECT l.*, count(*) as cx FROM
			// lessons l,
			// userlessons ul where ul.lessonID=l.id group by l.id");
			String sql = "SELECT l.id as lessonid, l.* FROM lessons l ";
			sql += "where ownerID=? OR (ownergroupID=? and ownergroupID!=0)";
			sql += "order by description ";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, user.getId());
			ps.setLong(2, user.getGroupID());

			rs = ps.executeQuery();

			while (rs.next()) {
				lesson = new Lesson(rs);
				ret.add(lesson);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public UserLesson subscribeLesson(User user, Lesson lesson) throws DBException {
		return subscribeLessonExperimental(user, lesson);
	}

	// public void subscribeLessonSimple(User user, Lesson lesson) throws
	// DBException {
	//
	// log.debug("subscribe...");
	//
	// Vector<Long> items = new Vector<Long>();
	// Connection conn = getConnection("subscribeLesson");
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// try {
	// String sql = "select id from items i where ";
	// sql += "lessonID=?";
	// ps = conn.prepareStatement(sql);
	// ps.setLong(1, lesson.getId());
	//
	// rs = ps.executeQuery();
	// Long itemId = null;
	// while (rs.next()) {
	// itemId = new Long(rs.getLong("ID"));
	// items.add(itemId);
	// }
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// close(ps);
	// close(conn);
	// }
	//
	// conn = getConnection("subscribeLesson");
	// ps = null;
	//
	// UserLesson userLesson = addUserLesson(user, lesson);
	//
	// try {
	//
	// Iterator<Long> it = items.iterator();
	// int c = 0;
	// int max0 = userLesson.getMaxItemsLevel0();
	// java.sql.Timestamp now = new
	// java.sql.Timestamp(System.currentTimeMillis());
	// ps =
	// conn.prepareStatement("insert into useritems (userlessonID, itemID,
	// level, last, next, active) values (?,?,?,?,?,?)");
	// while (it.hasNext()) {
	// Long itemId = it.next();
	// // log.debug("subscribe.-." + item.getText());
	//
	// ps.setLong(1, userLesson.getId());
	// ps.setLong(2, itemId.longValue());
	// ps.setLong(3, 0);
	// ps.setTimestamp(4, now);
	// ps.setTimestamp(5, now);
	// ps.setInt(6, (c++ < max0) ? 1 : 0);
	// ps.execute();
	// }
	// close(ps);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// close(ps);
	// close(conn);
	// }
	//
	// return;
	// }

	public UserLesson subscribeLessonExperimental(User user, Lesson lesson) throws DBException {

		log.debug("subscribe Exp...");

		UserLesson userLesson = addUserLesson(user, lesson);
		activateUserItemExp(userLesson, 1); // 20

		return userLesson;
	}

	private UserLesson addUserLesson(User user, Lesson lesson) throws DBException {
		Connection conn = getConnection("addUserLesson");
		PreparedStatement ps = null;
		ResultSet rs = null;
		UserLesson ret = null;
		long newId = 0;
		try {
			ps = conn.prepareStatement("insert into userlessons (userID, lessonID) values (?,?)", Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, user.getId());
			ps.setLong(2, lesson.getId());
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				newId = rs.getLong(1);
				ret = getUserLesson(user, newId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		return ret;
	}

	/**
	 * In einer UserLesson können sich nur die Lernintervalle ändern. Diese
	 * werden hier gespeichert.
	 * 
	 * @param userLesson
	 * @return
	 * @throws DBException
	 */
	public void storeUserLesson(UserLesson userLesson) throws DBException {
		Connection conn = getConnection("storeUserLesson");
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("update userlessons set interval_unit=?, target_date=?, intervall_type=? where id=?");

			if (userLesson.isIntervallType() == false) {
				ps.setLong(1, 0);
				ps.setDate(2, null);
			} else {
				ps.setLong(1, userLesson.getIntervallUnit());
				ps.setDate(2, new java.sql.Date(userLesson.getRealTargetDate().getTime()));
			}
			ps.setShort(3, userLesson.isIntervallType() ? (short) 1 : (short) 0);
			ps.setLong(4, userLesson.getId());
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
	}

	public void unsubscribeLesson(User user, Lesson lesson) throws DBException {

		log.debug("unsubscribe..." + user + "-" + lesson);

		Connection conn = getConnection("unsubscribeLesson");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			String sql = "select id from userlessons where lessonID=? and userID=?";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, lesson.getId());
			ps.setLong(2, user.getId());

			rs = ps.executeQuery();
			long userLessonId = 0;
			if (rs.next()) {
				userLessonId = rs.getLong("id");

				log.debug("delete userlesson: " + userLessonId);

				close(rs);
				close(ps);

				ps = conn.prepareStatement("delete from userlessons where id=?");
				ps.setLong(1, userLessonId);
				ps.execute();
			}
		} catch (SQLException e) {
			log.debug("oops" + e);
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		log.debug("unsubscribe...done.");

		return;
	}

	public void unsubscribeLesson(User user, UserLesson lesson) throws DBException {

		log.debug("unsubscribe..." + user + "-" + lesson);

		Connection conn = getConnection("unsubscribeLesson");
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("delete from userlessons where id=?");
			ps.setLong(1, lesson.getId());
			ps.execute();
		} catch (SQLException e) {
			log.debug("oops" + e);
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
		log.debug("unsubscribe...done.");

		return;
	}

	public void deleteItem(Item item) throws DBException {

		log.debug("delete..." + item);

		Connection conn = getConnection("deleteItem");
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("delete from items where id=?");
			ps.setLong(1, item.getId());
			ps.execute();
		} catch (SQLException e) {
			log.debug("oops" + e);
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
		log.debug("delete...done.");

		return;
	}

	DataSource ds = null;

	public Connection getConnection(String caller) throws DBException {
		log.debug("Caller: " + caller);
		return getConnection();
	}

	public Connection getConnection() throws DBException {
		return getConnectionPool();
	}

	public Connection getConnectionPool() throws DBException {
		Connection ret = null;
		try {
			ret = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			log.debug("EXC", e);
			log.fatal(e.getMessage());
			throw new DBException("No Connection available.");
		}

		return ret;
	}

	// public Connection getConnectionDirect() {
	// Connection ret = null;
	// try {
	// if (dbcs.equals(""))
	// dbcs = "jdbc:mysql://localhost:3306/";
	// String cstr = dbcs + dbname + "?user=" + user + "&password=" + pass;
	// // String cstr=dbcs+"?user="+user+"&password="+pass;
	// log.debug("DB: " + cstr);
	// // log.debug("DB: " + dbcs);
	// // ret = DriverManager.getConnection(dbcs + dbname, user, pass);
	// ret = DriverManager.getConnection(cstr);
	// // ret = DriverManager.getConnection(dbcs, user, pass);
	// // ret.setCatalog(dbname);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// return ret;
	// }

	static public void close(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
		}
	}

	static public void close(Statement st) {
		try {
			if (st != null)
				st.close();
		} catch (Exception e) {
		}
	}

	static public void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
		}
	}

	public Vector<UserLesson> getUserLessons(User user) throws DBException {
		Vector<UserLesson> ret = new Vector<UserLesson>();

		Connection conn = getConnection("getUserLessons");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select *, ul.id as ulid, l.id as lessonid, l.id as lid from userlessons ul, lessons l ";
			sql += "where userID=? and ul.lessonID=l.id and parentID=0 order by description";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, user.getId());

			rs = ps.executeQuery();
			UserLesson ul = null;
			while (rs.next()) {

				Lesson lesson = new Lesson(rs);

				ul = new UserLesson(rs, lesson);
				ret.add(ul);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

		return ret;
	}

	public Vector<UserLesson> getUserLessons(Lesson lesson) throws DBException {
		Vector<UserLesson> ret = new Vector<UserLesson>();

		Connection conn = getConnection("getUserLessons");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select *, ul.id as ulid, l.id as lid from userlessons ul, lessons l ";
			sql += "where lessonID=? and ul.lessonID=l.id and parentID=0";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, lesson.getId());

			rs = ps.executeQuery();
			UserLesson ul = null;
			while (rs.next()) {
				ul = new UserLesson(rs, lesson);
				ret.add(ul);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

		return ret;
	}

	public Lesson getLesson(long lessonId) throws DBException {
		Lesson ret = null;
		Connection conn = getConnection("getLesson");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select l.id as lessonid, l.* from lessons l where id=?");
			ps.setLong(1, lessonId);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new Lesson(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Item getItem(long itemId) throws DBException {
		Item ret = null;
		Connection conn = getConnection("getItem");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from items where id=?");
			ps.setLong(1, itemId);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new Item(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Category> getCategories() throws DBException {
		Vector<Category> ret = new Vector<Category>();
		Connection conn = getConnection("getCategories");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("SELECT c.*, count(*) cx FROM categories c, les_cat lc where lc.categoryID=c.id group by c.id, c.`text`, c.locale order by text");

			rs = ps.executeQuery();

			while (rs.next()) {
				Category cat = new Category(rs);
				ret.add(cat);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Category> getCategories(Lesson lesson) throws DBException {
		Vector<Category> ret = new Vector<Category>();
		Connection conn = getConnection("getCategories2");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select c.* from categories c, les_cat lc where lc.lessonID=? and lc.categoryID=c.id order by text");
			ps.setLong(1, lesson.getId());

			rs = ps.executeQuery();

			while (rs.next()) {
				Category cat = new Category(rs);
				ret.add(cat);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Item> getItems(Lesson lesson) throws DBException {
		Vector<Item> ret = new Vector<Item>();
		Connection conn = getConnection("getItems1");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from items where lessonID=? order by text");
			ps.setLong(1, lesson.getId());

			rs = ps.executeQuery();

			while (rs.next()) {
				Item item = new Item(rs);
				ret.add(item);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public int getItemCount(Lesson lesson) throws DBException {
		int ret = 0;
		Connection conn = getConnection("getItemsCount");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select count(*) as cx from items where lessonID=?");
			ps.setLong(1, lesson.getId());

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("cx");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public int getSubscriberCount(Lesson lesson) throws DBException {
		int ret = 0;
		Connection conn = getConnection("getSubscriberCount");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select count(*) as cx from userlessons where lessonID=?");
			ps.setLong(1, lesson.getId());

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("cx");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public int getHighestChapter(Lesson lesson) throws DBException {
		int ret = 0;
		Connection conn = getConnection("getHighestChapter");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select max(chapter) as mc from items where lessonID=?");
			ps.setLong(1, lesson.getId());

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("mc");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<Item> getItems(Lesson lesson, String filter) throws DBException {
		Vector<Item> ret = new Vector<Item>();
		Connection conn = getConnection("getItems2");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from items where lessonID=? and text like ? order by text");
			ps.setLong(1, lesson.getId());
			ps.setString(2, "%" + filter + "%");

			rs = ps.executeQuery();

			while (rs.next()) {
				Item item = new Item(rs);
				ret.add(item);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public UserItem getNextUserItem(UserLesson userLesson) throws DBException {
		long userLessonId = userLesson.getId();
		return getNextUserItem(userLessonId);
	}

	public UserItem getNextUserItem(long userLessonId) throws DBException {

		UserItem ret = null;
		Connection conn = getConnection("getNextUserItem");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from useritems where userlessonID=? and next<=? and active=1 ORDER BY RAND()*(level+1) LIMIT 1");
			ps.setLong(1, userLessonId);
			ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new UserItem(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		return ret;
	}

	public UserItem getNextUserItem(User user, long userLessonId) throws DBException {

		UserItem ret = null;
		Connection conn = getConnection("getNextUserItem");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			String sql = "select ui.* from useritems ui, userlessons ul, users u ";
			sql += "where ";
			sql += "ui.userlessonID=ul.id and ";
			sql += "ul.userid=u.id and ";
			sql += "u.id=? and ";
			sql += "userlessonID=? and next<=? and active=1 ORDER BY RAND()*(level+1) LIMIT 1 ";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, user.getId());
			ps.setLong(2, userLessonId);
			ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new UserItem(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		return ret;
	}

	public Date getNextUserItemTime(long userLessonId) throws DBException {

		Date ret = null;
		Timestamp ts = null;
		long now = System.currentTimeMillis();

		Connection conn = getConnection("getNextUserItemTime");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select next from useritems where userlessonID=? and active=1 order by next LIMIT 1");
			ps.setLong(1, userLessonId);

			rs = ps.executeQuery();

			log.debug("now " + now);

			if (rs.next()) {
				ts = rs.getTimestamp("next");
				log.debug("next " + ts);
				ret = new Date(ts.getTime());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		return ret;
	}

	public long getNextUserItemTimeDiff(long userLessonId) throws DBException {

		long ret = 0;
		long now = System.currentTimeMillis();

		Connection conn = getConnection("getNextUserItemTimeDiff");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select next from useritems where userlessonID=? and active=1 order by next LIMIT 1");
			ps.setLong(1, userLessonId);

			rs = ps.executeQuery();

			log.debug("now " + now);

			if (rs.next()) {
				ret = rs.getTimestamp("next").getTime();
				// log.debug("next " + ms);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		return ret;
	}

	public UserItem getUserItem(long userItemId) throws DBException {

		UserItem ret = null;
		Connection conn = getConnection("getNextUserItem");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from useritems where ID=? and active=1 and next<? LIMIT 1");
			ps.setLong(1, userItemId);
			ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new UserItem(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		return ret;
	}

	public UserItem getUserItem(User user, long userItemId) throws DBException {

		UserItem ret = null;
		Connection conn = getConnection("getNextUserItem");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "select ui.* from useritems ui, userlessons ul, users u ";
			sql += "where ";
			sql += "ui.userlessonID=ul.id and ";
			sql += "ul.userID=u.id and ";
			sql += "u.id=? and ";
			sql += "ui.ID=? and active=1 and next<? LIMIT 1 ";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, user.getId());
			ps.setLong(2, userItemId);
			ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new UserItem(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		return ret;
	}

	public void update(UserItem useritem) throws DBException {
		Connection conn = getConnection("update");
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update useritems set level=?, last=?, next=?, wright=?, wrong=? where id=?");
			ps.setLong(1, useritem.getLevel());
			ps.setTimestamp(2, new java.sql.Timestamp(useritem.getLast().getTime()));
			ps.setTimestamp(3, new java.sql.Timestamp(useritem.getNext().getTime()));
			ps.setLong(4, useritem.getWright());
			ps.setLong(5, useritem.getWrong());
			ps.setLong(6, useritem.getId());
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
	}

	public int getUserAvailable(User user) throws DBException {
		int ret = 0;
		Connection conn = getConnection("getUserAvailable");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "select count(*) as cx from useritems ui, userlessons ul ";
			sql += "where userlessonID=ul.id and ul.userID=? and next<? and ui.active=1";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, user.getId());
			ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("cx");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public int getUserScore(User user) throws DBException {
		int ret = 0;
		Connection conn = getConnection("getUserScore");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "select SUM(level) as sl from userlessons ul, useritems ui ";
			sql += "where ul.userID=? and ul.id=ui.userlessonID and level>0";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, user.getId());

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("sl");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public int getUserLessonScore(UserLesson userLesson, Connection conn) {
		int ret = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "";
			sql = "select SUM(level) as sl from useritems ui ";
			sql += "where ui.userlessonID=? and ui.active=1";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, userLesson.getId());

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("sl");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
		}
		return ret;
	}

	public int getUserLessonAvailable(UserLesson userLesson) throws DBException {
		int ret = 0;
		Connection conn = getConnection("getUserLessonAvail1");
		try {
			ret = getUserLessonAvailable(userLesson, conn);
		} finally {
			close(conn);
		}
		return ret;
	}

	public int getUserLessonAvailable(UserLesson userLesson, Connection conn) {
		int ret = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "";

			sql = "select count(*) as cx from useritems ui, userlessons ul ";
			sql += "where ui.userlessonID=ul.id and ul.id=? and next<? and ui.active=1";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, userLesson.getId());
			ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("cx");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
		}
		return ret;
	}

	/**
	 * direkter Aufruf der offenen Fragen einer UserLesson für die API
	 */
	public int getUserLessonAvailable(long userLessonId) {
		int ret = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;

		Connection conn = null;

		try {
			conn = getConnection("getUserLessonAvail2");
			String sql = "";

			sql = "select count(*) as cx from useritems ui, userlessons ul ";
			sql += "where ui.userlessonID=ul.id and ul.id=? and next<? and ui.active=1";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, userLessonId);
			ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("cx");
			}

		} catch (SQLException e) {
			log.error(e.toString());
		} catch (DBException e) {
			log.error(e.toString());
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public UserLesson getUserLesson(User user, long userLessonId) throws DBException {
		UserLesson ret = null;
		Connection conn = getConnection("getUserLesson");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select ul.id as ulid, userID, lessonID, combined, interval_unit, target_date, intervall_type, l.id as lessonid, l.* from userlessons ul, lessons l where ul.id=? and userID=? and ul.lessonID=l.id");

			ps.setLong(1, userLessonId);
			ps.setLong(2, user.getId());

			rs = ps.executeQuery();

			// TODO Lesson ergänzen
			if (rs.next()) {
				ret = new UserLesson(rs, new Lesson(rs));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public UserLesson getUserLessonByLessonID(User user, long lid) throws DBException {
		UserLesson ret = null;
		Connection conn = getConnection("getUserLesson");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select id as ulid, userID, lessonID, combined, interval_unit, target_date, intervall_type from userlessons where lessonID=? and userID=?");
			ps.setLong(1, lid);
			ps.setLong(2, user.getId());

			rs = ps.executeQuery();

			Lesson lesson = getLesson(lid);

			if (rs.next()) {
				ret = new UserLesson(rs, lesson);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public User getUser(long userId) throws DBException {
		User ret = null;
		Connection conn = getConnection("getUser");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from users where id=? and status=0");
			ps.setLong(1, userId);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new User();
				ret.initUser(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public void loadUser(User user, String name) throws DBException {
		Connection conn = getConnection("getUser");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from users where (name=? or nick=?) and status=0");
			ps.setString(1, name);
			ps.setString(2, name);

			rs = ps.executeQuery();

			if (rs.next()) {
				user.initUser(rs);
			}

		} catch (SQLException e) {
			log.error("SQL Error.", e);
		} catch (Exception e) {
			log.error("General Error.", e);
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void getUserLessonLevels(UserLesson parentUserLesson) throws DBException {
		Connection conn = getConnection("getUserLessonLevels");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			UserStats userScore = parentUserLesson.getUserStats();
			int[] levelList = userScore.getLevelList();

			String sql = "";
			sql = "select count(*) as cx,level from useritems ";
			sql += "where userlessonID in " + parentUserLesson.getIdList() + " and active=1 group by level";
			ps = conn.prepareStatement(sql);
			// ps.setLong(1, userLesson.getId());

			rs = ps.executeQuery();
			while (rs.next()) {
				int level = rs.getInt("level");
				levelList[level < 5 ? level : 5] += rs.getInt("cx");
			}

			userScore.setAvailable(getUserLessonAvailable(parentUserLesson, conn));
			userScore.setScore(getUserLessonScore(parentUserLesson, conn));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

	}

	public String getLessonLevels(Lesson lesson) throws DBException {
		StringBuffer ret = new StringBuffer(20);
		Connection conn = getConnection("getUserLessonLevels");
		PreparedStatement ps = null;
		ResultSet rs = null;
		int[] levelList = { 0, 0, 0, 0, 0, 0 };
		try {

			String sql = "";
			sql = "select count(*) as cx,level from useritems ui, userlessons ul ";
			sql += "where lessonID=? and ul.id=ui.userlessonid and active=1 group by level";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, lesson.getId());

			rs = ps.executeQuery();

			while (rs.next()) {
				int level = rs.getInt("level");
				levelList[level < 5 ? level : 5] += rs.getInt("cx");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

		for (int i = 0; i < levelList.length; i++) {
			ret.append(levelList[i]);
			if (i < 5)
				ret.append("-");
		}

		return ret.toString();
	}

	// public int getUserLevel(User user, int level) throws DBException {
	// int ret = 0;
	// Connection conn = getConnection("getUserLevel");
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	//
	// try {
	// String sql = "select count(*) as cx from useritems ui, userlessons ul ";
	// if (level > 4)
	// sql +=
	// "where ui.userlessonID=ul.id and ul.userID=? and level>? and active=1";
	// else
	// sql +=
	// "where ui.userlessonID=ul.id and ul.userID=? and level=? and active=1";
	// ps = conn.prepareStatement(sql);
	// ps.setLong(1, user.getId());
	// ps.setInt(2, level>4?4:level);
	//
	// rs = ps.executeQuery();
	//
	// if (rs.next()) {
	// ret = rs.getInt("cx");
	// }
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// close(rs);
	// close(ps);
	// close(conn);
	// }
	// return ret;
	// }

	public void deactivateUserItem(UserItem userItem) throws DBException {
		Connection conn = getConnection("deactivateUserItem");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("update useritems set active=2 where id=?");
			ps.setLong(1, userItem.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void activateUserItemExp(UserLesson userLesson) throws DBException {
		activateUserItemExp(userLesson, 1);
	}

	public void activateUserItemExp(UserLesson userLesson, int numItems) throws DBException {

		log.debug("activate Exp...");

		Connection conn = getConnection("activateExp");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT i.* FROM items i ";
			sql += "LEFT OUTER JOIN useritems ui ON i.id=ui.itemid and ui.userlessonid=? ";
			sql += "where i.lessonid=? and ui.id is null order by chapter, rand() limit ?";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, userLesson.getId());
			ps.setLong(2, userLesson.getLesson().getId());
			ps.setInt(3, numItems);

			rs = ps.executeQuery();

			while (rs.next()) {
				long itemId = rs.getLong("ID");
				addUserItem(userLesson.getId(), itemId, conn);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
	}

	// public void addUserItem(long ulid, long iid) {
	// Connection conn=null;
	// try {
	// conn = getConnection("activateExp2");
	// addUserItem(ulid, iid, conn);
	// } catch (DBException e) {
	// e.printStackTrace();
	// } finally {
	// close(conn);
	// }
	// }

	public void addUserItem(long ulid, long iid, Connection conn) throws DBException {
		PreparedStatement ps = null;

		try {

			java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis() - 5000); // racecondition
			// daher
			// -5
			// sec

			ps = conn.prepareStatement("insert into useritems (userlessonID, itemID, level, last, next, active) values (?,?,?,?,?,?)");
			ps.setLong(1, ulid);
			ps.setLong(2, iid);
			ps.setLong(3, 0);
			ps.setTimestamp(4, now);
			ps.setTimestamp(5, now);
			ps.setInt(6, 1);
			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
		}
	}

	// public void activateUserItem(UserLesson userLesson) throws DBException {
	//
	// Connection conn = getConnection("activateUserItem");
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	//
	// try {
	//
	// ps =
	// conn.prepareStatement("select id from useritems where userlessonID in " +
	// userLesson.getIdList() + " and active=0 order by RAND() LIMIT 1");
	// // ps.setLong(1, userLesson.getId());
	//
	// rs = ps.executeQuery();
	//
	// if (rs.next()) {
	// long userItemId = rs.getLong("id");
	// close(rs);
	// close(ps);
	// ps = conn.prepareStatement("update useritems set active=1 where id=?");
	// ps.setLong(1, userItemId);
	// ps.executeUpdate();
	// }
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// close(rs);
	// close(ps);
	// close(conn);
	// }
	//
	// }

	public Vector<Answer> getAnswers(long itemId) throws DBException {
		Vector<Answer> ret = new Vector<Answer>();
		Connection conn = getConnection("getAnswers");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from answers where itemID=?");
			ps.setLong(1, itemId);

			rs = ps.executeQuery();

			log.debug("load answers");

			while (rs.next()) {
				Answer answer = new Answer(rs);
				log.debug("answers found:" + answer.getText());
				ret.add(answer);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public void storeUserScore(User user, int score) throws DBException {
		Connection conn = getConnection("storeUserScore");
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("update stats set score=? where userID=? and logindate=?");
			ps.setLong(1, score);
			ps.setLong(2, user.getId());
			ps.setDate(3, new java.sql.Date(System.currentTimeMillis()));
			ps.execute();
			int uc = ps.getUpdateCount();
			log.debug("UC: " + uc);
			close(ps);

			if (uc == 0) {
				// if (uc < 50) {
				ps = conn.prepareStatement("insert into stats (userID, logindate, score) values (?,?,?)");
				ps.setLong(1, user.getId());
				ps.setDate(2, new java.sql.Date(System.currentTimeMillis()));
				ps.setLong(3, score);
				ps.execute();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

	}

	// public void storeUserScore(User user, int score) throws DBException {
	// Connection conn = getConnection("storeUserScore");
	// PreparedStatement ps = null;
	//
	// try {
	// ps = conn.prepareStatement("update stats set score=?, score1=?, score2=?,
	// score3=?, score4=?,score5=? where userID=? and logindate=?");
	// ps.setLong(1, score);
	// ps.setLong(2, getUserLevel(user, 0));
	// ps.setLong(3, getUserLevel(user, 1));
	// ps.setLong(4, getUserLevel(user, 2));
	// ps.setLong(5, getUserLevel(user, 3));
	// ps.setLong(6, getUserLevel(user, 4));
	// ps.setLong(7, user.getId());
	// ps.setDate(8, new java.sql.Date(System.currentTimeMillis()));
	// ps.execute();
	// int uc = ps.getUpdateCount();
	// log.debug("UC: " + uc);
	// close(ps);
	//
	// if (uc == 0) {
	// // if (uc < 50) {
	// ps = conn.prepareStatement("insert into stats (userID, logindate, score,
	// score1, score2, score3, score4, score5) values (?,?,?,?,?,?,?,?)");
	// ps.setLong(1, user.getId());
	// ps.setDate(2, new java.sql.Date(System.currentTimeMillis()));
	// ps.setLong(3, score);
	// ps.setLong(4, getUserLevel(user, 0));
	// ps.setLong(5, getUserLevel(user, 1));
	// ps.setLong(6, getUserLevel(user, 2));
	// ps.setLong(7, getUserLevel(user, 3));
	// ps.setLong(8, getUserLevel(user, 4));
	// ps.execute();
	// }
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// close(ps);
	// close(conn);
	// }
	//
	// }

	public boolean emailIsAlreadyUsed(String email) throws DBException {
		boolean ret = false;
		Connection conn = getConnection("getAnswers");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from users where name=? and status=0");
			ps.setString(1, email);

			rs = ps.executeQuery();

			ret = rs.next();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public boolean unlockUser(String emailAddress, String unlock) throws DBException {
		boolean ret = false;
		Connection conn = getConnection("unlockUser email");
		PreparedStatement ps = null;
		ResultSet rs = null;

		log.debug("unlock:" + emailAddress);

		try {
			ps = conn.prepareStatement("select * from users where name=? and status=0");
			ps.setString(1, emailAddress);

			rs = ps.executeQuery();

			if (rs.next()) {
				String code = DBUtil.getString(rs, "unlocktext");
				if (code.equals(unlock)) {
					long userId = DBUtil.getLong(rs, "id");
					unlockUser(userId);
					log.debug("Unlocked user: " + userId + "(" + unlock + ")");
					ret = true;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public void storeComment(UserItem userItem, String comment, String commentType) throws DBException {
		Connection conn = getConnection("storeComment");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("insert into comments (text,type,userID,itemID) values (?,?,?,?)");
			ps.setString(1, comment);
			ps.setString(2, commentType);
			ps.setLong(3, userItem.getUserLesson().getUser().getId());
			ps.setLong(4, userItem.getItem().getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void storeLastLogin(User user) throws DBException {
		Connection conn = getConnection("storeLastLogin");
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("update users set lastlogin_date=? where ID=?");
			ps.setTimestamp(1, new Timestamp(user.getLastLoginDate().getTime()));
			ps.setLong(2, user.getId());
			ps.execute();
			close(ps);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

	}

	public void deleteLesson(Lesson lesson) throws DBException {

		// TODO: delete useritems as well

		log.debug("delete..." + lesson);

		Connection conn = getConnection("deleteLesson");
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("delete from lessons where ID=?");
			ps.setLong(1, lesson.getId());
			ps.execute();

		} catch (SQLException e) {
			log.debug("oops" + e);
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
		log.debug("delete...done.");

	}

	public int getCategoryItemCount(Category category) throws DBException {
		int ret = -1;
		Connection conn = getConnection("getCategoryItemCount");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select count(distinct i.id) cx from items i, les_cat lc where lc.categoryID=? and lc.lessonID=i.lessonID");
			ps.setLong(1, category.getID());

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("cx");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public void storeCategories(Lesson lesson, String tags, String locale) throws DBException {
		Connection conn = getConnection("storeCategories");
		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement("delete les_cat.* from les_cat, categories c where lessonID=? and categoryID = c.id and c.locale=?");
			ps.setLong(1, lesson.getId());
			ps.setString(2, locale);
			ps.execute();
			close(ps);

			StringTokenizer st = new StringTokenizer(tags, " ");

			while (st.hasMoreTokens()) {
				String cat = st.nextToken();
				log.debug("Cat " + cat + "\n");

				long catID = getCategoryID(conn, cat, locale);

				ps = conn.prepareStatement("insert into les_cat (lessonID,categoryID) values (?,?)");
				ps.setLong(1, lesson.getId());
				ps.setLong(2, catID);
				ps.execute();
				close(ps);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
	}

	private long getCategoryID(Connection conn, String cat, String locale) {
		long ret = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = conn.prepareStatement("select id from categories where text=? and locale=?");
			ps.setString(1, cat);
			ps.setString(2, locale);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getLong("id");
			} else {
				close(rs);
				close(ps);

				ps = conn.prepareStatement("insert into categories (text, locale) values (?,?)", Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, cat);
				ps.setString(2, locale);
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();

				if (rs.next()) {
					ret = rs.getLong(1);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
		}

		return ret;
	}

	public String getUserPassword(String email) {
		String ret = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getConnection("getUserPassword");
			ps = conn.prepareStatement("select * from users where name=? and status=0");
			ps.setString(1, email);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getString("password");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DBException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;

	}

	public void changePassword(User cu, String passnew) throws DBException {
		Connection conn = getConnection("change password");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set password=? where id=?");
			ps.setString(1, passnew);
			ps.setLong(2, cu.getId());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public Vector<News> getNews(Locale locale) throws DBException {
		Vector<News> ret = new Vector<News>();
		Connection conn = getConnection("getNews");
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT p.ID ";
		sql += "FROM wordpress.wp_posts p, wordpress.wp_term_relationships tr ";
		sql += "where p.post_type='post' ";
		sql += "and p.post_status='publish' ";
		sql += "and tr.object_id=p.id ";
		sql += "and tr.term_taxonomy_id=? ";
		sql += "order by post_date desc LIMIT 4";

		try {
			if (locale != null) {
				int localeCategoryID = locale.getLanguage().equals("de") ? 3 : 4;
				ps = conn.prepareStatement(sql);
				ps.setInt(1, localeCategoryID);
			} else {
				ps = conn.prepareStatement(sql);
				ps.setInt(1, 3);
			}
			rs = ps.executeQuery();

			while (rs.next()) {
				News news = readNews(rs.getInt("ID"), conn);
				if (news != null)
					ret.add(news);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	private News readNews(int newsID, Connection conn) {
		News ret = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT p.id, post_date, post_content, post_title, t.name as tn, t.slug as slug FROM wordpress.wp_posts p, wordpress.wp_term_relationships tr, wordpress.wp_term_taxonomy tt, wordpress.wp_terms t ";
			sql += "where p.ID=? ";
			sql += "and tr.object_id=p.id ";
			sql += "and tt.taxonomy='post_tag' ";
			sql += "and tr.term_taxonomy_id=tt.term_taxonomy_id ";
			sql += "and tt.term_id=t.term_id ";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, newsID);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new News(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
		}
		return ret;
	}

	public int getUserAvailable(long userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addNews(News news) throws DBException {
		if (news.getId() >= 0)
			updateNews(news);
		else
			insertNews(news);
	}

	public void updateNews(News news) throws DBException {
		Connection conn = getConnection("updateNews");
		PreparedStatement ps = null;
		// ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update news set title=?, text=?, type=?, locale=?, date=? where id=?");
			ps.setString(1, news.getTitle());
			ps.setString(2, news.getText());
			ps.setInt(3, news.getType());
			ps.setString(4, news.getLocale());
			ps.setDate(5, new java.sql.Date(news.getDate().getTime()));
			ps.setLong(6, news.getId());
			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

	}

	public void insertNews(News news) throws DBException {
		Connection conn = getConnection("insertNews");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("insert into news (title,text,type,locale,date) values (?,?,?,?,?)");
			ps.setString(1, news.getTitle());
			ps.setString(2, news.getText());
			ps.setInt(3, news.getType());
			ps.setString(4, news.getLocale());
			ps.setDate(5, new java.sql.Date(news.getDate().getTime()));

			ps.executeUpdate();
			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				long newId = rs.getLong(1);
				news.setId(newId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

	}

	public String getConfigParameter(UserLesson ul, String name) throws DBException {
		String ret = null;
		Connection conn = getConnection("getConfigParameter");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from lessonconfig where userlessonID=? and name=?");
			ps.setLong(1, ul.getId());
			ps.setString(2, name);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = DBUtil.getString(rs, "value");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;

	}

	public void setConfigParameter(UserLesson ul, String name, String value) throws DBException {
		if (updateConfigParameter(ul, name, value) == 0)
			insertConfigParameter(ul, name, value);
	}

	int updateConfigParameter(UserLesson ul, String name, String value) throws DBException {
		Connection conn = getConnection("setConfigParameter");
		PreparedStatement ps = null;
		ResultSet rs = null;
		int ret = 0;
		try {
			ps = conn.prepareStatement("update lessonconfig set value=? where userlessonID=? and name=?");
			ps.setString(1, value);
			ps.setLong(2, ul.getId());
			ps.setString(3, name);
			ret = ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	void insertConfigParameter(UserLesson ul, String name, String value) throws DBException {
		Connection conn = getConnection("setConfigParameter");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("insert into lessonconfig (userlessonID,name,value) values (?,?,?)");
			ps.setLong(1, ul.getId());
			ps.setString(2, name);
			ps.setString(3, value);
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public Vector<UserScore> getTop5() {
		Vector<UserScore> ret = new Vector<UserScore>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getConnection("getUserScore");
			String sql = "select u.name as name, u.nick as nick, sum(level) as score from users u, userlessons ul, useritems ui ";
			sql += "where u.id=ul.userid and ui.userlessonid=ul.id and ui.last>now() - interval 14 month ";
			sql += "group by u.name order by score desc limit 10";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				String nick = DBUtil.getString(rs, "nick");
				UserScore us = new UserScore(DBUtil.getString(rs, "name"), nick, DBUtil.getInt(rs, "score"));
				ret.add(us);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DBException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Vector<UserScore> getLessonTop5(Lesson lesson) {
		Vector<UserScore> ret = new Vector<UserScore>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getConnection("getUserScore");
			String sql = "select u.name as name, u.nick as nick, sum(level) as score from users u, userlessons ul, useritems ui, lessons l ";
			sql += "where u.id=ul.userid and ui.userlessonid=ul.id and ul.lessonID=l.id and l.id=? and ui.last>now() - interval 14 month ";
			sql += "group by u.name order by score desc limit 10";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, lesson.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				String nick = DBUtil.getString(rs, "nick");
				UserScore us = new UserScore(DBUtil.getString(rs, "name"), nick, DBUtil.getInt(rs, "score"));
				ret.add(us);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DBException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public void changeNickname(User cu, String nick) throws DBException {
		Connection conn = getConnection("change nick");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set nick=? where id=?");
			ps.setString(1, nick);
			ps.setLong(2, cu.getId());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void changeName(User cu, String name) throws DBException {
		Connection conn = getConnection("change name");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set name=?, unlocked=0 where id=?");
			ps.setString(1, name);
			ps.setLong(2, cu.getId());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void changePrefix(User cu, String prefix) throws DBException {
		Connection conn = getConnection("change prefix");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set prefix=? where id=?");
			ps.setString(1, prefix);
			ps.setLong(2, cu.getId());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public boolean checkNickname(User cu, String nick) throws DBException {
		Connection conn = getConnection("check nick");
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean ret = false;
		try {
			ps = conn.prepareStatement("select count(*) as cx from users where nick=?");
			ps.setString(1, nick);
			// ps.setLong(2, cu.getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				int cx = rs.getInt("cx");
				ret = (cx == 0);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public boolean checkName(User cu, String name) throws DBException {
		Connection conn = getConnection("check name");
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean ret = false;
		try {
			ps = conn.prepareStatement("select count(*) as cx from users where name=?");
			ps.setString(1, name);
			// ps.setLong(2, cu.getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				int cx = rs.getInt("cx");
				ret = (cx == 0);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	
	public boolean checkUserPrefix(User cu, String prefix) throws DBException {
		Connection conn = getConnection("check prefix");
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean ret = false;
		try {
			ps = conn.prepareStatement("select count(*) as cx from users where prefix=?");
			ps.setString(1, prefix);
			// ps.setLong(2, cu.getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				int cx = rs.getInt("cx");
				ret = (cx == 0);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public Topic getTopicTree(String lang) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Topic ret = null;
		try {
			conn = getConnection("getTopicTree");

			String sql = "select * from topics t, topictext tt ";
			sql += "where parentID=0 ";
			sql += "and t.id=tt.topicID ";
			sql += "and tt.lang=? ";
			sql += "limit 1 ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, lang);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new Topic(rs);
				loadSubTopics(conn, ret, lang);
			}

		} catch (DBException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	private void loadSubTopics(Connection conn, Topic topic, String lang) {

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "select * from topics t, topictext tt ";
			sql += "where parentID=? ";
			sql += "and t.id=tt.topicID ";
			sql += "and tt.lang=? ";

			ps = conn.prepareStatement(sql);
			ps.setLong(1, topic.getId());
			ps.setString(2, lang);
			rs = ps.executeQuery();

			while (rs.next()) {
				Topic subTopic = new Topic(rs);
				topic.addSubTopic(subTopic);
				loadSubTopics(conn, subTopic, lang);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
		}
		return;
	}

	public Group getGroup(long groupId) throws DBException {
		Group ret = null;
		Connection conn = getConnection("getGroup");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from groups where id=?");
			ps.setLong(1, groupId);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new Group(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

	public void deleteAccount(User user) throws DBException {
		Connection conn = getConnection("delete user");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set name=?, nick=?, password=?, status=1, facebookID=0 where id=?");
			ps.setString(1, "#DEL_" + user.getName());
			ps.setString(2, "#DEL_" + user.getNick());
			ps.setString(3, "#DEL_" + user.getPassword());
			ps.setLong(4, user.getId());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void changeStatusMailFreq(User user) throws DBException {
		Connection conn = getConnection("change email freq");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set newsletter=?, status_lang=? where id=?");
			ps.setInt(1, user.getStatusMailFreq());
			ps.setString(2, user.getStatusLang());
			ps.setLong(3, user.getId());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void fb_linkUser(User user, long facebookID) throws DBException {
		Connection conn = getConnection("fb link user");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("update users set facebookID=? where id=?");
			ps.setLong(1, facebookID);
			ps.setLong(2, user.getId());

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

	public void resetUserLesson(UserLesson userLesson) throws DBException {

		Connection conn = getConnection("resetUserLesson");
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("delete from useritems where userlessonID=?");
			ps.setLong(1, userLesson.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}

		activateUserItemExp(userLesson, 1); // 20

	}

	@Override
	public User getUser(String name, String password) throws DBException {
		User ret = null;
		Connection conn = getConnection("getUser");
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement("select * from users where (name=? or nick=?) and password=? and status=0");
			ps.setString(1, name);
			ps.setString(2, name);
			ps.setString(3, password);

			rs = ps.executeQuery();

			if (rs.next()) {
				ret = new User();
				ret.initUser(rs);
			}

		} catch (SQLException e) {
			log.error("SQL Error.", e);
		} catch (Exception e) {
			log.error("General Error.", e);
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
		return ret;
	}

}