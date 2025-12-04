package Command;

import java.io.IOException;

import Server.ServerWorker;

public class NEW_ORDER implements Command {

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		// TODO Auto-generated method stub
		if(worker.getClientSession()==null) {
			worker.getOut().println("ERROR_NEW_ORDER");
			return;
		}
		worker.getOrderService().handleNewOrder(msg, worker.getClientSession(), worker.getOut());
	}

}
