package Server_v1;

public class MenuDTO { 
	// menuboard 테이블의 데이터 교환을 위한 DTO
	private String name;
	private int price;
	public MenuDTO() {}
	
	public MenuDTO(String name, int price) {
		this.name = name;
		this.price = price;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}	
}
