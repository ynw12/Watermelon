package Session;

import java.io.PrintWriter;

public class StaffSession implements Session{
	private final PrintWriter out;
	//@Override
	public StaffSession (PrintWriter out) {
		this.out = out;
	}
	public PrintWriter getOut() {
		// TODO Auto-generated method stub
		return out;
	}
	
}
