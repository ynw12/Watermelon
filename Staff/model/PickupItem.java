package Staff.model;

public class PickupItem {
	private int no;
	private String name;
	
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
	
	@Override
    public String toString() {
        return String.format("번호: %d | 메뉴: %s ", no, name);
    }
	public PickupItem(int no, String name) {
		super();
		this.no = no;
		this.name = name;
	}
}
