package Sever;



public class AuctionSession {
    private  String id;
    private Item item;
    private int isActive = 1;// 1 là đang diễn ra, 0 là kết thúc
    private int duration;// khoảng thời gian diễn ra đấu giá
    private Thread timerThread;
    private AuctionResultListener resultListener=null;
    public AuctionSession(Item item) {
        this.item = item;
        this.duration = 40; // mặc định 60s
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public Item getItem() {return item;}

    public void setItem(Item item) {this.item = item;}

  

    public boolean isActive() {return isActive == 1;}

    public void setActive(boolean active) {isActive = active ? 1 : 0;}

    public int getDuration() {return duration;}

    public void setDuration(int duration) {this.duration = duration;}

    /**
     * Bắt đầu đếm ngược phiên đấu giá và khi kết thúc sẽ gọi DB để lấy bid cao nhất
     * Sau khi có kết quả sẽ gọi AuctionServer để broadcast kết quả
     */
    public void startAndScheduleEnd(AuctionResultListener resultListener) {

        this.resultListener = resultListener;
        // ensure only one timer
        if (timerThread != null && timerThread.isAlive()) return;
        timerThread = new Thread(() -> {
            try {
                Thread.sleep(this.duration * 1000L);
            } catch (InterruptedException e) {
                // nếu bị interrupt thì coi như cancel
                return;
            }
            this.isActive = 0; // kết thúc phiên đấu giá
            Database.DatabaseManager.getInstance().updateAuctionSessionStatus(this.id, false);
            // lấy bid cao nhất từ DB
            Bid winner = Database.DatabaseManager.getInstance().getHighestBidForSession(this.id);
            if (winner != null) {

                AuctionServer.getInstance()
                        .setCurrentCapacity(AuctionServer.getInstance().getCurrentCapacity() - this.item.getCapacity());
                AuctionServer.getInstance().setRevenue(AuctionServer.getInstance().getRevenue() + winner.getValue());
                // thông báo kết quả đến UI
                if (this.resultListener != null) {
                    this.resultListener.Update();
                    // notify UI of auction end
                    this.resultListener.onAuctionEnded(winner.getClientId(), winner.getValue());
                     
                }
                else {
                    System.out.println("AuctionSession: resultListener is null");
                }
                double winningBid = winner.getValue();
                String winnerId = winner.getClientId();
                double remainingCapacity = AuctionServer.getInstance().getCurrentCapacity();
                
                // server xử lý broadcast kết quả
                AuctionServer.getInstance().makeMsg_SendResultSession(winnerId, winningBid, remainingCapacity);
            } else {
                // không có người thằng, broadcast null/zero
                if (this.resultListener != null) {
                    this.resultListener.onAuctionEnded(null, 0.0);
                }
                AuctionServer.getInstance().makeMsg_SendResultSession(null, 0.0, AuctionServer.getInstance().getCurrentCapacity());
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }


}