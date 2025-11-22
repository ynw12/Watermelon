package Server_v1;

import java.io.PrintWriter;

public class OrderService {

    private final OrderDAOimpl orderDAO;
    private final ClientQueueLogic queueLogic;
    private final ServerBroadcaster broadcaster;
    private final Order_Ready orderReady;

    public OrderService(OrderDAOimpl orderDAO, ClientQueueLogic queueLogic, ServerBroadcaster broadcaster, Order_Ready orderReady) {
        this.orderDAO = orderDAO;
        this.queueLogic = queueLogic;
        this.broadcaster = broadcaster;
        this.orderReady = orderReady;
    }

    public ClientSession handleNewOrder(String msg, ClientSession session, PrintWriter out) {
        //1. DTO 생성 후 DB에 손님 추가
        OrderDTO dto = new OrderDTO();
        orderDAO.CreateOrder(dto); 
        //2. 손님 주문번호생성, 주문 메뉴 넣기
        if (session == null) {
    		session = new ClientSession(out);
    	}
        session.setNo(dto.getNo()); session.setcafeordername(dto.getName());
        //3. 손님 대기열에 추가
        queueLogic.addClient(session);
        //4. 대기 손님 수 계산
        int ahead = queueLogic.getPeopleAhead(session);

        //5. 손님 콘솔에 출력 -> ORDER 1101 WAITING 1 이런식
        broadcaster.sendTo(session,
                "ORDER " + session.getno() + " WAITING " + ahead);

        //6. 메뉴 제조 완료 (1분 설정) -> 손님 대기열에서 제거 후 ORDER_READY 출력
        orderReady.scheduleOrderReady(session, queueLogic, broadcaster);

        return session;
    }
    //UI에서 GET_STATUS 요청받을 때 사용 -> STATUS 1101 WAITING 1 
    public void handleGetStatus(ClientSession session) {
        int orderId = session.getno();
        int ahead   = queueLogic.getPeopleAhead(session);

        broadcaster.sendTo(session,
                "STATUS " + orderId + " WAITING " + ahead);
    }
}