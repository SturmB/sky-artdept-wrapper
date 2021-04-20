package info.chrismcgee.sky.artdept;

import info.chrismcgee.components.Sanitizer;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.prefs.Preferences;


/**
 * TODO: Settings that need to be included
 *  - return email address [christopher.mcgee@main.skyunlimitedinc.com]
 *  - initials [CM]
 *  - email address to be notified of a broken text file [customerservice@skyunlimitedinc.com]
 *  - which script to run:
 *      - local copy (on local HDD)
 *      - server, test version (beta)
 *      - server, production version (main)
 *  - directory/location of local script
 *  - directory/location of server scripts
 *  - PC name (pre-fill)
 *  - printer name (drop-down?)
 */

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
    private ButtonGroup scriptGroup;

    public final Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    public static final String PATH_SERVER = File.separator + File.separator + "SKYFS"
            + File.separator + "ArtDept"
            + File.separator + "ArtDept"
            + File.separator + "Scripts"
            + File.separator + "sky-artdept";
    public static final String PATH_TEST = PATH_SERVER + File.separator + "Test";

    public static final String PREFS_PRINTER_KEY = "Printer";
    public static final String PREFS_PRINTER_DEFAULT = PrintServiceLookup.lookupDefaultPrintService().getName();

    public static final String PREFS_NOTIFY_EMAIL_KEY = "NotifyEmail";
    public static final String PREFS_NOTIFY_EMAIL_DEFAULT = "customerservice@skyunlimitedinc.com";

    public static final String PREFS_YOUR_EMAIL_KEY = "YourEmail";
    public static final String PREFS_YOUR_EMAIL_DEFAULT = "";

    public static final String PREFS_INITIALS_KEY = "YourInitials";
    public static final String PREFS_INITIALS_DEFAULT = "";

    public static final String PREFS_DIR_PROD_KEY = "ScriptDirProd";
    public static final String PREFS_DIR_PROD_DEFAULT = PATH_SERVER + File.separator + "Production";

    public static final String PREFS_DIR_TEST_KEY = "ScriptDirTest";
    public static final String PREFS_DIR_TEST_DEFAULT = PATH_SERVER + File.separator + "Test";

    public static final String PREFS_DIR_LOCAL_KEY = "ScriptDirLocal";
    public static final String PREFS_DIR_LOCAL_DEFAULT = "";

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

        // Initialize the Path fields
        tfDirProd.setText(prefs.get(PREFS_DIR_PROD_KEY, PREFS_DIR_PROD_DEFAULT));
        tfDirTest.setText(prefs.get(PREFS_DIR_TEST_KEY, PREFS_DIR_TEST_DEFAULT));
        tfDirLocal.setText(prefs.get(PREFS_DIR_LOCAL_KEY, PREFS_DIR_LOCAL_DEFAULT));

        // Decorate and add listeners to the browse buttons
        IconFontSwing.register(FontAwesome.getIconFont());
        Icon iconFolder = IconFontSwing.buildIcon(FontAwesome.FOLDER_OPEN, 12);
        btnProdBrowse.setIcon(iconFolder);
        btnTestBrowse.setIcon(iconFolder);
        btnLocalBrowse.setIcon(iconFolder);
        btnProdBrowse.addActionListener(e -> onBrowse(tfDirProd));
        btnTestBrowse.addActionListener(e -> onBrowse(tfDirTest));
        btnLocalBrowse.addActionListener(e -> onBrowse(tfDirLocal));

        // Initialize Radio Buttons and their group
        scriptGroup = new ButtonGroup();
        scriptGroup.add(rbProd);
        scriptGroup.add(rbTest);
        scriptGroup.add(rbLocal);
        rbProd.addActionListener(e -> onRadioButton());
        rbTest.addActionListener(e -> onRadioButton());
        rbLocal.addActionListener(e -> onRadioButton());
        rbProd.setSelected(true);
        onRadioButton();

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
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void onBrowse(JTextField textField) {
        String startDir = textField.getText().length() > 0 ? textField.getText() : System.getProperty("user.home");
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(startDir));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File chosenDir = chooser.getSelectedFile();
        textField.setText(chosenDir.getPath());
    }

    private void validateAll() {
        buttonOK.setEnabled(
                validateEmail(tfNotifyEmail)
                        & validateEmail(tfYourEmail)
                        & validateInitials()
        );
    }

    private boolean validateEmail(JTextField textField) {
        if (Sanitizer.isValidEmail(textField.getText())) {
            textField.setBackground(Color.WHITE);
            textField.setForeground(Color.BLACK);
            return true;
        }
        textField.setBackground(Color.RED);
        textField.setForeground(Color.WHITE);
        return false;
    }

    private boolean validateInitials() {
        if (Sanitizer.isNotEmpty(tfInitials.getText())) {
            tfInitials.setBackground(Color.WHITE);
            tfInitials.setForeground(Color.BLACK);
            return true;
        }
        tfInitials.setBackground(Color.RED);
        tfInitials.setForeground(Color.WHITE);
        return false;
    }

    public static void main(String[] args) {
        // Set the look and feel.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Settings dialog = new Settings();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
