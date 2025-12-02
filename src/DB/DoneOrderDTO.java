package DB;


public class DoneOrderDTO {
	private int doneNo;
	private String doneName;
	public DoneOrderDTO() {};
	public DoneOrderDTO(int no) {
		this.doneNo= no;
	}
	public int getDoneNo() {
		return doneNo;
	}
	public void setDoneNo(int doneNo) {
		this.doneNo = doneNo;
	}
	public String getDoneName() {
		return doneName;
	}
	public void setDoneName(String doneName) {
		this.doneName = doneName;
	}
}


