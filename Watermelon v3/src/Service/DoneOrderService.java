package Service;

import java.util.List;

import DB.DoneOrderDAO;
import DB.DoneOrderDTO;
import DB.OrderDAOimpl;
import Logic.ServerBroadcaster;
import Session.StaffSession;

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
			broadcaster.sendTo(staff, "번호 : " + doDTO.getDoneNo()+" | "+doDTO.getDoneName()+" | DONE\n");
		}
	}
	// 기능 2. 픽업 후 삭제 (기존 기능 2 - (손님한테 픽업 알리고) doneorder로 손님 옮기는 코드를 order_ready로 이동시킴)
	public void handleStaffPickup(String msg) {
        String[] parts = msg.split(" ");
        int no = Integer.parseInt(parts[1]);
        orderDAO.updateStatustoPickUp(no);
        doneOrderDAO.deleteDoneOrder(no);
	}

}





