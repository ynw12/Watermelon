package Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import DB.DatabaseConnector;
import DB.DoneOrderDAO;
import DB.MenuDAO;
import DB.OrderDAOimpl;
import Logic.ClientQueueLogic;
import Logic.Order_Ready;
import Logic.ServerBroadcaster;
import Service.DoneOrderService;
import Service.MenuService;
import Service.OrderService;
//서버 실행 담당
public class ServerMain {
	   private static final ClientQueueLogic queueLogic = new ClientQueueLogic();
	   private static final ServerBroadcaster broadcaster = new ServerBroadcaster();
	   private static final Order_Ready orderready = new Order_Ready();
	   private static final DatabaseConnector connector = new DatabaseConnector();
	   private static final OrderDAOimpl orderDAO = new OrderDAOimpl(connector);
	   private static final DoneOrderDAO doneorderDAO = new DoneOrderDAO(connector);
	   private static final MenuDAO menuDAO = new MenuDAO(connector);
	   private static final OrderService orderservice = new OrderService(orderDAO, queueLogic, broadcaster,orderready);
	   private static final DoneOrderService doneorderservice = new DoneOrderService(orderDAO, doneorderDAO, broadcaster);
	   private static final MenuService menuservice = new MenuService(menuDAO);
	   

	   public static void main(String[] args) {
	        try (ServerSocket serverSocket = new ServerSocket(50023)) {
	            System.out.println("서버 시작됨");

	            while (true) {
	                Socket socket = serverSocket.accept();
	                System.out.println("클라이언트 연결됨 : " + socket.getRemoteSocketAddress());

	      
	                ServerWorker worker =
	                    new ServerWorker(socket, orderservice, doneorderservice, menuservice);

	                worker.start();
	            }
	        } catch (IOException e) {
	            System.out.println("서버 오류 발생");
	            e.printStackTrace();
	        }
	    }
	}
