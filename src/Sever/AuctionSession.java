package Sever;

import java.util.ArrayList;
import java.util.List;

public class AuctionSession {
    private static int count = 0;
    private  String id;
    private Item item;
    private List<Bid> bids = new ArrayList<>();// trong đây có ai thắng và giá thẳng luôn
//    private List<ClientHandler> participants = new ArrayList<>();
    private boolean isActive = true;
    private int duration;// khoảng thời gian diễn ra đấu giá

    public AuctionSession(Item item) {
        this.id = "Session" + (++count);
        this.item = item;
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public Item getItem() {return item;}

    public void setItem(Item item) {this.item = item;}

    public List<Bid> getBids() {return bids;}

    public void setBids(List<Bid> bids) {this.bids = bids;}



    public boolean isActive() {return isActive;}

    public void setActive(boolean active) {isActive = active;}

    public int getDuration() {return duration;}

    public void setDuration(int duration) {this.duration = duration;}
}