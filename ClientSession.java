package Server_v1;

import java.io.PrintWriter;

public class ClientSession {
    private final PrintWriter out;   // 이 손님에게 메시지 보낼 통로
    private final int orderId;       // 주문번호
    //기존 Clinet -> ClientSession 로 변경
    public ClientSession(PrintWriter out, int orderId) {
        this.out = out;
        this.orderId = orderId;
    }

    public PrintWriter getOut() { return out; }
    public int getOrderId() { return orderId; }
}