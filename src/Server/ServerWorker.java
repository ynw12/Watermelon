package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Command.CLIENT_HELLO;
import Command.Client_DISCONNECT;
import Command.GET_STATUS;
import Command.NEW_ORDER;
import Command.PICKUP;
import Command.STAFF_GET_DONE;
import Command.STAFF_HELLO;
import Command.Staff_DISCONNECT;
import Service.DoneOrderService;
import Service.OrderService;
import Service.MenuService;
import Session.ClientSession;
import Session.StaffSession;

// 클라이언트 1명당 1개 생성되는 컨트롤러 역할 스레드
public class ServerWorker extends Thread {

    private final Socket socket;

    private final OrderService orderService;
    private final DoneOrderService doneorderService;
    private final Service.MenuService MenuService;

    private BufferedReader in;
    private PrintWriter out;

    private ClientSession clientSession;
    private StaffSession staffSession;

    public ServerWorker(Socket socket, OrderService orderService,
                        DoneOrderService doneorderService, MenuService MenuService) throws IOException {
        this.socket = socket;
        this.orderService = orderService;
        this.doneorderService = doneorderService;
        this.MenuService = MenuService;

        this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    // ======= Getter / Setter들 (Command들이 사용) =======
    public PrintWriter getOut() {
        return out;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public DoneOrderService getDoneorderService() {
        return doneorderService;
    }

    public MenuService getMenuService() {
        return MenuService;
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    public void setClientSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    public StaffSession getStaffSession() {
        return staffSession;
    }

    public void setStaffSession(StaffSession staffSession) {
        this.staffSession = staffSession;
    }

    @Override
    public void run() {
        System.out.println("[SERVER] : 연결 클라이언트 ip:port " + socket.getRemoteSocketAddress());

        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[SERVER] 수신: " + msg);

                // 1) 직원 / 손님 접속 HELLO 처리
                if (msg.equals("STAFF_HELLO")) {
                    new STAFF_HELLO().execute(msg, this);
                    continue;
                } else if (msg.equals("CLIENT_HELLO")) {
                    new CLIENT_HELLO().execute(msg, this);
                    continue;
                }

                // 2) 직원 세션이 있는 경우: STAFF 명령 처리
                if (staffSession != null) {
                    if (msg.equals("STAFF_GET_DONE")) {
                        new STAFF_GET_DONE().execute(msg, this);
                    } else if (msg.startsWith("PICKUP")) {
                        new PICKUP().execute(msg, this);
                    } else if (msg.equals("DISCONNECT")) {
                        new Staff_DISCONNECT().execute(msg, this);
                        break;  // while 루프 종료
                    } else {
                        out.println("ERROR");
                    }
                    continue;
                }

                // 3) 그 외: 고객 명령 처리
                if (msg.startsWith("NEW_ORDER")) {
                    new NEW_ORDER().execute(msg, this);
                }
                else if (msg.equals("GET_STATUS")) {
                    new GET_STATUS().execute(msg, this);
                }
                else if (msg.equals("DISCONNECT")) {
                    new Client_DISCONNECT().execute(msg, this);
                    break; // while 루프 종료 → finally에서 소켓 정리
                }
                else {
                	out.println("ERROR");
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