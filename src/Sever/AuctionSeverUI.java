package Sever;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AuctionSeverUI extends JFrame {
    private AuctionServer server;
    private JLabel lbCapacity;
    private JButton btnCreateAuction;
    private JLabel lbCapacityOfItem = new JLabel("Capacity: ");
    private JLabel lbReservePriceOfItem = new JLabel("ReservePrice: ");

    private JTextField capacityTxt = new JTextField(10);
    private JTextField reservePriceTxt = new JTextField(10);

    private JTable auctionTable;
    private DefaultTableModel tableModel;


    public static void main(String[] args) {
        AuctionServer server = new AuctionServer(100);
        AuctionSeverUI severUI = new AuctionSeverUI(server);
    }

    public AuctionSeverUI(AuctionServer server) {
        this.server = server;
        makeLableCapacity();
        makeFormInput();
        makeTable();
        initCpn();
        server.start();
    }

    public void makeLableCapacity() {
        // Label Capacity (góc trên bên phải)
        lbCapacity = new JLabel("Capacity: " + server.getCurrentCapacity() + "/" + server.getTotalCapa());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(lbCapacity, BorderLayout.EAST);
        this.add(topPanel, BorderLayout.NORTH);
    }

    public void makeFormInput() {
// Panel trái (form nhập)
        JPanel leftPanel = new JPanel(new GridLayout(1, 1, 10, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.add(lbCapacityOfItem);
        leftPanel.add(capacityTxt);
//        leftPanel.add(lbReservePriceOfItem);
//        leftPanel.add(reservePriceTxt);

        // Panel phải (nút)
        btnCreateAuction = new JButton("Tạo phiên đấu giá");
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        rightPanel.add(btnCreateAuction);
        btnCreateAuction.addActionListener(e -> {

            /*
            * check dk
            * tạo item , tạo phiên , update bảng, update capacity, xóa trắng txt,
            * */

                    String capacityStr = capacityTxt.getText().trim();
//                    String reservePriceStr = reservePriceTxt.getText().trim();
                    if (capacityStr.isEmpty() ) {
                        JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    checkCapacity(capacityStr);
//                    checkReservePrice(reservePriceStr);

                    Item item = new Item(Double.valueOf(capacityStr), "", false);
                    server.startAuctionSession(item);

                    // Cập nhật lại bảng và nhãn Capacity
                    tableModel.addRow(new Object[]{item.getId(), item.getCapacity(), "Đang mở"});
                    server.setCurrentCapacity(server.getCurrentCapacity()-item.getCapacity());
                    lbCapacity.setText("Capacity: " + server.getCurrentCapacity() + "/" + server.getTotalCapa());
                    // Xóa trắng các trường nhập liệu
                    capacityTxt.setText("");
                    reservePriceTxt.setText("");

                }
        );
        // Gom trái + phải vào center
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        this.add(centerPanel, BorderLayout.CENTER);

    }
    public void makeTable() {
        String[] columnNames = {"Phiên", "Số tài nguyên", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0);//tạo ra bảng với cột ở trên nhưng ko có data
        auctionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(auctionTable);
        this.add(scrollPane, BorderLayout.SOUTH);

    }
    public void checkCapacity(String capacity) {
        try {
            double cap = Double.parseDouble(capacity);
            if (cap <= 0 || cap > server.getCurrentCapacity()) {
                JOptionPane.showMessageDialog(null,
                        "Capacity phải là số dương và không vượt quá Capacity hiện tại của Server",
                        "Lỗi giá trị", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Capacity phải là một số hợp lệ",
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void checkReservePrice(String reservePrice) {
        if (reservePrice == null || !reservePrice.contains(",")) {
            JOptionPane.showMessageDialog(null,
                    "ReservePrice phải có dạng a,b (có dấu phẩy)",
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] parts = reservePrice.split(",");
        if (parts.length != 2) {
            JOptionPane.showMessageDialog(null,
                    "ReservePrice phải có đúng 2 giá trị dạng a,b",
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double a = Double.parseDouble(parts[0].trim());
            double b = Double.parseDouble(parts[1].trim());

            if (a >= b) {
                JOptionPane.showMessageDialog(null,
                        "Giá trị a phải nhỏ hơn b",
                        "Lỗi logic", JOptionPane.ERROR_MESSAGE);
                return;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Cả a và b phải là số hợp lệ",
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void initCpn() {
        this.setTitle("Auction Server");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

}