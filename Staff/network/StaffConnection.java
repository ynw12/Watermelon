package Staff.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.SwingUtilities;

import client.network.ClientMessageListener;

public class StaffConnection {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private Thread readerThread;
	private boolean isConnected = false;
	private ClientMessageListener listener;
	
	// 서버와의 연결 시도 
	public void connect(
			String host, 
			int port, 
			ClientMessageListener listener
		) throws IOException {
		
		this.listener = listener;
		socket = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		out = new PrintWriter(socket.getOutputStream(), true);
		isConnected = true;
		
		startReaderThread();
		sendStaffHello();
		staffGetDone();
	}
	
	// 서버에서 오는 메시지를 계속 읽는 스레드
    private void startReaderThread() {
        readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;

                    // isConnected가 true이고, readLine()이 null이 아니면 계속 읽기
                    while (isConnected && (line = in.readLine()) != null) {

                        final String msg = line;

                        // 여기서 직접 Swing 컴포넌트를 수정하면 스레드 문제가 생길 수 있어
                        // SwingUtilities.invokeLater를 사용해 이벤트 스레드에서 수정하도록 했어요.
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onMessage(msg);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {                 
                	isConnected = false;                	

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onDisconnected();
                            }
                        }
                    });

                }
            }
        });

        readerThread.start();
    }
    
    // 서버측에서 StaffSession을 생성하도록 요청
    public void sendStaffHello() {
        sendMessage("STAFF_HELLO"); 
    }
    
    // staffGetDone 요청 
    public void staffGetDone() {
        sendMessage("STAFF_GET_DONE"); 
    }
 
    // staffGetDone 요청 
    public void staffPickupOk(String no) {
    	System.out.printf("staffPickupOk() : no = %s", no);
    	sendMessage("PICKUP "+no); 
    }
 
    
    // 서버에 문자열 한 줄을 전송
    public void sendMessage(String msg) {
        if (out != null && isConnected) {
            out.println(msg);
        }
    }
    
    // 서버와의 연결 해제
    public void sendDisconnect() {
        sendMessage("DISCONNECT");
        disconnect();
    }

    // 로컬에서의 연결 해제
    public void disconnect() {

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
}
