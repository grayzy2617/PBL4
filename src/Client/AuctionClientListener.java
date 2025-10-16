package Client;

import Common.AuctionMessage;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

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
                        JOptionPane.showMessageDialog(null, "New auction started: " + msg.getParam("AuctionID"));
            
                        eventListener.onNewAuction(msg);
                    } else if (msg.getTitle().equals("Client's BidAverage")) {

                    } else if (msg.getTitle().equals("AUCTION_RESULT")) {
                    } else if (msg.getTitle().equals("Connected_ACK")) {
                        System.out.println("Received Connected_ACK message from server.");
                        Node node = new Node(
                                (String) msg.getParam("NodeID"),
                                (double) msg.getParam("Pt"),
                                (double) msg.getParam("Gt"),
                                (double) msg.getParam("Gr"),
                                (double) msg.getParam("B_max"),
                                (int) msg.getParam("T"),
                                (double) msg.getParam("Budget_max"),
                                (double) msg.getParam("CurrentBudget"),
                                (double) msg.getParam("AcquiredResources"));
                        dataReceivedListener.onDataReceived(node);
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


