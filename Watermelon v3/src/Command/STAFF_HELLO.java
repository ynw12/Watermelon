package Command;

import java.io.IOException;
import Server.ServerWorker;
import Session.StaffSession;

public class STAFF_HELLO implements Command {

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		StaffSession staffSession = new StaffSession(worker.getOut());
		// TODO Auto-generated method stub
		worker.setStaffSession(staffSession);
        worker.getOut().println("STAFF_LOGIN");
        System.out.println("[SERVER] Staff 세션 생성");
        
        worker.getOrderService().showAllOrders(worker.getOut());
	}

}
