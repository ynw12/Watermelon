package client.ui.utils;

import client.model.*;

public class ProtocolParser {
	private ProtocolParser() {}
	
	public static boolean isMenuHeader(String msg) {
		return msg != null && msg.startsWith("======메뉴판======");
	}
	
	public static boolean isMenuLine(String msg) {
		return msg != null && msg.trim().startsWith("메뉴");
	}
	
	public static boolean isMenuEnd(String msg) {
		return msg != null && msg.trim().isEmpty();
	}
	
    // 서버에서 받아오는 메뉴 라인 파싱
    public static MenuItem parseMenuLine(String line) {
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

            // 유효성 검사
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
        	e.printStackTrace();
            return null;
        }
    }
    
    public static int parseInt(String s) {
    	try {
    		return Integer.parseInt(s);
    	} catch (NumberFormatException e) {
    		return 0;
    	}
    }
    
    public static OrderStatus parseStatus(String s) {
    	if (s == null) {
    		return OrderStatus.UNKNOWN;
    	}
    	if("WAITING".equalsIgnoreCase(s)) {
    		return OrderStatus.WAITING;
    	}
    	if("DONE".equalsIgnoreCase(s)) {
    		return OrderStatus.DONE;
    	}
    	if("PICKUP".equalsIgnoreCase(s)) {
    		return OrderStatus.PICKUP;
    	}
    	return OrderStatus.UNKNOWN;
		
    }
    
    public static String joinTokensFrom(String[] parts, int startIndex) {
        if (parts == null || startIndex >= parts.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < parts.length; i++) {
            if (i > startIndex) {
                sb.append(" ");
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

}

