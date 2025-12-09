package client.ui.utils;

import client.model.OrderInfo;

public class ClientMessageHandler {
	
	private ClientMessageHandler() {}
	
	public static boolean applyOrderMessage(String[] parts, OrderInfo order) {
		if (parts == null || order == null) {
			return false;
		}
		
		if(parts.length < 4) {
			return false;
		}
		
		int orderId = ProtocolParser.parseInt(parts[1]);
		String status = parts[2];
		int peopleAhead = ProtocolParser.parseInt(parts[3]);
		
		order.setOrderId(orderId);
		order.setStatus(ProtocolParser.parseStatus(status));
		order.setPeopleAhead(peopleAhead);
		
		return true;
	}

}
