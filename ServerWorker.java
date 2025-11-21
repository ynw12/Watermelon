package Server_v1;
import java.io.*;
import java.net.Socket;
//클라이언트별 처리 스레드-서버는 클라이언트 1명당 하나의 ServerWorker 스레드 생성,실행
//즉 각 클라이언트 1명씩 담당하는 컨트롤러 : 
public class ServerWorker extends Thread {
	
    private final Socket socket;//클라이언트와의 연결된 통신 통로
    private final ClientQueueLogic QueueLogic;
    private final ServerBroadcaster Broadcaster;
    private BufferedReader in;
    private PrintWriter out;//클라이언트로 메세지 보낼 출력 스트림 객체
    
    private ClientSession session;//손님 개별 정보
    
    public ServerWorker(Socket socket, ClientQueueLogic QueueLogic, ServerBroadcaster Broadcaster) throws IOException {
        this.socket = socket;
        this.QueueLogic = QueueLogic;
        this.Broadcaster = Broadcaster;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        
    }

    @Override
    public void run() {
    	try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[SERVER] 수신: " + msg);

                //NEW_ORDER 주문번호 입력받으면 실행 -> ORDER 주문번호 WAITING 대기손님수 출력해줌
                if (msg.startsWith("NEW_ORDER")) {
                    handleNewOrder(msg);
                }
                //GET_STATUS 입력 새로고침
                else if (msg.equals("GET_STATUS")) {
                    handleGetStatus();
                }
                // 예: 손님이 주문 완료 처리하고 나가기 "ORDER_READY"
                else if (msg.equals("ORDER_READY")) {
                    handleOrderReady();
                    break; // 소켓 종료
                }
                // 앱 종료 "DISCONNECT"
                /*else if (msg.equals("DISCONNECT")) {
                    handleDisconnect();
                    break;
                }*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 혹시 남아 있으면 대기열에서 제거
            if (session != null) {
                QueueLogic.subClient(session);
            }
            try { socket.close(); } catch (IOException e) {}
            System.out.println("[SERVER] 클라이언트 연결 종료");
        }
    }

    // NEW_ORDER orderId -> 주문번호, 예상 대기 인원 수 출력(주문번호 받는 방법 수정해야됨)
    //DB로 주문번호 연동되면 수정 필요:일단은 주문번호 수동으로 입력하는 메커니즘으로 함
    private void handleNewOrder(String msg) {
        String[] parts = msg.split(" ");
        int orderId = Integer.parseInt(parts[1]); // DB에서 받은 주문번호

        // 이 소켓에 대한 세션 생성
        session = new ClientSession(out, orderId);

        // 대기열에 추가
        QueueLogic.addClient(session);

        // 내 앞에 몇 명 받는 변수임
        int ahead = QueueLogic.getPeopleAhead(session);

        // 손님에게 응답 : ORDER 주문번호 WAITING 예상대기인원수
        Broadcaster.sendTo(session,
                "ORDER " + orderId + " WAITING " + ahead);
    }
    //GET_STATUS -> 실시간 대기 손님 수 출력
    private void handleGetStatus() {
        if (session == null) {
            out.println("STATUS NONE");
            return;
        }//예외처리 만약에 손님 주문 완료인데 getStatus 상황가정

        int orderId = session.getOrderId();
        int ahead   = QueueLogic.getPeopleAhead(session);

        Broadcaster.sendTo(session,
                "STATUS " + orderId + " WAITING " + ahead);
    }

    private void handleOrderReady() {
        if (session != null) {
            QueueLogic.subClient(session);
            Broadcaster.sendTo(session,
                    "DONE " + session.getOrderId());
        }
    }

  /*  private void handleDisconnect() {
        if (session != null) {
            queueLogic.subClient(session);
        }
    }*/
}
