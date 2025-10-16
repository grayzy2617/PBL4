// src/Sever/AuctionServer.java
package Sever;

import Common.AuctionMessage;
import Database.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {
    private static final int port = 5002;
    private static AuctionServer instance;
    private final double capacity = 100;
    private double totalCapacity;
    private double currentCapacity;
    private double revenue;

    List<ClientHandler> clients = new ArrayList<>();
    List<Double> bidAverages = new ArrayList<>();
    private AuctionServer() {
        totalCapacity = capacity;
        currentCapacity = totalCapacity;
        revenue = 0;
    }

    public static synchronized AuctionServer getInstance() {
        if (instance == null) {
            instance = new AuctionServer();
        }
        return instance;
    }

    public double getTotalCapa() {
        return this.totalCapacity;
    }

    public double getCurrentCapacity() {
        return this.currentCapacity;
    }

    public double getRevenue() {
        return this.revenue;
    }

    public double setCurrentCapacity(double budget) {
        return this.currentCapacity = budget;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startAuctionSession(Item item) {
        AuctionSession session = new AuctionSession(item);
        String sessionId = DatabaseManager.getInstance().insertAuctionSession(item.getId(), session.isActive(), session.getDuration());
        session.setId(sessionId);
        makeMsg_CrAuctionSession(item, session.getId());// gửi thêm id session vì để cho client biết đây là phiên đấu giá nào để client gửi lại khi hắn gửi bid
    }

    public void makeMsg_CrAuctionSession(Item item, String idSession) {
        AuctionMessage msg = new AuctionMessage("NEW_AUCTION");
        msg.addParam("ItemId", item.getId());
        msg.addParam("Capacity", item.getCapacity());
        msg.addParam("ReservePrice", item.getReservePrice());
        msg.addParam("idSession", idSession);
        this.broadcast(msg);
    }
    public void makeMsg_SendResultSession(String winnerId, double winningBid, double remainingCapacity) {
        AuctionMessage msg = new AuctionMessage("AUCTION_RESULT");
        msg.addParam("WinnerId", winnerId);
        msg.addParam("WinningBid", winningBid);
        msg.addParam("RemainingCapacity", remainingCapacity);
        this.broadcast(msg);
    }
    public  void makeMsg_RequestCLientSendBudget(){
        AuctionMessage msg = new AuctionMessage("REQUEST_BIDAVERAGE");
        this.broadcast(msg);
    }

    public void broadcast(AuctionMessage msg) {
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }


}

/*
 * làm sao với session đó sau đó sẽ tạo hàm sever. receiveBid(bid, sessionID)
 *  hàm addBIds vào session , hàm tìm ai win , hàm update doanh thu , hàm update tài nguyên hiện có
 * hàm gửi message đến các client, về việc ai thắng, giá bao nhiêu, còn lại tài nguyên bao nhiêu
 *  hàm update lại bảng trong giao diện
 * sau đó lặp lại tạo phiên đấu giá mới
 *
 * */






/*
 *
 * 1 là nhận biết được class đó nhiệm vụ chính là gì (rất quan trọng để biết 1 hàm xử lý nên đặt ở đâu, class nào )*
 * 2 là biết được class đó có những thuộc tính gì (biết được thuộc tính sẽ giúp ta biết được class đó có thể làm được gì)
 *
 *
 * */