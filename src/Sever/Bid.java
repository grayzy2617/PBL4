package Sever;

public class Bid {
    private String clientId;
    private double value;
    // thuộc session nào


    public Bid(String clientId, double value) {
        this.clientId = clientId;
        this.value = value;
    }

    public String getClientId() { return clientId; }
    public double getValue() { return value; }
    public void setClientId(String clientId) {this.clientId = clientId;}
    public void setValue(double value) {this.value = value;}
}

