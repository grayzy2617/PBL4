package Sever;

public class Item// 1 món đồ cho 1 lần đấu giá
{
    private static  int count=0;
    private String id;
    private double capacity;// tài nguyên cần đấu giá
    private String reservePrice; // giá khởi điểm
    private Boolean status;// false chưa đấu giá, true đã đấu giá
// thuộc session nào


    public Item( double capacity, String reservePrice, boolean status) {
        this.id = "Item"+(++count);
//        this.name = name;
        this.capacity = capacity;
        this.reservePrice = reservePrice;
        this.status=status;
    }

    public String getId() { return id; }
    public double getCapacity() { return capacity; }
    public String getReservePrice() { return reservePrice; }
    public  boolean getStatus(){return  status;}
}
