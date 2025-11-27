package Server_v1;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
//여기가 일단은 클라 View 역할
public class TestStaff {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 50023);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        new Thread(() -> {
            try {
            	out.println("STAFF_HELLO");
                String serverMsg;
                while ((serverMsg = in.readLine()) != null) {
                    System.out.println("[서버 → 매니저] " + serverMsg);
                    
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        
        Scanner sc = new Scanner(System.in);

        System.out.println("문자 입력해서 서버로 보내세요:");
        
        while (true) {
            String msg = sc.nextLine(); // 내가 입력한 메시지
            out.println(msg);// 서버로 보냄
        }
    }
}