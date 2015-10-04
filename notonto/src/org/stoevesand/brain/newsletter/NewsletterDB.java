package org.stoevesand.brain.newsletter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import org.jboss.logging.Logger;
import org.stoevesand.brain.exceptions.DBException;

public class NewsletterDB {

	private static Logger log = Logger.getLogger(NewsletterDB.class);
	private String db_user;
	private String db_pass;
	private String db_server;

	public NewsletterDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			log.error("Can't find JDBC Driver");
		}
		db_user = System.getProperty("db_user");
		db_pass = System.getProperty("db_pass");
		db_server = System.getProperty("db_server");

		log.debug("DB: [" + db_server + "][" + db_user + "][" + db_pass + "]");
	}

	void updateUser(SimplifiedUser user) throws SQLException {
		Connection conn = getConnection("updateUser");
		PreparedStatement ps = null;

		long day = 1000 * 60 * 60 * 24;

		try {
			ps = conn.prepareStatement("update users set laststatus_date=?, nextstatus_date=? where ID=?");
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setTimestamp(2, new Timestamp((new Date().getTime()) + day * user.getStatusMailFreq()));
			ps.setLong(3, user.getId());
			ps.execute();
			close(ps);
		} finally {
			close(ps);
			close(conn);
		}
	}

	public Vector<SimplifiedUserLesson> getLessons(SimplifiedUser user) {
		Vector<SimplifiedUserLesson> ret = new Vector<SimplifiedUserLesson>();
		Connection conn = getConnection("getUsers");
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		SimplifiedUserLesson sul = null;
		if (conn != null) {
			try {
				String sql = "select ul.id as ulid, l.id as lid, l.description as description from userlessons ul, lessons l ";
				sql += "where userID=? and ul.lessonID=l.id and parentID=0";
				ps = conn.prepareStatement(sql);

				String sql2 = "select level, count(*) as cx from useritems ";
				sql2 += "where userlessonID=? and active=1 ";
				sql2 += "group by level";
				ps2 = conn.prepareStatement(sql2);

				ps.setLong(1, user.getId());

				rs = ps.executeQuery();

				while (rs.next()) {
					sul = new SimplifiedUserLesson(rs);

					try {
						ps2.setLong(1, sul.getId());

						rs2 = ps2.executeQuery();

						while (rs2.next()) {
							sul.setLevel(rs2);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						close(rs2);
					}

					getUserLessonAvailable(sul, conn);

					ret.add(sul);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				close(rs);
				close(ps);
				close(ps2);
				close(conn);
			}
		} else {
			log.error("Failed to retrieve db connection.");
		}
		return ret;
	}

	public Vector<SimplifiedUser> getNewsletterUsers() throws DBException {
		Vector<SimplifiedUser> ret = new Vector<SimplifiedUser>();
		Connection conn = getConnection("getUsers");
		PreparedStatement ps = null;
		ResultSet rs = null;
		SimplifiedUser user = null;
		if (conn != null) {
			try {
				long debug_user_id = DEBUG_GET_USERID();

				if (debug_user_id == 0) {
					ps = conn.prepareStatement("select * from users where newsletter>0 and status=0 and unlocked=1 and nextstatus_date<? limit 10");
					ps.setTimestamp(1, new Timestamp(new Date().getTime()));
				} else {
					ps = conn.prepareStatement("select * from users where id=? and status=0");
					ps.setLong(1, debug_user_id);
				}

				rs = ps.executeQuery();

				while (rs.next()) {
					user = new SimplifiedUser(rs);
					ret.add(user);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				close(rs);
				close(ps);
				close(conn);
			}
		} else {
			log.error("Failed to retrieve db connection.");
		}
		return ret;
	}

	private long DEBUG_GET_USERID() {
		long ret = 0;
		String sid = System.getProperty("debug_user_id");
		if (sid != null) {
			try {
				ret = Long.parseLong(sid);
			} catch (Exception e) {
				ret = 0;
			}
		}
		return ret;
	}

	public void getUserLessonAvailable(SimplifiedUserLesson userLesson, Connection conn) {
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
		userLesson.setAvailable(ret);
	}

	private Connection getConnection(String string) {
		Connection conn = null;
		try {
			String url = "jdbc:mysql://localhost:3306/" + db_server;
			conn = DriverManager.getConnection(url, db_user, db_pass);
		} catch (SQLException e) {
			log.error("EXC", e);
		}

		return conn;
	}

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
			if (conn != null)
				conn.close();
		} catch (Exception e) {
		}
	}

}
