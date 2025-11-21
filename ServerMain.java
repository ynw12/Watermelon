package Server_v1;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//서버 실행 진입
public class ServerMain {
	private static final ClientQueueLogic queueLogic = new ClientQueueLogic();
	private static final ServerBroadcaster broadcaster = new ServerBroadcaster();
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(50023)) {
            System.out.println("서버 시작됨");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("클라이언트 연결됨 : "+socket.getRemoteSocketAddress());
                //각 클라이언트마다 ServerWorker 할당
                ServerWorker Worker = new ServerWorker(socket, queueLogic, broadcaster);
                
                Worker.start();
            }
        } catch (IOException e) {
            System.out.println("서버 오류 발생");
            e.printStackTrace();
        }
    }
}
