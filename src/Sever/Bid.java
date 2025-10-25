package Sever;

public class Bid {
    private String id;
    private String clientId;
    private double value;
  private String auctionSessionId;

  public Bid(String clientId, double value, String auctionSessionId) {
      this.clientId = clientId;
      this.value = value;
      this.auctionSessionId = auctionSessionId;

  }

  public Bid() {
      this.id = "";
      this.clientId = "";
      this.value = 0.0;
      this.auctionSessionId = "";
        
    
}

  public String getId() {
      return id;
  }
    public void setId(String id) {
        this.id = id;
    }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientId() { return clientId; }
    public double getValue() { return value; }

    public String getAuctionSessionId() {
        return auctionSessionId;
    }
    public void setAuctionSessionId(String auctionSessionId) {
        this.auctionSessionId = auctionSessionId;
    }

    public void setValue(double value) {
        this.value = value;
    }

         

}

