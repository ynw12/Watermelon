package Staff.ui;

import javax.swing.SwingUtilities;

import Staff.ui.StaffFrame;

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