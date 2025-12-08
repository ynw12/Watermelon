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
    
    private JLabel lblPickupSign;

    // 오른쪽 사이드바: 서버 로그
    private JTextArea txtServerLog;

    // 서버 접속 정보 입력 필드 + 버튼
    private JTextField txtHost;
    private JTextField txtPort;
    private JButton btnConnect;

    // 업데이트된 DB 조회 타이머
    private Timer refreshTimer;
    
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
        lblPickupSign = new JLabel("픽업 완료된 주문을 선택하고 전송버튼을 누르세요.");

        JButton btnPay = new JButton("전송");
        btnPay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	pickupOk();
            }
        });

        pickupBottom.add(lblPickupSign);
        pickupBottom.add(btnPay);

        pickupPanel.add(pickupBottom, BorderLayout.SOUTH);

        centerPanel.add(pickupPanel);

        getContentPane().add(centerPanel, BorderLayout.CENTER);

        // 우측 대기열&상태 사이드바 
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(280, 0));

        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        JLabel lblLogTitle = new JLabel("서버 메시지 로그:");

        infoPanel.add(lblLogTitle);

        sidePanel.add(infoPanel, BorderLayout.NORTH);

        txtServerLog = new JTextArea();
        txtServerLog.setEditable(false);
        sidePanel.add(new JScrollPane(txtServerLog), BorderLayout.CENTER);

        getContentPane().add(sidePanel, BorderLayout.EAST);
        
        // 업데이트된 DB 조회 타이머 (20초마다 출력)
        refreshTimer = new Timer(20000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	appendLog("[STAFF] actionPerformed() 실행");
                if (connection != null) {
                    connection.sendStaffHello();
                    connection.staffGetDone();
                }
            }
        });
        refreshTimer.setRepeats(true);
    }

    // 버튼/UI 이벤트 핸들러 모음
    private void onConnectClicked(ActionEvent e) { 
        // 이미 connection이 존재한다면, 연결된 상태로 보고 해제 처리
        if (connection != null) {
            connection.sendDisconnect();
            connection = null;
            btnConnect.setText("서버 연결");
            appendLog("[STAFF] DISCONNECT 전송 및 로컬 연결 해제");
            refreshTimer.stop();
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
            // 서버 연결 후 타이머 시작
            refreshTimer.start();
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
        
        PickupItem selected = pickupList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "픽업 완료로 삭제할 주문을 선택하세요.");
            return;
        } 
        appendLog(selected.getNo() +", "+selected.getName());
        connection.staffPickupOk(String.format("%d", selected.getNo()));
        
        connection.sendStaffHello();
        connection.staffGetDone();
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
            // 주문내역 목록 수신 시작
        	isReceivingStaff = true;
            orderModel.clear();  // 이전 주문내역 비우기
            appendLog("[SYSTEM] 주문내역 수신 시작");
            return;
        }
        // order table 주문내역 내용 담기 
        if (isReceivingStaff && msg.startsWith("번호")) {
    		OrderItem parsed = parseOrderLine(msg);
            if (parsed != null) {
                orderModel.addElement(parsed);
            }
            return;
        }
        // order table 수신 종료 
        if (isReceivingStaff && msg.trim().isEmpty()) {
        	isReceivingStaff = false;
            appendLog("[SYSTEM] 주문내역 수신 종료"); 
            return;
        }
        
        // pickup table display
        if (msg.startsWith("======완료된 주문 내역======")) {
            // pickup 목록 수신 시작
        	isReceivingStaffOk = true;
        	pickupModel.clear();  // 이전 pickup 비우기
            appendLog("[SYSTEM] 픽업 수신 시작");
            return;
        }
        // pick table 내용 담기
        if (isReceivingStaffOk && msg.startsWith("완료")) {
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
        
        String[] parts = msg.split(" ");
        if (parts.length == 0) {
            return;
        }

        String cmd = parts[0];

        //그 밖의 메시지(에러 등)
        handleMiscMessage(msg);
    }

    // 서버 연결 종료
    @Override
    public void onDisconnected() {
        appendLog("[SYSTEM] 서버와의 연결이 종료되었습니다.");
        btnConnect.setText("서버 연결");
        connection = null;
        refreshTimer.stop();
    }

    private void handleMiscMessage(String msg) {
        if (msg.startsWith("ERROR")) {
            JOptionPane.showMessageDialog(this, "서버 오류: " + msg);
        }
    }

    // 서버에서 받아오는 주문내역 라인 파싱
    private OrderItem parseOrderLine(String line) {
        try {
            if (line == null) { 
                return null;
            }
            line = line.trim();
            if (!line.startsWith("번호")) {
                return null;
            }
            
            int noStart = line.indexOf("번호 : ");
            int Index1 = line.indexOf("+");
            int Index2 = line.indexOf("|");
            // appendLog("[DEBUG]" + String.format("%d, %d, %d", noStart, Index1, Index2));
            
            if (noStart == -1 || Index1 == -1 || Index2 == -1) {
            	appendLog("1 [파싱에러] 필수 구분자 누락: " + line); 
            	return null;
            }
            
            // "번호 : " 와 "메뉴 : " 사이가 번호 
            String noPart = line.substring(noStart + "번호 : ".length(), Index1).trim();
            int no = Integer.parseInt(noPart);
            
            // "메뉴 :" 와 "상태 :" 사이가 메뉴명
            String namePart = line.substring(Index1 +1, Index2).trim();
            // appendLog("[DEBUG]" + namePart);
            
            // "상태 :"부터 끝까지가 상태 
            String statusPart = line.substring(Index2 +1).trim();

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
            appendLog("[파싱에러] 메뉴 라인 해석 실패: " + line + " - 오류: " + e.getMessage());
            return null;
        } 
    }
    
    // 서버에서 받아오는 픽업 라인 파싱 
    private PickupItem parsePickupLine(String line) {
        try {
            if (line == null) {
                return null;
            }
            line = line.trim();
            if (!line.startsWith("완료")) {
                return null;
            }
            
            int noStart = line.indexOf("완료 : ");
            int pipeIndex1 = line.indexOf("|");

            if (noStart == -1 || pipeIndex1 == -1) {
            	appendLog("[파싱에러] 필수 구분자 누락: " + line);
            	return null;
            }
            
            // "번호 : " 와 "메뉴 : " 사이가 번호 
            String noPart = line.substring(noStart + "완료 : ".length(), pipeIndex1).trim();
            int no = Integer.parseInt(noPart);
            // appendLog("[DEBUG] noPart 추출: " + noPart); 
            
            // " | " 와 "DONE"전까지 가 메뉴명 
            String namePart = line.substring(pipeIndex1 +1).trim();
            // appendLog("[DEBUG]" + namePart);

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
            appendLog("[파싱에러] 메뉴 라인 해석 실패: " + line + " - 오류: " + e.getMessage());
            return null;
        } 
    }

    private void appendLog(String text) {
        txtServerLog.append(text + "\n");
        txtServerLog.setCaretPosition(txtServerLog.getDocument().getLength());
    }

}
