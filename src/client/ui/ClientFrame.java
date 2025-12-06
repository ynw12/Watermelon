package client.ui;

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

import client.model.MenuItem;
import client.model.OrderInfo;
import client.model.OrderStatus;
import client.network.ClientConnection;
import client.network.ClientMessageListener;

public class ClientFrame extends JFrame implements ClientMessageListener {

    // 네트워크 담당 객체
    private ClientConnection connection;

    // 메뉴 리스트(왼쪽)
    //  - 하드코딩 배열 대신 DefaultListModel로 변경
    private DefaultListModel<MenuItem> menuModel;
    private JList<MenuItem> menuList;

    // 장바구니(오른쪽)
    private DefaultListModel<MenuItem> cartModel;
    private JList<MenuItem> cartList;
    private JLabel lblTotalPrice;

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

    // GET_STATUS 타이머
    private Timer statusTimer;

    private boolean isReceivingMenu = false;

    public ClientFrame() {
        setTitle("주문하기");
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

        // 왼쪽 메뉴 패널
        JPanel menuPanel = new JPanel(new BorderLayout());
        JLabel lblMenuTitle = new JLabel("메뉴판 (MenuBoard - DB 연동)", SwingConstants.CENTER);
        menuPanel.add(lblMenuTitle, BorderLayout.NORTH);

        menuModel = new DefaultListModel<MenuItem>();
        menuList = new JList<MenuItem>(menuModel);
        menuPanel.add(new JScrollPane(menuList), BorderLayout.CENTER);

        JButton btnAddToCart = new JButton("장바구니에 추가");
        btnAddToCart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedMenuToCart();
            }
        });
        menuPanel.add(btnAddToCart, BorderLayout.SOUTH);

        centerPanel.add(menuPanel);

        // 우측 장바구니 패널
        JPanel cartPanel = new JPanel(new BorderLayout());
        JLabel lblCartTitle = new JLabel("장바구니 & 가상 결제", SwingConstants.CENTER);
        cartPanel.add(lblCartTitle, BorderLayout.NORTH);

        cartModel = new DefaultListModel<MenuItem>();
        cartList = new JList<MenuItem>(cartModel);
        cartPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);

        JPanel cartBottom = new JPanel();
        lblTotalPrice = new JLabel("총 금액: 0원");

        JButton btnRemove = new JButton("선택 삭제");
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedFromCart();
            }
        });

        JButton btnPay = new JButton("가상 결제 / 주문 전송");
        btnPay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendOrder();
            }
        });

        cartBottom.add(lblTotalPrice);
        cartBottom.add(btnRemove);
        cartBottom.add(btnPay);

        cartPanel.add(cartBottom, BorderLayout.SOUTH);

        centerPanel.add(cartPanel);

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

        // GET_STATUS 타이머 (3초마다 출력)
        statusTimer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connection != null && currentOrder.getStatus() != OrderStatus.UNKNOWN) {
                    connection.sendGetStatus();
                    appendLog("[CLIENT] GET_STATUS 전송");
                }
            }
        });
        statusTimer.setRepeats(true);
    }

    // 버튼/UI 이벤트 핸들러 모음
    private void onConnectClicked(ActionEvent e) {
        // 이미 connection이 존재한다면, 연결된 상태로 보고 해제 처리
        if (connection != null) {
            connection.sendDisconnect();
            connection = null;
            btnConnect.setText("서버 연결");
            appendLog("[CLIENT] DISCONNECT 전송 및 로컬 연결 해제");
            statusTimer.stop();
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

        connection = new ClientConnection();
        try {
            connection.connect(host, port, this);
            btnConnect.setText("연결 끊기");
            appendLog("[CLIENT] 서버 연결 시도 완료 (CLIENT_HELLO 전송 포함)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + ex.getMessage());
            connection = null;
        }
    }

    // 장바구니 추가
    private void addSelectedMenuToCart() {
        MenuItem selected = menuList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "장바구니에 추가할 메뉴를 선택하세요.");
            return;
        }
        if (cartModel.getSize() >= 1) {
            JOptionPane.showMessageDialog(this, "장바구니에는 한 개의 메뉴만 담을 수 있습니다.");
            return;
        }
        
        cartModel.addElement(selected);
        updateTotalPrice();
    }

    // 장바구니 삭제
    private void removeSelectedFromCart() {
        int index = cartList.getSelectedIndex();
        if (index >= 0) {
            cartModel.remove(index);
            updateTotalPrice();
        }
    }

    // 장바구니에 들어간 메뉴들의 가격 합계 출력
    private void updateTotalPrice() {
        int sum = 0;
        int size = cartModel.size();
        for (int i = 0; i < size; i++) {
            sum += cartModel.getElementAt(i).getPrice();
        }
        lblTotalPrice.setText("총 금액: " + sum + "원");
    }

    private void sendOrder() {
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "먼저 서버에 연결하세요.");
            return;
        }
        if (cartModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
            return;
        }

        MenuItem first = cartModel.getElementAt(0);
        String menuName = first.getName(); // DB MenuBoard.name과 동일하게 관리하는 것이 좋음

        connection.sendNewOrder(menuName);
        appendLog("[CLIENT] NEW_ORDER " + menuName + " 전송");

        // 주문 후 상태 조회 타이머 시작
        statusTimer.start();
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

        if (msg.startsWith("======메뉴판======")) {
            // 메뉴 목록 수신 시작
            isReceivingMenu = true;
            menuModel.clear();  // 이전 메뉴 비우기
            appendLog("[SYSTEM] 메뉴판 수신 시작");
            return;
        }

        // 메뉴 내용 라인
        if (isReceivingMenu && msg.startsWith("메뉴")) {
            MenuItem parsed = parseMenuLine(msg);
            if (parsed != null) {
                menuModel.addElement(parsed);
            }
            return;
        }

        if (isReceivingMenu && msg.trim().isEmpty()) {
            isReceivingMenu = false;
            appendLog("[SYSTEM] 메뉴판 수신 종료");
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
        statusTimer.stop();
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
        statusTimer.stop();
    }

    private void handleMiscMessage(String msg) {
        if (msg.startsWith("ERROR")) {
            JOptionPane.showMessageDialog(this, "서버 오류: " + msg);
        }
    }

    // 서버에서 받아오는 메뉴 라인 파싱
    private MenuItem parseMenuLine(String line) {
        try {
            if (line == null) {
                return null;
            }
            line = line.trim();
            if (!line.startsWith("메뉴")) {
                return null;
            }

            int nameStart = line.indexOf("메뉴 :");
            int priceStart = line.indexOf("가격 :");

            if (nameStart == -1 || priceStart == -1) {
                return null;
            }

            // "메뉴 :" 와 "가격 :" 사이가 메뉴명
            String namePart =
                line.substring(nameStart + "메뉴 :".length(), priceStart).trim();

            // "가격 :" 이후부터 "원" 앞까지가 가격
            int wonIndex = line.indexOf("원", priceStart);
            String pricePart;
            if (wonIndex == -1) {
                pricePart =
                    line.substring(priceStart + "가격 :".length()).trim();
            } else {
                pricePart =
                    line.substring(priceStart + "가격 :".length(), wonIndex).trim();
            }

            int price = Integer.parseInt(pricePart);

            return new MenuItem(namePart, price);
        } catch (Exception e) {
            appendLog("[파싱에러] 메뉴 라인 해석 실패: " + line);
            return null;
        }
    }

    // 클라이언트 안내 문구 출력 관련
    private void updateOrderDisplay() {
        if (currentOrder.getOrderId() > 0) {
            lblOrderId.setText("주문번호: " + currentOrder.getOrderId());
        }
        lblPeopleAhead.setText("앞에 대기 중인 손님 수: " + currentOrder.getPeopleAhead());
        lblStatusText.setText("상태: " + currentOrder.getStatus());
        
        if (currentOrder.getStatus() == OrderStatus.DONE) {
        	if(statusTimer != null && statusTimer.isRunning()) {
        		statusTimer.stop();
        		appendLog("[SYSTEM] 주문이 완료되었습니다.");
        	}
        }
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
