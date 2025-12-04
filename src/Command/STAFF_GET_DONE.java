package Command;

import java.io.IOException;

import Server.ServerWorker;

public class STAFF_GET_DONE implements Command {

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		// TODO Auto-generated method stub
		if(worker.getStaffSession()==null) {
			worker.getOut().println("ERROR_STAFF_GET_DONE");
			return;
		}
		worker.getDoneorderService().showDoneOrder(worker.getStaffSession());
	}

}
