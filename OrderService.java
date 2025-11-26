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

    // [new method] : 각 클라이언트(손님)의 장바구니 가져오기, 출력(UI/콘솔에 보여줄 수 있도록) 메소드 새로 만들기 
    
    public ClientSession handleNewOrder(String msg, ClientSession session, PrintWriter out) {
        if (session == null) {
    		session = new ClientSession(out);
    	}
        //2.클라->서버 프로토콜 파싱 -> 주문내역 db에 저장
        //클라 : NEW_ORDER 아메리카노 (프로토콜양식: NEW_ORDER 주문메뉴명) -> NEW_ORDER는 파싱 필요 없이 서버가 인식 ㄱㄴ하게 했음
        String[] parts = msg.split(" ");
        if (parts.length<2) {
        	broadcaster.sendTo(session,"Error");
        	return session;
        }
        
        String menuName = parts[1];
        
        OrderDTO dto = new OrderDTO();
        dto.setName(menuName);
        dto.setStatus("WAITING");
        orderDAO.CreateOrder(dto); 
        session.setNo(dto.getNo()); session.setName(dto.getName()); session.getStatus();
        //3. 손님 대기열에 추가
        queueLogic.addClient(session);
        //4. 대기 손님 수 계산
        int ahead = queueLogic.getPeopleAhead(session);

        //5. 손님 콘솔에 출력 -> ORDER 1101 WAITING 1 이런식
        int OrderID = dto.getNo();
        String Status = dto.getStatus();
        broadcaster.sendTo(session,
                "ORDER " + OrderID + " " + Status + " " +ahead);

        //6. 메뉴 제조 완료 (1분 설정, 테스트하다가 변경해도됨) -> 손님 대기열에서 제거 후 주문 완료 알려줌
        orderReady.scheduleOrderReady(session, queueLogic, broadcaster, orderDAO);

        return session;
    }
    //UI에서 GET_STATUS 요청받을 때 + 앞 손님 빠질 때 사용
    //서버 -> 클라 (UI에서 프로토콜 파싱 필요)
    //서버 : STATUS 1101 WAITING 1 (프로토콜 양식: STATUS 주문번호 WAITING 대기손님수)
    public void handleGetStatus(ClientSession session) {
        int orderId = session.getNo();
        int ahead   = queueLogic.getPeopleAhead(session);
        String Status = session.getStatus();
        broadcaster.sendTo(session,
                "STATUS " + orderId + " " + Status + " " + ahead);
    }
}



