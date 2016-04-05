package info.chrismcgee.sky.artdept;

import info.chrismcgee.components.DateManager;
import info.chrismcgee.sky.beans.Day;
import info.chrismcgee.sky.beans.Job;
import info.chrismcgee.sky.beans.OrderDetail;
import info.chrismcgee.sky.enums.PrintType;
import info.chrismcgee.sky.enums.PrintingCompany;
import info.chrismcgee.sky.enums.ScriptType;
import info.chrismcgee.sky.tables.DayManager;
import info.chrismcgee.sky.tables.JobManager;
import info.chrismcgee.sky.tables.OrderDetailManager;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScriptManager {
	
	static final Logger log = LogManager.getLogger(ScriptManager.class.getName());
	private static String artDeptFolder = "/Volumes/ArtDept/ArtDept/";
	private static NumberFormat usFormat = NumberFormat.getIntegerInstance(Locale.US);
	private static DateFormat usDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, Locale.US);

	public static boolean scriptRunner(ScriptType scriptType, String jobNumber, String initials, Job bean, int proofNum, String scriptFolder, String customerServiceRep, boolean creditCard, int shipDays, String wnaPo) {
		
		log.entry("scriptRunner");
		log.debug("Username will be " + System.getenv("USER"));
		
		// A few variables to help get the information we need into a properly-formatted String.
		String pCompany = (bean == null || bean.getPrintingCompany() == null) ? "ACCENTS" : getPrintCompanyString(bean.getPrintingCompany());
		log.debug("Got pCompany: " + pCompany);
		String shipDate = (bean == null || bean.getShipDate() == null) ? "" : DateManager.getDisplayDate(bean.getShipDate());
		log.debug("Got shipDate: " + shipDate);
		String customerName = (bean == null || bean.getCustomerName() == null) ? "" : bean.getCustomerName();
		log.debug("Got customerName: " + customerName);
		String customerPO = (bean == null || bean.getCustomerPO() == null) ? "" : bean.getCustomerPO();
		log.debug("Got customerPO: " + customerPO);
		boolean overruns = (bean == null) ? false : bean.areOverruns();
		log.debug("Got overruns: " + overruns);
		boolean sampleShelf = (bean == null) ? false : bean.isSampleShelfNote();
		log.debug("Got sampleShelf: " + sampleShelf);
		PrintingCompany pCompanyEnum = (bean == null || bean.getPrintingCompany() == null) ? PrintingCompany.AMERICAN_ACCENTS : bean.getPrintingCompany();
		log.debug("Got pCompanyEnum: " + pCompanyEnum.toString());
		
		// Prepare the AppleScript file to be executed.
		log.trace("Prepare the AppleScript file to be executed.");
//		String scriptFolder = artDeptFolder + "Scripts/sky-artdept/";
		File scriptFile;
		String script = null;
		if (scriptType == ScriptType.PROOF)
			scriptFile = new File(scriptFolder + "Proof.applescript");
		else // Output.
			scriptFile = new File(scriptFolder + "Output.applescript");

		log.debug("scriptFolder and scriptFile variables set.");
		
		// Take all of the Job and Order Detail bean's information and join it together
		// into a single String that can be parsed in the Proofing script.
		String orderDetailInfo = "";
		try {
			for (Iterator<OrderDetail> odIterator = bean.getOrderDetailList().iterator(); odIterator.hasNext();) {
				OrderDetail od = (OrderDetail) odIterator.next();
				log.debug("01. Getting Product ID: " + od.getProductId());
				orderDetailInfo += (od.getProductId() == null ? "" : od.getProductId());
				log.debug("02. Getting Product Detail: " + od.getProductDetail());
				orderDetailInfo += "@" + (od.getProductDetail() == null ? "" : od.getProductDetail());
				log.debug("03. Getting Quantity: " + od.getQuantity());
				orderDetailInfo += "@" + (od.getQuantity() == 0 ? "" : usFormat.format(od.getQuantity()));
				log.debug("04. Getting Flags: " + od.getFlags());
				orderDetailInfo += "@" + (od.getFlags() == 0 ? "" : String.valueOf(od.getFlags()));
				log.debug("05. Getting Reorder ID: " + od.getReorderId());
				orderDetailInfo += "@" + (od.getReorderId() == null ? "" : od.getReorderId());
				log.debug("06. Getting Packing Instructions: " + od.getPackingInstructions().replace("\"", "\\\""));
				orderDetailInfo += "@" + (od.getPackingInstructions() == null ? "" : od.getPackingInstructions().replace("\"", "\\\""));
				log.debug("07. Getting Package Quantity: " + od.getPackageQuantity());
				orderDetailInfo += "@" + (od.getPackageQuantity() == null ? "" : od.getPackageQuantity());
				log.debug("08. Getting Case Quantity: " + od.getCaseQuantity());
				orderDetailInfo += "@" + (od.getCaseQuantity() == null ? "" : od.getCaseQuantity());
				log.debug("09. Getting Label Quantity: " + od.getLabelQuantity());
				orderDetailInfo += "@" + (od.getLabelQuantity() == 0 ? "" : usFormat.format(od.getLabelQuantity()));
				log.debug("10. Getting Label Text: " + od.getLabelText().replace("\"", "\\\""));
				orderDetailInfo += "@" + (od.getLabelText() == null ? "" : od.getLabelText().replace("\"", "\\\""));
				if (odIterator.hasNext()) orderDetailInfo += "@"; // Place another separator between groups.
			}
		} catch (NullPointerException err) {
			log.error("Null Pointer to the Order Detail List. Creating one now.");
			orderDetailInfo = "@@@@@@@@@";
		}
		
		// Begin building the String that will be the script we run.
		// The first few lines are just to add in the arguments from the ArtDept class.
		// The rest is read from the file defined above.
		if (scriptType == ScriptType.PROOF)
		{
			script = "set JOrderNum to \"" + jobNumber + "\"\n";
			script += "set JPrintingCompany to \"" + pCompany + "\"\n";
			script += "set JClientName to \"" + customerName + "\"\n";
			script += "set JClientPO to \"" + customerPO + "\"\n";
			script += "set JProofNum to \"" + proofNum + "\"\n";
			script += "set JOverruns to \"" + overruns + "\"\n";
			script += "set JSampleShelf to \"" + sampleShelf + "\"\n";
			script += "set JInitials to \"" + initials + "\"\n";
			script += "set JOrderDetail to \"" + orderDetailInfo + "\"\n";
			script += "set JCustomerServiceRep to \"" + customerServiceRep + "\"\n";
			script += "set JCreditCard to \"" + creditCard + "\"\n";
			script += "set JShipDays to \"" + shipDays + "\"\n";
			script += "set JWnaPo to \"" + wnaPo + "\"\n";
			log.debug("Reading the Proofing script into a string.");
			try {
				script += FileUtils.readFileToString(scriptFile);
			} catch (IOException e) {
				log.error("Could not find the Proofing AppleScript file.", e);
				JOptionPane.showMessageDialog(null, "Could not find the Proofing AppleScript file.  Please verify connection to the server.", "File not found", JOptionPane.ERROR_MESSAGE);
				return false;
//				ConnectionManager.getInstance().close();
//				System.exit(1);
			}
		}
		else // Output.
		{
			{
				script = "set JOrderNum to \"" + jobNumber + "\"\n";
				script += "set JShipDate to \"" + shipDate + "\"\n";
				script += "set JOverruns to \"" + overruns + "\"\n";
				script += "set JInitials to \"" + initials + "\"\n";
				log.debug("Reading the Output script into a string.");
				try {
					script += FileUtils.readFileToString(scriptFile);
				} catch (IOException e) {
					log.error("Could not find the Output AppleScript file.", e);
					JOptionPane.showMessageDialog(null, "Could not find the Output AppleScript file.  Please verify connection to the server.", "File not found", JOptionPane.ERROR_MESSAGE);
					return false;
//					ConnectionManager.getInstance().close();
//					System.exit(1);
				}
			}
		}
		
		// These two lines prepare the scripting engine, ready to run the script.
		log.debug("Setting the script engine.");
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = null;
		try {
			engine = mgr.getEngineByName("AppleScriptEngine");
		} catch (Exception err) {
			log.error("Could not find the scripting engine needed.", err);
		}

		// Add the parameters to the engine so they will be passed to the script.
		if (scriptType == ScriptType.PROOF)
		{
			engine.put("javaOrderNum", jobNumber);
			engine.put("javaPrintCompany", pCompany);
			engine.put("javaClientName", customerName);
			engine.put("javaClientPO", customerPO);
			engine.put("javaProofNum", proofNum);
			engine.put("javaOverruns", overruns);
			engine.put("javaSampleShelf", sampleShelf);
			engine.put("javaInitials", initials);
			engine.put("javaOrderDetails", orderDetailInfo);
			engine.put("javaCustomerServiceRep", customerServiceRep);
			engine.put("javaCreditCard", creditCard);
			engine.put("javaShipDays", shipDays);
			engine.put("javaWnaPo", wnaPo);
		}
		else // Output
		{
			engine.put("javaOrderNum", jobNumber);
			engine.put("javaShipDate", shipDate);
			engine.put("javaOverruns", overruns);
			engine.put("javaInitials", initials);
		}
		
		// Run the script and evaluate the result.
		log.trace("Running the script and evaluating the result.");
		Object result = null;
		try {
			result = engine.eval(script); // Run the script and place the result into an abstract object.
		} catch (ScriptException e) {
			log.error("An error occurred with the script. Line number: " + e.getLineNumber(), e);
			JOptionPane.showMessageDialog(null, "An error occurred with the script.", "Script error / cancel", JOptionPane.ERROR_MESSAGE);
			return false;
//			ConnectionManager.getInstance().close();
//			System.exit(1);
		}
		log.debug(result); // Check that we received the correct information back from the script.
		log.debug("");
		String resultStr = result.toString(); // Convert it to a String.
		if (isInteger(resultStr)) {
			/**
			 * Result possibilities:
			 *  0: Unused. Normally means that everything ran without errors, but since we are expecting an array instead, we will leave this unused for now.
			 *  1: Connection to server not found.
			 *  2: MRAP error returned from Illustrator.
			 *  3: Another type of error returned from Illustrator (besides MRAP).
	  		 *  4: No files with the given order number were found.
			 *  5: Product type (multicolor) does not exist in script.
			 *  6: Tried to create a new "thousands" folder and failed. If problem persists, create the folder manually.
			 *  7: Cancel button was pressed.
			 *  8: Could not find existing files to rename them.
			 *  9: Could not find InDesign template file.
			 * 10: InDesign is crashing / has crashed.
			 * 11: Script could not continue because another InDesign file was open.
			 * 12: Extra files/folders in the job folder.
			 */
			int x = Integer.parseInt(resultStr);
			switch (x) {
			case 0:
				log.trace("The unused '0' error code was returned!");
				return false;
			case 1:
				log.trace("Connection to server not found.");
				return false;
			case 2:
				log.trace("MRAP error returned from Illustrator.");
				return false;
			case 3:
				log.trace("Another type of error returned from Illustrator (besides MRAP).");
				return false;
			case 4:
				log.trace("No files with the given order number were found.");
				return false;
			case 5:
				log.trace("Product type (multicolor) does not exist in script.");
				return false;
			case 6:
				log.trace("Tried to create a new 'thousands' folder and failed. If problem persists, create the folder manually.");
				return false;
			case 7:
				log.trace("Cancel button pressed.");
				return false;
			case 8:
				log.trace("Could not find existing files to rename them.");
				return false;
			case 9:
				log.trace("Could not find InDesign template file.");
				return false;
			case 10:
				log.trace("InDesign is crashing / has crashed.");
				return false;
			case 11:
				log.trace("Script could not continue because another InDesign file was open.");
				return false;
			case 12:
				log.trace("Extra files/folders in the job folder.");
				return false;
			case 100:
				log.trace("Script did not return any info to the Applescript script, so it was undefined.");
				return false;
			default:
				log.trace("Script exited by some other means.");
				return false;
			}
		}
		String[] aResult = resultStr.split("@");  // Convert the String into an Array of Strings.
		String[] aTemp = new String[24]; // This temporary Array will hold the twenty-two items common to each product in an order.
		ArrayList<String[]> aJobDetails = new ArrayList<String[]>(); // Setting up a two-dimensional ArrayList of Arrays.
		for (int i = 0; i < aResult.length; i+=24) { // Go through each block of twenty-two items in the list and add them to the ArrayList.
			aTemp = Arrays.copyOfRange(aResult, i, i+24);
			aJobDetails.add(aTemp);
		}

		// If we got nothing back, then return with a failure condition.
		if (aJobDetails.get(0)[0].length() < 1)
		{
			log.trace("Nothing returned from script. User possibly cancelled it.");
			return log.exit(false);
		}
		
		// Finally, some test sample output.
		log.debug(new Date(Long.parseLong(aJobDetails.get(0)[0], 10)).toString());
//		log.debug(LocalDate.parse(aJobDetails.get(0)[0]).toString("yyyy-MM-dd")); // Ship Date
		log.debug(aJobDetails.get(0)[1]); // Job ID
		log.debug(aJobDetails.get(0)[2]); // Customer Name
		log.debug(usDateFormat.format(new Date(Long.parseLong(aJobDetails.get(0)[3], 10))));
//		log.debug(LocalDateTime.parse(aJobDetails.get(0)[3]).toString("yyyy-MM-dd HH:mm:ss")); // Proof Date / Spec Date
		log.debug(aJobDetails.get(0)[4]); // Product ID
		log.debug(aJobDetails.get(0)[5]); // Product Detail
		log.debug(aJobDetails.get(0)[6]); // Print Type (See Output script or database table schema for description.)
		log.debug(aJobDetails.get(0)[7]); // Number of Colors
		log.debug(aJobDetails.get(0)[8]); // Quantity
		log.debug(aJobDetails.get(0)[9]); // Print Company (Accents / Cabin / Yacht)
		log.debug(aJobDetails.get(0)[10]); // Proof Number
		log.debug(aJobDetails.get(0)[11]); // Overruns boolean
		log.debug(aJobDetails.get(0)[12]); // Customer PO
		log.debug(aJobDetails.get(0)[13]); // Thumbnail filename
		log.debug(aJobDetails.get(0)[14]); // Flags
		log.debug(aJobDetails.get(0)[15]); // Reorder ID
		log.debug(aJobDetails.get(0)[16]); // Packing / Labeling Info
		log.debug(aJobDetails.get(0)[17]); // Package Quantity
		log.debug(aJobDetails.get(0)[18]); // Case Quantity
		log.debug(aJobDetails.get(0)[19]); // Label Quantity
		log.debug(aJobDetails.get(0)[20]); // Label Text
		log.debug(aJobDetails.get(0)[21]); // Sample Shelf Note boolean
		log.debug(aJobDetails.get(0)[22]); // Proofer's Initials
		log.debug(aJobDetails.get(0)[23]); // Outputter's Initials
		log.debug("");


		// Now to insert the returned data into the database.
		// First, place the day's info into the Day table.  This should only insert one row.
		log.trace("First, place the day's info into the Day table.  This should only insert one row.");
		Day dayBean = new Day(new Date(Long.parseLong(aJobDetails.get(0)[0], 10)));
//		dayBean.setDate(Date.valueOf(LocalDate.parse(aJobDetails.get(0)[0]).toString("yyyy-MM-dd")));
		dayBean.setDate(new Date(Long.parseLong(aJobDetails.get(0)[0])));
		
		boolean successfulDayInsert = false;
		try {
			if (!DayManager.dayExists(dayBean.getDate())) {
				successfulDayInsert = DayManager.insert(dayBean); // Insert the day info into the Job table. 
			} else {
				log.trace("" + dayBean.getDate() + " already exists in the Day table. Setting it to INCOMPLETE.");
				DayManager.setNotCompleted(dayBean.getDate());
			}
		} catch (Exception e) {
			log.error("Error inserting data into database", e);
			JOptionPane.showMessageDialog(null, "Error inserting data into database", "Database error", JOptionPane.ERROR_MESSAGE);
			return false;
//			ConnectionManager.getInstance().close();
//			System.exit(1);
		}
		
		if (successfulDayInsert) {
			log.debug("New row with date of " + dayBean.getDate() + " was inserted!");
		}
		
		
		// Next, set the Job info bean.
		log.trace("Next, set the Job info bean.");
		if (bean == null)
		{
			log.debug("Bean was null; creating new Job bean.");
			bean = new Job();
/*			customerName = aJobDetails.get(0)[2];
			customerPO = aJobDetails.get(0)[12];*/
		}
		customerName = aJobDetails.get(0)[2];
		customerPO = aJobDetails.get(0)[12];
/*		if (bean.getCustomerName() == null) customerName = aJobDetails.get(0)[2];
		if (bean.getCustomerPO() == null) customerPO = aJobDetails.get(0)[12];*/
		
		bean.setJobId(aJobDetails.get(0)[1]);
//		bean.setProofSpecDate(Timestamp.valueOf(LocalDateTime.parse(aJobDetails.get(0)[3]).toString("yyyy-MM-dd HH:mm:ss")));
		bean.setProofSpecDate(new Date(Long.parseLong(aJobDetails.get(0)[3], 10)));
		if (scriptType == ScriptType.OUTPUT)
		{
			bean.setCustomerName(customerName);
			bean.setCustomerPO(customerPO);
			bean.setPrintingCompany(pCompanyEnum);
			bean.setShipDate(new Date(Long.parseLong(aJobDetails.get(0)[0], 10)));
			bean.setOverruns(areOverruns(aJobDetails.get(0)[11]));
			bean.setSigOutput(aJobDetails.get(0)[23]);
		}
		else // Proofing
		{
			bean.setCustomerName(aJobDetails.get(0)[2]);
			bean.setCustomerPO(aJobDetails.get(0)[12]);
			bean.setPrintingCompany(PrintingCompany.getPrintingCompany(Integer.parseInt(aJobDetails.get(0)[9])));
			bean.setShipDate(DateManager.localDateToSqlDate(DateManager.PROOF_DATE));
			bean.setOverruns(overruns);
			bean.setSampleShelfNote(false); // Default to false. Will test for the note shortly to possibly change this.
			bean.setSigProof(aJobDetails.get(0)[22]);
		}
		
		// As part of setting the Job bean, set its OrderDetail List with OrderDetail beans.
		log.trace("As part of setting the Job bean, set its OrderDetail List with OrderDetail beans.");
		Pattern prProof = Pattern.compile("Proof|NaN", Pattern.CASE_INSENSITIVE);
		List<OrderDetail> odList = new ArrayList<OrderDetail>();
		boolean allDigitalProofs = true; // This boolean will store whether or not all of the items
										 // in a multi-item proof are digital. If so, it should not be added to the database.
		for (int i = 0; i < aJobDetails.size(); i++) {
			// If ANY of the items has the "Sample Shelf" note, then set the "Sample Shelf" boolean for the entire job.
			if (aJobDetails.get(i)[21].equalsIgnoreCase("true")) {
				log.debug("Sample Shelf String returned from InDesign: " + aJobDetails.get(i)[21] + ". Setting Sample Shelf Note to true.");
				bean.setSampleShelfNote(true);
			}
			OrderDetail detailBean = new OrderDetail();
			detailBean.setOrderId(aJobDetails.get(i)[1]);
			detailBean.setProductId(aJobDetails.get(i)[4]);
			detailBean.setProductDetail(aJobDetails.get(i)[5]);
			detailBean.setThumbnail(aJobDetails.get(i)[13]);
			if (scriptType == ScriptType.PROOF)
			{
				detailBean.setProofNum(Integer.parseInt(aJobDetails.get(i)[10]));
				detailBean.setProofDate(new Date(Long.parseLong(aJobDetails.get(i)[3], 10)));
				detailBean.setFlags(Integer.parseInt(aJobDetails.get(i)[14]));
				detailBean.setReorderId(aJobDetails.get(i)[15]);
				detailBean.setPackingInstructions(aJobDetails.get(i)[16]);
				detailBean.setPackageQuantity(aJobDetails.get(i)[17]);
				detailBean.setCaseQuantity(aJobDetails.get(i)[18]);
				try { // Since the Label Quantity field could be blank
					detailBean.setLabelQuantity(Integer.parseInt(aJobDetails.get(i)[19]));
				} catch (NumberFormatException err) {
					log.error("Could not parse Label Quantity into an integer. Setting it to 0 instead.");
					detailBean.setLabelQuantity(0);
				}
				detailBean.setLabelText(aJobDetails.get(i)[20]);
			}
			else // Output
			{
				detailBean.setProofNum(proofNum);
				detailBean.setFlags((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? 0 : bean.getOrderDetailList().get(i).getFlags());
				detailBean.setReorderId((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "" : bean.getOrderDetailList().get(i).getReorderId());
				detailBean.setPackingInstructions((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "" : bean.getOrderDetailList().get(i).getPackingInstructions());
				detailBean.setPackageQuantity((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "0" : bean.getOrderDetailList().get(i).getPackageQuantity());
				detailBean.setCaseQuantity((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "0" : bean.getOrderDetailList().get(i).getCaseQuantity());
				detailBean.setLabelQuantity((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? 0 : bean.getOrderDetailList().get(i).getLabelQuantity());
				detailBean.setLabelText((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "" : bean.getOrderDetailList().get(i).getLabelText());
			}
			try {
				String correctedQuantity = aJobDetails.get(i)[8];
				Matcher matcher = prProof.matcher(correctedQuantity);
				if (matcher.find()) correctedQuantity = "1"; // If the word "proof" (case insensitive) is found for the quantity, change it to "1".
				
				detailBean.setQuantity(NumberFormat.getNumberInstance().parse(correctedQuantity).intValue());
				// Check that quantity for 0 (Digital Proof) and set the boolean accordingly.
				if (detailBean.getQuantity() > 0) allDigitalProofs = false;
				
				detailBean.setNumColors(Integer.parseInt(aJobDetails.get(i)[7]));
				detailBean.setPrintType(PrintType.getPrintType(Integer.parseInt(aJobDetails.get(i)[6])));
			} catch (ParseException e) {
				log.error("Could not parse the quantity, number of colors, or print type.", e);
				JOptionPane.showMessageDialog(null, "Could not parse the quantity, number of colors, or print type.  Please fix and run again.", "Parse error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			odList.add(detailBean);
			
		}

		// Now check that boolean to see if all of the items are Digital Proofs only.
		// If so, just immediately return without putting anything into the database.
		log.trace("All of the items in this job are Digital Proofs only: " + allDigitalProofs);
		if (allDigitalProofs) return true;
		
		// Place the job info into the Job table.  This should only insert one row.
		// If the job already exists, though, just update it.
		boolean successfulJobInsert = false;
		try {
			if (JobManager.getRow(bean.getJobId()) == null)
			{
				log.debug("INSERTing a new Job entry into the Job table.");
				successfulJobInsert = JobManager.insert(bean); // Insert the job & order detail in the database.
			}
			else
			{
				log.debug("UPDATing the existing Job in the Job table.");
				successfulJobInsert = JobManager.update(bean); // Update the job & order detail in the database.
			}
		} catch (Exception e) {
			log.error("Error inserting/updating Job data in the database", e);
			JOptionPane.showMessageDialog(null, "Error inserting Job data into database", "Database error", JOptionPane.ERROR_MESSAGE);
			return log.exit(false);
		}

		if (successfulJobInsert) {
			log.debug("Job id " + bean.getJobId() + " was inserted/updated!");
		}
		else
		{
			log.debug("One or more items did NOT make it into the database.");
			return false;
		}
		
		
		// This next section should only run if there are existing OrderDetail items in the database.
		boolean successfulODInsert = false;
		if (bean.getOrderDetailList() != null)
		{
			// This is where things get tricky. We need to get a list of the .INDD files in the job directory as an ArrayList of Strings first.
			// We then compare this List of Strings to the incoming OrderDetail List and pop out any matches from the List of Strings.
			// What we're left with is a list of only those items that have NOT been updated / added to the job.
			List<File> protectionList = getProtectionList(jobNumber, odList);
			
			// We want to protect these items and not update/delete them from the database. To that end, we now compare this list
			// to the existing list of OrderDetail items that are in the database. Matches are popped out of that existing list,
			// so that all that remains should be only the items to be updated or deleted.
			List<OrderDetail> modifiedODList = protectODs(bean.getOrderDetailList(), protectionList);
			
			// Now we compare our two Lists of OrderDetail items and add/update/remove, depending upon how many are in each list.
			successfulODInsert = updateTables(odList, modifiedODList);
			
			// As a final step, check to see if there are some items in the protectionList that do NOT exist in the table.
			// This can happen if InDesign crashed mid-way through a multi-item order or some such.
			// If this is the case, then go ahead and add dummy entries to the database, so at least SOMETHING is there.
			addDummyEntries(bean.getOrderDetailList(), protectionList);
		}
		else
		{
			bean.setOrderDetailList(odList);
			addDummyEntries(bean.getOrderDetailList(), bean.getJobId());
			try {
				for (OrderDetail orderDetail : bean.getOrderDetailList())
				{
					log.debug("INSERTing a new OrderDetail entry into the Job table.");
					successfulODInsert = OrderDetailManager.insert(orderDetail); // Insert the order detail in the database.
				}
			} catch (Exception err) {
				log.error("Error inserting OrderDetail data in the database", err);
				JOptionPane.showMessageDialog(null, "Error inserting Order Detail data into database", "Database error", JOptionPane.ERROR_MESSAGE);
				return log.exit(false);
			}
		}
		
		if (successfulODInsert) {
			log.debug("At least one row was inserted/updated!");
		}
		else
		{
			log.debug("One or more items did NOT make it into the database.");
			return false;
		}

		
		log.trace("Completed main section.");
		
//		ConnectionManager.getInstance().close();

		return log.exit(true);
	}

	
	private static boolean updateTables(List<OrderDetail> incomingList, List<OrderDetail> existingList)
	{
		log.entry("updateTables (ScriptManager)");

/*		if (incomingList.size() == 0)
		{
			if (existingList.size() == 0)
			{
				log.exit("Both lists are empty!");
				return;
			}
			deleteItems(existingList);
			log.exit("Items removed from database.");
			return;
		}
		if (existingList.size() == 0)
		{
			insertItems(incomingList);
			log.exit("Items added to database.");
			return;
		}*/
		// Last part here occurs if there are items in both lists. Update until one of them drops to 0.
		Iterator<OrderDetail> incomingIterator = incomingList.iterator();
		Iterator<OrderDetail> existingIterator = existingList.iterator();
		
		log.debug("Incoming list has " + incomingList.size() + " elements.");
		log.debug("Existing list has " + existingList.size() + " elements.");

		try {
			while (incomingIterator.hasNext() && existingIterator.hasNext()) {
				log.debug("Updating an existing OD item.");
				OrderDetail incomingOD = (OrderDetail) incomingIterator.next();
				OrderDetail existingOD = (OrderDetail) existingIterator.next();
				incomingOD.setId(existingOD.getId());
				if (incomingOD.getProofDate() == null)
					incomingOD.setProofDate(existingOD.getProofDate());
				OrderDetailManager.update(incomingOD);
			}
//			incomingIterator = incomingList.iterator();
			while (incomingIterator.hasNext()) {
				log.debug("Inserting a new OD item.");
				OrderDetail incomingOD = (OrderDetail) incomingIterator.next();
				OrderDetailManager.insert(incomingOD);
			}
//			existingIterator = existingList.iterator();
			while (existingIterator.hasNext()) {
				log.debug("Deleting an existing OD item.");
				OrderDetail existingOD = (OrderDetail) existingIterator.next();
				OrderDetailManager.delete(existingOD.getId());
			}
		} catch (Exception err) {
			log.error("Error while attempting to update/insert/delete data in database.", err);
			return log.exit(false);
		}
		
		return log.exit(true);
	}

/*	private static void insertItems(List<OrderDetail> odList)
	{
		log.entry("insertItems (ScriptManager)");

		for (OrderDetail orderDetail : odList) {
			try {
				OrderDetailManager.insert(orderDetail);
			} catch (Exception err) {
				log.error("Exception when attempting to insert an OrderDetail item into the database.", err);
				return;
			}
		}
		log.exit("insertItems (ScriptManager)");
	}

	private static void deleteItems(List<OrderDetail> odList)
	{
		log.entry("deleteItems (ScriptManager)");

		for (OrderDetail orderDetail : odList) {
			try {
				OrderDetailManager.delete(orderDetail.getId());
			} catch (Exception err) {
				log.error("Exception when attempting to delete an item from the database.", err);
				return;
			}
		}
		log.exit("deleteItems (ScriptManager)");
	}*/

/*	private static List<OrderDetail> cloneList(List<OrderDetail> list)
	{
		List<OrderDetail> clone = new ArrayList<OrderDetail>(list.size());
		for (OrderDetail od : list) clone.add(od);
		return clone;
	}*/
	
	private static List<OrderDetail> protectODs(List<OrderDetail> existingList, List<File> protectionList)
	{
		log.entry("protectODs (ScriptManager)");

		List<OrderDetail> trimmedList = new ArrayList<OrderDetail>();
		trimmedList.addAll(existingList);
//		Collections.copy(trimmedList, existingList);
//		trimmedList = (ArrayList) ((ArrayList) existingList).clone();
		Iterator<File> fileIterator = protectionList.iterator();
		Iterator<OrderDetail> odIterator;
		String itemDetail = "";
		
		// Since it is entirely possible for an item in the protectionList to NOT exist in the existingList,
		// when we find a match, we pop that item out of BOTH lists. If any files remain in the protectionList afterward,
		// then we can add 'dummy' entries into the database, so at least there's SOMETHING in there.
		while (fileIterator.hasNext()) {
			File file = (File) fileIterator.next();
			itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
			odIterator = trimmedList.iterator();
			while (odIterator.hasNext()) {
				OrderDetail orderDetail = (OrderDetail) odIterator.next();
				log.debug("Comparing " + orderDetail.getProductDetail() + " (existing item in db) to " + itemDetail + " (item in protection list).");
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals("")) || orderDetail.getProductDetail().equals(itemDetail))
				{
					log.debug("Found a match. Removing from trimmed list. (Item protected)");
					odIterator.remove();
//					fileIterator.remove();
					break;
				}
			}
		}
				
		return log.exit(trimmedList);
	}

	private static void addDummyEntries(List<OrderDetail> existingList, List<File> protectionList)
	{
		log.entry("addDummyEntries (ScriptManager)");
		
		Iterator<File> fileIterator = protectionList.iterator();
		Iterator<OrderDetail> odIterator;
		String itemDetail = "";
		
		// Since it is entirely possible for an item in the protectionList to NOT exist in the existingList,
		// when we find a match, we pop that item out of BOTH lists. If any files remain in the protectionList afterward,
		// then we can add 'dummy' entries into the database, so at least there's SOMETHING in there.
		while (fileIterator.hasNext()) {
			File file = (File) fileIterator.next();
			itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
			odIterator = existingList.iterator();
			while (odIterator.hasNext()) {
				OrderDetail orderDetail = (OrderDetail) odIterator.next();
				log.debug("Comparing " + orderDetail.getProductDetail() + " to " + itemDetail);
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
						|| itemDetail == null
						|| orderDetail.getProductDetail().equals(itemDetail)
						|| StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
						|| StringUtils.containsIgnoreCase(itemDetail, "Carousel"))
				{
					log.debug("Found a match. Removing from the protectionList. (Will NOT be added as a dummy item.)");
					fileIterator.remove();
					break;
				}
			}
		}
	
		if (protectionList.size() > 0)
		{
			for (File file : protectionList) {
				OrderDetail bean = new OrderDetail();
				bean.setOrderId(StringUtils.substringBefore(file.getName(), "_"));
				bean.setProductId("");
				bean.setProductDetail(StringUtils.substringBetween(file.getName(), "_", "."));
				bean.setPrintType(PrintType.PAD);
				bean.setNumColors(0);
				bean.setQuantity(0);
//				bean.setProofDate(new Timestamp(DateTimeUtils.currentTimeMillis()));
				bean.setProofDate(new Date(new java.util.Date().getTime()));
				
				try {
					OrderDetailManager.insert(bean);
				} catch (Exception err) {
					log.error("Exception when trying to insert a dummy OrderDetail into the database.", err);
				}
			}
		}
		
		log.exit("addDummyEntries (ScriptManager)");
	}
	
	
	private static void addDummyEntries(List<OrderDetail> incomingList, String jobNumber)
	{
		log.entry("addDummyEntries (when bean doesn't exist yet) (ScriptManager)");
		
		String jobPrefix = jobNumber.substring(0, 3);
		String jobFolderBeginning = artDeptFolder + "JOBS/" + jobPrefix + "000-" + jobPrefix + "999/";
		File searchFolder = new File(jobFolderBeginning + jobNumber + "/");
		if (!searchFolder.exists()) searchFolder = new File(jobFolderBeginning + jobNumber + " Folder/");
		
		String[] inDesign = {"indd"};
		List<File> fileList = (List<File>) FileUtils.listFiles(searchFolder, inDesign, true);
		Iterator<File> fileIterator = fileList.iterator();
		Iterator<OrderDetail> odIterator;
		String itemDetail = "";
		
		// This is similar to the previous addDummyEntries method, but this one is called when no job exists already
		// in the database.
		while (fileIterator.hasNext()) {
			File file = (File) fileIterator.next();
			itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
			odIterator = incomingList.iterator();
			while (odIterator.hasNext()) {
				OrderDetail orderDetail = (OrderDetail) odIterator.next();
				log.debug("Comparing " + orderDetail.getProductDetail() + " (incoming item) to " + itemDetail + " (existing file in directory).");
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
						|| itemDetail == null
						|| orderDetail.getProductDetail().equals(itemDetail)
						|| StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
						|| StringUtils.containsIgnoreCase(itemDetail, "Carousel"))
				{
					log.debug("Found a match. Removing from the existing File list. (Will not be added as a dummy item to the database.)");
					fileIterator.remove();
					break;
				}
			}
		}
/*		Iterator<File> fileIterator = protectionList.iterator();
		Iterator<OrderDetail> odIterator;
		String itemDetail = "";
		
		// Since it is entirely possible for an item in the protectionList to NOT exist in the existingList,
		// when we find a match, we pop that item out of BOTH lists. If any files remain in the protectionList afterward,
		// then we can add 'dummy' entries into the database, so at least there's SOMETHING in there.
		while (fileIterator.hasNext()) {
			File file = (File) fileIterator.next();
			itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
			odIterator = existingList.iterator();
			while (odIterator.hasNext()) {
				OrderDetail orderDetail = (OrderDetail) odIterator.next();
				log.debug("Comparing " + orderDetail.getProductDetail() + " to " + itemDetail);
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
						|| itemDetail == null
						|| orderDetail.getProductDetail().equals(itemDetail)
						|| StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
						|| StringUtils.containsIgnoreCase(itemDetail, "Carousel"))
				{
					log.debug("Found a match. Removing from the protectionList. (Will NOT be added as a dummy item.)");
					fileIterator.remove();
					break;
				}
			}
		}*/
		
		if (fileList.size() > 0)
		{
			for (File file : fileList) {
				OrderDetail bean = new OrderDetail();
				bean.setOrderId(StringUtils.substringBefore(file.getName(), "_"));
				bean.setProductId("");
				bean.setProductDetail(StringUtils.substringBetween(file.getName(), "_", "."));
				bean.setPrintType(PrintType.PAD);
				bean.setNumColors(0);
				bean.setQuantity(0);
//				bean.setProofDate(new Timestamp(DateTimeUtils.currentTimeMillis()));
				bean.setProofDate(new Date(new java.util.Date().getTime()));
				
				try {
					OrderDetailManager.insert(bean);
				} catch (Exception err) {
					log.error("Exception when trying to insert a dummy OrderDetail into the database.", err);
				}
			}
		}
		
		log.exit("addDummyEntries (ScriptManager)");
	}


	/**
	 * @param jobNumber
	 * @param odList
	 * @return List<File> A list of files that will be protected from deletion/updating.
	 * 	These are files that exist in the folder but are NOT in the incoming list of jobs.
	 */
	private static List<File> getProtectionList(String jobNumber, List<OrderDetail> odList)
	{
		log.entry("getProtectionList (ScriptManager)");

		String jobPrefix = jobNumber.substring(0, 3);
		String jobFolderBeginning = artDeptFolder + "JOBS/" + jobPrefix + "000-" + jobPrefix + "999/";
		File searchFolder = new File(jobFolderBeginning + jobNumber + "/");
		if (!searchFolder.exists()) searchFolder = new File(jobFolderBeginning + jobNumber + " Folder/");
		
		String[] inDesign = {"indd"};
		List<File> fileList = (List<File>) FileUtils.listFiles(searchFolder, inDesign, true);
		Iterator<File> fileIterator;
		String itemDetail = "";
		
		for (OrderDetail od : odList)
		{
			fileIterator = fileList.iterator();
			while (fileIterator.hasNext()) {
				File file = (File) fileIterator.next();
				itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
				if (itemDetail == null) itemDetail = "";
				log.debug("Comparing " + od.getProductDetail() + " (incoming item) to " + itemDetail + " (existing file in directory).");
				if (itemDetail.equals(od.getProductDetail()) || itemDetail.equals("MiniPad") || itemDetail.equals("Carousel"))
				{
					log.debug("Found a match. Removing from the protectionList. (Will be deleted/updated later.)");
					fileIterator.remove();
//					break;
				}
			}
		}
		
		return log.exit(fileList);
	}

	private static String getPrintCompanyString (PrintingCompany printingCompany)
	{
		return StringUtils.substringBefore(StringUtils.substringAfter(printingCompany.toString(), "_"), "_");
	}
	
	public static boolean areOverruns (String str)
	{
		if (str.equalsIgnoreCase("true")) return true;
		return false;
	}

	private static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
	
}
