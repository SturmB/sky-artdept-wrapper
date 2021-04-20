package info.chrismcgee.sky.tables;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.chrismcgee.sky.artdept.ArtDept;
import info.chrismcgee.sky.beans.PrintType;
import info.chrismcgee.util.ConnectionManager;

public class ProductionMaxesManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(ProductionMaxesManager.class.getName());
	
	public static boolean maxExists (Date date, PrintType printType) throws SQLException
	{
		if (ArtDept.loggingEnabled) log.entry("maxExists");
		
		conn = ConnectionManager.getInstance().getConnection();
		ResultSet rs = null;
		String sql = "SELECT * FROM production_maxes "
				+ "WHERE date = ? "
				+ "AND print_type_id = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				) {
			stmt.setDate(1, date);
			stmt.setString(2, printType.getId());
			
			rs = stmt.executeQuery();
			
			return rs.next();
		} catch (SQLException err) {
			// TODO: handle exception
			if (ArtDept.loggingEnabled) log.error(err);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return false;
	}
	
	public static boolean insert (Date date, PrintType printType)
	{
		if (ArtDept.loggingEnabled) log.entry("insert");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO production_maxes ("
				+ "date, "
				+ "print_type_id, "
				+ "maximum, "
				+ "created_at, "
				+ "updated_at) "
				+ "VALUES (?, ?, ?, ?, ?)";
		int affected = 0;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setDate(1, date);
			stmt.setString(2, printType.getId());
			stmt.setLong(3, printType.getDefaultMaximum());
			stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			
			affected = stmt.executeUpdate();
			
		} catch (SQLException err) {
			// TODO: handle exception
			if (ArtDept.loggingEnabled) log.error(err);
			return false;
		}
		
		return affected == 1;
	}

}
