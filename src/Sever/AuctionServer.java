package Sever;

import Common.AuctionMessage;
import Database.DatabaseManager;

import com.google.gson.*;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionServer {
    private static final int port = 5003;
    private static AuctionServer instance;
    private final double capacity = 100;
    private double totalCapacity;
    private double currentCapacity;
    private double revenue;
    private AuctionResultListener ui;
    List<ClientHandler> clients = new ArrayList<>();
    List<Double> bidAverages = new ArrayList<>();
    // cached links loaded from Data/links.json for inference
    private final Map<String, LinkInfo> linksById = new ConcurrentHashMap<>();
    private final List<LinkInfo> linksList = new ArrayList<>();

    private AuctionServer() {
        totalCapacity = capacity;
        currentCapacity = totalCapacity;
        revenue = 0;
        loadLinksFromJson();
    }

    private static class LinkInfo {
        String id;
        String tx;
        String rx;
        LinkInfo(String id, String tx, String rx) { this.id = id; this.tx = tx; this.rx = rx; }
    }

    private void loadLinksFromJson() {
        try (FileReader r = new FileReader("src/Data/links.json")) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String id = o.get("id").getAsString();
                String tx = o.get("tx").getAsString();
                String rx = o.get("rx").getAsString();
                LinkInfo info = new LinkInfo(id, tx, rx);
                linksById.put(id, info);
                linksList.add(info);
            }
            System.out.println("Loaded " + linksList.size() + " links for inference.");
        } catch (Exception ex) {
            System.out.println("Could not load links.json from src/Data: " + ex.getMessage());
            // fallback: try classpath
            InputStream is = AuctionServer.class.getClassLoader().getResourceAsStream("Data/links.json");
            if (is == null) is = AuctionServer.class.getClassLoader().getResourceAsStream("links.json");
            if (is != null) {
                try (InputStreamReader ir = new InputStreamReader(is)) {
                    JsonArray arr = JsonParser.parseReader(ir).getAsJsonArray();
                    for (JsonElement el : arr) {
                        JsonObject o = el.getAsJsonObject();
                        String id = o.get("id").getAsString();
                        String tx = o.get("tx").getAsString();
                        String rx = o.get("rx").getAsString();
                        LinkInfo info = new LinkInfo(id, tx, rx);
                        linksById.put(id, info);
                        linksList.add(info);
                    }
                    System.out.println("Loaded " + linksList.size() + " links for inference (from classpath).");
                } catch (Exception ex2) { System.out.println("Failed to parse links from classpath: " + ex2.getMessage()); }
            } else {
                System.out.println("No links.json found on classpath either.");
            }
        }
    }

    public void setUi(AuctionResultListener ui) {
        this.ui = ui;
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
   public void setRevenue(double revenue) {
        this.revenue = revenue;
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
        } catch (java.net.BindException be) {
            System.out.println("Server failed to start: port " + port + " is already in use. " + be.getMessage());
            // do not rethrow; caller (UI) will be notified separately
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startAuctionSession(Item item) {
        AuctionSession session = new AuctionSession(item);
        String sessionId = DatabaseManager.getInstance().generateId("auctionsession");
        System.out.println("Generated Session ID: " + sessionId);
        session.setId(sessionId);
        if (!DatabaseManager.getInstance().insertAuctionSession(session)) {
            System.out.println("Failed to insert auction session into database.");
            return;
        }
        if (ui == null) {
            System.out.println("AuctionServer: ui is null");
        }
        else {
           System.out.println("Starting auction session with UI listener.");
         }
            // Start the auction session timer (will end after session.getDuration() seconds)
            // notify UI that auction started
            if (ui != null) ui.onAuctionStarted(session.getId(), session.getDuration());
            session.startAndScheduleEnd(ui);
        
        makeMsg_CrAuctionSession(item, session.getId());// gửi thêm id session vì để cho client biết đây là phiên đấu giá nào để client gửi lại khi hắn gửi bid
         
    }

    // Called when a client/node connects (or announces itself)
    public void notifyNodeConnected(String nodeId) {
        if (ui != null) ui.onNodeConnected(nodeId);
    }

    // Called when a bid message is received so UI can highlight the link
    public void notifyBidReceived(String nodeId, String linkId, String sessionId) {
        // infer linkId if missing by matching tx==nodeId or rx==nodeId
        String finalLink = linkId;
        if ((finalLink == null || finalLink.isEmpty()) && nodeId != null) {
            for (LinkInfo li : linksList) {
                if (nodeId.equals(li.tx) || nodeId.equals(li.rx)) {
                    finalLink = li.id;
                    break;
                }
            }
        }
        if (ui != null) ui.onBidForLink(nodeId, finalLink, sessionId);
    }

    public void makeMsg_CrAuctionSession(Item item, String idSession) {
        AuctionMessage msg = new AuctionMessage("NEW_AUCTION");
        msg.addParam("ItemId", item.getId());
        msg.addParam("Capacity", item.getCapacity());
        msg.addParam("ReservePrice", item.getReservePrice());
        msg.addParam("idSession", idSession);
        msg.addParam("participants",  clients.size());
        this.broadcast(msg);

    }

    public void makeMsg_SendResultSession(String winnerId, double winningBid, double remainingCapacity) {
        AuctionMessage msg = new AuctionMessage("AUCTION_RESULT");
        msg.addParam("WinnerId", winnerId);
        msg.addParam("WinningBid", winningBid);
        msg.addParam("RemainingCapacity", remainingCapacity);
        this.broadcast(msg);
    }
     
    public void broadcast(AuctionMessage msg) {
        System.out.println("Broadcasting message: " + msg.getTitle());
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }


}