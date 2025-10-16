// src/Sever/AuctionSeverUI.java
package Sever;

import javax.swing.*;

import java.awt.*;

public class AuctionSeverUI extends JFrame {
    private JLabel lbRevenue;
    private JLabel lbResources;
    private NavigationManager navigationManager;
    JButton btnCreateAuction = new JButton("Create Auction");
    JButton btnViewStatus = new JButton("View Status");
    JButton btnHistory = new JButton("History");

    public static void main(String[] args)// main
    {
     new AuctionSeverUI();
    }

    // Constructor
    public AuctionSeverUI() {
        this.navigationManager = new NavigationManager(this);
        initUI();
    }
    // khởi tạo các thành phần giao diện
    private void initUI() {
        topPanel();
        centerPanel();
        eventCreateAuction();
        eventViewStatus();
        eventHistory();
        initWindow();
    }
    private void eventCreateAuction() {
        // Button actions
        btnCreateAuction.addActionListener(e -> {
            navigationManager.showCreateAuctionView();
        });
    }

    private void eventViewStatus() {
        btnViewStatus.addActionListener(e -> {
            navigationManager.showStatusView();
        });
    }

    private void eventHistory() {
        btnHistory.addActionListener(e -> {
            navigationManager.showHistoryView();
        });
    }

    private void topPanel() {
        // Top panel: Revenue (left), Resources (right)
        lbRevenue = new JLabel("Revenue: " + AuctionServer.getInstance().getRevenue());
        lbResources = new JLabel("Resources: " + AuctionServer.getInstance().getCurrentCapacity() + "/" + AuctionServer.getInstance().getTotalCapa());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(lbRevenue, BorderLayout.WEST);
        topPanel.add(lbResources, BorderLayout.EAST);
        this.add(topPanel, BorderLayout.NORTH);
    }// doanh thu và tài nguyên

    private void centerPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 200, 40, 200));


        btnCreateAuction.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnViewStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHistory.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(btnCreateAuction);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(btnViewStatus);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(btnHistory);

        this.add(centerPanel, BorderLayout.CENTER);
    }// các chức năng

    private void initWindow() {
        this.setTitle("Auction Server");
        this.setSize(600, 350);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }


}