package info.chrismcgee.sky.artdept;

import info.chrismcgee.components.DateManager;
import info.chrismcgee.enums.OSType;
import info.chrismcgee.sky.beans.Artwork;
import info.chrismcgee.sky.beans.LineItem;
import info.chrismcgee.sky.beans.Order;
import info.chrismcgee.sky.beans.PrintType;
import info.chrismcgee.sky.enums.PrintTypeEnum;
import info.chrismcgee.sky.enums.PrintingCompany;
import info.chrismcgee.sky.enums.ScriptType;
import info.chrismcgee.sky.tables.*;
import info.chrismcgee.util.RedisManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ScriptManager {

    static final Logger log = LogManager.getLogger(ScriptManager.class.getName());
    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);

    public static boolean scriptRunner(ScriptType scriptType, String jobNumber, String initials, Order bean, int proofNum, String scriptFolder, String customerServiceRep, boolean creditCard, int shipDays, String wnaPo, String printerName) {

        if (ArtDept.loggingEnabled) log.entry("scriptRunner");
        if (ArtDept.loggingEnabled) log.debug("Username will be " + System.getenv("USER"));

        // A few variables to help get the information we need into a properly-formatted String.
        String pCompany = (bean == null || bean.getPrintingCompany() == null) ? "ACCENTS" : getPrintCompanyString(bean.getPrintingCompany());
        if (ArtDept.loggingEnabled) log.debug("Got pCompany: " + pCompany);
        String shipDate = (bean == null || bean.getShipDateId() == null) ? "" : DateManager.getDisplayDate(bean.getShipDateId());
        if (ArtDept.loggingEnabled) log.debug("Got shipDate: " + shipDate);
        String customerName = (bean == null || bean.getCustomerName() == null) ? "" : bean.getCustomerName();
        if (ArtDept.loggingEnabled) log.debug("Got customerName: " + customerName);
        String customerPO = (bean == null || bean.getCustomerPO() == null) ? "" : bean.getCustomerPO();
        if (ArtDept.loggingEnabled) log.debug("Got customerPO: " + customerPO);
        boolean overruns = bean != null && bean.isOverruns();
        if (ArtDept.loggingEnabled) log.debug("Got overruns: " + overruns);
        boolean sampleShelf = bean != null && bean.isSampleShelfNote();
        if (ArtDept.loggingEnabled) log.debug("Got sampleShelf: " + sampleShelf);
        PrintingCompany pCompanyEnum = (bean == null || bean.getPrintingCompany() == null) ? PrintingCompany.AMERICAN_ACCENTS : bean.getPrintingCompany();
        if (ArtDept.loggingEnabled) log.debug("Got pCompanyEnum: " + pCompanyEnum.toString());

        // Prepare the AppleScript file to be executed.
        if (ArtDept.loggingEnabled) log.trace("Prepare the intermediate file to be executed.");
        File scriptFile;
        String script;
        // If this is a Mac, then we need to call an AppleScript file.
        // Otherwise, we call a VBS file.
        if (scriptType == ScriptType.PROOF) {
            scriptFile = OSType.getOSType() == OSType.MAC
                    ? new File(scriptFolder + File.separator + "Proof.applescript")
                    : new File(scriptFolder + File.separator + "Proof.vbs");
        } else { // Output.
            scriptFile = OSType.getOSType() == OSType.MAC
                    ? new File(scriptFolder + File.separator + "Output.applescript")
                    : new File(scriptFolder + File.separator + "Output.vbs");
        }

        if (ArtDept.loggingEnabled) log.debug("scriptFolder and scriptFile variables set.");

        // Create a JSON string to pass to the script.
        JSONObject outgoing;
        try {
            assert bean != null;
            outgoing = new JSONObject(bean);
        } catch (NullPointerException err) {
            // Likely this is a Proofing job and the user tried running it through Output.
            if (ArtDept.loggingEnabled)
                log.error("Could not find the Proof/Output AppleScript file. Likely this is a Proofing job and the user tried running it through Output.", err);
            JOptionPane.showMessageDialog(null, "This is most likely a Proofing job, not Output. Please try again.", "Order Type Mismatch", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        outgoing.put("thisProofNumber", proofNum);
        outgoing.put("customerServiceRep", customerServiceRep);
        outgoing.put("creditCard", creditCard);
        outgoing.put("shipDays", shipDays);
        outgoing.put("wnaPo", wnaPo);
        outgoing.put("userInitials", initials);
        outgoing.put("shipDateId", shipDate);
        outgoing.put("printerName", printerName);
        String jsonOut;

        // Call the scripts with the necessary arguments, including the JSON string created in the previous step.
        String resultUntrimmed;
        if (OSType.getOSType() == OSType.MAC) {
            // If this is a Mac, then we need to give the AppleScript some initial variables.

            // Begin building the String that will be the script we run.
            jsonOut = JSONObject.quote(outgoing.toString());
            if (ArtDept.loggingEnabled) log.debug("JSON-Out:\n" + jsonOut);

            // The first few lines are just to add in the arguments from the ArtDept class.
            // The rest is read from the file defined above.
            script = "set json to " + jsonOut + "\n";
            script += "set jLogging to \"" + ArtDept.loggingEnabled + "\"\n";
            script += "set jPath to \"" + prefs.get(Settings.PREFS_DIR_KEY, Settings.PREFS_DIR_DEFAULT) + File.separator + "\"\n";
            if (ArtDept.loggingEnabled) log.debug("Reading the Proof/Output script into a string.");
            if (ArtDept.loggingEnabled) log.debug("Before adding the script, here are the variables:\n" + script);
            try {
                script += FileUtils.readFileToString(scriptFile);
            } catch (IOException e) {
                if (ArtDept.loggingEnabled) log.error("Could not find the Proof/Output AppleScript file.", e);
                JOptionPane.showMessageDialog(null, "Could not find the Proof/Output AppleScript file.  Please verify connection to the server.", "File not found", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // These two lines prepare the scripting engine, ready to run the script.
            if (ArtDept.loggingEnabled) log.debug("Setting the script engine.");
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = null;
            try {
                engine = mgr.getEngineByName("AppleScriptEngine");
            } catch (Exception err) {
                if (ArtDept.loggingEnabled) log.error("Could not find the scripting engine needed.", err);
            }

            // Run the script and evaluate the result.
            Object result;
            if (ArtDept.loggingEnabled) log.trace("Running the script and evaluating the result.");
            try {
                assert engine != null;
                result = engine.eval(script); // Run the script and place the result into an abstract object.
            } catch (ScriptException e) {
                if (ArtDept.loggingEnabled)
                    log.error("An error occurred with the script. Line number: " + e.getLineNumber(), e);
                JOptionPane.showMessageDialog(null, "An error occurred with the script.", "Script error / cancel", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (ArtDept.loggingEnabled)
                log.debug(result); // Check that we received the correct information back from the script.
            if (ArtDept.loggingEnabled) log.debug("");
            resultUntrimmed = result.toString(); // Convert it to a String.
        } else {
            // Otherwise, if it's Windows, then we don't need to set up variables ahead of time.
            String jsonIntermediate = outgoing.toString();
            //noinspection RegExpRedundantEscape
            jsonOut = jsonIntermediate.replaceAll("\\\"", "~\"");
            if (ArtDept.loggingEnabled) log.debug("JSON-Out:\n" + jsonOut);

            Process p = null;
            ExecutorService executor = null;
            try {
                executor = Executors.newFixedThreadPool(10);
                //noinspection SpellCheckingInspection
                p = Runtime.getRuntime().exec("cscript //NoLogo " + scriptFile + " " + jsonOut + " " + ArtDept.loggingEnabled + " " + (prefs.get(Settings.PREFS_DIR_KEY, Settings.PREFS_DIR_DEFAULT) + File.separator));
                Future<String> fut1 = executor.submit(new ReadStreamWithCall("stdin", p.getInputStream()));
//				Future<String> fut2 = executor.submit(new ReadStreamWithCall("stdin", p.getErrorStream()));
                resultUntrimmed = fut1.get();
                p.waitFor();
                if (ArtDept.loggingEnabled) log.debug("Done waiting for the script to execute.");
            } catch (Exception e) {
                if (ArtDept.loggingEnabled) log.error("An error occurred with the script.", e);
                JOptionPane.showMessageDialog(null, "An error occurred with the script.", "Script error / cancel", JOptionPane.ERROR_MESSAGE);
                return false;
            } finally {
                if (p != null) {
                    p.destroy();
                }
                if (executor != null) {
                    executor.shutdown();
                }
            }

        }

        if (ArtDept.loggingEnabled) log.debug("Trimming the result.");
        String resultStr = resultUntrimmed.trim();
//		System.out.println(resultStr);
        if (ArtDept.loggingEnabled)
            log.debug(resultStr); // Check that we received the correct information back from the script.
        if (isInteger(resultStr)) {
            /*
                Result possibilities:
                 0: Unused. Normally means that everything ran without errors, but since we are expecting an array instead, we will leave this unused for now.
                 1: Connection to server not found.
                 2: MRAP error returned from Illustrator.
                 3: Another type of error returned from Illustrator (besides MRAP).
                 4: No files with the given order number were found.
                 5: Product type (multicolor) does not exist in script.
                 6: Tried to create a new "thousands" folder and failed. If problem persists, create the folder manually.
                 7: Cancel button was pressed.
                 8: Could not find existing files to rename them.
                 9: Could not find InDesign template file.
                10: InDesign is crashing / has crashed.
                11: Script could not continue because another InDesign file was open.
                12: Extra files/folders in the job folder.
            */
            int x = Integer.parseInt(resultStr);
            switch (x) {
                case 0 -> {
                    if (ArtDept.loggingEnabled) log.trace("The unused '0' error code was returned!");
                    return false;
                }
                case 1 -> {
                    if (ArtDept.loggingEnabled) log.trace("Connection to server not found.");
                    return false;
                }
                case 2 -> {
                    if (ArtDept.loggingEnabled) log.trace("MRAP error returned from Illustrator.");
                    return false;
                }
                case 3 -> {
                    if (ArtDept.loggingEnabled)
                        log.trace("Another type of error returned from Illustrator (besides MRAP).");
                    return false;
                }
                case 4 -> {
                    if (ArtDept.loggingEnabled) log.trace("No files with the given order number were found.");
                    return false;
                }
                case 5 -> {
                    if (ArtDept.loggingEnabled) log.trace("Product type (multicolor) does not exist in script.");
                    return false;
                }
                case 6 -> {
                    if (ArtDept.loggingEnabled)
                        log.trace("Tried to create a new 'thousands' folder and failed. If problem persists, create the folder manually.");
                    return false;
                }
                case 7 -> {
                    if (ArtDept.loggingEnabled) log.trace("Cancel button pressed.");
                    return false;
                }
                case 8 -> {
                    if (ArtDept.loggingEnabled) log.trace("Could not find existing files to rename them.");
                    return false;
                }
                case 9 -> {
                    if (ArtDept.loggingEnabled) log.trace("Could not find InDesign template file.");
                    return false;
                }
                case 10 -> {
                    if (ArtDept.loggingEnabled) log.trace("InDesign is crashing / has crashed.");
                    return false;
                }
                case 11 -> {
                    if (ArtDept.loggingEnabled)
                        log.trace("Script could not continue because another InDesign file was open.");
                    return false;
                }
                case 12 -> {
                    if (ArtDept.loggingEnabled) log.trace("Extra files/folders in the job folder.");
                    return false;
                }
                case 100 -> {
                    if (ArtDept.loggingEnabled)
                        log.trace("Script did not return any info to the Applescript script, so it was undefined.");
                    return false;
                }
                default -> {
                    if (ArtDept.loggingEnabled) log.trace("Script exited by some other means.");
                    return false;
                }
            }
        }

        // Parse the returned string into a JSON Object.
        JSONObject thisOrder = new JSONObject(resultStr);
        JSONArray lineItems = thisOrder.getJSONArray("lineItems");
        if (ArtDept.loggingEnabled) log.debug("JSON Object: " + thisOrder);

        if (ArtDept.loggingEnabled) log.debug("Ship Date as epoch int: " + thisOrder.getLong("shipDate"));


//		String[] aResult = resultStr.split("@");  // Convert the String into an Array of Strings.
//		String[] aTemp; // This temporary Array will hold the items common to each product in an order.
//		ArrayList<String[]> aJobDetails = new ArrayList<>(); // Setting up a two-dimensional ArrayList of Arrays.
//		for (int i = 0; i < aResult.length; i+=29) { // Go through each block of items in the list and add them to the ArrayList.
//			aTemp = Arrays.copyOfRange(aResult, i, i+29);
//			aJobDetails.add(aTemp);
//		}

        // If we got nothing back, then return with a failure condition.
        if (resultStr.length() < 1) {
            if (ArtDept.loggingEnabled) log.trace("Nothing returned from script. User possibly cancelled it.");
            return false;
        }


        // Now to insert the returned data into the database.

        // First, place the day's info into the `production_maxes` table.
        // This starts by getting the default production maxes for each print type.
        // (Just get all of the print types from the `print_types` table.)
        ArrayList<PrintType> printTypes;
        try {
            printTypes = PrintTypeManager.getAllPrintTypes();
        } catch (SQLException err) {
            if (ArtDept.loggingEnabled) log.error(err);
            err.printStackTrace();
            return false;
        }

        // Next, for each print type, insert a row into the `production_maxes` table for this order's date.
        Date orderDate = new Date(thisOrder.getLong("shipDate"));
        boolean successfulProductionMaxInsertion = false;
        assert printTypes != null;
        for (PrintType printType : printTypes) {
            try {
                if (!ProductionMaxesManager.maxExists(orderDate, printType)) {
                    successfulProductionMaxInsertion = ProductionMaxesManager.insert(orderDate, printType);
                }
                if (successfulProductionMaxInsertion) {
                    if (ArtDept.loggingEnabled)
                        log.debug("New production_max row with date of " + orderDate + " and printType of " + printType.getId() + " was inserted!");
                }
            } catch (SQLException err) {
                if (ArtDept.loggingEnabled) log.error(err);
                err.printStackTrace();
                return false;
            }
        }


        // Next, set the Order info bean.
        if (ArtDept.loggingEnabled) log.trace("Next, set the Order info bean.");
        customerName = thisOrder.getString("customerName");
        customerPO = thisOrder.getString("customerPO");

        bean.setId(thisOrder.getString("id"));
        bean.setProofSpecDate(new Date(thisOrder.getLong("proofSpecDate")));
        bean.setCustomerName(customerName);
        bean.setCustomerPO(customerPO);
        bean.setSampleShelfNote(thisOrder.getBoolean("sampleShelfNote"));
        if (scriptType == ScriptType.OUTPUT) {
            bean.setPrintingCompany(pCompanyEnum);
            bean.setShipDateId(orderDate);
            bean.setOverruns(thisOrder.getBoolean("overruns"));
            bean.setSigOutput(thisOrder.getString("outputterSignature"));
        } else { // Proofing
            bean.setPrintingCompany(PrintingCompany.getPrintingCompany(thisOrder.getInt("printingCompany")));
            bean.setShipDateId(DateManager.localDateToSqlDate(DateManager.PROOF_DATE));
            bean.setOverruns(overruns);
            bean.setSampleShelfNote(false); // Default to false. Will test for the note shortly to possibly change this.
            bean.setSigProof(thisOrder.getString("prooferSignature"));
        }

        // As part of setting the Order bean, set its LineItem List with LineItem beans.
        if (ArtDept.loggingEnabled)
            log.trace("As part of setting the Order bean, set its LineItem List with LineItem beans.");
//		Pattern prProof = Pattern.compile("Proof|NaN", Pattern.CASE_INSENSITIVE);
        List<LineItem> lineItemList = new ArrayList<>();
        boolean allDigitalProofs = true; // This boolean will store whether or not all of the items
        // in a multi-item proof are digital. If so, it should not be added to the database.
//		for (int i = 0; i < aJobDetails.size(); i++) {
        for (int i = 0; i < lineItems.length(); i++) {
            JSONObject thisLineItem = (JSONObject) lineItems.get(i);
            if (ArtDept.loggingEnabled) log.debug("Start: item " + i + ": " + thisLineItem.getString("productNum"));

            LineItem lineItemBean = new LineItem();
            lineItemBean.setOrderId(thisOrder.getString("id"));
            lineItemBean.setProductNum(thisLineItem.getString("productNum"));
            lineItemBean.setProductDetail(thisLineItem.getString("productDetail"));
            lineItemBean.setThumbnail(thisLineItem.getString("thumbnail"));
            if (scriptType == ScriptType.PROOF) {
                lineItemBean.setProofNum(thisLineItem.getInt("proofNumber"));
                lineItemBean.setProofDate(new Date(thisOrder.getLong("proofSpecDate")));
                lineItemBean.setFlags(thisLineItem.getInt("flags"));
                lineItemBean.setReorderNum(thisLineItem.getString("reorderNum"));
                lineItemBean.setPackingInstructions(thisLineItem.getString("packingInstructions"));
                lineItemBean.setPackageQuantity(thisLineItem.getString("packageQuantity"));
                lineItemBean.setCaseQuantity(thisLineItem.getString("caseQuantity"));
                try { // Since the Label Quantity field could be blank
                    lineItemBean.setLabelQuantity(thisLineItem.optInt("labelQuantity", 0));
//					lineItemBean.setLabelQuantity(thisLineItem.getInt("labelQuantity"));
                } catch (NumberFormatException err) {
                    if (ArtDept.loggingEnabled)
                        log.error("Could not parse Label Quantity into an integer. Setting it to 0 instead.");
                    lineItemBean.setLabelQuantity(0);
                }
                lineItemBean.setLabelText(thisLineItem.getString("labelText"));
                lineItemBean.setItemStatusId("woa");
            } else { // Output
                if (ArtDept.loggingEnabled) log.info("OUTPUT JOB");
                if (thisLineItem.getJSONArray("digitalArtFiles").length() > 0) {
                    if (ArtDept.loggingEnabled)
                        log.info("24: " + thisLineItem.getJSONArray("digitalArtFiles").getString(0));
                }
                lineItemBean.setProofNum(proofNum);
                lineItemBean.setFlags(thisLineItem.getInt("flags"));
                lineItemBean.setReorderNum(thisLineItem.getString("reorderNum"));
                lineItemBean.setPackingInstructions(thisLineItem.getString("packingInstructions"));
                lineItemBean.setPackageQuantity(thisLineItem.getString("packageQuantity"));
                lineItemBean.setCaseQuantity(thisLineItem.getString("caseQuantity"));
                try { // Since the Label Quantity field could be blank
                    lineItemBean.setLabelQuantity(thisLineItem.getInt("labelQuantity"));
                } catch (NumberFormatException err) {
                    if (ArtDept.loggingEnabled)
                        log.error("Could not parse Label Quantity into an integer. Setting it to 0 instead.");
                    lineItemBean.setLabelQuantity(0);
                }
                lineItemBean.setLabelText(thisLineItem.getString("labelText"));

                lineItemBean.setItemStatusId(thisLineItem.getString("itemStatusId"));
//				detailBean.setDigitalFilename(thisItem.getJSONArray("digitalArtFiles").getString(0));
                JSONArray artFiles = thisLineItem.getJSONArray("digitalArtFiles");
                List<Artwork> newArtList = lineItemBean.getArtworkList();
                if (ArtDept.loggingEnabled) log.debug("existingArtList is: " + newArtList.toString());

                // Get all "Artwork" rows from db where the od id matches this od's id. Store in an ArrayList.

                // Loop through the incoming JSONArray and update any of those rows in the db, one at a time.
                int j = -1;
                while (++j < artFiles.length()) {
                    String artFileText = artFiles.getString(j);
                    if (ArtDept.loggingEnabled) log.debug("Storing art file # " + j);
                    if (ArtDept.loggingEnabled) log.debug("Creating new art bean and adding it to the list.");
                    Artwork thisArt = new Artwork();
                    thisArt.setDigitalArtFile(artFileText);
                    newArtList.add(thisArt);
                }

            }
            if (ArtDept.loggingEnabled) log.info("Package Quantity in bean: " + lineItemBean.getPackageQuantity());
            if (ArtDept.loggingEnabled) log.info("Case Quantity in bean: " + lineItemBean.getCaseQuantity());
//			if (ArtDept.loggingEnabled) log.info("Digital Filename in bean: " + detailBean.getDigitalFilename());
            try {
                lineItemBean.setQuantity(thisLineItem.getLong("quantity"));
                // Check that quantity for 0 (Digital Proof) and set the boolean accordingly.
                if (lineItemBean.getQuantity() > 0) allDigitalProofs = false;

                lineItemBean.setNumImpressions(thisLineItem.getLong("numImpressions"));
                lineItemBean.setImpressionsTradition(thisLineItem.getLong("impressionsTradition"));
                lineItemBean.setImpressionsHiSpeed(thisLineItem.getLong("impressionsHiSpeed"));
                lineItemBean.setImpressionsDigital(thisLineItem.getLong("impressionsDigital"));

                lineItemBean.setPrintTypeId(PrintTypeEnum.getSqlValue(PrintTypeEnum.getPrintType(thisLineItem.getInt("printType"))));

            } catch (Exception e) {
                if (ArtDept.loggingEnabled)
                    log.error("Could not parse the quantity, number of colors, or print type.", e);
                JOptionPane.showMessageDialog(null, "Could not parse the quantity, number of colors, or print type.  Please fix and run again.", "Parse error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            lineItemList.add(lineItemBean);

        }
        if (ArtDept.loggingEnabled) {
            for (int k = 0; k < lineItemList.size(); k++) {
                log.debug("End: item " + k + ": " + lineItemList.get(k).getProductNum());
            }
        }

        // Now check that boolean to see if all of the items are Digital Proofs only.
        // If so, just immediately return without putting anything into the database.
        if (ArtDept.loggingEnabled)
            log.trace("All of the items in this job are Digital Proofs only: " + allDigitalProofs);
        if (allDigitalProofs) return true;

        // Place the job info into the Order table.  This should only insert one row.
        // If the job already exists, though, just update it.
        boolean successfulOrderInsertion;
        try {
            if (OrderManager.getRow(bean.getId()) == null) {
                if (ArtDept.loggingEnabled) log.debug("INSERTing a new Order entry into the Order table.");
                successfulOrderInsertion = OrderManager.insert(bean); // Insert the job & order detail in the database.
            } else {
                if (ArtDept.loggingEnabled) log.debug("UPDATing the existing Order in the Order table.");
                successfulOrderInsertion = OrderManager.update(bean); // Update the job & order detail in the database.
            }
        } catch (Exception e) {
            if (ArtDept.loggingEnabled) log.error("Error inserting/updating Order data in the database", e);
            JOptionPane.showMessageDialog(null, "Error inserting Order data into database", "Database error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Log the successfulness of the job insertion/update-ion.
        if (successfulOrderInsertion) {
            if (ArtDept.loggingEnabled) log.debug("Order id " + bean.getId() + " was inserted/updated!");
        } else {
            if (ArtDept.loggingEnabled) log.debug("One or more items did NOT make it into the database.");
            return false;
        }


        // This next section should only run if there are existing LineItem items in the database.
        boolean successfulLineItemInsertion = false;
        if (bean.getLineItemList() != null) {
            // This is where things get tricky. We need to get a list of the .INDD files in the job directory as an ArrayList of Strings first.
            // We then compare this List of Strings to the incoming LineItem List and pop out any matches from the List of Strings.
            // What we're left with is a list of only those items that have NOT been updated / added to the job.
            List<File> protectionList = getProtectionList(jobNumber, lineItemList);

            // We want to protect these items and not update/delete them from the database. To that end, we now compare this list
            // to the existing list of LineItem items that are in the database. Matches are popped out of that existing list,
            // so that all that remains should be only the items to be updated or deleted.
            List<LineItem> modifiedODList = protectODs(bean.getLineItemList(), protectionList);

            // Now we compare our two Lists of LineItem items and add/update/remove, depending upon how many are in each list.
            successfulLineItemInsertion = updateTables(lineItemList, modifiedODList, scriptType);

            // As a final step, check to see if there are some items in the protectionList that do NOT exist in the table.
            // This can happen if InDesign crashed mid-way through a multi-item order or some such.
            // If this is the case, then go ahead and add dummy entries to the database, so at least SOMETHING is there.
            addDummyEntries(bean.getLineItemList(), protectionList);
        } else { // No existing LineItem items in the database.
            bean.setLineItemList(lineItemList);
            addDummyEntries(bean.getLineItemList(), bean.getId());
            try {
                for (LineItem lineItem : bean.getLineItemList()) {
                    if (ArtDept.loggingEnabled) log.debug("INSERTing a new LineItem entry into the LineItem table.");
                    successfulLineItemInsertion = LineItemManager.insert(lineItem); // Insert the order detail in the database.
                }
            } catch (Exception err) {
                if (ArtDept.loggingEnabled) log.error("Error inserting LineItem data in the database", err);
                JOptionPane.showMessageDialog(null, "Error inserting Line Item data into database", "Database error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // Insert/Update/Delete Artworks into/from the database.
        for (LineItem thisLineItem : lineItemList) {
            if (ArtDept.loggingEnabled)
                log.debug("Number of Artworks in this Line Item bean's list: " + thisLineItem.getArtworkList().size());

            try {
                // Get the Artworks from the database that are a part of the same LineItem that we're looking at.
                ArrayList<Artwork> existingArtworks = ArtworkManager.getArtworksByOrderId(thisLineItem.getId());
                if (ArtDept.loggingEnabled) {
                    assert existingArtworks != null;
                    log.debug("existingArtworks: " + existingArtworks);
                }

                // Loop through to insert/update/delete Artworks.
                int j = 0;
                while (j < thisLineItem.getArtworkList().size()) {
                    if (ArtDept.loggingEnabled) log.debug("Checking art number " + j + ".");
                    Artwork thisArt = thisLineItem.getArtworkList().get(j);
                    if (ArtDept.loggingEnabled) log.debug("This art file's name: " + thisArt.getDigitalArtFile());
                    assert existingArtworks != null;
                    if (existingArtworks.size() > 0
                            && existingArtworks.size() - 1 >= j
                            && existingArtworks.get(j) != null) {
                        // If there is an Artwork already in the database at this "slot",
                        // go ahead and replace it with the new one.
                        // This is done by setting the incoming Artwork object's ID
                        // to the old one, then overwriting the old one in the db with the new one.
                        if (ArtDept.loggingEnabled)
                            log.debug("Found an existing artwork in the same \"slot\" (#" + j + ", ID #" + existingArtworks.get(j).getId() + "). Updating it with new Artwork.");
                        thisArt.setId(existingArtworks.get(j).getId());
                        thisArt.setLineItemId(existingArtworks.get(j).getLineItemId());
                        if (ArtDept.loggingEnabled) log.debug("Updating with Art File: " + thisArt.getDigitalArtFile());
                        ArtworkManager.update(thisArt);
                    } else {
                        // If an Artwork doesn't exist in the database for this "slot",
                        // then create a new one with our new Artwork.
                        if (ArtDept.loggingEnabled)
                            log.debug("No more Artworks found for this LineItem in database (Slot #" + j + "). Inserting this new Artwork.");
                        thisArt.setLineItemId(thisLineItem.getId());
                        ArtworkManager.insert(thisArt);
                    }
                    j++;
                }
                if (ArtDept.loggingEnabled) log.debug("j is now: " + j);
                while (true) {
                    assert existingArtworks != null;
                    if (!(j < existingArtworks.size())) break;
                    // If there are still Artworks in the database after we've already gone through
                    // all of the new Artworks, just delete whatever is left.
                    if (ArtDept.loggingEnabled)
                        log.debug("No more new artworks, so deleting an existing Artwork from the database. Slot #" + j + "; ID #" + existingArtworks.get(j).getId());
                    ArtworkManager.delete(existingArtworks.get(j++).getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // Log the successfulness of the insertion/update-ion.
        if (successfulLineItemInsertion) {
            if (ArtDept.loggingEnabled) log.debug("At least one row was inserted/updated!");
        } else {
            if (ArtDept.loggingEnabled) log.debug("One or more items did NOT make it into the database.");
            return false;
        }


        if (ArtDept.loggingEnabled) log.trace("Completed main section.");

//		ConnectionManager.getInstance().close();

        // Get the first day of the week for this order.
        final DayOfWeek firstDayOfWeek = WeekFields.of(Locale.US).getFirstDayOfWeek();
        LocalDate weekStart = bean.getShipDateId().toLocalDate().with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
        if (ArtDept.loggingEnabled) log.info("current ship date id: " + bean.getShipDateId().toString());
        if (ArtDept.loggingEnabled) log.info("weekStart: " + weekStart.toString());

        // Pluck all of the Item Statuses and Print Types from the order's LineItems.
        List<String> itemStatuses = bean.getLineItemList().stream().map(LineItem::getItemStatusId).collect(Collectors.toList());
        List<String> itemPrintTypes = bean.getLineItemList().stream().map(LineItem::getPrintTypeId).collect(Collectors.toList());

        final String cachePrefix = "sky_schedule_database_sky_schedule_cache:";

        // Flush certain keys from the Redis data store.
        if (ArtDept.loggingEnabled) log.info("Before deleting, order-by-id for " + bean.getId() + " is: " +
                RedisManager.getInstance().getCommands().get(cachePrefix + "order-by-id:" + bean.getId()));

        RedisManager.getInstance().getCommands().del(
                cachePrefix + "chart:print-type-pie:" + firstDayOfWeek,
                cachePrefix + "chart:impressions-per-print-type",
                cachePrefix + "production-maxes:" + bean.getShipDateId().toString(),
                cachePrefix + "daily-totals:" + bean.getShipDateId().toString(),
                cachePrefix + "weekly-totals:" + weekStart.toString(),
                cachePrefix + "weekly-order-progress:" + weekStart,
                cachePrefix + "weekly-line-item-progress:" + weekStart,
                cachePrefix + "orders-by-date:" + bean.getShipDateId().toString(),
                cachePrefix + "recent-orders",
                cachePrefix + "order-by-id:" + bean.getId()
        );

        if (ArtDept.loggingEnabled) log.info("After deleting, order-by-id for " + bean.getId() + " is: " +
                RedisManager.getInstance().getCommands().get(cachePrefix + "order-by-id:" + bean.getId()));

        for (String status : itemStatuses) {
            RedisManager.getInstance().getCommands().del(cachePrefix + "orders-by-status:" + status);
        }
        for (String printType : itemPrintTypes) {
            RedisManager.getInstance().getCommands().del(cachePrefix + "orders-by-print-type:" + printType);
        }

        return true;
    }


    private static boolean updateTables(List<LineItem> incomingList, List<LineItem> existingList, ScriptType scriptType) {
        if (ArtDept.loggingEnabled) log.entry("updateTables (ScriptManager)");

        // Last part here occurs if there are items in both lists. Update until one of them drops to 0.
        Iterator<LineItem> incomingIterator = incomingList.iterator();
        Iterator<LineItem> existingIterator = existingList.iterator();

        if (ArtDept.loggingEnabled) log.debug("Incoming list has " + incomingList.size() + " elements.");
        if (ArtDept.loggingEnabled) log.debug("Existing list has " + existingList.size() + " elements.");

        try {
            if (scriptType == ScriptType.PROOF) {
                while (incomingIterator.hasNext() && existingIterator.hasNext()) {
                    if (ArtDept.loggingEnabled) log.debug("Updating an existing Line Item. (Proofing)");
                    LineItem incomingOD = incomingIterator.next();
                    LineItem existingOD = existingIterator.next();
                    incomingOD.setId(existingOD.getId());
                    if (incomingOD.getProofDate() == null)
                        incomingOD.setProofDate(existingOD.getProofDate());
                    LineItemManager.update(incomingOD);
                    // Not sure if this break should be commented out. If not, the while loop only runs once.
//					break;
                }
            } else { // Output.
                while (incomingIterator.hasNext()) {
                    LineItem incomingOD = incomingIterator.next();
                    while (existingIterator.hasNext()) {
                        LineItem existingOD = existingIterator.next();
                        if (incomingOD.getProductDetail().equals(existingOD.getProductDetail())
                                && incomingOD.getProductNum().equals(existingOD.getProductNum())) {
                            if (ArtDept.loggingEnabled) log.debug("Updating an existing Line Item. (Output)");
                            incomingOD.setId(existingOD.getId());
                            if (incomingOD.getProofDate() == null)
                                incomingOD.setProofDate(existingOD.getProofDate());
                            LineItemManager.update(incomingOD);
                            existingIterator = existingList.iterator();
                            break;
                        }
                    }
                }
            }

            if (scriptType == ScriptType.PROOF) {
                while (incomingIterator.hasNext()) {
                    if (ArtDept.loggingEnabled) log.debug("Inserting a new Line Item.");
                    LineItem incomingOD = incomingIterator.next();
                    LineItemManager.insert(incomingOD);
                }
                while (existingIterator.hasNext()) {
                    if (ArtDept.loggingEnabled) log.debug("Deleting an existing Line Item.");
                    LineItem existingOD = existingIterator.next();
                    LineItemManager.delete(existingOD.getId());
                }
            }
        } catch (Exception err) {
            if (ArtDept.loggingEnabled)
                log.error("Error while attempting to update/insert/delete data in database.", err);
            return false;
        }

        return true;
    }

    private static List<LineItem> protectODs(List<LineItem> existingList, List<File> protectionList) {
        if (ArtDept.loggingEnabled) log.entry("protectODs (ScriptManager)");

        List<LineItem> trimmedList = new ArrayList<>(existingList);
        Iterator<File> fileIterator = protectionList.iterator();
        Iterator<LineItem> odIterator;
        String itemDetail;

        // Since it is entirely possible for an item in the protectionList to NOT exist in the existingList,
        // when we find a match, we pop that item out of BOTH lists. If any files remain in the protectionList afterward,
        // then we can add 'dummy' entries into the database, so at least there's SOMETHING in there.
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
            odIterator = trimmedList.iterator();
            while (odIterator.hasNext()) {
                LineItem orderDetail = odIterator.next();
                if (ArtDept.loggingEnabled)
                    log.debug("Comparing " + orderDetail.getProductDetail() + " (existing item in db) to " + itemDetail + " (item in protection list).");
                if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals("")) || orderDetail.getProductDetail().equals(itemDetail)) {
                    if (ArtDept.loggingEnabled)
                        log.debug("Found a match. Removing from trimmed list. (Item protected)");
                    odIterator.remove();
//					fileIterator.remove();
                    break;
                }
            }
        }

        return trimmedList;
    }

    private static void addDummyEntries(List<LineItem> existingList, List<File> protectionList) {
        if (ArtDept.loggingEnabled) log.entry("addDummyEntries (ScriptManager)");

        Iterator<File> fileIterator = protectionList.iterator();
        Iterator<LineItem> odIterator;
        String itemDetail;

        // Since it is entirely possible for an item in the protectionList to NOT exist in the existingList,
        // when we find a match, we pop that item out of BOTH lists. If any files remain in the protectionList afterward,
        // then we can add 'dummy' entries into the database, so at least there's SOMETHING in there.
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
            odIterator = existingList.iterator();
            while (odIterator.hasNext()) {
                LineItem orderDetail = odIterator.next();
                if (ArtDept.loggingEnabled)
                    log.debug("Comparing " + orderDetail.getProductDetail() + " to " + itemDetail);
                if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
                        || itemDetail == null
                        || orderDetail.getProductDetail().equals(itemDetail)
                        || StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
                        || StringUtils.containsIgnoreCase(itemDetail, "Carousel")
                        || StringUtils.containsIgnoreCase(itemDetail, "cover")
                        || StringUtils.containsIgnoreCase(itemDetail, "label")) {
                    if (ArtDept.loggingEnabled)
                        log.debug("Found a match. Removing from the protectionList. (Will NOT be added as a dummy item.)");
                    fileIterator.remove();
                    break;
                }
            }
        }

        if (protectionList.size() > 0) {
            for (File file : protectionList) {
                LineItem bean = new LineItem();
                bean.setOrderId(StringUtils.substringBefore(file.getName(), "_"));
                bean.setProductNum("");
                bean.setProductDetail(StringUtils.substringBetween(file.getName(), "_", "."));
                bean.setPrintTypeId("pad");
                bean.setNumImpressions(0);
                bean.setImpressionsTradition(0);
                bean.setImpressionsHiSpeed(0);
                bean.setImpressionsDigital(0);
                bean.setQuantity(0);
//				bean.setProofDate(new Timestamp(DateTimeUtils.currentTimeMillis()));
                bean.setProofDate(new Date(new java.util.Date().getTime()));

                try {
                    LineItemManager.insert(bean);
                } catch (Exception err) {
                    if (ArtDept.loggingEnabled)
                        log.error("Exception when trying to insert a dummy LineItem into the database.", err);
                }
            }
        }

    }


    private static void addDummyEntries(List<LineItem> incomingList, String jobNumber) {
        if (ArtDept.loggingEnabled) log.entry("addDummyEntries (when bean doesn't exist yet) (ScriptManager)");

        String jobPrefix = jobNumber.substring(0, 3);
        String jobFolderBeginning = Settings.PATH_JOBS + File.separator + jobPrefix + "000-" + jobPrefix + "999" + File.separator;
        if (ArtDept.loggingEnabled) log.info("[addDummyEntries] jobFolderBeginning: " + jobFolderBeginning);
        File searchFolder = new File(jobFolderBeginning + jobNumber + "/");
        if (!searchFolder.exists()) searchFolder = new File(jobFolderBeginning + jobNumber + " Folder/");

        String[] inDesign = {"indd"};
        List<File> fileList = (List<File>) FileUtils.listFiles(searchFolder, inDesign, true);
        Iterator<File> fileIterator = fileList.iterator();
        Iterator<LineItem> odIterator;
        String itemDetail;

        // This is similar to the previous addDummyEntries method, but this one is called when no job exists already
        // in the database.
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
            odIterator = incomingList.iterator();
            while (odIterator.hasNext()) {
                LineItem orderDetail = odIterator.next();
                if (ArtDept.loggingEnabled)
                    log.debug("Comparing " + orderDetail.getProductDetail() + " (incoming item) to " + itemDetail + " (existing file in directory).");
                if (itemDetail != null && (orderDetail.getProductDetail().equals("") && !itemDetail.equals(""))
                        || itemDetail == null
                        || orderDetail.getProductDetail().equals(itemDetail)
                        || StringUtils.containsIgnoreCase(itemDetail, "MiniPad")
                        || StringUtils.containsIgnoreCase(itemDetail, "Carousel")
                        || StringUtils.containsIgnoreCase(itemDetail, "cover")
                        || StringUtils.containsIgnoreCase(itemDetail, "label")) {
                    if (ArtDept.loggingEnabled)
                        log.debug("Found a match. Removing from the existing File list. (Will not be added as a dummy item to the database.)");
                    fileIterator.remove();
                    break;
                }
            }
        }

        if (fileList.size() > 0) {
            for (File file : fileList) {
                LineItem bean = new LineItem();
                bean.setOrderId(StringUtils.substringBefore(file.getName(), "_"));
                bean.setProductNum("");
                bean.setProductDetail(StringUtils.substringBetween(file.getName(), "_", "."));
                bean.setPrintTypeId("pad");
                bean.setNumImpressions(0);
                bean.setImpressionsTradition(0);
                bean.setImpressionsHiSpeed(0);
                bean.setImpressionsDigital(0);
                bean.setQuantity(0);
//				bean.setProofDate(new Timestamp(DateTimeUtils.currentTimeMillis()));
                bean.setProofDate(new Date(new java.util.Date().getTime()));

                try {
                    LineItemManager.insert(bean);
                } catch (Exception err) {
                    if (ArtDept.loggingEnabled)
                        log.error("Exception when trying to insert a dummy LineItem into the database.", err);
                }
            }
        }

    }


    /**
     * @param jobNumber The number of the Order
     * @param odList    List of LineItems
     * @return List<File> A list of files that will be protected from deletion/updating.
     * These are files that exist in the folder but are NOT in the incoming list of jobs.
     */
    private static List<File> getProtectionList(String jobNumber, List<LineItem> odList) {
        if (ArtDept.loggingEnabled) log.entry("getProtectionList (ScriptManager)");

        String jobPrefix = jobNumber.substring(0, 3);
        String jobFolderBeginning = Settings.PATH_JOBS + File.separator + jobPrefix + "000-" + jobPrefix + "999" + File.separator;
        File searchFolder = new File(jobFolderBeginning + jobNumber + "/");
        if (!searchFolder.exists()) searchFolder = new File(jobFolderBeginning + jobNumber + " Folder/");

        String[] inDesign = {"indd"};
        List<File> fileList = (List<File>) FileUtils.listFiles(searchFolder, inDesign, true);
        Iterator<File> fileIterator;
        String itemDetail;

        for (LineItem od : odList) {
            fileIterator = fileList.iterator();
            while (fileIterator.hasNext()) {
                File file = fileIterator.next();
                itemDetail = StringUtils.substringBetween(file.getName(), "_", ".");
                if (itemDetail == null) itemDetail = "";
                if (ArtDept.loggingEnabled)
                    log.debug("Comparing " + od.getProductDetail() + " (incoming item) to " + itemDetail + " (existing file in directory).");
                if (itemDetail.equals(od.getProductDetail())
                        || itemDetail.equals("MiniPad")
                        || itemDetail.equals("Carousel")
                        || itemDetail.equals("cover")
                        || itemDetail.equals("label")) {
                    if (ArtDept.loggingEnabled)
                        log.debug("Found a match. Removing from the protectionList. (Will be deleted/updated later.)");
                    fileIterator.remove();
//					break;
                }
            }
        }

        return fileList;
    }

    private static String getPrintCompanyString(PrintingCompany printingCompany) {
        return StringUtils.substringBefore(StringUtils.substringAfter(printingCompany.toString(), "_"), "_");
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
