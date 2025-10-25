// src/Sever/CreateAuctionDialog.java
package Sever;

import javax.swing.*;

import Database.DatabaseManager;

import java.awt.*;
 

public class CreateAuctionDialog extends JDialog {
    private JTextField resourceField = new JTextField(10);
    private JTextField reservePriceField = new JTextField(10);
    JButton btnConfirmResource = new JButton("Confirm Resource");
    JButton btnCreate = new JButton("Create Auction");
    JButton btnBack = new JButton("Back");

    public CreateAuctionDialog(JFrame parent) {
        super(parent, "Create Auction Session", true);
        initDialog();
    }

    private void initDialog() {
        setLayout(new BorderLayout());
        topPanel();
        centerPanel();
        btnBack.addActionListener(e -> dispose());
        processConfirmResource();
        processCreateAuction();
        initWindow();

    }

    private void processConfirmResource() {
        btnConfirmResource.addActionListener(e -> {
            String resourceStr = resourceField.getText().trim();
            try {
                double resource = Double.parseDouble(resourceStr);
                if (resource <= 0) {
                    JOptionPane.showMessageDialog(this, "Resource must be positive.");
                } else if (resource > AuctionServer.getInstance().getCurrentCapacity()) {
                    JOptionPane.showMessageDialog(this, "Not enough resources available.");
                } else {
                    
                    double avg_bid = DatabaseManager.getInstance().getAverageBidAmount();
                    
                    if (avg_bid == 0.00) {
                        System.out.println("avg bid = 0");
                        reservePriceField.setText("500");
                    } else {
                        System.out.println("avg bid = " + avg_bid);
                        reservePriceField.setText(String.format("%.2f", avg_bid * 0.8));
                    }

                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid resource input.");
            }
        });
    }

    private void processCreateAuction() {
        btnCreate.addActionListener(e -> {
            String resourceStr = resourceField.getText().trim();
            String reserveStr = reservePriceField.getText().trim();
            if (resourceStr.isEmpty() || reserveStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
            dispose();
            Item item = new Item(Double.parseDouble(resourceStr), reserveStr);
            String id = DatabaseManager.getInstance().generateId("item");
            item.setId(id);
            System.out.println("Item ID before insert: " + item.getId());
      if(!DatabaseManager.getInstance().insertItem(item)) {
        JOptionPane.showMessageDialog(this, "Failed to create item in database.");
      } else {
                AuctionServer.getInstance().startAuctionSession(item);
          
      }
        });
    }

    private void topPanel() {
        // Top panel: Revenue (left), Resources (right)
        JLabel lbRevenue = new JLabel("Revenue: " + AuctionServer.getInstance().getRevenue());
        JLabel lbResources = new JLabel("Resources: " + AuctionServer.getInstance().getCurrentCapacity() + "/"
                + AuctionServer.getInstance().getTotalCapa());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(lbRevenue, BorderLayout.WEST);
        topPanel.add(lbResources, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void centerPanel() {
        // Center panel: Resource input, confirm button
        JPanel resourcePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel resourceLabel = new JLabel("Resource:");
        resourcePanel.add(resourceLabel);
        resourcePanel.add(resourceField);
        resourcePanel.add(btnConfirmResource);

        // Reserve price input
        JPanel reservePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel reserveLabel = new JLabel("Reserve Price:");
        reservePriceField = new JTextField(10);
        reservePanel.add(reserveLabel);
        reservePanel.add(reservePriceField);

        // Bottom panel: Confirm and Back buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(btnCreate);
        buttonPanel.add(btnBack);

        // Main center layout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(resourcePanel);
        centerPanel.add(reservePanel);
        centerPanel.add(buttonPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void initWindow() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}