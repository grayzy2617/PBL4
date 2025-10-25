package Client;

import Common.AuctionMessage;

import java.io.ObjectOutputStream;
import java.net.Socket;

import Interface.OnDataReceivedListener;
public class AuctionClient  implements OnDataReceivedListener {
    private String clientId;
    private Node node;
    private AuctionClientListener listener;
    private Socket socket;
    private static final String url = "localhost";
    private static final int port = 5003;

    public AuctionClient(int clientId) {
        this.clientId = "Client" + clientId;
        this.node = NodeManager.getInstance().chooseRandomNode();
        System.out.println(this.getNode().getBudget_max() + this.getNode().getId());
    }

    public void startConnSever(AuctionClientUI ui) //
    {
        try {
            this.socket = new Socket(url, port);
            System.out.println("Connected to server at " + url + ":" + port);
           
            this.listener = new AuctionClientListener(this, ui);
            new Thread(listener).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void joinAuction(String auctionId) {
        // gửi yêu cầu tham gia phiên đấu giá lên server
    }

    public void getNodeFromServer() {

        try {
            ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());

            AuctionMessage joinMessage = new AuctionMessage("Connected");
            joinMessage.addParam("ClientID", this.clientId);
            // send NodeID so server can map this client to a node and light it in UI
            if (this.node != null) joinMessage.addParam("NodeID", this.node.getId());

            out.writeObject(joinMessage);
            out.flush();

        } catch (Exception e) {
            System.out.println("err get node");
        }
    }

    public void sendBid(double amount, String linkId, String idSession) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());

            AuctionMessage bidMessage = new AuctionMessage("BID");
            bidMessage.addParam("bid", String.valueOf(amount));
            bidMessage.addParam("NodeID", this.node.getId());
            bidMessage.addParam("linkId", linkId);
            bidMessage.addParam("idSession",idSession);

            out.writeObject(bidMessage);
            out.flush();

        } catch (Exception e) {
            System.out.println("err send bid");
        }
    }
       @Override
       public void onDataReceived(Node node) // hàm này hiện tại ko cần vì khởi tạo node bên client luôn
     {
        this.node = node;
        System.out.println("Received node data: "+ node.getId());
    }
//     // filter the links related to this node
//     public List<Link> getMyLinks() {
//         List<Link> myLinks = new ArrayList<>();
//         for (Link link : NodeManager.getInstance().getAllLinks()) {
//             if (link.getTx().equals(this.node.getId()) || link.getRx().equals(this.node.getId())) {
//                 myLinks.add(link);
//             }
//         }
//         return myLinks;
//     }

//    // tính SNR, latency, value, bid cho các link của node này
//     public void analyzeLinks(int n,double reservePrice) {
//         List<Link> myLinks = getMyLinks();
//         for (Link link : myLinks) {
//             Node txNode = NodeManager.getInstance().findNodeById(link.getTx());
//             Node rxNode = NodeManager.getInstance().findNodeById(link.getRx());

//             double snr = Utility.computeSNR(
//                     txNode.getPt(),
//                     txNode.getGt(),
//                     rxNode.getGr(),
//                     link.getDistanceKm(),
//                     link.getFrequencyMHz(),
//                     rxNode.getT(),
//                     link.getB_alloc()
//             );
//             double latency = Utility.computeLatency(link.getDistanceKm(), rxNode.getT());

//             double snrNorm = Utility.normalizeSNR(snr);
//             double latNorm = Utility.normalizeLatency(latency);
//             double alpha = Utility.computeAlpha(snrNorm, latNorm);
//             double value = Utility.computeValue(snr, latency);
//             double Bid = Utility.calculateBid(
//                     value,
//                     n,// cần lấy từ server
//                     this.node.getCurrentBudget(),     // Ngân sách còn lại
//                     this.node.getBudget_max(),         // Ngân sách tối đa (B_max)
//                     link.getPriority(),         // Delta Priority của Link
//                    reservePrice            // Giá sàn cần ấy từ server

//             );

//             System.out.printf(
//                     "Link %s -> %s:\n  SNR = %.2f dB, Latency = %.3f ms\n  Norm(SNR)=%.3f, Norm(Lat)=%.3f, Alpha=%.3f\n  ==> Value = %.3f\n\n",
//                     txNode.getId(), rxNode.getId(),
//                     snr, latency * 1000,
//                     snrNorm, latNorm, alpha, value
//             );
//         }
//     }


    //-----------------Get,Set-----------------------//
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getClientId() {
        return clientId;
    }
 public Node getNode() {
        return node;
    }
    public void setNode(Node node) {
        this.node = node;
    }
    public Socket getSocket() {
        return socket;
    }
 
}
