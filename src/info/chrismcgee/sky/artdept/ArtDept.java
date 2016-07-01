package info.chrismcgee.sky.artdept;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.chrismcgee.components.DateManager;
import info.chrismcgee.components.Sanitizer;
import info.chrismcgee.sky.beans.Job;
import info.chrismcgee.sky.beans.OrderDetail;
import info.chrismcgee.sky.enums.DBType;
import info.chrismcgee.sky.enums.PrintingCompany;
import info.chrismcgee.sky.enums.ScriptType;
import info.chrismcgee.sky.tables.JobManager;
import info.chrismcgee.util.ConnectionManager;
import info.chrismcgee.util.SendMail;
import net.miginfocom.swing.MigLayout;

public class ArtDept extends JDialog {

	/**
	 * Serialize, to keep Eclipse from throwing a warning message.
	 */
	private static final long serialVersionUID = -185001290066987954L;
	static final Logger log = LogManager.getLogger(ArtDept.class.getName()); // For logging.
	// Preferences variables
	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	private static final String PREFS_EMAIL = "email";
	private static final String PREFS_INITIALS = "initials";
	// The location of the window (It's not resizable)
	private Point windowLocation;
	private final JPanel contentPanel = new JPanel(); // default.
	// These fields allow their respective components to be accessed anywhere.
	private JTextField tfOrderNum;
	private JTextField tfInitials;
	private JTextField tfEmail;
	private JButton btnProof;
	private JButton btnOutput;
	private JButton cancelButton;
	// Simple booleans to state whether or not each text field has passed sanitization.
	private boolean jobNumberReady = false;
	private boolean initialsReady = false;
	private boolean usernameReady = false;
	private File textFile = null;
	// Define keystrokes that will be used as shortcuts for the "Proof" and "Output" buttons.
	private KeyStroke ksMenuP = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	private KeyStroke ksMenuO = KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	private KeyStroke ksF1 = KeyStroke.getKeyStroke("F1");
	private KeyStroke ksF2 = KeyStroke.getKeyStroke("F2");
	private Pattern upperPattern;
	private Pattern lowerPattern;
	private Matcher ucMatcher;
	private Matcher lcMatcher;
	private File patternFile = null; // Text file that has the two RegEx patterns for fixing capitalizations.
	private String scriptPath = "/Volumes/ArtDept/ArtDept/Scripts/sky-artdept/Test/"; // Location of the script files to be run. This is either with or without the "Test/" portion.
	private String customerServiceRep = "";
	private boolean creditCard = false;
	private int shipDays = 0;
	private String wnaPo = "";
	private String messageToAddr = "customerservice@skyunlimitedinc.com";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				log.entry("main");
				try { // Create the dialog window and display it.
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					ArtDept dialog = new ArtDept();
					
					dialog.setTitle("Art Department v2.11 (Test)");
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setLocationByPlatform(true);
					
					// Set the location and size of the window
					dialog.windowLocation = new Point(dialog.prefs.getInt("x", 100), dialog.prefs.getInt("y", 100));
					dialog.setLocation(dialog.windowLocation);

					dialog.setVisible(true);
				} catch (Exception e) {
					log.error("Error in Main", e);
				}
				log.exit("main");
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public ArtDept() {
		setResizable(false);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent evt) {
				prefs.putInt("x", getX());
				prefs.putInt("y", getY());
			}
			@Override
			public void componentResized(ComponentEvent evt) {
				prefs.putInt("width", getWidth());
				prefs.putInt("height", getHeight());
			}
		});

		// Define two actions, one for each button.
		final Action doProof = new AbstractAction() {
			/**
			 * Serialize, to keep Eclipse from throwing a warning message.
			 */
			private static final long serialVersionUID = -3718879213082545441L;

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 * 
			 * This action should first check to see if the given order number exists in the database.
			 * If so (usually for proof numbers greater than 1), it will extract its information
			 * and pass it to the Proofing script. If not, it will try to obtain the Customer information
			 * from the text file generated by Quickey. If this cannot be found, either,
			 * then the Customer fields will remain blank in the ScriptManager script, ready for the user to fill them in.
			 */
			@Override
			public void actionPerformed(ActionEvent evt) {
				log.entry("doProof Action");
				if (jobNumberReady && initialsReady)
				{
					log.trace("Proofing button hit!");
					Thread proofThread = new Thread()
					{
						public void run()
						{
							try {
								callScript(ScriptType.PROOF);
							} catch (InterruptedException e) {
								log.error("Interrupted!", e);
							}
						}
					};
					proofThread.start();
				}
				log.exit("doProof Action");
			}
		};
		final Action doOutput = new AbstractAction() {
			/**
			 * Serialize, to keep Eclipse from throwing a warning message.
			 */
			private static final long serialVersionUID = -1892240535496986876L;

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 * 
			 * Similar to the ScriptManager action above. Tries to get info from the database first,
			 * then the text file, then just presenting blank fields if all else fails.
			 */
			@Override
			public void actionPerformed(ActionEvent evt) {
				log.entry("doOutput Action");
				if (jobNumberReady && initialsReady)
				{
					log.trace("Output button hit!");
					Thread outputThread = new Thread()
					{
						public void run()
						{
							try {
								callScript(ScriptType.OUTPUT);
							} catch (InterruptedException e) {
								log.error("Interrupted!", e);
							}
						}
					};
					outputThread.start();
				}
				log.exit("doOutput Action");
			}
		};
		// Bind the KeyStrokes to the action we want them to perform.
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ksMenuP, "doProof");
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ksF1, "doProof");
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ksMenuO, "doOutput");
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ksF2, "doOutput");
		getRootPane().getActionMap().put("doProof", doProof);
		getRootPane().getActionMap().put("doOutput", doOutput);
		
		// Default items created by WindowBuilder.
		setBounds(100, 100, 500, 175);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow]", "[][][]"));
		contentPanel.setBackground(Color.DARK_GRAY);

		{ // Simple label describing that the following TextField is for the user's email address.
			JLabel lblEmail = new JLabel("Email:");
			lblEmail.setHorizontalAlignment(SwingConstants.TRAILING);
			lblEmail.setForeground(Color.WHITE);
			contentPanel.add(lblEmail, "cell 0 0,alignx trailing");
		}
		{ // Text field for the user to enter his/her email address.
			tfEmail = new JTextField(prefs.get(PREFS_EMAIL, ""));
			tfEmail.setColumns(25);
			tfEmail.getDocument().addDocumentListener(new DocumentListener() {
				// When the user types or deletes characters, sanitize the input.
				@Override
				public void removeUpdate(DocumentEvent e) {
					log.trace("Deleting a character");
					sanitizeUsername();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					log.trace("Inserting a character");
					sanitizeUsername();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					// Plain text components do not fire these events.
				}
			});
			// Add the Initials TextField to the content panel.
			contentPanel.add(tfEmail, "cell 1 0,growx");
		}
		{ // Simple label describing that the following TextField is for the user's initials.
			JLabel lblInitials = new JLabel("Initials:");
			lblInitials.setHorizontalAlignment(SwingConstants.TRAILING);
			lblInitials.setForeground(Color.WHITE);
			contentPanel.add(lblInitials, "cell 0 1,alignx trailing");
		}
		{ // Text field for the user to enter his/her initials.
			tfInitials = new JTextField(prefs.get(PREFS_INITIALS, ""));
			tfInitials.setColumns(3);
			tfInitials.getDocument().addDocumentListener(new DocumentListener() {
				// When the user types or deletes characters, sanitize the input.
				@Override
				public void removeUpdate(DocumentEvent e) {
					log.trace("Deleting a character");
					sanitizeInitials();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					log.trace("Inserting a character");
					sanitizeInitials();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					// Plain text components do not fire these events.
				}
			});
			// Add the Initials TextField to the content panel.
			contentPanel.add(tfInitials, "cell 1 1");
		}
		{ // Simple label describing that the following TextField is for the Job/Order number.
			JLabel lblOrderNumber = new JLabel("Order Number:");
			lblOrderNumber.setHorizontalAlignment(SwingConstants.TRAILING);
			lblOrderNumber.setForeground(Color.WHITE);
			contentPanel.add(lblOrderNumber, "cell 0 2,alignx trailing");
		}
		{ // Text field for the user to enter the job/order number.
			tfOrderNum = new JTextField();
			tfOrderNum.setColumns(10);
			tfOrderNum.getDocument().addDocumentListener(new DocumentListener() {
				// When the user types or deletes characters, sanitize the input.
				@Override
				public void removeUpdate(DocumentEvent e) {
					log.trace("Deleting a character");
					santizeOrderNum();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					log.trace("Inserting a character");
					santizeOrderNum();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					// Plain text components do not fire these events.
				}
			});
			// Add the Order Number TextField to the content panel.
			contentPanel.add(tfOrderNum, "cell 1 2");
		}

		{ // Create the button panel at the bottom of the frame. 
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER)); // Centers the buttons.
			getContentPane().add(buttonPane, BorderLayout.SOUTH); // Default. Aligns the panel to the bottom.
			buttonPane.setBackground(Color.DARK_GRAY);
			
			{ // Create the "ScriptManager" button plus its listener.
				btnProof = new JButton("Proof");
				btnProof.setEnabled(false); // Spawns disabled until input is sanitized.
				btnProof.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						log.trace("Pressed the ScriptManager button");
						doProof.actionPerformed(e); // When pressed, call the "doProof" action.
					}
				});
				// This item has been commented out because I am unsure as to what it does, specifically.
				// I believe it maps the <ENTER> or <RETURN> key to the button.
				// If so, I do not want any action to be performed when those buttons are pressed,
				// so I've commented it out in case that's the case.
//				btnProof.setActionCommand("OK");
				// Add the "ScriptManager" button the button pane.
				buttonPane.add(btnProof);
			}
			{	// Create the "Output" button plus its listener.
				btnOutput = new JButton("Output");
				btnOutput.setEnabled(false); // Spawns disabled until input is sanitized.
				btnOutput.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						log.trace("Pressed the Output button");
						doOutput.actionPerformed(e); // When pressed, call the "doOutput" action.
					}
				});
//				btnOutput.setActionCommand("OK"); // default. But I don't want to set an action command right now. (see above)
				// Add the "Output" button to the button pane.
				buttonPane.add(btnOutput);
//				getRootPane().setDefaultButton(btnOutput); // default.
			}
			{	// Adding an invisible strut to separate the Cancel button from the other two buttons. 
				Component horizontalStrut = Box.createHorizontalStrut(180);
				// Add the strut to the button pane.
				buttonPane.add(horizontalStrut);
			}
			{	// Create the "Cancel" button plus its listener.
				cancelButton = new JButton("Close");
				cancelButton.setActionCommand("Cancel"); // default.
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Close this ArtDept window when the button is pressed.
						log.trace("Cancel button pressed");
						Window window = SwingUtilities.windowForComponent((Component) e.getSource());
						window.dispose();
					}
				});
				// Add the "Cancel" button to the button pane.
				buttonPane.add(cancelButton);
			}
		}
		sanitizeInitials();
		sanitizeUsername();
		
		// Set the focus on the appropriate text field.
		addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		        tfOrderNum.requestFocusInWindow();
		        if (tfInitials.getText().length() == 0) {
		        	tfInitials.requestFocusInWindow();
		        }
		        if (tfEmail.getText().length() == 0) {
		        	tfEmail.requestFocusInWindow();
		        }
		    }
		});
	}
	
	/**
	 * Checks to see if the Job Number text field has been properly filled out (via the boolean field)
	 * and enables the "ScriptManager" and "Output" buttons if so. Otherwise, it disables them.
	 * 
	 * This method should be called every time a change is made to the Job Number TextField.
	 * 
	 * In addition, this method is separate from the sanitizeOrderNum() method below in case
	 * other field need to be checked first, as well, before the buttons can be set as
	 * enabled or not.
	 */
	private void verifyInputs() {
		log.entry("verifyInputs");
		btnProof.setEnabled(jobNumberReady && initialsReady && usernameReady);
		btnOutput.setEnabled(jobNumberReady && initialsReady && usernameReady);
		log.exit("verifyInputs");
	}

	/**
	 * Checks to see if the Job Number TextField has been properly filled out by calling the checkOrderNum()
	 * method in the Santizer class. It then changes the TextField's background accordingly (red for incorrect,
	 * white for correct) and sets the jobNumberReady boolean. It finally calls the verifyInputs() method
	 * to enable / disable the buttons as needed.
	 * 
	 * This method should be called every time a change is made to the Job Number TextField.
	 */
	private void santizeOrderNum()
	{
		log.entry("sanitizeOrderNum");
		if (Sanitizer.checkOrderNum(tfOrderNum.getText()))
		{
			tfOrderNum.setBackground(Color.GREEN);
			jobNumberReady = true;
		}
		else
		{
			tfOrderNum.setBackground(Color.WHITE);
			jobNumberReady = false;
		}
		verifyInputs();
		log.exit("sanitizeOrderNum");
	}
	
	/**
	 * Equivalent to the method above (sanitizeOrderNum()), this method does the exact same things,
	 * only in reference to the Initials TextField.
	 */
	private void sanitizeInitials() {
		if (Sanitizer.checkInitials(tfInitials.getText())) {
			tfInitials.setBackground(Color.GREEN);
			initialsReady = true;
			prefs.put(PREFS_INITIALS, tfInitials.getText());
		} else {
			tfInitials.setBackground(Color.WHITE);
			initialsReady = false;
		}
		verifyInputs();
		log.exit("sanitizeInitials");
	}
	
	/**
	 * Equivalent to the method above (sanitizeOrderNum()), this method does the exact same things,
	 * only in reference to the Username TextField.
	 */
	private void sanitizeUsername() {
		if (Sanitizer.checkInitials(tfEmail.getText())) {
			tfEmail.setBackground(Color.GREEN);
			usernameReady = true;
			prefs.put(PREFS_EMAIL, tfEmail.getText());
		} else {
			tfEmail.setBackground(Color.WHITE);
			usernameReady = false;
		}
		verifyInputs();
		log.exit("sanitizeUsername");
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
	 * @throws InterruptedException 
	 */
	private void callScript (ScriptType scriptType) throws InterruptedException
	{
		log.entry("callScript");

		// Disable all controls.
		enableControls(false);

		// Set the proof number to 0, in case the Job isn't found in the database.
		// (It will be incremented to 1 when the script runner is called.)
		int proofNum = 0;
		// Define the Job bean. If the order is not found in the database
		// nor as a text file, then it will remain NULL.
//		Job jobBean = null;
		// Changed the above line to create a Job object. Thus, it will
		// never be NULL.
		Job jobBean = new Job();
		// Connect to the database and set the database type.
		ConnectionManager.getInstance().setDBType(DBType.MSSQL);
		
		// Try getting the data from the database.
		try {
			jobBean = JobManager.getJob(tfOrderNum.getText());
			// If the job bean is now no longer null, then we have at least one order detail item,
			// so find the one that has the highest proof number and save that number
			// for the main proofing window in the script.
			/*if (jobBean != null) */proofNum = getHighestProofNumber(jobBean);
		} catch (SQLException e) {
			log.error("Could not obtain data from database.", e);
		}
//		log.debug("After database query, Job bean is null? " + (jobBean == null));
		log.debug("Proof Number from database (if non-zero) is: " + proofNum);
		
		// Whether or not the database search was successful,
		// try getting additional data from a text file.
		try {
			jobBean = readText(tfOrderNum.getText(), jobBean, scriptType);
		} catch (IOException e) {
			log.error("Text file not found");
			log.error(e);
			JOptionPane.showMessageDialog(null, "Text file not found. Please have it created and retry.", "Text File Not Found", JOptionPane.ERROR_MESSAGE);
			log.exit("Text file not found, returning prematurely.");
			enableControls(true);
			return;
		} finally {
		}
//		log.debug("After text file grab, Job bean is null? " + (jobBean == null));
		
		// If this is a proofing job, then pre-add 1 to the proofing number.
		// Also do some more checks.
		if (scriptType == ScriptType.PROOF) {
			proofNum++;
			if (proofNum == 1 && jobBean == null) {
				// If the text file doesn't exist during a first proof of a job.
				if (JOptionPane.showConfirmDialog(null, "Text file not found.\n"
						+ "Send an email to Customer Service to create it?", "Send Email?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					SendMail.send(prefs.get(PREFS_EMAIL, "skyartdept@mainserver.com"),
							messageToAddr,
							"Need text file for " + tfOrderNum.getText() + ".",
							"I need a text file made for Job #" + tfOrderNum.getText() + ", please.");
					JOptionPane.showMessageDialog(null,
							"An email has been sent to have the file created.\n" +
									"Please keep a close eye on your inbox for a message\n" +
									"stating that it has been created. Then retry.",
							"Text File Not Found",
							JOptionPane.WARNING_MESSAGE);
				}
				log.exit("Text file not found for this Proof #1; returning prematurely.");
				enableControls(true);
				return;
			} else if (!tfOrderNum.getText().equals(jobBean.getJobId())) {
				// If the entered job number doesn't match the one retrieved from the text file.
				if (JOptionPane.showConfirmDialog(null, "The job number you entered and the one read from the text file do not match.\n"
						+ "Send an email to Customer Service to fix it?", "Send Email?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					SendMail.send(prefs.get(PREFS_EMAIL, "skyartdept@mainserver.com"),
							messageToAddr,
							"Need text file for " + tfOrderNum.getText() + ".",
							"The text file with the name " + tfOrderNum.getText() + ".TXT is not for that job number. " +
									"Please overwrite it with one made for Job #" + tfOrderNum.getText() + ", please.");
					JOptionPane.showMessageDialog(null, "An email has been sent to have the file corrected.\n" +
									"Please keep a close eye on your inbox for a message\n" +
									"stating that it has been created. Then retry.",
							"Job Number Mismatch!",
							JOptionPane.WARNING_MESSAGE);
				}
				enableControls(true);
				return;
			}
		}
		
		// Finally, call the method that will run the script on this Job.
		boolean successfulRun = ScriptManager.scriptRunner(scriptType, tfOrderNum.getText(), tfInitials.getText(), jobBean, proofNum, scriptPath, customerServiceRep, creditCard, shipDays, wnaPo);
		
		// Close the connection to the database.
		ConnectionManager.getInstance().close();
		
		// Delete the text file, if it exists.
		if (successfulRun && textFile.exists()) textFile.delete();
		
		// Re-enable all controls.
		enableControls(true);
		
		// Reset fields. (May make into a separate method later.)
		customerServiceRep = "";
		creditCard = false;
		shipDays = 0;
		wnaPo = "";

		log.exit("callScript");
	}
	
	/**
	 * If a job order IS found in the database, then get the highest proof number
	 * of all of its Order Detail items.
	 * 
	 * @param bean	The Job bean created from the database, so it has at least one OrderDetail item.
	 * @return	An integer of the highest proof number in the Job.
	 */
	private int getHighestProofNumber(Job bean)
	{
		log.entry("getHighestProofNumber");
		
		List<Integer> proofNums = new ArrayList<Integer>();
		try {
			for (OrderDetail od : bean.getOrderDetailList()) {
				proofNums.add(od.getProofNum());
			}
		} catch (NullPointerException err) {
			log.error("Nothing in the OrderDetail List! Returning 0.", err);
			return log.exit(0);
		}

		return log.exit(Collections.max(proofNums));
	}
	
	/**
	 * When the database does not already have data for a job, then the needed customer data
	 * is taken from a text file of the job order's cover page.
	 * 
	 * @param jobNum	The Job number.
	 * @return	Job	A Job bean with the needed input fields filled out.
	 * @throws IOException
	 */
	private Job readText (String jobNum, Job bean, ScriptType scriptType) throws IOException {
		log.entry("readText");
		
		// Create a new Job bean.
		if (bean == null) bean = new Job();
		// Define where the ScriptManager text files are located.
		String workOrderFolder = "/Volumes/ArtDept/Work Orders/";
		textFile = new File(workOrderFolder + jobNum + ".txt");
		// If the text file is too small, then it is either empty or has some garbage in it.
		log.trace("The length of the associated text file is: " + textFile.length());
		if (textFile.length() < 2500)
		{
			if (bean.getJobId() != null)
				return log.exit(bean);
			else
				return log.exit(null);
		}
		
		// Read the text file's contents in two ways.
/*		List<String> workOrderLines = FileUtils.readLines(textFile);
		String workOrderText = FileUtils.readFileToString(textFile);*/

		// Declare variables to be filled by reading the text file
		// and (mostly) stored in the Job bean.
		String companyNameRaw = "";
		String companyPO = "";
		String shipDate = "";
		String printingCompany = "";
		String shipDaysString = "";
		String readJobNum = "";
		
		// Prepare search patterns
		final Pattern shipDaysPattern = Pattern.compile("SHIP\\s[^\\sWORKING]+\\sWORKING", Pattern.CASE_INSENSITIVE);
		final Pattern overrunsPattern = Pattern.compile("(?<!DON'T\\s)SEND\\sOVERRUNS", Pattern.CASE_INSENSITIVE);
		final Pattern noSamplesPattern = Pattern.compile("DON'T\\sPUT\\sSAMPLES", Pattern.CASE_INSENSITIVE);
		final Pattern creditCardPattern = Pattern.compile("CREDIT\\s?CARD", Pattern.CASE_INSENSITIVE);
		// Removed these two variables since we will no longer be using the "String" method to find the key phrase.
//		final String shipDaysPhrasePre = "WILL SHIP ";
//		final String shipDaysPhraseSuf = " WORKING DAYS";
		final String wnaOrderPoPhrase = "ON WNA ORDER ";
		
		// Matchers for the Patterns above. They must be initialized.
		Matcher shipDaysMatcher = shipDaysPattern.matcher("");
		Matcher creditCardMatcher = creditCardPattern.matcher("");
		Matcher overrunsMatcher = overrunsPattern.matcher("");
		Matcher noSamplesMatcher = noSamplesPattern.matcher("");

		// Prepare the text file for reading.
		String cache = null;
		Path workOrder = Paths.get(workOrderFolder, jobNum + ".TXT");
		log.debug("Path is: " + workOrder.toString());
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
				shipDaysMatcher = shipDaysPattern.matcher(concatLine);
				if (shipDaysMatcher.find()) {
//				if (concatLine.contains(shipDaysPhrasePre) && concatLine.contains(shipDaysPhraseSuf)) { // Removing this "String" method.
//					shipDaysString = concatLine.substring(concatLine.indexOf(shipDaysPhrasePre) + shipDaysPhrasePre.length(), concatLine.indexOf(shipDaysPhraseSuf)); // Ditto.
					// No need to remove linebreaks, because we're reading line by line with BufferedReader.
					
					// Newer, more succinct code for removing underscores (and possibly other characters).
					//   It might be less efficient, but easier to understand and type.
					log.debug("Matcher has found: " + shipDaysMatcher.group(0));
//					shipDaysString = shipDaysString.replaceAll("\\D+", ""); // Removing the "String" method.
					shipDaysString = shipDaysMatcher.group(0).replaceAll("\\D+", "");
					log.debug("shipDaysString (from Matcher) is now: " + shipDaysString);
					
					// Remove underscores ("_") in front of and after the digit(s).
					// The following block has been commented out because the numbers we need
					//   will not always be surrounded by *only* underscores. (There might be other characters.)
/*					if (shipDaysString.startsWith("_") && shipDaysString.endsWith("_")) {
						// Before (example): shipDaysString = "_5_"
						shipDaysString = shipDaysString.substring(1, shipDaysString.length()-1);
						// After (example): shipDaysString = "5"
					}*/
					// Then convert it to an integer.
					if (shipDaysString.length() > 0) {
						try {
							shipDays = Integer.parseInt(shipDaysString);
						} catch (NumberFormatException nfe) {
							log.error(nfe);
							// If the string *still* cannot be parsed to an integer, just do nothing.
							// This will leave the shipDays variable at its default value of 0.
						}
					}
				}
				
				// Second one looks for Overruns.
				if (concatLine.contains("OVERRUNS")) {
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
		
		// Begin setting the variables that will be placed into the Job bean.
/*		String printingCompany;
		try {
			printingCompany = StringUtils.trim(workOrderLines.get(0));
		} catch (Exception e) {
			log.error("It seems the Work Order is blank.", e);
			if (bean.getJobId() != null)
				return log.exit(bean);
			else
				return log.exit(null);
		}*/
//		companyNameRaw = WordUtils.capitalizeFully(StringUtils.trim(StringUtils.substring(workOrderLines.get(8), 1, 39)));
//		companyPO = StringUtils.trim(StringUtils.substring(workOrderLines.get(14), 25, 39));
//		shipDate = StringUtils.trim(StringUtils.substring(workOrderLines.get(14), 50, 60));
//		customerServiceRep = StringUtils.trim(workOrderLines.get(4));
//		Matcher overrunsMatcher = overrunsPattern.matcher(workOrderText);
//		Matcher noSamplesMatcher = noSamplesPattern.matcher(workOrderText);
//		Matcher creditCardMatcher = creditCardPattern.matcher(workOrderLines.get(14));
//		String shipDaysString = workOrderText.substring(workOrderText.indexOf(shipDaysPhrasePre) + shipDaysPhrasePre.length(), workOrderText.indexOf(shipDaysPhraseSuf));
//		if (shipDaysString.startsWith("_") && shipDaysString.endsWith("_")) {
//			shipDaysString = shipDaysString.substring(1, shipDaysString.length()-1);
//		}
//		shipDays = Integer.parseInt(shipDaysString);

		// Fix the company name's capitalization.
		final String companyName = fixCapitalization(companyNameRaw);
		
		// Assign the Credit Card variable the true value from the text file.
		creditCard = creditCardMatcher.find();
		
		// Also assign the other two booleans that will be placed into the Job bean.
		final boolean overruns = overrunsMatcher.find();
		final boolean noSamples = noSamplesMatcher.find();

		// Debugging lines to make sure the variables extracted are correct.
		log.debug("Printing Company: " + printingCompany + " with a length of " + printingCompany.length());
		log.debug("Company Name: " + companyName + " with a length of " + companyName.length());
		log.debug("Company PO: " + companyPO + " with a length of " + companyPO.length());
		log.debug("Shipping Date: " + shipDate + " with a length of " + shipDate.length());
		log.debug("CSR: " + customerServiceRep + " with a length of " + customerServiceRep.length());
		log.debug("Credit Card? " + creditCard);
		log.debug("Ship Days: " + shipDays);
		log.debug("WNA Order: " + wnaPo + " with a length of " + wnaPo.length());
		log.debug("Job Number read from file: " + readJobNum);
		log.debug("...compared to the entered job number: " + jobNum);
		
		// If the job numbers do not match, then alert the user and quit out of the program.
/*		if (!jobNum.equals(readJobNum)) {
			JOptionPane.showMessageDialog(null,
					"The job number you entered and the one read from the text file do not match. Please correct and try again.",
					"Job Number Mismatch!",
					JOptionPane.ERROR_MESSAGE);
			return log.exit(null);
		}*/
		// Commented-out because I currently cannot see a way to have it NOT run the script, even though the bean will be null.
		
		// Now set the bean's properties.
		log.trace("Setting the Job bean's properties.");
		if (scriptType == ScriptType.PROOF)
		{
			switch (printingCompany) {
				case "AMERICAN CABIN SUPPLY":
					log.debug("This is an ACS order.");
					bean.setPrintingCompany(PrintingCompany.AMERICAN_CABIN_SUPPLY);
					break;
				case "AMERICAN YACHT SUPPLY":
					log.debug("This is an AYS order.");
					bean.setPrintingCompany(PrintingCompany.AMERICAN_YACHT_SUPPLY);
					break;
				default:
					log.debug("This is an AA order.");
					bean.setPrintingCompany(PrintingCompany.AMERICAN_ACCENTS);
					break;
			}
			bean.setJobId(readJobNum);
			bean.setCustomerName(companyName);
			bean.setCustomerPO(companyPO);
			
			try {
				bean.getOrderDetailList();
			} catch (NullPointerException err) {
				log.error("No OrderDetail List. Creating one.", err);
				OrderDetail odBean = new OrderDetail();
				List<OrderDetail> odList = new ArrayList<OrderDetail>();
				odList.add(odBean);
				bean.setOrderDetailList(odList);
			}
/*			if (bean.getOrderDetailList().isEmpty()) {
				OrderDetail odBean = new OrderDetail();
				List<OrderDetail> odList = new ArrayList<OrderDetail>();
//				if (noSamplesMatcher.find()) {
//					odBean.setPackingInstructions("Don't put samples on sample shelf.");
					odList.add(odBean);
//				}
				bean.setOrderDetailList(odList);
			}*/
			bean.setSampleShelfNote(noSamples); // If the magic phrase is found in the text file, set the boolean in the job bean.
		}
		bean.setOverruns(overruns); // If the magic phrase is found in the text file, set the boolean in the job bean.
		
		try {
			if (shipDate.length() > 0)
				bean.setShipDate(DateManager.usDateStringToSqlDate(shipDate));
		} catch (IllegalArgumentException e) {
			log.error("Date not in the correct format.", e);
			return log.exit(null);
		}

		log.debug("Bean set (inside readText).");
		// Now delete the text file, since it is no longer needed.
//		textFile.delete();
		
		return log.exit(bean);
	}
	
	private String fixCapitalization(String companyNameRaw) {
		
		String companyName = WordUtils.capitalizeFully(companyNameRaw);
		
		upperPattern = Pattern.compile("(?i)(^(?!The\\b)(?!An\\b)(?![IO][nf]\\b)(?!Sir\\b)(?!Bob\\b)[\\w\\-]{2,3}(?=[\\s\\-\\+\\&])|(?<=\\s)\\w{2}(?=$)|\\b[b-df-hj-np-tv-z]{3}\\b|(?<!\\w)\\w{2,3}(?=\\))|(?<=[\\(\\-\\+\\&])\\w)");
		lowerPattern = Pattern.compile("(?i)(?!^)(?<!\\-)\\b(of|and|an?)\\b");

		patternFile = new File(scriptPath + "patterns.txt");
		if (patternFile.exists()) {
			log.trace("Pattern file exists! Reading from it now.");
			try {
				List<String> patternText = FileUtils.readLines(patternFile);
				String ucText = patternText.get(0).trim();
				String lcText = patternText.get(1).trim();
				log.trace("Upper Pattern is: ");
				log.trace("    " + ucText);
				upperPattern = Pattern.compile(ucText);
				lowerPattern = Pattern.compile(lcText);
			} catch (IOException e) {
				log.error("Error when attempting to read from the RegEx pattern file", e);
			}
		}
		
		ucMatcher = upperPattern.matcher("abc");
		lcMatcher = lowerPattern.matcher("abc");

		StringBuffer sb = new StringBuffer();
		ucMatcher.reset(companyName);
		while (ucMatcher.find()) {
			ucMatcher.appendReplacement(sb, ucMatcher.group().toUpperCase());
		}
		ucMatcher.appendTail(sb);
		
		log.debug("Between mods, sb is currently " + sb.toString());
		
		lcMatcher.reset(sb.toString());
		sb.setLength(0);
		while (lcMatcher.find()) {
			lcMatcher.appendReplacement(sb, lcMatcher.group().toLowerCase());
		}
		lcMatcher.appendTail(sb);
		
		log.debug("After second mod, sb is: " + sb.toString());
		
		return sb.toString();
	}

	private void enableControls (final boolean b)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				log.entry("setControlsEnabled");

				tfEmail.setEnabled(b);
				tfInitials.setEnabled(b);
				tfOrderNum.setEnabled(b);
				btnProof.setEnabled(b);
				btnOutput.setEnabled(b);
				cancelButton.setEnabled(b);
				
				if (b)
				{
					// Blank out the order number and put the focus on it.
					tfOrderNum.setText("");
					tfOrderNum.requestFocusInWindow();
				}

				log.exit("setControlsEnabled");
			}
		});
	}
}
