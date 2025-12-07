package Logic;

import java.io.PrintWriter;
import java.util.List;

import Session.Session;

public class ServerBroadcaster {
	public void sendTo(Session client, String msg) {
		client.getOut().println(msg);
	}
//각 클라이언트마다 메세지 보내기
     public void broadcast(List<? extends Session> session, String msg) {
        for (Session s : session) {
            s.getOut().println(msg);
        }
    }
}
