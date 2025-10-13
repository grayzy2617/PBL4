package Client;

import utility.DataLoader;
import utility.NodeManager;
import utility.Utility;
import Model.Node;
import Model.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utility.Utility.computeLatency;

public class AuctionClient {
    private Node selfNode;
    private NodeManager manager;

    public AuctionClient(NodeManager manager) {
        this.manager = manager;
        this.selfNode = manager.chooseRandomNode();
    }

    // filter the links related to this node
    public List<Link> getMyLinks() {
        List<Link> myLinks = new ArrayList<>();
        for (Link link : manager.getAllLinks()) {
            if (link.getTx().equals(selfNode.getId()) || link.getRx().equals(selfNode.getId())) {
                myLinks.add(link);
            }
        }
        return myLinks;
    }

    //compute SNR & Latency
    public void analyzeLinks() {
        List<Link> myLinks = getMyLinks();
        for (Link link : myLinks) {
            Node txNode = manager.findNodeById(link.getTx());
            Node rxNode = manager.findNodeById(link.getRx());

            double snr = Utility.computeSNR(
                    txNode.getPt(),
                    txNode.getGt(),
                    rxNode.getGr(),
                    link.getDistanceKm(),
                    link.getFrequencyMHz(),
                    rxNode.getT(),
                    link.getB_alloc()
            );
            double latency = Utility.computeLatency(link.getDistanceKm(), rxNode.getT());

            double snrNorm = Utility.normalizeSNR(snr);
            double latNorm = Utility.normalizeLatency(latency);
            double alpha = Utility.computeAlpha(snrNorm, latNorm);
            double value = Utility.computeValue(snr, latency);
            double Bid = Utility.calculateBid(
                    value,
                    n,// cần lấy từ server
                    selfNode.getCurrentBudget(),     // Ngân sách còn lại
                    selfNode.getBudget_max(),         // Ngân sách tối đa (B_max)
                    link.getPriority(),         // Delta Priority của Link
                    link.getBidMin()                // Giá sàn cần ấy từ server

            );

            System.out.printf(
                    "Link %s -> %s:\n  SNR = %.2f dB, Latency = %.3f ms\n  Norm(SNR)=%.3f, Norm(Lat)=%.3f, Alpha=%.3f\n  ==> Value = %.3f\n\n",
                    txNode.getId(), rxNode.getId(),
                    snr, latency * 1000,
                    snrNorm, latNorm, alpha, value
            );
        }
    }


}