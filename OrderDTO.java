package Server_v1;

public class OrderDTO {
	 private int no;
	 private String name;
	 public OrderDTO() {};
	 public OrderDTO(String name) {
	     this.name = name;
	  }

	 public OrderDTO(int no, String name) {
	     this.no = no;
	     this.name = name;
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
		 this.name=name;
	 }
}
