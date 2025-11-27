package Server_v1;

import java.util.List;

public class DoneOrderService {
	// private final DatabaseConnector connector; 
	private final OrderDAOimpl orderDAO;
	private final DoneOrderDAO doneOrderDAO;
	private final ServerBroadcaster broadcaster;
	/*public DoneOrderService(DatabaseConnector connector) {
		this.connector = connector;
	}*/
	public DoneOrderService(OrderDAOimpl orderDAO,DoneOrderDAO doneOrderDAO,ServerBroadcaster broadcaster){
		this.orderDAO = orderDAO;
		this.doneOrderDAO = doneOrderDAO;
		this.broadcaster = broadcaster;
	}
	
	
	// 기능1. 완료된 주문내역 전체 출력
	public void showDoneOrder(StaffSession staff) {
		List<DoneOrderDTO> doneOrderList = doneOrderDAO.getAllDoneOrders();
		broadcaster.sendTo(staff, "======완료된 주문 내역======");
		for(DoneOrderDTO doDTO : doneOrderList) {
			broadcaster.sendTo(staff, doDTO.getDoneNo()+"번 손님, "+doDTO.getDoneName()+"완료되었습니다!\n");
		}
	}
	// 기능 2. 픽업 후 삭제 (기존 기능 2 - (손님한테 픽업 알리고) doneorder로 손님 옮기는 코드를 order_ready로 이동시킴)
	public void handleStaffPickup(String msg) {
        String[] parts = msg.split(" ");
        int no = Integer.parseInt(parts[1]);
        orderDAO.updateStatustoPickUp(no);
        // OrderDTO orderDTO = orderDAO.getOrderByNo(no);
        
        //client.setStatus(orderDTO.getStatus());
        //String Status = client.getStatus();
        doneOrderDAO.deleteDoneOrder(no);
        //client.setNo(no); 
        //broadcaster.sendTo(client, Status + no);
	}
	
	
//	// 기능 3. 관리자가 직접 픽완료된 주문 삭제
//	public void handleDeletePickup(int no) {
//		System.out.println("관리자가 직접 pickup된 메뉴를 삭제하겠습니다. ");
//		doneOrderDAO.deleteDoneOrder(no);
//	}
}




