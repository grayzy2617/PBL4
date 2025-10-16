package Sever;

public class Item// 1 món đồ cho 1 lần đấu giá
{
    private String id;
    private double capacity;// tài nguyên cần đấu giá
    private String reservePrice; // giá khởi điểm


    public Item( double capacity, String reservePrice) {
        this.capacity = capacity;
        this.reservePrice = reservePrice;
    }

    public void setId(String id) { this.id = id; }

    public String getId() { return id; }
    public double getCapacity() { return capacity; }
    public String getReservePrice() { return reservePrice; }
}
