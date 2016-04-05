package info.chrismcgee.sky.tables;

import info.chrismcgee.sky.beans.Day;
import info.chrismcgee.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DayManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(DayManager.class.getName());
	
	
	public static Day getRow(Date theDate) throws SQLException {
		
		log.entry("getRow (DayManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		ResultSet rs = null;
		String sql = "SELECT * FROM Day WHERE day_id = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setDate(1, theDate);
			rs = stmt.executeQuery();
			
			
			if (rs.next()) {
				Day bean = new Day(rs.getDate("day_id"));
//				bean.setDate(rs.getDate("day_id"));
				return log.exit(bean);
			} else {
				return log.exit(null);
			}
			
		} catch (SQLException e) {
			log.error(e);
			 return log.exit(null);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}
	
	public static boolean dayExists(Date theDate) throws SQLException
	{
		log.entry("dayExists");
		
		conn = ConnectionManager.getInstance().getConnection();
		ResultSet rs = null;
		String sql = "SELECT * FROM Day WHERE id_day = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				) {
			stmt.setDate(1, theDate);
			rs = stmt.executeQuery();
			
			
			return log.exit(rs.next());
			
			
		} catch (SQLException e) {
			log.error(e);
			 return log.exit(false);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		
	}
	
	public static boolean insert(Day bean) throws Exception {
		
		log.entry("Inserting Day bean into database.");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO Day ("
				+ "id_day, "
				+ "avail_screen, "
				+ "avail_screen_naps, "
				+ "avail_pad, "
				+ "avail_hotstamp, "
				+ "avail_digital, "
				+ "avail_offset_cups, "
				+ "avail_offset_naps, "
				+ "avail_outsourced, "
				+ "remain_screen, "
				+ "remain_screen_naps, "
				+ "remain_pad, "
				+ "remain_hotstamp, "
				+ "remain_digital, "
				+ "remain_offset_cups, "
				+ "remain_offset_naps,"
				+ "remain_outsourced,"
				+ "day_completed) " +
				"VALUES (?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, "
				+ "?, ?, ?)";

		try (
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				){
			
			log.debug("INSERTing into db. AvailableScreenCups are: " + bean.getAvailableScreenCups());
			stmt.setDate(1, bean.getDate());
			
			stmt.setLong(2, bean.getAvailableScreenCups());
			stmt.setLong(3, bean.getAvailableScreenNaps());
			stmt.setLong(4, bean.getAvailablePad());
			stmt.setLong(5, bean.getAvailableHotstamp());
			stmt.setLong(6, bean.getAvailableDigital());
			stmt.setLong(7, bean.getAvailableOffsetCups());
			stmt.setLong(8, bean.getAvailableOffsetNaps());
			stmt.setLong(9, bean.getAvailableOutsourced());
			
			stmt.setLong(10, bean.getRemainScreenCups());
			stmt.setLong(11, bean.getAvailableScreenNaps());
			stmt.setLong(12, bean.getRemainPad());
			stmt.setLong(13, bean.getRemainHotstamp());
			stmt.setLong(14, bean.getRemainDigital());
			stmt.setLong(15, bean.getRemainOffsetCups());
			stmt.setLong(16, bean.getRemainOffsetNaps());
			stmt.setLong(17, bean.getRemainOutsourced());
			
			stmt.setTimestamp(18, bean.getDayCompleted());
			stmt.executeUpdate();
						
		} catch (SQLException e) {
			log.error(e);
			return log.exit(false);
		}
		return log.exit(true);
	}
	
	public static boolean setNotCompleted(Date theDate)
	{
		log.entry("Setting the date in the Day table as NOT completed (null the 'day_completed' field.");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "UPDATE Day "
				+ "SET day_completed = NULL "
				+ "WHERE id_day = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			stmt.setDate(1, theDate);
			stmt.executeUpdate();
			
		} catch (SQLException err) {
			log.error("Setting the 'day_completed' field to 'NULL' generated an exception.", err);
			return log.exit(false);
		}
		return log.exit(true);
	}

}
