package info.chrismcgee.sky.artdept;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Dialog {

	public static void main(String[] args) {
		Thread dialogThread = new Thread(new Runnable() {
			@Override
			public void run() {
//				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JFrame frm = new JFrame();
				frm.setVisible(true);
				frm.setAlwaysOnTop(true);
				frm.setLocationRelativeTo(null);
				JOptionPane.showMessageDialog(frm, "Script completed successfully!", "Script Complete", JOptionPane.INFORMATION_MESSAGE);
				frm.setVisible(false);
				
//				JOptionPane pane = new JOptionPane();
//				JDialog dialog = pane.createDialog("Script completed successfully!");
//				dialog.setAlwaysOnTop(true);
////				dialog.show();
//				dialog.setVisible(true);
			}
		});
		dialogThread.start();
	}

}
