package info.chrismcgee.sky.artdept;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import java.awt.event.ActionEvent;
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

    public final Preferences userPrefs = Preferences.userRoot().node(this.getClass().getName());

    public static final String PREFS_PRINTER_KEY = "printer";
    public static final String PREFS_PRINTER_DEFAULT = PrintServiceLookup.lookupDefaultPrintService().getName();

    public static final String PREFS_EMAIL_KEY = "email";
    public static final String PREFS_EMAIL_DEFAULT = "";

    public static final String PREFS_INITIALS_KEY = "initials";
    public static final String PREFS_INITIALS_DEFAULT = "";

    public Settings() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        try {
            lblPcName.setText(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            lblPcName.setText("Unknown");
        }

        PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer:
                printers) {
            cbPrinterName.addItem(printer.getName());
        }
        cbPrinterName.addActionListener(e -> onPrinterChange(e));
        cbPrinterName.setSelectedItem(userPrefs.get(PREFS_PRINTER_KEY, PREFS_PRINTER_DEFAULT));

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void onPrinterChange(ActionEvent e) {
        userPrefs.put(PREFS_PRINTER_KEY, (String) cbPrinterName.getSelectedItem());
    }

    public static void main(String[] args) {
        Settings dialog = new Settings();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
