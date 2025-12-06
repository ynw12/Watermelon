package Staff.ui;

import java.awt.BorderLayout;          
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;      
import java.awt.event.ActionListener;   
import java.awt.event.WindowAdapter;    
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;    
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import Staff.model.OrderItem;
import Staff.model.PickupItem;
import Staff.network.StaffConnection;
import client.model.MenuItem;
import client.model.OrderInfo;
import client.model.OrderStatus;
import client.network.ClientConnection;
import client.network.ClientMessageListener;

public class StaffFrame extends JFrame implements ClientMessageListener {
	
    // 네트워크 담당 객체
    private StaffConnection connection;

    // 주문내역 리스트(왼쪽)
    //  - 하드코딩 배열 대신 DefaultListModel로 변경
    private DefaultListModel<OrderItem> orderModel;
    private JList<OrderItem> orderList;

    // 픽업 할 리스트 (오른쪽)
    private DefaultListModel<PickupItem> pickupModel;
    private JList<PickupItem> pickupList;
    
    private JLabel lblPickupNo;
    private JTextField txtPickupNo;

    // 오른쪽 사이드바: 주문번호/대기 인원/상태 표시 라벨
    private JLabel lblOrderId;
    private JLabel lblPeopleAhead;
    private JLabel lblStatusText;
    private JTextArea txtServerLog;

    // 서버 접속 정보 입력 필드 + 버튼
    private JTextField txtHost;
    private JTextField txtPort;
    private JButton btnConnect;

    // 현재 이 클라이언트의 주문 정보
    private final OrderInfo currentOrder = new OrderInfo();

    private boolean isReceivingStaff = false;
    private boolean isReceivingStaffOk = false;


    public StaffFrame() {
        setTitle("관리자");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null); 

        initComponents();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
    }
    

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        // 최상단 서버 접속 패널
        JPanel topPanel = new JPanel();

        JLabel lblHost = new JLabel("Host:");
        txtHost = new JTextField("127.0.0.1", 10);  

        JLabel lblPort = new JLabel("Port:");
        txtPort = new JTextField("50023", 6);       
        
        btnConnect = new JButton("서버 연결");

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConnectClicked(e);
            }
        });

        topPanel.add(lblHost);
        topPanel.add(txtHost);
        topPanel.add(lblPort);
        topPanel.add(txtPort);
        topPanel.add(btnConnect);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        // 왼쪽 주문내역 패널
        JPanel orderPanel = new JPanel(new BorderLayout());
        JLabel lblOrderTitle = new JLabel("주문내역 (ordermanagement - DB 연동)", SwingConstants.CENTER);
        orderPanel.add(lblOrderTitle, BorderLayout.NORTH);

        orderModel = new DefaultListModel<OrderItem>(); 
        orderList = new JList<OrderItem>(orderModel); 
        orderPanel.add(new JScrollPane(orderList), BorderLayout.CENTER);

        centerPanel.add(orderPanel);

        // 우측 pickup 패널
        JPanel pickupPanel = new JPanel(new BorderLayout());
        JLabel lblPickupTitle = new JLabel("PICK UP 할 메뉴", SwingConstants.CENTER);
        pickupPanel.add(lblPickupTitle, BorderLayout.NORTH);

        pickupModel = new DefaultListModel<PickupItem>();
        pickupList = new JList<PickupItem>(pickupModel);
        pickupPanel.add(new JScrollPane(pickupList), BorderLayout.CENTER);

        JPanel pickupBottom = new JPanel(); 
        lblPickupNo = new JLabel("픽업 완료된 번호 : ");
       
        txtPickupNo = new JTextField("                   "); 

        JButton btnPay = new JButton("전송");
        btnPay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	pickupOk();
            }
        });

        pickupBottom.add(lblPickupNo);
        pickupBottom.add(txtPickupNo);
        pickupBottom.add(btnPay);

        pickupPanel.add(pickupBottom, BorderLayout.SOUTH);

        centerPanel.add(pickupPanel);

        getContentPane().add(centerPanel, BorderLayout.CENTER);

        // 우측 대기열&상태 사이드바 
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(280, 0));

        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        lblOrderId = new JLabel("주문번호: -");
        lblPeopleAhead = new JLabel("앞에 대기 중인 손님 수: -");
        lblStatusText = new JLabel("상태: -");
        JLabel lblLogTitle = new JLabel("서버 메시지 로그:");

        infoPanel.add(lblOrderId);
        infoPanel.add(lblPeopleAhead);
        infoPanel.add(lblStatusText);
        infoPanel.add(lblLogTitle);

        sidePanel.add(infoPanel, BorderLayout.NORTH);

        txtServerLog = new JTextArea();
        txtServerLog.setEditable(false);
        sidePanel.add(new JScrollPane(txtServerLog), BorderLayout.CENTER);

        getContentPane().add(sidePanel, BorderLayout.EAST);

    }

    // 버튼/UI 이벤트 핸들러 모음
    private void onConnectClicked(ActionEvent e) { 
        // 이미 connection이 존재한다면, 연결된 상태로 보고 해제 처리
        if (connection != null) {
            connection.sendDisconnect();
            connection = null;
            btnConnect.setText("서버 연결");
            appendLog("[STAFF] DISCONNECT 전송 및 로컬 연결 해제");
            clearOrderDisplay();
            return;
        }
        String host = txtHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "포트 번호가 올바르지 않습니다.");
            return;
        }

        connection = new StaffConnection();
        try {
            connection.connect(host, port, this);
            btnConnect.setText("연결 끊기");
            appendLog("[STAFF] 서버 연결 시도 완료 (STAFF_HELLO 전송 포함)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + ex.getMessage());
            connection = null;
        }
   
    }

    private void pickupOk() {
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "먼저 서버에 연결하세요.");
            return;
        }
        if (pickupModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
            return;
        }

        PickupItem first = pickupModel.getElementAt(0); 
        String no = Integer.toString(first.getNo());
        connection.staffPickupOk(no);

    }

    // 프로그램 종료
    private void onExit() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "프로그램을 종료하시겠습니까?",
            "종료 확인",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            if (connection != null) {
                connection.sendDisconnect();
            }
            dispose();
            System.exit(0);
        }
    }

    // ClientMessageListener
    @Override
    public void onMessage(String msg) {
        appendLog("[SERVER] " + msg);
        
        // order table display
        if (msg.startsWith("======주문내역======")) {
            // 메뉴 목록 수신 시작
        	isReceivingStaff = true;
            orderModel.clear();  // 이전 메뉴 비우기
            appendLog("[SYSTEM] 주문내역 수신 시작");
            return;
        }
        // 내용 
        if (isReceivingStaff && msg.startsWith("번호")) {
    		OrderItem parsed = parseOrderLine(msg);
            if (parsed != null) {
                orderModel.addElement(parsed);
            }
            return;
        }
        // 수신 종료 
        if (isReceivingStaff && msg.trim().isEmpty()) {
        	isReceivingStaff = false;
            appendLog("[SYSTEM] 주문내역 수신 종료"); 
            return;
        }
        
        // pickup table display
        if (msg.startsWith("======완료된 주문 내역======")) {
            // 메뉴 목록 수신 시작
        	isReceivingStaffOk = true;
        	pickupModel.clear();  // 이전 메뉴 비우기
            appendLog("[SYSTEM] 픽업 수신 시작");
            return;
        }
        // pick table 내용 
        if (isReceivingStaffOk && msg.startsWith("번호")) {
    		PickupItem parsed = parsePickupLine(msg);
            if (parsed != null) {
            	pickupModel.addElement(parsed);
            }
            return;
        }
        // pickup table 수신 종료 
        if (isReceivingStaffOk && msg.trim().isEmpty()) {
        	isReceivingStaffOk = false;
            appendLog("[SYSTEM] 픽업 수신 종료"); 
            return;
        }
        
        if (msg.startsWith("PICKUP")) {
            handlePickupCompactMessage(msg);
            return;
        }
        
        String[] parts = msg.split(" ");
        if (parts.length == 0) {
            return;
        }

        String cmd = parts[0];

        if ("Client_LOGIN".equals(cmd)) {
            JOptionPane.showMessageDialog(this, "서버 연결 완료 (Client 세션 생성)");
        } else if ("ORDER".equals(cmd)) {
            // 형식: ORDER 주문번호 WAITING 대기인원수
            handleOrderMessage(parts);
        } else if ("STATUS".equals(cmd)) {
            // 형식: STATUS 주문번호 상태 대기인원수
            handleStatusMessage(parts);
        } else if ("DONE".equals(cmd)) {
            // 형식: DONE 주문번호 메뉴명
            handleDoneMessage(parts);
        } else {
            // 그 밖의 메시지(에러 등)
            handleMiscMessage(msg);
        }
    }

    // 서버 연결 종료
    @Override
    public void onDisconnected() {
        appendLog("[SYSTEM] 서버와의 연결이 종료되었습니다.");
        btnConnect.setText("서버 연결");
        connection = null;
        clearOrderDisplay();
    }

    // 서버 메시지 처리
    private void handleOrderMessage(String[] parts) {
        if (parts.length < 4) {
            return;
        }

        int orderId = parseIntSafe(parts[1]);
        String statusStr = parts[2]; 
        int ahead = parseIntSafe(parts[3]);

        currentOrder.setOrderId(orderId);
        currentOrder.setStatus(parseStatus(statusStr));
        currentOrder.setPeopleAhead(ahead);

        updateOrderDisplay();
    }

    private void handleStatusMessage(String[] parts) {
        // STATUS 주문번호 상태 대기인원수
        if (parts.length < 4) {
            return;
        }

        int orderId = parseIntSafe(parts[1]);
        String statusStr = parts[2]; // WAITING / DONE / PICKUP
        int ahead = parseIntSafe(parts[3]);

        currentOrder.setOrderId(orderId);
        currentOrder.setStatus(parseStatus(statusStr));
        currentOrder.setPeopleAhead(ahead);

        updateOrderDisplay();
    }

    // DONE 메시지 처리
    private void handleDoneMessage(String[] parts) {
        if (parts.length < 3) {
            return;
        }

        int orderId = parseIntSafe(parts[1]);

        String menuName = joinTokensFrom(parts, 2);

        currentOrder.setOrderId(orderId);
        currentOrder.setStatus(OrderStatus.DONE);
        currentOrder.setPeopleAhead(0);

        updateOrderDisplay();

        JOptionPane.showMessageDialog(
            this,
            "주문이 완료되었습니다.\n"
          + "주문번호: " + orderId + "\n"
          + "메뉴: " + menuName + "\n"
          + "픽업대에서 음료를 수령해 주세요."
        );
    }

    // 픽업 처리
    private void handlePickupCompactMessage(String msg) {
        String prefix = "PICKUP";
        String numPart = msg.substring(prefix.length());

        int orderId = parseIntSafe(numPart);

        currentOrder.setOrderId(orderId);
        currentOrder.setStatus(OrderStatus.PICKUP);
        currentOrder.setPeopleAhead(0);

        updateOrderDisplay();

        JOptionPane.showMessageDialog(
            this,
            "주문번호 " + orderId + "번 픽업이 완료되었습니다.\n이용해 주셔서 감사합니다."
        );

        // 픽업까지 완료되면 주문 정보를 초기화하고 타이머 정지
        currentOrder.setStatus(OrderStatus.UNKNOWN);
        clearOrderDisplay();
    }

    private void handleMiscMessage(String msg) {
        if (msg.startsWith("ERROR")) {
            JOptionPane.showMessageDialog(this, "서버 오류: " + msg);
        }
    }

    // 서버에서 받아오는 메뉴 라인 파싱
    private OrderItem parseOrderLine(String line) {
        try {
            if (line == null) { 
                return null;
            }
            line = line.trim();
            if (!line.startsWith("번호")) {
                return null;
            }
            
            int noStart = line.indexOf("번호 :");
            int pipeIndex1 = line.indexOf("|");
            int nameStart = line.indexOf("메뉴 :");
            int statusStart = line.indexOf("상태 :");

            if (noStart == -1 || pipeIndex1 == -1 || nameStart == -1 || statusStart == -1) {
            	appendLog("[파싱에러] 필수 구분자 누락: " + line); 
            	return null;
            }
            
            // "번호 : " 와 "메뉴 : " 사이가 번호 
            String noPart = line.substring(noStart + "번호 :".length(), pipeIndex1).trim();
            int no = Integer.parseInt(noPart);
            // appendLog("[DEBUG] noPart 추출: " + noPart); 
            
            // "메뉴 :" 와 "상태 :" 사이가 메뉴명
            String namePart = line.substring(nameStart + "메뉴 :".length(), statusStart).trim();
            
            // "상태 :"부터 끝까지가 상태 
            String statusPart = line.substring(statusStart + "상태 :".length()).trim();

            return new OrderItem(no, namePart, statusPart); 
        } 
        catch (NumberFormatException e) {
            // 숫자 변환 오류 (noPart가 숫자가 아닐 때)
            appendLog("[파싱에러] 주문 번호 변환 오류: " + line + " - 오류: " + e.getMessage());
            return null;
        } catch (StringIndexOutOfBoundsException e) {
            // 인덱스 계산 오류
            appendLog("[파싱에러] 인덱스 범위 오류 (구분자 문제): " + line + " - 오류: " + e.getMessage());
            return null;
        } catch (Exception e) {
            appendLog("[파싱에러] 기타 파싱 실패: " + line + " - 오류: " + e.getMessage());
            return null;
        } 
//        catch (Exception e) {
//            appendLog("[파싱에러] 메뉴 라인 해석 실패: " + line);
//            return null;
//        }
    }
    
    // 서버에서 받아오는 픽업 라인 파싱
    private PickupItem parsePickupLine(String line) {
        try {
            if (line == null) { 
                return null;
            }
            line = line.trim();
            if (!line.startsWith("번호")) {
                return null;
            }
            
            int noStart = line.indexOf("번호 :");
            int pipeIndex1 = line.indexOf("|"); 
            int pipeIndex2 = line.indexOf("|"); 

            if (noStart == -1 || pipeIndex1 == -1 || doneCheck == -1) {
            	appendLog("[파싱에러] 필수 구분자 누락: " + line); 
            	return null;
            }
            
            // "번호 : " 와 "메뉴 : " 사이가 번호 
            String noPart = line.substring(noStart + "번호 :".length(), pipeIndex1).trim();
            int no = Integer.parseInt(noPart);
            // appendLog("[DEBUG] noPart 추출: " + noPart); 
            
            // " | " 와 "DONE"전까지 가 메뉴명 
            String namePart = line.substring(pipeIndex1+ "DONE".length(), doneCheck).trim();


            return new PickupItem(no, namePart); 
        } 
        catch (NumberFormatException e) {
            // 숫자 변환 오류 (noPart가 숫자가 아닐 때)
            appendLog("[파싱에러] 주문 번호 변환 오류: " + line + " - 오류: " + e.getMessage());
            return null;
        } catch (StringIndexOutOfBoundsException e) { 
            // 인덱스 계산 오류
            appendLog("[파싱에러] 인덱스 범위 오류 (구분자 문제): " + line + " - 오류: " + e.getMessage());
            return null;
        } catch (Exception e) {
            appendLog("[파싱에러] 기타 파싱 실패: " + line + " - 오류: " + e.getMessage());
            return null;
        } 
//        catch (Exception e) {
//            appendLog("[파싱에러] 메뉴 라인 해석 실패: " + line);
//            return null;
//        }
    }
    
    
    

    // 클라이언트 안내 문구 출력 관련
    private void updateOrderDisplay() {
        if (currentOrder.getOrderId() > 0) {
            lblOrderId.setText("주문번호: " + currentOrder.getOrderId());
        }
        lblPeopleAhead.setText("앞에 대기 중인 손님 수: " + currentOrder.getPeopleAhead());
        lblStatusText.setText("상태: " + currentOrder.getStatus());
    }

    private void clearOrderDisplay() {
        lblOrderId.setText("주문번호: -");
        lblPeopleAhead.setText("앞에 대기 중인 손님 수: -");
        lblStatusText.setText("상태: -");
    }

    private void appendLog(String text) {
        txtServerLog.append(text + "\n");
        txtServerLog.setCaretPosition(txtServerLog.getDocument().getLength());
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private OrderStatus parseStatus(String s) {
        if ("WAITING".equalsIgnoreCase(s)) {
            return OrderStatus.WAITING;
        }
        if ("DONE".equalsIgnoreCase(s)) {
            return OrderStatus.DONE;
        }
        if ("PICKUP".equalsIgnoreCase(s)) {
            return OrderStatus.PICKUP;
        }
        return OrderStatus.UNKNOWN;
    }

    // 배열 parts에서 startIndex부터 끝까지를 공백으로 이어붙인 문자열을 반환.
    // 예: ["DONE", "1101", "아이스", "아메리카노"], startIndex=2 → "아이스 아메리카노"
    private String joinTokensFrom(String[] parts, int startIndex) {
        if (startIndex >= parts.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i;
        for (i = startIndex; i < parts.length; i++) {
            if (i > startIndex) {
                sb.append(" ");
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
