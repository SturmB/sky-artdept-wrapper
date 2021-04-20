package info.chrismcgee.sky.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.chrismcgee.sky.artdept.ArtDept;
import info.chrismcgee.sky.beans.PrintType;
import info.chrismcgee.util.ConnectionManager;

public class PrintTypeManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(PrintTypeManager.class.getName());
	
	public static ArrayList<PrintType> getAllPrintTypes () throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getAllPrintTypes (PrintTypeManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM print_types";
		ResultSet rs = null;
		ArrayList<PrintType> printTypes = new ArrayList<PrintType>();
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				PrintType printType = new PrintType();
				printType.setId(rs.getString("id"));
				printType.setSort(rs.getInt("sort"));
				printType.setColor(rs.getString("color"));
				printType.setPrintMethodId(rs.getString("print_method_id"));
				printType.setDefaultMaximum(rs.getLong("default_maximum"));
				
				printType.setCreatedAt(rs.getTimestamp("created_at"));
				printType.setUpdatedAt(rs.getTimestamp("updated_at"));
				
				printTypes.add(printType);
			}
			
			return printTypes;
			
		} catch (SQLException err) {
			if (ArtDept.loggingEnabled) log.error(err);
			return null;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

	}

}
