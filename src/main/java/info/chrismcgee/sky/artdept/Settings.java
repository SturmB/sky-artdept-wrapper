package info.chrismcgee.sky.artdept;

import info.chrismcgee.components.Sanitizer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private JTextField tfProd;
    private JRadioButton rbTest;
    private JTextField tfTest;
    private JRadioButton rbLocal;
    private JTextField tfLocal;
    private JButton bProdBrowse;

    public final Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    public static final String PREFS_PRINTER_KEY = "Printer";
    public static final String PREFS_PRINTER_DEFAULT = PrintServiceLookup.lookupDefaultPrintService().getName();

    public static final String PREFS_NOTIFY_EMAIL_KEY = "NotifyEmail";
    public static final String PREFS_NOTIFY_EMAIL_DEFAULT = "customerservice@skyunlimitedinc.com";

    public static final String PREFS_YOUR_EMAIL_KEY = "YourEmail";
    public static final String PREFS_YOUR_EMAIL_DEFAULT = "";

    public static final String PREFS_INITIALS_KEY = "YourInitials";
    public static final String PREFS_INITIALS_DEFAULT = "";

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

    private void onOK() {
        // Save all settings
        prefs.put(PREFS_PRINTER_KEY, (String) cbPrinterName.getSelectedItem());
        prefs.put(PREFS_NOTIFY_EMAIL_KEY, tfNotifyEmail.getText());
        prefs.put(PREFS_YOUR_EMAIL_KEY, tfYourEmail.getText());
        prefs.put(PREFS_INITIALS_KEY, tfInitials.getText());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void validateAll() {
        buttonOK.setEnabled(validateEmail(tfNotifyEmail) & validateEmail(tfYourEmail) & validateInitials());
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
        Settings dialog = new Settings();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}