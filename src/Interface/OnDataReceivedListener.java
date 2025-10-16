package Interface;

import Client.Node;

public interface OnDataReceivedListener {
    void onDataReceived(Node node);// interface này định nghĩa một phương thức để nhận NOde từ listener chuyển sang Client

}
// interface này dùng để nhận gửi data từ listener sang client