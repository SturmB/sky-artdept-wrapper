package info.chrismcgee.sky.artdept;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.jthemedetecor.OsThemeDetector;
import info.chrismcgee.components.Sanitizer;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
//import java.util.ResourceBundle;
import java.util.prefs.Preferences;


public class Settings extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblPcName;
    private JComboBox<String> cbPrinterName;
    private JTextField tfNotifyEmail;
    private JTextField tfYourEmail;
    private JTextField tfInitials;
    private JRadioButton rbProd;
    private JTextField tfDirProd;
    private JButton btnProdBrowse;
    private JRadioButton rbTest;
    private JTextField tfDirTest;
    private JButton btnTestBrowse;
    private JRadioButton rbLocal;
    private JTextField tfDirLocal;
    private JButton btnLocalBrowse;
    private JTextField tfPatternsFile;
    private JButton btnPatternsBrowse;
    private JTextField tfWorkOrdersDir;
    private JButton btnWorkOrdersBrowse;
    private JCheckBox debugLogCheckBox;

//    private static final ResourceBundle settingsBundle = ResourceBundle.getBundle("Settings");

    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    public static final String PATH_ARTDEPT = File.separator + File.separator + "SKYFS"
            + File.separator + "ArtDept";
    public static final String PATH_SCRIPTS_ROOT = PATH_ARTDEPT
            + File.separator + "ArtDept"
            + File.separator + "Scripts"
            + File.separator + "sky-artdept";
//    public static final String PATH_TEST = PATH_SCRIPTS_ROOT + File.separator + "Test";

    public static final String PREFS_PRINTER_KEY = "Printer";
    public static final String PREFS_PRINTER_DEFAULT = PrintServiceLookup.lookupDefaultPrintService().getName();

    public static final String PREFS_NOTIFY_EMAIL_KEY = "NotifyEmail";
    public static final String PREFS_NOTIFY_EMAIL_DEFAULT = "customerservice@skyunlimitedinc.com";

    public static final String PREFS_YOUR_EMAIL_KEY = "YourEmail";
    public static final String PREFS_YOUR_EMAIL_DEFAULT = "";

    public static final String PREFS_INITIALS_KEY = "YourInitials";
    public static final String PREFS_INITIALS_DEFAULT = "";

    private static final String PREFS_DIR_PROD_KEY = "ScriptDirProd";
    private static final String PREFS_DIR_PROD_DEFAULT = PATH_SCRIPTS_ROOT + File.separator + "Production";

    private static final String PREFS_DIR_TEST_KEY = "ScriptDirTest";
    private static final String PREFS_DIR_TEST_DEFAULT = PATH_SCRIPTS_ROOT + File.separator + "Test";

    private static final String PREFS_DIR_LOCAL_KEY = "ScriptDirLocal";
    private static final String PREFS_DIR_LOCAL_DEFAULT = "";

    public static final String PREFS_DIR_KEY = "ScriptDir";
    public static final String PREFS_DIR_DEFAULT = PREFS_DIR_PROD_DEFAULT;

    public static final String PREFS_PATTERNS_KEY = "PatternsFile";
    public static final String PREFS_PATTERNS_DEFAULT = PREFS_DIR_DEFAULT + File.separator + "patterns.txt";

    public static final String PREFS_WORK_ORDERS_KEY = "WorkOrdersDir";
    public static final String PREFS_WORK_ORDERS_DEFAULT = PATH_ARTDEPT + File.separator + "Work Orders";

    public static final String PREFS_LOGGING_KEY = "DebugLog";
    public static final boolean PREFS_LOGGING_DEFAULT = false;

    public Settings() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        DocumentListener tfListener = new DocumentListener() {
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
                // Plain text components do not fire these events.
            }
        };

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // Populate the PC Name label
        try {
            lblPcName.setText(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            lblPcName.setText("Unknown");
        }

        // Populate the Printer list and select the default one
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer :
                printers) {
            cbPrinterName.addItem(printer.getName());
        }
        cbPrinterName.setSelectedItem(prefs.get(PREFS_PRINTER_KEY, PREFS_PRINTER_DEFAULT));

        // Initialize the notification Email text field
        tfNotifyEmail.getDocument().addDocumentListener(tfListener);
        tfNotifyEmail.setText(prefs.get(PREFS_NOTIFY_EMAIL_KEY, PREFS_NOTIFY_EMAIL_DEFAULT));

        // Initialize the user's Email text field
        tfYourEmail.getDocument().addDocumentListener(tfListener);
        tfYourEmail.setText(prefs.get(PREFS_YOUR_EMAIL_KEY, PREFS_YOUR_EMAIL_DEFAULT));

        // Initialize the Initials text field
        tfInitials.getDocument().addDocumentListener(tfListener);
        tfInitials.setText(prefs.get(PREFS_INITIALS_KEY, PREFS_INITIALS_DEFAULT));

        // Initialize the Debug Log checkbox
        debugLogCheckBox.setSelected(prefs.getBoolean(PREFS_LOGGING_KEY, PREFS_LOGGING_DEFAULT));

        // Initialize the Path fields
        tfDirProd.setText(prefs.get(PREFS_DIR_PROD_KEY, PREFS_DIR_PROD_DEFAULT));
        tfDirTest.setText(prefs.get(PREFS_DIR_TEST_KEY, PREFS_DIR_TEST_DEFAULT));
        tfDirLocal.setText(prefs.get(PREFS_DIR_LOCAL_KEY, PREFS_DIR_LOCAL_DEFAULT));

        // Add listeners to the Path fields
        tfDirProd.getDocument().addDocumentListener(tfListener);
        tfDirTest.getDocument().addDocumentListener(tfListener);
        tfDirLocal.getDocument().addDocumentListener(tfListener);

        // Decorate and add listeners to the browse buttons
        IconFontSwing.register(FontAwesome.getIconFont());
        Icon iconFolder = IconFontSwing.buildIcon(FontAwesome.FOLDER_OPEN, 12);
        btnProdBrowse.setIcon(iconFolder);
        btnTestBrowse.setIcon(iconFolder);
        btnLocalBrowse.setIcon(iconFolder);
        btnProdBrowse.addActionListener(e -> onBrowse(tfDirProd, JFileChooser.DIRECTORIES_ONLY));
        btnTestBrowse.addActionListener(e -> onBrowse(tfDirTest, JFileChooser.DIRECTORIES_ONLY));
        btnLocalBrowse.addActionListener(e -> onBrowse(tfDirLocal, JFileChooser.DIRECTORIES_ONLY));

        // Initialize Radio Buttons
        rbProd.addActionListener(e -> onRadioButton());
        rbTest.addActionListener(e -> onRadioButton());
        rbLocal.addActionListener(e -> onRadioButton());
        rbProd.setSelected(true);
        onRadioButton();

        // Initialize the Patterns text field
        tfPatternsFile.getDocument().addDocumentListener(tfListener);
        tfPatternsFile.setText(prefs.get(PREFS_PATTERNS_KEY, PREFS_PATTERNS_DEFAULT));

        // Initialize the Patterns browse button
        btnPatternsBrowse.setIcon(iconFolder);
        btnPatternsBrowse.addActionListener(e -> onBrowse(tfPatternsFile, JFileChooser.FILES_ONLY));

        // Initialize the Work Orders Dir text field
        tfWorkOrdersDir.getDocument().addDocumentListener(tfListener);
        tfWorkOrdersDir.setText(prefs.get(PREFS_WORK_ORDERS_KEY, PREFS_WORK_ORDERS_DEFAULT));

        // Initialize the Work Orders Dir browse button
        btnWorkOrdersBrowse.setIcon(iconFolder);
        btnWorkOrdersBrowse.addActionListener(e -> onBrowse(tfWorkOrdersDir, JFileChooser.DIRECTORIES_ONLY));

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Validate upon initializing this dialog
        validateAll();
    }

    public void onRadioButton() {
        tfDirProd.setEnabled(rbProd.isSelected());
        tfDirTest.setEnabled(rbTest.isSelected());
        tfDirLocal.setEnabled(rbLocal.isSelected());
        btnProdBrowse.setEnabled(rbProd.isSelected());
        btnTestBrowse.setEnabled(rbTest.isSelected());
        btnLocalBrowse.setEnabled(rbLocal.isSelected());
        validateAll();
    }

    private void onOK() {
        // Save all settings
        prefs.put(PREFS_PRINTER_KEY, (String) cbPrinterName.getSelectedItem());
        prefs.put(PREFS_NOTIFY_EMAIL_KEY, tfNotifyEmail.getText());
        prefs.put(PREFS_YOUR_EMAIL_KEY, tfYourEmail.getText());
        prefs.put(PREFS_INITIALS_KEY, tfInitials.getText());
        prefs.put(PREFS_DIR_PROD_KEY, tfDirProd.getText());
        prefs.put(PREFS_DIR_TEST_KEY, tfDirTest.getText());
        prefs.put(PREFS_DIR_LOCAL_KEY, tfDirLocal.getText());
        prefs.put(PREFS_PATTERNS_KEY, tfPatternsFile.getText());
        prefs.put(PREFS_WORK_ORDERS_KEY, tfWorkOrdersDir.getText());
        prefs.putBoolean(PREFS_LOGGING_KEY, debugLogCheckBox.isSelected());
        // Production
        if (tfDirTest.isEnabled()) {
            prefs.put(PREFS_DIR_KEY, prefs.get(PREFS_DIR_TEST_KEY, PREFS_DIR_TEST_DEFAULT));
        } else if (tfDirLocal.isEnabled()) {
            prefs.put(PREFS_DIR_KEY, prefs.get(PREFS_DIR_LOCAL_KEY, PREFS_DIR_LOCAL_DEFAULT));
        } else { // tfDirProd is enabled
            prefs.put(PREFS_DIR_KEY, prefs.get(PREFS_DIR_PROD_KEY, PREFS_DIR_PROD_DEFAULT));
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void validateField(JTextField textField, boolean valid) {
        textField.putClientProperty(FlatClientProperties.OUTLINE, valid? null : FlatClientProperties.OUTLINE_ERROR);
    }

    private boolean onPathChange(JTextField textField) {
        File file = new File(textField.getText());
        boolean valid = !textField.isEnabled() || file.exists();
        validateField(textField, valid);
        return valid;
    }

    private void onBrowse(JTextField textField, int mode) {
        String startDir = textField.getText().length() > 0 ? textField.getText() : System.getProperty("user.home");
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(startDir));
        chooser.setFileSelectionMode(mode);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // If the user cancels the dialog
        }
        File chosenDir = chooser.getSelectedFile();
        textField.setText(chosenDir.getPath());
    }

    private void validateAll() {
        buttonOK.setEnabled(
                validateEmail(tfNotifyEmail)
                        & validateEmail(tfYourEmail)
                        & validateInitials()
                        & onPathChange(tfDirProd)
                        & onPathChange(tfDirTest)
                        & onPathChange(tfDirLocal)
                        & onPathChange(tfPatternsFile)
                        & onPathChange(tfWorkOrdersDir)
        );
    }

    private boolean validateEmail(JTextField textField) {
        boolean validEmail = Sanitizer.isValidEmail(textField.getText());
        validateField(textField, validEmail);
        return validEmail;
    }

private boolean validateInitials(){
        boolean notEmpty = Sanitizer.isNotEmpty(tfInitials.getText());
        validateField(tfInitials, notEmpty);
        return notEmpty;
    }

    public static void main(String[] args) {
        // Set the look and feel.
        // Set the look and feel
        FlatCarbonIJTheme.install();

        final OsThemeDetector detector = OsThemeDetector.getDetector();
        detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> {
            if (isDark) {
                // The OS switched to a dark theme
                FlatCarbonIJTheme.install();
            } else {
                // The OS switched to a light theme
                FlatCyanLightIJTheme.install();
            }
            FlatLaf.updateUI();
        }));

        Settings dialog = new Settings();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
