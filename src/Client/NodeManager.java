package Client;

import Client.Node;
import Database.DatabaseManager;
import Client.Link;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class NodeManager {
    private List<Node> allNodes;
    private final List<Link> allLinks;
      private static NodeManager instance;

      public NodeManager() {
          // load data at once
          this.allNodes = DataLoader.loadNodes("src/Data/nodes.json");
          this.allLinks = DataLoader.loadLinks("src/Data/links.json");
          if (allNodes != null) {
              for (Node node : allNodes) {
                  node.initializeBudget();
              }
          }
      }
        public static synchronized NodeManager getInstance() {
            if (instance == null) {
                instance = new NodeManager();
            }
            return instance;
        }

    public Node chooseRandomNode() {

        if (allNodes.isEmpty()) {
            System.err.println("Error: The list is empty!!!");
            return null;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(allNodes.size());
        return allNodes.get(randomIndex);
    }

    public Node findNodeById(String id) {
        for (Node n : allNodes) {
            if (n.getId().equals(id))
                return n;
        }
        return null;
    }

    public List<Link> getLinks(String nodeId) {
        List<Link> links = new ArrayList<>();
        for (Link link : allLinks) {
            if (link.getIdNode().equals(nodeId)) {
                links.add(link);
            }
        }
        return links;
    }
 
    public List<Link> getAllLinks() {
        return allLinks;
    }
}


