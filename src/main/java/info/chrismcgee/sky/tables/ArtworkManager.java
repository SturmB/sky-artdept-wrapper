package info.chrismcgee.sky.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.chrismcgee.sky.artdept.ArtDept;
import info.chrismcgee.sky.beans.Artwork;
import info.chrismcgee.util.ConnectionManager;

public class ArtworkManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(ArtworkManager.class.getName());
	
	
	public static Artwork getRow(int id) throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getRow (ArtworkManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM artworks WHERE id = ?";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			
			if (rs.next()) {
				Artwork bean = new Artwork();
				bean.setId(id);
				bean.setLineItemId(rs.getInt("line_item_id"));
				bean.setDigitalArtFile(rs.getString("digital_art_file"));
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
	
	
	public static ArrayList<Artwork> getArtworksByOrderId(int id) throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getArtworksByOrderId (ArtworkManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM artworks WHERE line_item_id = ?";
		ResultSet rs = null;
		ArrayList<Artwork> artworks = new ArrayList<Artwork>();
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				Artwork bean = new Artwork();
				bean.setId(rs.getInt("id"));
				bean.setLineItemId(rs.getInt("line_item_id"));
				bean.setDigitalArtFile(rs.getString("digital_art_file"));
				artworks.add(bean);
			}
			
			return artworks;
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			 return null;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}
	
	
	public static boolean artworkExists(int id) throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getRow (ArtworkManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM artworks WHERE id = ?";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setInt(1, id);
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
	
	public static int getArtworkId(String artworkFileName) throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getRow (ArtworkManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM artworks WHERE digital_art_file = ?";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setString(1, artworkFileName);
			rs = stmt.executeQuery();
			
			
			if (rs.next()) {
				return rs.getInt("id");
			}
			
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			return 0;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		
		return 0;
	}
	
	public static boolean insert(Artwork bean) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("insert (Artwork)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO artworks ("
				+ "line_item_id, "
				+ "digital_art_file,"
				+ "created_at,"
				+ "updated_at) "
				+ "VALUES (?, ?, ?, ?)";
		ResultSet keys = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				){
			
			if (ArtDept.loggingEnabled) log.debug("odID: " + bean.getLineItemId());
			if (ArtDept.loggingEnabled) log.debug("DAF: " + bean.getDigitalArtFile());

			stmt.setInt(1, bean.getLineItemId());
			stmt.setString(2, bean.getDigitalArtFile());
			
			stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			
			
			if (ArtDept.loggingEnabled) log.debug("stmt: " + stmt.toString());
			
			int affected = stmt.executeUpdate();
			
			if (affected == 1) {
				keys = stmt.getGeneratedKeys();
				keys.next();
				int newKey = keys.getInt(1);
				bean.setId(newKey);
			} else {
				if (ArtDept.loggingEnabled) log.debug("No rows affected");
				return false;
			}
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			return false;
		} finally {
			if (keys != null) keys.close();
		}
		return true;
	}
	
	public static boolean update(Artwork bean) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("update (Artwork)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql =
				"UPDATE artworks SET "
				+ "line_item_id = ?, "
				+ "digital_art_file = ?, "
				+ "updated_at = ? "
				+ "WHERE id = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			if (ArtDept.loggingEnabled) log.debug("liID: " + bean.getLineItemId());
			if (ArtDept.loggingEnabled) log.debug("DAF: " + bean.getDigitalArtFile());
			if (ArtDept.loggingEnabled) log.debug("ID: " + bean.getId());
			
			stmt.setInt(1, bean.getLineItemId());
			stmt.setString(2, bean.getDigitalArtFile());
			
			stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			
			stmt.setInt(4, bean.getId());
			
			int affected = stmt.executeUpdate();
			if (affected == 1) {
				if (ArtDept.loggingEnabled) log.debug("Updated Artwork.");
				return true;
			} else {
				if (ArtDept.loggingEnabled) log.debug("Failed to update Artwork.");
				return false;
			}
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			return false;
		}
		
	}

	public static boolean delete(int id) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("delete (Artwork); id #" + id);
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "DELETE FROM artworks WHERE id = ?";
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			stmt.setInt(1, id);
			int affected = stmt.executeUpdate();
			
			if (affected == 1) {
				if (ArtDept.loggingEnabled) log.debug("Deleted Artwork.");
				return true;
			} else {
				if (ArtDept.loggingEnabled) log.debug("Did not delete Artwork.");
				return false;
			}
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			 return false;
		}
		
	}
	
}
