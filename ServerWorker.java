package Server_v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// 클라이언트 1명당 1개 생성되는 컨트롤러 역할 스레드
public class ServerWorker extends Thread {

    private final Socket socket;
    private final OrderService orderService;

    private BufferedReader in;
    private PrintWriter out;

    private ClientSession session;

    public ServerWorker(Socket socket, OrderService orderService) throws IOException {
        this.socket = socket;
        this.orderService = orderService;

        this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        System.out.println("[SERVER] : 연결 클라이언트 ip:port " + socket.getRemoteSocketAddress());

        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[SERVER] 수신: " + msg);

                // 1) 주문 생성: NEW_ORDER <메뉴이름>
                if (msg.startsWith("NEW_ORDER")) {
                    session = orderService.handleNewOrder(msg, session, out);
                }
                // 2) 상태 조회: GET_STATUS
                else if (msg.equals("GET_STATUS")) {
                    orderService.handleGetStatus(session);
                }
                /* 3) 앱 종료: DISCONNECT (얘는 왜 있냐면 클라이언트 소켓 종료때문에 만들어뒀어요
                 * 클라 소켓, 서버 소켓 모두 종료를 시켜야 하는데 이부분을 어떻게 해야할지 모르겠어서 
                 * 임시방편으로 지피티 도움 받아서 만들었습니다 그래서 UI에서 추가할 부분은 
                 * DISCONNECT 요청할 버튼만 있으면 될 것 같은데 저도 공부 더 해보고 어떤 거 더 추가해야하는지 
                 * 알아볼게요 일단은 발표할 때 여기 구현은 제외하고 발표하는게 좋을 것 같아요
                 */
                else if (msg.equals("DISCONNECT")) {
                    System.out.println("[SERVER] 클라이언트 DISCONNECT 요청");
                    break; // while 루프 종료 → finally에서 소켓 정리
                }
                // 4) 그 외 명령
                else {
                    out.println("ERROR UNKNOWN_COMMAND");
                }
            }
        } catch (IOException e) {
            System.out.println("[SERVER] 통신 중 오류 발생");
            e.printStackTrace();
        } finally {
            // 연결 종료 시 자원 정리
            try {
                socket.close();
            } catch (IOException e) {
                // 무시 가능
            }
            System.out.println("[SERVER] 클라이언트 연결 종료: " + socket.getRemoteSocketAddress());
        }
    }
}