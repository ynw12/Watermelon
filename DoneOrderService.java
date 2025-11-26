package Server_v1;

import java.util.List;

public class DoneOrderService {
	private final DatabaseConnector connector; 
	public DoneOrderService(DatabaseConnector connector) {
		this.connector = connector;
	}
	
	// 기능1. 완료된 주문내역 전체 출력
	public void showDoneOrder(DoneOrderDAO doneOrderDAO) {
		List<DoneOrderDTO> doneOrderList = doneOrderDAO.getAllDoneOrders();
		for(DoneOrderDTO doDTO : doneOrderList) {
			System.out.println("======완료된 주문 내역======");
			System.out.printf(doDTO.getDoneNo()+"번 손님, "+doDTO.getDoneName()+"완료되었습니다!\n");
		}
	}

	// 기능2. pickup 처리 
	// OrderDAO로 key값(no)으로 원하는 주문내역 읽어오기 -> DoneOrderDAO로 가져온 주문내역을 doneorder 테이블에 삽입하기 
	public void pickUpDoneOrder() {
		
		DoneOrderDAO.insertDoneOrder();
		
	}
}