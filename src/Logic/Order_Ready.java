package Logic;

import java.util.concurrent.*;

import DB.OrderDAOimpl;
import DB.OrderDTO;
import Session.ClientSession;
public class Order_Ready {
	/*ScheduledExecutorService는 concurrent 패키지에 포함되어 있으며, 
	 '일정 시간 후' 또는 '주기적'으로 command(작업)를 실행시켜 줄 수 있는 녀석이다.
	 블로그에서 찾아본 기능이에요*/
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(30);

    public void scheduleOrderReady(ClientSession session, ClientQueueLogic queueLogic, ServerBroadcaster broadcaster, OrderDAOimpl orderDAO) {
        // 주문 완료 후 서버가 메뉴 완료 ORDER_READY 실행해줌
        scheduler.schedule(() -> {
            try {
            	//주문 완료시 손님 db 이동 -> DoneOrder로
                int No = session.getNo();
                String Menuname = session.getName();
                //상태 업데이트 메소드 디비에서 가져오기
                orderDAO.updateStatustoDone(No);
                orderDAO.moveOrderToDone(No);
                OrderDTO orderDTO = orderDAO.getOrderByNo(No);
                //String Status = session.getStatus();
                session.setStatus(orderDTO.getStatus());
                String Status = session.getStatus();
                queueLogic.subClient(session);
                //서버 -> 클라 ORDER_READY 1101 아메리카노 (프로토콜 양식 : ORDER_READY 주문번호 주문메뉴명)
                //이부분 파싱하시면 됩니다! 주문번호랑 주문메뉴명 같이 보내니까 UI에서 주문완료 띄울 때 1101님 아메리카노 준비됐습니다
                //UI에서 주문완료 띄울 때 1101님 아메리카노 준비됐습니다 이런식으로 주문 메뉴까지 화면에 나오게 가도 될 것 같아요
                broadcaster.sendTo(session, Status + " " + No + " "+Menuname);

                // 다른 손님들에게도 대기열 보여줌 -> 손님 빠질 때마다 확인 ㄱㄴ임 
                broadcaster.broadcast(queueLogic.getClientList(), "대기열 변경 알림" );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, TimeUnit.MINUTES); // 일단은 1분 설정 하고 나중에 테스트 해보다 수정 ㄱㄱ
    }
}

