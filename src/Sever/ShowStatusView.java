package Sever;

import Database.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ShowStatusView extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public ShowStatusView() {
        setTitle("Active Auction Sessions");
        setSize(700, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        String[] cols = {"ID_session", "Capacity", "ReservePrice", "Đã nhận Bid", "Xem chi tiết"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // only detail button column
            }
        };

        table = new JTable(model);
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<AuctionSession> sessions = DatabaseManager.getInstance().getActiveSessions();
        for (AuctionSession s : sessions) {
            Item item = s.getItem();
            String capacity = item != null ? String.valueOf(item.getCapacity()) : "-";
            String reserve = item != null ? item.getReservePrice() : "-";
            boolean hasBids = DatabaseManager.getInstance().hasBidsForSession(s.getId());
            String hasBidStr = hasBids ? "Có" : "Chưa";
            model.addRow(new Object[]{s.getId(), capacity, reserve, hasBidStr, "Xem chi tiết"});
        }
    }

    // simple button renderer/editor
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private int editingRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    String sessionId = (String) model.getValueAt(editingRow, 0);
                    // open detail view
                    new SessionDetailView(sessionId);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            button.setText(value == null ? "" : value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() { return button.getText(); }
    }
}
