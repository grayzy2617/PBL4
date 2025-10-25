package Interface;
import  Common.AuctionMessage;

public interface AuctionEventListener {
    void onNewAuction(AuctionMessage message);
    void onAuctionResult(AuctionMessage message);
}
 
// interface này dùng để lắng nghe các sự kiện đấu giá mới từ server và xử lý chúng trong client.
