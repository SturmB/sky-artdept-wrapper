package info.chrismcgee.sky.artdept;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import info.chrismcgee.components.DateManager;
import info.chrismcgee.sky.beans.Artwork;
import info.chrismcgee.sky.beans.Day;
import info.chrismcgee.sky.beans.Job;
import info.chrismcgee.sky.beans.OrderDetail;
import info.chrismcgee.sky.enums.PrintType;
import info.chrismcgee.sky.enums.PrintingCompany;
import info.chrismcgee.sky.enums.ScriptType;
import info.chrismcgee.sky.tables.ArtworkManager;
import info.chrismcgee.sky.tables.DayManager;
import info.chrismcgee.sky.tables.JobManager;
import info.chrismcgee.sky.tables.OrderDetailManager;

public class ScriptManager {
	
	static final Logger log = LogManager.getLogger(ScriptManager.class.getName());
	private static String artDeptFolder = "/Volumes/ArtDept/ArtDept/";
//	private static NumberFormat usFormat = NumberFormat.getIntegerInstance(Locale.US);
//	private static DateFormat usDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, Locale.US);

	public static boolean scriptRunner(ScriptType scriptType, String jobNumber, String initials, Job bean, int proofNum, String scriptFolder, String customerServiceRep, boolean creditCard, int shipDays, String wnaPo) {
		
		if (ArtDept.loggingEnabled) log.entry("scriptRunner");
		if (ArtDept.loggingEnabled) log.debug("Username will be " + System.getenv("USER"));
		
		// A few variables to help get the information we need into a properly-formatted String.
		String pCompany = (bean == null || bean.getPrintingCompany() == null) ? "ACCENTS" : getPrintCompanyString(bean.getPrintingCompany());
		if (ArtDept.loggingEnabled) log.debug("Got pCompany: " + pCompany);
		String shipDate = (bean == null || bean.getShipDate() == null) ? "" : DateManager.getDisplayDate(bean.getShipDate());
		if (ArtDept.loggingEnabled) log.debug("Got shipDate: " + shipDate);
		String customerName = (bean == null || bean.getCustomerName() == null) ? "" : bean.getCustomerName();
		if (ArtDept.loggingEnabled) log.debug("Got customerName: " + customerName);
		String customerPO = (bean == null || bean.getCustomerPO() == null) ? "" : bean.getCustomerPO();
		if (ArtDept.loggingEnabled) log.debug("Got customerPO: " + customerPO);
		boolean overruns = (bean == null) ? false : bean.isOverruns();
		if (ArtDept.loggingEnabled) log.debug("Got overruns: " + overruns);
		boolean sampleShelf = (bean == null) ? false : bean.isSampleShelfNote();
		if (ArtDept.loggingEnabled) log.debug("Got sampleShelf: " + sampleShelf);
		PrintingCompany pCompanyEnum = (bean == null || bean.getPrintingCompany() == null) ? PrintingCompany.AMERICAN_ACCENTS : bean.getPrintingCompany();
		if (ArtDept.loggingEnabled) log.debug("Got pCompanyEnum: " + pCompanyEnum.toString());
		
		// Prepare the AppleScript file to be executed.
		if (ArtDept.loggingEnabled) log.trace("Prepare the AppleScript file to be executed.");
//		String scriptFolder = artDeptFolder + "Scripts/sky-artdept/";
		File scriptFile;
		String script = null;
		if (scriptType == ScriptType.PROOF)
			scriptFile = new File(scriptFolder + "Proof.applescript");
		else // Output.
			scriptFile = new File(scriptFolder + "Output.applescript");

		if (ArtDept.loggingEnabled) log.debug("scriptFolder and scriptFile variables set.");
		
		// Take all of the Job and Order Detail bean's information and join it together
		// into a single String that can be parsed in the Proofing script.
/*		String orderDetailInfo = "";
		try {
			for (Iterator<OrderDetail> odIterator = bean.getOrderDetailList().iterator(); odIterator.hasNext();) {
				OrderDetail od = (OrderDetail) odIterator.next();
				if (ArtDept.loggingEnabled) log.debug("01. Getting Product ID: " + od.getProductId());
				orderDetailInfo += (od.getProductId() == null ? "" : od.getProductId());
				if (ArtDept.loggingEnabled) log.debug("02. Getting Product Detail: " + od.getProductDetail());
				orderDetailInfo += "@" + (od.getProductDetail() == null ? "" : od.getProductDetail());
				if (ArtDept.loggingEnabled) log.debug("03. Getting Quantity: " + od.getQuantity());
				orderDetailInfo += "@" + (od.getQuantity() == 0 ? "" : usFormat.format(od.getQuantity()));
				if (ArtDept.loggingEnabled) log.debug("04. Getting Flags: " + od.getFlags());
				orderDetailInfo += "@" + (od.getFlags() == 0 ? "" : String.valueOf(od.getFlags()));
				if (ArtDept.loggingEnabled) log.debug("05. Getting Reorder ID: " + od.getReorderId());
				orderDetailInfo += "@" + (od.getReorderId() == null ? "" : od.getReorderId());
				if (ArtDept.loggingEnabled) log.debug("06. Getting Packing Instructions: " + od.getPackingInstructions().replace("\"", "\\\""));
				orderDetailInfo += "@" + (od.getPackingInstructions() == null ? "" : od.getPackingInstructions().replace("\"", "\\\""));
				if (ArtDept.loggingEnabled) log.debug("07. Getting Package Quantity: " + od.getPackageQuantity());
				orderDetailInfo += "@" + (od.getPackageQuantity() == null ? "" : od.getPackageQuantity());
				if (ArtDept.loggingEnabled) log.debug("08. Getting Case Quantity: " + od.getCaseQuantity());
				orderDetailInfo += "@" + (od.getCaseQuantity() == null ? "" : od.getCaseQuantity());
				if (ArtDept.loggingEnabled) log.debug("09. Getting Label Quantity: " + od.getLabelQuantity());
				orderDetailInfo += "@" + (od.getLabelQuantity() == 0 ? "" : usFormat.format(od.getLabelQuantity()));
				if (ArtDept.loggingEnabled) log.debug("10. Getting Label Text: " + od.getLabelText().replace("\"", "\\\""));
				orderDetailInfo += "@" + (od.getLabelText() == null ? "" : od.getLabelText().replace("\"", "\\\""));
				if (ArtDept.loggingEnabled) log.debug("11. Getting ID (primary key): " + String.valueOf(od.getId()));
				orderDetailInfo += "@" + (od.getLabelText() == null ? "" : String.valueOf(od.getId()));
				if (odIterator.hasNext()) orderDetailInfo += "@"; // Place another separator between groups.
			}
		} catch (NullPointerException err) {
			if (ArtDept.loggingEnabled) log.error("Null Pointer to the Order Detail List. Creating one now.");
			orderDetailInfo = "@@@@@@@@@@";
		}*/
		
		// Create a JSON string to pass to the script.
		JSONObject outgoing = new JSONObject();
		try {
			outgoing = new JSONObject(bean);
		} catch (NullPointerException err) {
			// Likely this is a Proofing job and the user tried running it through Output.
			if (ArtDept.loggingEnabled) log.error("Could not find the Proof/Output AppleScript file. Likely this is a Proofing job and the user tried running it through Output.", err);
			JOptionPane.showMessageDialog(null, "This is most likely a Proofing job, not Output. Please try again.", "Job Type Mismatch", JOptionPane.ERROR_MESSAGE);
			return false;			
		}
//		outgoing.put("jobId", jobNumber); // Removed this because if there is no Bean with the proper info, then the Proof was never added to the db.
		outgoing.put("thisProofNumber", proofNum);
		outgoing.put("customerServiceRep", customerServiceRep);
		outgoing.put("creditCard", creditCard);
		outgoing.put("shipDays", shipDays);
		outgoing.put("wnaPo", wnaPo);
		outgoing.put("userInitials", initials);
		outgoing.put("shipDate", shipDate);
//		outgoing.put("loggingEnabled", ArtDept.loggingEnabled);
		String jsonOut = JSONObject.quote(outgoing.toString());
//		jsonOut = jsonOut.replaceAll("\\{", "\\\\{");
//		jsonOut = jsonOut.replaceAll("\\}", "\\\\}");
		if (ArtDept.loggingEnabled) log.debug("JSON-Out:\n" + jsonOut);
		
		
		// Begin building the String that will be the script we run.
		// The first few lines are just to add in the arguments from the ArtDept class.
		// The rest is read from the file defined above.
		script = "set json to " + jsonOut + "\n";
		script += "set jLogging to \"" + ArtDept.loggingEnabled + "\"\n";
		script += "set jPath to \"" + ArtDept.scriptPath + "\"\n";
		if (ArtDept.loggingEnabled) log.debug("Reading the Proof/Output script into a string.");
		if (ArtDept.loggingEnabled) log.debug("Before adding the script, here are the variables:\n" + script);
		try {
			script += FileUtils.readFileToString(scriptFile);
		} catch (IOException e) {
			if (ArtDept.loggingEnabled) log.error("Could not find the Proof/Output AppleScript file.", e);
			JOptionPane.showMessageDialog(null, "Could not find the Proof/Output AppleScript file.  Please verify connection to the server.", "File not found", JOptionPane.ERROR_MESSAGE);
			return false;
//				ConnectionManager.getInstance().close();
//				System.exit(1);
		}
/*		if (scriptType == ScriptType.PROOF) {
//			script = "set jOrderNum to \"" + jobNumber + "\"\n";
//			script += "set jPrintingCompany to \"" + pCompany + "\"\n";
//			script += "set jClientName to \"" + customerName + "\"\n";
//			script += "set jClientPO to \"" + customerPO + "\"\n";
//			script += "set jProofNum to \"" + proofNum + "\"\n";
//			script += "set jOverruns to \"" + overruns + "\"\n";
//			script += "set jSampleShelf to \"" + sampleShelf + "\"\n";
//			script += "set jInitials to \"" + initials + "\"\n";
//			script += "set jOrderDetail to \"" + orderDetailInfo + "\"\n";
//			script += "set jCustomerServiceRep to \"" + customerServiceRep + "\"\n";
//			script += "set jCreditCard to \"" + creditCard + "\"\n";
//			script += "set jShipDays to \"" + shipDays + "\"\n";
//			script += "set jWnaPo to \"" + wnaPo + "\"\n";
			script = "";
//			script += "tell application \"JSON Helper\"\n";
			script += 	"set json to " + jsonOut + "\n";
//			script += 	"set json to make JSON from myRecord\n";
//			script += "end tell\n";
			script += "set jLogging to \"" + ArtDept.loggingEnabled + "\"\n";
			script += "set jPath to \"" + ArtDept.scriptPath + "\"\n";
			if (ArtDept.loggingEnabled) log.debug("Reading the Proofing script into a string.");
			if (ArtDept.loggingEnabled) log.debug("Before adding the script, here are the variables:\n" + script);
			try {
				script += FileUtils.readFileToString(scriptFile);
			} catch (IOException e) {
				if (ArtDept.loggingEnabled) log.error("Could not find the Proofing AppleScript file.", e);
				JOptionPane.showMessageDialog(null, "Could not find the Proofing AppleScript file.  Please verify connection to the server.", "File not found", JOptionPane.ERROR_MESSAGE);
				return false;
//				ConnectionManager.getInstance().close();
//				System.exit(1);
			}
		} else { // Output.
//			script = "set jOrderNum to \"" + jobNumber + "\"\n";
//			script += "set jShipDate to \"" + shipDate + "\"\n";
//			script += "set jOverruns to \"" + overruns + "\"\n";
//			script += "set jInitials to \"" + initials + "\"\n";
//			script += "set jOrderDetail to \"" + orderDetailInfo + "\"\n";
			script = "set json to " + jsonOut + "\n";
			script += "set jLogging to \"" + ArtDept.loggingEnabled + "\"\n";
			script += "set jPath to \"" + ArtDept.scriptPath + "\"\n";
			if (ArtDept.loggingEnabled) log.debug("Reading the Output script into a string.");
			if (ArtDept.loggingEnabled) log.debug("Before adding the script, here are the variables:\n" + script);
			try {
				script += FileUtils.readFileToString(scriptFile);
			} catch (IOException e) {
				if (ArtDept.loggingEnabled) log.error("Could not find the Output AppleScript file.", e);
				JOptionPane.showMessageDialog(null, "Could not find the Output AppleScript file.  Please verify connection to the server.", "File not found", JOptionPane.ERROR_MESSAGE);
				return false;
//				ConnectionManager.getInstance().close();
//				System.exit(1);
			}
		}*/
		
		
		// These two lines prepare the scripting engine, ready to run the script.
		if (ArtDept.loggingEnabled) log.debug("Setting the script engine.");
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = null;
		try {
			engine = mgr.getEngineByName("AppleScriptEngine");
		} catch (Exception err) {
			if (ArtDept.loggingEnabled) log.error("Could not find the scripting engine needed.", err);
		}

		// Add the parameters to the engine so they will be passed to the script.
/*		if (scriptType == ScriptType.PROOF) {
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
			engine.put("javaLogging", ArtDept.loggingEnabled);
		} else { // Output
			engine.put("javaOrderNum", jobNumber);
			engine.put("javaShipDate", shipDate);
			engine.put("javaOverruns", overruns);
			engine.put("javaInitials", initials);
			engine.put("javaOrderDetails", orderDetailInfo);
			engine.put("javaLogging", ArtDept.loggingEnabled);
			engine.put("javaPath",  ArtDept.scriptPath);
		}*/
		
		// Run the script and evaluate the result.
		if (ArtDept.loggingEnabled) log.trace("Running the script and evaluating the result.");
		Object result = null;
		try {
			result = engine.eval(script); // Run the script and place the result into an abstract object.
		} catch (ScriptException e) {
			if (ArtDept.loggingEnabled) log.error("An error occurred with the script. Line number: " + e.getLineNumber(), e);
			JOptionPane.showMessageDialog(null, "An error occurred with the script.", "Script error / cancel", JOptionPane.ERROR_MESSAGE);
			return false;
//			ConnectionManager.getInstance().close();
//			System.exit(1);
		}
		if (ArtDept.loggingEnabled) log.debug(result); // Check that we received the correct information back from the script.
		if (ArtDept.loggingEnabled) log.debug("");
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
				if (ArtDept.loggingEnabled) log.trace("The unused '0' error code was returned!");
				return false;
			case 1:
				if (ArtDept.loggingEnabled) log.trace("Connection to server not found.");
				return false;
			case 2:
				if (ArtDept.loggingEnabled) log.trace("MRAP error returned from Illustrator.");
				return false;
			case 3:
				if (ArtDept.loggingEnabled) log.trace("Another type of error returned from Illustrator (besides MRAP).");
				return false;
			case 4:
				if (ArtDept.loggingEnabled) log.trace("No files with the given order number were found.");
				return false;
			case 5:
				if (ArtDept.loggingEnabled) log.trace("Product type (multicolor) does not exist in script.");
				return false;
			case 6:
				if (ArtDept.loggingEnabled) log.trace("Tried to create a new 'thousands' folder and failed. If problem persists, create the folder manually.");
				return false;
			case 7:
				if (ArtDept.loggingEnabled) log.trace("Cancel button pressed.");
				return false;
			case 8:
				if (ArtDept.loggingEnabled) log.trace("Could not find existing files to rename them.");
				return false;
			case 9:
				if (ArtDept.loggingEnabled) log.trace("Could not find InDesign template file.");
				return false;
			case 10:
				if (ArtDept.loggingEnabled) log.trace("InDesign is crashing / has crashed.");
				return false;
			case 11:
				if (ArtDept.loggingEnabled) log.trace("Script could not continue because another InDesign file was open.");
				return false;
			case 12:
				if (ArtDept.loggingEnabled) log.trace("Extra files/folders in the job folder.");
				return false;
			case 100:
				if (ArtDept.loggingEnabled) log.trace("Script did not return any info to the Applescript script, so it was undefined.");
				return false;
			default:
				if (ArtDept.loggingEnabled) log.trace("Script exited by some other means.");
				return false;
			}
		}
		
		// Parse the returned string into a JSON Object.
		JSONObject thisJob = new JSONObject(resultStr);
		JSONArray items = thisJob.getJSONArray("items");
		if (ArtDept.loggingEnabled && thisJob != null) log.debug("JSON Object: " + thisJob);
		
//		JSONObject firstItem = resultArray.getJSONObject(0);
//		if (ArtDept.loggingEnabled) log.debug("JSON object: " + firstItem);
		
		if (ArtDept.loggingEnabled) log.debug("Ship Date as epoch int: " + thisJob.getLong("shipDate"));
		
		
		String[] aResult = resultStr.split("@");  // Convert the String into an Array of Strings.
		String[] aTemp = new String[25]; // This temporary Array will hold the items common to each product in an order.
		ArrayList<String[]> aJobDetails = new ArrayList<String[]>(); // Setting up a two-dimensional ArrayList of Arrays.
		for (int i = 0; i < aResult.length; i+=25) { // Go through each block of items in the list and add them to the ArrayList.
			aTemp = Arrays.copyOfRange(aResult, i, i+25);
			aJobDetails.add(aTemp);
		}

		// If we got nothing back, then return with a failure condition.
		if (resultStr.length() < 1) {
			if (ArtDept.loggingEnabled) log.trace("Nothing returned from script. User possibly cancelled it.");
			return false;
		}
		
		// Finally, some test sample output.
//		if (ArtDept.loggingEnabled) log.debug(new Date(Long.parseLong(aJobDetails.get(0)[0], 10)).toString());
////		if (ArtDept.loggingEnabled) log.debug(LocalDate.parse(aJobDetails.get(0)[0]).toString("yyyy-MM-dd")); // Ship Date
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[1]); // Job ID
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[2]); // Customer Name
//		if (ArtDept.loggingEnabled) log.debug(usDateFormat.format(new Date(Long.parseLong(aJobDetails.get(0)[3], 10))));
////		if (ArtDept.loggingEnabled) log.debug(LocalDateTime.parse(aJobDetails.get(0)[3]).toString("yyyy-MM-dd HH:mm:ss")); // Proof Date / Spec Date
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[4]); // Product ID
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[5]); // Product Detail
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[6]); // Print Type (See Output script or database table schema for description.)
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[7]); // Number of Colors
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[8]); // Quantity
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[9]); // Print Company (Accents / Cabin / Yacht)
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[10]); // Proof Number
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[11]); // Overruns boolean
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[12]); // Customer PO
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[13]); // Thumbnail filename
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[14]); // Flags
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[15]); // Reorder ID
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[16]); // Packing / Labeling Info
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[17]); // Package Quantity
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[18]); // Case Quantity
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[19]); // Label Quantity
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[20]); // Label Text
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[21]); // Sample Shelf Note boolean
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[22]); // Proofer's Initials
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[23]); // Outputter's Initials
//		if (ArtDept.loggingEnabled) log.debug(aJobDetails.get(0)[24]); // Digital file name
//		if (ArtDept.loggingEnabled) log.debug("");


		// Now to insert the returned data into the database.
		// First, place the day's info into the Day table.  This should only insert one row.
		if (ArtDept.loggingEnabled) log.trace("First, place the day's info into the Day table.  This should only insert one row.");
		Date jobDate = new Date(thisJob.getLong("shipDate"));
		Day dayBean = new Day(jobDate);
		dayBean.setDate(jobDate);
		
		// Add in defaults from the XML file.
		if (DateManager.isWeekDay(jobDate)) {
			Serializer serializer = new Persister(Day.getXmlFormat());
			Day preBean = new Day(jobDate);
			try {
				dayBean = serializer.read(preBean, Day.getXmlFile());
			} catch (Exception e) {
				// Commented out this block as Art Department users do not set defaults.
//				String msg = "<html><body>The defaults file does not exist. As soon as possible, please create one<br />"
//						+ "by going to the menu option <strong>Actions / Set Defaults</strong>.</body></html>";
//				JLabel message = new JLabel(msg);
//				JOptionPane.showMessageDialog(null,
//						message,
//						"XML File Not Found",
//						JOptionPane.WARNING_MESSAGE);
				dayBean = preBean;
			}
		}
		
		boolean successfulDayInsert = false;
		try {
			if (!DayManager.dayExists(dayBean.getDate())) {
				successfulDayInsert = DayManager.insert(dayBean); // Insert the day info into the Job table. 
			} else {
				if (ArtDept.loggingEnabled) log.trace("" + dayBean.getDate() + " already exists in the Day table. Setting it to INCOMPLETE.");
				DayManager.setNotCompleted(dayBean.getDate());
			}
		} catch (Exception e) {
			if (ArtDept.loggingEnabled) log.error("Error inserting data into database", e);
			JOptionPane.showMessageDialog(null, "Error inserting data into database", "Database error", JOptionPane.ERROR_MESSAGE);
			return false;
//			ConnectionManager.getInstance().close();
//			System.exit(1);
		}
		
		if (successfulDayInsert) {
			if (ArtDept.loggingEnabled) log.debug("New row with date of " + dayBean.getDate() + " was inserted!");
		}
		
		
		// Next, set the Job info bean.
		if (ArtDept.loggingEnabled) log.trace("Next, set the Job info bean.");
		if (bean == null) {
			if (ArtDept.loggingEnabled) log.debug("Bean was null; creating new Job bean.");
			bean = new Job();
		}
		customerName = thisJob.getString("customerName");
		customerPO = thisJob.getString("customerPO");
		
		bean.setJobId(thisJob.getString("jobID"));
		bean.setProofSpecDate(new Date(thisJob.getLong("proofSpecDate")));
		bean.setCustomerName(customerName);
		bean.setCustomerPO(customerPO);
		bean.setSampleShelfNote(thisJob.getBoolean("sampleShelfNote"));
		if (scriptType == ScriptType.OUTPUT) {
			bean.setPrintingCompany(pCompanyEnum);
			bean.setShipDate(jobDate);
			bean.setOverruns(thisJob.getBoolean("overruns"));
			bean.setSigOutput(thisJob.getString("outputterSignature"));
		} else { // Proofing
//			bean.setCustomerName(customerName);
//			bean.setCustomerPO(customerPO);
			bean.setPrintingCompany(PrintingCompany.getPrintingCompany(thisJob.getInt("printingCompany")));
			bean.setShipDate(DateManager.localDateToSqlDate(DateManager.PROOF_DATE));
			bean.setOverruns(overruns);
			bean.setSampleShelfNote(false); // Default to false. Will test for the note shortly to possibly change this.
			bean.setSigProof(thisJob.getString("prooferSignature"));
		}
		
		// As part of setting the Job bean, set its OrderDetail List with OrderDetail beans.
		if (ArtDept.loggingEnabled) log.trace("As part of setting the Job bean, set its OrderDetail List with OrderDetail beans.");
//		Pattern prProof = Pattern.compile("Proof|NaN", Pattern.CASE_INSENSITIVE);
		List<OrderDetail> odList = new ArrayList<OrderDetail>();
		boolean allDigitalProofs = true; // This boolean will store whether or not all of the items
										 // in a multi-item proof are digital. If so, it should not be added to the database.
//		for (int i = 0; i < aJobDetails.size(); i++) {
		for (int i = 0; i < items.length(); i++) {
			JSONObject thisItem = (JSONObject) items.get(i);
			if (ArtDept.loggingEnabled) log.debug("Start: item " + i + ": " + thisItem.getString("productID"));
			
			// If ANY of the items has the "Sample Shelf" note, then set the "Sample Shelf" boolean for the entire job.
//			if (aJobDetails.get(i)[21].equalsIgnoreCase("true")) {
//				if (ArtDept.loggingEnabled) log.debug("Sample Shelf String returned from InDesign: " + aJobDetails.get(i)[21] + ". Setting Sample Shelf Note to true.");
//				bean.setSampleShelfNote(true);
//			}
			OrderDetail detailBean = new OrderDetail();
			detailBean.setOrderId(thisJob.getString("jobID"));
			detailBean.setProductId(thisItem.getString("productID"));
			detailBean.setProductDetail(thisItem.getString("productDetail"));
			detailBean.setThumbnail(thisItem.getString("thumbnail"));
			if (scriptType == ScriptType.PROOF) {
				detailBean.setProofNum(thisItem.getInt("proofNumber"));
				detailBean.setProofDate(new Date(thisJob.getLong("proofSpecDate")));
				detailBean.setFlags(thisItem.getInt("flags"));
				detailBean.setReorderId(thisItem.getString("reorderID"));
				detailBean.setPackingInstructions(thisItem.getString("packingInstructions"));
				detailBean.setPackageQuantity(thisItem.getString("packageQuantity"));
				detailBean.setCaseQuantity(thisItem.getString("caseQuantity"));
				try { // Since the Label Quantity field could be blank
					detailBean.setLabelQuantity(thisItem.getInt("labelQuantity"));
				} catch (NumberFormatException err) {
					if (ArtDept.loggingEnabled) log.error("Could not parse Label Quantity into an integer. Setting it to 0 instead.");
					detailBean.setLabelQuantity(0);
				}
				detailBean.setLabelText(thisItem.getString("labelText"));
			} else { // Output
				if (ArtDept.loggingEnabled) log.info("OUTPUT JOB");
				if (thisItem.getJSONArray("digitalArtFiles").length() > 0) {
					if (ArtDept.loggingEnabled) log.info("24: " + thisItem.getJSONArray("digitalArtFiles").getString(0));
				}
				detailBean.setProofNum(proofNum);
				detailBean.setFlags((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? 0 : bean.getOrderDetailList().get(i).getFlags());
				detailBean.setReorderId((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "" : bean.getOrderDetailList().get(i).getReorderId());
				detailBean.setPackingInstructions((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "" : bean.getOrderDetailList().get(i).getPackingInstructions());
				detailBean.setPackageQuantity((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "0" : bean.getOrderDetailList().get(i).getPackageQuantity());
				detailBean.setCaseQuantity((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "0" : bean.getOrderDetailList().get(i).getCaseQuantity());
				detailBean.setLabelQuantity((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? 0 : bean.getOrderDetailList().get(i).getLabelQuantity());
				detailBean.setLabelText((bean.getOrderDetailList() == null || i >= bean.getOrderDetailList().size()) ? "" : bean.getOrderDetailList().get(i).getLabelText());
//				detailBean.setDigitalFilename(thisItem.getJSONArray("digitalArtFiles").getString(0));
				JSONArray artFiles = thisItem.getJSONArray("digitalArtFiles");
				List<Artwork> existingArtList = detailBean.getArtworkList();
				
				// Get all "Artwork" rows from db where the od id matches this od's id. Store in an ArrayList.
				
				// Loop through the incoming JSONArray and update any of those rows in the db, one at a time.
				int j = -1;
				while (++j < artFiles.length()) {
					String artFileText = artFiles.getString(j);
					if (ArtDept.loggingEnabled) log.debug("Storing art file # " + j);
					if (j < existingArtList.size()) {
						if (ArtDept.loggingEnabled) log.debug("Replacing existing art bean.");
						Artwork thisArt = existingArtList.get(j);
						thisArt.setDigitalArtFile(artFileText);
					} else { // If we run out of db rows first (including if it was empty to begin with),
						     // then "add" the remaining items from the JSONArray to the List in the OD item, one at a time.
						if (ArtDept.loggingEnabled) log.debug("Creating new art bean and adding it to the list.");
						Artwork thisArt = new Artwork();
						thisArt.setDigitalArtFile(artFileText);
						existingArtList.add(thisArt);
					}
				}
				if (ArtDept.loggingEnabled) log.debug("j is now: " + j);
				// If we run out of JSONArray items first, then delete the remaining db rows, one at a time.
				while (j < existingArtList.size()) {
					if (ArtDept.loggingEnabled) log.debug("Removing extraneous art beans from the list.");
					try {
						ArtworkManager.delete(existingArtList.get(j++).getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			if (ArtDept.loggingEnabled) log.info("Package Quantity in bean: " + detailBean.getPackageQuantity());
			if (ArtDept.loggingEnabled) log.info("Case Quantity in bean: " + detailBean.getCaseQuantity());
//			if (ArtDept.loggingEnabled) log.info("Digital Filename in bean: " + detailBean.getDigitalFilename());
			try {
//				String correctedQuantity = aJobDetails.get(i)[8];
//				Matcher matcher = prProof.matcher(correctedQuantity);
//				if (matcher.find()) correctedQuantity = "1"; // If the word "proof" (case insensitive) is found for the quantity, change it to "1".
				
//				detailBean.setQuantity(NumberFormat.getNumberInstance().parse(correctedQuantity).intValue());
				detailBean.setQuantity(thisItem.getLong("quantity"));
				// Check that quantity for 0 (Digital Proof) and set the boolean accordingly.
				if (detailBean.getQuantity() > 0) allDigitalProofs = false;
				
				detailBean.setNumColors(thisItem.getLong("numberOfColors"));
				detailBean.setPrintType(PrintType.getPrintType(thisItem.getInt("jobType")));
			} catch (Exception e) {
				if (ArtDept.loggingEnabled) log.error("Could not parse the quantity, number of colors, or print type.", e);
				JOptionPane.showMessageDialog(null, "Could not parse the quantity, number of colors, or print type.  Please fix and run again.", "Parse error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			odList.add(detailBean);
			
		}
		if (ArtDept.loggingEnabled) {
			for (int k = 0; k < odList.size(); k++) {
				log.debug("End: item " + k + ": " + odList.get(k).getProductId());
			}
		}

		// Now check that boolean to see if all of the items are Digital Proofs only.
		// If so, just immediately return without putting anything into the database.
		if (ArtDept.loggingEnabled) log.trace("All of the items in this job are Digital Proofs only: " + allDigitalProofs);
		if (allDigitalProofs) return true;
		
		// Place the job info into the Job table.  This should only insert one row.
		// If the job already exists, though, just update it.
		boolean successfulJobInsert = false;
		try {
			if (JobManager.getRow(bean.getJobId()) == null) {
				if (ArtDept.loggingEnabled) log.debug("INSERTing a new Job entry into the Job table.");
				successfulJobInsert = JobManager.insert(bean); // Insert the job & order detail in the database.
			} else {
				if (ArtDept.loggingEnabled) log.debug("UPDATing the existing Job in the Job table.");
				successfulJobInsert = JobManager.update(bean); // Update the job & order detail in the database.
			}
		} catch (Exception e) {
			if (ArtDept.loggingEnabled) log.error("Error inserting/updating Job data in the database", e);
			JOptionPane.showMessageDialog(null, "Error inserting Job data into database", "Database error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// Log the successfulness of the job insertion/updation.
		if (successfulJobInsert) {
			if (ArtDept.loggingEnabled) log.debug("Job id " + bean.getJobId() + " was inserted/updated!");
		} else {
			if (ArtDept.loggingEnabled) log.debug("One or more items did NOT make it into the database.");
			return false;
		}
		
		
		// This next section should only run if there are existing OrderDetail items in the database.
		boolean successfulODInsert = false;
		if (bean.getOrderDetailList() != null) {
			// This is where things get tricky. We need to get a list of the .INDD files in the job directory as an ArrayList of Strings first.
			// We then compare this List of Strings to the incoming OrderDetail List and pop out any matches from the List of Strings.
			// What we're left with is a list of only those items that have NOT been updated / added to the job.
			List<File> protectionList = getProtectionList(jobNumber, odList);
			
			// We want to protect these items and not update/delete them from the database. To that end, we now compare this list
			// to the existing list of OrderDetail items that are in the database. Matches are popped out of that existing list,
			// so that all that remains should be only the items to be updated or deleted.
			List<OrderDetail> modifiedODList = protectODs(bean.getOrderDetailList(), protectionList);
			
			// Now we compare our two Lists of OrderDetail items and add/update/remove, depending upon how many are in each list.
			successfulODInsert = updateTables(odList, modifiedODList, scriptType);
			
			// As a final step, check to see if there are some items in the protectionList that do NOT exist in the table.
			// This can happen if InDesign crashed mid-way through a multi-item order or some such.
			// If this is the case, then go ahead and add dummy entries to the database, so at least SOMETHING is there.
			addDummyEntries(bean.getOrderDetailList(), protectionList);
		} else { // No existing OrderDetail items in the database.
			bean.setOrderDetailList(odList);
			addDummyEntries(bean.getOrderDetailList(), bean.getJobId());
			try {
				for (OrderDetail orderDetail : bean.getOrderDetailList()) {
					if (ArtDept.loggingEnabled) log.debug("INSERTing a new OrderDetail entry into the Job table.");
					successfulODInsert = OrderDetailManager.insert(orderDetail); // Insert the order detail in the database.
				}
			} catch (Exception err) {
				if (ArtDept.loggingEnabled) log.error("Error inserting OrderDetail data in the database", err);
				JOptionPane.showMessageDialog(null, "Error inserting Order Detail data into database", "Database error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		
		// Add the OD's id to each of its Artwork beans, then add them to the db.
//		if (ArtDept.loggingEnabled) log.debug("Number of ODs in the Job bean's list: " + bean.getOrderDetailList().size());
//		for (int i = 0; i < bean.getOrderDetailList().size(); i++) {
		for (int i = 0; i < odList.size(); i++) {
//			OrderDetail thisOD = bean.getOrderDetailList().get(i);
			OrderDetail thisOD = odList.get(i);
			// Change the following 'for' loop so it will insert Artwork items,
			// even if the ArtworkList is empty. Right now, this loop doesn't
			// even run at all if that list is empty.
			if (ArtDept.loggingEnabled) log.debug("Number of Artworks in the OD bean's list: " + thisOD.getArtworkList().size());
			for (int j = 0; j < thisOD.getArtworkList().size(); j++) {
				Artwork thisArt = thisOD.getArtworkList().get(j);
				thisArt.setOrderDetailId(thisOD.getId());
				try {
					thisArt.setId(ArtworkManager.getArtworkId(thisArt.getDigitalArtFile()));
					if (thisArt.getId() > 0) {
						ArtworkManager.update(thisArt);
					} else {
						ArtworkManager.insert(thisArt);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		// Log the successfulness of the insertion/updation.
		if (successfulODInsert) {
			if (ArtDept.loggingEnabled) log.debug("At least one row was inserted/updated!");
		} else {
			if (ArtDept.loggingEnabled) log.debug("One or more items did NOT make it into the database.");
			return false;
		}

		
		if (ArtDept.loggingEnabled) log.trace("Completed main section.");
		
//		ConnectionManager.getInstance().close();

		return true;
	}

	
	private static boolean updateTables(List<OrderDetail> incomingList, List<OrderDetail> existingList, ScriptType scriptType) {
		if (ArtDept.loggingEnabled) log.entry("updateTables (ScriptManager)");

/*		if (incomingList.size() == 0)
		{
			if (existingList.size() == 0)
			{
				if (ArtDept.loggingEnabled) log.exit("Both lists are empty!");
				return;
			}
			deleteItems(existingList);
			if (ArtDept.loggingEnabled) log.exit("Items removed from database.");
			return;
		}
		if (existingList.size() == 0)
		{
			insertItems(incomingList);
			if (ArtDept.loggingEnabled) log.exit("Items added to database.");
			return;
		}*/
		
		// Last part here occurs if there are items in both lists. Update until one of them drops to 0.
		Iterator<OrderDetail> incomingIterator = incomingList.iterator();
		Iterator<OrderDetail> existingIterator = existingList.iterator();
		
		if (ArtDept.loggingEnabled) log.debug("Incoming list has " + incomingList.size() + " elements.");
		if (ArtDept.loggingEnabled) log.debug("Existing list has " + existingList.size() + " elements.");

		try {
			if (scriptType == ScriptType.PROOF) {
				while (incomingIterator.hasNext() && existingIterator.hasNext()) {
					if (ArtDept.loggingEnabled) log.debug("Updating an existing OD item. (Proofing)");
					OrderDetail incomingOD = (OrderDetail) incomingIterator.next();
					OrderDetail existingOD = (OrderDetail) existingIterator.next();
					incomingOD.setId(existingOD.getId());
					if (incomingOD.getProofDate() == null)
						incomingOD.setProofDate(existingOD.getProofDate());
					OrderDetailManager.update(incomingOD);
					break;
				}
			} else { // Output.
				while (incomingIterator.hasNext()) {
					OrderDetail incomingOD = (OrderDetail) incomingIterator.next();
					while (existingIterator.hasNext()) {
						OrderDetail existingOD = (OrderDetail) existingIterator.next();
						if (incomingOD.getProductDetail().equals(existingOD.getProductDetail())
								&& incomingOD.getProductId().equals(existingOD.getProductId())) {
							if (ArtDept.loggingEnabled) log.debug("Updating an existing OD item. (Output)");
							incomingOD.setId(existingOD.getId());
							if (incomingOD.getProofDate() == null)
								incomingOD.setProofDate(existingOD.getProofDate());
							OrderDetailManager.update(incomingOD);
//							incomingIterator.remove();
//							existingIterator.remove();
							existingIterator = existingList.iterator();
							break;
						}
					}
				}
			}
			
//			incomingIterator = incomingList.iterator();
//			existingIterator = existingList.iterator();
			
			if (scriptType == ScriptType.PROOF) {
				while (incomingIterator.hasNext()) {
					if (ArtDept.loggingEnabled) log.debug("Inserting a new OD item.");
					OrderDetail incomingOD = (OrderDetail) incomingIterator.next();
					OrderDetailManager.insert(incomingOD);
				}
				while (existingIterator.hasNext()) {
					if (ArtDept.loggingEnabled) log.debug("Deleting an existing OD item.");
					OrderDetail existingOD = (OrderDetail) existingIterator.next();
					OrderDetailManager.delete(existingOD.getId());
				}
			}
		} catch (Exception err) {
			if (ArtDept.loggingEnabled) log.error("Error while attempting to update/insert/delete data in database.", err);
			return false;
		}
		
		return true;
	}

/*	private static void insertItems(List<OrderDetail> odList)
	{
		if (ArtDept.loggingEnabled) log.entry("insertItems (ScriptManager)");

		for (OrderDetail orderDetail : odList) {
			try {
				OrderDetailManager.insert(orderDetail);
			} catch (Exception err) {
				if (ArtDept.loggingEnabled) log.error("Exception when attempting to insert an OrderDetail item into the database.", err);
				return;
			}
		}
		if (ArtDept.loggingEnabled) log.exit("insertItems (ScriptManager)");
	}

	private static void deleteItems(List<OrderDetail> odList)
	{
		if (ArtDept.loggingEnabled) log.entry("deleteItems (ScriptManager)");

		for (OrderDetail orderDetail : odList) {
			try {
				OrderDetailManager.delete(orderDetail.getId());
			} catch (Exception err) {
				if (ArtDept.loggingEnabled) log.error("Exception when attempting to delete an item from the database.", err);
				return;
			}
		}
		if (ArtDept.loggingEnabled) log.exit("deleteItems (ScriptManager)");
	}*/

/*	private static List<OrderDetail> cloneList(List<OrderDetail> list)
	{
		List<OrderDetail> clone = new ArrayList<OrderDetail>(list.size());
		for (OrderDetail od : list) clone.add(od);
		return clone;
	}*/
	
	private static List<OrderDetail> protectODs(List<OrderDetail> existingList, List<File> protectionList) {
		if (ArtDept.loggingEnabled) log.entry("protectODs (ScriptManager)");

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
				if (ArtDept.loggingEnabled) log.debug("Comparing " + orderDetail.getProductDetail() + " (existing item in db) to " + itemDetail + " (item in protection list).");
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals("")) || orderDetail.getProductDetail().equals(itemDetail))
				{
					if (ArtDept.loggingEnabled) log.debug("Found a match. Removing from trimmed list. (Item protected)");
					odIterator.remove();
//					fileIterator.remove();
					break;
				}
			}
		}
				
		return trimmedList;
	}

	private static void addDummyEntries(List<OrderDetail> existingList, List<File> protectionList) {
		if (ArtDept.loggingEnabled) log.entry("addDummyEntries (ScriptManager)");
		
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
				if (ArtDept.loggingEnabled) log.debug("Comparing " + orderDetail.getProductDetail() + " to " + itemDetail);
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
						|| itemDetail == null
						|| orderDetail.getProductDetail().equals(itemDetail)
						|| StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
						|| StringUtils.containsIgnoreCase(itemDetail, "Carousel")
						|| StringUtils.containsIgnoreCase(itemDetail, "cover")
						|| StringUtils.containsIgnoreCase(itemDetail, "label")) {
					if (ArtDept.loggingEnabled) log.debug("Found a match. Removing from the protectionList. (Will NOT be added as a dummy item.)");
					fileIterator.remove();
					break;
				}
			}
		}
	
		if (protectionList.size() > 0) {
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
					if (ArtDept.loggingEnabled) log.error("Exception when trying to insert a dummy OrderDetail into the database.", err);
				}
			}
		}
		
	}
	
	
	private static void addDummyEntries(List<OrderDetail> incomingList, String jobNumber) {
		if (ArtDept.loggingEnabled) log.entry("addDummyEntries (when bean doesn't exist yet) (ScriptManager)");
		
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
				if (ArtDept.loggingEnabled) log.debug("Comparing " + orderDetail.getProductDetail() + " (incoming item) to " + itemDetail + " (existing file in directory).");
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
						|| itemDetail == null
						|| orderDetail.getProductDetail().equals(itemDetail)
						|| StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
						|| StringUtils.containsIgnoreCase(itemDetail, "Carousel")
						|| StringUtils.containsIgnoreCase(itemDetail, "cover")
						|| StringUtils.containsIgnoreCase(itemDetail, "label")) {
					if (ArtDept.loggingEnabled) log.debug("Found a match. Removing from the existing File list. (Will not be added as a dummy item to the database.)");
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
				if (ArtDept.loggingEnabled) log.debug("Comparing " + orderDetail.getProductDetail() + " to " + itemDetail);
				if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
						|| itemDetail == null
						|| orderDetail.getProductDetail().equals(itemDetail)
						|| StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
						|| StringUtils.containsIgnoreCase(itemDetail, "Carousel"))
				{
					if (ArtDept.loggingEnabled) log.debug("Found a match. Removing from the protectionList. (Will NOT be added as a dummy item.)");
					fileIterator.remove();
					break;
				}
			}
		}*/
		
		if (fileList.size() > 0) {
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
					if (ArtDept.loggingEnabled) log.error("Exception when trying to insert a dummy OrderDetail into the database.", err);
				}
			}
		}
		
	}


	/**
	 * @param jobNumber
	 * @param odList
	 * @return List<File> A list of files that will be protected from deletion/updating.
	 * 	These are files that exist in the folder but are NOT in the incoming list of jobs.
	 */
	private static List<File> getProtectionList(String jobNumber, List<OrderDetail> odList) {
		if (ArtDept.loggingEnabled) log.entry("getProtectionList (ScriptManager)");

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
				if (ArtDept.loggingEnabled) log.debug("Comparing " + od.getProductDetail() + " (incoming item) to " + itemDetail + " (existing file in directory).");
				if (itemDetail.equals(od.getProductDetail())
						|| itemDetail.equals("MiniPad")
						|| itemDetail.equals("Carousel")
						|| itemDetail.equals("cover")
						|| itemDetail.equals("label")) {
					if (ArtDept.loggingEnabled) log.debug("Found a match. Removing from the protectionList. (Will be deleted/updated later.)");
					fileIterator.remove();
//					break;
				}
			}
		}
		
		return fileList;
	}

	private static String getPrintCompanyString (PrintingCompany printingCompany) {
		return StringUtils.substringBefore(StringUtils.substringAfter(printingCompany.toString(), "_"), "_");
	}
	
	public static boolean areOverruns (String str) {
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
