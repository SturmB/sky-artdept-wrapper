/**
 * 
 */
package info.chrismcgee.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * @author christopher.mcgee
 *
 */
public class RedisManager {

	public static void main(String[] args) {

		RedisClient redisClient = RedisClient.create("redis://192.168.1.71:6379");
		StatefulRedisConnection<String, String> connection = redisClient.connect();
		RedisCommands<String, String> syncCommands = connection.sync();
		
		System.out.println("Connected to Redis");
		System.out.println("keys - " + syncCommands.keys("*"));
		System.out.println("laravel:chart:statuses - " + syncCommands.get("laravel:chart:statuses"));
		
		connection.close();
		redisClient.shutdown();
	}
}
