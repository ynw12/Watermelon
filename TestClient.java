package Server_v1;
//이건 제가 실시간 대기열 연동이랑 소켓연결 확인하려고 지피티한테 만들어달라한거에요 그래서 저희 플젝에 넣는 건 아닙니다
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 50023);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Scanner sc = new Scanner(System.in);

        System.out.println("문자 입력해서 서버로 보내세요:");

        while (true) {
            String msg = sc.nextLine(); // 내가 입력한 메시지
            out.println(msg);           // 서버로 보냄

            String serverMsg = in.readLine(); // 서버 응답 받기
            System.out.println("서버 응답: " + serverMsg);
        }
    }
}