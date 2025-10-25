package Sever;

import Common.AuctionMessage;
import Database.DatabaseManager;

import java.io.*;
import java.net.Socket;

class ClientHandler implements Runnable {
    private static int count = 0;
    private Socket socket;
    private String clientId;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, AuctionServer server) {
        this.socket = socket;
        this.clientId = "CLHdler" + (++count);

    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            while (!socket.isClosed()) {
                AuctionMessage msg = (AuctionMessage) in.readObject();
                System.out.println("Received message from " + clientId + ": " + msg.getTitle());
                if (msg.getTitle().equals("BID")) {
                    //add bid to database
                    Bid bid = new Bid();
                    bid.setId(DatabaseManager.getInstance().generateId("bid"));
                    bid.setAuctionSessionId(String.valueOf(msg.getParam("idSession")));
                    bid.setClientId(String.valueOf(msg.getParam("NodeID")));
                    bid.setValue(Double.parseDouble(String.valueOf(msg.getParam("bid"))));
                    if (!DatabaseManager.getInstance().insertBid(bid)) {
                        System.out.println("Failed to insert bid into database");
                    }
                    // if linkId provided, notify server/UI to highlight
                    Object linkIdObj = msg.getParam("linkId");
                    String linkId = linkIdObj != null ? String.valueOf(linkIdObj) : null;
                    AuctionServer.getInstance().notifyBidReceived(bid.getClientId(), linkId, bid.getAuctionSessionId());
                }

                if (msg.getTitle().equals("Connected")) {
                    Object nodeIdObj = msg.getParam("NodeID");
                    if (nodeIdObj != null) {
                        String nodeId = String.valueOf(nodeIdObj);
                        AuctionServer.getInstance().notifyNodeConnected(nodeId);
                    }
                }
                
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientId);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket for " + clientId);
            }
        }
    }

    public void sendMessage(AuctionMessage msg) {
        try {
            out.writeObject(msg);
            System.out.println("Sent message to " + clientId + ": " + msg.getTitle());
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending message to " + clientId);
            e.getStackTrace();
        }
    }


}








/*



    // Đọc file nodes.json và trả về 1 id ngẫu nhiên, hoặc null nếu lỗi
    private String pickRandomNodeId() {
        try {
            String path = Paths.get("src", "Data", "nodes.json").toString();
            String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            // đơn giản parse tìm tất cả id
            List<String> ids = new ArrayList<>();
            int idx = 0;
            while ((idx = content.indexOf("\"id\"", idx)) >= 0) {
                int colon = content.indexOf(':', idx);
                if (colon < 0)
                    break;
                int quoteStart = content.indexOf('"', colon);
                if (quoteStart < 0)
                    break;
                int quoteEnd = content.indexOf('"', quoteStart + 1);
                if (quoteEnd < 0)
                    break;
                String id = content.substring(quoteStart + 1, quoteEnd);
                ids.add(id);
                idx = quoteEnd + 1;
            }
            if (ids.isEmpty())
                return null;
            Random rnd = new Random();
            return ids.get(rnd.nextInt(ids.size()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    if (msg.getTitle().equals("Connected")) {
                    String randomId = pickRandomNodeId();
                    System.out.println("Picked random node ID: " + randomId);
                    Node node = null;
                    if (randomId != null) {
                        // tìm node trong db
                        // node = DatabaseManager.getInstance().getNodeById(randomId);
                        if (node == null) {
                            System.out.println("Node not found: " + randomId);
                        }

                        AuctionMessage response = new AuctionMessage("Connected_ACK");

                        response.addParam("NodeID", node.getId());
                        response.addParam("Pt", node.getPt());
                        response.addParam("Gt", node.getGt());
                        response.addParam("Gr", node.getGr());
                        response.addParam("B_max", node.getB_max());
                        response.addParam("T", node.getT());
                        response.addParam("Budget_max", node.getBudget_max());
                        response.addParam("CurrentBudget", node.getCurrentBudget());
                        response.addParam("AcquiredResources", node.getAcquiredResources());
                        sendMessage(response);
                        
                    }
                }
 */