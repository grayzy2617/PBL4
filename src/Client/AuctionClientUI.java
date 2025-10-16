package Client;


import Common.AuctionMessage;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
// removed unused import
import java.awt.event.ActionListener;
// removed unused import

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import Interface.AuctionEventListener;

public class AuctionClientUI extends JFrame implements AuctionEventListener {
    private AuctionClient client;
    private static int clientCount = 0;
    private JLabel lbResourcesHeld;
    private JLabel lbParticipants = new JLabel("Participants: ");
    private JLabel lbYourBid = new JLabel("Your bid ($): ");
    private JLabel lbBudget;

    private JTextField bidField;
    private JTextField participantsField;
    private JButton btnSendBid;

    private JTable auctionTable;
    private DefaultTableModel auctionTableModel;

    // private double resourcesHeld = 0.0; // unused
    private double budget = 200.0;
    public static void main(String[] args) {
       new AuctionClientUI();
        
    }
    public AuctionClientUI() {
        client = new AuctionClient(++clientCount);
        client.startConnSever(this);
        initComponents();
        System.out.println("Client ID: " + client.getClientId() + ", Budget: $" + client.getNode().getCurrentBudget());
    }

    private void initComponents() {
        makeTopPanel();
    makeCenterPanel();
    makeAuctionTablePanel();
        initLookAtAndFeel();
        hideAuctionControls();

    }
    public void sendBid() {
        btnSendBid.addActionListener((ActionEvent e) -> {
            String bidText = bidField.getText().trim();
            try {
                double bidAmount = Double.parseDouble(bidText);
                if (bidValidate(bidAmount)) {
                    client.sendBid(bidAmount);// thêm link chỗ này
                    JOptionPane.showMessageDialog(
                            this,                           // component cha (nếu trong JFrame, dùng this)
                            "Đã gửi bid thành công!",       // nội dung thông báo
                            "Thông báo",                    // tiêu đề của hộp thoại
                            JOptionPane.INFORMATION_MESSAGE // icon (dấu i xanh)
                    );
                    budget -= bidAmount;
                    lbBudget.setText("Budget: $" + client.getNode().getCurrentBudget());
                    bidField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid bid amount.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        });
    }
    @Override
    public void onNewAuction(  AuctionMessage message) {
        System.out.println("Received new auction");
        if (message == null) return;
        String itemId = (String) message.getParam("ItemId");
        Double capacity = (Double) message.getParam("Capacity");
        SwingUtilities.invokeLater(() -> {
            Double reservePrice = null;
            if (message.getParam("ReservePrice") != null) {
                reservePrice = (Double) message.getParam("ReservePrice");
            }
            final JDialog dialog = new JDialog(this, message.getTitle(), true);
            dialog.setLayout(new BorderLayout());

            JLabel label = new JLabel("<html><br>" + message + "<br><br>Time left: <span id='timer'>30</span> seconds</html>");
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));
            dialog.add(label, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            JButton joinButton = new JButton("Join");
            JButton cancelButton = new JButton("Cancel");
            buttonPanel.add(joinButton);
            buttonPanel.add(cancelButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            final int[] timeLeft = {30};
            Timer timer = new Timer(1000, null);
            timer.addActionListener(e -> {
                timeLeft[0]--;
                label.setText("<html><br>" +"ID: "+itemId+ ", Capacity"+ capacity + "<br><br>Time left: <span id='timer'>" + timeLeft[0] + "</span> seconds</html>");
                if (timeLeft[0] <= 0) {
                    timer.stop();
                    dialog.dispose();
                }
            });

            joinButton.addActionListener(e -> {
                timer.stop();
                dialog.dispose();
                client.joinAuction("Session1");
                showAuctionControls();
            });

            cancelButton.addActionListener(e -> {
                timer.stop();
                dialog.dispose();
                hideAuctionControls();
            });

            dialog.setSize(350, 180);
            dialog.setLocationRelativeTo(this);
            timer.start();
            dialog.setVisible(true);
            // Add auction info to table
            addAuctionRow(itemId, capacity, reservePrice);
        });
    }

    private void makeAuctionTablePanel() {
        // Table columns: ItemId, Capacity, ReservePrice, signal, Tham gia
        String[] columnNames = {"ItemId", "Capacity", "ReservePrice", "signal", "Tham gia"};
        auctionTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // only the 'Tham gia' button column is editable (to receive clicks)
                return column == 4;
            }
        };
        auctionTable = new JTable(auctionTableModel);
        // set preferred widths
        auctionTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        auctionTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        auctionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        auctionTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        auctionTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        // Add button renderer/editor for 'Tham gia' column
        auctionTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        auctionTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(auctionTable);
        scrollPane.setPreferredSize(new Dimension(480, 120));
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void addAuctionRow(String itemId, Double capacity, Double reservePrice) {
        Object[] row = new Object[5];
        row[0] = itemId;
        row[1] = capacity;
        row[2] = reservePrice;
        row[3] = "--"; // signal placeholder
        row[4] = "Tham gia"; // button label (cell renderer will draw it)
        auctionTableModel.addRow(row);
    }

    // Button renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Button editor to handle clicks
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private int editingRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    // perform join action
                    String itemId = (String) auctionTable.getValueAt(editingRow, 0);
                    client.joinAuction(itemId);
                    showAuctionControls();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            editingRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }

    public boolean bidValidate(double amount) {
        if (amount > 0 && amount <= budget) {
            return true;
        }
        return false;
    }

    public void makeTopPanel() {
        // ---------- TOP: chứa 2 label (trái / phải) ----------
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        lbResourcesHeld = new JLabel("Resources: "  + client.getNode().getAcquiredResources() + " Mbps");
        lbResourcesHeld.setFont(lbResourcesHeld.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(lbResourcesHeld, BorderLayout.WEST);

        lbBudget = new JLabel("Budget: $" + client.getNode().getCurrentBudget());
        lbBudget.setFont(lbBudget.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(lbBudget, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    public void makeCenterPanel() {
        // ---------- CENTER: form gửi bid ----------
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 0: Participants
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(lbParticipants, gbc);

        gbc.gridx = 1;
        participantsField = new JTextField(10);
        participantsField.setEditable(false);
        participantsField.setText("0");
        centerPanel.add(participantsField, gbc);

        // Hàng 1: Your bid
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(lbYourBid, gbc);

        gbc.gridx = 1;
        bidField = new JTextField(10);
        centerPanel.add(bidField, gbc);

        // Hàng 2: Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        btnSendBid = new JButton("Send Bid");
        centerPanel.add(btnSendBid, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void initLookAtAndFeel() {
        setTitle("Auction Client");
        setSize(520, 360);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void hideAuctionControls() {
        bidField.setVisible(false);
        btnSendBid.setVisible(false);
        participantsField.setVisible(false);
        lbParticipants.setVisible(false);

        lbYourBid.setVisible(false);
    }

    public void showAuctionControls() {
        bidField.setVisible(true);
        btnSendBid.setVisible(true);
        participantsField.setVisible(true);
        lbParticipants.setVisible(true);
        lbYourBid.setVisible(true);
    }


}
// AuctionEventListener.java


