package staff.ui;

import javax.swing.SwingUtilities;

public class StaffMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StaffFrame frame = new StaffFrame();
                frame.setVisible(true);
            }
        });
    }
}