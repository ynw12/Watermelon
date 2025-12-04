package Command;

import java.io.IOException;

import Server.ServerWorker;
import Session.ClientSession;

public class CLIENT_HELLO implements Command {

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		// TODO Auto-generated method stub
		ClientSession clientsession = new ClientSession(worker.getOut());
		worker.setClientSession(clientsession);
		worker.getOut().println("Client_Login");
		
		worker.getMenuService().showAllMenus(worker.getOut());
	}

}
