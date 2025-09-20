package Sever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private static int count = 0;
    private Socket socket;
    private String clientId;
    private AuctionServer server;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, AuctionServer server) {
        this.socket = socket;
        this.clientId = "CLHdler" + (++count);
        this.server = server;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("err input,output ClientHander");
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {

                if (message.startsWith("BID:")) {
                    String bidAmountStr = message.substring(4);
                    Bid bid = processBid(bidAmountStr);
                    if (bid != null) {
                        // gửi 1 cái lời gọi đến acutionSever để hắn biết là à đã nhận được bid từ các client

                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientId);
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

    public void sendMessage(String msg) {
        PrintWriter pw = new PrintWriter(out, true);
        System.out.println(msg);
        pw.println(msg);//ở đây là sever , và đây là lệnh để sever gửi message đến các client khác
    }

}