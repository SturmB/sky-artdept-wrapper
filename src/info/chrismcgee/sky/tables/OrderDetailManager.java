package info.chrismcgee.sky.tables;

import info.chrismcgee.sky.artdept.ArtDept;
import info.chrismcgee.sky.beans.OrderDetail;
import info.chrismcgee.sky.enums.PrintType;
import info.chrismcgee.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderDetailManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(OrderDetailManager.class.getName());
	
	public static OrderDetail getRow(int id) throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getRow (OrderDetailManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM OrderDetail WHERE id = ?";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			
			if (rs.next()) {
				OrderDetail bean = new OrderDetail();
				bean.setId(id);
				bean.setOrderId(rs.getString("order_id"));
				bean.setProductId(rs.getString("product_id"));
				bean.setProductDetail(rs.getString("product_detail"));
				bean.setPrintType(PrintType.getPrintType(rs.getInt("print_type")));
				bean.setNumColors(rs.getLong("num_colors"));
				bean.setQuantity(rs.getLong("quantity"));
				bean.setItemCompleted(rs.getTimestamp("item_completed") == null ? null : new Date(rs.getTimestamp("item_completed").getTime()));
				bean.setProofNum(rs.getInt("proof_num"));
				bean.setProofDate(rs.getTimestamp("proof_date") == null ? null : new Date(rs.getTimestamp("proof_date").getTime()));
				bean.setThumbnail(rs.getString("thumbnail"));
				bean.setFlags(rs.getInt("flags"));
				bean.setReorderId(rs.getString("reorder_id"));
				bean.setPackingInstructions(rs.getString("packing_instructions"));
				bean.setPackageQuantity(rs.getString("package_quantity"));
				bean.setCaseQuantity(rs.getString("case_quantity"));
				bean.setLabelQuantity(rs.getInt("label_quantity"));
				bean.setLabelText(rs.getString("label_text"));
				bean.setDigitalFilename(rs.getString("digital_art_file"));
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

	public static boolean insert(OrderDetail bean) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("insert (OrderDetail)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO OrderDetail ("
				+ "order_id, "
				+ "product_id, "
				+ "product_detail, "
				+ "print_type, "
				+ "num_colors, "
				+ "quantity, "
				+ "item_completed, "
				+ "proof_num, "
				+ "proof_date, "
				+ "thumbnail, "
				+ "flags, "
				+ "reorder_id, "
				+ "packing_instructions, "
				+ "package_quantity, "
				+ "case_quantity, "
				+ "label_quantity, "
				+ "label_text, "
				+ "digital_art_file) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		ResultSet keys = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				){
			
			stmt.setString(1, bean.getOrderId());
			stmt.setString(2, bean.getProductId());
			stmt.setString(3, bean.getProductDetail());
			stmt.setInt(4, PrintType.getIntValue(bean.getPrintType()));
			stmt.setLong(5, bean.getNumColors());
			stmt.setLong(6, bean.getQuantity());
			stmt.setTimestamp(7, bean.getItemCompleted() == null ? null : new Timestamp(bean.getItemCompleted().getTime()));
			stmt.setInt(8, bean.getProofNum());
			stmt.setTimestamp(9, bean.getProofDate() == null ? null : new Timestamp(bean.getProofDate().getTime()));
			stmt.setString(10, bean.getThumbnail());
			stmt.setInt(11, bean.getFlags());
			stmt.setString(12, bean.getReorderId());
			stmt.setString(13, bean.getPackingInstructions());
			stmt.setString(14, bean.getPackageQuantity());
			stmt.setString(15, bean.getCaseQuantity());
			stmt.setInt(16, bean.getLabelQuantity());
			stmt.setString(17, bean.getLabelText());
			stmt.setString(18, bean.getDigitalFilename());
			
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
	
	public static boolean update(OrderDetail bean) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("update (OrderDetail)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql =
				"UPDATE OrderDetail SET "
				+ "order_id = ?, "
				+ "product_id = ?, "
				+ "product_detail = ?, "
				+ "print_type = ?, "
				+ "num_colors = ?, "
				+ "quantity = ?, "
				+ "item_completed = ?, "
				+ "proof_num = ?, "
				+ "proof_date = ?, "
				+ "thumbnail = ?, "
				+ "flags = ?, "
				+ "reorder_id = ?, "
				+ "packing_instructions = ?, "
				+ "package_quantity = ?, "
				+ "case_quantity = ?, "
				+ "label_quantity = ?, "
				+ "label_text = ?, "
				+ "digital_art_file = ? "
				+ "WHERE id = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			stmt.setString(1, bean.getOrderId());
			stmt.setString(2, bean.getProductId());
			stmt.setString(3, bean.getProductDetail());
			stmt.setInt(4, PrintType.getIntValue(bean.getPrintType()));
			stmt.setLong(5, bean.getNumColors());
			stmt.setLong(6, bean.getQuantity());
			stmt.setTimestamp(7, bean.getItemCompleted() == null ? null : new Timestamp(bean.getItemCompleted().getTime()));
			stmt.setInt(8, bean.getProofNum());
			stmt.setTimestamp(9, bean.getProofDate() == null ? null : new Timestamp(bean.getProofDate().getTime()));
			stmt.setString(10, bean.getThumbnail());
			stmt.setInt(11, bean.getFlags());
			stmt.setString(12, bean.getReorderId());
			stmt.setString(13, bean.getPackingInstructions());
			stmt.setString(14, bean.getPackageQuantity());
			stmt.setString(15, bean.getCaseQuantity());
			stmt.setInt(16, bean.getLabelQuantity());
			stmt.setString(17, bean.getLabelText());
			stmt.setString(18, bean.getDigitalFilename());
			stmt.setInt(19, bean.getId());
			
			int affected = stmt.executeUpdate();
			if (affected == 1) {
				return true;
			} else {
				return false;
			}
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			return false;
		}
		
	}

	public static boolean delete(int id) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("delete (OrderDetail)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "DELETE FROM OrderDetail WHERE id = ?";
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			stmt.setInt(1, id);
			int affected = stmt.executeUpdate();
			
			if (affected == 1) {
				return true;
			} else {
				return false;
			}
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			 return false;
		}
		
	}
	
}
