package Server_v1;
import java.io.PrintWriter;
import java.util.List;

//
public class ServerBroadcaster {
	public void sendTo(ClientSession client, String msg) {
		client.getOut().println(msg);
	}
//각 클라이언트마다 메세지 보내기
    public void broadcast(List<ClientSession> session, String msg) {
        for (ClientSession client : session) {
            client.getOut().println(msg);
        }
    }
}