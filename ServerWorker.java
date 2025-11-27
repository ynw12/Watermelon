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
    private final DoneOrderService doneorderService;
    private final MenuService MenuService;

    private BufferedReader in;
    private PrintWriter out;

    // 손님용 세션
    private ClientSession clientSession;
    // 직원용 세션
    private StaffSession staffSession;

    public ServerWorker(Socket socket, OrderService orderService, DoneOrderService doneorderService, MenuService MenuService) throws IOException {
        this.socket = socket;
        this.orderService = orderService;
        this.doneorderService = doneorderService;
        this.MenuService = MenuService;

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

//                String[] parts = msg.split(" ");
//                String cmd = parts[0];

                //직원 접속
                if (msg.equals("STAFF_HELLO")) {
                    staffSession = new StaffSession(out);
                    out.println("STAFF_LOGIN");
                    System.out.println("[SERVER] Staff 세션 생성");
                    continue;
                }
                else if(msg.equals("CLIENT_HELLO")){
                	clientSession = new ClientSession(out);
                	out.println("Client_LOGIN");
                	System.out.println("[SERVER] Client 세션 생성");
                	MenuService.showAllMenus(out);
                    continue;
                }

              //관리자 명령 처리 -> 프로토콜 확인
                if (staffSession != null) {
                    if (msg.equals("STAFF_GET_DONE"))
                		doneorderService.showDoneOrder(staffSession);
                	else if (msg.startsWith("PICKUP"))
                		doneorderService.handleStaffPickup(msg);
                	else if (msg.equals("DISCONNECT")) {
                		System.out.println("[SERVER] STAFF DISCONNECT 요청");
                      	break;}
                	else {
                		out.println("ERROR_STAFF");
                    }
                    continue;
                } 

                //고객
                // 1) 주문 생성: NEW_ORDER 주문메뉴명 (클라 -> 서버 프로토콜 양식)
                if (msg.startsWith("NEW_ORDER")) {
                    orderService.handleNewOrder(msg, clientSession, out);
                }
                // 2) 상태 조회: GET_STATUS (클라 -> 서버 프로토콜 양식)
                else if (msg.equals("GET_STATUS")) {
                    orderService.handleGetStatus(clientSession);
                }

                /* 3) 앱 종료: DISCONNECT
                 * 클라 소켓, 서버 소켓 모두 종료를 시켜야 하는데
                 * 임시방편으로 DISCONNECT로 연결 종료
                 */
                else if (msg.equals("DISCONNECT")) {
                    System.out.println("[SERVER] 클라이언트 DISCONNECT 요청");
                    break; // while 루프 종료 → finally에서 소켓 정리
                }
                // 4) 그 외 명령
                else {
                    out.println("ERROR_Client");
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
