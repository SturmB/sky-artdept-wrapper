package info.chrismcgee.sky.artdept;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
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
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
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

/*import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
*/
import info.chrismcgee.components.DateManager;
import info.chrismcgee.components.Sanitizer;
import info.chrismcgee.enums.OSType;
import info.chrismcgee.sky.beans.LineItem;
import info.chrismcgee.sky.beans.Order;
import info.chrismcgee.sky.enums.DBType;
import info.chrismcgee.sky.enums.PrintingCompany;
import info.chrismcgee.sky.enums.ScriptType;
import info.chrismcgee.sky.tables.OrderManager;
import info.chrismcgee.util.ConnectionManager;
import info.chrismcgee.util.SendMail;
import net.miginfocom.swing.MigLayout;

public class ArtDept extends JFrame {

	/**
	 * Serialize, to keep Eclipse from throwing a warning message.
	 */
	private static final long serialVersionUID = -185001290066987954L;
	
	/**
	 * Identify our OS and build the scripts location from that.
	 */
	private static final String FILE_SYSTEM_PREFIX = OSType.getOSType() == OSType.MAC
			? File.separator + "Volumes"
			: File.separator + File.separator + "SKYFS";
	public static final String ARTDEPT_DRIVE = FILE_SYSTEM_PREFIX + File.separator + "ArtDept" + File.separator;

	static final Logger log = LogManager.getLogger(ArtDept.class.getName()); // For logging.
	public static boolean loggingEnabled = false;
	// Location of the script files to be run. This is either with or without the "Test/" portion.
	public static String scriptPath = ARTDEPT_DRIVE + "ArtDept" + File.separator
			+ "Scripts" + File.separator
			+ "sky-artdept" + File.separator
			+ "Production" + File.separator;
	// Preferences variables
	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	private static final String PREFS_EMAIL = "email";
	private static final String PREFS_INITIALS = "initials";
	// The location of the window (It's not resizable)
	private Point windowLocation;
	private Dimension windowSize;
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
	private KeyStroke ksMenuP = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	private KeyStroke ksMenuO = KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	private KeyStroke ksF1 = KeyStroke.getKeyStroke("F1");
	private KeyStroke ksF2 = KeyStroke.getKeyStroke("F2");
	private static String appName = "Sky Launcher";
	private static String appVersion = "4.0.4";
	private static String defaultTitle = appName + " v" + appVersion;
	private Pattern upperPattern;
	private Pattern lowerPattern;
	private Matcher ucMatcher;
	private Matcher lcMatcher;
	private File patternFile = null; // Text file that has the two RegEx patterns for fixing capitalizations.

	private String customerServiceRep = "";
	private boolean creditCard = false;
	private int shipDays = 0;
	private String wnaPo = "";
	private String messageToAddr = "customerservice@skyunlimitedinc.com";
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JCheckBoxMenuItem chckbxmntmTestVersion;
	private JCheckBoxMenuItem chckbxmntmDebugLog;
	private JPanel leftButtonsPane;
	private JPanel rightButtonsPane;
	private JFrame frm;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// Set some logging properties.
		
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (loggingEnabled) log.entry("main");
				try { // Create the dialog window and display it.
					
					if (OSType.getOSType() == OSType.MAC) {
						// Take the menu bar off the JFrame.
						System.setProperty("apple.laf.useScreenMenuBar", "true");
						// Set the name of the application menu item.
						System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Script Launcher");
					}
					
					// Set the look and feel.
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					// Create the frame and show it.
					ArtDept frame = new ArtDept();

					frame.setTitle(defaultTitle);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setLocationByPlatform(true);
					
					// Set the location and size of the window
					frame.windowLocation = new Point(frame.prefs.getInt("x", 100), frame.prefs.getInt("y", 100));
					frame.setLocation(frame.windowLocation);
					frame.windowSize = new Dimension(frame.prefs.getInt("width", 502), frame.prefs.getInt("height", 200));
					frame.setSize(frame.windowSize);

					frame.setVisible(true);
				} catch (Exception e) {
					if (loggingEnabled) log.error("Error in Main", e);
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public ArtDept() {
		
		List<Image> icons = new ArrayList<Image>();
		icons.add(new ImageIcon(getClass().getResource("/images/sky_launcher-02_16x16.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/images/sky_launcher-02_32x32.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/images/sky_launcher-02_48x48.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/images/sky_launcher-02_256x256.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/images/sky_launcher-02_512x512.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/images/sky_launcher-02_768x768.png")).getImage());
		setIconImages(icons);

		if (OSType.getOSType() == OSType.MAC) {
			setMinimumSize(new Dimension(502, 176));
		} else {
			setMinimumSize(new Dimension(502, 200));
		}

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
				if (loggingEnabled) log.entry("doProof Action");
				if (jobNumberReady && initialsReady)
				{
					if (loggingEnabled) log.trace("Proofing button hit!");
					Thread proofThread = new Thread()
					{
						public void run()
						{
							try {
								callScript(ScriptType.PROOF);
							} catch (InterruptedException e) {
								if (loggingEnabled) log.error("Interrupted!", e);
							}
						}
					};
					proofThread.start();
				}
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
				if (loggingEnabled) log.entry("doOutput Action");
				if (jobNumberReady && initialsReady)
				{
					if (loggingEnabled) log.trace("Output button hit!");
					Thread outputThread = new Thread()
					{
						public void run()
						{
							try {
								callScript(ScriptType.OUTPUT);
							} catch (InterruptedException e) {
								if (loggingEnabled) log.error("Interrupted!", e);
							}
						}
					};
					outputThread.start();
				}
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
//		setBounds(100, 100, 502, 176);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow]", "[][][]"));
		contentPanel.setBackground(Color.DARK_GRAY);

		{ // Menu bar that holds options for whether or not to run the
		  // test version of the script as well as to keep a if (loggingEnabled) log.
			menuBar = new JMenuBar();
			setJMenuBar(menuBar);
			{
				mnFile = new JMenu("Settings");
				menuBar.add(mnFile);
				{
					chckbxmntmTestVersion = new JCheckBoxMenuItem("Test Version");
					chckbxmntmTestVersion.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							AbstractButton aButton = (AbstractButton) e.getSource();
							setScriptPath(aButton.getModel().isSelected());
							setTitle();
						}
					});
					mnFile.add(chckbxmntmTestVersion);
				}
				{
					chckbxmntmDebugLog = new JCheckBoxMenuItem("Debug Log");
					chckbxmntmDebugLog.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							AbstractButton aButton = (AbstractButton) e.getSource();
							loggingEnabled = aButton.getModel().isSelected();
							setTitle();
						}
					});
					mnFile.add(chckbxmntmDebugLog);
				}
			}
		}
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
					if (loggingEnabled) log.trace("Deleting a character");
					sanitizeUsername();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					if (loggingEnabled) log.trace("Inserting a character");
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
					if (loggingEnabled) log.trace("Deleting a character");
					sanitizeInitials();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					if (loggingEnabled) log.trace("Inserting a character");
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
					if (loggingEnabled) log.trace("Deleting a character");
					santizeOrderNum();
//					log.debug(tfOrderNum.getText());
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					if (loggingEnabled) log.trace("Inserting a character");
					santizeOrderNum();
//					log.debug(tfOrderNum.getText());
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
			getContentPane().add(buttonPane, BorderLayout.SOUTH); // Default. Aligns the panel to the bottom.
			buttonPane.setBackground(Color.DARK_GRAY);
			
			{ // Create the "ScriptManager" button plus its listener.
				buttonPane.setLayout(new BorderLayout(0, 0));
			}
			{
				leftButtonsPane = new JPanel();
				leftButtonsPane.setBorder(null);
				leftButtonsPane.setBackground(Color.DARK_GRAY);
				buttonPane.add(leftButtonsPane, BorderLayout.WEST);
				btnProof = new JButton("Proof");
				leftButtonsPane.add(btnProof);
				btnProof.setEnabled(false); // Spawns disabled until input is sanitized.
				{	// Create the "Output" button plus its listener.
					btnOutput = new JButton("Output");
					leftButtonsPane.add(btnOutput);
					btnOutput.setEnabled(false); // Spawns disabled until input is sanitized.
					btnOutput.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (loggingEnabled) log.trace("Pressed the Output button");
							doOutput.actionPerformed(e); // When pressed, call the "doOutput" action.
						}
					});
//				getRootPane().setDefaultButton(btnOutput); // default.
				}
				btnProof.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (loggingEnabled) log.trace("Pressed the ScriptManager button");
						doProof.actionPerformed(e); // When pressed, call the "doProof" action.
					}
				});
			}
			{
				rightButtonsPane = new JPanel();
				rightButtonsPane.setBorder(null);
				rightButtonsPane.setBackground(Color.DARK_GRAY);
				buttonPane.add(rightButtonsPane, BorderLayout.EAST);
				{	// Create the "Cancel" button plus its listener.
					cancelButton = new JButton("Close");
					rightButtonsPane.add(cancelButton);
					cancelButton.setActionCommand("Cancel"); // default.
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// Close this ArtDept window when the button is pressed.
							if (loggingEnabled) log.trace("Cancel button pressed");
							Window window = SwingUtilities.windowForComponent((Component) e.getSource());
							window.dispose();
						}
					});
				}
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
		
/*		
		// Handle the Mac OS menu items & events
		Application macApplication = Application.getApplication();
		
		// About menu handler
		macApplication.setAboutHandler(new AboutHandler() {
			
			@Override
			public void handleAbout(AboutEvent ae) {
				JOptionPane.showMessageDialog(rootPane,
						appName + "\nversion " + appVersion + "\nCreated by Chris McGee for Sky Unlimited, Inc.\nhttp://chrismcgee.info\n©2014–2017 Chris McGee",
						"About the Script Runner",
						JOptionPane.PLAIN_MESSAGE);
				
			}
		});
		
		// Quit menu handler
		macApplication.setQuitHandler(new QuitHandler() {
			
			@Override
			public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
				qr.performQuit();
			}
		});
*/	}
	
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
		if (loggingEnabled) log.entry("verifyInputs");
		btnProof.setEnabled(jobNumberReady && initialsReady && usernameReady);
		btnOutput.setEnabled(jobNumberReady && initialsReady && usernameReady);
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
		if (loggingEnabled) log.entry("sanitizeOrderNum");
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
		if (loggingEnabled) log.entry("callScript");

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
		Order orderBean = new Order();
		// Connect to the database and set the database type.
		ConnectionManager.getInstance().setDBType(DBType.MYSQL);
		
		// Try getting the data from the database.
		try {
			orderBean = OrderManager.getOrder(tfOrderNum.getText());
			// If the job bean is now no longer null, then we have at least one order detail item,
			// so find the one that has the highest proof number and save that number
			// for the main proofing window in the script.
			/*if (jobBean != null) */proofNum = getHighestProofNumber(orderBean);
		} catch (SQLException e) {
			if (loggingEnabled) log.error("Could not obtain data from database.", e);
		}
//		if (loggingEnabled) log.debug("After database query, Job bean is null? " + (jobBean == null));
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
		} finally {
		}
//		if (loggingEnabled) log.debug("After text file grab, Job bean is null? " + (jobBean == null));
		
		// If this is a proofing job, then pre-add 1 to the proofing number.
		// Also do some more checks.
		if (scriptType == ScriptType.PROOF) {
			proofNum++;
			if (proofNum == 1 && orderBean == null) {
				// If the text file doesn't exist during a first proof of a job.
				if (JOptionPane.showConfirmDialog(null, "Text file not found.\n"
						+ "Send an email to Customer Service to create it?", "Send Email?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					SendMail.send(prefs.get(PREFS_EMAIL, "skyartdept@main.skyunlimitedinc.com"),
							messageToAddr,
							"Need text file for " + tfOrderNum.getText() + ".",
							"I need a text file made for Job #" + tfOrderNum.getText() + ", please. Thank you!");
					JOptionPane.showMessageDialog(null,
							"An email has been sent to have the file created.\n" +
									"Please keep a close eye on your inbox for a message\n" +
									"stating that it has been created. Then retry.",
							"Text File Not Found",
							JOptionPane.WARNING_MESSAGE);
				}
				enableControls(true);
				return;
			} else if (orderBean.getPrintingCompany() == null) {
				// If the Printing Company retrieved from the text file is null,
				// Then it is most likely an Order Acknowledgement text file.
				if (JOptionPane.showConfirmDialog(null, "The text file appears to be an Order Acknowledgement rather than a Sales Copy.\n"
						+ "Send an email to Customer Service to fix it?", "Send Email?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					SendMail.send(prefs.get(PREFS_EMAIL, "skyartdept@main.skyunlimitedinc.com"),
							messageToAddr,
							"Need correct text file for " + tfOrderNum.getText() + ".",
							"The text file with the name " + tfOrderNum.getText() + ".TXT is an Order Acknowledgement. " +
									"Please overwrite it with a Sales Copy for that Job number. Thank you!");
					JOptionPane.showMessageDialog(null, "An email has been sent to have the file corrected.\n" +
							"Please keep a close eye on your inbox for a message\n" +
							"stating that it has been created. Then retry.",
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
					SendMail.send(prefs.get(PREFS_EMAIL, "skyartdept@main.skyunlimitedinc.com"),
							messageToAddr,
							"Need text file for " + tfOrderNum.getText() + ".",
							"The text file with the name " + tfOrderNum.getText() + ".TXT is not for that job number. " +
									"Please overwrite it with one made for Job #" + tfOrderNum.getText() + ". Thank you!");
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
		boolean successfulRun = ScriptManager.scriptRunner(scriptType, tfOrderNum.getText(), tfInitials.getText(), orderBean, proofNum, scriptPath, customerServiceRep, creditCard, shipDays, wnaPo);
		
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
		
		if (successfulRun) {
			Thread dialogThread = new Thread(new Runnable() {
				@Override
				public void run() {
					if (frm == null) {
						frm = new JFrame();
					}
					frm.setVisible(true);
					frm.setAlwaysOnTop(true);
					frm.setLocationRelativeTo(null);
					JOptionPane.showMessageDialog(frm, "Script completed successfully!", "Script Complete", JOptionPane.INFORMATION_MESSAGE);
					frm.setVisible(false);
				}
			});
			dialogThread.start();
		}

	}
	
	/**
	 * If a job order IS found in the database, then get the highest proof number
	 * of all of its Order Detail items.
	 * 
	 * @param bean	The Job bean created from the database, so it has at least one OrderDetail item.
	 * @return	An integer of the highest proof number in the Job.
	 */
	private int getHighestProofNumber(Order bean)
	{
		if (loggingEnabled) log.entry("getHighestProofNumber");
		
		List<Integer> proofNums = new ArrayList<Integer>();
		try {
			for (LineItem li : bean.getLineItemList()) {
				proofNums.add(li.getProofNum());
			}
		} catch (NullPointerException err) {
			if (loggingEnabled) log.error("Nothing in the OrderDetail List! Returning 0.", err);
			return 0;
		}

		return Collections.max(proofNums);
	}
	
	/**
	 * When the database does not already have data for a job, then the needed customer data
	 * is taken from a text file of the job order's cover page.
	 * 
	 * @param jobNum	The Job number.
	 * @return	Job	A Job bean with the needed input fields filled out.
	 * @throws IOException
	 */
	private Order readText (String jobNum, Order bean, ScriptType scriptType) throws IOException {
		if (loggingEnabled) log.entry("readText");
		
		// Create a new Job bean.
		if (bean == null) bean = new Order();
		// Define where the ScriptManager text files are located.
		String workOrderFolder = ARTDEPT_DRIVE + "Work Orders/";
		textFile = new File(workOrderFolder + jobNum + ".txt");
		// If the text file is too small, then it is either empty or has some garbage in it.
		if (loggingEnabled) log.trace("The length of the associated text file is: " + textFile.length());
		if (textFile.length() < 2500)
		{
			if (bean.getId() != null)
				return bean;
			else
				return null;
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
		final Pattern rushPattern = Pattern.compile("QUICKSHIP", Pattern.CASE_INSENSITIVE);
		// Removed these two variables since we will no longer be using the "String" method to find the key phrase.
//		final String shipDaysPhrasePre = "WILL SHIP ";
//		final String shipDaysPhraseSuf = " WORKING DAYS";
		final String wnaOrderPoPhrase = "ON WNA ORDER ";
		
		// Matchers for the Patterns above. They must be initialized.
		Matcher shipDaysMatcher = shipDaysPattern.matcher("");
		Matcher creditCardMatcher = creditCardPattern.matcher("");
		Matcher overrunsMatcher = overrunsPattern.matcher("");
		Matcher noSamplesMatcher = noSamplesPattern.matcher("");
		Matcher rushMatcher = rushPattern.matcher("");

		// Prepare the text file for reading.
		String cache = null;
		Path workOrder = Paths.get(workOrderFolder, jobNum + ".TXT");
		if (loggingEnabled) log.debug("Path is: " + workOrder.toString());
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
					if (loggingEnabled) log.debug("Matcher has found: " + shipDaysMatcher.group(0));
//					shipDaysString = shipDaysString.replaceAll("\\D+", ""); // Removing the "String" method.
					shipDaysString = shipDaysMatcher.group(0).replaceAll("\\D+", "");
					if (loggingEnabled) log.debug("shipDaysString (from Matcher) is now: " + shipDaysString);
					
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
		
		// Begin setting the variables that will be placed into the Job bean.
/*		String printingCompany;
		try {
			printingCompany = StringUtils.trim(workOrderLines.get(0));
		} catch (Exception e) {
			if (loggingEnabled) log.error("It seems the Work Order is blank.", e);
			if (bean.getJobId() != null)
				return if (loggingEnabled) log.exit(bean);
			else
				return if (loggingEnabled) log.exit(null);
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
		
		// Also assign the other three booleans that will be placed into the Job bean.
		final boolean overruns = overrunsMatcher.find();
		final boolean noSamples = noSamplesMatcher.find();
		final boolean rush = rushMatcher.find();

		// Debugging lines to make sure the variables extracted are correct.
		if (loggingEnabled) log.debug("Printing Company: " + printingCompany + " with a length of " + printingCompany.length());
		if (loggingEnabled) log.debug("Company Name: " + companyName + " with a length of " + companyName.length());
		if (loggingEnabled) log.debug("Company PO: " + companyPO + " with a length of " + companyPO.length());
		if (loggingEnabled) log.debug("Shipping Date: " + shipDate + " with a length of " + shipDate.length());
		if (loggingEnabled) log.debug("CSR: " + customerServiceRep + " with a length of " + customerServiceRep.length());
		if (loggingEnabled) log.debug("Credit Card? " + creditCard);
		if (loggingEnabled) log.debug("Ship Days: " + shipDays);
		if (loggingEnabled) log.debug("WNA Order: " + wnaPo + " with a length of " + wnaPo.length());
		if (loggingEnabled) log.debug("Job Number read from file: " + readJobNum);
		if (loggingEnabled) log.debug("...compared to the entered job number: " + jobNum);
/*		if (printingCompany.toLowerCase().contains("acknowledgement")) {
			// The text file is an Order Acknowledgement. The program should inform the user and then quit gracefully.
			return null;
		}*/
		
		// If the job numbers do not match, then alert the user and quit out of the program.
/*		if (!jobNum.equals(readJobNum)) {
			JOptionPane.showMessageDialog(null,
					"The job number you entered and the one read from the text file do not match. Please correct and try again.",
					"Job Number Mismatch!",
					JOptionPane.ERROR_MESSAGE);
			return if (loggingEnabled) log.exit(null);
		}*/
		// Commented-out because I currently cannot see a way to have it NOT run the script, even though the bean will be null.
		
		// Now set the bean's properties.
		if (loggingEnabled) log.trace("Setting the Job bean's properties.");
		if (scriptType == ScriptType.PROOF) {
			switch (printingCompany) {
				case "AMERICAN ACCENTS":
					if (loggingEnabled) log.debug("This is an AA order.");
					bean.setPrintingCompany(PrintingCompany.AMERICAN_ACCENTS);
					break;
				case "AMERICAN CABIN SUPPLY":
					if (loggingEnabled) log.debug("This is an ACS order.");
					bean.setPrintingCompany(PrintingCompany.AMERICAN_CABIN_SUPPLY);
					break;
				case "AMERICAN YACHT SUPPLY":
					if (loggingEnabled) log.debug("This is an AYS order.");
					bean.setPrintingCompany(PrintingCompany.AMERICAN_YACHT_SUPPLY);
					break;
				case "SKY UNLIMITED, INC.":
					if (loggingEnabled) log.debug("This is a SKY order. (internal)");
					bean.setPrintingCompany(PrintingCompany.SKY_UNLIMITED_INC);
					break;
				default:
					if (loggingEnabled) log.debug("Incorrect printing company in text file. "
							+ "It is probably an Order Acknowledgement.");
					bean.setPrintingCompany(null);
			}
			bean.setId(readJobNum);
			bean.setCustomerName(companyName);
			bean.setCustomerPO(companyPO);
			
			try {
				bean.getLineItemList();
			} catch (NullPointerException err) {
				if (loggingEnabled) log.error("No LineItem List. Creating one.", err);
				LineItem lineItemBean = new LineItem();
				List<LineItem> lineItemList = new ArrayList<LineItem>();
				lineItemList.add(lineItemBean);
				bean.setLineItemList(lineItemList);
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
		bean.setRush(rush);
		
		try {
			if (shipDate.length() > 0)
				bean.setShipDateId(DateManager.usDateStringToSqlDate(shipDate));
		} catch (IllegalArgumentException e) {
			if (loggingEnabled) log.error("Date not in the correct format.", e);
//			return null;
		}

		if (loggingEnabled) log.debug("Bean set (inside readText).");
		// Now delete the text file, since it is no longer needed.
//		textFile.delete();
		
		return bean;
	}
	
	private String fixCapitalization(String companyNameRaw) {
		
		String companyName = WordUtils.capitalizeFully(companyNameRaw);
		
		upperPattern = Pattern.compile("(?i)(^(?!The\\b)(?!An\\b)(?![IO][nf]\\b)(?!Sir\\b)(?!Bob\\b)[\\w\\-]{2,3}(?=[\\s\\-\\+\\&])|(?<=\\s)\\w{2}(?=$)|\\b[b-df-hj-np-tv-z]{3}\\b|(?<!\\w)\\w{2,3}(?=\\))|(?<=[\\(\\-\\+\\&])\\w)");
		lowerPattern = Pattern.compile("(?i)(?!^)(?<!\\-)\\b(of|and|an?)\\b");

		patternFile = new File(scriptPath + "patterns.txt");
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
		
		ucMatcher = upperPattern.matcher("abc");
		lcMatcher = lowerPattern.matcher("abc");

		StringBuffer sb = new StringBuffer();
		ucMatcher.reset(companyName);
		while (ucMatcher.find()) {
			ucMatcher.appendReplacement(sb, ucMatcher.group().toUpperCase());
		}
		ucMatcher.appendTail(sb);
		
		if (loggingEnabled) log.debug("Between mods, sb is currently " + sb.toString());
		
		lcMatcher.reset(sb.toString());
		sb.setLength(0);
		while (lcMatcher.find()) {
			lcMatcher.appendReplacement(sb, lcMatcher.group().toLowerCase());
		}
		lcMatcher.appendTail(sb);
		
		if (loggingEnabled) log.debug("After second mod, sb is: " + sb.toString());
		
		return sb.toString();
	}

	private void enableControls (final boolean b)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (loggingEnabled) log.entry("setControlsEnabled");

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

			}
		});
	}
	
	/**
	 * Sets the path where the scripts will be used,
	 * depending on whether or not the user has chosen
	 * to use the Test scripts.
	 * 
	 * @param isTest True if the user has selected the checkbox
	 *               to use the Test version of the script.
	 */
	private void setScriptPath (boolean isTest) {
		scriptPath = ARTDEPT_DRIVE + "ArtDept" + File.separator
				+ "Scripts" + File.separator
				+ "sky-artdept" + File.separator;
		if (isTest) {
			scriptPath += "Test" + File.separator;
		} else {
			scriptPath += "Production" + File.separator;
		}
	}
	
	/**
	 * Sets the title of the JFrame window depending on
	 * what the user has selected from the menu checkboxes.
	 * 
	 */
	private void setTitle () {
		StringBuilder title = new StringBuilder(defaultTitle);
		String endParen = "";
		boolean bTest = chckbxmntmTestVersion.isSelected();
		boolean bLog = chckbxmntmDebugLog.isSelected();
		
		if (bTest || bLog) {
			title.append(" (");
			endParen = ")";
		}
		if (bTest) {
			title.append("Test");
		}
		if (bLog) {
			if (bTest) {
				title.append(", ");
			}
			title.append("Logging");
		}
		title.append(endParen);
		
		this.setTitle(title.toString());
	}

}
