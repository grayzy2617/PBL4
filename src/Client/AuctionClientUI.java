package Client;

import Common.AuctionMessage;
import Interface.AuctionEventListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;



public class AuctionClientUI extends JFrame implements AuctionEventListener {

 private AuctionClient client;
private static int clientCount = 0;

private JLabel lbResourcesHeld;
private JLabel lbBudget;
private JLabel lbParticipants;
private double reservePrice;
private String idSession;
private int participants = 0;
private double bidAmount = 0.0;
private JTable auctionTable;
private DefaultTableModel auctionTableModel;

private JTable bidTable;
private DefaultTableModel bidTableModel;
private JButton btnSendBid;

private JPanel topPanel;
private JPanel centerPanel;
private JPanel bidPanel;


public static void main(String[] args) {
    new AuctionClientUI();
}

public AuctionClientUI() {
    client = new AuctionClient(++clientCount);
    client.startConnSever(this);
    initComponents();
    System.out.println("Client ID: " + client.getClientId() +
            ", Budget: $" + client.getNode().getCurrentBudget());
}

private void initComponents() {
    setLayout(new BorderLayout());
    makeTopPanel();
    makeAuctionTablePanel();
    initWindow();
}

// ====== Top Panel (Resources - Participants - Budget) ======
private void makeTopPanel() {
    topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

    lbResourcesHeld = new JLabel("Resources: " + client.getNode().getAcquiredResources() + " Mbps");
    lbResourcesHeld.setFont(lbResourcesHeld.getFont().deriveFont(Font.BOLD, 14f));
    topPanel.add(lbResourcesHeld, BorderLayout.WEST);

    lbParticipants = new JLabel("Participants: 0", SwingConstants.CENTER);
    lbParticipants.setFont(lbParticipants.getFont().deriveFont(Font.BOLD, 14f));
    topPanel.add(lbParticipants, BorderLayout.CENTER);

    lbBudget = new JLabel("Budget: $" + client.getNode().getCurrentBudget());
    lbBudget.setFont(lbBudget.getFont().deriveFont(Font.BOLD, 14f));
    topPanel.add(lbBudget, BorderLayout.EAST);

    add(topPanel, BorderLayout.NORTH);
}

// ====== Bảng đấu giá chính ======
private void makeAuctionTablePanel() {
    String[] columnNames = {"ItemId", "Capacity", "ReservePrice", "Signal", "Time Left", "Tham gia"};
    auctionTableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 5;
        }
    };

    auctionTable = new JTable(auctionTableModel);
    auctionTable.setRowHeight(28);
    auctionTable.getColumnModel().getColumn(5)
            .setCellRenderer(new ButtonRenderer());
    auctionTable.getColumnModel().getColumn(5)
            .setCellEditor(new ButtonEditor(new JCheckBox()));

    JScrollPane scrollPane = new JScrollPane(auctionTable);
    scrollPane.setPreferredSize(new Dimension(550, 180));
    add(scrollPane, BorderLayout.CENTER);
}

private void initWindow() {
    setTitle("Auction Client");
    setSize(640, 480);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setVisible(true);
}

// =================== Khi có phiên đấu giá mới ===================
@Override
public void onNewAuction(AuctionMessage message) {
    if (message == null) return;

    String itemId = (String) message.getParam("ItemId");
    Double capacity = (Double) message.getParam("Capacity");
    String reservePrice = (String) message.getParam("ReservePrice");
    this.idSession = (String) message.getParam("idSession");
    System.out.println("idSession in UI: " + this.idSession + ", ItemId: " + itemId);
    this.reservePrice = Double.parseDouble(reservePrice);
    Integer participants = (Integer) message.getParam("participants");
    this.participants = participants;
        lbParticipants.setText("Participants: " + String.valueOf(participants));
    System.out.println(participants);
    addAuctionRow(itemId, capacity, reservePrice);
}

@Override
public void onAuctionResult(AuctionMessage message) {
    if (message == null) return;

    String winnerId = (String) message.getParam("WinnerId");
    Double winningBid = (Double) message.getParam("WinningBid");
    Double remainingCapacity = (Double) message.getParam("RemainingCapacity");

    String resultMsg;
    if (winnerId != null && winnerId.equals(client.getNode().getId())) {
        resultMsg = "Chúc mừng! Bạn đã thắng phiên đấu giá với bid: $" + winningBid;
        client.getNode().setAcquiredResources(
                client.getNode().getAcquiredResources() + winningBid 
        );
        lbResourcesHeld.setText("Resources: " + client.getNode().getAcquiredResources() + " Mbps");
    } else if (winnerId != null) {
        resultMsg = "Phiên đấu giá kết thúc. Người thắng: " + winnerId +
                " với bid: $" + winningBid;
        client.getNode().setCurrentBudget(client.getNode().getCurrentBudget() + this.bidAmount);
        lbBudget.setText("Budget: $" + client.getNode().getCurrentBudget());
    } else {
        resultMsg = "Phiên đấu giá kết thúc. Không có người thắng.";
    }

    JOptionPane.showMessageDialog(this, resultMsg, "Kết quả đấu giá",
            JOptionPane.INFORMATION_MESSAGE);

}
private void addAuctionRow(String itemId, Double capacity, String reservePrice ) {
    Object[] row = {itemId, capacity, reservePrice, "Strong", "30", "Tham gia"};
    auctionTableModel.addRow(row);
    startCountdownTimer(auctionTableModel.getRowCount() - 1);
}

private void startCountdownTimer(int rowIndex) {
    final int[] timeLeft = {30};
    Timer timer = new Timer(1000, e -> {
        timeLeft[0]--;
        if (timeLeft[0] >= 0)
            auctionTableModel.setValueAt(timeLeft[0], rowIndex, 4);
        if (timeLeft[0] <= 0) {
            ((Timer) e.getSource()).stop();
            if (!auctionTableModel.getValueAt(rowIndex, 5).equals("Hết hạn")) {
                auctionTableModel.setValueAt("Hết hạn", rowIndex, 5);
            }
        }
    });
    timer.start();
}

// =================== Button Renderer/Editor ===================
class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() { setOpaque(true); }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int col) {
        setText(value == null ? "" : value.toString());
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private int editingRow;

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> {

            fireEditingStopped();
            if (!"Hết hạn".equals(auctionTable.getValueAt(editingRow, 5))) {
                showBidPanel(); // hiển thị bảng gửi bid
            } else {
                JOptionPane.showMessageDialog(null, "Phiên này đã hết thời gian tham gia!");
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int col) {
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        editingRow = row;
        return button;
    }

    @Override
    public Object getCellEditorValue() { return label; }
}

// =================== Giao diện gửi Bid ===================
private void showBidPanel() {
    if (bidPanel != null) remove(bidPanel);

    bidPanel = new JPanel(new BorderLayout());
    bidPanel.setBorder(BorderFactory.createTitledBorder("Bidding Panel"));

    String[] cols = {"Id", "Tx", "Rx", "Signal", "Value", "Bid"};
    bidTableModel = new DefaultTableModel(cols, 0);
    bidTable = new JTable(bidTableModel);
    bidTable.setRowHeight(26);

   System.out.println(client.getNode().getId());
   List<Link> myLinks = NodeManager.getInstance().getLinks(client.getNode().getId());
   System.out.println(myLinks.size());
   for (Link link : myLinks) {
       Double Latency = Utility.computeLatency(link.getDistanceKm(), client.getNode().getTproc());
       Double SNR = Utility.computeSNR(client.getNode().getPt(), client.getNode().getGt(), client.getNode().getGr(),
               link.getDistanceKm(), link.getFrequencyMHz(), client.getNode().getT(), link.getB_alloc());
       String signal = String.format("%.4f", Latency*1000) + " ms, SNR: " + String.format("%.2f", SNR) + " dB";
       String value = String.valueOf(Utility.computeValue(SNR, Latency));
       String bid = String.valueOf(Utility.calculateBid(Double.valueOf(value),this.participants,client.getNode().getCurrentBudget(),client.getNode().getBudget_max(), link.getPriority(), this.reservePrice));
       bidTableModel.addRow(new Object[]{
                link.getId(),
                link.getTx(),
                link.getRx(),
                 signal,
               value,
                bid
         });

}
     
    JScrollPane bidScroll = new JScrollPane(bidTable);
    bidScroll.setPreferredSize(new Dimension(550, 120));
    bidPanel.add(bidScroll, BorderLayout.CENTER);

    // Nút gửi bid
    btnSendBid = new JButton("Send Bid");
    btnSendBid.addActionListener(e -> handleSendBid());
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    btnPanel.add(btnSendBid);

    bidPanel.add(btnPanel, BorderLayout.SOUTH);

    add(bidPanel, BorderLayout.SOUTH);
    revalidate();
    repaint();
}

private void handleSendBid() {
    int selectedRow = bidTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Hãy chọn 1 dòng để gửi bid!");
        return;
    }

    String bidText = (String) bidTableModel.getValueAt(selectedRow, 5);
    String linkId = (String) bidTableModel.getValueAt(selectedRow, 0);
    try {
        double bidAmount = Double.parseDouble(bidText);
        this.bidAmount = bidAmount;
            client.sendBid(bidAmount, linkId , this.idSession);
            JOptionPane.showMessageDialog(this, "Đã gửi bid thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            Double budget = client.getNode().getCurrentBudget() - bidAmount;
            client.getNode().setCurrentBudget(budget);
            lbBudget.setText("Budget: $" + client.getNode().getCurrentBudget());
        
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Please enter a valid number.");
    }
}



}
