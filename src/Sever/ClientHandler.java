package Sever;

import Common.AuctionMessage;

import Database.DatabaseManager;
import Client.Node;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                // Xử lý tin nhắn từ client
                if (msg.getTitle().equals("Connected")) {
                    String randomId = pickRandomNodeId();
                    System.out.println("Picked random node ID: " + randomId);
                    Node node = null;
                    if (randomId != null) {
                        // tìm node trong db
                        node = DatabaseManager.getInstance().getNodeById(randomId);
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
                        
                    } else if (msg.getTitle().equals("BID")) {
                        //                String bidStr = (String) msg.getParam("BID");
                        //                Bid bid = processBid(bidStr);
                        //                if (bid != null) {
                        //                    System.out.println("Processed bid from " + clientId + ": " + bid);
                        //                    // Gửi phản hồi lại client
                        //                    AuctionMessage response = new AuctionMessage("BID_RECEIVED");
                        //                    response.addParam("BID", bid);
                        //                    sendMessage(response);
                        //                } else {
                        //                    System.out.println("Invalid bid format from " + clientId + ": " + bidStr);
                        //                    AuctionMessage response = new AuctionMessage("BID_INVALID");
                        //                    sendMessage(response);
                        //                }
                    } else if (msg.getTitle().equals("Client's BidAverage")) {
                        double budget = (double) msg.getParam("BidAverage");
                        // add vào list bidAverage của server
                        // lấy id của ss ra để gọi ss đó và add vào list budget của ss
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

    // bidStr có dạng id,bid
    public Bid processBid(String bidStr) {

        if (bidStr == null || bidStr.isEmpty() || !bidStr.contains(",")) return null;
        String[] parts = bidStr.split(",");
        if (parts.length != 2) return null;
        String id = parts[0].trim();
        double bidAmount;
        try {
            bidAmount = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            System.out.println("err processBid");
            return null;
        }
        return new Bid(id, bidAmount);
    }

    public void sendMessage(AuctionMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending message to " + clientId);
            e.getStackTrace();
        }
    }

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
                if (colon < 0) break;
                int quoteStart = content.indexOf('"', colon);
                if (quoteStart < 0) break;
                int quoteEnd = content.indexOf('"', quoteStart + 1);
                if (quoteEnd < 0) break;
                String id = content.substring(quoteStart + 1, quoteEnd);
                ids.add(id);
                idx = quoteEnd + 1;
            }
            if (ids.isEmpty()) return null;
            Random rnd = new Random();
            return ids.get(rnd.nextInt(ids.size()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}