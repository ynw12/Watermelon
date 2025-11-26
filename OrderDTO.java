package Server_v1;

public class OrderDTO {
	 private int no;
	 private String name;
	 private String status;
	
	 public OrderDTO() {};
	 public OrderDTO(String name) {
	     this.name = name;
	  }
	 public OrderDTO(int no, String name) {
	     this.no = no;
	     this.name = name;
	 }
	 public OrderDTO(int no, String name, String status) {
		 super();
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
		 this.name=name;
	 }
	 public String getStatus() {
			return status;
	 }
	 public void setStatus(String status) {
		this.status = status;
	 }
}

