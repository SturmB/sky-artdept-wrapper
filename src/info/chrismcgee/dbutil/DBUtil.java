package info.chrismcgee.dbutil;

import info.chrismcgee.sky.enums.DBType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	
	private static String userName = System.getenv("USER"); // The username of the current logged-in computer.

	// Preparing static Strings for connecting to the database.
	public static final String USERNAME = userName;
	private static final String PASSWORD = (userName.equalsIgnoreCase("Marketing")) ? "marketing" : userName;
	private static final String M_CONN_STRING = "jdbc:mysql://192.168.1.71/sky_schedule?useSSL=false";
//	private static final String M_CONN_STRING = "jdbc:mysql://127.0.0.1/sky_schedule?useSSL=false";
	private static final String H_CONN_STRING = "jdbc:hsqldb:data/job_orders";
	private static final String S_CONN_STRING = "jdbc:sqlserver://192.168.0.248;databaseName=job_orders_2014";
	
	public static Connection getConnection(DBType dbType) throws SQLException {
		
		switch (dbType) {
		case MYSQL:
			return DriverManager.getConnection(M_CONN_STRING, USERNAME, PASSWORD);
		case HSQLDB:
			return DriverManager.getConnection(H_CONN_STRING, USERNAME, PASSWORD);
		case MSSQL:
			return DriverManager.getConnection(S_CONN_STRING, USERNAME, PASSWORD);
		default:
			return null;
		}
		
	}
	
	public static void processException(SQLException e) {
		System.err.println("Error message: " + e.getMessage());
		System.err.println("Error code: " + e.getErrorCode());
		System.err.println("SQL state: " + e.getSQLState());
	}
	
}
