package info.chrismcgee.sky.tables;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.chrismcgee.sky.artdept.ArtDept;
import info.chrismcgee.sky.beans.Day;
import info.chrismcgee.util.ConnectionManager;

public class DayManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(DayManager.class.getName());
	
	// This method is currently unused in this project.
	public static Day getRow(Date theDate) throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getRow (DayManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		ResultSet rs = null;
		String sql = "SELECT * FROM Day WHERE id_day = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setDate(1, theDate);
			rs = stmt.executeQuery();
			
			
			if (rs.next()) {
				Day bean = new Day(rs.getDate("id_day"));
//				bean.setDate(rs.getDate("id_day"));
				return bean;
			} else {
				return null;
			}
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			 return null;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}
	
	public static boolean dayExists(Date theDate) throws SQLException
	{
		if (ArtDept.loggingEnabled) log.entry("dayExists");
		
		conn = ConnectionManager.getInstance().getConnection();
		ResultSet rs = null;
		String sql = "SELECT * FROM Day WHERE id_day = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				) {
			stmt.setDate(1, theDate);
			rs = stmt.executeQuery();
			
			
			return rs.next();
			
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			 return false;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		
	}
	
	public static boolean insert(Day bean) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("Inserting Day bean into database.");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO Day ("
				+ "id_day, "
				+ "avail_screen_cups, "
				+ "avail_screen_naps, "
				+ "avail_pad, "
				+ "avail_hotstamp, "
				+ "avail_digital_cups, "
				+ "avail_digital_flats, "
				+ "avail_offset_cups, "
				+ "avail_offset_naps, "
				+ "avail_outsourced, "
				+ "remain_screen_cups, "
				+ "remain_screen_naps, "
				+ "remain_pad, "
				+ "remain_hotstamp, "
				+ "remain_digital_cups, "
				+ "remain_digital_flats, "
				+ "remain_offset_cups, "
				+ "remain_offset_naps, "
				+ "remain_outsourced, "
				+ "day_completed) " +
				"VALUES (?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?)";

		try (
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				){
			
			if (ArtDept.loggingEnabled) log.debug("INSERTing into db. AvailableScreenCups are: " + bean.getAvailableScreenCups());
			stmt.setDate(1, bean.getDate());
			
			stmt.setLong(2, bean.getAvailableScreenCups());
			stmt.setLong(3, bean.getAvailableScreenNaps());
			stmt.setLong(4, bean.getAvailablePad());
			stmt.setLong(5, bean.getAvailableHotstamp());
			stmt.setLong(6, bean.getAvailableDigitalCups());
			stmt.setLong(7, bean.getAvailableDigitalFlats());
			stmt.setLong(8, bean.getAvailableOffsetCups());
			stmt.setLong(9, bean.getAvailableOffsetNaps());
			stmt.setLong(10, bean.getAvailableOutsourced());
			
			stmt.setLong(11, bean.getRemainScreenCups());
			stmt.setLong(12, bean.getRemainScreenNaps());
			stmt.setLong(13, bean.getRemainPad());
			stmt.setLong(14, bean.getRemainHotstamp());
			stmt.setLong(15, bean.getRemainDigitalCups());
			stmt.setLong(16, bean.getRemainDigitalFlats());
			stmt.setLong(17, bean.getRemainOffsetCups());
			stmt.setLong(18, bean.getRemainOffsetNaps());
			stmt.setLong(19, bean.getRemainOutsourced());
			
			stmt.setTimestamp(20, bean.getDayCompleted());
			stmt.executeUpdate();
						
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			return false;
		}
		return true;
	}
	
	public static boolean setNotCompleted(Date theDate)
	{
		if (ArtDept.loggingEnabled) log.entry("Setting the date in the Day table as NOT completed (null the 'day_completed' field.");
		
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
			if (ArtDept.loggingEnabled) log.error("Setting the 'day_completed' field to 'NULL' generated an exception.", err);
			return false;
		}
		return true;
	}
	
}
