package client.network;

public interface ClientMessageListener {
	void onMessage(String msg);
	
	void onDisconnected();
}
