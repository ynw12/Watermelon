package Command;

import java.io.IOException;

import Server.ServerWorker;

public class GET_STATUS implements Command {

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		// TODO Auto-generated method stub
		if(worker.getClientSession()==null) {
			worker.getOut().println("ERROR_GET_STATUS");
			return;
		}
		worker.getOrderService().handleGetStatus(worker.getClientSession());
	}

}
