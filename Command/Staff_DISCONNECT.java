package Command;

import java.io.IOException;

import Server.ServerWorker;

public class Staff_DISCONNECT implements Command{

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("[SERVER] STAFF DISCONNECT 요청");
	}

}
