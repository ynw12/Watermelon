package Server_v1;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//서버 실행 담당
public class ServerMain {
	   private static final ClientQueueLogic queueLogic = new ClientQueueLogic();
	   private static final ServerBroadcaster broadcaster = new ServerBroadcaster();
	   private static final Order_Ready orderready = new Order_Ready();
	   private static final DatabaseConnector connector = new DatabaseConnector();
	   private static final OrderDAOimpl orderDAO = new OrderDAOimpl(connector);
	   private static final OrderService orderservice = new OrderService(orderDAO, queueLogic, broadcaster, orderready);

	   public static void main(String[] args) {
	        try (ServerSocket serverSocket = new ServerSocket(50023)) {
	            System.out.println("서버 시작됨");

	            while (true) {
	                Socket socket = serverSocket.accept();
	                System.out.println("클라이언트 연결됨 : " + socket.getRemoteSocketAddress());

	      
	                ServerWorker worker =
	                    new ServerWorker(socket, orderservice);

	                worker.start();
	            }
	        } catch (IOException e) {
	            System.out.println("서버 오류 발생");
	            e.printStackTrace();
	        }
	    }
	}