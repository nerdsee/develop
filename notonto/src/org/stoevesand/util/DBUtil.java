package org.stoevesand.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class DBUtil {
	
	private static Logger log = LogManager.getLogger(DBUtil.class);

	public static long getLong(ResultSet rs, String field) {
		try {
			return rs.getLong(field);
		} catch (SQLException e) {
			log.error("Unknown Long Field: " + field);
			return 0L;
		}
	}

	public static String getString(ResultSet rs, String field) {
		try {
			return rs.getString(field);
		} catch (SQLException e) {
			log.error("Unknown String Field: " + field);
			return "";
		}
	}

	public static boolean getBoolean(ResultSet rs, String field) {
		try {
			return rs.getBoolean(field);
		} catch (SQLException e) {
			log.error("Unknown Boolean Field: " + field);
			return false;
		}
	}

	public static int getInt(ResultSet rs, String field) {
		try {
			return rs.getInt(field);
		} catch (SQLException e) {
			log.error("Unknown Int Field: " + field);
			return -1;
		}
	}

	public static short getShort(ResultSet rs, String field) {
		try {
			return rs.getShort(field);
		} catch (SQLException e) {
			log.error("Unknown Short Field: " + field);
			return -1;
		}
	}

	public static Timestamp getTimestamp(ResultSet rs, String field) {
		try {
			return rs.getTimestamp(field);
		} catch (SQLException e) {
			log.error("Unknown Timestamp Field: " + field);
			return new Timestamp(1);
		}
	}

	public static Date getDate(ResultSet rs, String field) {
		try {
			return rs.getDate(field);
		} catch (SQLException e) {
			log.error("Unknown Date Field: " + field);
			return null;
		}
	}

}
