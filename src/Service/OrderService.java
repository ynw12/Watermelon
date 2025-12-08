package Service;

import java.io.PrintWriter;
import java.util.List;

import DB.OrderDAOimpl;
import DB.OrderDTO;
import Logic.ClientQueueLogic;
import Logic.Order_Ready;
import Logic.ServerBroadcaster;
import Session.ClientSession;

public class OrderService {

    private final OrderDAOimpl orderDAO;
    private final ClientQueueLogic queueLogic;
    private final ServerBroadcaster broadcaster;
    private final Order_Ready orderReady;
    
    public OrderService(OrderDAOimpl orderDAO,
                        ClientQueueLogic queueLogic,
                        ServerBroadcaster broadcaster,
                        Order_Ready orderReady) {
    	this.orderDAO = orderDAO;
        this.queueLogic = queueLogic;
        this.broadcaster = broadcaster;
        this.orderReady = orderReady;
    }
//리펙토링 코드 (클래스 나누는 거보다 지금 기간으로는 복잡한 부분만 아래에 다른 메소드로 빼서 여기에 부르는 방식으로 수정했어요
//그래서 이 수정으로 다른 코드에 영향은 없게 했습니다
    public void handleNewOrder(String msg, ClientSession session, PrintWriter out) {

        // 1. 메시지 파싱 (형식 검사 포함)
        String menuName = extractMenuName(msg, session);
        if (menuName == null) {
            System.out.println("MENU_ERROR");
            return;
        }

        // 2. 주문 생성 -> 주문 정보 DB 저장 
        OrderDTO order = createWaitingOrder(menuName);

        // 3. 세션에 주문 정보 반영
        updateClientSessionFromOrder(session, order);

        // 4. 대기열에 추가
        queueLogic.addClient(session);

        // 5. 앞 사람 수 계산
        int ahead = queueLogic.getPeopleAhead(session);

        // 6. 손님에게 주문 결과 전송
        sendOrderCreatedMessage(session, order, ahead);

        // 7. 일정 시간 후 주문 완료 처리 스케줄링
        orderReady.scheduleOrderReady(session, queueLogic, broadcaster, orderDAO);
    }

    private String extractMenuName(String msg, ClientSession session) {
        String[] parts = msg.split(" ");

        if (parts.length < 2) {
            broadcaster.sendTo(session, "ERROR");
            return null;
        }
        return parts[1];
    }

    private OrderDTO createWaitingOrder(String menuName) {
        OrderDTO dto = new OrderDTO();
        dto.setName(menuName);
        dto.setStatus("WAITING");

        orderDAO.CreateOrder(dto); 

        return dto;
    }

   
    private void updateClientSessionFromOrder(ClientSession session, OrderDTO order) {
        session.setNo(order.getNo());
        session.setName(order.getName());
        session.setStatus(order.getStatus());
    }

    private void sendOrderCreatedMessage(ClientSession session, OrderDTO order, int ahead) {
        int orderId = order.getNo();
        String status = order.getStatus();

        String message = "ORDER " + orderId + " " + status + " " + ahead;
        broadcaster.sendTo(session, message);
    }

    // UI에서 GET_STATUS 요청받을 때 + 앞 손님 빠질 때 사용
    // 서버 -> 클라 (UI에서 프로토콜 파싱 필요)
    // 서버 : STATUS 1101 WAITING 1
    // (프로토콜 양식: STATUS 주문번호 WAITING 대기손님수)
    public void handleGetStatus(ClientSession session) {
        int orderId = session.getNo();
        int ahead   = queueLogic.getPeopleAhead(session);
        String status = session.getStatus();

        String message = "STATUS " + orderId + " " + status + " " + ahead;
        broadcaster.sendTo(session, message);
    }

    // ordermanagement 테이블 전체 출력
 	public void showAllOrders(PrintWriter out) {
 		List<OrderDTO> orderList = orderDAO.getAllOrders();

 		out.println("======주문내역======");
 		for(OrderDTO order : orderList) {
     		out.printf("번호 : "+order.getNo()+"+"+order.getName()+"|"+order.getStatus()+"\n");
 		}
 		// out.println("order1");
 	}
    
}





