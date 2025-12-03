package client.model;

public class OrderInfo {
	private int orderId;
	private OrderStatus status = OrderStatus.UNKNOWN;
	private int peopleAhead;
	
	public int getOrderId() {
		return orderId;
	}
	
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	
	public OrderStatus getStatus() {
		return status;
	}
	
	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public int getPeopleAhead() {
		return peopleAhead;
	}

	public void setPeopleAhead(int peopleAhead) {
		this.peopleAhead = peopleAhead;
	}

}
