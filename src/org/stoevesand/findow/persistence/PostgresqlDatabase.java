package org.stoevesand.findow.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresqlDatabase {

	private static boolean initialized = false;

	public PostgresqlDatabase() {
		try {
			Class.forName("org.postgresql.Driver");
			initialized = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static String dburi = "jdbc:postgresql://ec2-54-247-120-169.eu-west-1.compute.amazonaws.com:5432/dbce6l4mja9b9h?sslmode=require";
	private static String user = "tvqsilkojqyoew";
	private static String pass = "ecf847e4bf9d1275a867595261e0bb882989d9fbdda4c1fc34f089b01d2aa44e";

	public Connection getConnection() throws SQLException {
		Connection connection = null;
		connection = DriverManager.getConnection(dburi, user, pass);
		return connection;
	}

	public void init_DB() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("select 1");
			rs = ps.executeQuery();

			if (rs.next()) {
				int r = rs.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}

	}

	public static void close(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	public static void close(PreparedStatement ps) {
		try {
			if (ps != null)
				ps.close();
		} catch (SQLException e) {
		}
	}

	public static void close(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
		}
	}

}
