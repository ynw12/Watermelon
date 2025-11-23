package Server_v1;

import java.io.PrintWriter;

public class ClientSession {
    private final PrintWriter out;   // 이 손님에게 메시지 보낼 통로
    private int no;    //주문번호
    private String name;// 주문한 메뉴명
    //기존 Client -> ClientSession 로 변경
    public ClientSession(PrintWriter out) {
        this.out = out;
         }

    public PrintWriter getOut() { 
    	return out; 
    }
    public void setNo(int no) {
    	this.no = no;
    }
    public int getNo() { 
    	return no; 
    }
    //주문메뉴명 메소드 변경
    public void setName(String name) {
    	this.name = name;
    }
    public String getName() {
    	return name;
    }
   

}
