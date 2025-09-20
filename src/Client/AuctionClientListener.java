package Client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class AuctionClientListener implements Runnable {
    private Socket clientSocket;
    private InputStream input;
    private AuctionEventListener eventListener;
    public AuctionClientListener(Socket cs, AuctionClientUI listener) {
        this.clientSocket = cs;
        this.eventListener = listener;
        try {
            input = clientSocket.getInputStream();

        } catch (Exception e) {
            System.out.println("err input client");
        }
    }

    @Override
    public void run() {
        BufferedReader bf = new BufferedReader(new InputStreamReader(input));
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("AUCTION:")) {
                    if (eventListener != null) {
                        eventListener.onNewAuction(line);
                    }
                }
                //else if ....
            }
        } catch (Exception e) {
            System.out.println("err write sever side");
        }
    }
}


