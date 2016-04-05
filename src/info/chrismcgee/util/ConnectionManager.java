package info.chrismcgee.util;
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

	private String userName = System.getenv("USER"); // The username of the current logged-in computer.
	// The Username and Password below will be used for connecting to the database.
	// The Password may remain blank, and the Username will be taken from the computer's
	// logged-in user.
	private final String USERNAME = userName;
	private final String PASSWORD = (userName.equalsIgnoreCase("Marketing")) ? "marketing" : userName;
	// Two variables to specify different database types to which to connect. 
	private final String H_CONN_STRING = "jdbc:hsqldb:data/job_orders";
	private final String M_CONN_STRING = "jdbc:mysql://192.168.0.135/job_orders";
	private final String S_CONN_STRING = "jdbc:sqlserver://192.168.0.248;databaseName=job_orders_2014";

	// Using a previously-set enum to specify the type of database we're connecting to.
	private DBType dbType = DBType.MSSQL;

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
		log.entry("getInstance");
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return log.exit(instance);
	}

	/**
	 * @param dbType	Sets the type of database we'll be working with.
	 */
	public void setDBType(DBType dbType) {
		log.entry("setDBType");
		this.dbType = dbType;
		log.exit("setDBType");
	}

	/**
	 * @return	{@link Boolean}	True if the connection opened successfully, false if it did not.
	 * @throws ClassNotFoundException If the class cannot be found for the SQLServerDriver.
	 */
	private boolean openConnection() throws ClassNotFoundException
	{
		log.entry("openConnection");

		// For Java versions below 7.
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
		try {
			switch (dbType) {
			// Depending upon which type of database we're using, the parameters will differ for the DriverManager.
			case MYSQL:
				log.trace("Using MySQL.");
				conn = DriverManager.getConnection(M_CONN_STRING, USERNAME, PASSWORD);
				return log.exit(true);

			case HSQLDB:
				log.trace("Using HyperSQL.");
				conn = DriverManager.getConnection(H_CONN_STRING, USERNAME, PASSWORD);
				return log.exit(true);
				
			case MSSQL:
				log.trace("Using Micrososft SQL Server.");
				conn = DriverManager.getConnection(S_CONN_STRING, USERNAME, PASSWORD);
				return log.exit(true);

			default: 
				return log.exit(false);
			}
		}
		catch (SQLException e) {
			log.error("Exception when trying to open a connection to the database.", e);
			return log.exit(false);
		}

	}

	/**
	 * @return	{@link Connection}	Gets the Connection object to the database.
	 */
	public Connection getConnection()
	{
		log.entry("getConnection");
		if (conn == null) {
			try {
				if (openConnection()) {
					log.debug("Connection opened");
					return log.exit(conn);
				} else {
					return log.exit(null);
				}
			} catch (ClassNotFoundException e) {
				log.error("ClassNotFound exception", e);
				return log.exit(null);
			}
		}
		return log.exit(conn);
	}

	/**
	 * Just a simple method that closes any open connection to the database.
	 */
	public void close() {
		log.entry("Closing connection");
		try {
			conn.close();
			conn = null;
		} catch (Exception e) {
			log.error("Error when closing the database connection.", e);
		}
	}

	/**
	 * @param e	The exception object created when a SQLException is thrown.
	 */
	public static void processException(SQLException e) {
		log.entry("Processing exception...");
		log.error("Error message: " + e.getMessage());
		log.error("Error code: " + e.getErrorCode());
		log.error("SQL state: " + e.getSQLState());
		log.exit("Finished processing exception");
	}
	
}