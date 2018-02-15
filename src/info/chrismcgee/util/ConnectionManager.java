package info.chrismcgee.util;
import info.chrismcgee.sky.artdept.ArtDept;
import info.chrismcgee.sky.enums.DBType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionManager
{
	static final Logger log = LogManager.getLogger(ConnectionManager.class.getName()); // For logging.
	private static ConnectionManager instance = null;

//	private String userName = System.getenv("USER"); // The username of the current logged-in computer.
	// The Username and Password below will be used for connecting to the database.
	// The Password may remain blank, and the Username will be taken from the computer's
	// logged-in user.
	private final String USERNAME = "skylauncher";
	private final String PASSWORD = "sky241";
//	private final String PASSWORD = (userName.equalsIgnoreCase("Marketing")) ? "marketing" : userName;
	// Two variables to specify different database types to which to connect. 
	private final String H_CONN_STRING = "jdbc:hsqldb:data/job_orders";
	private final String M_CONN_STRING = "jdbc:mysql://192.168.10.10/sky_unlimited";
	private final String S_CONN_STRING = "jdbc:sqlserver://192.168.0.248;databaseName=job_orders_2014";

	// Using a previously-set enum to specify the type of database we're connecting to.
	private DBType dbType = DBType.MYSQL;

	// The connection to the database starts out as null.
	private Connection conn = null;

	/**
	 * Empty constructor.
	 */
	private ConnectionManager() {
	}

	/**
	 * @return	{@link ConnectionManager}	A connection to the database. Is a singleton, so only one can exist.
	 */
	public static ConnectionManager getInstance() {
		if (ArtDept.loggingEnabled) log.entry("getInstance");
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	/**
	 * @param dbType	Sets the type of database we'll be working with.
	 */
	public void setDBType(DBType dbType) {
		if (ArtDept.loggingEnabled) log.entry("setDBType");
		this.dbType = dbType;
	}

	/**
	 * @return	{@link Boolean}	True if the connection opened successfully, false if it did not.
	 * @throws ClassNotFoundException If the class cannot be found for the SQLServerDriver.
	 */
	private boolean openConnection() throws ClassNotFoundException
	{
		if (ArtDept.loggingEnabled) log.entry("openConnection");

		// For Java versions below 7.
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
		try {
			switch (dbType) {
			// Depending upon which type of database we're using, the parameters will differ for the DriverManager.
			case MYSQL:
				if (ArtDept.loggingEnabled) log.trace("Using MySQL.");
				conn = DriverManager.getConnection(M_CONN_STRING, USERNAME, PASSWORD);
				return true;

			case HSQLDB:
				if (ArtDept.loggingEnabled) log.trace("Using HyperSQL.");
				conn = DriverManager.getConnection(H_CONN_STRING, USERNAME, PASSWORD);
				return true;
				
			case MSSQL:
				if (ArtDept.loggingEnabled) log.trace("Using Micrososft SQL Server.");
				conn = DriverManager.getConnection(S_CONN_STRING, USERNAME, PASSWORD);
				return true;

			default: 
				return false;
			}
		}
		catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error("Exception when trying to open a connection to the database.", e);
			return false;
		}

	}

	/**
	 * @return	{@link Connection}	Gets the Connection object to the database.
	 */
	public Connection getConnection()
	{
		if (ArtDept.loggingEnabled) log.entry("getConnection");
		if (conn == null) {
			try {
				if (openConnection()) {
					if (ArtDept.loggingEnabled) log.debug("Connection opened");
					return conn;
				} else {
					return null;
				}
			} catch (ClassNotFoundException e) {
				if (ArtDept.loggingEnabled) log.error("ClassNotFound exception", e);
				return null;
			}
		}
		return conn;
	}

	/**
	 * Just a simple method that closes any open connection to the database.
	 */
	public void close() {
		if (ArtDept.loggingEnabled) log.entry("Closing connection");
		try {
			conn.close();
			conn = null;
		} catch (Exception e) {
			if (ArtDept.loggingEnabled) log.error("Error when closing the database connection.", e);
		}
	}

	/**
	 * @param e	The exception object created when a SQLException is thrown.
	 */
	public static void processException(SQLException e) {
		if (ArtDept.loggingEnabled) log.entry("Processing exception...");
		if (ArtDept.loggingEnabled) log.error("Error message: " + e.getMessage());
		if (ArtDept.loggingEnabled) log.error("Error code: " + e.getErrorCode());
		if (ArtDept.loggingEnabled) log.error("SQL state: " + e.getSQLState());
	}
	
}