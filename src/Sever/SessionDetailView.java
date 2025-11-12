package Sever;

import Database.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SessionDetailView extends JFrame {
    private String sessionId;
    private JTable table;
    private DefaultTableModel model;

    public SessionDetailView(String sessionId) {
        this.sessionId = sessionId;
        setTitle("Session details: " + sessionId);
        setSize(700, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        String[] cols = {"Client_id", "bid_amount"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadBids();
    }

    private void loadBids() {
        model.setRowCount(0);
        List<Bid> bids = DatabaseManager.getInstance().getBidsBySessionId(sessionId);
        // Return List<Bid> to caller for further processing as requested
        System.out.println("Bids for session " + sessionId + ": " + bids.size());
        for (Bid b : bids) {
            // tx/rx/distance not available from bid table directly; leave empty for caller to fill

            model.addRow(new Object[]{b.getClientId(), b.getValue()});
        }
    }
}
