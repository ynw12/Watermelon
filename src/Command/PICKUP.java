package Command;

import java.io.IOException;

import Server.ServerWorker;

public class PICKUP implements Command {

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		// TODO Auto-generated method stub
		if(worker.getStaffSession()==null) {
			worker.getOut().println("ERROR_PICKUP");
			return;
		}
		worker.getDoneorderService().handleStaffPickup(msg);
	}

}
