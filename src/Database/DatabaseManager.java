package Database;

import Sever.AuctionSession;
import Sever.Item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Sever.Bid;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/pbl4";
    private final String USER = "root";
    private final String PASSWORD = "";

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // nạp driver
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("conn success");
            connection.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.err.println(" conn err");
        }
    }

    // Trả về instance duy nhất
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Double getAverageBidAmount() {
        String sql = "SELECT AVG(bid_amount) AS avg_bid FROM bid";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble("avg_bid");
                if (rs.wasNull()) {
                    return 0.00; // no bids in table
                }
                return avg;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.00;
    }

  public synchronized    String generateId(String tableName) {
    String prefix = "";
    String idColumn = "";

    switch (tableName.toLowerCase()) {
        case "link":
            prefix = "L"; idColumn = "link_id"; break;
        case "item":
            prefix = "ITEM"; idColumn = "item_id"; break;
        case "auctionsession":
            prefix = "SS"; idColumn = "id"; break;
        case "bid":
            prefix = "B"; idColumn = "bid_id"; break;
        default:
            System.out.println("⚠️ Unknown table name: " + tableName);
            return null;
    }

    String sql = "SELECT " + idColumn + " FROM " + tableName + " ORDER BY " + idColumn + " DESC LIMIT 1";

    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        int newNum = 1; // mặc định là 1 nếu bảng trống

        if (rs.next()) {
            String lastId = rs.getString(idColumn); // ví dụ "ITEM001"
            System.out.println("Last ID in " + tableName + ": " + lastId);
            if (lastId != null && lastId.matches(".*\\d+$"))// kiểm tra khác null và có phần số ở cuối 
            {
                System.out.println("qua san loc 1 ");
                String numPart = lastId.replaceAll("\\D+", "");// \\D là ký tự không phải chữ số, và thay thể chúng ="" (chuỗi rỗng)
                System.out.println(numPart);
                newNum = Integer.parseInt(numPart) + 1;
                System.out.println("New number: " + newNum);
            }
        }
        /*
         ý tưởng là chọn id lớn nhất hiện có trong bảng, tách phần số, tăng lên 1, rồi ghép lại với prefix
         ví dụ: ITEM001 -> tách số 001 -> tăng lên 002 -> ghép lại thành ITEM002
        
         */
        // định dạng thành ITEM001, SS002, ...
        String formattedNum = String.format("%03d", newNum);
        System.out.println("Generated ID: " + prefix + formattedNum);
        return prefix + formattedNum;

    } catch (SQLException e) {
        System.out.println("xay ra loi roi");
        e.printStackTrace();
        return null;
    }
}


    public boolean insertItem(Item item) {
        String sql = "INSERT INTO Item (item_id, capacity, reserve_price) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Gán giá trị cho các cột trong bảng Item
            stmt.setString(1, item.getId());
            stmt.setDouble(2, item.getCapacity());
            stmt.setString(3, item.getReservePrice());

            int affectedRows = stmt.executeUpdate();

            // Nếu thêm thành công ít nhất 1 dòng, trả về true
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertAuctionSession(AuctionSession session) {
        String sql = "INSERT INTO auctionsession (id, item_id, is_active, duration) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, session.getId());
            stmt.setString(2, session.getItem().getId());
            stmt.setBoolean(3, session.isActive());
            stmt.setInt(4, session.getDuration());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0; // true nếu thêm thành công

        } catch (SQLException e) {
            System.out.println("Error inserting auction session:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Trả về Bid có bid_amount lớn nhất cho một session (theo session id).
     * Nếu không có bid trả về null.
     */
    public Bid getHighestBidForSession(String sessionId) {
        String sql = "SELECT bid_id, client_id, bid_amount, session_id FROM bid WHERE session_id = ? ORDER BY bid_amount DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Bid b = new Bid();
                try {
                    b.setId(rs.getString("bid_id"));
                } catch (Exception ignored) {}
                b.setClientId(rs.getString("client_id"));
                b.setValue(rs.getDouble("bid_amount"));
                b.setAuctionSessionId(rs.getString("session_id"));
                return b;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
   
    public boolean insertBid(Bid bid) {
        String sql = "INSERT INTO bid (bid_id, session_id,client_id, bid_amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, bid.getId());
            stmt.setString(2, bid.getAuctionSessionId());
            stmt.setString(3, bid.getClientId());
            stmt.setDouble(4, bid.getValue());
            int affectedRows = stmt.executeUpdate();
           
            return affectedRows > 0; // true nếu thêm thành công

        } catch (SQLException e) {
            System.out.println("Error inserting bid:");
            e.printStackTrace();
            return false;
        }
    }

    
    public List<AuctionSession> getActiveSessions() {
        List<AuctionSession> sessions = new ArrayList<>();
        String sql = "SELECT id, item_id, is_active, duration FROM auctionsession WHERE is_active = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String itemId = rs.getString("item_id");
                boolean isActive = rs.getBoolean("is_active");
                int duration = rs.getInt("duration");
                // create session with placeholder item (item will be fetched separately)
                AuctionSession session = new AuctionSession(null);
                session.setId(id);
                session.setActive(isActive);
                session.setDuration(duration);
                // set item if available
                Item item = getItemById(itemId);
                if (item != null) session.setItem(item);
                sessions.add(session);
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, e);
        }
        return sessions;
    }

   
    public Item getItemById(String itemId) {
        String sql = "SELECT item_id, capacity, reserve_price FROM item WHERE item_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Item item = new Item(rs.getDouble("capacity"), rs.getString("reserve_price"));
                    item.setId(rs.getString("item_id"));
                    return item;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

   
    public boolean hasBidsForSession(String sessionId) {
        String sql = "SELECT COUNT(*) AS cnt FROM bid WHERE session_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    return cnt > 0;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

   
    public List<Bid> getBidsBySessionId(String sessionId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT bid_id, session_id, client_id, bid_amount FROM bid WHERE session_id = ? ORDER BY bid_amount DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Bid b = new Bid();
                    b.setId(rs.getString("bid_id"));
                    b.setAuctionSessionId(rs.getString("session_id"));
                    b.setClientId(rs.getString("client_id"));
                    b.setValue(rs.getDouble("bid_amount"));
                    bids.add(b);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, e);
        }
        return bids;
    }

    /**
     * Thay đổi (cộng/trừ) currentBudget của node (client) trong database.
     * delta có thể âm để trừ, dương để cộng (refund).
     */
    public boolean adjustNodeBudget(String nodeId, double delta) {
        String sql = "UPDATE node SET currentBudget = currentBudget + ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, delta);
            stmt.setString(2, nodeId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error adjusting node budget for " + nodeId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Double getNodeBudget(String nodeId) {
        String sql = "SELECT currentBudget FROM node WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nodeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("currentBudget");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    
}