package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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

    private JTextArea logArea;

    private double resourcesHeld = 0.0;
    private double budget = 200.0;

    public AuctionClientUI() {
        client = new AuctionClient(++clientCount);
        client.startConnSever(this);

        initComponents();
        System.out.println("Client ID: " + client.getClientId() + ", Budget: $" + client.getBudget());
    }

    private void initComponents() {
        makeTopPanel();
        makeCenterPanel();
        initLookAtAndFeel();
        hideAuctionControls();

        /*
        * chú ý: là khi mà đã join vào việc đặt bid thì phải show thời gian diễn ra phiên đấu giá
        *
        * */
    }
    // bao gồm id, bid dạng  Bid: id,bid
    public void sendBid() {
        btnSendBid.addActionListener((ActionEvent e) -> {
            String bidText = bidField.getText().trim();
            try {
                double bidAmount = Double.parseDouble(bidText);
                if (bidValidate(bidAmount)) {
                    client.sendBid(bidAmount);
                    budget -= bidAmount;
                    lbBudget.setText("Budget: $" + client.getBudget());
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
    public void onNewAuction(String message) {
        if (message.isEmpty() || message == null) return;
        SwingUtilities.invokeLater(() -> {
            final JDialog dialog = new JDialog(this, "Auction Notification", true);
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
                label.setText("<html><br>" + message + "<br><br>Time left: <span id='timer'>" + timeLeft[0] + "</span> seconds</html>");
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
        });
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

        lbResourcesHeld = new JLabel("Resources: " + client.getAcquiredResources() + " Mbps");
        lbResourcesHeld.setFont(lbResourcesHeld.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(lbResourcesHeld, BorderLayout.WEST);

        lbBudget = new JLabel("Budget: $" + client.getBudget());
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
        lbYourBid.setVisible(true);
    }


    public static void main(String[] args) {
        AuctionClientUI ac = new AuctionClientUI();
        ;
    }
}
// AuctionEventListener.java


