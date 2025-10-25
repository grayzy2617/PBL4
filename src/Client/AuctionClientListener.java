package Client;

import Common.AuctionMessage;
import java.io.ObjectInputStream;
import java.net.Socket;


import Interface.*;
public class AuctionClientListener implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private AuctionEventListener eventListener;
    private OnDataReceivedListener dataReceivedListener;
    public AuctionClientListener(AuctionClient client, AuctionClientUI ui) {
        this.clientSocket = client.getSocket();
        this.eventListener = ui;
        this.dataReceivedListener = client;
    }

    @Override
    public void run() {
        try {
            this.input = new ObjectInputStream(clientSocket.getInputStream());
            while (!clientSocket.isClosed()) {
                AuctionMessage msg = (AuctionMessage) input.readObject();
                if (msg != null) {
                    if (msg.getTitle().equals("NEW_AUCTION")) {
                       System.out.println("Received NEW_AUCTION message from server.");
                        eventListener.onNewAuction(msg);
                    } else if (msg.getTitle().equals("Client's BidAverage")) {

                    } else if (msg.getTitle().equals("AUCTION_RESULT")) {
                        eventListener.onAuctionResult(msg);
                    }
                }
            }
            System.out.println("Client listener stopped.");
        } catch (Exception e) {
            System.out.println("err at run function of AuctionClientListener");
            e.printStackTrace();
        }
    }
}


