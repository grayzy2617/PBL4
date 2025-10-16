package Sever;

import java.util.ArrayList;
import java.util.List;

public class AuctionSession {
    private  String id;
    private Item item;
    private List<Bid> bids = new ArrayList<>();// trong đây có ai thắng và giá thẳng luôn
    private int isActive = 1;// 1 là đang diễn ra, 0 là kết thúc
    private int duration;// khoảng thời gian diễn ra đấu giá

    public AuctionSession(Item item) {
        this.item = item;
        this.duration = 60; // mặc định 60s
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public Item getItem() {return item;}

    public void setItem(Item item) {this.item = item;}

    public List<Bid> getBids() {return bids;}

    public void setBids(List<Bid> bids) {this.bids = bids;}


    public boolean isActive() {return isActive == 1;}

    public void setActive(boolean active) {isActive = active ? 1 : 0;}

    public int getDuration() {return duration;}

    public void setDuration(int duration) {this.duration = duration;}


}