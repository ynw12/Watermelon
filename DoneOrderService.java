package Server_v1;

import java.util.List;

public class DoneOrderService {
	// private final DatabaseConnector connector; 
	private final OrderDAOimpl orderDAO;
	private final DoneOrderDAO doneOrderDAO;
	/*public DoneOrderService(DatabaseConnector connector) {
		this.connector = connector;
	}*/
	public DoneOrderService(OrderDAOimpl orderDAO,DoneOrderDAO doneOrderDAO){
		this.orderDAO = orderDAO;
		this.doneOrderDAO = doneOrderDAO;
	}
	
	// 기능1. 완료된 주문내역 전체 출력
	public void showDoneOrder() {
		List<DoneOrderDTO> doneOrderList = doneOrderDAO.getAllDoneOrders();
		for(DoneOrderDTO doDTO : doneOrderList) {
			System.out.println("======완료된 주문 내역======");
			System.out.printf(doDTO.getDoneNo()+"번 손님, "+doDTO.getDoneName()+"완료되었습니다!\n");
		}
	}

	// 기능2. pickup 처리 
	// OrderDAO로 key값(no)으로 원하는 주문내역 읽어오기 -> DoneOrderDAO로 가져온 주문내역을 doneorder 테이블에 삽입하기 (moveOrderToDone(no))
	public void pickUpDoneOrder(int no) {
		orderDAO.moveOrderToDone(no);
		System.out.println("메뉴가 완료되었습니다! 픽업대에서 음료를 픽업해주세요"); 
	}
	
	// 기능 3. pickup 후 삭제(픽업 완료된 주문 처리) 
	// 관리자 UI에서 픽업완료 신호를 주면 -> key값(no)으로 원하는 주문내역을 doneorder 테이블에서 삭제 
	public void DonePickUp(int no) {
		doneOrderDAO.deleteDoneOrder(no);	
		System.out.println("손님이 음료를 픽업해갔습니다. 주문 끝!"); 
	}
}



