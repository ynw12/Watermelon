package Command;

import java.io.IOException;

import Server.ServerWorker;

public class Client_DISCONNECT implements Command{

	@Override
	public void execute(String msg, ServerWorker worker) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("[SERVER] 클라이언트 DISCONNECT 요청");
	}

}
