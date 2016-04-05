package info.chrismcgee.sky.tables;

import info.chrismcgee.sky.beans.Job;
import info.chrismcgee.sky.beans.OrderDetail;
import info.chrismcgee.sky.enums.PrintType;
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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JobManager {
	
	private static Connection conn;
	static final Logger log = LogManager.getLogger(JobManager.class.getName());
	
	
	public static Job getRow(String jobId) throws SQLException {
		
		log.entry("getRow (JobManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT * FROM Job WHERE job_id = ?";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setString(1, jobId);
			rs = stmt.executeQuery();
			
			
			if (rs.next()) {
				Job bean = new Job();
				bean.setShipDate(rs.getDate("ship_date"));
				bean.setJobId(jobId);
				bean.setCustomerName(rs.getString("customer_name"));
				bean.setCustomerPO(rs.getString("customer_po"));
				bean.setProofSpecDate(new Date(rs.getTimestamp("proof_spec_date").getTime()));
				bean.setJobCompleted(rs.getTimestamp("job_completed") == null ? null : new Date(rs.getTimestamp("job_completed").getTime()));
				bean.setPrintingCompany(PrintingCompany.getPrintingCompany(rs.getInt("printing_company")));
				bean.setOverruns(rs.getBoolean("overruns"));
				bean.setSampleShelfNote(rs.getBoolean("sample_shelf_note"));
				bean.setSigProof(rs.getString("sig_proof"));
				bean.setSigOutput(rs.getString("sig_output"));
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
	
	public static Job getJob (String jobId) throws SQLException
	{
		log.entry("getJob (JobManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "SELECT ship_date, job_id, customer_name, customer_po, proof_spec_date, "
				+ "job_completed, printing_company, overruns, sample_shelf_note, sig_proof, sig_output, "
				+ "id, product_id, product_detail, print_type, num_colors, quantity, "
				+ "item_completed, proof_num, proof_date, thumbnail, flags, reorder_id, "
				+ "packing_instructions, package_quantity, case_quantity, label_quantity, label_text "
				+ "FROM Job AS j "
				+ "JOIN OrderDetail AS o "
				+ "ON j.job_id = o.order_id "
				+ "WHERE j.job_id = ? ";
		ResultSet rs = null;
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			stmt.setString(1, jobId);
			rs = stmt.executeQuery();
			
			if (rs.next())
			{
				Job bean = new Job();
				bean.setShipDate(rs.getDate("ship_date"));
				bean.setJobId(jobId);
				bean.setCustomerName(rs.getString("customer_name"));
				bean.setCustomerPO(rs.getString("customer_po"));
				bean.setProofSpecDate(new Date(rs.getTimestamp("proof_spec_date").getTime()));
				log.debug("In the middle of getting a Job.");
				log.debug("Job_Completed: " + rs.getTimestamp("job_completed"));
				bean.setJobCompleted(rs.getTimestamp("job_completed") == null ? null : new Date(rs.getTimestamp("job_completed").getTime()));
				bean.setPrintingCompany(PrintingCompany.getPrintingCompany(rs.getInt("printing_company")));
				bean.setOverruns(rs.getBoolean("overruns"));
				bean.setSampleShelfNote(rs.getBoolean("sample_shelf_note"));
				bean.setSigProof(rs.getString("sig_proof"));
				bean.setSigOutput(rs.getString("sig_output"));
				List<OrderDetail> odList = new ArrayList<OrderDetail>();
				do {
					OrderDetail od = new OrderDetail();
					od.setId(rs.getInt("id"));
					od.setOrderId(jobId);
					od.setProductId(rs.getString("product_id"));
					od.setProductDetail(rs.getString("product_detail"));
					od.setPrintType(PrintType.getPrintType(rs.getInt("print_type")));
					od.setNumColors(rs.getInt("num_colors"));
					od.setQuantity(rs.getInt("quantity"));
					od.setItemCompleted(rs.getTimestamp("item_completed") == null ? null : new Date(rs.getTimestamp("item_completed").getTime()));
					od.setProofNum(rs.getInt("proof_num"));
					od.setProofDate(rs.getTimestamp("proof_date") == null ? null : new Date(rs.getTimestamp("proof_date").getTime()));
					od.setThumbnail(rs.getString("thumbnail"));
					od.setFlags(rs.getInt("flags"));
					od.setReorderId(rs.getString("reorder_id"));
					od.setPackingInstructions(rs.getString("packing_instructions"));
					od.setPackageQuantity(rs.getString("package_quantity"));
					od.setCaseQuantity(rs.getString("case_quantity"));
					od.setLabelQuantity(rs.getInt("label_quantity"));
					od.setLabelText(rs.getString("label_text"));
					odList.add(od);
				} while (rs.next());
				bean.setOrderDetailList(odList);

				return log.exit(bean);
			}
			else
			{
				return log.exit(null);
			}
		} catch (SQLException e) {
			log.error(e);
			return log.exit(null);
		} finally {
			if (rs != null) rs.close();
		}
	}

	public static boolean insert(Job bean) throws Exception {
		
		log.entry("insert (JobManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO Job (ship_date, job_id, customer_name, customer_po, "
				+ "proof_spec_date, job_completed, printing_company, overruns, sample_shelf_note, "
				+ "sig_proof, sig_output) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				){
			
			stmt.setDate(1, bean.getShipDate());
			stmt.setString(2, bean.getJobId());
			stmt.setString(3, bean.getCustomerName());
			stmt.setString(4, bean.getCustomerPO());
			stmt.setTimestamp(5, new Timestamp(bean.getProofSpecDate().getTime()));
			stmt.setTimestamp(6, bean.getJobCompleted() == null ? null : new Timestamp(bean.getJobCompleted().getTime()));
			stmt.setInt(7, bean.getPrintingCompany().getValue());
			stmt.setBoolean(8, bean.areOverruns());
			stmt.setBoolean(9, bean.isSampleShelfNote());
			stmt.setString(10, bean.getSigProof());
			stmt.setString(11, bean.getSigOutput());
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			log.error(e);
			return log.exit(false);
		}

/*		// Also insert all of the Job's OrderDetail items into its table.
		for (OrderDetail od : bean.getOrderDetailList()) {
			OrderDetailManager.insert(od);
		}*/
		
		// And finally, since an order was ADDED to the day (not UPDATED),
		// The day is no longer completed.
		DayManager.setNotCompleted(bean.getShipDate());
		
		return log.exit(true);
	}
		
	public static boolean update(Job bean) throws Exception {
		
		log.entry("update (JobManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql =
				"UPDATE Job SET ship_date = ?, customer_name = ?, customer_po = ?, "
				+ "proof_spec_date = ?, job_completed = ?, printing_company = ?, "
				+ "overruns = ?, sample_shelf_note = ?, sig_proof = ?, sig_output = ? "
				+ "WHERE job_id = ?";
		
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			stmt.setDate(1, bean.getShipDate());
			stmt.setString(2, bean.getCustomerName());
			stmt.setString(3, bean.getCustomerPO());
			stmt.setTimestamp(4, new Timestamp(bean.getProofSpecDate().getTime()));
			stmt.setTimestamp(5, bean.getJobCompleted() == null ? null : new Timestamp(bean.getJobCompleted().getTime()));
			stmt.setInt(6, bean.getPrintingCompany().getValue());
			stmt.setBoolean(7, bean.areOverruns());
			stmt.setBoolean(8, bean.isSampleShelfNote());
			stmt.setString(9, bean.getSigProof());
			stmt.setString(10, bean.getSigOutput());
			stmt.setString(11, bean.getJobId());
			
			int affected = stmt.executeUpdate();
			
/*			// Now also update the Job's OrderDetail items in its table.
			boolean successfulODUpdate = true;
			for (OrderDetail od : bean.getOrderDetailList())
			{
				log.trace("Inserting the OrderDetail table after updating the Job table.");
				if (!OrderDetailManager.insert(od))
					successfulODUpdate = false;
			}
			if (!successfulODUpdate) affected = 0;*/
			
			if (affected == 1) {
				return log.exit(true);
			} else {
				return log.exit(false);
			}
			
		} catch (SQLException e) {
			log.error(e);
			return log.exit(false);
		}
		
	}

	public static boolean delete(String jobId) throws Exception {
		
		log.entry("delete (JobManager)");
		
		conn = ConnectionManager.getInstance().getConnection();
		String sql = "DELETE FROM Job WHERE job_id = ?";
		try (
				PreparedStatement stmt = conn.prepareStatement(sql);
				){
			
			stmt.setString(1, jobId);
			int affected = stmt.executeUpdate();
			
			if (affected == 1) {
				return log.exit(true);
			} else {
				return log.exit(false);
			}
			
		} catch (SQLException e) {
			log.error(e);
			 return log.exit(false);
		}
		
	}

}
