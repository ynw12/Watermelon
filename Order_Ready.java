package Server_v1;
import java.util.concurrent.*;
public class Order_Ready {
	/*ScheduledExecutorService는 concurrent 패키지에 포함되어 있으며, 
	 '일정 시간 후' 또는 '주기적'으로 command(작업)를 실행시켜 줄 수 있는 녀석이다.
	 블로그에서 찾아본 기능이에요*/
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(10);

    public void scheduleOrderReady(ClientSession session, ClientQueueLogic queueLogic, ServerBroadcaster broadcaster) {
        // 주문 완료 후 서버가 메뉴 완료 ORDER_READY 실행해줌
        scheduler.schedule(() -> {
            try {
                // 대기열에서 손님 제거
                queueLogic.subClient(session);

                // 손님한테 ORDER_READY 보냄 자세한 출력문은 완성되고 수정 ㄱㄱ
                broadcaster.sendTo(session,
                        "ORDER_READY " + session.getno());

                // 다른 손님들에게도 대기열 보여줌 -> 손님 빠질 때마다 확인 ㄱㄴ임 
                broadcaster.broadcast(queueLogic.getClientList(),
                		 "대기열 변경 알림");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, TimeUnit.MINUTES); // 일단은 1분 설정 하고 나중에 테스트 해보다 수정 ㄱㄱ
    }
}
