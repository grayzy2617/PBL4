package Client;

import java.net.Socket;
import java.util.Random;

public class AuctionClient {

    private String clientId;
    private double budget;
    private double acquiredResources;
    AuctionClientListener listener;
    private Strategy  strategy;

    private static final double BASE_BUDGET = 100.0;
    private static final String url = "localhost";
    private static final int port = 5002;

    public AuctionClient(int clientId) {
        this.acquiredResources=0;// khởi tạo tài nguyên đã mua
        String temp=  randomId();
        this.budget = allocateBudget(temp);//  cái budget này là do sever cấp
        this.clientId=  temp + clientId;
    }
    // Random chọn 1 trong 4 loại node
    private String randomId() {
        String[] types = {"SP", "A", "GR", "S"};
        Random rand = new Random();
        int index = rand.nextInt(types.length);
        return types[index];
    }
    // Cấp phát budget dựa vào loại node
    private double allocateBudget(String id) {
        switch (id) {
            case "SP": return BASE_BUDGET * 1.2;
            case "A":  return BASE_BUDGET * 1.0;
            case "GR": return BASE_BUDGET * 0.9;
            case "S":  return BASE_BUDGET * 0.8;
            default:   return BASE_BUDGET; // fallback
        }
    }
    public  void startConnSever ( AuctionClientUI ui) {
        try {
            Socket socket = new Socket(url, port);
            System.out.println("Connected to server at " + url + ":" + port);
            // Khởi tạo luồng lắng nghe từ server
            this.listener = new AuctionClientListener(socket, ui);
            new Thread(listener).start();


        } catch (Exception e) {
            System.out.println("err conn");
        }
    }


    public void joinAuction(String auctionId) {
        // gửi yêu cầu tham gia phiên đấu giá lên server
        

    }

    public void sendBid(double amount) {
        // gửi giá thầu lên server
    }

    public void receiveMessage(String msg) {
        System.out.println("From server: " + msg);
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getAcquiredResources() {
        return acquiredResources;
    }

    public void setAcquiredResources(double acquiredResources) {
        this.acquiredResources = acquiredResources;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }



    public String getClientId() {return clientId;}
    public double getBudget() {return budget;}




}
