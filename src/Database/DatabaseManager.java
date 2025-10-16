package Database;

import Client.Node;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    /**
     * Read the local nodes.json and return the JSON string for the node with the given id.
     * Returns null if not found or on error.
     */
   public Node getNodeById(String nodeId) {
        String sql = "SELECT * FROM Node WHERE node_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Node node = new Node(null,0,0,0,0,0,0,0,0);
                node.setId(rs.getString("node_id"));
                node.setPt(rs.getDouble("Pt"));
                node.setGt(rs.getDouble("Gt"));
                node.setGr(rs.getDouble("Gr"));
                node.setB_max(rs.getDouble("B_max"));
                node.setT(rs.getDouble("T"));
                node.setBudgetMax(rs.getDouble("budget_max"));
                node.setCurrentBudget(rs.getDouble("currentBudget"));
                node.setAcquiredResources(rs.getDouble("acquiredResources"));
                return node;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calculate the average bid amount (column `bid_amount`) from table `Bid`.
     * Returns null if there are no bids or on error.
     */
    public Double getAverageBidAmount() {
        String sql = "SELECT AVG(bid_amount) AS avg_bid FROM bid";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble("avg_bid");
                if (rs.wasNull()) {
                    return null;
                }
                return avg;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert an Item into the database and return the generated id as String.
     * Assumes a table named `Item` with columns (`id` AUTO_INCREMENT primary key, `capacity` DOUBLE,
     * `reserve_price` VARCHAR(...), `status` BOOLEAN).
     * Returns the generated id as String, or null on error.
     */
    public String insertItem(double capacity, String reservePrice) {
        String sql = "INSERT INTO Item (capacity, reserve_price) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, capacity);
            stmt.setString(2, reservePrice);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return String.valueOf(id);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Insert an AuctionSession row into the database and return the generated id as String.
     * Assumes a table named `auctionsession` with columns (`id` AUTO_INCREMENT primary key,
     * `item_id` (FK), `is_active` BOOLEAN, `duration` INT).
     * Returns generated id as String or null on error.
     */
    public String insertAuctionSession(String itemId, boolean isActive, int duration) {
        String sql = "INSERT INTO auctionsession (item_id, is_active, duration) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, itemId);
            stmt.setBoolean(2, isActive);
            stmt.setInt(3, duration);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return String.valueOf(id);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
