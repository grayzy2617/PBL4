// src/Sever/NavigationManager.java
package Sever;

import javax.swing.*;

public class NavigationManager {
    private JFrame mainFrame;

    public NavigationManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void showCreateAuctionView() {
         new CreateAuctionDialog(mainFrame);
    }

    public void showStatusView() {
        JOptionPane.showMessageDialog(mainFrame, "Status View (implement as needed)");
    }

    public void showHistoryView() {
        JOptionPane.showMessageDialog(mainFrame, "History View (implement as needed)");
    }
}