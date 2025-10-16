package Common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AuctionMessage implements Serializable {
    private String title; // loại message, ví dụ: "NEW_AUCTION", "BID", "AUCTION_RESULT"
    private Map<String, Object> params; // các tham số đi kèm

    public AuctionMessage(String title) {
        this.title = title;
        this.params = new HashMap<>();
    }

    public String getTitle() {return title;}
    public Object getParam(String key) {return params.get(key);}
    public Map<String, Object> getParams() {return params;}


    public void addParam(String key, Object value) {params.put(key, value);}


    @Override
    public String toString() {
        return "Common.AuctionMessage{" +
                "title='" + title + '\'' +
                ", params=" + params +
                '}';
    }
}
//Serializable là interface đánh dấu để cho phép object được chuyển thành byte stream.