package Logic;

import java.util.ArrayList;
import java.util.List;

import Session.ClientSession;

public class ClientQueueLogic {

    private final List<ClientSession> ClientNum = new ArrayList<>();

    public synchronized void addClient(ClientSession session) {
    	ClientNum.add(session);
    }

    public synchronized void subClient(ClientSession session) {
    	ClientNum.remove(session);
    }

    // 각 손님 앞에 몇 명 있는 지 바로 가능 - 손님을 매개로 받으니까
    public synchronized int getPeopleAhead(ClientSession session) {
        int idx = ClientNum.indexOf(session);  // 0이면 맨 앞이고 indexOf로 각 손님마다 처리 가능
        return idx;                        
    }
    
    public synchronized int getQueueSize() {
        return ClientNum.size();
    }
    
    public synchronized List<ClientSession> getClientList() {
        return new ArrayList<>(ClientNum); // Broadcaster가 전체에게 보낼 때 쓸 수 있음
    }
}