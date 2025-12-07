package Staff.model;

public class OrderItem {
	private int no;
	private String name;
	private String status;
	
	public OrderItem(int no, String name, String status) {
		this.no = no;
		this.name = name;
		this.status = status;
	}

	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
    public String toString() {
        return String.format("번호: %d | 메뉴: %s | 상태: %s", no, name, status);
    }
}