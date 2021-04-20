package info.chrismcgee.sky.tables;

import info.chrismcgee.sky.artdept.ArtDept;
import info.chrismcgee.sky.beans.Artwork;
import info.chrismcgee.sky.beans.Order;
import info.chrismcgee.sky.beans.LineItem;
import info.chrismcgee.sky.enums.PrintingCompany;
import info.chrismcgee.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(OrderManager.class.getName());
	
	
	public static Order getRow(String id) throws SQLException {
		
		if (ArtDept.loggingEnabled) log.entry("getRow (OrderManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM orders WHERE id = ?";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			
			
			if (rs.next()) {
				Order bean = new Order();
				bean.setShipDateId(rs.getDate("ship_date_id"));
				bean.setId(id);
				bean.setCustomerName(rs.getString("customer_name"));
				bean.setCustomerPO(rs.getString("customer_po"));
				bean.setProofSpecDate(new Date(rs.getTimestamp("proof_spec_date").getTime()));
				bean.setJobCompleted(rs.getTimestamp("job_completed") == null ? null : new Date(rs.getTimestamp("job_completed").getTime()));
				bean.setPrintingCompany(PrintingCompany.getPrintingCompany(rs.getInt("printing_company")));
				bean.setOverruns(rs.getBoolean("overruns"));
				bean.setSampleShelfNote(rs.getBoolean("sample_shelf_note"));
				bean.setSigProof(rs.getString("sig_proof"));
				bean.setSigOutput(rs.getString("sig_output"));
				bean.setRush(rs.getBoolean("rush"));
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
	
	public static Order getOrder (String id) throws SQLException {
		if (ArtDept.loggingEnabled) log.entry("getJob (OrderManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT ship_date_id, o.id AS order_id, customer_name, customer_po, proof_spec_date, "
				+ "job_completed, printing_company, overruns, sample_shelf_note, sig_proof, sig_output, "
				+ "li.id AS li_id, product_num, product_detail, print_type_id, num_impressions, "
				+ "impressions_tradition, impressions_hispeed, impressions_digital, quantity, "
				+ "item_completed, proof_num, proof_date, thumbnail, flags, reorder_num, "
				+ "packing_instructions, package_quantity, case_quantity, label_quantity, label_text, item_status_id, "
				+ "a.id AS artwork_id, a.line_item_id AS artwork_li_id, a.digital_art_file "
				+ "FROM orders AS o "
				+ "JOIN line_items AS li "
				+ "ON o.id = li.order_id "
				+ "LEFT JOIN artworks AS a "
				+ "ON a.line_item_id = li.id "
				+ "WHERE o.id = ? ";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				){
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				Order bean = new Order();
				bean.setShipDateId(rs.getDate("ship_date_id"));
				bean.setId(id);
				bean.setCustomerName(rs.getString("customer_name"));
				bean.setCustomerPO(rs.getString("customer_po"));
				bean.setProofSpecDate(new Date(rs.getTimestamp("proof_spec_date").getTime()));
				if (ArtDept.loggingEnabled) log.debug("In the middle of getting an Order.");
				if (ArtDept.loggingEnabled) log.debug("Job Completed: " + rs.getTimestamp("job_completed"));
				bean.setJobCompleted(rs.getTimestamp("job_completed") == null ? null : new Date(rs.getTimestamp("job_completed").getTime()));
				bean.setPrintingCompany(PrintingCompany.getPrintingCompany(rs.getInt("printing_company")));
				bean.setOverruns(rs.getBoolean("overruns"));
				bean.setSampleShelfNote(rs.getBoolean("sample_shelf_note"));
				bean.setSigProof(rs.getString("sig_proof"));
				bean.setSigOutput(rs.getString("sig_output"));
				List<LineItem> liList = new ArrayList<LineItem>();
				do {
					LineItem li = new LineItem();
					li.setId(rs.getInt("li_id"));
					li.setOrderId(id);
					li.setProductNum(rs.getString("product_num"));
					li.setProductDetail(rs.getString("product_detail"));
					li.setPrintTypeId(rs.getString("print_type_id"));
					li.setNumImpressions(rs.getLong("num_impressions"));
					li.setImpressionsTradition(rs.getLong("impressions_tradition"));
					li.setImpressionsHiSpeed(rs.getLong("impressions_hispeed"));
					li.setImpressionsDigital(rs.getLong("impressions_digital"));
					li.setQuantity(rs.getLong("quantity"));
					li.setItemCompleted(rs.getTimestamp("item_completed") == null ? null : new Date(rs.getTimestamp("item_completed").getTime()));
					li.setProofNum(rs.getInt("proof_num"));
					li.setProofDate(rs.getTimestamp("proof_date") == null ? null : new Date(rs.getTimestamp("proof_date").getTime()));
					li.setThumbnail(rs.getString("thumbnail"));
					li.setFlags(rs.getInt("flags"));
					li.setReorderNum(rs.getString("reorder_num"));
					li.setPackingInstructions(rs.getString("packing_instructions"));
					li.setPackageQuantity(rs.getString("package_quantity"));
					li.setCaseQuantity(rs.getString("case_quantity"));
					li.setLabelQuantity(rs.getInt("label_quantity"));
					li.setLabelText(rs.getString("label_text"));
					li.setItemStatusId(rs.getString("item_status_id"));
					if (rs.getInt("artwork_id") > 0) {
						List<Artwork> artworkList = new ArrayList<Artwork>();
						while (li.getId() == rs.getInt("artwork_li_id")) {
							Artwork aw = new Artwork();
							aw.setId(rs.getInt("artwork_id"));
							aw.setLineItemId(rs.getInt("li_id"));
							aw.setDigitalArtFile(rs.getString("digital_art_file"));
							artworkList.add(aw);
							if (!rs.next()) {
								break;
							}
						}
						rs.previous();
						li.setArtworkList(artworkList);
					}
					liList.add(li);
				} while (rs.next());
				if (ArtDept.loggingEnabled) {
					log.debug("liList: ");
					for (Iterator<LineItem> iterator = liList.iterator(); iterator
							.hasNext();) {
						LineItem lineItem = (LineItem) iterator.next();
						log.debug("  LI: " + lineItem.toString());
					}
				}
				bean.setLineItemList(liList);

				return bean;
				
			} else {
				return null;
			}
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			return null;
		} finally {
			if (rs != null) rs.close();
		}
	}

	public static boolean insert(Order bean) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("insert (OrderManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO orders (ship_date_id, id, customer_name, customer_po, "
				+ "proof_spec_date, job_completed, printing_company, overruns, sample_shelf_note, "
				+ "sig_proof, sig_output, rush, created_at, updated_at) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				){
			
			stmt.setDate(1, bean.getShipDateId());
			stmt.setString(2, bean.getId());
			stmt.setString(3, bean.getCustomerName());
			stmt.setString(4, bean.getCustomerPO());
			stmt.setTimestamp(5, new Timestamp(bean.getProofSpecDate().getTime()));
			stmt.setTimestamp(6, bean.getJobCompleted() == null ? null : new Timestamp(bean.getJobCompleted().getTime()));
			stmt.setInt(7, bean.getPrintingCompany().getValue());
			stmt.setBoolean(8, bean.isOverruns());
			stmt.setBoolean(9, bean.isSampleShelfNote());
			stmt.setString(10, bean.getSigProof());
			stmt.setString(11, bean.getSigOutput());
			stmt.setBoolean(12, bean.isRush());
			
			stmt.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
			stmt.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			if (ArtDept.loggingEnabled) log.error(e);
			return false;
		}

/*		// Also insert all of the Order's LineItem items into its table.
		for (LineItem li : bean.getOrderDetailList()) {
			OrderDetailManager.insert(li);
		}*/
		
		// And finally, since an order was ADDED to the day (not UPDATED),
		// The day is no longer completed.
		ShipDateManager.setNotCompleted(bean.getShipDateId());
		
		return true;
	}
		
	public static boolean update(Order bean) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("update (OrderManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql =
				"UPDATE orders "
				+ "SET ship_date_id = ?, "
				+ "customer_name = ?, "
				+ "customer_po = ?, "
				+ "proof_spec_date = ?, "
				+ "job_completed = ?, "
				+ "printing_company = ?, "
				+ "overruns = ?, "
				+ "sample_shelf_note = ?, "
				+ "sig_proof = ?, "
				+ "sig_output = ?, "
				+ "rush = ?, "
				+ "updated_at = ? "
				+ "WHERE id = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			if (ArtDept.loggingEnabled) log.debug("Ship date from the Order bean is: " + bean.getShipDateId());
			
			stmt.setDate(1, bean.getShipDateId());
			stmt.setString(2, bean.getCustomerName());
			stmt.setString(3, bean.getCustomerPO());
			stmt.setTimestamp(4, new Timestamp(bean.getProofSpecDate().getTime()));
			stmt.setTimestamp(5, bean.getJobCompleted() == null ? null : new Timestamp(bean.getJobCompleted().getTime()));
			stmt.setInt(6, bean.getPrintingCompany().getValue());
			stmt.setBoolean(7, bean.isOverruns());
			stmt.setBoolean(8, bean.isSampleShelfNote());
			stmt.setString(9, bean.getSigProof());
			stmt.setString(10, bean.getSigOutput());
			stmt.setBoolean(11, bean.isRush());

			stmt.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
			
			stmt.setString(13, bean.getId());
			
			
			if (ArtDept.loggingEnabled) log.debug("Native SQL: " + stmt.getConnection().nativeSQL(sql));
			
			int affected = stmt.executeUpdate();
			
/*			// Now also update the Order's LineItem items in its table.
			boolean successfulODUpdate = true;
			for (LineItem li : bean.getOrderDetailList())
			{
				if (ArtDept.loggingEnabled) log.trace("Inserting the LineItem table after updating the Order table.");
				if (!OrderDetailManager.insert(li))
					successfulODUpdate = false;
			}
			if (!successfulODUpdate) affected = 0;*/
			
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

	public static boolean delete(String jobId) throws Exception {
		
		if (ArtDept.loggingEnabled) log.entry("delete (OrderManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "DELETE FROM orders WHERE id = ?";
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			stmt.setString(1, jobId);
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
