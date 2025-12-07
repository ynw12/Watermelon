package client.ui;

import javax.swing.SwingUtilities;

import Staff.ui.StaffFrame;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClientFrame frame = new ClientFrame();
                frame.setVisible(true);
            }
        });
    }
}
