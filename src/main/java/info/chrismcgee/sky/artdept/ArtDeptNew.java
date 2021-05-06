package info.chrismcgee.sky.artdept;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.jthemedetecor.OsThemeDetector;
import info.chrismcgee.components.DateManager;
import info.chrismcgee.components.Sanitizer;
import info.chrismcgee.sky.beans.LineItem;
import info.chrismcgee.sky.beans.Order;
import info.chrismcgee.sky.enums.DBType;
import info.chrismcgee.sky.enums.PrintingCompany;
import info.chrismcgee.sky.enums.ScriptType;
import info.chrismcgee.sky.tables.OrderManager;
import info.chrismcgee.util.ConnectionManager;
import info.chrismcgee.util.SendMail;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtDeptNew extends JFrame {
    private JPanel contentPane;
    private JButton buttonProof;
    private JButton buttonOutput;
    private JButton buttonCancel;
    private JTextField tfOrderNum;

    // App identification
    private static final String APP_NAME = "Sky Launcher";
    private static final String APP_VERSION = "5.0.0";
    private static final String DEFAULT_TITLE = APP_NAME + " v" + APP_VERSION;
    // Logging
    static final Logger log = LogManager.getLogger(ArtDeptNew.class.getName());
    public static boolean loggingEnabled = false;
    // Preferences variables
    public final Preferences prefs = Preferences.userNodeForPackage(Settings.class);
    // The order's text file and its variables, pre-defined here
    private File textFile = null;
    private String customerServiceRep = "";
    private boolean creditCard = false;
    private int shipDays = 0;
    private String wnaPo = "";
    // "Job Complete" JFrame
    private JFrame frm;
    // List of app icon images
    public static List<Image> icons = new ArrayList<>();

    private static final String PREFS_ARTDEPT_X_KEY = "artdept_x";
    private static final int PREFS_ARTDEPT_X_DEFAULT = 100;

    private static final String PREFS_ARTDEPT_Y_KEY = "artdept_y";
    private static final int PREFS_ARTDEPT_Y_DEFAULT = 100;

    private static final String PREFS_ARTDEPT_WIDTH_KEY = "artdept_width";
    private static final int PREFS_ARTDEPT_WIDTH_DEFAULT = 400;

    private static final String PREFS_ARTDEPT_HEIGHT_KEY = "artdept_height";
    private static final int PREFS_ARTDEPT_HEIGHT_DEFAULT = 120;

    public ArtDeptNew() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonProof);

        MenuListener settingsListener = new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                openSettings();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                // Don't do anything
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                // Don't do anything
            }
        };

        // Set the application icons
        setIconImages(icons);

        // Save the location and size of the dialog whenever either are changed
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent evt) {
                prefs.putInt(PREFS_ARTDEPT_X_KEY, getX());
                prefs.putInt(PREFS_ARTDEPT_Y_KEY, getY());
            }

            @Override
            public void componentResized(ComponentEvent evt) {
                prefs.putInt(PREFS_ARTDEPT_WIDTH_KEY, getWidth());
                prefs.putInt(PREFS_ARTDEPT_HEIGHT_KEY, getHeight());
            }
        });

        buttonProof.addActionListener(e -> onProof());

        buttonOutput.addActionListener(e -> onOutput());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // call onProof() on CTRL+P and F1
        contentPane.registerKeyboardAction(e -> onProof(), KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onProof(), KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // call onOutput() on CTRL+O and F2
        contentPane.registerKeyboardAction(e -> onOutput(), KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> onOutput(), KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Menu bar that allows access to the Settings dialog
        JMenu mnSettings = new JMenu("Settings");
        mnSettings.setMnemonic('s');
        mnSettings.addMenuListener(settingsListener);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(mnSettings);
        setJMenuBar(menuBar);

        // Text field for the user to enter the job/order number
        tfOrderNum.getDocument().addDocumentListener(new DocumentListener() {
            // When the user types or deletes characters, validate the input
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateAll();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateAll();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Plain text components do not fire these events
            }
        });

        // Set the focus on the Order Number text field.
        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                tfOrderNum.requestFocusInWindow();
            }
        });

        // Validate upon initializing this dialog
        validateAll();
    }

    private void openSettings() {
        // Create the Settings dialog
        Settings settings = new Settings();

        // Set the location of the Settings dialog
        settings.pack();
        settings.setLocationRelativeTo(this);

        // Show the Settings dialog
        settings.setVisible(true);
    }

    private void onProof() {
        if (validateAll()) {
            if (loggingEnabled) log.trace("Proofing button hit!");
            Thread proofThread = new Thread(() -> {
                try {
                    callScript(ScriptType.PROOF);
                } catch (InterruptedException e) {
                    if (loggingEnabled) log.error("Interrupted!", e);
                }
            });
            proofThread.start();
        }
    }

    private void onOutput() {
        if (validateAll()) {
            if (loggingEnabled) log.trace("Output button hit!");
            Thread outputThread = new Thread(() -> {
                try {
                    callScript(ScriptType.OUTPUT);
                } catch (InterruptedException e) {
                    if (loggingEnabled) log.error("Interrupted!", e);
                }
            });
            outputThread.start();
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        // Set the look and feel
        final OsThemeDetector detector = OsThemeDetector.getDetector();

        // Setting the base look and feel
        installTheme(detector.isDark());

        // Detect OS theme changes
        detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> installTheme(isDark)));

        // Set the icons to be used for the Frame. This MUST come before the frame is instantiated.
        try {
            icons.add(ImageIO.read(Objects.requireNonNull(ArtDeptNew.class.getClassLoader().getResource("images/sky_launcher-02_16x16.png"))));
            icons.add(ImageIO.read(Objects.requireNonNull(ArtDeptNew.class.getClassLoader().getResource("images/sky_launcher-02_32x32.png"))));
            icons.add(ImageIO.read(Objects.requireNonNull(ArtDeptNew.class.getClassLoader().getResource("images/sky_launcher-02_48x48.png"))));
            icons.add(ImageIO.read(Objects.requireNonNull(ArtDeptNew.class.getClassLoader().getResource("images/sky_launcher-02_256x256.png"))));
            icons.add(ImageIO.read(Objects.requireNonNull(ArtDeptNew.class.getClassLoader().getResource("images/sky_launcher-02_512x512.png"))));
            icons.add(ImageIO.read(Objects.requireNonNull(ArtDeptNew.class.getClassLoader().getResource("images/sky_launcher-02_768x768.png"))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create the frame
        ArtDeptNew frame = new ArtDeptNew();
        frame.setTitle(DEFAULT_TITLE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Set the location and size of the window
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setMinimumSize(new Dimension(
                frame.prefs.getInt(PREFS_ARTDEPT_WIDTH_KEY, PREFS_ARTDEPT_WIDTH_DEFAULT),
                frame.prefs.getInt(PREFS_ARTDEPT_HEIGHT_KEY, PREFS_ARTDEPT_HEIGHT_DEFAULT)
        ));
        frame.setLocation(new Point(
                frame.prefs.getInt(PREFS_ARTDEPT_X_KEY, PREFS_ARTDEPT_X_DEFAULT),
                frame.prefs.getInt(PREFS_ARTDEPT_Y_KEY, PREFS_ARTDEPT_Y_DEFAULT)
        ));

        // Show the frame
        frame.setVisible(true);
    }

    /**
     * This method is called when the user has clicked on the "ScriptManager" button
     * (or pressed one of its keyboard shortcuts) after the Order Number field
     * has been sanitized. It will first attempt to get the Job's info from
     * the database (including the highest proof number). Failing that, it will
     * then get the necessary customer information from an appropriate text file
     * generated from Customer Service. If the text file does not exist, then
     * the Job bean will be NULL, and the ScriptManager class's proofRunner() method will
     * just leave the customer fields blank, ready for the user to fill them out.
     *
     * @throws InterruptedException Description for the exception goes here.
     */
    private void callScript(ScriptType scriptType) throws InterruptedException {
        if (loggingEnabled) log.entry("callScript");

        // Disable all controls.
        enableControls(false);

        // Set the proof number to 0, in case the Job isn't found in the database.
        // (It will be incremented to 1 when the script runner is called.)
        int proofNum = 0;
        // Define the Job object.
        Order orderBean = new Order();
        // Connect to the database and set the database type.
        ConnectionManager.getInstance().setDBType(DBType.MYSQL);

        // Try getting the data from the database.
        try {
            orderBean = OrderManager.getOrder(tfOrderNum.getText());
            // If the job bean is now no longer null, then we have at least one order detail item,
            // so find the one that has the highest proof number and save that number
            // for the main proofing window in the script.
            proofNum = getHighestProofNumber(orderBean);
        } catch (SQLException e) {
            if (loggingEnabled) log.error("Could not obtain data from database.", e);
        }
        if (loggingEnabled) log.debug("Proof Number from database (if non-zero) is: " + proofNum);

        // Whether or not the database search was successful,
        // try getting additional data from a text file.
        try {
            orderBean = readText(tfOrderNum.getText(), orderBean, scriptType);
        } catch (IOException e) {
            if (loggingEnabled) log.error("Text file not found");
            if (loggingEnabled) log.error(e);
            JOptionPane.showMessageDialog(null, "Text file not found. Please have it created and retry.", "Text File Not Found", JOptionPane.ERROR_MESSAGE);
            enableControls(true);
            return;
        }

        // Validate information retrieved from db and text file
        if (scriptType == ScriptType.PROOF) {
            // If this is a proofing job, then pre-add 1 to the proofing number.
            proofNum++;
            String messageFromAddress = prefs.get(Settings.PREFS_YOUR_EMAIL_KEY, Settings.PREFS_YOUR_EMAIL_DEFAULT);
            String messageToAddress = prefs.get(Settings.PREFS_NOTIFY_EMAIL_KEY, Settings.PREFS_NOTIFY_EMAIL_DEFAULT);
            if (proofNum == 1 && orderBean == null) {
                // If the text file doesn't exist during a first proof of a job.
                if (JOptionPane.showConfirmDialog(null, "Text file not found.\n"
                                + "Send an email to Customer Service to create it?", "Send Email?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    SendMail.send(messageFromAddress,
                            messageToAddress,
                            "Need text file for " + tfOrderNum.getText() + ".",
                            "I need a text file made for Job #" + tfOrderNum.getText() + ", please. Thank you!");
                    JOptionPane.showMessageDialog(null,
                            """
                                    An email has been sent to have the file created.
                                    Please keep a close eye on your inbox for a message
                                    stating that it has been created, then retry.""",
                            "Text File Not Found",
                            JOptionPane.WARNING_MESSAGE);
                }
                enableControls(true);
                return;
            } else {
                assert orderBean != null;
                if (orderBean.getPrintingCompany() == null) {
                    // If the Printing Company retrieved from the text file is null,
                    // Then it is most likely an Order Acknowledgement text file.
                    if (JOptionPane.showConfirmDialog(null, "The text file appears to be an Order Acknowledgement rather than a Sales Copy.\n"
                                    + "Send an email to Customer Service to fix it?", "Send Email?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        SendMail.send(messageFromAddress,
                                messageToAddress,
                                "Need correct text file for " + tfOrderNum.getText() + ".",
                                "The text file with the name " + tfOrderNum.getText() + ".TXT is an Order Acknowledgement. " +
                                        "Please overwrite it with a Sales Copy for that Job number. Thank you!");
                        JOptionPane.showMessageDialog(null, """
                                        An email has been sent to have the file corrected.
                                        Please keep a close eye on your inbox for a message
                                        stating that it has been created. Then retry.""",
                                "Incorrect Type of Text File!",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    enableControls(true);
                    return;
                } else if (!tfOrderNum.getText().equals(orderBean.getId())) {
                    // If the entered job number doesn't match the one retrieved from the text file.
                    if (JOptionPane.showConfirmDialog(null, "The job number you entered and the one read from the text file do not match.\n"
                                    + "Send an email to Customer Service to fix it?", "Send Email?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        SendMail.send(messageFromAddress,
                                messageToAddress,
                                "Need text file for " + tfOrderNum.getText() + ".",
                                "The text file with the name " + tfOrderNum.getText() + ".TXT is not for that job number. " +
                                        "Please overwrite it with one made for Job #" + tfOrderNum.getText() + ". Thank you!");
                        JOptionPane.showMessageDialog(null, """
                                        An email has been sent to have the file corrected.
                                        Please keep a close eye on your inbox for a message
                                        stating that it has been created. Then retry.""",
                                "Job Number Mismatch!",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    enableControls(true);
                    return;
                }
            }
        }

        // Finally, call the method that will run the script on this Job.
        boolean successfulRun = ScriptManager.scriptRunner(
                scriptType,
                tfOrderNum.getText(),
                prefs.get(Settings.PREFS_INITIALS_KEY, Settings.PREFS_INITIALS_DEFAULT),
                orderBean,
                proofNum,
                prefs.get(Settings.PREFS_DIR_KEY, Settings.PREFS_DIR_DEFAULT),
                customerServiceRep,
                creditCard,
                shipDays,
                wnaPo
        );

        // Close the connection to the database.
        ConnectionManager.getInstance().close();

        // Delete the text file, if it exists.
        if (successfulRun && textFile.exists()) {
            if (!textFile.delete()) {
                JOptionPane.showMessageDialog(null,
                        "The text file " + textFile.getName() + " could not be deleted.\n" +
                                "Please delete it manually.",
                        "Failed to delete text file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        // Re-enable all controls.
        enableControls(true);

        // Reset fields. (May make into a separate method later.)
        customerServiceRep = "";
        creditCard = false;
        shipDays = 0;
        wnaPo = "";

        if (successfulRun) {
            Thread dialogThread = new Thread(() -> {
                if (frm == null) {
                    frm = new JFrame();
                }
                frm.setVisible(true);
                frm.setAlwaysOnTop(true);
                frm.setLocationRelativeTo(null);
                JOptionPane.showMessageDialog(frm, "Script completed successfully!", "Script Complete", JOptionPane.INFORMATION_MESSAGE);
                frm.setVisible(false);
            });
            dialogThread.start();
        }
    }

    /**
     * When the database does not already have data for a job, then the needed customer data
     * is taken from a text file of the job order's cover page.
     *
     * @param jobNum The Job number.
     * @throws IOException Exception description goes here.
     * @return Job    A Job bean with the needed input fields filled out.
     */
    private Order readText(String jobNum, Order bean, ScriptType scriptType) throws IOException {
        if (loggingEnabled) log.entry("readText");

        // Create a new Job bean.
        if (bean == null) bean = new Order();
        // Define where the ScriptManager text files are located.
        String workOrderFolder = prefs.get(Settings.PREFS_WORK_ORDERS_KEY, Settings.PREFS_WORK_ORDERS_DEFAULT);
        textFile = new File(workOrderFolder + jobNum + ".txt");
        // If the text file is too small, then it is either empty or has some garbage in it.
        if (loggingEnabled) log.trace("The length of the associated text file is: " + textFile.length());
        if (textFile.length() < 2500) {
            if (bean.getId() != null)
                return bean;
            else
                return null;
        }

        // Declare variables to be filled by reading the text file
        // and (mostly) stored in the Job bean.
        String companyNameRaw = "";
        String companyPO = "";
        String shipDate = "";
        String printingCompany = "";
        String readJobNum = "";

        // Prepare search patterns
        final Pattern shipDaysPattern = Pattern.compile("SHIP\\s[^\\sWORKING]+\\sWORKING", Pattern.CASE_INSENSITIVE);
        final Pattern overrunsPattern = Pattern.compile("(?<!DON'T\\s)SEND\\sOVERRUNS", Pattern.CASE_INSENSITIVE);
        final Pattern noSamplesPattern = Pattern.compile("DON'T\\sPUT\\sSAMPLES", Pattern.CASE_INSENSITIVE);
        final Pattern creditCardPattern = Pattern.compile("CREDIT\\s?CARD", Pattern.CASE_INSENSITIVE);
        final Pattern rushPattern = Pattern.compile("QUICKSHIP", Pattern.CASE_INSENSITIVE);
        final String wnaOrderPoPhrase = "ON WNA ORDER ";

        // Matchers for the Patterns above. They must be initialized.
        Matcher creditCardMatcher = creditCardPattern.matcher("");
        Matcher overrunsMatcher = overrunsPattern.matcher("");
        Matcher noSamplesMatcher = noSamplesPattern.matcher("");
        Matcher rushMatcher = rushPattern.matcher("");

        // Prepare the text file for reading.
        String cache = null;
        Path workOrder = Paths.get(workOrderFolder, jobNum + ".TXT");
        if (loggingEnabled) log.debug("Path is: " + workOrder);
        BufferedReader bufferedReader = Files.newBufferedReader(workOrder, StandardCharsets.ISO_8859_1);
        int lineNumber = -1;

        while (bufferedReader.ready()) {
            String readLine = bufferedReader.readLine();
            lineNumber++;
            if (readLine != null) {
                readLine = readLine.replaceAll("\n", "");
                // Concatenate the last read line to the current read one.
                String concatLine = (cache != null ? cache : "") + readLine;

                // Now begin the checks for our various variables.
                // First, the line-based ones:
                switch (lineNumber) {
                    case (0): // The printing company (AA, ACS, AYS). (Line 0 in 0-based index)
                        printingCompany = StringUtils.trim(readLine);
                        break;
                    case (3):
                        readJobNum = StringUtils.trim(StringUtils.substring(readLine, 68));
                    case (4): // The customer service representative. (Line 4 in 0-based index)
                        customerServiceRep = StringUtils.trim(readLine);
                        break;
                    case (8): // The company's name. (Line 8 in 0-based index)
                        companyNameRaw = StringUtils.trim(StringUtils.substring(readLine, 1, 39));
                        break;
                    case (14): // The company's PO, Ship Date, and Credit Card info. (Line 14 in 0-based index)
                        companyPO = StringUtils.trim(StringUtils.substring(readLine, 25, 39));
                        shipDate = StringUtils.trim(StringUtils.substring(readLine, 50, 60));
                        creditCardMatcher = creditCardPattern.matcher(readLine);
                        break;
                    default:
                        break;
                }

                // Then check for the non-line-based items.

                // This one checks for the number of shipping days.
                // Check if the concatenated line matches our regex..
                Matcher shipDaysMatcher = shipDaysPattern.matcher(concatLine);
                if (shipDaysMatcher.find()) {
                    // Remove underscores (and possibly other characters).
                    if (loggingEnabled) log.debug("Matcher has found: " + shipDaysMatcher.group(0));
                    String shipDaysString = shipDaysMatcher.group(0).replaceAll("\\D+", "");
                    if (loggingEnabled) log.debug("shipDaysString (from Matcher) is now: " + shipDaysString);

                    // Then convert it to an integer.
                    if (shipDaysString.length() > 0) {
                        try {
                            shipDays = Integer.parseInt(shipDaysString);
                        } catch (NumberFormatException nfe) {
                            if (loggingEnabled) log.error(nfe);
                            // If the string *still* cannot be parsed to an integer, just do nothing.
                            // This will leave the shipDays variable at its default value of 0.
                        }
                    }
                }

                // Second one looks for Overruns.
                if (concatLine.contains("SEND OVERRUNS")) {
                    overrunsMatcher = overrunsPattern.matcher(concatLine);
                }

                // Third looks for the Sample Shelf note.
                if (concatLine.contains("SAMPLES")) {
                    noSamplesMatcher = noSamplesPattern.matcher(concatLine);
                }

                // Fourth looks for the PO # for WNA orders and saves it.
                if (concatLine.contains(wnaOrderPoPhrase)) {
                    int poIndex = concatLine.indexOf(wnaOrderPoPhrase) + wnaOrderPoPhrase.length();
                    wnaPo = StringUtils.trim(concatLine.substring(poIndex, poIndex + 15));
                }

                // Cached line is now the one we just read.
                cache = readLine;
            } else {
                break; // We've reached the end of the file.
            }
        }
        // Close the reader.
        bufferedReader.close();

        // Fix the company name's capitalization.
        final String companyName = fixCapitalization(companyNameRaw);

        // Assign the Credit Card variable the true value from the text file.
        creditCard = creditCardMatcher.find();

        // Also assign the other three booleans that will be placed into the Job bean.
        final boolean overruns = overrunsMatcher.find();
        final boolean noSamples = noSamplesMatcher.find();
        final boolean rush = rushMatcher.find();

        // Debugging lines to make sure the variables extracted are correct.
        if (loggingEnabled)
            log.debug("Printing Company: " + printingCompany + " with a length of " + printingCompany.length());
        if (loggingEnabled) log.debug("Company Name: " + companyName + " with a length of " + companyName.length());
        if (loggingEnabled) log.debug("Company PO: " + companyPO + " with a length of " + companyPO.length());
        if (loggingEnabled) log.debug("Shipping Date: " + shipDate + " with a length of " + shipDate.length());
        if (loggingEnabled)
            log.debug("CSR: " + customerServiceRep + " with a length of " + customerServiceRep.length());
        if (loggingEnabled) log.debug("Credit Card? " + creditCard);
        if (loggingEnabled) log.debug("Ship Days: " + shipDays);
        if (loggingEnabled) log.debug("WNA Order: " + wnaPo + " with a length of " + wnaPo.length());
        if (loggingEnabled) log.debug("Job Number read from file: " + readJobNum);
        if (loggingEnabled) log.debug("...compared to the entered job number: " + jobNum);

        // Now set the bean's properties.
        if (loggingEnabled) log.trace("Setting the Job bean's properties.");
        if (scriptType == ScriptType.PROOF) {
            switch (printingCompany) {
                case "AMERICAN ACCENTS" -> {
                    if (loggingEnabled) log.debug("This is an AA order.");
                    bean.setPrintingCompany(PrintingCompany.AMERICAN_ACCENTS);
                }
                case "AMERICAN CABIN SUPPLY" -> {
                    if (loggingEnabled) log.debug("This is an ACS order.");
                    bean.setPrintingCompany(PrintingCompany.AMERICAN_CABIN_SUPPLY);
                }
                case "AMERICAN YACHT SUPPLY" -> {
                    if (loggingEnabled) log.debug("This is an AYS order.");
                    bean.setPrintingCompany(PrintingCompany.AMERICAN_YACHT_SUPPLY);
                }
                case "SKY UNLIMITED, INC." -> {
                    if (loggingEnabled) log.debug("This is a SKY order. (internal)");
                    bean.setPrintingCompany(PrintingCompany.SKY_UNLIMITED_INC);
                }
                default -> {
                    if (loggingEnabled) log.debug("Incorrect printing company in text file. "
                            + "It is probably an Order Acknowledgement.");
                    bean.setPrintingCompany(null);
                }
            }
            bean.setId(readJobNum);
            bean.setCustomerName(companyName);
            bean.setCustomerPO(companyPO);

            try {
                //noinspection ResultOfMethodCallIgnored
                bean.getLineItemList();
            } catch (NullPointerException err) {
                if (loggingEnabled) log.error("No LineItem List. Creating one.", err);
                LineItem lineItemBean = new LineItem();
                List<LineItem> lineItemList = new ArrayList<>();
                lineItemList.add(lineItemBean);
                bean.setLineItemList(lineItemList);
            }
            bean.setSampleShelfNote(noSamples); // If the magic phrase is found in the text file, set the boolean in the job bean.
        }
        bean.setOverruns(overruns); // If the magic phrase is found in the text file, set the boolean in the job bean.
        bean.setRush(rush);

        try {
            if (shipDate.length() > 0)
                bean.setShipDateId(DateManager.usDateStringToSqlDate(shipDate));
        } catch (IllegalArgumentException e) {
            if (loggingEnabled) log.error("Date not in the correct format.", e);
        }

        if (loggingEnabled) log.debug("Bean set (inside readText).");
        // Now delete the text file, since it is no longer needed.

        return bean;
    }

    private String fixCapitalization(String companyNameRaw) {

        String companyName = WordUtils.capitalizeFully(companyNameRaw);

        Pattern upperPattern = Pattern.compile("(?i)(^(?!The\\b)(?!An\\b)(?![IO][nf]\\b)(?!Sir\\b)(?!Bob\\b)[\\w\\-]{2,3}(?=[\\s\\-+&])|(?<=\\s)\\w{2}(?=$)|\\b[b-df-hj-np-tv-z]{3}\\b|(?<!\\w)\\w{2,3}(?=\\))|(?<=[(\\-+&])\\w)");
        Pattern lowerPattern = Pattern.compile("(?i)(?!^)(?<!-)\\b(of|and|an?)\\b");

        // Text file that has the two RegEx patterns for fixing capitalizations.
        File patternFile = new File(prefs.get(Settings.PREFS_PATTERNS_KEY, Settings.PREFS_PATTERNS_DEFAULT));
        if (patternFile.exists()) {
            if (loggingEnabled) log.trace("Pattern file exists! Reading from it now.");
            try {
                List<String> patternText = FileUtils.readLines(patternFile);
                String ucText = patternText.get(0).trim();
                String lcText = patternText.get(1).trim();
                if (loggingEnabled) log.trace("Upper Pattern is: ");
                if (loggingEnabled) log.trace("    " + ucText);
                upperPattern = Pattern.compile(ucText);
                lowerPattern = Pattern.compile(lcText);
            } catch (IOException e) {
                if (loggingEnabled) log.error("Error when attempting to read from the RegEx pattern file", e);
            }
        }

        Matcher ucMatcher = upperPattern.matcher("abc");
        Matcher lcMatcher = lowerPattern.matcher("abc");

        StringBuilder sb = new StringBuilder();
        ucMatcher.reset(companyName);
        while (ucMatcher.find()) {
            ucMatcher.appendReplacement(sb, ucMatcher.group().toUpperCase());
        }
        ucMatcher.appendTail(sb);

        if (loggingEnabled) log.debug("Between mods, sb is currently " + sb);

        lcMatcher.reset(sb.toString());
        sb.setLength(0);
        while (lcMatcher.find()) {
            lcMatcher.appendReplacement(sb, lcMatcher.group().toLowerCase());
        }
        lcMatcher.appendTail(sb);

        if (loggingEnabled) log.debug("After second mod, sb is: " + sb);

        return sb.toString();
    }

    /**
     * If a job order IS found in the database, then get the highest proof number
     * of all of its Order Detail items.
     *
     * @param bean The Job bean created from the database, so it has at least one OrderDetail item.
     * @return An integer of the highest proof number in the Job.
     */
    private int getHighestProofNumber(Order bean) {
        if (loggingEnabled) log.entry("getHighestProofNumber");

        List<Integer> proofNumbers = new ArrayList<>();
        try {
            for (LineItem li : bean.getLineItemList()) {
                proofNumbers.add(li.getProofNum());
            }
        } catch (NullPointerException err) {
            if (loggingEnabled) log.error("Nothing in the OrderDetail List! Returning 0.", err);
            return 0;
        }

        return Collections.max(proofNumbers);
    }

    private boolean validateAll() {
        boolean orderNumOkay = validateOrderNum();
        buttonProof.setEnabled(orderNumOkay);
        buttonOutput.setEnabled(orderNumOkay);
        return orderNumOkay;
    }

    private boolean validateOrderNum() {
        boolean isOrderNum = Sanitizer.checkOrderNum(tfOrderNum.getText());
        Settings.validateField(tfOrderNum, isOrderNum);
        return isOrderNum;
    }

    private void enableControls(final boolean b) {
        SwingUtilities.invokeLater(() -> {
            if (loggingEnabled) log.entry("setControlsEnabled");

            tfOrderNum.setEnabled(b);
            buttonProof.setEnabled(b);
            buttonOutput.setEnabled(b);
            // TODO: Do we need to disable/enable the Cancel button?
            buttonCancel.setEnabled(b);

            if (b) {
                // Blank out the order number and put the focus on it
                tfOrderNum.setText("");
                tfOrderNum.requestFocusInWindow();
            }

        });
    }

    public static void installTheme(boolean darkTheme) {
        if (darkTheme) {
            FlatCarbonIJTheme.install();
        } else {
            FlatCyanLightIJTheme.install();
        }
        FlatLaf.updateUI();
    }
}
