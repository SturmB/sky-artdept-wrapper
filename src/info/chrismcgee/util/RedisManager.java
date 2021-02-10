/**
 * 
 */
package info.chrismcgee.util;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.chrismcgee.sky.artdept.ArtDept;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * @author christopher.mcgee
 *
 */
public class RedisManager {

	static final Logger log = LogManager.getLogger(RedisManager.class.getName()); // For logging.
	private static RedisManager instance = null;

	private final String REMOTE_CONN_STRING = "redis://192.168.1.71:6379/1";
	private final String LOCAL_CONN_STRING = "redis://127.0.0.1:6379/1";

	// The connection to the database starts out as null.
	private RedisClient redisClient = null;
	private StatefulRedisConnection<String, String> connection = null;
	private RedisCommands<String, String> syncCommands = null;

	public static void main(String[] args) {
		System.out.println(RedisManager.getInstance().getCommands().keys("*chedule*"));
		RedisManager.getInstance().close();
	}
	
	/**
	 * @return	{@link RedisManager}	A connection to Redis. Is a singleton, so only one can exist.
	 */
	public static RedisManager getInstance() {
		if (instance == null) {
			instance = new RedisManager();
		}
		return instance;
	}
	
	private boolean openConnection() {
		try {
			redisClient = RedisClient.create(REMOTE_CONN_STRING);
			connection = redisClient.connect();
			syncCommands = connection.sync();
			return true;
		} catch (Exception e) {
			if (ArtDept.loggingEnabled) log.error("Exception when trying to open a connection to Redis.", e);
			return false;
		}
	}

	/**
	 * @return	{@link Connection}	Gets the RedisCommands object to Redis.
	 */
	public RedisCommands<String, String> getCommands() {
		if (syncCommands == null) {
			if (openConnection()) {
				return syncCommands;
			} else {
				return null;
			}
		}
		return syncCommands;
	}

	/**
	 * Just a simple method that closes any open connection to Redis.
	 */
	public void close() {
		if (ArtDept.loggingEnabled) log.entry("Closing connection");
		try {
			connection.close();
			redisClient.shutdown();
			connection = null;
			redisClient = null;
		} catch (Exception e) {
			if (ArtDept.loggingEnabled) log.error("Error when closing the Redis connection.", e);
		}
	}

}
