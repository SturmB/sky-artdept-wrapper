package info.chrismcgee.sky.artdept;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

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
    private JComboBox cbPrinterName;

    private PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
    private PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();

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

        for (PrintService printer:
             printers) {
            cbPrinterName.addItem(printer.getName());
        }
        cbPrinterName.setSelectedItem(defaultPrinter.getName());

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

    public static void main(String[] args) {
        Settings dialog = new Settings();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
