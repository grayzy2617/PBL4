package Sever;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {
    private static final int port = 5002;

    private double totalCapacity;
    private double currentCapacity;
    private double revenue;

    List<ClientHandler> clients = new ArrayList<>();
    List<AuctionSession> sessions = new ArrayList<>();

    public AuctionServer(double capacity) {
        totalCapacity = capacity;
        currentCapacity = totalCapacity;
        revenue = 0; //đầu vào cho doanh thu =0
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
        return this.currentCapacity =budget;
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
        sessions.add(session);
        this.broadcast("AUCTION: Auction started: " + item.getId() +
                " [Capacity: " + item.getCapacity() + " Mbps, " +
                "Reserve Price: " + item.getReservePrice() + "]");
    }

    public List<AuctionSession> getSessions() {
        return sessions;
    }

    public void broadcast(String msg) {
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
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
}









/*
*
* 1 là nhận biết được class đó nhiệm vụ chính là gì (rất quan trọng để biết 1 hàm xử lý nên đặt ở đâu, class nào )*
* 2 là biết được class đó có những thuộc tính gì (biết được thuộc tính sẽ giúp ta biết được class đó có thể làm được gì)
*
*
* */