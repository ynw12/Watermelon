package Command;

import java.io.IOException;

import Server.ServerWorker;

public interface Command {
	
	void execute(String msg, ServerWorker worker) throws IOException;
}
